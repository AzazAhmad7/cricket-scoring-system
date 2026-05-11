package com.cricket.scoring.services.Impl;

import com.cricket.scoring.dtos.*;
import com.cricket.scoring.dtos.ResponseFiles.*;
import com.cricket.scoring.entities.*;
import com.cricket.scoring.entities.Team;
import com.cricket.scoring.entities.Venue;
import com.cricket.scoring.entities.enums.BattingStatus;
import com.cricket.scoring.entities.enums.MatchStatus;
import com.cricket.scoring.entities.enums.TossDecision;
import com.cricket.scoring.exceptions.ResourceNotFoundException;
import com.cricket.scoring.repositories.MatchRepository;
import com.cricket.scoring.repositories.MatchSquadRepository;
import com.cricket.scoring.services.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        jsonFileService.createSetupFile(setupFile);
        MatchState matchState = startMatch(setupFile);
        jsonFileService.loadMatchAllData(setupFile.getMatchInfo().getMatchId());
        MatchAllData matchAllData = MatchAllData.builder()
                .setupFile(setupFile)
                .matchState(matchState)
                .build();
        return matchAllData;
    }
    @Override
    public MatchAllData getMatchAllData(Long matchId){
        if(!matchRepository.existsById(matchId)) throw new ResourceNotFoundException("Match does not exist with id "+matchId);
        MatchState loadedMatch = jsonFileService.getMatchStateFromMemory(matchId);
        SetupFile loadedSetup = jsonFileService.getSetupFileFromMemory(matchId);
        EventFile eventFile = jsonFileService.getEventsFromMemory(matchId);

        return MatchAllData.builder()
                .setupFile(loadedSetup)
                .matchState(loadedMatch)
                .eventFile(eventFile)
                .build();
    }

    @Override
    public MatchState startMatch(SetupFile setupFile) {
        ScoreSummary scoreSummary = ScoreSummary.builder()
                .runs(0)
                .wickets(0)
                .overs(0)
                .balls(0)
                .runRate(0.0)
                .declared(null)
                .build();
        Extras extras = Extras.builder()
                .wides(0)
                .noBalls(0)
                .byes(0)
                .legByes(0)
                .penalty(0)
                .build();

        Long battingTeamId = setupFile.getTeams().getBattingTeamId();
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

        BattingCard battingCard = BattingCard.builder()
                .batters(batterCards)
                .build();
        BowlingCard bowlingCard = BowlingCard.builder()
                .bowlers(new ArrayList<>())
                .build();
        PartnershipCard partnershipCard = PartnershipCard.builder()
                .partnerships(new ArrayList<>())
                .build();
        OverProgression overProgression = OverProgression.builder()
                .overs(null)
                .build();
        FallOfWicket fallOfWicket = FallOfWicket.builder()
                .wicketNumber(null)
                .scoreAtFall(null)
                .over(null)
                .batterId(null)
                .batterName(null)
                .build();
        Inning inning = Inning.builder()
                .inningNumber(1)
                .battingTeamId(battingTeamId)
                .scoreSummary(scoreSummary)
                .extras(extras)
                .battingCard(battingCard)
                .bowlingCard(bowlingCard)
                .partnershipCard(partnershipCard)
                .overProgression(overProgression)
                .phaseBreakdown(null)
                .fallOfWickets(new ArrayList<>())
                .build();
        List<Inning> innings = new ArrayList<>();
        innings.add(inning);
        ScoreCard scoreCard = ScoreCard.builder()
                .innings(innings)
                .build();

        MatchState matchState = MatchState.builder()
                .matchId(setupFile.getMatchInfo().getMatchId())
                .matchName(setupFile.getMatchInfo().getMatchName())
                .matchStatus(String.valueOf(MatchStatus.SCHEDULED))
                .currentInningNumber(1)
                .nextBattingPosition(1)
                .strikerId(null)
                .nonStrikerId(null)
                .currentBowlerId(null)
                .scoreCard(scoreCard)
                .build();
        jsonFileService.createMatchStateFile(matchState);
        jsonFileService.createEventFile(matchState, new EventFile());
        return matchState;
    }

    @Override
    public MatchDTO getMatch(Long matchId) {
        if(!matchRepository.existsById(matchId)) throw new ResourceNotFoundException("Match not found with id "+matchId);
        return modelMapper.map(matchRepository.findById(matchId), MatchDTO.class);
    }

    @Override
    public List<Long> getAllMatches() {
        List<Match> matches = matchRepository.findAll();
        List<Long> matchIds = new ArrayList<>();
        for(Match match : matches) {
            matchIds.add(match.getId());
        }
        System.out.println(matchIds);
        return matchIds;
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
}
