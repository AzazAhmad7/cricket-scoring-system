const menu = [
  "Overview",
  "Match Details",
  "Teams & Squads",
  "Toss",
  "Review",
  "Settings",
];

export default function Sidebar() {
  return (
    <aside className="w-72 bg-white border-r border-slate-200 min-h-[calc(100vh-64px)] p-6">
      <div className="mb-6">
        <h2 className="text-lg font-semibold text-slate-900">Match Wizard</h2>
        <p className="text-sm text-slate-500 mt-1">
          Follow the steps to create a match.
        </p>
      </div>

      <nav className="space-y-2">
        {menu.map((item, index) => (
          <button
            key={item}
            className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl text-left transition ${
              index === 1
                ? "bg-indigo-50 text-indigo-700 border border-indigo-100"
                : "text-slate-600 hover:bg-slate-50"
            }`}
          >
            <span className="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-sm font-medium">
              {index + 1}
            </span>
            <span className="font-medium">{item}</span>
          </button>
        ))}
      </nav>
    </aside>
  );
}
