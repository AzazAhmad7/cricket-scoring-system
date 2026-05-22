package com.cricket.scoring.services.Impl;

import com.cricket.scoring.dtos.*;
import com.cricket.scoring.dtos.ResponseFiles.*;
import com.cricket.scoring.entities.*;
import com.cricket.scoring.entities.Team;
import com.cricket.scoring.entities.Venue;
import com.cricket.scoring.entities.enums.BattingStatus;
import com.cricket.scoring.entities.enums.MatchFormat;
import com.cricket.scoring.entities.enums.MatchStatus;
import com.cricket.scoring.entities.enums.TossDecision;
import com.cricket.scoring.exceptions.ResourceNotFoundException;
import com.cricket.scoring.exceptions.RuntimeConflictException;
import com.cricket.scoring.repositories.MatchRepository;
import com.cricket.scoring.repositories.MatchSquadRepository;
import com.cricket.scoring.services.*;
import com.cricket.scoring.utils.Util;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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

    @Override
    public MatchAllData createMatch(CreateMatchRequest request) {
        Match match = createMatchEntity(request);
        Match savedMatch = matchRepository.save(match);
        Squad squad = buildSquad(request);
        saveMatchSquad(savedMatch, squad);
        SetupFile setupFile = buildSetupFile(savedMatch, squad);
        setupFile.getMatchInfo().setStatus(MatchStatus.SCHEDULED);
        jsonFileService.createSetupFile(setupFile);
        MatchState matchState = startMatch(setupFile,null, 1, MatchStatus.SCHEDULED);
        jsonFileService.loadMatchAllData(setupFile.getMatchInfo().getMatchId());
        MatchAllData matchAllData = MatchAllData.builder()
                .setupFile(setupFile)
                .matchState(matchState)
                .build();
        return matchAllData;
    }

    @Override
    public MatchAllData updateMatchStatus(Long matchId, MatchStatus matchStatus) {
        MatchAllData matchAllData = jsonFileService.loadMatchAllData(matchId);
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
        MatchAllData matchAllData = jsonFileService.loadMatchAllData(matchId);
        MatchState matchState = matchAllData.getMatchState();
        SetupFile setupFile = matchAllData.getSetupFile();
        if(matchState.getMatchStatus() != MatchStatus.INNINGS_BREAK) throw new RuntimeConflictException("Pause the match to change inning");
        int nextInning = 0;
        if(setupFile.getMatchInfo().getFormat().equals(MatchFormat.T20) || setupFile.getMatchInfo().getFormat().equals(MatchFormat.ODI)){
            nextInning = 3 - matchState.getCurrentInningNumber();
        }


        if(matchState.getScoreCard().getInnings().size() == 1){
            Long battingTeamId = setupFile.getTeams().getBattingTeamId().equals(setupFile.getTeams().getHomeTeam().getId())
                    ? setupFile.getTeams().getAwayTeam().getId()
                    : setupFile.getTeams().getHomeTeam().getId();
            setupFile.getTeams().setBattingTeamId(battingTeamId);
            matchAllData.setSetupFile(setupFile);
            startMatch(setupFile, matchState,nextInning, MatchStatus.INNINGS_BREAK);
        }else{
            Long battingTeamId = setupFile.getTeams().getBattingTeamId().equals(setupFile.getTeams().getHomeTeam().getId())
                    ? setupFile.getTeams().getAwayTeam().getId()
                    : setupFile.getTeams().getHomeTeam().getId();
            setupFile.getTeams().setBattingTeamId(battingTeamId);
            matchState.setBattingTeamId(battingTeamId);
            matchState.setCurrentInningNumber(nextInning);
            matchAllData.setSetupFile(setupFile);
            matchAllData.setMatchState(matchState);
            jsonFileService.updateStateFile(matchState);
        }
        jsonFileService.updateSetUpFile(setupFile);
        return matchAllData;
    }

    @Override
    public MatchAllData getMatchAllData(Long matchId){
        if(!matchRepository.existsById(matchId)) throw new ResourceNotFoundException("Match does not exist with id "+matchId);
        MatchState loadedMatch;
        SetupFile loadedSetup;
        EventFile eventFile;
        if(jsonFileService.getMatchAllDataFromMemory(matchId) == null){
            loadedMatch = jsonFileService.loadStateFile(matchId);
            loadedSetup = jsonFileService.loadSetupFile(matchId);
            eventFile = jsonFileService.loadEventFile(matchId);
            MatchAllData mth = MatchAllData.builder()
                    .setupFile(loadedSetup)
                    .matchState(loadedMatch)
                    .eventFile(eventFile)
                    .build();
            jsonFileService.updateMap(matchId, mth);
        }else{
            loadedMatch = jsonFileService.getMatchStateFromMemory(matchId);
            loadedSetup = jsonFileService.getSetupFileFromMemory(matchId);
            eventFile = jsonFileService.getEventsFromMemory(matchId);
        }

        return MatchAllData.builder()
                .setupFile(loadedSetup)
                .matchState(loadedMatch)
                .eventFile(eventFile)
                .build();
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
        jsonFileService.createEventFile(matchState, new EventFile());
        return matchState;
    }

    @Override
    public MatchAllData endMatch(MatchAllData matchAllData) {
        MatchState matchState = matchAllData.getMatchState();
        SetupFile setupFile = matchAllData.getSetupFile();
        EventFile eventFile = matchAllData.getEventFile();
        if(!matchState.getMatchStatus().equals(MatchStatus.LIVE))
            throw new RuntimeConflictException("Match status is "+matchState.getMatchStatus()+" make it live for scoring");

        Inning inning = Util.getCurrentInning(matchState);
        if(setupFile.getMatchInfo().getFormat().equals(MatchFormat.T20) || setupFile.getMatchInfo().getFormat().equals(MatchFormat.ODI)){
            if(inning.getInningNumber().equals(2)){
                int firstInningScore = matchState.getScoreCard().getInnings().get(0).getScoreSummary().getRuns();
                int secondInningScore = inning.getScoreSummary().getRuns();
            }
        }

        return null;
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
    public SetupFile updateMatch(Long matchId, CreateMatchRequest request) {

        if(!matchRepository.existsById(matchId)) throw new ResourceNotFoundException("Match not found with id "+matchId);

        Match match = createMatchEntity(request);
        match.setId(matchId);
        Match savedMatch = matchRepository.save(match);

        Squad squad = buildSquad(request);
        saveMatchSquad(savedMatch, squad);
        SetupFile setupFile = buildSetupFile(savedMatch, squad);

        jsonFileService.createSetupFile(setupFile);
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

        Toss toss = Toss.builder()
                .winnerId(savedMatch.getTossWinner().getId())
                .winnerName(savedMatch.getTossWinner().getName())
                .tossDecision(savedMatch.getTossDecision())
                .build();

        List<Player> hPlayers = new ArrayList<>();
        List<Player> aPlayers = new ArrayList<>();
        List<Player> allPlayers = playerService.getAllPlayers();

        for(Player player : allPlayers){
            if(player.getTeam().getId().equals(savedMatch.getHomeTeam().getId())){
                hPlayers.add(player);
            }
        }
        for(Player player : allPlayers){
            if(player.getTeam().getId().equals(savedMatch.getAwayTeam().getId())){
                aPlayers.add(player);
            }
        }
        com.cricket.scoring.dtos.ResponseFiles.Team hTeam = com.cricket.scoring.dtos.ResponseFiles.Team.builder()
                .id(savedMatch.getHomeTeam().getId())
                .name(savedMatch.getHomeTeam().getName())
                .players(hPlayers)
                .build();
        com.cricket.scoring.dtos.ResponseFiles.Team aTeam = com.cricket.scoring.dtos.ResponseFiles.Team.builder()
                .id(savedMatch.getAwayTeam().getId())
                .name(savedMatch.getAwayTeam().getName())
                .players(aPlayers)
                .build();

        Long battingTeamId;
        boolean homeWon = Objects.equals( toss.getWinnerId(),hTeam.getId());

        boolean batFirst = toss.getTossDecision() == TossDecision.BAT;

        if(homeWon){
            battingTeamId = batFirst ? hTeam.getId() : aTeam.getId();
        }else{
            battingTeamId = batFirst ? aTeam.getId() : hTeam.getId();
        }

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
        Playing11 aPlaying11 = Playing11.builder()
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
                .build();

        return match;
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
                ? setupFile.getSquads().getHomeTeamPlaying11().getPlayers()
                : setupFile.getSquads().getAwayTeamPlaying11().getPlayers();

        List<BatterCard> batterCards = new ArrayList<>();

        for(PlayerDTO plyr : players){
            PlayerInfo playerInfo = PlayerInfo.builder()
                    .playerId(plyr.getId())
                    .playerName(plyr.getFullName())
                    .battingPosition(plyr.getBattingOrder())
                    .build();
            DismissalInfo dismissalInfo = DismissalInfo.builder()
                    .status(BattingStatus.STILL_TO_BAT)
                    .build();
            BatterCard bc = BatterCard.builder()
                    .batter(playerInfo)
                    .scoring(null)
                    .dismissal(dismissalInfo)
                    .phases(null)
                    .control(null)
                    .context(null)
                    .onStrike(false)
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

