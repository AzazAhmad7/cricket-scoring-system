import { useEffect, useState } from "react";
import Sidebar from "../components/scoringUi/Sidebar";
import Header from "../components/scoringUi/Header";
import ScoreSummary from "../components/scoringUi/ScoreSummary";
import NextBallControls from "../components/scoringUi/NextBallControls";
import BallInfo from "../components/scoringUi/BallInfo";
import PartnershipCard from "../components/scoringUi/PartnershipCard";
import FallOfWickets from "../components/scoringUi/FallOfWickets";
import OverHistory from "../components/scoringUi/OverHistory";
import Squad from "../components/scoringUi/Squad";
import MatchInfo from "../components/scoringUi/MatchInfo";
import InningsInsights from "../components/scoringUi/InningsInsights";
import {
  getMatchAllData,
  scoreBall,
  logNewBatter,
  logNewBowler,
  endOver,
  changeInning,
  resetMatch,
  rebuildMatch,
  swapStriker,
  impactPlayer,
  handleApiError,
} from "../services/api";
import { useParams } from "react-router-dom";

export default function ScoringDashboard() {
  const { matchId } = useParams();
  const [matchAllData, setMatchAllData] = useState(null);
  const [matchData, setMatchData] = useState(null);
  const [matchState, setMatchState] = useState(null);

  const fetchMatch = async () => {
    try {
      const res = await getMatchAllData(matchId);
      console.log("DATA : ", res);
      setMatchAllData(res.data);
      setMatchData(res.data.setupFile);
      setMatchState(res.data.matchState);
    } catch (error) {
      handleApiError(error);
    }
  };

  useEffect(() => {
    fetchMatch();
  }, []);

  const handleBallEvent = async (thisEventData) => {
    const event =
      typeof thisEventData === "string"
        ? {
            matchId: matchData.matchInfo.matchId,
            eventType: thisEventData,
          }
        : {
            matchId: matchData.matchInfo.matchId,
            eventType: thisEventData.eventType,
            fielderId: thisEventData.fielderId,
            dismissedType: thisEventData.dismissedType,
            runs: thisEventData.runs,
            isWicket: thisEventData.isWicket,
            dismissedPlayerId: thisEventData.dismissedPlayerId,

            isWideAnyBall: thisEventData.isWide,
            isNoBallAnyBall: thisEventData.isNoBall,
            isByeAnyBall: thisEventData.isBye,
            isLegByeAnyBall: thisEventData.isLegBye,
            runsOfByeAnyBall: thisEventData.runsOfBye,
            runsOffBatAnyBall: thisEventData.runsOfBat,
          };

    try {
      console.log("event To be Sent", event);
      await scoreBall(event);
      await fetchMatch();
    } catch (error) {
      handleApiError(error);
    }
  };

  const handleEndOverEvent = async () => {
    try {
      await endOver({
        matchId: matchData.matchInfo.matchId,
        eventType: "END_OVER",
      });

      await fetchMatch();
    } catch (error) {
      handleApiError(error);
    }
  };

  const handleChangeInning = async () => {
    try {
      await changeInning({
        matchId: matchData.matchInfo.matchId,
      });

      await fetchMatch();
    } catch (error) {
      handleApiError(error);
    }
  };
  const handleResetMatch = async () => {
    try {
      await resetMatch({
        matchId: matchData.matchInfo.matchId,
      });

      await fetchMatch();
    } catch (error) {
      handleApiError(error);
    }
  };
  const handleRebuildMatch = async () => {
    try {
      await rebuildMatch({
        matchId: matchData.matchInfo.matchId,
      });

      await fetchMatch();
    } catch (error) {
      handleApiError(error);
    }
  };
  const handleSwapStriker = async () => {
    try {
      await swapStriker({
        matchState: matchState,
      });

      await fetchMatch();
    } catch (error) {
      handleApiError(error);
    }
  };

  const handleImpactPlayer = async (impactEvent) => {
    try {
      await impactPlayer({
        matchId: matchData.matchInfo.matchId,
        eventType: impactEvent.eventType,
        teamId: impactEvent.teamId,
        impactInPlayerId: impactEvent.impactInPlayerId,
        impactOutPlayerId: impactEvent.impactOutPlayerId,
      });

      await fetchMatch();
    } catch (error) {
      handleApiError(error);
    }
  };

  const handleNewBatterEvent = async (thisEventData) => {
    try {
      await logNewBatter({
        matchId: matchData.matchInfo.matchId,
        eventType: thisEventData.eventType,
        playerId: thisEventData.playerId,
      });

      await fetchMatch();
    } catch (error) {
      handleApiError(error);
    }
  };
  const handleNewBowlerEvent = async (thisEventData) => {
    try {
      await logNewBowler({
        matchId: matchData.matchInfo.matchId,
        eventType: thisEventData.eventType,
        playerId: thisEventData.playerId,
      });

      await fetchMatch();
    } catch (error) {
      handleApiError(error);
    }
  };

  if (!matchData || !matchState) return null;

  return (
    <div className="h-screen bg-slate-100 flex text-sm overflow-hidden">
      {/* SIDEBAR */}
      <Sidebar matchState={matchState} />

      {/* MAIN */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* HEADER */}
        <Header data={matchData} />

        {/* CONTENT GRID */}
        <div className="flex-1 overflow-hidden grid grid-cols-12 gap-3 p-3">
          {/* LEFT (9 cols) */}
          <div className="col-span-9 flex flex-col gap-3 overflow-auto">
            {/* Score Summary */}
            <ScoreSummary matchState={matchState} matchData={matchData} />

            {/* Controls + Ball Info */}
            <div className="grid grid-cols-12 gap-3">
              <div className="col-span-8">
                <NextBallControls
                  onScore={handleBallEvent}
                  matchState={matchState}
                  matchData={matchData}
                  matchAllData={matchAllData}
                  onEndOver={handleEndOverEvent}
                  onNextInning={handleChangeInning}
                  onResetMatch={handleResetMatch}
                  onRebuildMatch={handleRebuildMatch}
                  onSwapStriker={handleSwapStriker}
                  onImpact={handleImpactPlayer}
                  onSelectNewBatter={(batter) => {
                    handleNewBatterEvent({
                      eventType: "NEW_BATTER",
                      playerId: batter.playerId,
                    });
                  }}
                  onChangeBowler={(bowler) => {
                    handleNewBowlerEvent({
                      eventType: "BOWLER_CHANGE",
                      playerId: bowler.id,
                    });
                  }}
                />
              </div>
              <div className="col-span-4">
                <InningsInsights
                  matchData={matchData}
                  matchState={matchState}
                />
              </div>
            </div>

            {/* Squad + Match Info */}
            <div className="grid grid-cols-2 gap-3">
              <Squad matchData={matchData} />
              <MatchInfo matchData={matchData} />
            </div>
          </div>

          {/* RIGHT (3 cols) */}
          <div className="col-span-3 flex flex-col gap-3 overflow-auto min-h-0">
            {/* Fixed-height cards to prevent one card from pushing others out */}
            <div className="max-h-72 overflow-y-auto flex-shrink-0">
              <OverHistory matchState={matchState} />
            </div>
            <div className="flex-shrink-0">
              <PartnershipCard matchState={matchState} />
            </div>
            <div className="max-h-64 overflow-y-auto flex-shrink-0">
              <FallOfWickets matchState={matchState} />
            </div>

            <div className="flex-shrink-0">
              <BallInfo matchData={matchData} matchState={matchState} />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
