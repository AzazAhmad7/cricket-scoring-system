export default function Header() {
  return (
    <header className="bg-slate-900 text-white h-16 px-6 flex items-center justify-between shadow-lg">
      <div className="flex items-center gap-3">
        <div className="w-10 h-10 rounded-lg bg-indigo-600 flex items-center justify-center font-bold text-lg">
          C
        </div>
        <div>
          <h1 className="font-semibold">Cricket Scoring System</h1>
          <p className="text-xs text-slate-400">Professional Match Setup</p>
        </div>
      </div>

      <div className="flex items-center gap-3">
        <button className="px-4 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-sm">
          Dashboard
        </button>
        <button className="px-4 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-sm font-medium">
          Create Match
        </button>
      </div>
    </header>
  );
}
