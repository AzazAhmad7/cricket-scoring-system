export default function TeamSquadCard({ teamName, defaultName, players }) {
  return (
    <div className="border border-slate-200 rounded-xl p-5 bg-slate-50">
      <div className="mb-4">
        <label className="block text-sm font-medium text-slate-700 mb-2">
          {teamName} Name
        </label>
        <input
          defaultValue={defaultName}
          className="w-full rounded-lg border border-slate-300 px-4 py-2.5 focus:outline-none focus:ring-2 focus:ring-indigo-500"
        />
      </div>

      <div>
        <h3 className="font-medium text-slate-900 mb-3">Playing XI</h3>
        <div className="space-y-2 max-h-80 overflow-y-auto pr-2">
          {players.map((player, index) => (
            <label
              key={player}
              className="flex items-center gap-3 p-2 rounded-lg hover:bg-white"
            >
              <input
                type="checkbox"
                defaultChecked={index < 11}
                className="w-4 h-4 text-indigo-600 rounded"
              />
              <span className="text-sm text-slate-700">{player}</span>
            </label>
          ))}
        </div>
      </div>

      <button className="mt-4 w-full py-2.5 rounded-lg border border-dashed border-slate-300 text-slate-600 hover:bg-white">
        + Add Player
      </button>
    </div>
  );
}