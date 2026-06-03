import { useState } from "react";
import { handleApiError, createTournament } from "../../services/api";

export default function CreateTournament() {
  const [formData, setFormData] = useState({
    name: "",
    location: "",
    type: "LEAGUE",
    overs: 20,
    maxTeams: 4,
    startDate: "",
    endDate: "",
    logoUrl: ""
  });

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    console.log(formData);
    try {
      await createTournament(formData);
    } catch (error) {
      handleApiError(error);
    }
  };

  return (
    <div className="min-h-screen bg-slate-100 flex justify-center items-center p-6">
      <div className="bg-white p-8 rounded-3xl shadow-lg w-full max-w-3xl">
        <h1 className="text-3xl font-bold mb-6">Create Tournament</h1>

        <form onSubmit={handleSubmit} className="space-y-5">
          {/* Tournament Name */}
          <div>
            <label className="block mb-2 font-medium text-slate-700">
              Tournament Name
            </label>
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder="Enter tournament name"
              className="w-full border border-slate-300 rounded-xl p-3"
            />
          </div>

          {/* Location */}
          <div>
            <label className="block mb-2 font-medium text-slate-700">
              Location
            </label>
            <input
              type="text"
              name="location"
              value={formData.location}
              onChange={handleChange}
              placeholder="Enter location"
              className="w-full border border-slate-300 rounded-xl p-3"
            />
          </div>

          {/* Tournament Type */}
          <div>
            <label className="block mb-2 font-medium text-slate-700">
              Tournament Type
            </label>
            <select
              name="type"
              value={formData.type}
              onChange={handleChange}
              className="w-full border border-slate-300 rounded-xl p-3"
            >
              <option value="LEAGUE">League</option>
              <option value="KNOCKOUT">Knockout</option>
              <option value="ROUND_ROBIN">Round Robin</option>
            </select>
          </div>

          {/* Overs */}
          <div>
            <label className="block mb-2 font-medium text-slate-700">
              Overs Per Match
            </label>
            <input
              type="number"
              name="overs"
              value={formData.overs}
              onChange={handleChange}
              className="w-full border border-slate-300 rounded-xl p-3"
            />
          </div>

          {/* Maximum Teams */}
          <div>
            <label className="block mb-2 font-medium text-slate-700">
              Maximum Teams
            </label>
            <input
              type="number"
              name="maxTeams"
              value={formData.maxTeams}
              onChange={handleChange}
              className="w-full border border-slate-300 rounded-xl p-3"
            />
          </div>

          {/* Start & End Dates */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block mb-2 font-medium text-slate-700">
                Start Date
              </label>
              <input
                type="date"
                name="startDate"
                value={formData.startDate}
                onChange={handleChange}
                className="w-full border border-slate-300 rounded-xl p-3"
              />
            </div>

            <div>
              <label className="block mb-2 font-medium text-slate-700">
                End Date
              </label>
              <input
                type="date"
                name="endDate"
                value={formData.endDate}
                onChange={handleChange}
                className="w-full border border-slate-300 rounded-xl p-3"
              />
            </div>
          </div>

          {/* Logo URL */}
          <div>
            <label className="block mb-2 font-medium text-slate-700">
              Tournament Logo URL
            </label>
            <input
              type="text"
              name="logoUrl"
              value={formData.logoUrl}
              onChange={handleChange}
              placeholder="https://example.com/logo.png"
              className="w-full border border-slate-300 rounded-xl p-3"
            />
          </div>

          <button
            type="submit"
            className="w-full bg-purple-600 text-white py-3 rounded-xl hover:bg-purple-700 transition"
          >
            Create Tournament
          </button>
        </form>
      </div>
    </div>
  );
}
