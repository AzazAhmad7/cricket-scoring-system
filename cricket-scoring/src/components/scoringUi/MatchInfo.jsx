export default function MatchInfo({ matchData }) {
  const rows = [
    {
      label: "Venue",
      value: `${matchData?.venue?.name}, ${matchData?.venue?.city}`,
    },
    { label: "Date", value: matchData?.matchInfo?.matchDate },
    { label: "Start Time", value: matchData?.matchInfo?.startTime },
    /*{ label: "Umpires", value: info?.umpires },
    { label: "Third Umpire", value: info?.thirdUmpire },
    { label: "Match Referee", value: info?.matchReferee },*/
  ];

  return (
    <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-4">
      <h3 className="font-semibold text-gray-800 text-xs uppercase tracking-wide mb-3">
        Match Info
      </h3>
      <div className="space-y-2">
        {rows
          .filter((r) => r.value)
          .map((r) => (
            <div
              key={r.label}
              className="flex justify-between items-start text-xs gap-2"
            >
              <span className="text-gray-500 shrink-0">{r.label}</span>
              <span className="text-gray-800 font-medium text-right">
                {r.value}
              </span>
            </div>
          ))}
      </div>
    </div>
  );
}
