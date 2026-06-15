import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import {
  getPlayerById,
  getAllTeams,
  assignTeamToPlayer,
} from "../services/api";

export default function PlayerDetailsPage() {
  const { id } = useParams();

  const [player, setPlayer] = useState(null);

  const [showAssignModal, setShowAssignModal] = useState(false);
  const [teams, setTeams] = useState([]);
  const [selectedTeamId, setSelectedTeamId] = useState("");

  useEffect(() => {
    loadPlayer();
  }, [id]);

  const loadPlayer = async () => {
    try {
      const res = await getPlayerById(id);
      console.log(res);
      setPlayer(res);

      if (res.teamDTO) {
        setSelectedTeamId(res.teamDTO.id);
      }
    } catch (err) {
      console.error(err);
      alert("Failed to load player");
    }
  };

  const loadTeams = async () => {
    try {
      const res = await getAllTeams();
      setTeams(res);
    } catch (err) {
      console.error(err);
      alert("Failed to load teams");
    }
  };

  const openAssignModal = async () => {
    await loadTeams();
    setShowAssignModal(true);
  };

  const saveTeam = async () => {
    if (!selectedTeamId) {
      alert("Please select a team");
      return;
    }

    try {
      await assignTeamToPlayer(id, selectedTeamId);

      await loadPlayer();

      setShowAssignModal(false);

      alert("Team assigned successfully");
    } catch (err) {
      console.error(err);
      alert("Failed to assign team");
    }
  };

  if (!player) {
    return (
      <div className="flex justify-center items-center h-screen">
        Loading...
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-100 p-8">
      <div className="max-w-4xl mx-auto bg-white rounded-3xl shadow-xl p-8">
        {/* Header */}
        <div className="flex justify-between items-start mb-8">
          <div>
            <h1 className="text-4xl font-bold">{player.fullName}</h1>

            <p className="text-slate-500 mt-2">{player.shortName}</p>
          </div>

          <button
            onClick={openAssignModal}
            className="bg-blue-600 text-white px-5 py-3 rounded-xl hover:bg-blue-700 transition"
          >
            Assign Team
          </button>
        </div>

        {/* Team Card */}
        <div className="bg-slate-50 rounded-2xl p-5 mb-8 border">
          <p className="text-slate-500 text-sm">Current Team</p>

          <p className="font-bold text-xl text-slate-800 mt-1">
            {player.teamDTO?.name || "No Team Assigned"}
          </p>

          {player.teamDTO?.shortName && (
            <p className="text-slate-500">{player.teamDTO.shortName}</p>
          )}
        </div>

        {/* Player Info */}
        <div className="grid md:grid-cols-2 gap-6">
          <Info label="Role" value={player.role} />

          <Info label="Batting Style" value={player.battingStyle} />

          <Info label="Bowling Style" value={player.bowlingStyle} />

          <Info label="Date Of Birth" value={player.dateOfBirth} />

          <Info label="Jersey Number" value={player.jerseyNumber} />

          <Info label="Nationality" value={player.nationality} />

          <Info label="Captain" value={player.captain ? "Yes" : "No"} />

          <Info
            label="Wicket Keeper"
            value={player.wicketKeeper ? "Yes" : "No"}
          />

          <Info label="Matches" value={player.matchesPlayed} />

          <Info label="Runs" value={player.runs} />

          <Info label="Wickets" value={player.wickets} />
        </div>
      </div>

      {/* Assign Team Modal */}
      {showAssignModal && (
        <div className="fixed inset-0 bg-black/40 flex justify-center items-center z-50">
          <div className="bg-white rounded-3xl p-6 w-full max-w-md shadow-xl">
            <h2 className="text-2xl font-bold mb-5">Assign Team</h2>

            <select
              value={selectedTeamId}
              onChange={(e) => setSelectedTeamId(Number(e.target.value))}
              className="w-full border rounded-xl p-3"
            >
              <option value="">Select Team</option>

              {teams.map((team) => (
                <option key={team.id} value={team.id}>
                  {team.name}
                </option>
              ))}
            </select>

            <div className="flex justify-end gap-3 mt-6">
              <button
                onClick={() => setShowAssignModal(false)}
                className="px-5 py-2 bg-slate-200 rounded-xl"
              >
                Cancel
              </button>

              <button
                onClick={saveTeam}
                className="px-5 py-2 bg-blue-600 text-white rounded-xl hover:bg-blue-700"
              >
                Save
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function Info({ label, value }) {
  return (
    <div className="bg-slate-50 rounded-2xl p-4 border">
      <p className="text-slate-500 text-sm">{label}</p>

      <p className="font-semibold text-lg mt-1">{value || "-"}</p>
    </div>
  );
}
