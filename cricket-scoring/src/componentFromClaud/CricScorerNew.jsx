import { useState, useEffect, useCallback } from "react";

const API_BASE = "https://your-backend.com/api"; // ← replace with your backend URL

async function apiFetch(path, options = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  if (!res.ok) throw new Error(`API error ${res.status}`);
  return res.json();
}

const INIT = {
  matchId: 25,
  innings: 1,
  over: 10,
  ball: 3,
  score: 72,
  wickets: 3,
  crr: 7.2,
  rrr: 8.45,
  batsmen: [
    {
      name: "Virat Kohli",
      onStrike: true,
      runs: 34,
      balls: 28,
      fours: 4,
      sixes: 1,
    },
    {
      name: "Rohit Sharma",
      onStrike: false,
      runs: 22,
      balls: 18,
      fours: 3,
      sixes: 0,
    },
  ],
  bowler: {
    name: "Mitchell Starc",
    overs: "2.3",
    maidens: 0,
    runs: 18,
    wickets: 1,
    econ: "7.20",
  },
  fow: [
    { score: "1-12", over: "2.1", name: "Shubman Gill" },
    { score: "2-24", over: "3.6", name: "Shreyas Iyer" },
    { score: "3-38", over: "6.2", name: "KL Rahul" },
  ],
  // Full squad — batted players removed as innings progresses
  remainingBatters: [
    "Hardik Pandya",
    "Ravindra Jadeja",
    "Axar Patel",
    "Jasprit Bumrah",
    "Mohammed Siraj",
    "Arshdeep Singh",
  ],
  squadPlaying: [
    "Rohit Sharma (c)",
    "Shubman Gill",
    "Virat Kohli",
    "Shreyas Iyer",
    "KL Rahul (wk)",
    "Hardik Pandya",
    "Ravindra Jadeja",
    "Axar Patel",
    "Jasprit Bumrah",
    "Mohammed Siraj",
    "Arshdeep Singh",
  ],
  squadBench: ["Yuzvendra Chahal", "Ishan Kishan", "Sanju Samson"],
};

const SPELL = ["1", "•", "0", "•", "W", "•", "4", "•", "1", "•", "0", "•"];
const OVER_HISTORY = [
  { label: "10.3", balls: [1, 0, "W", 4, 1, 0] },
  { label: "10.2", balls: [0, 2, 1, 0, 1, 0] },
  { label: "10.1", balls: [4, 0, 0, 1, "W", 2] },
  { label: "9", balls: [1, 1, 4, 0, 2, 1] },
  { label: "8", balls: [0, 0, 1, 1, 4, 0] },
  { label: "7", balls: [2, 4, 0, 1, 0, 1] },
];

function ballBg(b) {
  if (b === "W") return { bg: "#e53e3e", color: "#fff" };
  if (b === 4) return { bg: "#38a169", color: "#fff" };
  if (b === 6) return { bg: "#805ad5", color: "#fff" };
  if (b === 0) return { bg: "#e2e8f0", color: "#4a5568" };
  return { bg: "#bee3f8", color: "#2c5282" };
}

function SpellItem({ val }) {
  if (val === "•")
    return <span style={{ color: "#a0aec0", fontSize: 14 }}>•</span>;
  const isW = val === "W",
    is4 = val === "4" || val === 4,
    is6 = val === "6" || val === 6;
  return (
    <span
      style={{
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        width: 24,
        height: 24,
        borderRadius: "50%",
        fontWeight: 700,
        fontSize: 11,
        background: isW
          ? "#e53e3e"
          : is4
            ? "#38a169"
            : is6
              ? "#805ad5"
              : "#e2e8f0",
        color: isW || is4 || is6 ? "#fff" : "#2d3748",
      }}
    >
      {val}
    </span>
  );
}

function OverBall({ val }) {
  const { bg, color } = ballBg(val);
  return (
    <span
      style={{
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        width: 21,
        height: 21,
        borderRadius: "50%",
        fontWeight: 700,
        fontSize: 10.5,
        background: bg,
        color,
      }}
    >
      {val}
    </span>
  );
}

function card(extra = {}) {
  return {
    background: "#fff",
    borderRadius: 7,
    border: "1px solid #e2e8f0",
    boxShadow: "0 1px 2px rgba(0,0,0,.05)",
    ...extra,
  };
}

const NAV = [
  { icon: "✏️", label: "Scoring" },
  { icon: "📋", label: "Scoreboard" },
  { icon: "💬", label: "Commentary" },
  { icon: "👤", label: "Players" },
  { icon: "🤝", label: "Partnerships" },
  { icon: "⚪", label: "Wagon Wheel" },
  { icon: "📊", label: "Manhattan" },
  { icon: "📄", label: "Reports" },
  { icon: "⚙️", label: "Settings" },
];

// ─── NEW BATTER MODAL ─────────────────────────────────────────────────────────
function NewBatterModal({ outBatsman, remainingBatters, onSelect, onCancel }) {
  const [selected, setSelected] = useState(null);
  const [search, setSearch] = useState("");

  const filtered = remainingBatters.filter((b) =>
    b.toLowerCase().includes(search.toLowerCase()),
  );

  return (
    <div
      style={{
        position: "fixed",
        inset: 0,
        background: "rgba(0,0,0,.55)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        zIndex: 999,
      }}
    >
      <div
        style={{
          background: "#fff",
          borderRadius: 12,
          width: 420,
          boxShadow: "0 20px 60px rgba(0,0,0,.3)",
          overflow: "hidden",
        }}
      >
        {/* Header */}
        <div
          style={{
            background: "#1a202c",
            padding: "16px 20px",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
          }}
        >
          <div>
            <div style={{ color: "#fff", fontWeight: 700, fontSize: 15 }}>
              New Batter
            </div>
            <div style={{ color: "#a0aec0", fontSize: 12, marginTop: 2 }}>
              {outBatsman} is out — select next batter
            </div>
          </div>
          <button
            onClick={onCancel}
            style={{
              background: "#2d3748",
              border: "none",
              color: "#a0aec0",
              width: 28,
              height: 28,
              borderRadius: "50%",
              cursor: "pointer",
              fontSize: 16,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
            }}
          >
            ✕
          </button>
        </div>

        {/* Search */}
        <div
          style={{ padding: "12px 20px", borderBottom: "1px solid #e2e8f0" }}
        >
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search player..."
            autoFocus
            style={{
              width: "100%",
              padding: "8px 12px",
              borderRadius: 6,
              border: "1px solid #cbd5e0",
              fontSize: 13,
              outline: "none",
              background: "#f7fafc",
            }}
          />
        </div>

        {/* Player list */}
        <div style={{ maxHeight: 280, overflowY: "auto", padding: "8px 12px" }}>
          {filtered.length === 0 && (
            <div
              style={{
                textAlign: "center",
                color: "#a0aec0",
                padding: "30px 0",
                fontSize: 13,
              }}
            >
              No players found
            </div>
          )}
          {filtered.map((player, i) => (
            <button
              key={player}
              onClick={() => setSelected(player)}
              style={{
                display: "flex",
                alignItems: "center",
                gap: 12,
                width: "100%",
                padding: "10px 12px",
                borderRadius: 7,
                border: "none",
                background: selected === player ? "#ebf8ff" : "transparent",
                cursor: "pointer",
                textAlign: "left",
                marginBottom: 2,
                transition: "background .15s",
              }}
            >
              {/* Avatar */}
              <div
                style={{
                  width: 36,
                  height: 36,
                  borderRadius: "50%",
                  flexShrink: 0,
                  background: selected === player ? "#3182ce" : "#e2e8f0",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  fontWeight: 700,
                  fontSize: 13,
                  color: selected === player ? "#fff" : "#4a5568",
                }}
              >
                {player
                  .split(" ")
                  .map((w) => w[0])
                  .join("")
                  .slice(0, 2)}
              </div>
              <div style={{ flex: 1 }}>
                <div
                  style={{
                    fontWeight: selected === player ? 700 : 500,
                    fontSize: 13,
                    color: "#2d3748",
                  }}
                >
                  {player}
                </div>
                <div style={{ fontSize: 11, color: "#718096" }}>
                  Batting position #{i + 5}
                </div>
              </div>
              {selected === player && (
                <span style={{ color: "#3182ce", fontSize: 18 }}>✓</span>
              )}
            </button>
          ))}
        </div>

        {/* Footer */}
        <div
          style={{
            padding: "12px 20px",
            borderTop: "1px solid #e2e8f0",
            display: "flex",
            gap: 10,
            justifyContent: "flex-end",
            background: "#f7fafc",
          }}
        >
          <button
            onClick={onCancel}
            style={{
              padding: "9px 18px",
              borderRadius: 6,
              border: "1px solid #e2e8f0",
              background: "#fff",
              color: "#4a5568",
              fontWeight: 600,
              fontSize: 13,
              cursor: "pointer",
            }}
          >
            Cancel
          </button>
          <button
            onClick={() => selected && onSelect(selected)}
            disabled={!selected}
            style={{
              padding: "9px 20px",
              borderRadius: 6,
              border: "none",
              background: selected ? "#3182ce" : "#a0aec0",
              color: "#fff",
              fontWeight: 700,
              fontSize: 13,
              cursor: selected ? "pointer" : "not-allowed",
            }}
          >
            Send In {selected ? `— ${selected.split(" ")[0]}` : ""}
          </button>
        </div>
      </div>
    </div>
  );
}

// ─── WICKET MODAL ─────────────────────────────────────────────────────────────
function WicketModal({ batsmen, onConfirm, onCancel }) {
  const [outBatsman, setOutBatsman] = useState(
    batsmen.find((b) => b.onStrike)?.name || batsmen[0]?.name,
  );
  const [wicketType, setWicketType] = useState("Bowled");

  const WICKET_TYPES = [
    "Bowled",
    "Caught",
    "LBW",
    "Run Out",
    "Stumped",
    "Hit Wicket",
    "Caught & Bowled",
    "Obstructing the Field",
  ];

  return (
    <div
      style={{
        position: "fixed",
        inset: 0,
        background: "rgba(0,0,0,.55)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        zIndex: 998,
      }}
    >
      <div
        style={{
          background: "#fff",
          borderRadius: 12,
          width: 400,
          boxShadow: "0 20px 60px rgba(0,0,0,.3)",
          overflow: "hidden",
        }}
      >
        <div
          style={{
            background: "#c53030",
            padding: "14px 20px",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
          }}
        >
          <div style={{ color: "#fff", fontWeight: 700, fontSize: 15 }}>
            🏏 Wicket!
          </div>
          <button
            onClick={onCancel}
            style={{
              background: "rgba(255,255,255,.2)",
              border: "none",
              color: "#fff",
              width: 28,
              height: 28,
              borderRadius: "50%",
              cursor: "pointer",
              fontSize: 15,
            }}
          >
            ✕
          </button>
        </div>

        <div
          style={{
            padding: "16px 20px",
            display: "flex",
            flexDirection: "column",
            gap: 14,
          }}
        >
          {/* Who's out */}
          <div>
            <div
              style={{
                fontSize: 11,
                fontWeight: 600,
                color: "#718096",
                marginBottom: 6,
              }}
            >
              BATTER OUT
            </div>
            <div style={{ display: "flex", gap: 8 }}>
              {batsmen.map((b) => (
                <button
                  key={b.name}
                  onClick={() => setOutBatsman(b.name)}
                  style={{
                    flex: 1,
                    padding: "9px 10px",
                    borderRadius: 7,
                    cursor: "pointer",
                    fontWeight: 600,
                    fontSize: 13,
                    border: `2px solid ${outBatsman === b.name ? "#e53e3e" : "#e2e8f0"}`,
                    background: outBatsman === b.name ? "#fff5f5" : "#f7fafc",
                    color: outBatsman === b.name ? "#c53030" : "#4a5568",
                  }}
                >
                  {b.name}
                  {b.onStrike && (
                    <span
                      style={{ fontSize: 10, color: "#718096", marginLeft: 4 }}
                    >
                      (striker)
                    </span>
                  )}
                </button>
              ))}
            </div>
          </div>

          {/* Wicket type */}
          <div>
            <div
              style={{
                fontSize: 11,
                fontWeight: 600,
                color: "#718096",
                marginBottom: 6,
              }}
            >
              DISMISSAL TYPE
            </div>
            <div style={{ display: "flex", flexWrap: "wrap", gap: 6 }}>
              {WICKET_TYPES.map((wt) => (
                <button
                  key={wt}
                  onClick={() => setWicketType(wt)}
                  style={{
                    padding: "6px 11px",
                    borderRadius: 5,
                    cursor: "pointer",
                    fontWeight: 600,
                    fontSize: 12,
                    border: `1px solid ${wicketType === wt ? "#e53e3e" : "#e2e8f0"}`,
                    background: wicketType === wt ? "#fff5f5" : "#f7fafc",
                    color: wicketType === wt ? "#c53030" : "#4a5568",
                  }}
                >
                  {wt}
                </button>
              ))}
            </div>
          </div>
        </div>

        <div
          style={{
            padding: "12px 20px",
            borderTop: "1px solid #e2e8f0",
            display: "flex",
            gap: 10,
            justifyContent: "flex-end",
            background: "#f7fafc",
          }}
        >
          <button
            onClick={onCancel}
            style={{
              padding: "9px 18px",
              borderRadius: 6,
              border: "1px solid #e2e8f0",
              background: "#fff",
              color: "#4a5568",
              fontWeight: 600,
              fontSize: 13,
              cursor: "pointer",
            }}
          >
            Cancel
          </button>
          <button
            onClick={() => onConfirm({ outBatsman, wicketType })}
            style={{
              padding: "9px 20px",
              borderRadius: 6,
              border: "none",
              background: "#c53030",
              color: "#fff",
              fontWeight: 700,
              fontSize: 13,
              cursor: "pointer",
            }}
          >
            Confirm Wicket
          </button>
        </div>
      </div>
    </div>
  );
}

// ─── MAIN ─────────────────────────────────────────────────────────────────────
export default function CricScorer() {
  const [s, setS] = useState(INIT);
  const [activeNav, setActiveNav] = useState("Scoring");
  const [activeTab, setActiveTab] = useState("OVERS");
  const [toast, setToast] = useState(null);
  // modal state
  const [showWicket, setShowWicket] = useState(false);
  const [showNewBatter, setShowNewBatter] = useState(false);
  const [pendingWicket, setPendingWicket] = useState(null); // { outBatsman, wicketType }

  const showToast = (msg) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  };

  const fetchState = useCallback(async () => {
    try {
      const data = await apiFetch(`/match/${s.matchId}/state`);
      setS((prev) => ({ ...prev, ...data }));
    } catch {}
  }, [s.matchId]);

  useEffect(() => {
    fetchState();
    const t = setInterval(fetchState, 5000);
    return () => clearInterval(t);
  }, [fetchState]);

  // Called after wicket confirmed + new batter selected
  const handleNewBatterSelected = async (newBatterName) => {
    const { outBatsman, wicketType } = pendingWicket;
    const payload = {
      matchId: s.matchId,
      innings: s.innings,
      over: s.over,
      ball: s.ball,
      event: "WICKET",
      wicketType,
      outBatsman,
      newBatter: newBatterName,
      bowler: s.bowler.name,
      timestamp: new Date().toISOString(),
    };
    try {
      const data = await apiFetch(`/match/${s.matchId}/ball`, {
        method: "POST",
        body: JSON.stringify(payload),
      });
      setS((prev) => ({ ...prev, ...data }));
    } catch {
      showToast("Demo mode – backend not connected");
      // Local update
      setS((prev) => {
        const newBatsmen = prev.batsmen.map((b) =>
          b.name === outBatsman
            ? {
                name: newBatterName,
                onStrike: b.onStrike,
                runs: 0,
                balls: 0,
                fours: 0,
                sixes: 0,
              }
            : b,
        );
        const newFow = [
          ...prev.fow,
          {
            score: `${prev.wickets + 1}-${prev.score}`,
            over: `${prev.over}.${prev.ball}`,
            name: outBatsman,
          },
        ];
        const newRemaining = prev.remainingBatters.filter(
          (b) => b !== newBatterName,
        );
        const newBall = prev.ball >= 6 ? 1 : prev.ball + 1;
        const newOver = prev.ball >= 6 ? prev.over + 1 : prev.over;
        return {
          ...prev,
          wickets: prev.wickets + 1,
          batsmen: newBatsmen,
          fow: newFow,
          remainingBatters: newRemaining,
          over: newOver,
          ball: newBall,
        };
      });
    }
    setShowNewBatter(false);
    setPendingWicket(null);
  };

  const handleWicketConfirmed = ({ outBatsman, wicketType }) => {
    setPendingWicket({ outBatsman, wicketType });
    setShowWicket(false);
    setShowNewBatter(true);
  };

  const onBall = async (event) => {
    if (event === "WICKET") {
      setShowWicket(true);
      return;
    }

    const payload = {
      matchId: s.matchId,
      innings: s.innings,
      over: s.over,
      ball: s.ball,
      event,
      striker: s.batsmen.find((b) => b.onStrike)?.name,
      bowler: s.bowler.name,
      timestamp: new Date().toISOString(),
    };
    try {
      const data = await apiFetch(`/match/${s.matchId}/ball`, {
        method: "POST",
        body: JSON.stringify(payload),
      });
      setS((prev) => ({ ...prev, ...data }));
    } catch {
      showToast("Demo mode – backend not connected");
      const runs = parseInt(event);
      if (!isNaN(runs)) {
        setS((prev) => {
          const newBall = prev.ball >= 6 ? 1 : prev.ball + 1;
          const newOver = prev.ball >= 6 ? prev.over + 1 : prev.over;
          const newScore = prev.score + runs;
          const newCrr = parseFloat(
            (newScore / (newOver + newBall / 6)).toFixed(2),
          );
          const newBatsmen = prev.batsmen.map((b) =>
            b.onStrike
              ? {
                  ...b,
                  runs: b.runs + runs,
                  balls: b.balls + 1,
                  fours: b.fours + (runs === 4 ? 1 : 0),
                  sixes: b.sixes + (runs === 6 ? 1 : 0),
                }
              : b,
          );
          return {
            ...prev,
            score: newScore,
            crr: newCrr,
            over: newOver,
            ball: newBall,
            batsmen: newBatsmen,
            bowler: { ...prev.bowler, runs: prev.bowler.runs + runs },
          };
        });
      }
    }
  };

  const onAction = async (action) => {
    try {
      const data = await apiFetch(`/match/${s.matchId}/action`, {
        method: "POST",
        body: JSON.stringify({ action, matchId: s.matchId }),
      });
      setS((prev) => ({ ...prev, ...data }));
    } catch {
      showToast(`"${action}" – backend not connected`);
    }
  };

  const oversStr = `${s.over}.${s.ball}`;
  const striker = s.batsmen.find((b) => b.onStrike);
  const nonStriker = s.batsmen.find((b) => !b.onStrike);

  return (
    <div
      style={{
        display: "flex",
        height: "100vh",
        fontFamily: "'Segoe UI',system-ui,sans-serif",
        fontSize: 13,
        background: "#f0f4f8",
        color: "#2d3748",
        overflow: "hidden",
      }}
    >
      {/* WICKET MODAL */}
      {showWicket && (
        <WicketModal
          batsmen={s.batsmen}
          onConfirm={handleWicketConfirmed}
          onCancel={() => setShowWicket(false)}
        />
      )}

      {/* NEW BATTER MODAL */}
      {showNewBatter && pendingWicket && (
        <NewBatterModal
          outBatsman={pendingWicket.outBatsman}
          remainingBatters={s.remainingBatters}
          onSelect={handleNewBatterSelected}
          onCancel={() => {
            setShowNewBatter(false);
            setPendingWicket(null);
          }}
        />
      )}

      {/* SIDEBAR */}
      <aside
        style={{
          width: 190,
          background: "#1a202c",
          color: "#a0aec0",
          display: "flex",
          flexDirection: "column",
          flexShrink: 0,
        }}
      >
        <div
          style={{
            padding: "13px 14px",
            display: "flex",
            borderBottom: "1px solid #2d3748",
          }}
        >
          <span
            style={{
              background: "#fff",
              color: "#1a202c",
              fontWeight: 900,
              fontSize: 13,
              padding: "3px 6px",
            }}
          >
            CRIC
          </span>
          <span
            style={{
              background: "#e53e3e",
              color: "#fff",
              fontWeight: 900,
              fontSize: 11,
              padding: "3px 6px",
            }}
          >
            SCORER
          </span>
        </div>
        <nav>
          {NAV.map(({ icon, label }) => (
            <button
              key={label}
              onClick={() => setActiveNav(label)}
              style={{
                display: "flex",
                alignItems: "center",
                gap: 9,
                width: "100%",
                padding: "10px 14px",
                border: "none",
                background: activeNav === label ? "#2b4c7e" : "transparent",
                color: activeNav === label ? "#fff" : "#a0aec0",
                fontSize: 12.5,
                fontWeight: activeNav === label ? 600 : 400,
                cursor: "pointer",
                textAlign: "left",
                borderLeft:
                  activeNav === label
                    ? "3px solid #63b3ed"
                    : "3px solid transparent",
              }}
            >
              <span style={{ fontSize: 13 }}>{icon}</span>
              {label}
            </button>
          ))}
        </nav>
        <div
          style={{
            padding: "12px 14px",
            borderTop: "1px solid #2d3748",
            fontSize: 11,
            marginTop: "auto",
          }}
        >
          <div style={{ color: "#718096", marginBottom: 2 }}>
            Over in Progress
          </div>
          <div
            style={{
              display: "flex",
              alignItems: "center",
              gap: 8,
              marginBottom: 10,
            }}
          >
            <span style={{ fontSize: 22, fontWeight: 800, color: "#fff" }}>
              {oversStr}
            </span>
            <span
              style={{
                background: "#2b6cb0",
                color: "#90cdf4",
                fontSize: 10,
                fontWeight: 700,
                padding: "2px 6px",
                borderRadius: 3,
              }}
            >
              P1
            </span>
          </div>
          <div style={{ color: "#718096", marginBottom: 2 }}>
            Current Run Rate
          </div>
          <div
            style={{
              fontSize: 20,
              fontWeight: 800,
              color: "#fff",
              marginBottom: 10,
            }}
          >
            {s.crr.toFixed(2)}
          </div>
          <div style={{ color: "#718096", marginBottom: 2 }}>
            Required Run Rate
          </div>
          <div style={{ fontSize: 20, fontWeight: 800, color: "#fc8181" }}>
            {s.rrr.toFixed(2)}
          </div>
        </div>
      </aside>

      {/* MAIN */}
      <div
        style={{
          flex: 1,
          display: "flex",
          flexDirection: "column",
          overflow: "hidden",
        }}
      >
        {/* TOP BAR */}
        <header
          style={{
            background: "#1a202c",
            color: "#fff",
            padding: "8px 16px",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            flexShrink: 0,
          }}
        >
          <div
            style={{
              background: "#2d3748",
              border: "1px solid #4a5568",
              borderRadius: 5,
              padding: "5px 10px",
              display: "flex",
              alignItems: "center",
              gap: 6,
              cursor: "pointer",
            }}
          >
            <span style={{ fontWeight: 600, fontSize: 12 }}>
              Match {s.matchId}
            </span>
            <span style={{ fontSize: 9, color: "#a0aec0" }}>▼</span>
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: 14 }}>
            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <span style={{ fontSize: 22 }}>🇮🇳</span>
              <div>
                <div style={{ fontWeight: 700, fontSize: 14 }}>India</div>
                <div style={{ fontSize: 11, color: "#a0aec0" }}>IND</div>
              </div>
            </div>
            <span style={{ color: "#718096", fontWeight: 700 }}>vs</span>
            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <div>
                <div style={{ fontWeight: 700, fontSize: 14 }}>Australia</div>
                <div style={{ fontSize: 11, color: "#a0aec0" }}>AUS</div>
              </div>
              <span style={{ fontSize: 22 }}>🇦🇺</span>
            </div>
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <div
              style={{
                background: "#276749",
                color: "#9ae6b4",
                fontWeight: 700,
                fontSize: 11,
                padding: "4px 10px",
                borderRadius: 4,
                display: "flex",
                alignItems: "center",
                gap: 5,
              }}
            >
              <span
                style={{
                  width: 6,
                  height: 6,
                  borderRadius: "50%",
                  background: "#9ae6b4",
                  display: "inline-block",
                  animation: "blink 1s infinite",
                }}
              />
              LIVE &nbsp;00:45:32
            </div>
            <span style={{ color: "#718096", fontSize: 15, cursor: "pointer" }}>
              ⚙
            </span>
          </div>
        </header>

        {toast && (
          <div
            style={{
              background: "#d69e2e",
              color: "#fff",
              padding: "6px 16px",
              fontSize: 11,
              fontWeight: 600,
            }}
          >
            ⚠ {toast}
          </div>
        )}

        {/* BODY */}
        <div style={{ flex: 1, display: "flex", overflow: "hidden" }}>
          {/* CENTER */}
          <div
            style={{
              flex: 1,
              overflowY: "auto",
              padding: 12,
              display: "flex",
              flexDirection: "column",
              gap: 10,
            }}
          >
            {/* Score Strip */}
            <div
              style={card({
                padding: "13px 18px",
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
              })}
            >
              <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                <span style={{ fontSize: 28 }}>🇮🇳</span>
                <span style={{ fontWeight: 700, fontSize: 15 }}>IND</span>
                <div>
                  <div
                    style={{
                      fontSize: 32,
                      fontWeight: 900,
                      color: "#1a202c",
                      lineHeight: 1,
                    }}
                  >
                    {s.score}/{s.wickets}
                  </div>
                  <div style={{ fontSize: 11, color: "#718096", marginTop: 3 }}>
                    {oversStr} OVERS
                  </div>
                </div>
              </div>
              <div style={{ textAlign: "center" }}>
                <div
                  style={{
                    background: "#ebf8ff",
                    border: "1px solid #bee3f8",
                    borderRadius: 5,
                    padding: "4px 12px",
                    fontWeight: 700,
                    color: "#2b6cb0",
                    fontSize: 12,
                    marginBottom: 4,
                  }}
                >
                  P1
                </div>
                <div style={{ fontSize: 11, color: "#718096" }}>
                  CRR {s.crr.toFixed(2)}
                </div>
              </div>
              <div
                style={{
                  fontSize: 11,
                  color: "#718096",
                  textAlign: "center",
                  lineHeight: 1.8,
                }}
              >
                <div>
                  TOSS: <strong style={{ color: "#2d3748" }}>INDIA</strong>
                </div>
                <div>
                  ELECTED TO <strong style={{ color: "#2d3748" }}>BAT</strong>
                </div>
              </div>
              <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                <span style={{ fontWeight: 700, fontSize: 15 }}>AUS</span>
                <span style={{ fontSize: 28 }}>🇦🇺</span>
              </div>
            </div>

            {/* Batsmen + Bowler */}
            <div
              style={{
                display: "grid",
                gridTemplateColumns: "1fr 1fr",
                gap: 10,
              }}
            >
              <div style={card()}>
                <table style={{ width: "100%", borderCollapse: "collapse" }}>
                  <thead>
                    <tr style={{ background: "#f7fafc" }}>
                      {["BATSMEN", "R", "B", "4s", "6s", "SR"].map((h) => (
                        <th
                          key={h}
                          style={{
                            padding: "7px 10px",
                            textAlign: h === "BATSMEN" ? "left" : "center",
                            fontSize: 11,
                            fontWeight: 600,
                            color: "#718096",
                            borderBottom: "1px solid #e2e8f0",
                          }}
                        >
                          {h}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {s.batsmen.map((b, i) => (
                      <tr
                        key={b.name}
                        style={{
                          borderBottom:
                            i < s.batsmen.length - 1
                              ? "1px solid #f0f4f8"
                              : "none",
                        }}
                      >
                        <td
                          style={{
                            padding: "9px 10px",
                            display: "flex",
                            alignItems: "center",
                            gap: 5,
                          }}
                        >
                          {b.onStrike && (
                            <span
                              style={{
                                width: 8,
                                height: 8,
                                borderRadius: "50%",
                                background: "#38a169",
                                display: "inline-block",
                                flexShrink: 0,
                              }}
                            />
                          )}
                          <span style={{ fontWeight: b.onStrike ? 700 : 500 }}>
                            {b.name}
                            {b.onStrike ? " *" : ""}
                          </span>
                        </td>
                        <td
                          style={{
                            textAlign: "center",
                            fontWeight: 700,
                            padding: "9px 10px",
                          }}
                        >
                          {b.runs}
                        </td>
                        <td
                          style={{
                            textAlign: "center",
                            color: "#718096",
                            padding: "9px 10px",
                          }}
                        >
                          {b.balls}
                        </td>
                        <td
                          style={{ textAlign: "center", padding: "9px 10px" }}
                        >
                          {b.fours}
                        </td>
                        <td
                          style={{ textAlign: "center", padding: "9px 10px" }}
                        >
                          {b.sixes}
                        </td>
                        <td
                          style={{
                            textAlign: "center",
                            color: "#718096",
                            padding: "9px 10px",
                          }}
                        >
                          {b.balls > 0
                            ? ((b.runs / b.balls) * 100).toFixed(2)
                            : "0.00"}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <div style={card()}>
                <table style={{ width: "100%", borderCollapse: "collapse" }}>
                  <thead>
                    <tr style={{ background: "#f7fafc" }}>
                      {["BOWLER", "O", "M", "R", "W", "ECON"].map((h) => (
                        <th
                          key={h}
                          style={{
                            padding: "7px 10px",
                            textAlign: h === "BOWLER" ? "left" : "center",
                            fontSize: 11,
                            fontWeight: 600,
                            color: "#718096",
                            borderBottom: "1px solid #e2e8f0",
                          }}
                        >
                          {h}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      <td style={{ padding: "9px 10px", fontWeight: 600 }}>
                        {s.bowler.name}
                      </td>
                      <td style={{ textAlign: "center", padding: "9px 10px" }}>
                        {s.bowler.overs}
                      </td>
                      <td style={{ textAlign: "center", padding: "9px 10px" }}>
                        {s.bowler.maidens}
                      </td>
                      <td style={{ textAlign: "center", padding: "9px 10px" }}>
                        {s.bowler.runs}
                      </td>
                      <td
                        style={{
                          textAlign: "center",
                          fontWeight: 700,
                          color: "#e53e3e",
                          padding: "9px 10px",
                        }}
                      >
                        {s.bowler.wickets}
                      </td>
                      <td style={{ textAlign: "center", padding: "9px 10px" }}>
                        {s.bowler.econ}
                      </td>
                    </tr>
                  </tbody>
                </table>
                <div
                  style={{
                    padding: "9px 12px",
                    borderTop: "1px solid #e2e8f0",
                  }}
                >
                  <div
                    style={{
                      fontSize: 10,
                      fontWeight: 600,
                      color: "#718096",
                      marginBottom: 6,
                    }}
                  >
                    THIS SPELL
                  </div>
                  <div
                    style={{ display: "flex", alignItems: "center", gap: 4 }}
                  >
                    {SPELL.map((v, i) => (
                      <SpellItem key={i} val={v} />
                    ))}
                  </div>
                </div>
              </div>
            </div>

            {/* Next Ball + Ball Info */}
            <div
              style={{
                display: "grid",
                gridTemplateColumns: "1fr auto",
                gap: 10,
              }}
            >
              <div style={card({ padding: "14px 15px" })}>
                <div
                  style={{ fontWeight: 700, fontSize: 13.5, marginBottom: 2 }}
                >
                  NEXT BALL
                </div>
                <div
                  style={{ fontSize: 11, color: "#718096", marginBottom: 13 }}
                >
                  Select Run or Event
                </div>
                <div
                  style={{
                    display: "flex",
                    gap: 10,
                    marginBottom: 14,
                    flexWrap: "wrap",
                  }}
                >
                  {[
                    {
                      v: "0",
                      sub: "Dot Ball",
                      bg: "#fff",
                      border: "#cbd5e0",
                      color: "#4a5568",
                      subColor: "#718096",
                    },
                    {
                      v: "1",
                      sub: "Single",
                      bg: "#38a169",
                      border: "#38a169",
                      color: "#fff",
                      subColor: "#c6f6d5",
                    },
                    {
                      v: "2",
                      sub: "Two",
                      bg: "#fff",
                      border: "#cbd5e0",
                      color: "#4a5568",
                      subColor: "#718096",
                    },
                    {
                      v: "3",
                      sub: "Three",
                      bg: "#fff",
                      border: "#cbd5e0",
                      color: "#4a5568",
                      subColor: "#718096",
                    },
                    {
                      v: "4",
                      sub: "Four",
                      bg: "#3182ce",
                      border: "#3182ce",
                      color: "#fff",
                      subColor: "#bee3f8",
                    },
                    {
                      v: "6",
                      sub: "Six",
                      bg: "#805ad5",
                      border: "#805ad5",
                      color: "#fff",
                      subColor: "#e9d8fd",
                    },
                  ].map((b) => (
                    <button
                      key={b.v}
                      onClick={() => onBall(b.v)}
                      style={{
                        width: 60,
                        height: 60,
                        borderRadius: "50%",
                        border: `2px solid ${b.border}`,
                        background: b.bg,
                        color: b.color,
                        fontWeight: 800,
                        fontSize: 19,
                        cursor: "pointer",
                        display: "flex",
                        flexDirection: "column",
                        alignItems: "center",
                        justifyContent: "center",
                        boxShadow: "0 1px 3px rgba(0,0,0,.1)",
                      }}
                    >
                      <span>{b.v}</span>
                      <span
                        style={{
                          fontSize: 8.5,
                          fontWeight: 500,
                          color: b.subColor,
                          marginTop: 1,
                        }}
                      >
                        {b.sub}
                      </span>
                    </button>
                  ))}
                </div>
                <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
                  {[
                    {
                      v: "W",
                      label: "Wide",
                      bg: "#fefcbf",
                      border: "#f6e05e",
                      color: "#744210",
                    },
                    {
                      v: "NB",
                      label: "No Ball",
                      bg: "#fed7d7",
                      border: "#fc8181",
                      color: "#742a2a",
                    },
                    {
                      v: "BY",
                      label: "Bye",
                      bg: "#fefcbf",
                      border: "#f6e05e",
                      color: "#744210",
                    },
                    {
                      v: "LB",
                      label: "Leg Bye",
                      bg: "#fefcbf",
                      border: "#f6e05e",
                      color: "#744210",
                    },
                    {
                      v: "WICKET",
                      label: "",
                      bg: "#fff5f5",
                      border: "#fc8181",
                      color: "#c53030",
                      icon: "🏏",
                    },
                    {
                      v: "MORE",
                      label: "",
                      bg: "#f7fafc",
                      border: "#e2e8f0",
                      color: "#4a5568",
                      icon: "···",
                    },
                  ].map((b) => (
                    <button
                      key={b.v}
                      onClick={() => onBall(b.v)}
                      style={{
                        padding: "8px 13px",
                        borderRadius: 6,
                        border: `1px solid ${b.border}`,
                        background: b.bg,
                        color: b.color,
                        fontWeight: 700,
                        fontSize: 12,
                        cursor: "pointer",
                        display: "flex",
                        flexDirection: "column",
                        alignItems: "center",
                        gap: 1,
                      }}
                    >
                      {b.icon && (
                        <span style={{ fontSize: b.icon === "🏏" ? 14 : 13 }}>
                          {b.icon}
                        </span>
                      )}
                      <span>{b.v}</span>
                      {b.label && (
                        <span style={{ fontSize: 9, fontWeight: 500 }}>
                          {b.label}
                        </span>
                      )}
                    </button>
                  ))}
                </div>
              </div>

              <div style={card({ padding: "13px 15px", minWidth: 195 })}>
                <div
                  style={{
                    fontWeight: 700,
                    fontSize: 11,
                    color: "#718096",
                    textTransform: "uppercase",
                    marginBottom: 10,
                  }}
                >
                  Ball Info
                </div>
                {[
                  ["Over", s.over],
                  ["Ball", s.ball],
                  ["Innings", "1st Innings"],
                  ["Powerplay", "P1 (1 - 6)"],
                  ["Field", "Day"],
                  ["Pitch", "Good"],
                ].map(([k, v]) => (
                  <div
                    key={k}
                    style={{
                      display: "flex",
                      justifyContent: "space-between",
                      marginBottom: 7,
                      fontSize: 12.5,
                    }}
                  >
                    <span style={{ color: "#718096" }}>{k}</span>
                    <span style={{ fontWeight: 600, color: "#2d3748" }}>
                      {v}
                    </span>
                  </div>
                ))}
              </div>
            </div>

            {/* Control Buttons */}
            <div style={{ display: "flex", gap: 9 }}>
              {[
                { label: "END OVER", action: "end_over", bg: "#2b6cb0" },
                {
                  label: "CHANGE BOWLER",
                  action: "change_bowler",
                  bg: "#6b46c1",
                },
                { label: "TIME OUT", action: "time_out", bg: "#0987a0" },
                {
                  label: "NEXT INNINGS",
                  action: "next_innings",
                  bg: "#276749",
                },
              ].map((b) => (
                <button
                  key={b.label}
                  onClick={() => onAction(b.action)}
                  style={{
                    flex: 1,
                    padding: "12px 8px",
                    borderRadius: 6,
                    border: "none",
                    background: b.bg,
                    color: "#fff",
                    fontWeight: 700,
                    fontSize: 12,
                    cursor: "pointer",
                  }}
                >
                  {b.label}
                </button>
              ))}
            </div>

            {/* Squads + Match Info */}
            <div
              style={{
                display: "grid",
                gridTemplateColumns: "1fr 1fr 1fr",
                gap: 10,
              }}
            >
              <SquadCard
                title="INDIA SQUAD"
                playing={s.squadPlaying}
                bench={s.squadBench}
                remainingBatters={s.remainingBatters}
              />
              <SquadCard
                title="AUSTRALIA SQUAD"
                playing={[
                  "David Warner",
                  "Travis Head",
                  "Marnus Labuschagne",
                  "Steve Smith",
                  "Glenn Maxwell",
                  "Marcus Stoinis",
                  "Alex Carey (wk)",
                  "Pat Cummins (c)",
                  "Mitchell Starc",
                  "Josh Hazlewood",
                  "Adam Zampa",
                ]}
                bench={["Mitchell Marsh", "Cameron Green", "Matthew Wade"]}
              />
              <div style={card({ padding: 13 })}>
                <div
                  style={{
                    fontWeight: 700,
                    fontSize: 11,
                    color: "#718096",
                    textTransform: "uppercase",
                    marginBottom: 10,
                  }}
                >
                  Match Info
                </div>
                {[
                  ["Venue", "Wankhede Stadium, Mumbai"],
                  ["Date", "25 Apr 2026"],
                  ["Start Time", "7:30 PM IST"],
                  ["Umpires", "Nitin Menon, Kumar Dharmasena"],
                  ["Third Umpire", "Paul Reiffel"],
                  ["Match Referee", "Andy Pycroft"],
                ].map(([k, v]) => (
                  <div key={k} style={{ marginBottom: 7 }}>
                    <div style={{ color: "#718096", fontSize: 10.5 }}>{k}</div>
                    <div
                      style={{
                        fontWeight: 500,
                        fontSize: 12,
                        color: "#2d3748",
                      }}
                    >
                      {v}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* RIGHT PANEL */}
          <aside
            style={{
              width: 265,
              background: "#fff",
              borderLeft: "1px solid #e2e8f0",
              display: "flex",
              flexDirection: "column",
              flexShrink: 0,
            }}
          >
            <div style={{ display: "flex", borderBottom: "2px solid #e2e8f0" }}>
              {["OVERS", "RUNS", "WICKETS"].map((tab) => (
                <button
                  key={tab}
                  onClick={() => setActiveTab(tab)}
                  style={{
                    flex: 1,
                    padding: "11px 5px",
                    border: "none",
                    background: "transparent",
                    fontSize: 12,
                    fontWeight: 700,
                    cursor: "pointer",
                    color: activeTab === tab ? "#3182ce" : "#718096",
                    borderBottom:
                      activeTab === tab
                        ? "2px solid #3182ce"
                        : "2px solid transparent",
                    marginBottom: -2,
                  }}
                >
                  {tab}
                </button>
              ))}
            </div>
            <div
              style={{
                padding: "11px 13px",
                borderBottom: "1px solid #e2e8f0",
                fontWeight: 700,
                fontSize: 13.5,
              }}
            >
              Australia
            </div>
            <div style={{ flex: 1, overflowY: "auto", padding: "10px 13px" }}>
              {OVER_HISTORY.map((ov, i) => (
                <div
                  key={i}
                  style={{
                    display: "flex",
                    alignItems: "center",
                    gap: 5,
                    marginBottom: 9,
                  }}
                >
                  <span
                    style={{
                      width: 32,
                      fontSize: 11.5,
                      color: "#718096",
                      flexShrink: 0,
                    }}
                  >
                    {ov.label}
                  </span>
                  <div style={{ display: "flex", gap: 3 }}>
                    {ov.balls.map((b, j) => (
                      <OverBall key={j} val={b} />
                    ))}
                  </div>
                </div>
              ))}
              <button
                style={{
                  width: "100%",
                  padding: 8,
                  borderRadius: 6,
                  border: "1px solid #e2e8f0",
                  background: "#f7fafc",
                  color: "#3182ce",
                  fontWeight: 600,
                  fontSize: 12,
                  cursor: "pointer",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  gap: 5,
                  marginTop: 4,
                }}
              >
                All Overs <span>›</span>
              </button>
            </div>
            <div
              style={{ padding: "11px 13px", borderTop: "1px solid #e2e8f0" }}
            >
              <div
                style={{
                  fontWeight: 700,
                  fontSize: 11,
                  color: "#718096",
                  textTransform: "uppercase",
                  marginBottom: 8,
                }}
              >
                Partnership
              </div>
              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  fontSize: 12.5,
                  marginBottom: 5,
                }}
              >
                <span style={{ color: "#718096" }}>Due</span>
                <span style={{ fontWeight: 700 }}>
                  {s.batsmen.reduce((a, b) => a + b.runs, 0)} (
                  {s.batsmen.reduce((a, b) => a + b.balls, 0)})
                </span>
              </div>
              {s.batsmen.map((b) => (
                <div
                  key={b.name}
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    fontSize: 12.5,
                    marginBottom: 4,
                  }}
                >
                  <span>{b.name}</span>
                  <span style={{ fontWeight: 600 }}>
                    {b.runs} ({b.balls})
                  </span>
                </div>
              ))}
            </div>
            <div
              style={{ padding: "11px 13px", borderTop: "1px solid #e2e8f0" }}
            >
              <div
                style={{
                  fontWeight: 700,
                  fontSize: 11,
                  color: "#718096",
                  textTransform: "uppercase",
                  marginBottom: 8,
                }}
              >
                Fall of Wickets
              </div>
              {s.fow.map((fw) => (
                <div
                  key={fw.score}
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    fontSize: 12,
                    marginBottom: 5,
                  }}
                >
                  <span>
                    <strong>{fw.score}</strong>
                    <span
                      style={{ color: "#718096", fontSize: 11, marginLeft: 3 }}
                    >
                      ({fw.over})
                    </span>
                  </span>
                  <span style={{ color: "#718096" }}>{fw.name}</span>
                </div>
              ))}
            </div>
          </aside>
        </div>
      </div>

      <style>{`
        @keyframes blink{0%,100%{opacity:1}50%{opacity:.3}}
        button:hover{filter:brightness(0.93)}
        ::-webkit-scrollbar{width:4px}
        ::-webkit-scrollbar-thumb{background:#cbd5e0;border-radius:4px}
      `}</style>
    </div>
  );
}

function SquadCard({ title, playing, bench, remainingBatters }) {
  return (
    <div style={card({ padding: 13 })}>
      <div
        style={{
          fontWeight: 700,
          fontSize: 11,
          color: "#718096",
          textTransform: "uppercase",
          marginBottom: 8,
        }}
      >
        {title}
      </div>
      <div
        style={{
          fontSize: 10,
          color: "#718096",
          fontWeight: 600,
          marginBottom: 5,
        }}
      >
        Playing XI
      </div>
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "1fr 1fr",
          gap: "2px 8px",
        }}
      >
        {playing.map((p, i) => {
          const isRemaining = remainingBatters && remainingBatters.includes(p);
          return (
            <div
              key={p}
              style={{
                fontSize: 11.5,
                color: isRemaining ? "#276749" : "#2d3748",
                padding: "1px 0",
                fontWeight: isRemaining ? 600 : 400,
              }}
            >
              {i + 1}. {p}{" "}
              {isRemaining && (
                <span style={{ fontSize: 9, color: "#38a169" }}>●</span>
              )}
            </div>
          );
        })}
      </div>
      <div
        style={{
          fontSize: 10,
          color: "#718096",
          fontWeight: 600,
          marginTop: 8,
          marginBottom: 3,
        }}
      >
        BENCH
      </div>
      {bench.map((p) => (
        <div key={p} style={{ fontSize: 11.5, color: "#718096" }}>
          {p}
        </div>
      ))}
    </div>
  );
}
