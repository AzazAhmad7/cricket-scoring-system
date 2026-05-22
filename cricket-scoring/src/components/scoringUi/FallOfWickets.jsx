export default function FallOfWickets({ matchState }) {
  const inning = matchState?.scoreCard?.innings?.find(
    (i) => i.inningNumber === matchState.currentInningNumber,
  );

  const wickets = inning?.fallOfWickets || [];

  return (
    <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
      <h3 className="font-semibold text-gray-700 text-xs uppercase tracking-wide px-4 py-2.5 border-b border-gray-100 bg-gray-50">
        Fall of Wickets
      </h3>

      {wickets.length === 0 ? (
        <p className="text-xs text-gray-400 text-center py-4">No wickets yet</p>
      ) : (
        <table className="w-full text-xs">
          <thead>
            <tr className="text-gray-400 border-b border-gray-100">
              <th className="text-left font-medium py-1.5 px-4">#</th>
              <th className="text-left font-medium py-1.5 px-2">
                Score (Over)
              </th>
              <th className="text-right font-medium py-1.5 px-4">Batter</th>
            </tr>
          </thead>
          <tbody>
            {wickets.map((w, idx) => (
              <tr
                key={w.wicketNumber}
                className={`border-b border-gray-50 ${idx % 2 === 0 ? "bg-white" : "bg-gray-50/50"}`}
              >
                <td className="py-2 px-4 font-bold text-gray-500">
                  {w.wicketNumber}
                </td>
                <td className="py-2 px-2">
                  <span className="font-semibold text-gray-800">
                    {w.wicketNumber}-{w.scoreAtFall}
                  </span>
                  <span className="text-gray-400 ml-1">({w.over})</span>
                </td>
                <td className="py-2 px-4 text-right text-gray-600 font-medium">
                  {w.batterName}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
