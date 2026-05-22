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
} from "../services/api";
import { useParams } from "react-router-dom";

export default function ScoringDashboard() {
  const { matchId } = useParams();
  const [matchAllData, setMatchAllData] = useState(null);
  const [matchData, setMatchData] = useState(null);
  const [matchState, setMatchState] = useState(null);

  const fetchMatch = async () => {
    console.log("match Id ", matchId);
    const res = await getMatchAllData(matchId);
    setMatchAllData(res.data);
    setMatchData(res.data.setupFile);
    setMatchState(res.data.matchState);
  };

  useEffect(() => {
    fetchMatch();
  }, []);

  const handleBallEvent = async (thisEventData) => {
    // If a simple string is passed, e.g. "ONE", "FOUR"
    console.log(thisEventData);
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

            //any ball extra runs
            isWideAnyBall: thisEventData.isWide,
            isNoBallAnyBall: thisEventData.isNoBall,
            isByeAnyBall: thisEventData.isBye,
            isLegByeAnyBall: thisEventData.isLegBye,
            runsOfByeAnyBall: thisEventData.runsOfBye,
            runsOffBatAnyBall: thisEventData.runsOfBat,
          };

    console.log("Event to send:", event);
    await scoreBall(event);

    await fetchMatch();
  };

  const handleEndOverEvent = async () => {
    await endOver({
      matchId: matchData.matchInfo.matchId,
      eventType: "END_OVER",
    });

    await fetchMatch();
  };

  const handleChangeInning = async () => {
    await changeInning({
      matchId: matchData.matchInfo.matchId,
    });
    await fetchMatch();
  };

  const handleNewBatterEvent = async (thisEventData) => {
    await logNewBatter({
      matchId: matchData.matchInfo.matchId,
      eventType: thisEventData.eventType,
      playerId: thisEventData.playerId,
    });
    await fetchMatch();
  };
  const handleNewBowlerEvent = async (thisEventData) => {
    await logNewBowler({
      matchId: matchData.matchInfo.matchId,
      eventType: thisEventData.eventType,
      playerId: thisEventData.playerId,
    });
    await fetchMatch();
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
            <ScoreSummary matchState={matchState} teams={matchData.teams} />

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
