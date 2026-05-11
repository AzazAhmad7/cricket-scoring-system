export default function BallInfo({ matchData, matchState }) {
  const inning = matchState?.scoreCard?.innings?.find(
    (i) => i.inningNumber === matchState.currentInningNumber,
  );

  const rows = [
    { label: "Over", value: inning?.scoreSummary?.overs ?? "-" },
    { label: "Ball", value: inning?.scoreSummary?.balls ?? "-" },
    {
      label: "Innings",
      value:
        matchState?.currentInningNumber === 1 ? "1st Innings" : "2nd Innings",
    },
    {
      label: "Powerplay",
      value: `P (${matchData?.rules?.powerPlayStartOver}-${matchData?.rules?.powerPlayEndOver})`,
    },
    { label: "Field", value: matchData?.matchInfo?.fieldType || "Day" },
    { label: "Pitch", value: matchData?.matchInfo?.pitch || "Good" },
  ];

  return (
    <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-4 h-full">
      <h3 className="font-semibold text-gray-800 text-sm mb-3 uppercase tracking-wide">
        Ball Info
      </h3>
      <div className="space-y-2">
        {rows.map((r) => (
          <div
            key={r.label}
            className="flex justify-between items-center text-xs"
          >
            <span className="text-gray-500">{r.label}</span>
            <span className="font-medium text-gray-800">{r.value}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
