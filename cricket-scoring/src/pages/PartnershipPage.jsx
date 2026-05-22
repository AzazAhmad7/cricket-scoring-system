// pages/ScoreCardPage.jsx

import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { getMatchAllData } from "../services/api";
import Sidebar from "../components/scoringUi/Sidebar";
import Header from "../components/scoringUi/Header";
import Partnership from "../components/scoringUi/Partnership";

export default function PartnershipPage() {
  const { matchId } = useParams();

  const [matchData, setMatchData] = useState(null);
  const [matchState, setMatchState] = useState(null);

  useEffect(() => {
    fetchMatch();
  }, []);

  const fetchMatch = async () => {
    const res = await getMatchAllData(matchId);
    setMatchData(res.data.setupFile);
    setMatchState(res.data.matchState);
  };

  if (!matchData || !matchState) return null;

  return (
    <div className="h-screen bg-slate-100 flex text-sm overflow-hidden">
      {/* Sidebar */}
      <Sidebar matchState={matchState} />

      {/* Main Content */}
      <div className="flex-1 flex flex-col min-w-0">
        <Header data={matchData} />

        <div className="flex-1 overflow-auto p-3">
          <Partnership matchData={matchData} matchState={matchState} />
        </div>
      </div>
    </div>
  );
}
