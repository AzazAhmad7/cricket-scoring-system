import { useEffect, useState } from "react";
import { data, useNavigate } from "react-router-dom";
import { getAllMatches } from "../services/api";

export default function HomePage() {
  const navigate = useNavigate();

  const [matches, setMatches] = useState([]);
  const [loading, setLoading] = useState(true);

  const [selectedLoadMatch, setSelectedLoadMatch] = useState("");
  const [selectedEditMatch, setSelectedEditMatch] = useState("");

  useEffect(() => {
    const fetchMatches = async () => {
      try {
        // Start loading
        setLoading(true);

        // Fetch all matches
        const res = await getAllMatches();

        console.log(res.data);
        // Update state
        setMatches(res.data);
      } catch (err) {
        console.error("Failed to load matches:", err);
        alert("Failed to load matches");
      } finally {
        // Stop loading whether request succeeds or fails
        setLoading(false);
      }
    };

    fetchMatches();
  }, []);
  const handleCreateMatch = () => {
    navigate("/matches/create");
  };

  const handleLoadMatch = () => {
    if (!selectedLoadMatch) {
      alert("Please select a match.");
      return;
    }
    console.log("selected match ", selectedLoadMatch);
    navigate(`/score/${selectedLoadMatch}`);
  };

  const handleEditMatch = () => {
    if (!selectedEditMatch) {
      alert("Please select a match.");
      return;
    }

    navigate(`/edit-match/${selectedEditMatch}`);
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-100">
        <p className="text-slate-500">Loading matches...</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-100 flex items-center justify-center p-6">
      <div className="bg-white rounded-3xl shadow-xl border border-slate-200 p-10 w-full max-w-2xl">
        <h1 className="text-4xl font-bold text-slate-800 text-center mb-2">
          Cricket Scoring System
        </h1>

        <p className="text-slate-500 text-center mb-10">
          Create, load, or edit a match
        </p>

        <div className="space-y-8">
          {/* Create Match */}
          <div>
            <button
              onClick={handleCreateMatch}
              className="w-full py-4 rounded-2xl bg-blue-600 text-white font-semibold text-lg hover:bg-blue-700 transition"
            >
              Create Match
            </button>
          </div>

          {/* Load Match */}
          <div className="space-y-3">
            <h2 className="text-lg font-semibold text-slate-700">Load Match</h2>

            <select
              value={selectedLoadMatch}
              onChange={(e) => setSelectedLoadMatch(e.target.value)}
              className="w-full border border-slate-300 rounded-xl px-4 py-3"
            >
              <option value="">Select Match</option>
              {matches.map((match) => (
                <option key={match.id} value={match.id}>
                  {`Match - ${match.id}, ${match.matchName}`}
                </option>
              ))}
            </select>

            <button
              onClick={handleLoadMatch}
              className="w-full py-3 rounded-xl bg-green-600 text-white font-medium hover:bg-green-700 transition"
            >
              Load Selected Match
            </button>
          </div>

          {/* Edit Match */}
          <div className="space-y-3">
            <h2 className="text-lg font-semibold text-slate-700">Edit Match</h2>

            <select
              value={selectedEditMatch}
              onChange={(e) => setSelectedEditMatch(e.target.value)}
              className="w-full border border-slate-300 rounded-xl px-4 py-3"
            >
              <option value="">Select Match</option>
              {matches.map((match) => (
                <option key={match.id} value={match.id}>
                  {`Match - ${match.id}, ${match.matchName}`}
                </option>
              ))}
            </select>

            <button
              onClick={handleEditMatch}
              className="w-full py-3 rounded-xl bg-amber-500 text-white font-medium hover:bg-amber-600 transition"
            >
              Edit Selected Match
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
