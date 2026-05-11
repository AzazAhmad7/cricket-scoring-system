import { useState, useEffect, useCallback } from "react";

// ─── CONFIG ──────────────────────────────────────────────────────────────────
const API_BASE = "https://your-backend.com/api"; // ← replace with your backend URL

async function apiFetch(path, options = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  if (!res.ok) throw new Error(`API error ${res.status}`);
  return res.json();
}

// ─── MOCK DATA (used when backend is unavailable) ─────────────────────────────
const MOCK_STATE = {
  match: { id: 25, venue: "Wankhede Stadium, Mumbai", date: "25 Apr 2026", startTime: "7:30 PM IST", umpires: "Nitin Menon, Kumar Dharmasena", thirdUmpire: "Paul Reiffel", matchReferee: "Andy Pycroft", toss: "India", electedTo: "Bat", field: "Day", pitch: "Good" },
  innings: { number: 1, powerplay: "P1 (1 - 6)", currentOver: 10, currentBall: 3 },
  battingTeam: {
    name: "India", code: "IND", flag: "🇮🇳",
    score: 72, wickets: 3, overs: "10.3",
    crr: 7.20,
    batsmen: [
      { name: "Virat Kohli", runs: 34, balls: 28, fours: 4, sixes: 1, sr: 121.43, onStrike: true },
      { name: "Rohit Sharma", runs: 22, balls: 18, fours: 3, sixes: 0, sr: 122.22, onStrike: false },
    ],
    squad: { playing: ["Rohit Sharma (c)", "Shubman Gill", "Virat Kohli", "Shreyas Iyer", "KL Rahul (wk)", "Hardik Pandya", "Ravindra Jadeja", "Axar Patel", "Jasprit Bumrah", "Mohammed Siraj", "Arshdeep Singh"], bench: ["Yuzvendra Chahal", "Ishan Kishan", "Sanju Samson"] },
    fallOfWickets: [
      { wicket: 1, score: 12, over: 2.1, batsman: "Shubman Gill" },
      { wicket: 2, score: 24, over: 3.6, batsman: "Shreyas Iyer" },
      { wicket: 3, score: 38, over: 6.2, batsman: "KL Rahul" },
    ],
  },
  bowlingTeam: {
    name: "Australia", code: "AUS", flag: "🇦🇺",
    rrr: 8.45,
    currentBowler: { name: "Mitchell Starc", overs: "2.3", maidens: 0, runs: 18, wickets: 1, econ: 7.20 },
    thisSpell: ["1", "•", "0", "•", "W", "•", "4", "•", "1", "•", "0", "•"],
    squad: { playing: ["David Warner", "Travis Head", "Marnus Labuschagne", "Steve Smith", "Glenn Maxwell", "Marcus Stoinis", "Alex Carey (wk)", "Pat Cummins (c)", "Mitchell Starc", "Josh Hazlewood", "Adam Zampa"], bench: ["Mitchell Marsh", "Cameron Green", "Matthew Wade"] },
    overHistory: [
      { over: 10.3, balls: [1, 0, "W", 4, 1, 0] },
      { over: 10.2, balls: [0, 2, 1, 0, 1, 0] },
      { over: 10.1, balls: [4, 0, 0, 1, "W", 2] },
      { over: 9, balls: [1, 1, 4, 0, 2, 1] },
      { over: 8, balls: [0, 0, 1, 1, 4, 0] },
      { over: 7, balls: [2, 4, 0, 1, 0, 1] },
    ],
  },
  partnership: { runs: 48, balls: 36 },
};

// ─── HELPERS ─────────────────────────────────────────────────────────────────
const ballColor = (b) => {
  if (b === "W") return "#ef4444";
  if (b === 4) return "#22c55e";
  if (b === 6) return "#a855f7";
  if (b === 0 || b === "•") return "#6b7280";
  return "#3b82f6";
};

const BallDot = ({ val }) => (
  <span style={{
    display: "inline-flex", alignItems: "center", justifyContent: "center",
    width: 28, height: 28, borderRadius: "50%",
    background: val === "•" ? "transparent" : ballColor(val),
    border: val === "•" ? "none" : "none",
    color: val === "•" ? "#6b7280" : "#fff",
    fontWeight: 700, fontSize: 12,
  }}>{val}</span>
);

// ─── MAIN COMPONENT ───────────────────────────────────────────────────────────
export default function CricScorer() {
  const [state, setState] = useState(MOCK_STATE);
  const [activeTab, setActiveTab] = useState("OVERS");
  const [activeNav, setActiveNav] = useState("Scoring");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [lastAction, setLastAction] = useState(null);

  // ── FETCH MATCH STATE FROM BACKEND ────────────────────────────────────────
  const fetchMatchState = useCallback(async () => {
    try {
      const data = await apiFetch(`/match/${MOCK_STATE.match.id}/state`);
      setState(data);
    } catch {
      // silently fall back to mock data in demo mode
    }
  }, []);

  useEffect(() => {
    fetchMatchState();
    const interval = setInterval(fetchMatchState, 5000); // poll every 5s
    return () => clearInterval(interval);
  }, [fetchMatchState]);

  // ── SEND BALL EVENT TO BACKEND ─────────────────────────────────────────────
  const sendBallEvent = async (event) => {
    setLoading(true);
    setLastAction(event);
    try {
      const payload = {
        matchId: state.match.id,
        inningsNumber: state.innings.number,
        over: state.innings.currentOver,
        ball: state.innings.currentBall,
        event,
        batsmanOnStrike: state.battingTeam.batsmen.find(b => b.onStrike)?.name,
        bowler: state.bowlingTeam.currentBowler.name,
        timestamp: new Date().toISOString(),
      };
      const updated = await apiFetch(`/match/${state.match.id}/ball`, {
        method: "POST",
        body: JSON.stringify(payload),
      });
      setState(updated);
    } catch {
      setError("Backend unavailable – running in demo mode");
      setTimeout(() => setError(null), 3000);
      // local mock update for demo
      setState(prev => localMockUpdate(prev, event));
    } finally {
      setLoading(false);
    }
  };

  // ── SEND CONTROL ACTIONS ───────────────────────────────────────────────────
  const sendAction = async (action) => {
    try {
      const updated = await apiFetch(`/match/${state.match.id}/action`, {
        method: "POST",
        body: JSON.stringify({ action, matchId: state.match.id }),
      });
      setState(updated);
    } catch {
      setError(`Action "${action}" – backend unavailable`);
      setTimeout(() => setError(null), 3000);
    }
  };

  const s = state;

  return (
    <div style={{ display: "flex", height: "100vh", fontFamily: "'DM Sans', 'Segoe UI', sans-serif", background: "#0f1117", color: "#e2e8f0", overflow: "hidden" }}>
      {/* ── SIDEBAR ── */}
      <aside style={{ width: 180, background: "#1a1d27", borderRight: "1px solid #2d3148", display: "flex", flexDirection: "column", flexShrink: 0 }}>
        <div style={{ padding: "16px 20px", borderBottom: "1px solid #2d3148", display: "flex", alignItems: "center", gap: 8 }}>
          <span style={{ background: "#ef4444", color: "#fff", fontWeight: 900, fontSize: 13, padding: "2px 6px", borderRadius: 4 }}>CRIC</span>
          <span style={{ fontWeight: 700, fontSize: 14, color: "#fff" }}>SCORER</span>
        </div>
        {["Scoring", "Scoreboard", "Commentary", "Players", "Partnerships", "Wagon Wheel", "Manhattan", "Reports", "Settings"].map(nav => (
          <button key={nav} onClick={() => setActiveNav(nav)} style={{
            display: "flex", alignItems: "center", gap: 10, padding: "11px 20px",
            background: activeNav === nav ? "#2563eb" : "transparent",
            color: activeNav === nav ? "#fff" : "#94a3b8",
            border: "none", cursor: "pointer", fontSize: 13, fontWeight: activeNav === nav ? 700 : 500,
            borderRadius: 0, textAlign: "left",
          }}>{nav}</button>
        ))}
        <div style={{ marginTop: "auto", padding: 16, borderTop: "1px solid #2d3148", fontSize: 12, color: "#64748b" }}>
          <div style={{ marginBottom: 4 }}>Over in Progress</div>
          <div style={{ fontSize: 22, fontWeight: 800, color: "#fff" }}>{s.innings.currentOver}.{s.innings.currentBall}</div>
          <span style={{ background: "#2563eb", color: "#fff", fontSize: 10, fontWeight: 700, padding: "2px 6px", borderRadius: 4 }}>P1</span>
          <div style={{ marginTop: 12, color: "#94a3b8" }}>Current Run Rate</div>
          <div style={{ fontSize: 20, fontWeight: 800, color: "#fff" }}>{s.battingTeam.crr.toFixed(2)}</div>
          <div style={{ marginTop: 8, color: "#94a3b8" }}>Required Run Rate</div>
          <div style={{ fontSize: 20, fontWeight: 800, color: "#ef4444" }}>{s.bowlingTeam.rrr.toFixed(2)}</div>
        </div>
      </aside>

      {/* ── MAIN ── */}
      <main style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
        {/* Top bar */}
        <header style={{ background: "#1a1d27", borderBottom: "1px solid #2d3148", padding: "10px 20px", display: "flex", alignItems: "center", justifyContent: "space-between", flexShrink: 0 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
            <select style={{ background: "#2d3148", color: "#e2e8f0", border: "1px solid #3d4160", borderRadius: 6, padding: "6px 12px", fontSize: 13 }}>
              <option>Match {s.match.id}</option>
            </select>
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: 16 }}>
            <div style={{ textAlign: "center" }}>
              <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                <span style={{ fontSize: 20 }}>{s.battingTeam.flag}</span>
                <div>
                  <div style={{ fontWeight: 800, fontSize: 15 }}>India</div>
                  <div style={{ fontSize: 11, color: "#64748b" }}>IND</div>
                </div>
              </div>
            </div>
            <span style={{ color: "#64748b", fontWeight: 700 }}>vs</span>
            <div style={{ textAlign: "center" }}>
              <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                <div>
                  <div style={{ fontWeight: 800, fontSize: 15 }}>Australia</div>
                  <div style={{ fontSize: 11, color: "#64748b" }}>AUS</div>
                </div>
                <span style={{ fontSize: 20 }}>{s.bowlingTeam.flag}</span>
              </div>
            </div>
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
            {loading && <span style={{ fontSize: 12, color: "#f59e0b", fontWeight: 600 }}>● Syncing…</span>}
            <div style={{ background: "#22c55e", color: "#fff", fontWeight: 700, fontSize: 12, padding: "4px 10px", borderRadius: 6, display: "flex", alignItems: "center", gap: 6 }}>
              <span style={{ width: 6, height: 6, borderRadius: "50%", background: "#fff", display: "inline-block" }} />
              LIVE &nbsp;00:45:32
            </div>
          </div>
        </header>

        {error && (
          <div style={{ background: "#7c2d12", color: "#fed7aa", padding: "8px 20px", fontSize: 13, fontWeight: 600 }}>
            ⚠ {error}
          </div>
        )}

        <div style={{ flex: 1, display: "flex", overflow: "hidden" }}>
          {/* ── CENTER PANEL ── */}
          <div style={{ flex: 1, overflowY: "auto", padding: 16, display: "flex", flexDirection: "column", gap: 12 }}>
            {/* Score header */}
            <div style={{ background: "#1a1d27", borderRadius: 10, padding: "16px 20px", display: "flex", alignItems: "center", justifyContent: "space-between", border: "1px solid #2d3148" }}>
              <div style={{ display: "flex", alignItems: "center", gap: 14 }}>
                <span style={{ fontSize: 28 }}>{s.battingTeam.flag}</span>
                <div>
                  <div style={{ fontWeight: 800, fontSize: 16 }}>{s.battingTeam.code}</div>
                </div>
                <div>
                  <div style={{ fontSize: 36, fontWeight: 900, color: "#fff" }}>{s.battingTeam.score}/{s.battingTeam.wickets}</div>
                  <div style={{ fontSize: 12, color: "#94a3b8" }}>{s.battingTeam.overs} OVERS</div>
                </div>
              </div>
              <div style={{ textAlign: "center" }}>
                <div style={{ background: "#2d3148", borderRadius: 8, padding: "6px 16px", fontWeight: 700, marginBottom: 4 }}>P1</div>
                <div style={{ fontSize: 12, color: "#94a3b8" }}>CRR {s.battingTeam.crr.toFixed(2)}</div>
              </div>
              <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                <div style={{ textAlign: "right", fontSize: 13, color: "#94a3b8" }}>
                  <div>TOSS: {s.match.toss.toUpperCase()}</div>
                  <div>ELECTED TO {s.match.electedTo.toUpperCase()}</div>
                </div>
                <span style={{ fontSize: 28 }}>{s.bowlingTeam.flag}</span>
                <div style={{ fontWeight: 800, fontSize: 16 }}>{s.bowlingTeam.code}</div>
              </div>
            </div>

            {/* Batsmen + Bowler */}
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12 }}>
              <div style={{ background: "#1a1d27", borderRadius: 10, border: "1px solid #2d3148", overflow: "hidden" }}>
                <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 13 }}>
                  <thead>
                    <tr style={{ borderBottom: "1px solid #2d3148" }}>
                      {["BATSMEN","R","B","4s","6s","SR"].map(h => (
                        <th key={h} style={{ padding: "10px 12px", textAlign: h === "BATSMEN" ? "left" : "center", color: "#64748b", fontWeight: 600, fontSize: 11 }}>{h}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {s.battingTeam.batsmen.map(b => (
                      <tr key={b.name} style={{ borderBottom: "1px solid #1e2235" }}>
                        <td style={{ padding: "10px 12px", display: "flex", alignItems: "center", gap: 6 }}>
                          {b.onStrike && <span style={{ width: 8, height: 8, borderRadius: "50%", background: "#22c55e", flexShrink: 0 }} />}
                          <span style={{ fontWeight: b.onStrike ? 700 : 500 }}>{b.name} {b.onStrike ? "*" : ""}</span>
                        </td>
                        <td style={{ textAlign: "center", fontWeight: 700 }}>{b.runs}</td>
                        <td style={{ textAlign: "center", color: "#94a3b8" }}>{b.balls}</td>
                        <td style={{ textAlign: "center", color: "#22c55e" }}>{b.fours}</td>
                        <td style={{ textAlign: "center", color: "#a855f7" }}>{b.sixes}</td>
                        <td style={{ textAlign: "center", color: "#64748b" }}>{b.sr.toFixed(2)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <div style={{ background: "#1a1d27", borderRadius: 10, border: "1px solid #2d3148", overflow: "hidden" }}>
                <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 13 }}>
                  <thead>
                    <tr style={{ borderBottom: "1px solid #2d3148" }}>
                      {["BOWLER","O","M","R","W","ECON"].map(h => (
                        <th key={h} style={{ padding: "10px 12px", textAlign: h === "BOWLER" ? "left" : "center", color: "#64748b", fontWeight: 600, fontSize: 11 }}>{h}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      <td style={{ padding: "10px 12px", fontWeight: 700 }}>{s.bowlingTeam.currentBowler.name}</td>
                      <td style={{ textAlign: "center" }}>{s.bowlingTeam.currentBowler.overs}</td>
                      <td style={{ textAlign: "center" }}>{s.bowlingTeam.currentBowler.maidens}</td>
                      <td style={{ textAlign: "center" }}>{s.bowlingTeam.currentBowler.runs}</td>
                      <td style={{ textAlign: "center", color: "#ef4444", fontWeight: 700 }}>{s.bowlingTeam.currentBowler.wickets}</td>
                      <td style={{ textAlign: "center" }}>{s.bowlingTeam.currentBowler.econ.toFixed(2)}</td>
                    </tr>
                  </tbody>
                </table>
                <div style={{ padding: "10px 12px", borderTop: "1px solid #2d3148" }}>
                  <div style={{ fontSize: 11, color: "#64748b", fontWeight: 600, marginBottom: 8 }}>THIS SPELL</div>
                  <div style={{ display: "flex", gap: 4, flexWrap: "wrap" }}>
                    {s.bowlingTeam.thisSpell.map((b, i) => (
                      <BallDot key={i} val={b} />
                    ))}
                  </div>
                </div>
              </div>
            </div>

            {/* Next Ball Controls */}
            <div style={{ background: "#1a1d27", borderRadius: 10, border: "1px solid #2d3148", padding: 16 }}>
              <div style={{ fontWeight: 700, fontSize: 14, marginBottom: 4 }}>NEXT BALL</div>
              <div style={{ fontSize: 12, color: "#64748b", marginBottom: 14 }}>Select Run or Event</div>
              <div style={{ display: "flex", gap: 10, marginBottom: 14, flexWrap: "wrap" }}>
                {[
                  { label: "0", sub: "Dot Ball", color: "#374151", fg: "#e2e8f0" },
                  { label: "1", sub: "Single", color: "#16a34a", fg: "#fff" },
                  { label: "2", sub: "Two", color: "#374151", fg: "#e2e8f0" },
                  { label: "3", sub: "Three", color: "#374151", fg: "#e2e8f0" },
                  { label: "4", sub: "Four", color: "#2563eb", fg: "#fff" },
                  { label: "6", sub: "Six", color: "#7c3aed", fg: "#fff" },
                ].map(btn => (
                  <button key={btn.label} onClick={() => sendBallEvent(btn.label)} disabled={loading} style={{
                    display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center",
                    width: 72, height: 72, borderRadius: "50%",
                    background: btn.color, color: btn.fg, border: "2px solid transparent",
                    fontWeight: 800, fontSize: 22, cursor: loading ? "not-allowed" : "pointer",
                    opacity: loading ? 0.6 : 1, transition: "transform 0.1s",
                  }} onMouseDown={e => e.currentTarget.style.transform = "scale(0.93)"}
                    onMouseUp={e => e.currentTarget.style.transform = "scale(1)"}>
                    {btn.label}
                    <span style={{ fontSize: 9, fontWeight: 500, marginTop: 2 }}>{btn.sub}</span>
                  </button>
                ))}
              </div>
              <div style={{ display: "flex", gap: 10, flexWrap: "wrap" }}>
                {[
                  { label: "W", sub: "Wide", color: "#d97706" },
                  { label: "NB", sub: "No Ball", color: "#dc2626" },
                  { label: "BY", sub: "Bye", color: "#b45309" },
                  { label: "LB", sub: "Leg Bye", color: "#b45309" },
                  { label: "🏏 WICKET", sub: "", color: "#7c2d12" },
                  { label: "• • •", sub: "MORE", color: "#374151" },
                ].map(btn => (
                  <button key={btn.label} onClick={() => sendBallEvent(btn.label)} disabled={loading} style={{
                    padding: "10px 18px", borderRadius: 8, border: "none",
                    background: btn.color, color: "#fff", fontWeight: 700, fontSize: 13,
                    cursor: loading ? "not-allowed" : "pointer", opacity: loading ? 0.6 : 1,
                    display: "flex", flexDirection: "column", alignItems: "center",
                  }}>
                    <span>{btn.label}</span>
                    {btn.sub && <span style={{ fontSize: 10, fontWeight: 500 }}>{btn.sub}</span>}
                  </button>
                ))}
              </div>
            </div>

            {/* Control Buttons */}
            <div style={{ display: "flex", gap: 10 }}>
              {[
                { label: "END OVER", color: "#1d4ed8", action: "end_over" },
                { label: "CHANGE BOWLER", color: "#7c3aed", action: "change_bowler" },
                { label: "TIME OUT", color: "#0891b2", action: "time_out" },
                { label: "NEXT INNINGS", color: "#15803d", action: "next_innings" },
              ].map(btn => (
                <button key={btn.label} onClick={() => sendAction(btn.action)} style={{
                  flex: 1, padding: "14px 10px", borderRadius: 8, border: "none",
                  background: btn.color, color: "#fff", fontWeight: 700, fontSize: 13,
                  cursor: "pointer", letterSpacing: 0.5,
                }}>{btn.label}</button>
              ))}
            </div>

            {/* Squads */}
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12 }}>
              {[
                { team: s.battingTeam.name.toUpperCase() + " SQUAD", squad: s.battingTeam.squad },
                { team: s.bowlingTeam.name.toUpperCase() + " SQUAD", squad: s.bowlingTeam.squad },
              ].map(({ team, squad }) => (
                <div key={team} style={{ background: "#1a1d27", borderRadius: 10, border: "1px solid #2d3148", padding: 14 }}>
                  <div style={{ fontWeight: 700, fontSize: 13, marginBottom: 10, color: "#94a3b8" }}>{team}</div>
                  <div style={{ fontSize: 11, color: "#64748b", marginBottom: 4 }}>Playing XI</div>
                  <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "3px 12px" }}>
                    {squad.playing.map((p, i) => (
                      <div key={p} style={{ fontSize: 12, color: "#e2e8f0", padding: "2px 0" }}>{i + 1}. {p}</div>
                    ))}
                  </div>
                  <div style={{ fontSize: 11, color: "#64748b", marginTop: 10, marginBottom: 4 }}>BENCH</div>
                  {squad.bench.map(p => <div key={p} style={{ fontSize: 12, color: "#94a3b8" }}>{p}</div>)}
                </div>
              ))}
            </div>
          </div>

          {/* ── RIGHT PANEL ── */}
          <aside style={{ width: 260, background: "#1a1d27", borderLeft: "1px solid #2d3148", display: "flex", flexDirection: "column", flexShrink: 0 }}>
            {/* Tabs */}
            <div style={{ display: "flex", borderBottom: "1px solid #2d3148" }}>
              {["OVERS", "RUNS", "WICKETS"].map(tab => (
                <button key={tab} onClick={() => setActiveTab(tab)} style={{
                  flex: 1, padding: "12px 8px", border: "none", cursor: "pointer",
                  background: "transparent", fontSize: 12, fontWeight: 700,
                  color: activeTab === tab ? "#2563eb" : "#64748b",
                  borderBottom: activeTab === tab ? "2px solid #2563eb" : "2px solid transparent",
                }}>{tab}</button>
              ))}
            </div>

            {/* Over history */}
            <div style={{ flex: 1, overflowY: "auto", padding: 12 }}>
              <div style={{ fontWeight: 700, fontSize: 13, marginBottom: 10 }}>{s.bowlingTeam.name}</div>
              {s.bowlingTeam.overHistory.map((ov, i) => (
                <div key={i} style={{ display: "flex", alignItems: "center", gap: 6, marginBottom: 8 }}>
                  <span style={{ width: 36, fontSize: 12, color: "#64748b", flexShrink: 0 }}>{ov.over}</span>
                  {ov.balls.map((b, j) => (
                    <span key={j} style={{
                      width: 24, height: 24, borderRadius: "50%",
                      display: "flex", alignItems: "center", justifyContent: "center",
                      background: b === "W" ? "#ef4444" : b === 4 ? "#22c55e" : b === 6 ? "#a855f7" : b === 0 ? "#2d3148" : "#374151",
                      color: "#fff", fontSize: 11, fontWeight: 700, flexShrink: 0,
                    }}>{b}</span>
                  ))}
                </div>
              ))}
              <button style={{ width: "100%", padding: "10px", borderRadius: 8, border: "1px solid #2d3148", background: "transparent", color: "#2563eb", fontWeight: 600, fontSize: 13, cursor: "pointer", marginTop: 8 }}>
                All Overs →
              </button>
            </div>

            {/* Partnership */}
            <div style={{ padding: 14, borderTop: "1px solid #2d3148" }}>
              <div style={{ fontWeight: 700, fontSize: 12, color: "#94a3b8", marginBottom: 8 }}>PARTNERSHIP</div>
              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 6 }}>
                <span style={{ fontSize: 12, color: "#64748b" }}>Due</span>
                <span style={{ fontWeight: 700 }}>{s.partnership.runs} ({s.partnership.balls})</span>
              </div>
              {s.battingTeam.batsmen.map(b => (
                <div key={b.name} style={{ display: "flex", justifyContent: "space-between", marginBottom: 4, fontSize: 13 }}>
                  <span>{b.name}</span>
                  <span style={{ fontWeight: 700 }}>{b.runs} ({b.balls})</span>
                </div>
              ))}
            </div>

            {/* Fall of Wickets */}
            <div style={{ padding: 14, borderTop: "1px solid #2d3148" }}>
              <div style={{ fontWeight: 700, fontSize: 12, color: "#94a3b8", marginBottom: 8 }}>FALL OF WICKETS</div>
              {s.battingTeam.fallOfWickets.map(fw => (
                <div key={fw.wicket} style={{ display: "flex", justifyContent: "space-between", marginBottom: 6, fontSize: 12 }}>
                  <span><strong>{fw.wicket}-{fw.score}</strong> <span style={{ color: "#64748b" }}>({fw.over})</span></span>
                  <span style={{ color: "#94a3b8" }}>{fw.batsman}</span>
                </div>
              ))}
            </div>

            {/* Match Info */}
            <div style={{ padding: 14, borderTop: "1px solid #2d3148", fontSize: 12 }}>
              <div style={{ fontWeight: 700, fontSize: 12, color: "#94a3b8", marginBottom: 8 }}>MATCH INFO</div>
              {[
                ["Venue", s.match.venue],
                ["Date", s.match.date],
                ["Start Time", s.match.startTime],
                ["Umpires", s.match.umpires],
                ["Third Umpire", s.match.thirdUmpire],
                ["Match Referee", s.match.matchReferee],
              ].map(([k, v]) => (
                <div key={k} style={{ display: "flex", gap: 8, marginBottom: 6 }}>
                  <span style={{ color: "#64748b", flexShrink: 0, minWidth: 90 }}>{k}</span>
                  <span style={{ color: "#e2e8f0" }}>{v}</span>
                </div>
              ))}
            </div>
          </aside>
        </div>
      </main>
    </div>
  );
}

// ── LOCAL MOCK UPDATER (for demo when backend is down) ─────────────────────
function localMockUpdate(prev, event) {
  const runs = parseInt(event);
  if (isNaN(runs)) return prev; // extras/wicket – skip for demo
  const s = JSON.parse(JSON.stringify(prev));
  s.battingTeam.score += runs;
  const striker = s.battingTeam.batsmen.find(b => b.onStrike);
  if (striker) {
    striker.runs += runs;
    striker.balls += 1;
    striker.sr = parseFloat(((striker.runs / striker.balls) * 100).toFixed(2));
    if (runs === 4) striker.fours += 1;
    if (runs === 6) striker.sixes += 1;
  }
  s.bowlingTeam.currentBowler.runs += runs;
  s.battingTeam.crr = parseFloat((s.battingTeam.score / (s.innings.currentOver + s.innings.currentBall / 6)).toFixed(2));
  s.innings.currentBall += 1;
  if (s.innings.currentBall > 6) { s.innings.currentBall = 1; s.innings.currentOver += 1; }
  s.battingTeam.overs = `${s.innings.currentOver}.${s.innings.currentBall}`;
  return s;
}
