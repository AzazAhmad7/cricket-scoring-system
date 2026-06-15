import { useEffect, useState } from "react";
import { FaEdit, FaTrash, FaUser, FaSearch } from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import { getAllPlayers, deletePlayer } from "../services/api";

export default function PlayersListPage() {
  const [players, setPlayers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");

  const navigate = useNavigate();

  useEffect(() => {
    fetchPlayers();
  }, []);

  const fetchPlayers = async () => {
    try {
      setLoading(true);

      const res = await getAllPlayers();

      setPlayers(res || []);
    } catch (err) {
      console.error(err);
      alert("Failed to load players");
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id, e) => {
    e.stopPropagation();

    const confirmed = window.confirm(
      "Are you sure you want to delete this player?",
    );

    if (!confirmed) return;

    try {
      await deletePlayer(id);

      setPlayers((prev) => prev.filter((p) => p.id !== id));
    } catch (err) {
      console.error(err);
      alert("Failed to delete player");
    }
  };

  const handleEdit = (id, e) => {
    e.stopPropagation();
    navigate(`/players/edit/${id}`);
  };

  const filteredPlayers = players.filter((player) =>
    [player.fullName, player.shortName, player.nationality, player.role]
      .join(" ")
      .toLowerCase()
      .includes(searchTerm.toLowerCase()),
  );

  if (loading) {
    return (
      <div className="flex justify-center items-center h-screen">
        <p className="text-xl text-slate-500">Loading Players...</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-100 p-8">
      <div className="max-w-7xl mx-auto">
        {/* Header */}

        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-8">
          <div>
            <h1 className="text-4xl font-bold text-slate-800">Players</h1>

            <p className="text-slate-500 mt-1">
              Total Players: {players.length}
            </p>
          </div>

          <button
            onClick={() => navigate("/players/create")}
            className="bg-blue-600 text-white px-5 py-3 rounded-xl hover:bg-blue-700 transition"
          >
            Add Player
          </button>
        </div>

        {/* Search */}

        <div className="relative mb-8">
          <FaSearch className="absolute left-4 top-4 text-slate-400" />

          <input
            type="text"
            placeholder="Search player by name, role, nationality..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full md:w-96 pl-12 pr-4 py-3 border border-slate-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
          />
        </div>

        {/* Empty State */}

        {filteredPlayers.length === 0 ? (
          <div className="bg-white rounded-3xl shadow-lg p-12 text-center">
            <FaUser className="mx-auto text-6xl text-slate-300 mb-4" />

            <h2 className="text-2xl font-bold text-slate-700">
              No Players Found
            </h2>

            <p className="text-slate-500 mt-2">
              {searchTerm
                ? "No player matches your search."
                : "Create your first player."}
            </p>
          </div>
        ) : (
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredPlayers.map((player) => (
              <div
                key={player.id}
                onClick={() => navigate(`/players/${player.id}`)}
                className="
                  bg-white
                  rounded-3xl
                  p-6
                  shadow-md
                  hover:shadow-xl
                  hover:-translate-y-1
                  transition-all
                  cursor-pointer
                "
              >
                <div className="flex justify-between items-start">
                  <div>
                    <h2 className="text-xl font-bold text-slate-800">
                      {player.fullName}
                    </h2>

                    <p className="text-slate-500 mt-1">{player.shortName}</p>
                  </div>

                  <div className="flex gap-3">
                    <button
                      onClick={(e) => handleEdit(player.id, e)}
                      className="text-amber-500 hover:text-amber-600"
                    >
                      <FaEdit size={18} />
                    </button>

                    <button
                      onClick={(e) => handleDelete(player.id, e)}
                      className="text-red-500 hover:text-red-600"
                    >
                      <FaTrash size={18} />
                    </button>
                  </div>
                </div>

                <div className="mt-5 space-y-2 text-sm text-slate-600">
                  <p>
                    <span className="font-semibold">Role:</span>{" "}
                    {player.role || "-"}
                  </p>

                  <p>
                    <span className="font-semibold">Batting:</span>{" "}
                    {player.battingStyle || "-"}
                  </p>

                  <p>
                    <span className="font-semibold">Bowling:</span>{" "}
                    {player.bowlingStyle || "-"}
                  </p>

                  <p>
                    <span className="font-semibold">Nationality:</span>{" "}
                    {player.nationality || "-"}
                  </p>

                  {player.jerseyNumber && (
                    <p>
                      <span className="font-semibold">Jersey:</span> #
                      {player.jerseyNumber}
                    </p>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
