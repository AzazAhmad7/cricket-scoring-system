// Replace your existing return statement and styling with the component below.
// Keep your existing data extraction logic:
// const inning = ...
// const batting = ...
// const bowling = ...

import { ImpactIn, ImpactOut } from "./impactIndicator";

export default function ScoreCard({ matchData, matchState }) {
  const home = matchData?.teams?.homeTeam;
  const away = matchData?.teams?.awayTeam;

  const inning = matchState?.scoreCard?.innings?.find(
    (i) => i.inningNumber === matchState?.currentInningNumber,
  );

  const battingTeam = matchState.battingTeamId == home.id ? home : away;

  const batting = inning?.battingCard?.batters || [];
  const bowling = inning?.bowlingCard?.bowlers || [];
  const fallOfWickets = inning?.fallOfWickets || [];
  const partnershipCard = inning?.partnershipCard?.partnerships || [];
  const currentPShip = partnershipCard.find((pship) => pship.isActive);

  if (!inning) {
    return (
      <div className="bg-slate-800 rounded-3xl border border-slate-700 shadow-xl p-12 text-center text-white">
        <div className="text-5xl mb-4">📊</div>
        <h3 className="text-2xl font-bold">No Scorecard Available</h3>
        <p className="text-slate-400 mt-2">
          Start the match to view live batting and bowling statistics.
        </p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-900 p-6 text-white">
      <div className="max-w-7xl mx-auto space-y-6">
        {/* ================= HEADER ================= */}
        <div className="bg-slate-800 rounded-2xl border border-slate-700 shadow-xl p-6">
          <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-6">
            <div>
              <h1 className="text-3xl uppercase font-bold">
                {battingTeam.name || "Current Innings"}
              </h1>
              <p className="text-slate-400 mt-1">
                {inning?.scoreSummary?.runs || 0}/
                {inning?.scoreSummary?.wickets || 0} (
                {`${inning?.scoreSummary?.overs}.${inning?.scoreSummary?.balls}` ||
                  "0.0"}{" "}
                Overs)
              </p>
            </div>

            <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
              <StatCard
                label="Run Rate"
                value={inning?.scoreSummary?.runRate || "0.00"}
              />
              <StatCard
                label="Req. Run Rate"
                value={matchState?.targetDTO?.requiredRunRate || "0.00"}
              />
              <StatCard
                label="Target"
                value={matchState?.targetDTO?.targetRuns || "-"}
              />
              <StatCard
                label="Partnership"
                value={
                  `${currentPShip?.partnershipRuns}* (${currentPShip?.partnershipBalls})` ||
                  "-"
                }
              />
            </div>
          </div>
        </div>

        {/* ================= BATTING + BOWLING ================= */}
        <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
          {/* ================= BATTING ================= */}
          <div className="bg-slate-800 rounded-2xl border border-slate-700 shadow-xl overflow-hidden">
            <SectionHeader
              title="🏏 Batting Scorecard"
              color="from-emerald-600 to-green-500"
            />

            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="bg-slate-700/60">
                  <tr className="text-slate-300">
                    <th className="text-left px-4 py-3">Batter</th>
                    <th className="text-left px-4 py-3">Dismissal</th>
                    <th className="text-center px-4 py-3">R</th>
                    <th className="text-center px-4 py-3">B</th>
                    <th className="text-center px-4 py-3">4s</th>
                    <th className="text-center px-4 py-3">6s</th>
                    <th className="text-center px-4 py-3">SR</th>
                  </tr>
                </thead>

                <tbody>
                  {batting.map((batter, index) => {
                    const playerId = batter?.batter?.playerId;
                    const balls = batter?.scoring?.balls || 0;

                    const isStriker = playerId === inning?.strikerId;
                    const isImpactIn = batter.isImpactIn;
                    const isImpactOut = batter.isImpactOut;
                    const isNonStriker = playerId === inning?.nonStrikerId;
                    const isCurrent = isStriker || isNonStriker;
                    const hasBatted = balls > 0 || isCurrent;

                    let dismissal = "Yet to bat";
                    if (hasBatted) {
                      dismissal = batter?.dismissal?.dismissalText || "Not out";
                    }

                    return (
                      <tr
                        key={playerId || index}
                        className={`border-t border-slate-700 ${
                          isCurrent
                            ? "bg-emerald-500/10"
                            : "hover:bg-slate-700/30"
                        }`}
                      >
                        <td className="px-4 py-3 font-semibold">
                          <div className="flex items-center gap-2">
                            <span
                              className={
                                isCurrent ? "text-emerald-400" : "text-white"
                              }
                            >
                              {batter?.batter?.playerName}

                              {/* {isStriker && " *"} */}
                            </span>
                            {isImpactIn && <ImpactIn />}
                            {isImpactOut && <ImpactOut />}
                            {isStriker && (
                              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
                            )}
                          </div>
                        </td>

                        <td className="px-4 py-3 text-slate-400">
                          {dismissal}
                        </td>

                        <td className="px-4 py-3 text-center font-bold text-emerald-400">
                          {hasBatted ? batter?.scoring?.runs || 0 : "-"}
                        </td>

                        <td className="px-4 py-3 text-center">
                          {hasBatted ? batter?.scoring?.balls || 0 : "-"}
                        </td>

                        <td className="px-4 py-3 text-center">
                          {hasBatted ? batter?.scoring?.fours || 0 : "-"}
                        </td>

                        <td className="px-4 py-3 text-center">
                          {hasBatted ? batter?.scoring?.sixes || 0 : "-"}
                        </td>

                        <td className="px-4 py-3 text-center text-cyan-400 font-semibold">
                          {hasBatted
                            ? batter?.scoring?.strikeRate || "0.00"
                            : "-"}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>

          {/* ================= BOWLING ================= */}
          <div className="bg-slate-800 rounded-2xl border border-slate-700 shadow-xl overflow-hidden">
            <SectionHeader
              title="🎯 Bowling Scorecard"
              color="from-violet-600 to-purple-500"
            />

            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="bg-slate-700/60">
                  <tr className="text-slate-300">
                    <th className="text-left px-4 py-3">Bowler</th>
                    <th className="text-center px-4 py-3">O</th>
                    <th className="text-center px-4 py-3">M</th>
                    <th className="text-center px-4 py-3">R</th>
                    <th className="text-center px-4 py-3">W</th>
                    <th className="text-center px-4 py-3">ECO</th>
                  </tr>
                </thead>

                <tbody>
                  {bowling.map((bowler, index) => {
                    const isCurrent =
                      bowler?.bowler?.playerId === inning?.currentBowlerId;

                    return (
                      <tr
                        key={index}
                        className={`border-t border-slate-700 ${
                          isCurrent
                            ? "bg-violet-500/10"
                            : "hover:bg-slate-700/30"
                        }`}
                      >
                        <td className="px-4 py-3 font-semibold">
                          <div className="flex items-center gap-2">
                            <span
                              className={
                                isCurrent ? "text-violet-400" : "text-white"
                              }
                            >
                              {bowler?.bowler?.playerName}
                              {/* {isCurrent && " *"} */}
                            </span>
                            {(bowler.bowler.playerId ===
                              matchData?.squads?.homeTeamImpactPlayerDTO
                                ?.impactInPlayerId ||
                              bowler.bowler.playerId ===
                                matchData?.squads?.awayTeamImpactPlayerDTO
                                  ?.impactInPlayerId) && <ImpactIn />}
                            {(bowler.bowler.playerId ===
                              matchData?.squads?.homeTeamImpactPlayerDTO
                                ?.impactOutPlayerId ||
                              bowler.bowler.playerId ===
                                matchData?.squads?.awayTeamImpactPlayerDTO
                                  ?.impactOutPlayerId) && <ImpactOut />}
                            {isCurrent && (
                              <span className="w-2 h-2 rounded-full bg-violet-400 animate-pulse" />
                            )}
                          </div>
                        </td>

                        <td className="px-4 py-3 text-center">
                          {bowler?.overs || "0.0"}
                        </td>
                        <td className="px-4 py-3 text-center">
                          {bowler?.maidens || 0}
                        </td>
                        <td className="px-4 py-3 text-center font-bold">
                          {bowler?.runsConceded || 0}
                        </td>
                        <td className="px-4 py-3 text-center font-bold text-red-400">
                          {bowler?.wickets || 0}
                        </td>
                        <td className="px-4 py-3 text-center text-cyan-400 font-semibold">
                          {bowler?.economy || "0.00"}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* ================= FALL OF WICKETS ================= */}
        <div className="bg-slate-800 rounded-2xl border border-slate-700 shadow-xl p-6">
          <h3 className="text-xl font-bold mb-4 text-white">
            🏏 Fall of Wickets
          </h3>

          {fallOfWickets.length > 0 ? (
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-2">
              {fallOfWickets.map((fow, index) => (
                <div
                  key={index}
                  className="bg-slate-700/40 rounded-lg p-3 border border-slate-600 text-center hover:bg-slate-700/60 transition"
                >
                  {/* Score at Fall */}
                  <div className="text-lg font-bold text-emerald-400">
                    {index + 1}-{fow.scoreAtFall}
                  </div>

                  {/* Batter Name */}
                  <div className="mt-2 text-xs font-semibold text-white">
                    {fow.batterName}
                    <span className="p-2">
                      {(fow.batterId ===
                        matchData?.squads?.homeTeamImpactPlayerDTO
                          ?.impactInPlayerId ||
                        fow.batterId ===
                          matchData?.squads?.awayTeamImpactPlayerDTO
                            ?.impactInPlayerId) && <ImpactIn />}
                      {(fow.batterId ===
                        matchData?.squads?.homeTeamImpactPlayerDTO
                          ?.impactOutPlayerId ||
                        fow.batterId ===
                          matchData?.squads?.awayTeamImpactPlayerDTO
                            ?.impactOutPlayerId) && <ImpactOut />}
                    </span>
                  </div>
                  {/* Over */}
                  <div className="mt-1 text-xs text-slate-400">
                    {fow.over} overs
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-slate-400">No wickets fallen yet.</p>
          )}
        </div>
      </div>
    </div>
  );
}

/* ================= REUSABLE COMPONENTS ================= */

function SectionHeader({ title, color }) {
  return (
    <div className={`px-6 py-4 bg-gradient-to-r ${color}`}>
      <h2 className="text-lg font-bold text-white uppercase">{title}</h2>
    </div>
  );
}

function StatCard({ label, value }) {
  return (
    <div className="bg-slate-700/40 rounded-xl px-4 py-3 border border-slate-600 text-center">
      <div className="text-xs uppercase tracking-wide text-slate-400">
        {label}
      </div>
      <div className="text-lg font-bold text-white mt-1">{value}</div>
    </div>
  );
}
