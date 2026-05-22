import { useState } from "react";

export default function TossPanel() {
  const team1 = {
    id: 1,
    name: "India",
    shortName: "IND",
  };

  const team2 = {
    id: 2,
    name: "Australia",
    shortName: "AUS",
  };

  const [toss, setToss] = useState({
    method: "Manual",
    winnerId: null,
    decision: "Bat",
  });

  const tossWinner =
    toss.winnerId === team1?.id
      ? team1
      : toss.winnerId === team2?.id
        ? team2
        : null;

  const summary = tossWinner
    ? `${tossWinner.name} won the toss and elected to ${toss.decision.toLowerCase()}.`
    : "Toss not decided yet.";

  return (
    <div className="max-w-md mx-auto">
      <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-8 space-y-7">
        <h3 className="text-xl font-bold text-slate-800">Toss</h3>

        <div>
          <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-3">
            Toss Method
          </p>
          <div className="grid grid-cols-2 gap-3">
            {["Manual", "Random"].map((method) => (
              <button
                key={method}
                onClick={() => setToss((prev) => ({ ...prev, method }))}
                className={`p-3 rounded-xl border text-sm font-medium transition ${
                  toss.method === method
                    ? "bg-blue-600 text-white border-blue-600"
                    : "border-slate-200 text-slate-600 hover:border-blue-300"
                }`}
              >
                {method}
              </button>
            ))}
          </div>
        </div>

        <div>
          <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-3">
            Toss Winner
          </p>
          <div className="space-y-3">
            {[team1, team2].map((team, index) =>
              team ? (
                <button
                  key={team.id}
                  onClick={() =>
                    setToss((prev) => ({
                      ...prev,
                      winnerId: team.id,
                    }))
                  }
                  className={`w-full p-3.5 rounded-xl border text-sm font-medium text-left transition ${
                    toss.winnerId === team.id
                      ? "bg-blue-50 border-blue-500 text-blue-700"
                      : "border-slate-200 text-slate-600 hover:border-blue-300"
                  }`}
                >
                  {team.shortName && (
                    <span className="mr-2 font-semibold">{team.shortName}</span>
                  )}
                  {team.name}
                  <span className="text-xs text-slate-400 ml-2">
                    {index === 0 ? "(Home)" : "(Away)"}
                  </span>
                </button>
              ) : (
                <div
                  key={index}
                  className="w-full p-3.5 rounded-xl border border-dashed border-slate-200 text-sm text-slate-300 text-center"
                >
                  Select team first
                </div>
              ),
            )}
          </div>
        </div>

        <div>
          <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-3">
            Decision
          </p>
          <div className="grid grid-cols-3 gap-3">
            {["Bat", "Bowl", "Field"].map((decision) => (
              <button
                key={decision}
                onClick={() =>
                  setToss((prev) => ({
                    ...prev,
                    decision,
                  }))
                }
                className={`p-3 rounded-xl border text-sm font-medium transition ${
                  toss.decision === decision
                    ? "bg-blue-600 text-white border-blue-600"
                    : "border-slate-200 text-slate-600 hover:border-blue-300"
                }`}
              >
                {decision}
              </button>
            ))}
          </div>
        </div>

        <div
          className={`rounded-xl p-4 text-center border ${
            tossWinner
              ? "bg-green-50 border-green-200"
              : "bg-slate-50 border-slate-200"
          }`}
        >
          <p
            className={`text-sm font-medium ${
              tossWinner ? "text-green-700" : "text-slate-400"
            }`}
          >
            {summary}
          </p>
        </div>
      </div>
    </div>
  );
}
