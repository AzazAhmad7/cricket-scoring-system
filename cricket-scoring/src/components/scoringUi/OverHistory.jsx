import { useState } from "react";

function getBallStyle(b) {
  if (b === "W") return "bg-red-500 text-white font-bold";
  if (b === "4") return "text-blue-600 font-bold";
  if (b === "6") return "text-purple-600 font-bold";
  if (b === "NB" || b === "WD") return "text-orange-500 font-bold";
  return "text-gray-600";
}

export default function OverHistory({ matchState }) {
  const [tab, setTab] = useState("OVERS");
  const inning = matchState?.scoreCard?.innings?.find(
    (i) => i.inningNumber === matchState.currentInningNumber,
  );
  const overs = inning?.overs || [];

  return (
    <div className="bg-white rounded-xl border border-gray-200 shadow-sm flex flex-col">
      {/* Tabs */}
      <div className="flex border-b border-gray-200">
        {["OVERS", "RUNS", "WICKETS"].map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`flex-1 py-2 text-xs font-semibold tracking-wide transition-colors ${
              tab === t
                ? "text-blue-600 border-b-2 border-blue-600"
                : "text-gray-400 hover:text-gray-600"
            }`}
          >
            {t}
          </button>
        ))}
      </div>

      {/* Over rows */}
      <div className="p-3 space-y-1 max-h-64 overflow-y-auto">
        {/* Team label */}
        <p className="text-xs font-semibold text-gray-700 mb-2">
          {matchState?.currentInningNumber === 1 ? "Australia" : "India"}
        </p>

        {overs.length === 0 && (
          <p className="text-xs text-gray-400 text-center py-4">No overs yet</p>
        )}

        {[...overs].reverse().map((o) => (
          <div key={o.overNumber} className="flex items-center gap-1.5 text-xs py-0.5">
            <span className="w-8 text-gray-500 shrink-0">{o.overNumber}</span>
            {o.balls?.map((b, i) => (
              <span
                key={i}
                className={`w-5 h-5 flex items-center justify-center text-[11px] rounded-full ${getBallStyle(b)}`}
              >
                {b}
              </span>
            ))}
          </div>
        ))}

        {/* "All Overs" button */}
        <button className="w-full mt-2 flex justify-between items-center text-xs text-blue-600 bg-blue-50 rounded-md px-3 py-1.5 hover:bg-blue-100 transition-colors">
          <span className="font-medium">All Overs</span>
          <span>›</span>
        </button>
      </div>
    </div>
  );
}
