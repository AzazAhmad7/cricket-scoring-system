import { useNavigate, useLocation } from "react-router-dom";

export default function Sidebar({ matchState }) {
  const navigate = useNavigate();
  const location = useLocation();

  // Define menus INSIDE the component so matchState is available
  const menus = [
    {
      label: "Scoring",
      icon: "✏️",
      path: `/score/${matchState?.matchId}`,
    },
    {
      label: "Scoreboard",
      icon: "📋",
      path: `/scorecard/${matchState?.matchId}`,
    },
    { label: "Commentary", icon: "🎙️", path: "/commentary" },
    { label: "Players", icon: "👤", path: `/players/${matchState?.matchId}` },
    {
      label: "Partnerships",
      icon: "🤝",
      path: `/partnership/${matchState?.matchId}`,
    },
    { label: "Wagon Wheel", icon: "🎡", path: "/wagon-wheel" },
    { label: "Manhattan", icon: "📊", path: "/manhattan" },
    { label: "Reports", icon: "📄", path: "/reports" },
    { label: "Settings", icon: "⚙️", path: "/settings" },
  ];

  const currentInning = matchState?.scoreCard?.innings?.find(
    (i) => i.inningNumber === matchState?.currentInningNumber,
  );

  const overs = `${currentInning?.scoreSummary?.overs ?? 0}.${currentInning?.scoreSummary?.balls ?? 0}`;
  const crr = currentInning?.scoreSummary?.runRate ?? "0.00";
  const rrr = matchState?.requiredRunRate ?? "0.00";

  return (
    <div className="w-48 h-screen bg-[#0f172a] text-white flex flex-col shrink-0 border-r border-slate-800">
      {/* Logo */}
      <div className="px-4 py-3 flex items-center gap-1 border-b border-slate-800">
        <span className="text-white font-extrabold text-base tracking-tight">
          CRIC
        </span>
        <span className="bg-red-500 text-white text-xs font-bold px-1.5 py-0.5 rounded">
          SCORER
        </span>
      </div>

      {/* Menu */}
      <nav className="flex-1 py-2 space-y-0.5 px-2">
        {menus.map((item) => {
          const isActive = location.pathname === item.path;

          return (
            <button
              key={item.label}
              onClick={() => navigate(item.path)}
              className={`w-full text-left px-3 py-2 rounded-md text-xs flex items-center gap-2 transition-colors ${
                isActive
                  ? "bg-blue-600 text-white font-medium"
                  : "text-slate-300 hover:bg-slate-800"
              }`}
            >
              <span className="text-sm">{item.icon}</span>
              {item.label}
            </button>
          );
        })}
      </nav>

      {/* Footer Stats Card */}
      <div className="mx-2 mb-3 bg-slate-800 rounded-lg p-3 text-xs border border-slate-700">
        <p className="text-slate-400 text-[10px] uppercase tracking-wider mb-1">
          Over in Progress
        </p>

        <div className="flex items-center gap-2 mb-1">
          <span className="text-xl font-bold">{overs}</span>
          <span className="bg-blue-600 text-white text-[10px] px-1.5 rounded font-bold">
            P1
          </span>
        </div>

        <div className="text-[10px] text-slate-300">
          Current Run Rate{" "}
          <span className="text-white font-semibold">{crr}</span>
        </div>

        <div className="text-[10px] text-slate-300 mt-0.5">
          Required Run Rate{" "}
          <span className="text-white font-semibold">{rrr}</span>
        </div>
      </div>
    </div>
  );
}
