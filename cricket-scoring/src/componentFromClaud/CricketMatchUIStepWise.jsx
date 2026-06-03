import React, { useState, useEffect } from "react";
import axios from "axios";
import { useParams } from "react-router-dom";
import { getMatchSetup, updateMatch, getAllTournaments } from "../services/api";

const client = axios.create({
  baseURL: "http://localhost:8080",
  headers: { "Content-Type": "application/json" },
});

const api = {
  getAllTeams: () =>
    client.get("/teams").then((r) => r.data.data ?? r.data.data),
  getTeamById: (id) =>
    client.get(`/teams/${id}`).then((r) => {
      return r.data.data ?? r.data.data;
    }),
  getAllVenues: () =>
    client.get("/venues").then((r) => {
      return r.data.data ?? r.data.data;
    }),
  createMatch: (body) =>
    client.post("/matches/create", body).then((r) => r.data),
};

const MATCH_FORMATS = ["T20", "ODI", "TEST", "T10", "THE_HUNDRED"];
const TOURNAMENTS = ["IPL", "BIG BASH LEAGUE", "CHAMPIONS TROPHY"];
const STEPS = ["Match Details", "Teams & Squads", "Toss", "Confirm & Create"];
const MAX_XI = 11;
const MAX_SUB = 5;
const EMPTY_SQUAD = {
  xi: Array(MAX_XI).fill(null),
  sub: Array(MAX_SUB).fill(null),
  bench: [],
};

// ─── Squad helpers ────────────────────────────────────────────────────────────
function initSquad(players = []) {
  return {
    xi: Array(MAX_XI)
      .fill(null)
      .map((_, i) => players[i] ?? null),
    sub: Array(MAX_SUB)
      .fill(null)
      .map((_, i) => players[MAX_XI + i] ?? null),
    bench: players.slice(MAX_XI + MAX_SUB).map((p) => ({ ...p })),
  };
}

// ─── Sidebar ──────────────────────────────────────────────────────────────────
const Sidebar = ({ currentStep }) => {
  const menu = [
    "Dashboard",
    "Create Match",
    "Matches",
    "Scoreboard",
    "Live Scoring",
    "Commentary",
    "Players",
    "Teams",
    "Venues",
    "Reports",
    "Settings",
  ];
  return (
    <aside className="w-64 bg-slate-950 text-white min-h-screen p-5 flex-shrink-0 flex flex-col">
      <h1 className="text-2xl font-bold mb-8 tracking-tight">
        CRIC <span className="text-blue-400">SCORER</span>
      </h1>
      <nav className="space-y-1 flex-1">
        {menu.map((item) => (
          <button
            key={item}
            className={`w-full text-left px-4 py-3 rounded-xl text-sm transition-all ${
              item === "Create Match"
                ? "bg-blue-600 text-white font-semibold"
                : "text-slate-400 hover:bg-slate-800 hover:text-white"
            }`}
          >
            {item}
          </button>
        ))}
      </nav>
      <div className="mt-6 rounded-xl bg-slate-900 p-4 border border-slate-800">
        <p className="text-xs text-slate-500 uppercase tracking-wider mb-2">
          Progress
        </p>
        <div className="space-y-2">
          {STEPS.map((s, i) => (
            <div key={i} className="flex items-center gap-2">
              <div
                className={`w-2 h-2 rounded-full ${i <= currentStep ? "bg-blue-400" : "bg-slate-700"}`}
              />
              <span
                className={`text-xs ${i === currentStep ? "text-white font-medium" : "text-slate-500"}`}
              >
                {s}
              </span>
            </div>
          ))}
        </div>
      </div>
    </aside>
  );
};

// ─── Step Bar ─────────────────────────────────────────────────────────────────
const StepBar = ({ current }) => (
  <div className="flex items-center gap-3 mb-8">
    {STEPS.map((step, i) => (
      <React.Fragment key={i}>
        <div className="flex items-center gap-2">
          <div
            className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold transition-all ${
              i < current
                ? "bg-blue-600 text-white"
                : i === current
                  ? "bg-blue-600 text-white ring-4 ring-blue-100"
                  : "bg-gray-200 text-gray-500"
            }`}
          >
            {i < current ? "✓" : i + 1}
          </div>
          <span
            className={`text-sm font-medium ${
              i === current
                ? "text-blue-700"
                : i < current
                  ? "text-blue-500"
                  : "text-gray-400"
            }`}
          >
            {step}
          </span>
        </div>
        {i !== STEPS.length - 1 && (
          <div
            className={`flex-1 h-[2px] transition-all ${i < current ? "bg-blue-400" : "bg-gray-200"}`}
          />
        )}
      </React.Fragment>
    ))}
  </div>
);

// ─── Form helpers ─────────────────────────────────────────────────────────────
const Field = ({ label, children }) => (
  <div>
    <label className="block text-sm font-medium text-slate-700 mb-1.5">
      {label}
    </label>
    {children}
  </div>
);

const inputCls =
  "w-full border border-slate-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition bg-white";
const selectCls =
  "w-full border border-slate-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white";

const Toggle = ({ label, checked, onChange }) => (
  <div className="flex items-center justify-between p-3.5 rounded-xl bg-slate-50 border border-slate-200">
    <span className="text-sm font-medium text-slate-700">{label}</span>
    <button
      onClick={() => onChange(!checked)}
      className={`relative w-11 h-6 rounded-full transition-colors ${checked ? "bg-blue-600" : "bg-slate-300"}`}
    >
      <span
        className={`absolute top-1 w-4 h-4 rounded-full bg-white shadow transition-transform ${checked ? "translate-x-6" : "translate-x-1"}`}
      />
    </button>
  </div>
);

// ─── Step 1: Match Details ────────────────────────────────────────────────────
const MatchDetailsStep = ({ form, setForm, venues, tournaments }) => (
  <div className="max-w-2xl mx-auto">
    <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-8">
      <h3 className="text-xl font-bold text-slate-800 mb-6">Match Details</h3>
      <div className="space-y-5">
        <div className="grid grid-cols-2 gap-4">
          <Field label="Match Name">
            <input
              className={inputCls}
              value={form.matchName}
              onChange={(e) =>
                setForm((f) => ({ ...f, matchName: e.target.value }))
              }
              placeholder="e.g. India vs Australia"
            />
          </Field>
          <Field label="Match Number">
            <input
              className={inputCls}
              type="number"
              value={form.matchNumber}
              onChange={(e) =>
                setForm((f) => ({ ...f, matchNumber: e.target.value }))
              }
              placeholder="1"
            />
          </Field>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <Field label="Format">
            <select
              className={selectCls}
              value={form.format}
              onChange={(e) =>
                setForm((f) => ({ ...f, format: e.target.value }))
              }
            >
              <option value="">Select Format</option>
              {MATCH_FORMATS.map((fmt) => (
                <option key={fmt}>{fmt}</option>
              ))}
            </select>
          </Field>
          <Field label="Venue">
            <select
              className={selectCls}
              value={form.venueId}
              onChange={(e) =>
                setForm((f) => ({ ...f, venueId: e.target.value }))
              }
            >
              <option value="">Select Venue</option>
              {(venues ?? []).map((v) => (
                <option key={v.id} value={v.id}>
                  {v.name}
                  {v.city ? `, ${v.city}` : ""}
                </option>
              ))}
            </select>
          </Field>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <Field label="Competition">
            <select
              className={selectCls}
              value={form.competition}
              onChange={(e) =>
                setForm((f) => ({ ...f, competition: e.target.value }))
              }
            >
              <option value="">Select Competition</option>
              {(tournaments ?? []).map((tour) => (
                <option key={tour.id} value={tour.id}>
                  {tour.name}
                </option>
              ))}
            </select>
          </Field>
          <Field label="Season">
            <input
              className={inputCls}
              value={form.season}
              onChange={(e) =>
                setForm((f) => ({ ...f, season: e.target.value }))
              }
              placeholder="e.g. 2024-25"
            />
          </Field>
        </div>

        <div className="border-t border-slate-100 pt-5">
          <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-4">
            Over Settings
          </p>
          <div className="grid grid-cols-2 gap-4">
            <Field label="Total Overs">
              <select
                className={selectCls}
                value={form.totalOvers}
                onChange={(e) =>
                  setForm((f) => ({ ...f, totalOvers: e.target.value }))
                }
              >
                {[5, 10, 20, 50].map((o) => (
                  <option key={o}>{o}</option>
                ))}
              </select>
            </Field>
            <Field label="Balls per Over">
              <select
                className={selectCls}
                value={form.ballsPerOver}
                onChange={(e) =>
                  setForm((f) => ({ ...f, ballsPerOver: e.target.value }))
                }
              >
                <option>6</option>
              </select>
            </Field>
          </div>
        </div>

        <div className="border-t border-slate-100 pt-5">
          <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-4">
            Powerplay
          </p>
          <div className="grid grid-cols-2 gap-4">
            <Field label="Start Over">
              <input
                className={inputCls}
                type="number"
                value={form.powerplayStartOver}
                onChange={(e) =>
                  setForm((f) => ({ ...f, powerplayStartOver: e.target.value }))
                }
                placeholder="1"
              />
            </Field>
            <Field label="End Over">
              <input
                className={inputCls}
                type="number"
                value={form.powerplayEndOver}
                onChange={(e) =>
                  setForm((f) => ({ ...f, powerplayEndOver: e.target.value }))
                }
                placeholder="6"
              />
            </Field>
          </div>
        </div>

        <div className="border-t border-slate-100 pt-5">
          <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-4">
            Rules
          </p>
          <div className="space-y-3">
            <Toggle
              label="DRS Enabled"
              checked={form.drsEnabled}
              onChange={(v) => setForm((f) => ({ ...f, drsEnabled: v }))}
            />
            {form.drsEnabled && (
              <Field label="Reviews per Team">
                <input
                  className={inputCls}
                  type="number"
                  value={form.reviewsPerTeam}
                  onChange={(e) =>
                    setForm((f) => ({ ...f, reviewsPerTeam: e.target.value }))
                  }
                  placeholder="2"
                />
              </Field>
            )}
            <Toggle
              label="Super Over Enabled"
              checked={form.superOverEnabled}
              onChange={(v) => setForm((f) => ({ ...f, superOverEnabled: v }))}
            />
            <Toggle
              label="DLS Enabled"
              checked={form.dlsEnabled}
              onChange={(v) => setForm((f) => ({ ...f, dlsEnabled: v }))}
            />
            <Toggle
              label="ImpactPlayer Enabled"
              checked={form.impactPlayerEnabled}
              onChange={(v) =>
                setForm((f) => ({ ...f, impactPlayerEnabled: v }))
              }
            />
          </div>
        </div>
      </div>
    </div>
  </div>
);

// ─── Step 2: Squad Manager ────────────────────────────────────────────────────
const badgeCfg = {
  xi: { label: "XI", bg: "bg-blue-100", text: "text-blue-700" },
  sub: { label: "SUB", bg: "bg-amber-100", text: "text-amber-700" },
  bench: { label: "BENCH", bg: "bg-slate-100", text: "text-slate-500" },
};

const PlayerCard = ({ player, index, section, onMove, xi, sub }) => {
  const xiCount = xi.filter(Boolean).length;
  const subCount = sub.filter(Boolean).length;
  const canToXI = section !== "xi" && xiCount < MAX_XI;
  const canToSub = section !== "sub" && subCount < MAX_SUB;
  const canToBench = section !== "bench";

  const cardStyle = {
    xi: "border-blue-200 bg-blue-50/60 hover:bg-blue-50",
    sub: "border-amber-200 bg-amber-50/60 hover:bg-amber-50",
    bench: "border-slate-100 bg-white hover:bg-slate-50",
  }[section];

  return (
    <div
      className={`flex items-center justify-between px-3 py-2.5 rounded-xl border transition group ${cardStyle}`}
    >
      <div className="flex items-center gap-3">
        {section === "xi" && (
          <span className="text-[10px] text-slate-300 font-mono w-4 text-center shrink-0">
            {index + 1}
          </span>
        )}
        <span
          className={`text-[10px] font-bold px-1.5 py-0.5 rounded-md shrink-0 ${badgeCfg[section].bg} ${badgeCfg[section].text}`}
        >
          {badgeCfg[section].label}
        </span>
        <div className="min-w-0">
          <p className="text-sm font-semibold text-slate-800 leading-tight truncate">
            {player.fullName}
          </p>
          <p className="text-xs text-slate-400">{player.role ?? "Player"}</p>
        </div>
      </div>
      <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity shrink-0 ml-2">
        {canToXI && (
          <button
            onClick={() => onMove(player, section, index, "xi")}
            className="text-[11px] px-2 py-1 rounded-lg bg-blue-600 text-white font-medium hover:bg-blue-700 transition"
          >
            → XI
          </button>
        )}
        {canToSub && (
          <button
            onClick={() => onMove(player, section, index, "sub")}
            className="text-[11px] px-2 py-1 rounded-lg bg-amber-500 text-white font-medium hover:bg-amber-600 transition"
          >
            → SUB
          </button>
        )}
        {canToBench && (
          <button
            onClick={() => onMove(player, section, index, "bench")}
            className="text-[11px] px-2 py-1 rounded-lg bg-slate-200 text-slate-600 font-medium hover:bg-slate-300 transition"
          >
            → BENCH
          </button>
        )}
      </div>
    </div>
  );
};

const EmptySlot = ({ index, label }) => (
  <div className="flex items-center gap-3 px-3 py-2.5 rounded-xl border-2 border-dashed border-slate-200 text-slate-300">
    {label === "xi" && (
      <span className="text-[10px] font-mono w-4 text-center shrink-0">
        {index + 1}
      </span>
    )}
    <span className="text-xs italic">Empty slot</span>
  </div>
);

const SectionHeader = ({ title, count, max, dotColor }) => {
  const pct = max ? Math.round((count / max) * 100) : 0;
  return (
    <div className="flex items-center justify-between mb-2">
      <div className="flex items-center gap-2">
        <span className={`w-2 h-2 rounded-full ${dotColor}`} />
        <span className="text-xs font-bold text-slate-600 uppercase tracking-wider">
          {title}
        </span>
      </div>
      {max ? (
        <div className="flex items-center gap-2">
          <div className="w-16 h-1.5 rounded-full bg-slate-200 overflow-hidden">
            <div
              className={`h-full rounded-full transition-all ${count === max ? "bg-green-500" : dotColor}`}
              style={{ width: `${pct}%` }}
            />
          </div>
          <span
            className={`text-xs font-semibold ${count === max ? "text-green-600" : "text-slate-500"}`}
          >
            {count}/{max}
          </span>
        </div>
      ) : (
        <span className="text-xs text-slate-400">{count} players</span>
      )}
    </div>
  );
};

// Squad state is now owned by parent — passed in as props
const TeamSquadManager = ({
  teamLabel,
  teamData,
  allTeams,
  onTeamChange,
  slot,
  squad,
  setSquad,
}) => {
  useEffect(() => {
    // ONLY initialize if squad is empty
    const hasPlayers =
      squad?.xi?.some(Boolean) ||
      squad?.sub?.some(Boolean) ||
      squad?.bench?.length > 0;

    if (hasPlayers) return;

    setSquad(
      teamData?.players
        ? initSquad(teamData.players)
        : {
            xi: Array(MAX_XI).fill(null),
            sub: Array(MAX_SUB).fill(null),
            bench: [],
          },
    );
  }, [teamData]);

  const move = (player, fromSection, fromIndex, toSection) => {
    setSquad((prev) => {
      const next = {
        xi: [...prev.xi],
        sub: [...prev.sub],
        bench: [...prev.bench],
      };

      // Place into target
      if (toSection === "xi") {
        const emptyIdx = next.xi.findIndex((s) => s === null);
        if (emptyIdx === -1) return prev;
        next.xi[emptyIdx] = player;
      } else if (toSection === "sub") {
        const emptyIdx = next.sub.findIndex((s) => s === null);
        if (emptyIdx === -1) return prev;
        next.sub[emptyIdx] = player;
      } else {
        next.bench = [...next.bench, player];
      }

      // Clear source
      if (fromSection === "xi") next.xi[fromIndex] = null;
      else if (fromSection === "sub") next.sub[fromIndex] = null;
      else next.bench = next.bench.filter((p) => p.id !== player.id);

      return next;
    });
  };

  const { xi = [], sub = [], bench = [] } = squad || {};
  const xiCount = xi.filter(Boolean).length;
  const subCount = sub.filter(Boolean).length;
  const totalLoaded = xiCount + subCount + bench.length;
  const isEmpty = totalLoaded === 0;

  return (
    <div
      className="bg-white rounded-2xl shadow-sm border border-slate-200 flex flex-col"
      style={{ minHeight: "72vh" }}
    >
      {/* Header */}
      <div className="flex items-center justify-between p-5 border-b border-slate-100 flex-shrink-0">
        <div>
          <h3 className="font-bold text-slate-800">
            {teamData?.name ?? `Select ${teamLabel}`}
          </h3>
          <p className="text-xs text-slate-400 mt-0.5">
            {slot === "A" ? "🏠 Home" : "✈️ Away"} · {totalLoaded} players
            loaded
          </p>
        </div>
        <select
          className="border border-slate-200 rounded-xl px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
          value={teamData?.id ?? ""}
          onChange={(e) => onTeamChange(slot, e.target.value)}
        >
          <option value="">Select Team</option>
          {(allTeams ?? []).map((t) => (
            <option key={t.id} value={t.id}>
              {t.name}
            </option>
          ))}
        </select>
      </div>

      {/* Empty state */}
      {isEmpty ? (
        <div className="flex-1 flex items-center justify-center p-8 text-center">
          <div>
            <div className="text-4xl mb-3">🏏</div>
            <p className="text-slate-400 text-sm">
              Select a team to load squad
            </p>
            <p className="text-slate-300 text-xs mt-1">
              First {MAX_XI} → XI · Next {MAX_SUB} → Subs · Rest → Bench
            </p>
          </div>
        </div>
      ) : (
        <div className="flex-1 overflow-y-auto p-5 space-y-6">
          {/* Playing XI */}
          <div>
            <SectionHeader
              title="Playing XI"
              count={xiCount}
              max={MAX_XI}
              dotColor="bg-blue-500"
            />
            <div className="space-y-1.5">
              {xi.map((p, i) =>
                p ? (
                  <PlayerCard
                    key={`xi-${p.id}-${i}`}
                    player={p}
                    index={i}
                    section="xi"
                    onMove={move}
                    xi={xi}
                    sub={sub}
                  />
                ) : (
                  <EmptySlot key={`xi-empty-${i}`} index={i} label="xi" />
                ),
              )}
            </div>
          </div>

          {/* Substitutes */}
          <div>
            <SectionHeader
              title="Substitutes"
              count={subCount}
              max={MAX_SUB}
              dotColor="bg-amber-400"
            />
            <div className="space-y-1.5">
              {sub.map((p, i) =>
                p ? (
                  <PlayerCard
                    key={`sub-${p.id}-${i}`}
                    player={p}
                    index={i}
                    section="sub"
                    onMove={move}
                    xi={xi}
                    sub={sub}
                  />
                ) : (
                  <EmptySlot key={`sub-empty-${i}`} index={i} label="sub" />
                ),
              )}
            </div>
          </div>

          {/* Bench */}
          {bench.length > 0 && (
            <div>
              <SectionHeader
                title="Bench"
                count={bench.length}
                max={null}
                dotColor="bg-slate-400"
              />
              <div className="space-y-1.5">
                {bench.map((p, i) => (
                  <PlayerCard
                    key={`bench-${p.id}-${i}`}
                    player={p}
                    index={i}
                    section="bench"
                    onMove={move}
                    xi={xi}
                    sub={sub}
                  />
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Footer */}
      {!isEmpty && (
        <div className="border-t border-slate-100 px-5 py-3 flex gap-5 flex-shrink-0 items-center">
          <div className="flex items-center gap-1.5">
            <span className="w-2 h-2 rounded-full bg-blue-500" />
            <span className="text-xs text-slate-500">
              XI:{" "}
              <strong
                className={
                  xiCount === MAX_XI ? "text-green-600" : "text-slate-700"
                }
              >
                {xiCount}
              </strong>
            </span>
          </div>
          <div className="flex items-center gap-1.5">
            <span className="w-2 h-2 rounded-full bg-amber-400" />
            <span className="text-xs text-slate-500">
              Subs: <strong>{subCount}</strong>
            </span>
          </div>
          <div className="flex items-center gap-1.5">
            <span className="w-2 h-2 rounded-full bg-slate-300" />
            <span className="text-xs text-slate-500">
              Bench: <strong>{bench.length}</strong>
            </span>
          </div>
          {xiCount === MAX_XI && (
            <span className="ml-auto text-xs font-semibold text-green-600">
              ✓ XI complete
            </span>
          )}
        </div>
      )}
    </div>
  );
};

// Squad state lifted to parent (main component) via props
const TeamsStep = ({
  team1,
  team2,
  allTeams,
  onTeamChange,
  squad1,
  setSquad1,
  squad2,
  setSquad2,
}) => (
  <div className="grid grid-cols-2 gap-6">
    <TeamSquadManager
      teamLabel="Home Team"
      teamData={team1}
      allTeams={allTeams}
      onTeamChange={onTeamChange}
      slot="A"
      squad={squad1}
      setSquad={setSquad1}
    />
    <TeamSquadManager
      teamLabel="Away Team"
      teamData={team2}
      allTeams={allTeams.filter((t) => t.id !== team1?.id)}
      onTeamChange={onTeamChange}
      slot="B"
      squad={squad2}
      setSquad={setSquad2}
    />
  </div>
);

// ─── Step 3: Toss ─────────────────────────────────────────────────────────────
const TossStep = ({ team1, team2, toss, setToss }) => {
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
            {["Manual", "Random"].map((m) => (
              <button
                key={m}
                onClick={() => setToss((t) => ({ ...t, method: m }))}
                className={`p-3 rounded-xl border text-sm font-medium transition ${
                  toss.method === m
                    ? "bg-blue-600 text-white border-blue-600"
                    : "border-slate-200 text-slate-600 hover:border-blue-300"
                }`}
              >
                {m}
              </button>
            ))}
          </div>
        </div>

        <div>
          <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-3">
            Toss Winner
          </p>
          <div className="space-y-3">
            {[team1, team2].map((t, i) =>
              t ? (
                <button
                  key={t.id}
                  onClick={() => setToss((tt) => ({ ...tt, winnerId: t.id }))}
                  className={`w-full p-3.5 rounded-xl border text-sm font-medium text-left transition ${
                    toss.winnerId === t.id
                      ? "bg-blue-50 border-blue-500 text-blue-700"
                      : "border-slate-200 text-slate-600 hover:border-blue-300"
                  }`}
                >
                  {t.shortName && <span className="mr-2">{t.shortName}</span>}
                  {t.name}
                  <span className="text-xs text-slate-400 ml-2">
                    {i === 0 ? "(Home)" : "(Away)"}
                  </span>
                </button>
              ) : (
                <div
                  key={i}
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
            {["Bat", "Bowl", "Field"].map((d) => (
              <button
                key={d}
                onClick={() => setToss((t) => ({ ...t, decision: d }))}
                className={`p-3 rounded-xl border text-sm font-medium transition ${
                  toss.decision === d
                    ? "bg-blue-600 text-white border-blue-600"
                    : "border-slate-200 text-slate-600 hover:border-blue-300"
                }`}
              >
                {d}
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
            className={`text-sm font-medium ${tossWinner ? "text-green-700" : "text-slate-400"}`}
          >
            {summary}
          </p>
        </div>
      </div>
    </div>
  );
};

// ─── Step 4: Confirm ──────────────────────────────────────────────────────────
const ConfirmStep = ({ form, team1, team2, toss, venues, squad1, squad2 }) => {
  const venue = venues.find((v) => String(v.id) === String(form.venueId));
  const tossWinner =
    toss.winnerId === team1?.id
      ? team1
      : toss.winnerId === team2?.id
        ? team2
        : null;

  const Row = ({ label, value }) => (
    <div className="flex justify-between py-2.5 border-b border-slate-100 last:border-0">
      <span className="text-sm text-slate-500">{label}</span>
      <span className="text-sm font-medium text-slate-800">{value ?? "—"}</span>
    </div>
  );

  const SquadSummary = ({ label, squad }) => (
    <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
      <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-4">
        {label}
      </p>
      <div className="space-y-3">
        <div>
          <p className="text-xs text-slate-500 mb-1">
            Playing XI ({squad.xi.filter(Boolean).length}/11)
          </p>
          <div className="flex flex-wrap gap-1.5">
            {squad.xi.map((p, i) =>
              p ? (
                <span
                  key={i}
                  className="text-xs px-2 py-1 bg-blue-50 text-blue-700 rounded-lg border border-blue-200"
                >
                  {i + 1}. {p.fullName}
                </span>
              ) : (
                <span
                  key={i}
                  className="text-xs px-2 py-1 bg-slate-50 text-slate-300 rounded-lg border border-dashed border-slate-200"
                >
                  {i + 1}. Empty
                </span>
              ),
            )}
          </div>
        </div>
        <div>
          <p className="text-xs text-slate-500 mb-1">
            Substitutes ({squad.sub.filter(Boolean).length}/5)
          </p>
          <div className="flex flex-wrap gap-1.5">
            {squad.sub.filter(Boolean).map((p, i) => (
              <span
                key={i}
                className="text-xs px-2 py-1 bg-amber-50 text-amber-700 rounded-lg border border-amber-200"
              >
                {p.fullName}
              </span>
            ))}
            {squad.sub.filter(Boolean).length === 0 && (
              <span className="text-xs text-slate-300 italic">None</span>
            )}
          </div>
        </div>
        {squad.bench.length > 0 && (
          <div>
            <p className="text-xs text-slate-500 mb-1">
              Bench ({squad.bench.length})
            </p>
            <div className="flex flex-wrap gap-1.5">
              {squad.bench.map((p, i) => (
                <span
                  key={i}
                  className="text-xs px-2 py-1 bg-slate-50 text-slate-600 rounded-lg border border-slate-200"
                >
                  {p.fullName}
                </span>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );

  return (
    <div className="max-w-3xl mx-auto space-y-5">
      <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
        <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-4">
          Match Details
        </p>
        <Row label="Match Name" value={form.matchName} />
        <Row label="Match Number" value={form.matchNumber} />
        <Row label="Format" value={form.format} />
        <Row label="Competition" value={form.competition} />
        <Row label="Season" value={form.season} />
        <Row label="Venue" value={venue?.name} />
        <Row label="Total Overs" value={form.totalOvers} />
        <Row label="Balls per Over" value={form.ballsPerOver} />
        <Row
          label="Powerplay"
          value={`Over ${form.powerplayStartOver} – ${form.powerplayEndOver}`}
        />
      </div>

      <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
        <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-4">
          Rules
        </p>
        <Row label="DRS Enabled" value={form.drsEnabled ? "Yes" : "No"} />
        {form.drsEnabled && (
          <Row label="Reviews per Team" value={form.reviewsPerTeam} />
        )}
        <Row label="Super Over" value={form.superOverEnabled ? "Yes" : "No"} />
        <Row label="DLS Enabled" value={form.dlsEnabled ? "Yes" : "No"} />
        <Row
          label="Impact Player Enabled"
          value={form.impactPlayerEnabled ? "Yes" : "No"}
        />
      </div>

      <SquadSummary
        label={`Home Team — ${team1?.name ?? "—"}`}
        squad={squad1}
      />
      <SquadSummary
        label={`Away Team — ${team2?.name ?? "—"}`}
        squad={squad2}
      />

      <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
        <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-4">
          Toss
        </p>
        <Row label="Method" value={toss.method} />
        <Row label="Winner" value={tossWinner?.name} />
        <Row label="Decision" value={toss.decision} />
      </div>
    </div>
  );
};

// ─── Main ─────────────────────────────────────────────────────────────────────
export default function CricketMatchUIStepWise({ mode }) {
  const { matchId } = useParams();
  const [allTeams, setAllTeams] = useState([]);
  const [allVenues, setAllVenues] = useState([]);
  const [allTournaments, setAllTournaments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentStep, setCurrentStep] = useState(0);
  const [submitting, setSubmitting] = useState(false);

  const [team1, setTeam1] = useState(null);
  const [team2, setTeam2] = useState(null);

  // Squad state lifted here so handleCreate can access it
  const [squad1, setSquad1] = useState({
    xi: Array(MAX_XI).fill(null),
    sub: Array(MAX_SUB).fill(null),
    bench: [],
  });
  const [squad2, setSquad2] = useState({
    xi: Array(MAX_XI).fill(null),
    sub: Array(MAX_SUB).fill(null),
    bench: [],
  });

  const [form, setForm] = useState({
    matchName: "",
    matchNumber: "",
    format: "",
    competition: "",
    season: "",
    venueId: "",
    totalOvers: 20,
    ballsPerOver: 6,
    powerplayStartOver: 1,
    powerplayEndOver: 6,
    drsEnabled: false,
    reviewsPerTeam: 2,
    superOverEnabled: true,
    dlsEnabled: false,
    impactPlayerEnabled: false,
  });

  const [toss, setToss] = useState({
    method: "Manual",
    winnerId: null,
    decision: "Bat",
  });

  const fetchTournaments = async () => {
    const res = await getAllTournaments();
    console.log("tourn ", res);
    setAllTournaments(res);
  };

  useEffect(() => {
    fetchTournaments();
  },[]);

  useEffect(() => {
    if (mode !== "edit") return;

    const fetchMatch = async () => {
      try {
        const res = await getMatchSetup(matchId);

        const setup = res.data;
        console.log(setup.data);

        // FORM
        setForm({
          matchName: setup.data.matchInfo.matchName || "",

          matchNumber: setup.data.matchInfo.matchNumber || "",

          format: setup.data.matchInfo.format || "",

          competition: setup.data.matchInfo.competition || "",

          season: setup.data.matchInfo.season || "",

          venueId: setup.data.venue.id || "",

          totalOvers: setup.data.rules.overs || 20,

          ballsPerOver: setup.data.rules.ballsPerOver || 6,

          powerplayStartOver: setup.data.rules.powerplayStartOver || 1,

          powerplayEndOver: setup.data.rules.powerplayEndOver || 6,

          drsEnabled: setup.data.rules.drsEnabled ?? false,

          reviewsPerTeam: setup.data.rules.reviewsPerTeam || 0,

          superOverEnabled: setup.data.rules.superOverEnabled ?? false,

          dlsEnabled: setup.data.rules.dlsEnabled ?? false,

          impactPlayerEnabled: setup.data.rules.impactPlayerEnabled ?? false,
        });

        // TEAMS
        setTeam1(setup.data.teams.homeTeam || {});

        setTeam2(setup.data.teams.awayTeam || {});

        // SQUADS
        setSquad1({
          xi: Array(MAX_XI)
            .fill(null)
            .map(
              (_, i) =>
                setup?.data?.squads?.homeTeamPlaying11?.players?.[i] || null,
            ),

          sub: Array(MAX_SUB)
            .fill(null)
            .map(
              (_, i) =>
                setup?.data?.squads?.homeTeamSubstitutes?.players?.[i] || null,
            ),

          bench: setup?.data?.squads?.homeTeamBenchPlayers?.players || [],
        });

        setSquad2({
          xi: Array(MAX_XI)
            .fill(null)
            .map(
              (_, i) =>
                setup?.data?.squads?.awayTeamPlaying11?.players?.[i] || null,
            ),

          sub: Array(MAX_SUB)
            .fill(null)
            .map(
              (_, i) =>
                setup?.data?.squads?.awayTeamSubstitutes?.players?.[i] || null,
            ),

          bench: setup?.data?.squads?.awayTeamBenchPlayers?.players || [],
        });

        // TOSS
        setToss({
          winnerId: setup?.data?.toss?.winnerId || null,
          decision: setup.data.toss.tossDecision || "",
          method: setup.data.toss.tossMethod || "",
        });
      } catch (err) {
        console.error(err);
      }
    };

    fetchMatch();
  }, [mode, matchId]);

  useEffect(() => {
    Promise.all([api.getAllTeams(), api.getAllVenues()])
      .then(([teams, venues]) => {
        setAllTeams(teams);
        setAllVenues(venues);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  const handleTeamChange = async (slot, teamId) => {
    if (!teamId) {
      slot === "A" ? setTeam1(null) : setTeam2(null);
      return;
    }
    try {
      const data = await api.getTeamById(teamId);
      if (slot === "A") {
        setTeam1(data);

        if (mode !== "edit") {
          setSquad1(initSquad(data.players || []));
        }
      } else {
        setTeam2(data);

        if (mode !== "edit") {
          setSquad2(initSquad(data.players || []));
        }
      }
    } catch (err) {
      console.error("Team load error:", err);
      alert("Failed to load team: " + err.message);
    }
  };

  const handleCreate = async () => {
    if (!team1 || !team2) {
      alert("Please select both teams.");
      return;
    }
    if (!form.venueId) {
      alert("Please select a venue.");
      return;
    }
    const payload = {
      // Match details
      matchName: form.matchName,
      matchNumber: Number(form.matchNumber),
      format: form.format,
      competition: Number(form.competition),
      season: form.season,
      venueId: Number(form.venueId),
      totalOvers: Number(form.totalOvers),
      ballsPerOver: Number(form.ballsPerOver),
      powerplayStartOver: Number(form.powerplayStartOver),
      powerplayEndOver: Number(form.powerplayEndOver),
      drsEnabled: form.drsEnabled,
      reviewsPerTeam: Number(form.reviewsPerTeam),
      superOverEnabled: form.superOverEnabled,
      dlsEnabled: form.dlsEnabled,
      impactPlayerEnabled: form.impactPlayerEnabled,

      // Teams
      homeTeamId: team1.id,
      awayTeamId: team2.id,

      // Home squad — only non-null players, mapped to their IDs
      homePlaying11: squad1.xi.filter(Boolean).map((p) => p.id),
      homeSubstitutes: squad1.sub.filter(Boolean).map((p) => p.id),
      homeBenchPlayers: squad1.bench.map((p) => p.id),

      // Away squad
      awayPlaying11: squad2.xi.filter(Boolean).map((p) => p.id),
      awaySubstitutes: squad2.sub.filter(Boolean).map((p) => p.id),
      awayBenchPlayers: squad2.bench.map((p) => p.id),

      // Toss
      tossWinner: toss.winnerId,
      tossDecision: toss.decision.toUpperCase(),
      tossMethod: toss.method,
    };

    setSubmitting(true);
    try {
      if (mode === "edit") {
        console.log("payload ", payload);
        await updateMatch(matchId, payload);

        alert("Match updated successfully!");
      } else {
        await api.createMatch(payload);

        alert("Match created successfully!");
      }
    } catch (err) {
      console.error("Create match error:", err);
      alert("Failed to create match: " + err.message);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading)
    return (
      <div className="flex items-center justify-center min-h-screen bg-slate-100">
        <p className="text-slate-500 animate-pulse">Connecting to server…</p>
      </div>
    );

  if (error)
    return (
      <div className="flex items-center justify-center min-h-screen bg-slate-100">
        <div className="bg-white rounded-2xl p-8 text-center shadow">
          <p className="text-red-500 font-semibold mb-1">Failed to connect</p>
          <p className="text-slate-400 text-sm">{error}</p>
          <p className="text-slate-300 text-xs mt-1">
            Make sure Spring Boot is running on port 8080
          </p>
        </div>
      </div>
    );

  return (
    <div className="flex bg-slate-100 min-h-screen">
      <Sidebar currentStep={currentStep} />

      <div className="flex-1 flex flex-col overflow-auto">
        <header className="bg-slate-950 text-white px-8 py-4 flex justify-between items-center shadow-lg flex-shrink-0">
          <div className="flex items-center gap-3">
            <span className="text-slate-400 text-sm">
              {mode === "edit" ? "Update Match" : "Create Match"}
            </span>
            <span className="text-slate-600">›</span>
            <span className="text-white text-sm font-medium">
              {STEPS[currentStep]}
            </span>
          </div>
          <div className="flex items-center gap-4">
            <button className="text-slate-400 hover:text-white transition text-sm">
              ⚙ Settings
            </button>
            <div className="w-8 h-8 rounded-full bg-blue-500 flex items-center justify-center font-bold text-sm">
              S
            </div>
          </div>
        </header>

        <main className="flex-1 p-8">
          <div className="flex justify-between items-center mb-6">
            <div>
              <h2 className="text-3xl font-bold text-slate-800">
                Create New Match
              </h2>
              <p className="text-slate-400 text-sm mt-1">
                Step {currentStep + 1} of {STEPS.length} — {STEPS[currentStep]}
              </p>
            </div>
            <div className="flex gap-3">
              <button
                onClick={() => setCurrentStep((s) => Math.max(0, s - 1))}
                disabled={currentStep === 0}
                className="px-5 py-2.5 border border-slate-200 rounded-xl bg-white text-sm font-medium text-slate-600 hover:border-slate-300 disabled:opacity-30 disabled:cursor-not-allowed transition"
              >
                ← Back
              </button>
              {currentStep < STEPS.length - 1 ? (
                <button
                  onClick={() => setCurrentStep((s) => s + 1)}
                  className="px-5 py-2.5 rounded-xl bg-blue-600 text-white text-sm font-medium hover:bg-blue-700 transition"
                >
                  Next →
                </button>
              ) : (
                <button
                  onClick={handleCreate}
                  disabled={submitting}
                  className="px-5 py-2.5 rounded-xl bg-green-600 text-white text-sm font-medium hover:bg-green-700 disabled:opacity-50 transition"
                >
                  {submitting
                    ? mode === "edit"
                      ? "Updating..."
                      : "Creating..."
                    : mode === "edit"
                      ? "✓ Update Match"
                      : "✓ Confirm & Create"}
                </button>
              )}
            </div>
          </div>

          <StepBar current={currentStep} />
          {currentStep === 0 && (
            <MatchDetailsStep
              form={form}
              setForm={setForm}
              venues={allVenues}
              tournaments={allTournaments}
            />
          )}
          {currentStep === 1 && (
            <TeamsStep
              team1={team1}
              team2={team2}
              allTeams={allTeams}
              onTeamChange={handleTeamChange}
              squad1={squad1}
              setSquad1={setSquad1}
              squad2={squad2}
              setSquad2={setSquad2}
            />
          )}
          {currentStep === 2 && (
            <TossStep
              team1={team1}
              team2={team2}
              toss={toss}
              setToss={setToss}
            />
          )}
          {currentStep === 3 && (
            <ConfirmStep
              form={form}
              team1={team1}
              team2={team2}
              toss={toss}
              venues={allVenues}
              squad1={squad1}
              squad2={squad2}
            />
          )}
        </main>
      </div>
    </div>
  );
}
