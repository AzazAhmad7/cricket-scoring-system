package com.cricket.scoring.services.Impl;

import com.cricket.scoring.dtos.*;
import com.cricket.scoring.dtos.ResponseFiles.*;
import com.cricket.scoring.entities.*;
import com.cricket.scoring.entities.Team;
import com.cricket.scoring.entities.Venue;
import com.cricket.scoring.entities.enums.*;
import com.cricket.scoring.exceptions.ResourceNotFoundException;
import com.cricket.scoring.exceptions.RuntimeConflictException;
import com.cricket.scoring.repositories.BatterStatsRepository;
import com.cricket.scoring.repositories.MatchRepository;
import com.cricket.scoring.repositories.MatchSquadRepository;
import com.cricket.scoring.repositories.PointsTableRepository;
import com.cricket.scoring.services.*;
import com.cricket.scoring.utils.Util;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final ModelMapper modelMapper;
    private final TeamService teamService;
    private final VenueService venueService;
    private final MatchSquadRepository matchSquadRepository;
    private final JsonFileService jsonFileService;
    private final PlayerService playerService;
    private final TournamentService tournamentService;
    private final BatterStatsService batterStatsService;
    private final BowlerStatsService bowlerStatsService;
    private final PointsTableRepository pointsTableRepository;

    @Override
    public MatchAllData createMatch(CreateMatchRequest request) {
        Match match = createMatchEntity(request);
        match.setStatus(MatchStatus.SCHEDULED);
        Match savedMatch = matchRepository.save(match);
        Squad squad = buildSquad(request);
        saveMatchSquad(savedMatch, squad);
        SetupFile setupFile = buildSetupFile(savedMatch, squad);
        setupFile.getMatchInfo().setStatus(MatchStatus.SCHEDULED);
        jsonFileService.createSetupFile(setupFile);
        MatchState matchState = startMatch(setupFile,null, 1, MatchStatus.SCHEDULED);
        EventFile eventFile = new EventFile();
        jsonFileService.createEventFile(matchState, eventFile);
        jsonFileService.loadMatch(setupFile.getMatchInfo().getMatchId());
        return jsonFileService.initializeMatch(matchState, setupFile, eventFile, setupFile.getMatchInfo().getMatchId());
    }

    @Override
    public MatchAllData updateMatchStatus(Long matchId, MatchStatus matchStatus) {
        MatchAllData matchAllData = jsonFileService.loadMatch(matchId);
        SetupFile setup = matchAllData.getSetupFile();
        MatchState matchState = matchAllData.getMatchState();
        if(!setup.getMatchInfo().getMatchId().equals(matchId)){
            throw new ResourceNotFoundException("Match not found");
        }
        if(setup.getMatchInfo().getStatus() == matchStatus){
            throw new RuntimeConflictException("Match Status is already "+matchStatus);
        }
        setup.getMatchInfo().setStatus(matchStatus);
        matchState.setMatchStatus(matchStatus);
        matchAllData.setSetupFile(setup);
        jsonFileService.updateSetUpFile(setup);
        jsonFileService.updateStateFile(matchState);
        return matchAllData;
    }

    @Override
    public MatchAllData changeInning(Long matchId) {
        MatchAllData matchAllData = jsonFileService.loadMatch(matchId);
        MatchState matchState = matchAllData.getMatchState();
        SetupFile setupFile = matchAllData.getSetupFile();
        if(matchState.getMatchStatus() != MatchStatus.INNINGS_BREAK) throw new RuntimeConflictException("Pause the match to change inning");
        int nextInning = 0;
        if(setupFile.getMatchInfo().getFormat().equals(MatchFormat.T20) || setupFile.getMatchInfo().getFormat().equals(MatchFormat.ODI)){
            nextInning = 3 - matchState.getCurrentInningNumber();
        }


        if(matchState.getScoreCard().getInnings().size() == 1){
            startMatch(setupFile, matchState,nextInning, MatchStatus.INNINGS_BREAK);
        }else{
            Long battingTeamId = setupFile.getTeams().getBattingTeamId().equals(setupFile.getTeams().getHomeTeam().getId())
                    ? setupFile.getTeams().getAwayTeam().getId()
                    : setupFile.getTeams().getHomeTeam().getId();
            matchState.setBattingTeamId(battingTeamId);
            matchState.setCurrentInningNumber(nextInning);
            jsonFileService.updateStateFile(matchState);
        }
        return matchAllData;
    }

    @Override
    public MatchAllData getMatchAllData(Long matchId){
        if(!matchRepository.existsById(matchId)) throw new ResourceNotFoundException("Match does not exist with id "+matchId);

        return jsonFileService.loadMatch(matchId);
    }

    @Override
    public MatchState startMatch(SetupFile setupFile, MatchState matchState, Integer inningNumber, MatchStatus matchStatus) {
        ScoreSummary scoreSummary = createScoreSummary();
        Extras extras = createExtras();

        Long battingTeamId = setupFile.getTeams().getBattingTeamId();
        BattingCard battingCard = createBattingCard(setupFile, battingTeamId);

        BowlingCard bowlingCard = BowlingCard.builder()
                .bowlers(new ArrayList<>())
                .build();
        PartnershipCard partnershipCard = PartnershipCard.builder()
                .partnerships(new ArrayList<>())
                .build();
        OverProgression overProgression = OverProgression.builder()
                .overs(null)
                .build();

        InningControlMetrices inningControlMetrices = createInningControlMetrices();

        FallOfWicket fallOfWicket = createFallOfWickets();
        List<Inning> innings = createInnings(matchState,inningNumber,battingTeamId,scoreSummary,extras,battingCard,bowlingCard,partnershipCard,overProgression, inningControlMetrices);

        ScoreCard scoreCard = ScoreCard.builder()
                .innings(innings)
                .build();

        if(matchState == null){
            matchState = createMatchState(setupFile, inningNumber, scoreCard, battingTeamId);
        }else{
            matchState.setCurrentInningNumber(inningNumber);
            matchState.setBattingTeamId(battingTeamId);
            matchState.getScoreCard().getInnings().get(inningNumber-1).setNextBattingPosition(1);
            matchState.getScoreCard().getInnings().get(inningNumber-1).setStrikerId(null);
            matchState.getScoreCard().getInnings().get(inningNumber-1).setNonStrikerId(null);
            matchState.getScoreCard().getInnings().get(inningNumber-1).setCurrentBowlerId(null);
            matchState.setScoreCard(scoreCard);
            matchState.setMatchStatus(matchStatus);
        }
        jsonFileService.createMatchStateFile(matchState);
        return matchState;
    }

    @Override
    public SetupFile endMatch(MatchState matchState, SetupFile setupFile) {
        PlayerStats playerStats = setupFile.getPlayerStats();
        List<BatterStatsDTO> hTeamBatterStats = playerStats.getBattingStats().getHomeTeamPlayersStats();
        List<BatterStatsDTO> aTeamBatterStats = playerStats.getBattingStats().getAwayTeamPlayersStats();
        List<BowlerStatsDTO> hTeamBowlerStats = playerStats.getBowlingStats().getHomeTeamPlayerStats();
        List<BowlerStatsDTO> aTeamBowlerStats = playerStats.getBowlingStats().getAwayTeamPlayerStats();

        Long battingTeamId;

        for(Inning inning : matchState.getScoreCard().getInnings()){
            if(inning.getInningNumber() == 1){
                battingTeamId = setupFile.getToss().getBattingTeamId();
            }else{
                battingTeamId = setupFile.getToss().getBowlingTeamId();
            }

            if(battingTeamId.equals(setupFile.getTeams().getHomeTeam().getId())){
                updateBattingStatsForTeam(inning.getBattingCard().getBatters(), hTeamBatterStats);
                updateBowlingStatsForTeam(inning.getBowlingCard().getBowlers(), aTeamBowlerStats);
            }else{
                updateBattingStatsForTeam(inning.getBattingCard().getBatters(),aTeamBatterStats);
                updateBowlingStatsForTeam(inning.getBowlingCard().getBowlers(), hTeamBowlerStats);
            }
        }
        persistBatterAndBowlerStats(hTeamBatterStats, hTeamBowlerStats);
        persistBatterAndBowlerStats(aTeamBatterStats, aTeamBowlerStats);
        playerStats.getBattingStats().setHomeTeamPlayersStats(hTeamBatterStats);
        playerStats.getBattingStats().setAwayTeamPlayersStats(aTeamBatterStats);
        playerStats.getBowlingStats().setHomeTeamPlayerStats(hTeamBowlerStats);
        playerStats.getBowlingStats().setAwayTeamPlayerStats(aTeamBowlerStats);
        setupFile.setPlayerStats(playerStats);
        jsonFileService.updateSetUpFile(setupFile);
        return setupFile;
    }

    private void persistBatterAndBowlerStats(List<BatterStatsDTO> batterStatsDTOS, List<BowlerStatsDTO> bowlerStatsDTOS){
        for(BatterStatsDTO batterStatsDTO : batterStatsDTOS){
            batterStatsService.addBatterStats(batterStatsDTO);
        }
        for(BowlerStatsDTO bowlerStatsDTO : bowlerStatsDTOS){
            bowlerStatsService.addBowlerStats(bowlerStatsDTO);
        }
    }



    private void updateBattingStatsForTeam(List<BatterCard> batterCards, List<BatterStatsDTO> playerStats) {

        Map<Long, BatterStatsDTO> statsMap = playerStats.stream()
                        .collect(Collectors.toMap(
                                BatterStatsDTO::getPlayerId,
                                Function.identity()
                        ));

        for(BatterCard card : batterCards){
            BatterStatsDTO stats = statsMap.get(card.getBatter().getPlayerId());
            if(stats != null){
                mergeBatterStats(stats, card);
            }
        }
    }
    private void updateBowlingStatsForTeam(List<BowlerCard> bowlerCards, List<BowlerStatsDTO> playerStats) {

        Map<Long, BowlerStatsDTO> statsMap = playerStats.stream()
                .collect(Collectors.toMap(
                        BowlerStatsDTO::getPlayerId,
                        Function.identity()
                ));

        for(BowlerCard card : bowlerCards){
            BowlerStatsDTO stats = statsMap.get(card.getBowler().getPlayerId());
            if(stats != null){
                mergeBowlerStats(stats, card);
            }
        }
    }

    private void mergeBatterStats(BatterStatsDTO stats, BatterCard card) {
        ScoringStats scoring = card.getScoring();
        if(scoring != null){
            int matchRuns = card.getScoring().getRuns() == null ? 0 : card.getScoring().getRuns();
            int matchBalls = card.getScoring().getBalls() == null ? 0 : card.getScoring().getBalls();
            int matchFours = card.getScoring().getFours() == null ? 0 : card.getScoring().getFours();
            int matchSixes = card.getScoring().getSixes() == null ? 0 : card.getScoring().getSixes();

            // MATCHES
            stats.setMatches(stats.getMatches() + 1);

            // INNINGS
            boolean batted = matchBalls > 0 || (card.getDismissal().getStatus() != null && card.getDismissal().getStatus() != BattingStatus.STILL_TO_BAT);

            if(batted){
                stats.setInnings(stats.getInnings() + 1);
            }

            // RUNS
            stats.setRuns(stats.getRuns() + matchRuns);

            // BALLS
            stats.setBalls(stats.getBalls() + matchBalls);

            // FOURS
            stats.setFours(stats.getFours() + matchFours);

            // SIXES
            stats.setSixes(stats.getSixes() + matchSixes);

            // NOT OUTS
            if(card.getDismissal().getStatus() == BattingStatus.NOT_OUT|| card.getDismissal().getStatus() == BattingStatus.RETIRED_HURT){

                stats.setNotOuts(stats.getNotOuts() + 1);
            }

            // DUCKS
            if(matchRuns == 0 && matchBalls > 0 && card.getDismissal().getStatus() == BattingStatus.OUT){

                stats.setDucks(stats.getDucks() + 1);
            }

            // FIFTIES / HUNDREDS
            if(matchRuns >= 100){

                stats.setHundreds(stats.getHundreds() + 1);

            } else if(matchRuns >= 50){

                stats.setFifties(stats.getFifties() + 1);
            }

            // HIGHEST SCORE
            stats.setHighestScore(Math.max(stats.getHighestScore(), matchRuns));

            // STRIKE RATE
            if(stats.getBalls() > 0){
                stats.setStrikeRate((stats.getRuns() * 100.0)/ stats.getBalls());
            }

            // AVERAGE
            int dismissals = stats.getInnings() - stats.getNotOuts();

            if(dismissals > 0){
                stats.setAverage(stats.getRuns() * 1.0/ dismissals);
            } else {
                stats.setAverage((double) stats.getRuns());
            }
        }

    }
    private void mergeBowlerStats(BowlerStatsDTO stats, BowlerCard card) {

        int matchWickets = card.getWickets();
        int matchBalls = card.getTotalLegalDeliveriesBowled();
        int matchRuns = card.getRunsConceded();
        int matchMaidens = card.getMaidens();

        // INNINGS BOWLED
        if(matchBalls > 0){
            stats.setInnings(stats.getInnings() + 1);
        }

        // WICKETS
        stats.setWickets(stats.getWickets() + matchWickets);

        // BALLS BOWLED
        stats.setBallsBowled( stats.getBallsBowled() + matchBalls);

        // RUNS CONCEDED
        stats.setRunsConceded(stats.getRunsConceded() + matchRuns);

        // MAIDENS
        stats.setMaidens(stats.getMaidens() + matchMaidens);

        // 3 WICKET HAUL
        if(matchWickets >= 3){
            stats.setThreeWicketHauls(stats.getThreeWicketHauls() + 1);
        }

        // 5 WICKET HAUL
        if(matchWickets >= 5){
            stats.setFiveWicketHauls(stats.getFiveWicketHauls() + 1);
        }

        // BEST BOWLING FIGURES
        boolean betterFigures = matchWickets > stats.getBestBowlingWickets()
                || (matchWickets == stats.getBestBowlingWickets() && (stats.getBestBowlingRuns() == 0 || matchRuns < stats.getBestBowlingRuns()));

        if(betterFigures){
            stats.setBestBowlingWickets(matchWickets);
            stats.setBestBowlingRuns(matchRuns);
        }

        // ECONOMY
        if(stats.getBallsBowled() > 0){
            double overs =stats.getBallsBowled() / 6.0;
            stats.setEconomy(stats.getRunsConceded() / overs);
        }

        // BOWLING AVERAGE
        if(stats.getWickets() > 0){
            stats.setAverage(stats.getRunsConceded() * 1.0 / stats.getWickets());
        } else {
            stats.setAverage(0.0);
        }

        // STRIKE RATE
        if(stats.getWickets() > 0){
            stats.setStrikeRate(stats.getBallsBowled() * 1.0 / stats.getWickets());
        } else {
            stats.setStrikeRate(0.0);
        }
    }

    @Override
    public MatchDTO getMatch(Long matchId) {
        if(!matchRepository.existsById(matchId)) throw new ResourceNotFoundException("Match not found with id "+matchId);
        return modelMapper.map(matchRepository.findById(matchId), MatchDTO.class);
    }

    @Override
    public List<MatchDTO> getAllMatches() {
        List<Match> matches = matchRepository.findAll();
        List<MatchDTO> matchDTOS = new ArrayList<>();
        for(Match match : matches) {
            matchDTOS.add(modelMapper.map(match, MatchDTO.class));
        }
        System.out.println(matchDTOS);
        return matchDTOS;
    }

    @Override
    @Transactional
    public SetupFile updateMatch(Long matchId,CreateMatchRequest request) {

        Match existingMatch = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found" ));

        // UPDATE EXISTING ENTITY
        updateMatchEntity(existingMatch, request);

        Match savedMatch =matchRepository.save(existingMatch);

        // DELETE OLD SQUADS
        matchSquadRepository.deleteByMatchId(matchId);

        // CREATE NEW SQUADS
        Squad squad = buildSquad(request);

        saveMatchSquad(savedMatch, squad);

        // UPDATE SETUP JSON
        SetupFile setupFile =buildSetupFile(savedMatch, squad);

        jsonFileService.updateSetUpFile(setupFile);

        return setupFile;
    }

    @Override
    public void deleteMatch(Long matchId) {
        matchRepository.deleteById(matchId);
    }

    @Override
    public Boolean matchExists(Long matchId) {
        return matchRepository.existsById(matchId);
    }

    @Override
    public void assignSquads() {

    }

    @Override
    public SetupFile getMatchSetup(Long matchId){
        return jsonFileService.loadSetupFile(matchId);
    }

    public SetupFile buildSetupFile(Match savedMatch, Squad squad){
        com.cricket.scoring.dtos.ResponseFiles.Venue fileVenue = com.cricket.scoring.dtos.ResponseFiles.Venue.builder()
                .id(savedMatch.getVenue().getId())
                .name(savedMatch.getVenue().getName())
                .city(savedMatch.getVenue().getCity())
                .build();

        Rules rules = Rules.builder()
                .overs(savedMatch.getTotalOvers())
                .ballsPerOver(savedMatch.getBallsPerOver())
                .powerPlayStartOver(savedMatch.getPowerplayStartOver())
                .powerPlayEndOver(savedMatch.getPowerplayEndOver())
                .drsEnabled(savedMatch.getDrsEnabled())
                .reviewPerTeam(savedMatch.getReviewsPerTeam())
                .superOverEnabled(savedMatch.getSuperOverEnabled())
                .dlsEnabled(savedMatch.getDlsEnabled())
                .impactPlayerEnabled(savedMatch.getImpactPlayerEnabled())
                .build();

        List<Player> hPlayers = new ArrayList<>();
        List<Player> aPlayers = new ArrayList<>();
        List<Player> allPlayers = playerService.getAllPlayers();


        List<BatterStatsDTO> hBatterStatsList = new ArrayList<>();
        List<BatterStatsDTO> aBatterStatsList = new ArrayList<>();
        List<BowlerStatsDTO> hBowlerStatsList = new ArrayList<>();
        List<BowlerStatsDTO> aBowlerStatsList = new ArrayList<>();

        for(Player player : allPlayers){
            if(player.getTeam() == null){
                continue;
            }
            if(player.getTeam().getId().equals(savedMatch.getHomeTeam().getId())){
                hPlayers.add(player);
                if(batterStatsService.statsExistByPlayerId(player.getId())){
                    hBatterStatsList.addAll(batterStatsService.getAllBatterStatsByPlayerId(player.getId()));
                }else{
                    BatterStatsDTO bs = new BatterStatsDTO();
                    bs.setPlayerId(player.getId());
                    bs.setTournamentId(savedMatch.getCompetition());
                    BatterStatsDTO batStat = batterStatsService.addBatterStats(bs);
                    hBatterStatsList.add(batStat);
                }
                if(bowlerStatsService.statsExistByPlayerId(player.getId())){
                    hBowlerStatsList.addAll(bowlerStatsService.getAllBowlerStatsByPlayerId(player.getId()));
                }else{
                    BowlerStatsDTO bs = new BowlerStatsDTO();
                    bs.setPlayerId(player.getId());
                    bs.setTournamentId(savedMatch.getCompetition());
                    BowlerStatsDTO bowlStats = bowlerStatsService.addBowlerStats(bs);
                    hBowlerStatsList.add(bowlStats);
                }
            }
        }
        for(Player player : allPlayers){
            if(player.getTeam() == null){
                continue;
            }
            if(player.getTeam().getId().equals(savedMatch.getAwayTeam().getId())){
                aPlayers.add(player);
                if(batterStatsService.statsExistByPlayerId(player.getId())){
                    aBatterStatsList.addAll(batterStatsService.getAllBatterStatsByPlayerId(player.getId()));
                }else{
                    BatterStatsDTO bs = new BatterStatsDTO();
                    bs.setPlayerId(player.getId());
                    bs.setTournamentId(savedMatch.getCompetition());
                    BatterStatsDTO batStat = batterStatsService.addBatterStats(bs);
                    aBatterStatsList.add(batStat);
                }
                if(bowlerStatsService.statsExistByPlayerId(player.getId())){
                    aBowlerStatsList.addAll(bowlerStatsService.getAllBowlerStatsByPlayerId(player.getId()));
                }else{
                    BowlerStatsDTO bs = new BowlerStatsDTO();
                    bs.setPlayerId(player.getId());
                    bs.setTournamentId(savedMatch.getCompetition());
                    BowlerStatsDTO bowlStats = bowlerStatsService.addBowlerStats(bs);
                    aBowlerStatsList.add(bowlStats);
                }
            }
        }
        BattingStats battingStats = new BattingStats(hBatterStatsList,aBatterStatsList);
        BowlingStats bowlingStats = new BowlingStats(hBowlerStatsList,aBowlerStatsList);
        PlayerStats playerStats = new PlayerStats();
        playerStats.setBattingStats(battingStats);
        playerStats.setBowlingStats(bowlingStats);

        com.cricket.scoring.dtos.ResponseFiles.Team hTeam = com.cricket.scoring.dtos.ResponseFiles.Team.builder()
                .id(savedMatch.getHomeTeam().getId())
                .name(savedMatch.getHomeTeam().getName())
                .shortName(savedMatch.getHomeTeam().getShortName())
                .players(hPlayers)
                .build();
        com.cricket.scoring.dtos.ResponseFiles.Team aTeam = com.cricket.scoring.dtos.ResponseFiles.Team.builder()
                .id(savedMatch.getAwayTeam().getId())
                .name(savedMatch.getAwayTeam().getName())
                .shortName(savedMatch.getAwayTeam().getShortName())
                .players(aPlayers)
                .build();

        Toss toss = Toss.builder()
                .winnerId(savedMatch.getTossWinner().getId())
                .winnerName(savedMatch.getTossWinner().getName())
                .tossDecision(savedMatch.getTossDecision())
                .build();

        Long battingTeamId;
        boolean homeWon = Objects.equals( toss.getWinnerId(),hTeam.getId());

        boolean batFirst = toss.getTossDecision() == TossDecision.BAT;

        if(homeWon){
            battingTeamId = batFirst ? hTeam.getId() : aTeam.getId();
        }else{
            battingTeamId = batFirst ? aTeam.getId() : hTeam.getId();
        }
        Long bowlingTeamId = battingTeamId.equals(hTeam.getId()) ? aTeam.getId() : hTeam.getId();
        toss.setBattingTeamId(battingTeamId);
        toss.setBowlingTeamId(bowlingTeamId);

        Teams teams = Teams.builder()
                .battingTeamId(battingTeamId)
                .homeTeam(hTeam)
                .awayTeam(aTeam)
                .build();

        MatchInfo matchInfo = MatchInfo.builder()
                .matchId(savedMatch.getId())
                .externalMatchId(savedMatch.getExternalMatchId())
                .matchName(savedMatch.getMatchName())
                .format(savedMatch.getFormat())
                .competition(savedMatch.getCompetition())
                .season(savedMatch.getSeason())
                .matchNumber(savedMatch.getMatchNumber())
                .status(MatchStatus.SCHEDULED)
                .matchDate(savedMatch.getMatchDate())
                .startTime(savedMatch.getStartTime())
                .build();

        return SetupFile.builder()
                .matchInfo(matchInfo)
                .teams(teams)
                .squads(squad)
                .toss(toss)
                .rules(rules)
                .venue(fileVenue)
                .playerStats(playerStats)
                .build();
    }

    public void saveMatchSquad(Match savedMatch, Squad squad){
        List<Player> homeTeamPlaying11 = squad.getHomeTeamPlaying11().getPlayers().stream().map((element) -> modelMapper.map(element, Player.class)).toList();
        List<Player> awayTeamPlaying11 = squad.getAwayTeamPlaying11().getPlayers().stream().map((element) -> modelMapper.map(element, Player.class)).toList();
        List<Player> homeSubs = squad.getHomeTeamSubstitutes().getPlayers().stream().map((element) -> modelMapper.map(element, Player.class)).toList();
        List<Player> awaySubs = squad.getAwayTeamSubstitutes().getPlayers().stream().map((element) -> modelMapper.map(element, Player.class)).toList();
        for(Player plyr : homeTeamPlaying11){
            MatchSquad matchSquad = MatchSquad.builder()
                    .match(savedMatch)
                    .player(plyr)
                    .team(savedMatch.getHomeTeam())
                    .playingXI(true)
                    .captain(plyr.getCaptain())
                    .wicketKeeper(plyr.getWicketKeeper())
                    .substitutePlayer(false)
                    .battingOrder(plyr.getBattingOrder())
                    .impactPlayer(false)
                    .dismissed(null)
                    .runsScored(null)
                    .ballsFaced(null)
                    .wicketsTaken(null)
                    .createdAt(LocalDateTime.now())
                    .build();
            matchSquadRepository.save(matchSquad);
        }
        for(Player plyr : awayTeamPlaying11){
            MatchSquad matchSquad = MatchSquad.builder()
                    .match(savedMatch)
                    .player(plyr)
                    .team(savedMatch.getAwayTeam())
                    .playingXI(true)
                    .captain(plyr.getCaptain())
                    .wicketKeeper(plyr.getWicketKeeper())
                    .substitutePlayer(false)
                    .battingOrder(plyr.getBattingOrder())
                    .impactPlayer(false)
                    .dismissed(null)
                    .runsScored(null)
                    .ballsFaced(null)
                    .wicketsTaken(null)
                    .createdAt(LocalDateTime.now())
                    .build();
            matchSquadRepository.save(matchSquad);
        }
        for(Player plyr : homeSubs){
            MatchSquad matchSquad = MatchSquad.builder()
                    .match(savedMatch)
                    .player(plyr)
                    .team(savedMatch.getHomeTeam())
                    .playingXI(false)
                    .captain(plyr.getCaptain())
                    .wicketKeeper(plyr.getWicketKeeper())
                    .substitutePlayer(true)
                    .battingOrder(plyr.getBattingOrder())
                    .impactPlayer(false)
                    .dismissed(null)
                    .runsScored(null)
                    .ballsFaced(null)
                    .wicketsTaken(null)
                    .createdAt(LocalDateTime.now())
                    .build();
            matchSquadRepository.save(matchSquad);
        }
        for(Player plyr : awaySubs){
            MatchSquad matchSquad = MatchSquad.builder()
                    .match(savedMatch)
                    .player(plyr)
                    .team(savedMatch.getAwayTeam())
                    .playingXI(false)
                    .captain(plyr.getCaptain())
                    .wicketKeeper(plyr.getWicketKeeper())
                    .substitutePlayer(true)
                    .battingOrder(plyr.getBattingOrder())
                    .impactPlayer(false)
                    .dismissed(null)
                    .runsScored(null)
                    .ballsFaced(null)
                    .wicketsTaken(null)
                    .createdAt(LocalDateTime.now())
                    .build();
            matchSquadRepository.save(matchSquad);
        }
    }


    public Squad buildSquad(CreateMatchRequest request){
        List<Player> players = playerService.getAllPlayers();
        List<Player> homeTeamPlaying11 = new ArrayList<>();
        List<Player> awayTeamPlaying11 = new ArrayList<>();
        List<Player> homeSubs = new ArrayList<>();
        List<Player> awaySubs = new ArrayList<>();
        List<Player> homeBenchPlayers = new ArrayList<>();
        List<Player> awayBenchPlayers = new ArrayList<>();

        for(int plyrId : request.getHomePlaying11()){
            homeTeamPlaying11
                    .add(players.stream()
                            .filter(plyr -> plyr.getId() == plyrId)
                            .findAny().orElseThrow(()->new ResourceNotFoundException("Player not found in db with id "+plyrId))
                    );

        }

        for(int plyrId : request.getAwayPlaying11()){
            awayTeamPlaying11
                    .add(players.stream()
                            .filter(plyr -> plyr.getId() == plyrId)
                            .findAny().orElseThrow(()->new ResourceNotFoundException("Player not found in db with id "+plyrId))
                    );
        }
        for(int plyrId : request.getHomeSubstitutes()){
            homeSubs
                    .add(players.stream()
                            .filter(plyr -> plyr.getId() == plyrId)
                            .findAny().orElseThrow(()->new ResourceNotFoundException("Player not found in db with id "+plyrId))
                    );
        }
        for(int plyrId : request.getAwaySubstitutes()){
            awaySubs
                    .add(players.stream()
                            .filter(plyr -> plyr.getId() == plyrId)
                            .findAny().orElseThrow(()->new ResourceNotFoundException("Player not found in db with id "+plyrId))
                    );
        }
        for(int plyrId : request.getHomeBenchPlayers()){
            homeBenchPlayers
                    .add(players.stream()
                            .filter(plyr -> plyr.getId() == plyrId)
                            .findAny().orElseThrow(()->new ResourceNotFoundException("Player not found in db with id "+plyrId))
                    );
        }
        for(int plyrId : request.getAwayBenchPlayers()){
            awayBenchPlayers
                    .add(players.stream()
                            .filter(plyr -> plyr.getId() == plyrId)
                            .findAny().orElseThrow(()->new ResourceNotFoundException("Player not found in db with id "+plyrId))
                    );
        }

        Playing11 hPlaying11 = Playing11.builder()
                .players(homeTeamPlaying11.stream().map((element) -> modelMapper.map(element, PlayerDTO.class)).collect(Collectors.toList()))
                .build();
        ActivePlayers hActivePlayers = ActivePlayers.builder()
                .players(homeTeamPlaying11.stream().map((element) -> modelMapper.map(element, PlayerDTO.class)).collect(Collectors.toList()))
                .build();
        Playing11 aPlaying11 = Playing11.builder()
                .players(awayTeamPlaying11.stream().map((element) -> modelMapper.map(element, PlayerDTO.class)).collect(Collectors.toList()))
                .build();
        ActivePlayers aActivePlayers = ActivePlayers.builder()
                .players(awayTeamPlaying11.stream().map((element) -> modelMapper.map(element, PlayerDTO.class)).collect(Collectors.toList()))
                .build();
        Substitutes hSubs = Substitutes.builder()
                .players(homeSubs.stream().map((element) -> modelMapper.map(element, PlayerDTO.class)).collect(Collectors.toList()))
                .build();
        Substitutes aSubs = Substitutes.builder()
                .players(awaySubs.stream().map((element) -> modelMapper.map(element, PlayerDTO.class)).collect(Collectors.toList()))
                .build();
        BenchPlayers hBenchPlayers = BenchPlayers.builder()
                .players(homeBenchPlayers.stream().map((element) -> modelMapper.map(element, PlayerDTO.class)).collect(Collectors.toList()))
                .build();
        BenchPlayers aBenchPlayers = BenchPlayers.builder()
                .players(awayBenchPlayers.stream().map((element) -> modelMapper.map(element, PlayerDTO.class)).collect(Collectors.toList()))
                .build();

        return Squad.builder()
                .homeTeamActivePlayers(hActivePlayers)
                .awayTeamActivePlayers(aActivePlayers)
                .homeTeamPlaying11(hPlaying11)
                .awayTeamPlaying11(aPlaying11)
                .homeTeamSubstitutes(hSubs)
                .awayTeamSubstitutes(aSubs)
                .homeTeamBenchPlayers(hBenchPlayers)
                .awayTeamBenchPlayers(aBenchPlayers)
                .homeTeamImpactPlayerDTO(null)
                .awayTeamImpactPlayerDTO(null)
                .build();
    }

    public Match createMatchEntity(CreateMatchRequest request){
        TeamDTO homeTeamDTO = teamService.getTeam(request.getHomeTeamId());
        TeamDTO awayTeamDTO = teamService.getTeam(request.getAwayTeamId());
        VenueDTO venueDTO = venueService.getVenueById(request.getVenueId());

        Team homeTeam = modelMapper.map(homeTeamDTO, Team.class);
        Team awayTeam = modelMapper.map(awayTeamDTO, Team.class);
        Venue venue = modelMapper.map(venueDTO, Venue.class);
        Team tossWinner = request.getTossWinner().equals(homeTeam.getId())?homeTeam:awayTeam;

        CreateTournamentResponse tournamentResponse = tournamentService.getTournamentById(request.getCompetition());

        Match match = Match.builder()
                .matchDate(request.getMatchDate())
                .matchName(request.getMatchName())
                .matchNumber(request.getMatchNumber())
                .format(request.getFormat())
                .competition(request.getCompetition())
                .season(request.getSeason())
                .awayTeam(awayTeam)
                .homeTeam(homeTeam)
                .venue(venue)
                .ballsPerOver(request.getBallsPerOver())
                .totalOvers(request.getTotalOvers())
                .createdAt(LocalDateTime.now())
                .dlsEnabled(request.getDlsEnabled())
                .drsEnabled(request.getDrsEnabled())
                .impactPlayerEnabled(request.getImpactPlayerEnabled())
                .powerplayStartOver(request.getPowerplayStartOver())
                .powerplayEndOver(request.getPowerplayEndOver())
                .reviewsPerTeam(request.getReviewsPerTeam())
                .startTime(request.getStartTime())
                .superOverEnabled(request.getSuperOverEnabled())
                .tossWinner(tossWinner)
                .tossDecision(request.getTossDecision())
                .tournament(modelMapper.map(tournamentResponse, Tournament.class))
                .build();

        return match;
    }
    public void updateMatchEntity( Match match, CreateMatchRequest request){
        TeamDTO homeTeamDTO = teamService.getTeam(request.getHomeTeamId());

        TeamDTO awayTeamDTO = teamService.getTeam(request.getAwayTeamId());

        VenueDTO venueDTO =venueService.getVenueById( request.getVenueId());

        Team homeTeam =modelMapper.map(homeTeamDTO, Team.class);

        Team awayTeam = modelMapper.map(awayTeamDTO, Team.class);

        Venue venue = modelMapper.map(venueDTO, Venue.class);

        Team tossWinner = request.getTossWinner()
                        .equals(homeTeam.getId())
                        ? homeTeam
                        : awayTeam;

        match.setMatchDate(request.getMatchDate());

        match.setMatchName(request.getMatchName());

        match.setMatchNumber(request.getMatchNumber());

        match.setFormat(request.getFormat());

        match.setCompetition(request.getCompetition());

        match.setSeason(request.getSeason());

        match.setAwayTeam(awayTeam);

        match.setHomeTeam(homeTeam);

        match.setVenue(venue);

        match.setBallsPerOver(request.getBallsPerOver());

        match.setTotalOvers(request.getTotalOvers());

        match.setDlsEnabled(request.getDlsEnabled());

        match.setDrsEnabled( request.getDrsEnabled());

        match.setImpactPlayerEnabled(request.getImpactPlayerEnabled());

        match.setPowerplayStartOver( request.getPowerplayStartOver());

        match.setPowerplayEndOver(request.getPowerplayEndOver());

        match.setReviewsPerTeam( request.getReviewsPerTeam());

        match.setStartTime( request.getStartTime() );

        match.setSuperOverEnabled(request.getSuperOverEnabled() );

        match.setTossWinner(tossWinner);

        match.setTossDecision(request.getTossDecision());

        // IMPORTANT
        match.setUpdatedAt(LocalDateTime.now());
    }
    public void resetMatch(Long matchId) {
        MatchAllData matchAllData = jsonFileService.loadMatch(matchId);
        // create fresh empty state
        MatchState freshState = createFreshMatchState(matchId);

        matchAllData.setEventFile(new EventFile());
        matchAllData.setMatchState(freshState);
        // overwrite state file
        jsonFileService.updateStateFile(freshState);
        // overwrite event file
        jsonFileService.clearEventFile(matchId);

        jsonFileService.loadMatch(matchId);
    }
    public MatchState createFreshMatchState(Long matchId) {

        SetupFile setupFile = jsonFileService.loadSetupFile(matchId);

        com.cricket.scoring.dtos.ResponseFiles.Team homeTeam = setupFile.getTeams().getHomeTeam();

        com.cricket.scoring.dtos.ResponseFiles.Team awayTeam = setupFile.getTeams().getAwayTeam();

        com.cricket.scoring.dtos.ResponseFiles.Team battingTeam = setupFile.getTeams().getBattingTeamId().equals(homeTeam.getId())
                ? homeTeam
                : awayTeam;
        com.cricket.scoring.dtos.ResponseFiles.Team bowlingTeam = setupFile.getTeams().getBattingTeamId().equals(homeTeam.getId())
                ? awayTeam
                : homeTeam;

        MatchState matchState = new MatchState();

        // BASIC INFO
        matchState.setMatchId(matchId);

        matchState.setMatchStatus(MatchStatus.LIVE);

        matchState.setCurrentInningNumber(1);
        matchState.setMatchName(setupFile.getMatchInfo().getMatchName());

        // TOSS WINNER / BATTING TEAM
        matchState.setBattingTeamId(setupFile.getTeams().getBattingTeamId());

        // CREATE EMPTY SCORECARD
        matchState.setScoreCard( createEmptyScoreCard(setupFile, battingTeam, bowlingTeam));

        return matchState;
    }
    private ScoreCard createEmptyScoreCard(SetupFile setupFile, com.cricket.scoring.dtos.ResponseFiles.Team battingTeam, com.cricket.scoring.dtos.ResponseFiles.Team bowlingTeam) {

        ScoreCard scoreCard = new ScoreCard();

        List<Inning> innings = new ArrayList<>();

        innings.add(createEmptyInning(1, setupFile, battingTeam));

        innings.add(createEmptyInning(2, setupFile, bowlingTeam));

        scoreCard.setInnings(innings);

        return scoreCard;
    }
    private Inning createEmptyInning(int inningNumber, SetupFile setupFile, com.cricket.scoring.dtos.ResponseFiles.Team battingTeam) {

        Inning inning = new Inning();

        inning.setInningNumber(inningNumber);

        inning.setScoreSummary(createScoreSummary());

        inning.setBattingCard(createBattingCard(setupFile, battingTeam.getId()));

        inning.setBowlingCard(new BowlingCard());

        inning.setPartnershipCard(new PartnershipCard());


        inning.setOverProgression( new OverProgression());

        inning.setFallOfWickets(new ArrayList<>());

        return Inning.builder()
                .inningNumber(inningNumber)
                .teamId(battingTeam.getId())
                .nextBattingPosition(1)
                .strikerId(null)
                .nonStrikerId(null)
                .currentBowlerId(null)
                .scoreSummary(createScoreSummary())
                .controlMetrics(createInningControlMetrices())
                .extras(createExtras())
                .battingCard(createBattingCard(setupFile, battingTeam.getId()))
                .bowlingCard(new BowlingCard())
                .partnershipCard(new PartnershipCard())
                .overProgression(new OverProgression())
                .phaseBreakdown(null)
                .fallOfWickets(new ArrayList<>())
                .build();
    }
    private ScoreSummary createEmptyScoreSummary() {

        ScoreSummary summary = new ScoreSummary();

        summary.setRuns(0);

        summary.setWickets(0);

        summary.setOvers(0);

        summary.setBalls(0);

        return summary;
    }
    private BattingCard createEmptyBattingCard(com.cricket.scoring.dtos.ResponseFiles.Team battingTeam) {

        BattingCard battingCard = new BattingCard();

        List<BatterCard> batters = battingTeam
                .getPlayers()
                .stream()
                .map(player -> {

                    BatterCard batter = new BatterCard();

                    batter.getBatter().setPlayerId(player.getId());
                    batter.getBatter().setPlayerName(player.getFullName());
                    batter.getBatter().setBattingPosition(player.getBattingOrder());

                    batter.getScoring().setRuns(0);

                    batter.getScoring().setBalls(0);

                    batter.getScoring().setFours(0);

                    batter.getScoring().setSixes(0);

                    batter.getScoring().setStrikeRate(0.0);

                    batter.getDismissal().setIsOut(false);
                    batter.getDismissal().setStatus(BattingStatus.STILL_TO_BAT);
                    batter.getDismissal().setDismissalType(null);
                    batter.getDismissal().setBowlerId(null);

                    return batter;

                }).toList();

        battingCard.setBatters(batters);

        return battingCard;
    }

    //INTERNAL METHODS
    public ScoreSummary createScoreSummary(){
        return ScoreSummary.builder()
                .runs(0)
                .wickets(0)
                .overs(0)
                .balls(0)
                .runRate(0.0)
                .declared(null)
                .build();
    }
    public Extras createExtras(){
        return Extras.builder()
                .wides(0)
                .noBalls(0)
                .byes(0)
                .legByes(0)
                .penalty(0)
                .build();
    }
    public BattingCard createBattingCard(SetupFile setupFile, Long battingTeamId){
        List<PlayerDTO> players = battingTeamId.equals(setupFile.getTeams().getHomeTeam().getId())
                ? setupFile.getSquads().getHomeTeamActivePlayers().getPlayers()
                : setupFile.getSquads().getAwayTeamActivePlayers().getPlayers();

        List<BatterCard> batterCards = new ArrayList<>();
        ImpactPlayerDTO impactPlayerDTO = null;
        if(battingTeamId.equals(setupFile.getTeams().getHomeTeam().getId())){
            impactPlayerDTO = setupFile.getSquads().getHomeTeamImpactPlayerDTO();
        }else{
            impactPlayerDTO = setupFile.getSquads().getAwayTeamImpactPlayerDTO();
        }


        for(PlayerDTO plyr : players){
            boolean isImpactIn = false;
            boolean isImpactOut = false;
            PlayerInfo playerInfo = PlayerInfo.builder()
                    .playerId(plyr.getId())
                    .playerName(plyr.getFullName())
                    .battingPosition(plyr.getBattingOrder())
                    .build();
            DismissalInfo dismissalInfo = DismissalInfo.builder()
                    .status(BattingStatus.STILL_TO_BAT)
                    .build();

            if(impactPlayerDTO != null){
                if(impactPlayerDTO.getImpactInPlayerId().equals(plyr.getId())){
                    isImpactIn = true;
                }else if(impactPlayerDTO.getImpactOutPlayerId().equals(plyr.getId())){
                    isImpactOut = true;
                }
            }

            BatterCard bc = BatterCard.builder()
                    .batter(playerInfo)
                    .scoring(new ScoringStats())
                    .dismissal(dismissalInfo)
                    .phases(null)
                    .control(null)
                    .context(null)
                    .onStrike(false)
                    .isImpactIn(isImpactIn)
                    .isImpactOut(isImpactOut)
                    .build();
            batterCards.add(bc);
        }
        return BattingCard.builder()
                .batters(batterCards)
                .build();
    }
    public FallOfWicket createFallOfWickets(){
        return FallOfWicket.builder()
                .wicketNumber(null)
                .scoreAtFall(null)
                .over(null)
                .batterId(null)
                .batterName(null)
                .build();
    }
    public List<Inning> createInnings(MatchState matchState, Integer inningNumber, Long battingTeamId, ScoreSummary scoreSummary, Extras extras, BattingCard battingCard,
                                       BowlingCard bowlingCard, PartnershipCard partnershipCard, OverProgression overProgression, InningControlMetrices inningControlMetrices){
        List<Inning> innings;
        if(matchState == null || matchState.getScoreCard().getInnings() == null){
            innings = new ArrayList<>();
        }else{
            innings = matchState.getScoreCard().getInnings();
        }

        Inning inning = Inning.builder()
                .inningNumber(inningNumber)
                .teamId(battingTeamId)
                .nextBattingPosition(1)
                .strikerId(null)
                .nonStrikerId(null)
                .currentBowlerId(null)
                .scoreSummary(scoreSummary)
                .controlMetrics(inningControlMetrices)
                .extras(extras)
                .battingCard(battingCard)
                .bowlingCard(bowlingCard)
                .partnershipCard(partnershipCard)
                .overProgression(overProgression)
                .phaseBreakdown(null)
                .oversHistory(new ArrayList<>())
                .fallOfWickets(new ArrayList<>())
                .build();
        innings.add(inning);
        return innings;
    }
    public InningControlMetrices createInningControlMetrices(){
        return InningControlMetrices.builder()
                .dots(null)
                .singles(null)
                .doubles(null)
                .threes(null)
                .fours(null)
                .sixes(null)
                .boundaries(null)
                .boundaryPercentage(null)
                .build();
    }
    public MatchState createMatchState(SetupFile setupFile, Integer inningNumber, ScoreCard scoreCard, Long battingTeamId){
        return MatchState.builder()
                .matchId(setupFile.getMatchInfo().getMatchId())
                .matchName(setupFile.getMatchInfo().getMatchName())
                .currentInningNumber(inningNumber)
                .battingTeamId(battingTeamId)
                .scoreCard(scoreCard)
                .matchStatus(MatchStatus.SCHEDULED)
                .build();
    }
}

