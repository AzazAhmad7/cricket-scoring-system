import React from "react";
import { ImpactIn, ImpactOut } from "./impactIndicator";

export default function Partnership({ matchData, matchState }) {
  const home = matchData?.teams?.homeTeam;
  const away = matchData?.teams?.awayTeam;

  const inning = matchState?.scoreCard?.innings?.find(
    (i) => i.inningNumber === matchState?.currentInningNumber,
  );

  const battingTeam = matchState.battingTeamId == home.id ? home : away;

  const partnershipCard = inning?.partnershipCard?.partnerships || [];
  const currentPShip = partnershipCard.find((pship) => pship.isActive);

  const totalPartnerships = partnershipCard.length;

  return (
    <div className="bg-slate-950 rounded-3xl border border-slate-800 shadow-2xl overflow-hidden">
      {/* ================= HEADER ================= */}
      <div className="px-8 py-6 border-b border-slate-800 bg-gradient-to-r from-slate-950 via-slate-900 to-slate-950">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-3xl uppercase font-bold text-white flex items-center gap-3">
              🤝 Partnerships
            </h2>
            <p className="text-slate-400 mt-1">
              All partnerships in this innings
            </p>
          </div>

          <div className="px-4 py-2 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 font-semibold">
            {totalPartnerships} Partnerships
          </div>
        </div>
      </div>

      {/* ================= COLUMN HEADERS ================= */}
      <div className="hidden xl:grid grid-cols-12 gap-6 px-8 py-4 border-b border-slate-800 text-xs uppercase tracking-wider font-semibold text-slate-400">
        <div className="col-span-3">Partnership</div>
        <div className="col-span-5">Contribution (Runs)</div>
        <div className="col-span-1 text-center">Runs</div>
        <div className="col-span-1 text-center">Balls</div>
        <div className="col-span-2 text-center">Run Rate</div>
      </div>

      {/* ================= PARTNERSHIP ROWS ================= */}
      <div className="p-6 space-y-4">
        {partnershipCard.map((p, index) => {
          const p1Runs = p.contributions[0].runs || 0;
          const p2Runs = p.contributions[1].runs || 0;
          const partnershipRuns = p.partnershipRuns || p1Runs + p2Runs;

          const runRate =
            p.partnershipBalls > 0
              ? ((partnershipRuns * 6) / p.partnershipBalls).toFixed(2)
              : "0.00";

          const p1Percent =
            partnershipRuns > 0
              ? ((p1Runs / partnershipRuns) * 100).toFixed(1)
              : 0;

          const p2Percent =
            partnershipRuns > 0
              ? ((p2Runs / partnershipRuns) * 100).toFixed(1)
              : 0;

          const active = p.isActive;

          return (
            <div
              key={index}
              className={`rounded-2xl border p-6 transition-all duration-300 ${
                active
                  ? "border-emerald-500/50 bg-emerald-500/5 shadow-lg shadow-emerald-500/10"
                  : "border-slate-800 bg-slate-900/70"
              }`}
            >
              <div className="grid grid-cols-1 xl:grid-cols-12 gap-6 items-center">
                {/* Partnership Info */}
                <div className="xl:col-span-3">
                  <div className="flex items-start gap-4">
                    <div
                      className={`w-12 h-12 rounded-full flex items-center justify-center font-bold text-lg ${
                        active
                          ? "bg-emerald-500/20 text-emerald-400"
                          : "bg-violet-500/20 text-violet-400"
                      }`}
                    >
                      {p.wicket || index + 1}
                    </div>

                    <div>
                      <div className="space-y-1">
                        <div className="font-semibold text-white">
                          {p.contributions[0].name}
                          <span className="p-2">
                            {(p.contributions[0].playerId ===
                              matchData?.squads?.homeTeamImpactPlayerDTO
                                ?.impactInPlayerId ||
                              p.contributions[0].playerId ===
                                matchData?.squads?.awayTeamImpactPlayerDTO
                                  ?.impactInPlayerId) && <ImpactIn />}
                            {(p.contributions[0].playerId ===
                              matchData?.squads?.homeTeamImpactPlayerDTO
                                ?.impactOutPlayerId ||
                              p.contributions[0].playerId ===
                                matchData?.squads?.awayTeamImpactPlayerDTO
                                  ?.impactOutPlayerId) && <ImpactOut />}
                          </span>
                        </div>
                        <div className="font-semibold text-white">
                          {p.contributions[1].name}
                          <span className="p-2">
                            {(p.contributions[1].playerId ===
                              matchData?.squads?.homeTeamImpactPlayerDTO
                                ?.impactInPlayerId ||
                              p.contributions[1].playerId ===
                                matchData?.squads?.awayTeamImpactPlayerDTO
                                  ?.impactInPlayerId) && <ImpactIn />}
                            {(p.contributions[1].playerId ===
                              matchData?.squads?.homeTeamImpactPlayerDTO
                                ?.impactOutPlayerId ||
                              p.contributions[1].playerId ===
                                matchData?.squads?.awayTeamImpactPlayerDTO
                                  ?.impactOutPlayerId) && <ImpactOut />}
                          </span>
                        </div>
                      </div>

                      {active && (
                        <div className="mt-3 inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs font-semibold">
                          <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
                          ACTIVE
                        </div>
                      )}
                    </div>
                  </div>
                </div>

                {/* Contribution Bars */}
                <div className="xl:col-span-5 space-y-4">
                  {/* Batter 1 */}
                  <div>
                    <div className="flex justify-between text-sm mb-2">
                      <span className="text-slate-300">
                        {p.contributions[0].name}
                      </span>
                      <span
                        className={`font-semibold ${
                          active ? "text-emerald-400" : "text-violet-400"
                        }`}
                      >
                        {p1Runs} ({p1Percent}%)
                      </span>
                    </div>

                    <div className="h-3 bg-slate-800 rounded-full overflow-hidden">
                      <div
                        className={`h-full rounded-full ${
                          active
                            ? "bg-gradient-to-r from-emerald-400 to-green-500"
                            : "bg-gradient-to-r from-violet-400 to-purple-500"
                        }`}
                        style={{ width: `${p1Percent}%` }}
                      />
                    </div>
                  </div>

                  {/* Batter 2 */}
                  <div>
                    <div className="flex justify-between text-sm mb-2">
                      <span className="text-slate-300">
                        {p.contributions[1].name}
                      </span>
                      <span
                        className={`font-semibold ${
                          active ? "text-emerald-400" : "text-blue-400"
                        }`}
                      >
                        {p2Runs} ({p2Percent}%)
                      </span>
                    </div>

                    <div className="h-3 bg-slate-800 rounded-full overflow-hidden">
                      <div
                        className={`h-full rounded-full ${
                          active
                            ? "bg-gradient-to-r from-blue-400 to-cyan-500"
                            : "bg-gradient-to-r from-blue-400 to-cyan-500"
                        }`}
                        style={{ width: `${p2Percent}%` }}
                      />
                    </div>
                  </div>
                </div>

                {/* Runs */}
                <div className="xl:col-span-1 text-center">
                  <div
                    className={`text-3xl font-bold ${
                      active ? "text-emerald-400" : "text-white"
                    }`}
                  >
                    {partnershipRuns}
                  </div>
                  <div className="text-xs uppercase text-slate-500 mt-1">
                    Runs
                  </div>
                </div>

                {/* Balls */}
                <div className="xl:col-span-1 text-center">
                  <div
                    className={`text-2xl font-bold ${
                      active ? "text-emerald-400" : "text-white"
                    }`}
                  >
                    {p.partnershipBalls || 0}
                  </div>
                  <div className="text-xs uppercase text-slate-500 mt-1">
                    Balls
                  </div>
                </div>

                {/* Run Rate */}
                <div className="xl:col-span-2 text-center">
                  <div
                    className={`text-2xl font-bold ${
                      active ? "text-emerald-400" : "text-white"
                    }`}
                  >
                    {runRate || "0.00"}
                  </div>
                  <div className="text-xs uppercase text-slate-500 mt-1">
                    Run Rate
                  </div>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* ================= FOOTER SUMMARY ================= */}
      {/* <div className="border-t border-slate-800 bg-slate-900/80 px-8 py-6">
        <div className="grid grid-cols-2 lg:grid-cols-5 gap-6">
          <SummaryStat label="Total Partnerships" value={totalPartnerships} />
          <SummaryStat label="Total Runs Added" value={totalRuns} />
          <SummaryStat label="Total Balls" value={totalBalls} />
          <SummaryStat label="Overall Run Rate" value={overallRunRate} />
          <SummaryStat
            label="Longest Partnership"
            value={
              longestPartnership
                ? `${longestPartnership.runs} (${longestPartnership.balls})`
                : "-"
            }
          />
        </div>
      </div> */}
    </div>
  );
}

/* ================= SUMMARY STAT ================= */
function SummaryStat({ label, value }) {
  return (
    <div className="text-center">
      <div className="text-xs uppercase tracking-wider text-slate-500 mb-2">
        {label}
      </div>
      <div className="text-2xl font-bold text-white">{value}</div>
    </div>
  );
}
