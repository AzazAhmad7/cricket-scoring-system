import { useEffect, useState } from "react";
import { FaEdit, FaTrash, FaTrophy } from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import { getAllTournaments, deleteTournament } from "../services/api";

export default function TournamentListPage() {
  const navigate = useNavigate();

  const [tournaments, setTournaments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadTournaments();
  }, []);

  const loadTournaments = async () => {
    try {
      const res = await getAllTournaments();
      setTournaments(res);
    } catch (err) {
      console.error(err);
      alert("Failed to load tournaments");
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    const confirmDelete = window.confirm(
      "Are you sure you want to delete this tournament?",
    );

    if (!confirmDelete) return;

    try {
      await deleteTournament(id);

      setTournaments((prev) =>
        prev.filter((tournament) => tournament.id !== id),
      );

      alert("Tournament deleted successfully");
    } catch (err) {
      console.error(err);
      alert("Failed to delete tournament");
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-100">
        <p className="text-slate-500 text-lg">Loading Tournaments...</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-100 p-6">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="flex flex-col md:flex-row justify-between md:items-center gap-4 mb-10">
          <div>
            <h1 className="text-4xl font-bold text-slate-800">Tournaments</h1>

            <p className="text-slate-500 mt-2">
              Manage all cricket tournaments
            </p>
          </div>

          <button
            onClick={() => navigate("/tournaments/create")}
            className="bg-blue-600 text-white px-6 py-3 rounded-2xl font-semibold hover:bg-blue-700 transition"
          >
            + Create Tournament
          </button>
        </div>

        {/* Empty State */}
        {tournaments.length === 0 ? (
          <div className="bg-white rounded-3xl shadow-md p-16 text-center">
            <h2 className="text-2xl font-bold text-slate-700">
              No Tournaments Found
            </h2>

            <p className="text-slate-500 mt-3">
              Create your first tournament to get started.
            </p>

            <button
              onClick={() => navigate("/tournaments/create")}
              className="mt-6 bg-blue-600 text-white px-6 py-3 rounded-xl hover:bg-blue-700 transition"
            >
              Create Tournament
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {tournaments.map((tournament) => (
              <div
                key={tournament.id}
                className="bg-white rounded-3xl shadow-md hover:shadow-xl transition-all duration-300 overflow-hidden border border-slate-200"
              >
                {/* Tournament Header Strip */}
                <div className="h-2 bg-yellow-500" />

                {/* Clickable Content */}
                <div
                  onClick={() => navigate(`/tournaments/${tournament.id}`)}
                  className="p-6 cursor-pointer"
                >
                  {/* Trophy */}
                  <div className="flex justify-center mb-5">
                    <div className="w-24 h-24 rounded-full bg-yellow-100 flex items-center justify-center">
                      <FaTrophy className="text-yellow-500 text-4xl" />
                    </div>
                  </div>

                  {/* Tournament Name */}
                  <h2 className="text-xl font-bold text-center text-slate-800">
                    {tournament.name}
                  </h2>

                  <p className="text-center text-slate-500 mt-1">
                    {tournament.location}
                  </p>

                  {/* Tournament Details */}
                  <div className="mt-6 space-y-3">
                    <div className="flex justify-between">
                      <span className="text-slate-500">Type</span>
                      <span className="font-medium">{tournament.type}</span>
                    </div>

                    <div className="flex justify-between">
                      <span className="text-slate-500">Overs</span>
                      <span className="font-medium">{tournament.overs}</span>
                    </div>

                    <div className="flex justify-between">
                      <span className="text-slate-500">Max Teams</span>
                      <span className="font-medium">{tournament.maxTeams}</span>
                    </div>

                    <div className="flex justify-between">
                      <span className="text-slate-500">Status</span>

                      <span
                        className={`font-medium ${
                          tournament.status === "ONGOING"
                            ? "text-green-600"
                            : tournament.status === "UPCOMING"
                              ? "text-yellow-600"
                              : "text-red-600"
                        }`}
                      >
                        {tournament.status}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Actions */}
                <div className="border-t border-slate-200 flex">
                  <button
                    // onClick={() =>
                    //   navigate(`/tournaments/edit/${tournament.id}`)
                    // }
                    className="flex-1 py-4 text-amber-500 hover:bg-amber-50 flex justify-center items-center gap-2 transition"
                  >
                    <FaEdit />
                    Edit
                  </button>

                  <div className="w-px bg-slate-200" />

                  <button
                    onClick={() => handleDelete(tournament.id)}
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
