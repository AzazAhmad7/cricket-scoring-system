function BatsmenTable({ players = [] }) {
  const playing = players.filter((p) => p.dismissal?.status === "NOT_OUT");

  return (
    <div className="flex-1">
      <table className="w-full text-xs">
        <thead>
          <tr className="text-gray-500 border-b border-gray-100">
            <th className="text-left py-1 font-medium">BATSMEN</th>
            <th className="py-1 font-medium text-center w-8">R</th>
            <th className="py-1 font-medium text-center w-8">B</th>
            <th className="py-1 font-medium text-center w-8">4s</th>
            <th className="py-1 font-medium text-center w-8">6s</th>
            <th className="py-1 font-medium text-center w-14">SR</th>
          </tr>
        </thead>
        <tbody>
          {playing.map((p) => (
            <tr
              key={p.batter?.playerId ?? p.batter?.playerName}
              className="border-b border-gray-50"
            >
              <td className="py-1.5 text-gray-800 font-medium flex items-center gap-1.5">
                {p.batter?.playerName}
                {p.onStrike && (
                  //<span className="text-gray-400 font-normal">*</span>
                  <span className="w-1.5 h-1.5 bg-green-500 rounded-full inline-block"></span>
                )}
              </td>
              <td className="py-1.5 text-center font-semibold">
                {p.scoring?.runs ?? 0}
              </td>
              <td className="py-1.5 text-center text-gray-500">
                {p.scoring?.balls ?? 0}
              </td>
              <td className="py-1.5 text-center text-gray-500">
                {p.scoring?.fours ?? 0}
              </td>
              <td className="py-1.5 text-center text-gray-500">
                {p.scoring?.sixes ?? 0}
              </td>
              <td className="py-1.5 text-center text-gray-500">
                {p.scoring?.strikeRate ?? 0}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function BowlerTable({ bowler }) {
  return (
    <div className="flex-1 border-l border-gray-100 pl-4">
      <table className="w-full text-xs">
        <thead>
          <tr className="text-gray-500 border-b border-gray-100">
            <th className="text-left py-1 font-medium">BOWLER</th>
            <th className="py-1 font-medium text-center w-10">O</th>
            <th className="py-1 font-medium text-center w-8">M</th>
            <th className="py-1 font-medium text-center w-8">R</th>
            <th className="py-1 font-medium text-center w-8">W</th>
            <th className="py-1 font-medium text-center w-14">ECON</th>
          </tr>
        </thead>
        <tbody>
          {bowler ? (
            <tr className="border-b border-gray-50">
              <td className="py-1.5 text-gray-800 font-medium">
                {bowler.bowler?.playerName}
              </td>
              <td className="py-1.5 text-center text-gray-500">
                {bowler.overs ?? 0}
              </td>
              <td className="py-1.5 text-center text-gray-500">
                {bowler.maidens ?? 0}
              </td>
              <td className="py-1.5 text-center font-semibold">
                {bowler.runsConceded ?? 0}
              </td>
              <td className="py-1.5 text-center font-semibold">
                {bowler.wickets ?? 0}
              </td>
              <td className="py-1.5 text-center text-gray-500">
                {bowler.economy ?? 0}
              </td>
            </tr>
          ) : (
            <tr>
              <td colSpan={6} className="text-center text-gray-400 py-2">
                No bowler selected
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

export default function ScoreSummary({ matchState, teams }) {
  const currentInning = matchState?.scoreCard?.innings?.find(
    (i) => i.inningNumber === matchState?.currentInningNumber,
  );
  const bowler = currentInning?.bowlingCard?.bowlers?.find(
    (b) => b.isCurrentBowler,
  );
  const spells = bowler?.eachOver || [];
  const lastOver = spells.length ? spells[spells.length - 1] : [];
  const spell = lastOver?.deliveries || [];

  if (!matchState || !currentInning) return null;

  const battingTeam =
    matchState.currentInningNumber === 1
      ? teams?.homeTeam?.shortName || "IND"
      : teams?.awayTeam?.shortName || "AUS";

  return (
    <div className="bg-white rounded-xl border border-gray-200 shadow-sm">
      {/* Score Row */}
      <div className="flex items-center justify-between px-4 py-3 border-b border-gray-100">
        <div className="flex items-center gap-3">
          <span className="text-2xl">🇮🇳</span>
          <span className="text-gray-500 font-semibold text-sm">
            {battingTeam}
          </span>
          <span className="text-3xl font-extrabold tracking-tight text-gray-900">
            {currentInning.scoreSummary?.runs ?? 0}/
            {currentInning.scoreSummary?.wickets ?? 0}
          </span>
          <span className="text-xs text-gray-400 mt-1">
            {currentInning.scoreSummary?.overs ?? 0}.
            {currentInning.scoreSummary?.balls ?? 0} OVERS
          </span>
        </div>

        <div className="flex items-center gap-4 text-xs text-gray-500">
          <div className="bg-gray-100 rounded px-2 py-1 font-semibold text-gray-700">
            P1
          </div>
          <div>
            CRR{" "}
            <span className="font-bold text-gray-800">
              {currentInning.scoreSummary?.runRate ?? 0}
            </span>
          </div>
          <span className="text-gray-400 text-lg font-light">vs</span>
          <span className="text-xl">🇦🇺</span>
        </div>
      </div>

      {/* Toss Info Row */}
      {matchState?.toss && (
        <div className="px-4 py-1.5 border-b border-gray-100 bg-gray-50 text-xs text-gray-500 flex gap-4">
          <span>
            TOSS:{" "}
            <span className="font-semibold text-gray-700">
              {matchState.toss.winner}
            </span>
          </span>
          <span>
            ELECTED TO{" "}
            <span className="font-semibold text-gray-700">
              {matchState.toss.elected}
            </span>
          </span>
        </div>
      )}

      {/* Batsmen + Bowler Tables */}
      <div className="flex px-4 py-2 gap-2">
        <BatsmenTable players={currentInning?.battingCard?.batters || []} />
        <BowlerTable bowler={bowler} />
      </div>

      {/* This Spell */}
      <div className="flex items-center gap-2 px-4 py-2 border-t border-gray-100">
        <span className="text-[10px] text-gray-400 uppercase tracking-wider w-16">
          This Spell
        </span>
        {(spell.length
          ? spell
          : ["-", "•", "-", "•", "-", "•", "-", "•", "-", "•", "-", "•"]
        ).map((b, i) => {
          if (b.display === "•")
            return (
              <span key={i} className="text-gray-300 text-xs">
                •
              </span>
            );
          const isW = b.display === "W";
          const is4 = b.display === "4";
          const is6 = b.display === "6";
          return (
            <div
              key={i}
              className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-semibold
                ${isW ? "bg-red-500 text-white" : is4 || is6 ? "bg-blue-500 text-white" : "bg-gray-100 text-gray-700"}`}
            >
              {b.display}
            </div>
          );
        })}
      </div>
    </div>
  );
}
