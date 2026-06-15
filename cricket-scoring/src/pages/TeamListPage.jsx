import { useEffect, useState } from "react";
import { FaEdit, FaTrash } from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import { getAllTeams, deleteTeam, handleApiError } from "../services/api";

export default function TeamsListPage() {
  const navigate = useNavigate();

  const [teams, setTeams] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadTeams();
  }, []);

  const loadTeams = async () => {
    try {
      const res = await getAllTeams();
      setTeams(res);
    } catch (err) {
      console.error(err);
      alert("Failed to load teams");
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    const confirmDelete = window.confirm(
      "Are you sure you want to delete this team?",
    );

    if (!confirmDelete) return;

    try {
      await deleteTeam(id);

      setTeams((prev) => prev.filter((team) => team.id !== id));

      alert("Team deleted successfully");
    } catch (err) {
      handleApiError(err);
      alert("Failed to delete team");
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-100">
        <p className="text-slate-500 text-lg">Loading Teams...</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-100 p-6">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="flex flex-col md:flex-row justify-between md:items-center gap-4 mb-10">
          <div>
            <h1 className="text-4xl font-bold text-slate-800">Teams</h1>

            <p className="text-slate-500 mt-2">Manage all cricket teams</p>
          </div>

          <button
            onClick={() => navigate("/teams/create")}
            className="bg-blue-600 text-white px-6 py-3 rounded-2xl font-semibold hover:bg-blue-700 transition"
          >
            + Create Team
          </button>
        </div>

        {/* Empty State */}
        {teams.length === 0 ? (
          <div className="bg-white rounded-3xl shadow-md p-16 text-center">
            <h2 className="text-2xl font-bold text-slate-700">
              No Teams Found
            </h2>

            <p className="text-slate-500 mt-3">
              Create your first team to get started.
            </p>

            <button
              onClick={() => navigate("/teams/create")}
              className="mt-6 bg-blue-600 text-white px-6 py-3 rounded-xl hover:bg-blue-700 transition"
            >
              Create Team
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {teams.map((team) => (
              <div
                key={team.id}
                className="bg-white rounded-3xl shadow-md hover:shadow-xl transition-all duration-300 overflow-hidden border border-slate-200"
              >
                {/* Team Color Strip */}
                <div
                  className="h-2"
                  style={{
                    backgroundColor: team.primaryColor || "#2563eb",
                  }}
                />

                {/* Clickable Content */}
                <div
                  onClick={() => navigate(`/teams/${team.id}`)}
                  className="p-6 cursor-pointer"
                >
                  {/* Logo */}
                  <div className="flex justify-center mb-5">
                    {team.logoUrl ? (
                      <img
                        src={team.logoUrl}
                        alt={team.name}
                        className="w-24 h-24 object-contain"
                      />
                    ) : (
                      <div className="w-24 h-24 rounded-full bg-slate-200 flex items-center justify-center text-xl font-bold text-slate-600">
                        {team.shortName}
                      </div>
                    )}
                  </div>

                  {/* Team Name */}
                  <h2 className="text-xl font-bold text-center text-slate-800">
                    {team.name}
                  </h2>

                  <p className="text-center text-slate-500 mt-1">
                    {team.shortName}
                  </p>

                  {/* Team Details */}
                  <div className="mt-6 space-y-3">
                    <div className="flex justify-between">
                      <span className="text-slate-500">Country</span>

                      <span className="font-medium">{team.country || "-"}</span>
                    </div>

                    <div className="flex justify-between">
                      <span className="text-slate-500">Coach</span>

                      <span className="font-medium truncate ml-3">
                        {team.coachName || "-"}
                      </span>
                    </div>

                    <div className="flex justify-between">
                      <span className="text-slate-500">Players</span>

                      <span className="font-medium">
                        {team.players?.length || 0}
                      </span>
                    </div>

                    <div className="flex justify-between">
                      <span className="text-slate-500">Ranking</span>

                      <span className="font-medium">{team.ranking || "-"}</span>
                    </div>
                  </div>
                </div>

                {/* Actions */}
                <div className="border-t border-slate-200 flex">
                  <button
                    onClick={() => navigate(`/teams/edit/${team.id}`)}
                    className="flex-1 py-4 text-amber-500 hover:bg-amber-50 flex justify-center items-center gap-2 transition"
                  >
                    <FaEdit />
                    Edit
                  </button>

                  <div className="w-px bg-slate-200" />

                  <button
                    onClick={() => handleDelete(team.id)}
                    className="flex-1 py-4 text-red-500 hover:bg-red-50 flex justify-center items-center gap-2 transition"
                  >
                    <FaTrash />
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
