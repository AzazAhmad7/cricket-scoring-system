import { useNavigate } from "react-router-dom";

export default function TeamForm({
  team,
  onChange,
  onSubmit,
  submitLabel = "Save Team",
  title = "Team",
  loading = false,
}) {
  const navigate = useNavigate();
  return (
    <div className="min-h-screen bg-slate-100 flex items-center justify-center p-6">
      <div className="bg-white rounded-3xl shadow-xl border border-slate-200 p-10 w-full max-w-3xl">
        <h1 className="text-3xl font-bold text-slate-800 text-center mb-8">
          {title}
        </h1>

        <form onSubmit={onSubmit} className="space-y-6">
          {/* Team Name */}
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-2">
              Team Name
            </label>

            <input
              type="text"
              name="name"
              value={team?.name || ""}
              onChange={onChange}
              placeholder="Mumbai Indians"
              className="w-full border border-slate-300 rounded-xl px-4 py-3"
              required
            />
          </div>

          {/* Short Name */}
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-2">
              Short Name
            </label>

            <input
              type="text"
              name="shortName"
              value={team?.shortName || ""}
              onChange={onChange}
              placeholder="MI"
              className="w-full border border-slate-300 rounded-xl px-4 py-3"
              required
            />
          </div>

          {/* Nickname */}
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-2">
              Nickname
            </label>

            <input
              type="text"
              name="nickname"
              value={team?.nickname || ""}
              onChange={onChange}
              placeholder="Paltan"
              className="w-full border border-slate-300 rounded-xl px-4 py-3"
            />
          </div>

          {/* Country */}
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-2">
              Country
            </label>

            <input
              type="text"
              name="country"
              value={team?.country || ""}
              onChange={onChange}
              placeholder="India"
              className="w-full border border-slate-300 rounded-xl px-4 py-3"
            />
          </div>

          {/* Logo URL */}
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-2">
              Logo URL
            </label>

            <input
              type="text"
              name="logoUrl"
              value={team?.logoUrl || ""}
              onChange={onChange}
              placeholder="https://example.com/logo.png"
              className="w-full border border-slate-300 rounded-xl px-4 py-3"
            />
          </div>

          {/* Logo Preview */}
          {team?.logoUrl && (
            <div className="flex justify-center">
              <img
                src={team?.logoUrl}
                alt="Team Logo"
                className="h-24 w-24 object-contain border rounded-xl p-2"
              />
            </div>
          )}

          {/* Primary Color */}
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-2">
              Primary Color
            </label>

            <div className="flex gap-3 items-center">
              <input
                type="text"
                name="primaryColor"
                value={team?.primaryColor || ""}
                onChange={onChange}
                placeholder="#004BA0"
                className="flex-1 border border-slate-300 rounded-xl px-4 py-3"
              />

              {team?.primaryColor && (
                <div
                  className="w-12 h-12 rounded-xl border"
                  style={{
                    backgroundColor: team?.primaryColor,
                  }}
                />
              )}
            </div>
          </div>

          {/* Coach */}
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-2">
              Coach Name
            </label>

            <input
              type="text"
              name="coachName"
              value={team?.coachName || ""}
              onChange={onChange}
              placeholder="Mahela Jayawardene"
              className="w-full border border-slate-300 rounded-xl px-4 py-3"
            />
          </div>

          {/* Ranking */}
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-2">
              Ranking
            </label>

            <input
              type="number"
              name="ranking"
              value={team?.ranking || ""}
              onChange={onChange}
              placeholder="1"
              className="w-full border border-slate-300 rounded-xl px-4 py-3"
              min="1"
            />
          </div>

          {/* Actions */}
          <div className="flex gap-4 pt-4">
            <button
              type="submit"
              disabled={loading}
              className="flex-1 py-3 rounded-xl bg-blue-600 text-white font-medium hover:bg-blue-700 transition disabled:bg-blue-300"
            >
              {loading ? "Saving..." : submitLabel}
            </button>

            <button
              type="button"
              onClick={() => navigate("/teams")}
              className="flex-1 py-3 rounded-xl bg-slate-500 text-white font-medium hover:bg-slate-600 transition"
            >
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
