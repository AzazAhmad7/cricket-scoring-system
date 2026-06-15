import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  getAllTeams,
  getAllTeamsOfTournament,
  assignTeamsToTournament,
} from "../services/api";

export default function AssignTeamsPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [allTeams, setAllTeams] = useState([]);
  const [selectedTeams, setSelectedTeams] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [teams, assignedTeams] = await Promise.all([
        getAllTeams(),
        getAllTeamsOfTournament(id),
      ]);

      setAllTeams(teams || []);

      const assignedIds = assignedTeams?.map((t) => t.team.id) || [];

      setSelectedTeams(assignedIds);
    } catch (error) {
      console.error(error);
      alert("Failed to load teams");
    } finally {
      setLoading(false);
    }
  };

  const toggleTeam = (teamId) => {
    if (selectedTeams.includes(teamId)) {
      setSelectedTeams(selectedTeams.filter((id) => id !== teamId));
    } else {
      setSelectedTeams([...selectedTeams, teamId]);
    }
  };

  const saveTeams = async () => {
    try {
      setSaving(true);

      await assignTeamsToTournament(id, selectedTeams);

      alert("Teams assigned successfully");

      navigate(`/tournaments/${id}`);
    } catch (error) {
      console.error(error);
      alert("Failed to assign teams");
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex justify-center items-center">
        Loading Teams...
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-100 p-6">
      <div className="max-w-5xl mx-auto bg-white rounded-3xl shadow-lg p-8">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold">Assign Teams</h1>

          <button
            onClick={() => navigate(-1)}
            className="px-4 py-2 bg-slate-200 rounded-xl"
          >
            Back
          </button>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
          {allTeams.map((team) => (
            <label
              key={team.id}
              className={`border rounded-2xl p-4 cursor-pointer transition ${
                selectedTeams.includes(team.id)
                  ? "border-blue-500 bg-blue-50"
                  : "border-slate-200"
              }`}
            >
              <div className="flex items-center gap-3">
                <input
                  type="checkbox"
                  checked={selectedTeams.includes(team.id)}
                  onChange={() => toggleTeam(team.id)}
                />

                <div>
                  <h3 className="font-semibold">{team.name}</h3>

                  <p className="text-sm text-slate-500">{team.shortName}</p>
                </div>
              </div>
            </label>
          ))}
        </div>

        <div className="mt-8 flex justify-end">
          <button
            onClick={saveTeams}
            disabled={saving}
            className="bg-blue-600 text-white px-6 py-3 rounded-xl hover:bg-blue-700"
          >
            {saving ? "Saving..." : "Save Teams"}
          </button>
        </div>
      </div>
    </div>
  );
}
