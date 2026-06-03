package com.cricket.scoring.services.Impl;

import com.cricket.scoring.dtos.CreateTournamentResponse;
import com.cricket.scoring.dtos.PointsTableDTO;
import com.cricket.scoring.dtos.ResponseFiles.LeagueTable;
import com.cricket.scoring.dtos.ResponseFiles.MatchResultDTO;
import com.cricket.scoring.dtos.ResponseFiles.MatchState;
import com.cricket.scoring.dtos.ResponseFiles.SetupFile;
import com.cricket.scoring.dtos.TeamDTO;
import com.cricket.scoring.dtos.TournamentTeamDTO;
import com.cricket.scoring.entities.PointsTable;
import com.cricket.scoring.entities.Team;
import com.cricket.scoring.entities.Tournament;
import com.cricket.scoring.entities.TournamentTeam;
import com.cricket.scoring.entities.enums.MatchResultType;
import com.cricket.scoring.exceptions.ResourceNotFoundException;
import com.cricket.scoring.repositories.PointsTableRepository;
import com.cricket.scoring.repositories.TournamentRepository;
import com.cricket.scoring.services.JsonFileService;
import com.cricket.scoring.services.PointsTableService;
import com.cricket.scoring.services.TournamentService;
import com.cricket.scoring.services.TournamentTeamService;
import com.cricket.scoring.utils.Util;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PointsTableServiceImpl implements PointsTableService {

    private final PointsTableRepository pointsTableRepository;
    private final ModelMapper modelMapper;
    private final TournamentTeamService tournamentTeamService;
    private final JsonFileService jsonFileService;
    private final TournamentRepository tournamentRepository;

    @Override
    public List<PointsTableDTO> getPointsTable(Long tournamentId) {
        List<PointsTable> pointsTables = pointsTableRepository.findByTournamentId(tournamentId);
        return pointsTables.stream()
                .map((element) -> modelMapper.map(element, PointsTableDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void saveAllPointsTable(List<PointsTableDTO> pointsTableDTOList) {
        List<PointsTable> pointsTable = pointsTableDTOList.stream()
                .map((element) -> modelMapper.map(element, PointsTable.class))
                .collect(Collectors.toList());
        pointsTableRepository.saveAll(pointsTable);
    }

    @Transactional
    @Override
    public void generatePointsTable(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(()-> new ResourceNotFoundException("Tournament not found with id "+tournamentId));
        List<TournamentTeamDTO> tournamentTeamDTOS = tournamentTeamService.getAllTeamsOfTournament(tournament.getId());
        List<Team> teams = new ArrayList<>();
        for(TournamentTeamDTO tt : tournamentTeamDTOS) {
            teams.add(tt.getTeam());
        }

        List<PointsTable> pointsTables = new ArrayList<>();

        for(Team team: teams){
            PointsTable pt = PointsTable.builder()
                                .tournament(tournament)
                                .team(team)
                                .matchesPlayed(0)
                                .matchesWon(0)
                                .matchesLost(0)
                                .matchesTied(0)
                                .noResults(0)
                                .points(0)
                                .netRunRate(0.0)
                                .runsScored(0)
                                .ballsFaced(0)
                                .runsConceded(0)
                                .ballsBowled(0)
                                .build();

            pointsTables.add(pt);
        }

        List<PointsTable> pt = pointsTableRepository.saveAll(pointsTables);

        LeagueTable leagueTable = new LeagueTable();
        List<PointsTableDTO> pointsTableDTOList = pt.stream().map((element) -> modelMapper.map(element, PointsTableDTO.class)).toList();
        leagueTable.setLeagueTable(pointsTableDTOList);

        jsonFileService.createPointsTable(tournament,leagueTable);
    }

    @Override
    public LeagueTable updateLeagueTable(LeagueTable leagueTable, MatchState matchState, SetupFile setupFile){
        Long battingTeamId = setupFile.getToss().getBattingTeamId();
        Long bowlingTeamId = setupFile.getToss().getBowlingTeamId();

        MatchResultDTO matchResultDTO = matchState.getMatchResultDTO();

        List<PointsTableDTO> pointsTableDTOList = leagueTable.getLeagueTable();

        Map<Long, PointsTableDTO> tableMap = leagueTable.getLeagueTable()
                .stream()
                .collect(Collectors.toMap(
                        PointsTableDTO::getTeamId,
                        Function.identity()
                ));

        PointsTableDTO battingTeamPointsTable = tableMap.get(battingTeamId);

        PointsTableDTO bowlingTeamPointsTable = tableMap.get(bowlingTeamId);

        int ballsFacedThisMatch = Util.overBallsToBalls(matchState.getScoreCard().getInnings().getFirst().getScoreSummary().getOvers(), matchState.getScoreCard().getInnings().getFirst().getScoreSummary().getBalls());
        int ballsBowledThisMatch = Util.overBallsToBalls(matchState.getScoreCard().getInnings().get(1).getScoreSummary().getOvers(), matchState.getScoreCard().getInnings().get(1).getScoreSummary().getBalls());

        if(matchState.getScoreCard().getInnings().getFirst().getScoreSummary().getWickets() >= 10){
            ballsFacedThisMatch = Util.overBallsToBalls(setupFile.getRules().getOvers(), 0);
        }

        if(matchState.getScoreCard().getInnings().get(1).getScoreSummary().getWickets() >= 10){
            ballsBowledThisMatch = Util.overBallsToBalls(setupFile.getRules().getOvers(), 0);
        }

        //BATTING TEAM
        int runsScoredThisMatch = matchState.getScoreCard().getInnings().getFirst().getScoreSummary().getRuns();
        int runsConcededThisMatch = matchState.getScoreCard().getInnings().get(1).getScoreSummary().getRuns();


        //batting team
        battingTeamPointsTable.setRunsScored(battingTeamPointsTable.getRunsScored() + runsScoredThisMatch);
        battingTeamPointsTable.setBallsFaced(battingTeamPointsTable.getBallsFaced() + ballsFacedThisMatch);
        battingTeamPointsTable.setRunsConceded(battingTeamPointsTable.getRunsConceded() + runsConcededThisMatch);
        battingTeamPointsTable.setBallsBowled(battingTeamPointsTable.getBallsBowled() + ballsBowledThisMatch);
        battingTeamPointsTable.setMatchesPlayed(battingTeamPointsTable.getMatchesPlayed() + 1);

        //bowling team opposite of batting team
        bowlingTeamPointsTable.setRunsConceded(bowlingTeamPointsTable.getRunsConceded() + runsScoredThisMatch);
        bowlingTeamPointsTable.setBallsBowled(bowlingTeamPointsTable.getBallsBowled() + ballsFacedThisMatch);
        bowlingTeamPointsTable.setRunsScored(bowlingTeamPointsTable.getRunsScored() + runsConcededThisMatch);
        bowlingTeamPointsTable.setBallsFaced(bowlingTeamPointsTable.getBallsFaced() + ballsBowledThisMatch);
        bowlingTeamPointsTable.setMatchesPlayed(bowlingTeamPointsTable.getMatchesPlayed() + 1);

        battingTeamPointsTable.setNetRunRate(Util.calculateRunRate(battingTeamPointsTable.getRunsScored(), battingTeamPointsTable.getBallsFaced(), battingTeamPointsTable.getRunsConceded(), battingTeamPointsTable.getBallsBowled()));
        bowlingTeamPointsTable.setNetRunRate(Util.calculateRunRate(bowlingTeamPointsTable.getRunsScored(), bowlingTeamPointsTable.getBallsFaced(), bowlingTeamPointsTable.getRunsConceded(), bowlingTeamPointsTable.getBallsBowled()));

        if(matchResultDTO == null){
            battingTeamPointsTable.setPoints(battingTeamPointsTable.getPoints() + 1);
            bowlingTeamPointsTable.setPoints(bowlingTeamPointsTable.getPoints() + 1);
            battingTeamPointsTable.setNoResults(battingTeamPointsTable.getNoResults() + 1);
            bowlingTeamPointsTable.setNoResults(bowlingTeamPointsTable.getNoResults() + 1);
        }else if(matchResultDTO.getResultType() == MatchResultType.TIE){
            battingTeamPointsTable.setPoints(battingTeamPointsTable.getPoints() + 1);
            bowlingTeamPointsTable.setPoints(bowlingTeamPointsTable.getPoints() + 1);
            battingTeamPointsTable.setMatchesTied(battingTeamPointsTable.getMatchesTied() + 1);
            bowlingTeamPointsTable.setMatchesTied(bowlingTeamPointsTable.getMatchesTied() + 1);
        }else{
            if(matchResultDTO.getWinningTeamId().equals(battingTeamId)){
                battingTeamPointsTable.setPoints(battingTeamPointsTable.getPoints() + 2);
                battingTeamPointsTable.setMatchesWon(battingTeamPointsTable.getMatchesWon() + 1);
                bowlingTeamPointsTable.setMatchesLost(bowlingTeamPointsTable.getMatchesLost() + 1);
            }else if(matchResultDTO.getWinningTeamId().equals(bowlingTeamId)){
                bowlingTeamPointsTable.setPoints(bowlingTeamPointsTable.getPoints() + 2);
                bowlingTeamPointsTable.setMatchesWon(bowlingTeamPointsTable.getMatchesWon() + 1);
                battingTeamPointsTable.setMatchesLost(battingTeamPointsTable.getMatchesLost() + 1);
            }
        }
        pointsTableRepository.saveAll(
                pointsTableDTOList.stream().map((element) -> modelMapper.map(element, PointsTable.class)).collect(Collectors.toList())
        );
        Tournament tournament = modelMapper.map(tournamentRepository.findById(battingTeamPointsTable.getTournamentId()), Tournament.class);
        jsonFileService.createPointsTable(tournament, leagueTable);
        return leagueTable;
    }
}
