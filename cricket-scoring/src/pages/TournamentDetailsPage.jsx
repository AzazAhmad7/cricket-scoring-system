import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { FaArrowLeft, FaEdit, FaTrophy } from "react-icons/fa";
import { getAllTeamsOfTournament, getAllTournaments, getTournamentById } from "../services/api";

export default function TournamentDetailsPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [tournament, setTournament] = useState(null);
  const [teams, setTeams] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadTournament();
  }, [id]);

  const loadTournament = async () => {
    try {
      const data = await getAllTeamsOfTournament(id);
      console.log(data);
      setTeams(data || []);
      const tour = await getTournamentById(id);
      console.log(tour)
      setTournament(tour);
    } catch (error) {
      console.error(error);
      alert("Failed to load tournament");
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex justify-center items-center bg-slate-100">
        <p className="text-slate-500 text-lg">Loading Tournament...</p>
      </div>
    );
  }

  if (!tournament) {
    return (
      <div className="min-h-screen flex justify-center items-center bg-slate-100">
        <p className="text-red-500 text-lg">Tournament not found</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-100 p-6">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="flex justify-between items-center mb-8">
          <button
            onClick={() => navigate("/tournaments")}
            className="flex items-center gap-2 text-slate-600 hover:text-slate-900"
          >
            <FaArrowLeft />
            Back
          </button>

          <button
            onClick={() => navigate(`/tournaments/edit/${tournament.id}`)}
            className="bg-amber-500 text-white px-5 py-2 rounded-xl hover:bg-amber-600 flex items-center gap-2"
          >
            <FaEdit />
            Edit Tournament
          </button>
        </div>

        {/* Main Card */}
        <div className="bg-white rounded-3xl shadow-lg overflow-hidden">
          <div className="h-3 bg-yellow-500" />

          <div className="p-8">
            {/* Tournament Header */}
            <div className="flex flex-col items-center">
              <div className="w-28 h-28 rounded-full bg-yellow-100 flex items-center justify-center">
                <FaTrophy className="text-yellow-500 text-5xl" />
              </div>

              <h1 className="text-4xl font-bold text-slate-800 mt-5">
                {tournament.name}
              </h1>

              <p className="text-slate-500 mt-2">{tournament.location}</p>
            </div>

            {/* Tournament Information */}
            <div className="grid md:grid-cols-2 gap-6 mt-10">
              <div className="bg-slate-50 rounded-2xl p-6">
                <h3 className="font-semibold text-slate-700 mb-4">
                  Tournament Information
                </h3>

                <div className="space-y-4">
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

              {/* Statistics */}
              <div className="bg-slate-50 rounded-2xl p-6">
                <h3 className="font-semibold text-slate-700 mb-4">
                  Statistics
                </h3>

                <div className="space-y-4">
                  <div className="flex justify-between">
                    <span className="text-slate-500">Teams</span>
                    <span className="font-medium">{teams.length}</span>
                  </div>

                  <div className="flex justify-between">
                    <span className="text-slate-500">Matches</span>
                    <span className="font-medium">
                      {tournament.matches.length}
                    </span>
                  </div>

                  <div className="flex justify-between">
                    <span className="text-slate-500">Start Date</span>
                    <span className="font-medium">
                      {tournament.startDate || "-"}
                    </span>
                  </div>

                  <div className="flex justify-between">
                    <span className="text-slate-500">End Date</span>
                    <span className="font-medium">
                      {tournament.endDate || "-"}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            {/* Participating Teams */}
            <div className="mt-10">
              <div className="flex justify-between items-center mb-5">
                <h2 className="text-2xl font-bold text-slate-800">
                  Participating Teams ({teams.length})
                </h2>

                <button
                  onClick={() =>
                    navigate(`/tournaments/${tournament.id}/assign-teams`)
                  }
                  className="bg-blue-600 text-white px-4 py-2 rounded-xl hover:bg-blue-700"
                >
                  Assign Teams
                </button>
              </div>

              {teams.length > 0 ? (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
                  {teams.map((item) => (
                    <div
                      key={item.id}
                      className="bg-slate-50 rounded-2xl border border-slate-200 p-5 hover:shadow-md transition"
                    >
                      <div className="flex items-center gap-4">
                        {item.team.logoUrl ? (
                          <img
                            src={item.team.logoUrl}
                            alt={item.team.name}
                            className="w-14 h-14 object-contain"
                          />
                        ) : (
                          <div className="w-14 h-14 rounded-full bg-slate-200 flex items-center justify-center font-bold text-slate-700">
                            {item.team.shortName}
                          </div>
                        )}

                        <div>
                          <h3 className="font-bold text-slate-800">
                            {item.team.name}
                          </h3>

                          <p className="text-slate-500 text-sm">
                            {item.team.shortName}
                          </p>
                        </div>
                      </div>

                      <div className="mt-4 space-y-2 text-sm">
                        <div className="flex justify-between">
                          <span className="text-slate-500">Country</span>

                          <span>{item.team.country || "-"}</span>
                        </div>

                        <div className="flex justify-between">
                          <span className="text-slate-500">Coach</span>

                          <span>{item.team.coachName || "-"}</span>
                        </div>

                        <div className="flex justify-between">
                          <span className="text-slate-500">Ranking</span>

                          <span>{item.team.ranking || "-"}</span>
                        </div>

                        <div className="flex justify-between">
                          <span className="text-slate-500">Seed</span>

                          <span>{item.seed || "-"}</span>
                        </div>

                        {item.groupName && (
                          <div className="flex justify-between">
                            <span className="text-slate-500">Group</span>

                            <span>{item.groupName}</span>
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="bg-slate-50 rounded-2xl p-8 text-center text-slate-500">
                  No teams added yet.
                </div>
              )}
            </div>
            {/* Matches Section */}
            <div className="mt-12">
              <h2 className="text-2xl font-bold text-slate-800 mb-5">
                Matches ({tournament.matches?.length || 0})
              </h2>

              {tournament.matches?.length > 0 ? (
                <div className="space-y-4">
                  {tournament.matches.map((match) => (
                    <div
                      key={match.id}
                      className="bg-slate-50 border border-slate-200 rounded-2xl p-5 hover:shadow-md transition"
                    >
                      <div className="flex flex-col md:flex-row md:justify-between md:items-center gap-4">
                        <div>
                          <h3 className="font-bold text-lg text-slate-800">
                            {match.homeTeam?.shortName || "TBD"} vs{" "}
                            {match.awayTeam?.shortName || "TBD"}
                          </h3>

                          <p className="text-slate-500 text-sm mt-1">
                            {match.venue.name || "Venue TBD"}
                          </p>
                        </div>

                        <div className="text-right">
                          <div
                            className={`inline-block px-3 py-1 rounded-full text-sm font-medium ${
                              match.status === "COMPLETED"
                                ? "bg-green-100 text-green-700"
                                : match.status === "LIVE"
                                  ? "bg-red-100 text-red-700"
                                  : "bg-yellow-100 text-yellow-700"
                            }`}
                          >
                            {match.status != null
                              ? match.status
                              : match.matchDate}
                          </div>

                          <p className="text-sm text-slate-500 mt-2">
                            {match.matchDate || "-"}
                          </p>
                        </div>
                      </div>

                      {match.result && (
                        <div className="mt-4 border-t pt-4 text-sm text-slate-700">
                          {match.result}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              ) : (
                <div className="bg-slate-50 rounded-2xl p-8 text-center text-slate-500">
                  No matches scheduled yet.
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
