import { BrowserRouter, Routes, Route } from "react-router-dom";
import HomePage from "./pages/HomePage";
import CricketMatchUIStepWise from "./componentFromClaud/CricketMatchUIStepWise";
import ScoringDashBoard from "./pages/ScoringDashboard";
import CreateMatchPage from "./pages/CreateMatchPage";
import ScoreCardPage from "./pages/ScoreCardPage";
import Players from "./components/scoringUi/Players";
import PlayerPage from "./pages/PlayersPage";
import PartnershipPage from "./pages/PartnershipPage";
import CreateTournament from "./components/setupUI/Tournament";
import TeamsListPage from "./pages/TeamListPage";
import EditTeamPage from "./pages/EditTeamPage";
import CreateTeamPage from "./pages/CreateTeamPage";
import TeamDetailsPage from "./pages/TeamDetailsPage";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<HomePage />} />

        {/*Team Related Routes */}
        <Route path="/teams/create" element={<CreateTeamPage />} />
        <Route path="/teams" element={<TeamsListPage />} />
        <Route path="/teams/:id" element={<TeamDetailsPage />} />
        <Route path="/teams/edit/:id" element={<EditTeamPage />} />
        {/* Create new match */}
        <Route
          path="/matches/create"
          element={<CricketMatchUIStepWise mode="create" />}
        />
        {/*Create tournament*/}
        <Route path="/tournaments/create" element={<CreateTournament />} />

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
