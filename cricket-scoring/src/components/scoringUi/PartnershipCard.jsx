export default function PartnershipCard({ matchState }) {
  const inning = matchState?.scoreCard?.innings?.find(
    (i) => i.inningNumber === matchState.currentInningNumber,
  );
  const p = inning?.partnershipCard?.partnerships?.find((p) => p.isActive);

  if (!p) return null;

  return (
    <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-3">
      <h3 className="font-semibold text-gray-800 text-xs uppercase tracking-wide mb-3">
        Partnership
      </h3>

      {/* Total partnership */}
      <div className="text-center mb-2">
        <span className="text-lg font-bold text-gray-900">
          {`${p.partnershipRuns}* `}
        </span>
        <span className="text-xs text-gray-400 ml-1">
          ({p.partnershipBalls})
        </span>
      </div>

      {/* Individual contributions */}
      <div className="space-y-1.5">
        {p.contributions?.map((c) => (
          <div
            key={c.playerId}
            className="flex justify-between items-center text-xs"
          >
            <span className="text-gray-700 font-medium">{c.name}</span>
            <span className="text-gray-500">
              {c.runs} <span className="text-gray-400">({c.balls})</span>
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
