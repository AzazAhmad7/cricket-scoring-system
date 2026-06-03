import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  FaUsers,
  FaTrophy,
  FaPlusCircle,
  FaPlayCircle,
  FaEdit,
} from "react-icons/fa";
import { GiCricketBat } from "react-icons/gi";
import { getAllMatches } from "../services/api";

export default function HomePage() {
  const navigate = useNavigate();

  const [matches, setMatches] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchMatches();
  }, []);

  const fetchMatches = async () => {
    try {
      setLoading(true);

      const res = await getAllMatches();

      setMatches(res.data || []);
    } catch (err) {
      console.error(err);
      alert("Failed to load matches");
    } finally {
      setLoading(false);
    }
  };

  const dashboardCards = [
    {
      title: "Create Match",
      icon: <GiCricketBat size={40} />,
      color: "bg-blue-600",
      path: "/matches/create",
    },
    {
      title: "Tournament",
      icon: <FaTrophy size={36} />,
      color: "bg-purple-600",
      path: "/tournaments/create",
    },
    {
      title: "Teams",
      icon: <FaUsers size={36} />,
      color: "bg-indigo-600",
      path: "/teams",
    },
    {
      title: "Create Team",
      icon: <FaPlusCircle size={36} />,
      color: "bg-green-600",
      path: "/teams/create",
    },
  ];

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-100">
        <p className="text-lg text-slate-500">Loading Dashboard...</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-100 to-blue-50">
      <div className="max-w-7xl mx-auto p-8">
        {/* Hero Section */}
        <div className="text-center mb-12">
          <div className="text-7xl mb-4">🏏</div>

          <h1 className="text-5xl font-bold text-slate-800">
            Cricket Scoring Hub
          </h1>

          <p className="text-slate-500 text-lg mt-4">
            Manage Teams, Matches, Tournaments & Live Scoring
          </p>
        </div>

        {/* Stats Row */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
          <div className="bg-white rounded-3xl shadow-lg p-6">
            <p className="text-slate-500">Total Matches</p>

            <h2 className="text-4xl font-bold text-slate-800 mt-2">
              {matches.length}
            </h2>
          </div>

          <div className="bg-white rounded-3xl shadow-lg p-6">
            <p className="text-slate-500">System Status</p>

            <h2 className="text-4xl font-bold text-green-600 mt-2">Active</h2>
          </div>

          <div className="bg-white rounded-3xl shadow-lg p-6">
            <p className="text-slate-500">Scoring Engine</p>

            <h2 className="text-4xl font-bold text-blue-600 mt-2">Ready</h2>
          </div>
        </div>

        {/* Dashboard Actions */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-12">
          {dashboardCards.map((card) => (
            <div
              key={card.title}
              onClick={() => navigate(card.path)}
              className={`${card.color}
                text-white
                rounded-3xl
                p-8
                cursor-pointer
                hover:scale-105
                hover:shadow-xl
                transition-all duration-300`}
            >
              <div className="mb-5">{card.icon}</div>

              <h2 className="text-2xl font-bold">{card.title}</h2>
            </div>
          ))}
        </div>

        {/* Recent Matches */}
        <div className="bg-white rounded-3xl shadow-xl p-8">
          <div className="flex justify-between items-center mb-8">
            <h2 className="text-3xl font-bold text-slate-800">
              Recent Matches
            </h2>

            <button
              onClick={() => navigate("/matches/create")}
              className="bg-blue-600 text-white px-5 py-3 rounded-xl hover:bg-blue-700"
            >
              New Match
            </button>
          </div>

          {matches.length === 0 ? (
            <div className="text-center py-16">
              <h3 className="text-2xl font-semibold text-slate-700">
                No Matches Found
              </h3>

              <p className="text-slate-500 mt-3">
                Create your first cricket match.
              </p>

              <button
                onClick={() => navigate("/matches/create")}
                className="mt-6 bg-blue-600 text-white px-6 py-3 rounded-xl hover:bg-blue-700"
              >
                Create Match
              </button>
            </div>
          ) : (
            <div className="space-y-4">
              {matches.map((match) => (
                <div
                  key={match.id}
                  className="border border-slate-200 rounded-2xl p-5 hover:bg-slate-50 transition"
                >
                  <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
                    <div>
                      <h3 className="text-xl font-semibold text-slate-800">
                        {match.matchName}
                      </h3>

                      <p className="text-slate-500">Match ID: {match.id}</p>
                    </div>

                    <div className="flex gap-3">
                      <button
                        onClick={() => navigate(`/score/${match.id}`)}
                        className="bg-green-600 text-white px-5 py-3 rounded-xl hover:bg-green-700 flex items-center gap-2"
                      >
                        <FaPlayCircle />
                        Load
                      </button>

                      <button
                        onClick={() => navigate(`/edit-match/${match.id}`)}
                        className="bg-amber-500 text-white px-5 py-3 rounded-xl hover:bg-amber-600 flex items-center gap-2"
                      >
                        <FaEdit />
                        Edit
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
