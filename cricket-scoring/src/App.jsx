import { BrowserRouter, Routes, Route } from "react-router-dom";
import HomePage from "./pages/HomePage";
import CricketMatchUIStepWise from "./componentFromClaud/CricketMatchUIStepWise";
import ScoringDashBoard from "./pages/ScoringDashboard";
import CreateMatchPage from "./pages/CreateMatchPage";
import ScoreCardPage from "./pages/ScoreCardPage";
import Players from "./components/scoringUi/Players";
import PlayerPage from "./pages/PlayersPage";
import PartnershipPage from "./pages/PartnershipPage";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<HomePage />} />

        {/* Create new match */}
        <Route path="/matches/create" element={<CricketMatchUIStepWise />} />

        {/* Load match for scoring */}
        <Route path="/score/:matchId" element={<ScoringDashBoard />} />

        {/* Edit existing match */}
        <Route
          path="/edit-match/:matchId"
          element={<CricketMatchUIStepWise mode="edit" />}
        />

        <Route path="/scorecard/:matchId" element={<ScoreCardPage />} />
        <Route path="/partnership/:matchId" element={<PartnershipPage />} />
        <Route path="/players/:matchId" element={<PlayerPage />} />
      </Routes>
    </BrowserRouter>
  );
}
