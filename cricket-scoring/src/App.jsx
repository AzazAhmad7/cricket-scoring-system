import { BrowserRouter, Routes, Route } from "react-router-dom";
import HomePage from "./pages/HomePage";
import CricketMatchUIStepWise from "./componentFromClaud/CricketMatchUIStepWise";
import ScoringDashBoard from "./pages/ScoringDashboard";
import CreateMatchPage from "./pages/CreateMatchPage";
import ScoreCardPage from "./pages/ScoreCardPage";
import Players from "./components/scoringUi/Players";
import PlayerPage from "./pages/PlayersPage";
import PartnershipPage from "./pages/PartnershipPage";
import TeamsListPage from "./pages/TeamListPage";
import EditTeamPage from "./pages/EditTeamPage";
import CreateTeamPage from "./pages/CreateTeamPage";
import TeamDetailsPage from "./pages/TeamDetailsPage";
import PlayersListPage from "./pages/PlayersListPage";
import CreatePlayerPage from "./pages/CreatePlayerPage";
import PlayerDetailsPage from "./pages/PlayerDetailsPage";
import EditPlayerPage from "./pages/EditPlayerPage";
import TournamentListPage from "./pages/TournamentListPage";
import CreateTournament from "./pages/CreateTournamentPage";
import TournamentDetailsPage from "./pages/TournamentDetailsPage";
import AssignTeamsPage from "./pages/AssignTeamsPage";
import ScoreBug from "./graphics/scorebug";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/scorebug" element={<ScoreBug />} />

        {/*Team Related Routes */}
        <Route path="/teams/create" element={<CreateTeamPage />} />
        <Route path="/teams" element={<TeamsListPage />} />
        <Route path="/teams/:id" element={<TeamDetailsPage />} />
        <Route path="/teams/edit/:id" element={<EditTeamPage />} />

        {/*player related routes */}
        <Route path="/players/create" element={<CreatePlayerPage />} />
        <Route path="/players" element={<PlayersListPage />} />
        <Route path="/players/:id" element={<PlayerDetailsPage />} />
        <Route path="/players/edit/:id" element={<EditPlayerPage />} />

        {/*Tournament related routes */}
        <Route path="/tournaments/create" element={<CreateTournament />} />
        <Route path="/tournaments" element={<TournamentListPage />} />
        <Route path="/tournaments/:id" element={<TournamentDetailsPage />} />
        <Route
          path="/tournaments/:id/assign-teams"
          element={<AssignTeamsPage />}
        />

        {/* Create new match */}
        <Route
          path="/matches/create"
          element={<CricketMatchUIStepWise mode="create" />}
        />

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
