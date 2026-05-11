import { useState, useEffect } from "react";

const runs = [
  {
    label: "0",
    sub: "DOT_BALL",
    style:
      "border-2 border-gray-300 bg-white text-gray-700 hover:border-blue-400",
  },
  {
    label: "1",
    sub: "ONE",
    style: "bg-green-500 text-white hover:bg-green-600",
  },
  {
    label: "2",
    sub: "TWO",
    style:
      "border-2 border-gray-300 bg-white text-gray-700 hover:border-blue-400",
  },
  {
    label: "3",
    sub: "THREE",
    style:
      "border-2 border-gray-300 bg-white text-gray-700 hover:border-blue-400",
  },
  {
    label: "4",
    sub: "FOUR",
    style: "bg-blue-500 text-white hover:bg-blue-600",
  },
  {
    label: "6",
    sub: "SIX",
    style: "bg-purple-600 text-white hover:bg-purple-700",
  },
];

const extras = [
  {
    label: "W",
    sub: "Wide",
    style:
      "bg-yellow-100 text-yellow-700 border border-yellow-300 hover:bg-yellow-200",
  },
  {
    label: "NB",
    sub: "No Ball",
    style: "bg-red-100 text-red-600 border border-red-300 hover:bg-red-200",
  },
  {
    label: "BY",
    sub: "Bye",
    style:
      "bg-amber-100 text-amber-700 border border-amber-300 hover:bg-amber-200",
  },
  {
    label: "LB",
    sub: "Leg Bye",
    style:
      "bg-orange-100 text-orange-700 border border-orange-300 hover:bg-orange-200",
  },
  {
    label: "🗑",
    sub: "Wicket",
    style: "bg-red-500 text-white hover:bg-red-600",
  },
  {
    label: "•••",
    sub: "More",
    style: "bg-gray-100 text-gray-600 border border-gray-200 hover:bg-gray-200",
  },
];

const actions = [
  { label: "END OVER", style: "bg-blue-600 hover:bg-blue-700 text-white" },
  {
    label: "CHANGE BOWLER",
    style: "bg-purple-600 hover:bg-purple-700 text-white",
  },
  {
    label: "SELECT NEW BATTER",
    style: "bg-orange-500 hover:bg-orange-600 text-white",
  },
  { label: "TIME OUT", style: "bg-cyan-400 hover:bg-cyan-500 text-white" },
  {
    label: "NEXT INNINGS",
    style: "bg-green-500 hover:bg-green-600 text-white",
  },
];

const howOutOptions = [
  "BOWLED",
  "CAUGHT",
  "RUN_OUT",
  "LBW",
  "STUMPED",
  "HIT_WICKET",
  "CAUGHT_AND_BOWLED",
  "RETIRED_HURT",
  "RETIRED_OUT",
  "OBSTRUCTING_THE_FIELD",
  "HANDLED_THE_BALL",
  "HIT_BALL_TWICE",
  "TIMED_OUT",
];

export default function NextBallControls({
  onScore,
  matchState,
  matchData,
  matchAllData,
  onSelectNewBatter,
  onChangeBowler,
  onEndOver,
}) {
  const [showBatterModal, setShowBatterModal] = useState(false);
  const [showBowlerModal, setShowBowlerModal] = useState(false);
  const [showWicketModal, setShowWicketModal] = useState(false);

  const [wicketForm, setWicketForm] = useState({
    dismissedType: "BOWLED",
    fielderId: "",
    runs: 0,
  });

  // Get current inning
  const inning = matchState?.scoreCard?.innings?.find(
    (i) => i.inningNumber === matchState?.currentInningNumber,
  );
  const overs = inning?.scoreSummary?.overs || 0;
  const balls = inning?.scoreSummary?.balls || 0;

  const totalBalls = overs * 6 + balls;
  useEffect(() => {
    if (totalBalls > 0 && totalBalls % 6 === 0) {
      alert("Over completed! Please select a new bowler.");
      setShowBowlerModal(true);
    }
  }, [totalBalls]);

  // Players still to bat
  const remainingBatters =
    inning?.battingCard?.batters?.filter(
      (batterEntry) => batterEntry?.dismissal?.status === "STILL_TO_BAT",
    ) || [];

  // Determine fielding team
  const fieldingTeam =
    matchData?.teams?.homeTeam?.id === inning?.battingTeamId
      ? matchData?.teams?.awayTeam
      : matchData?.teams?.homeTeam;

  // Fielders / Bowlers
  const fielders = fieldingTeam?.players || [];

  // Available bowlers (exclude current bowler)
  const availableBowlers = fielders.filter(
    (player) => player.id !== matchState?.currentBowlerId,
  );

  // Add this function inside NextBallControls.jsx

  const handleScoreClick = (eventData) => {
    // Current selected players
    const currentBowler = matchState?.currentBowlerId;
    const striker = matchState?.strikerId;
    const nonStriker = matchState?.nonStrikerId;

    // Check if bowler is selected
    if (!currentBowler) {
      alert("Please select a bowler first.");
      setShowBowlerModal(true);
      return;
    }

    // Check if both batters are selected
    if (!striker || !nonStriker) {
      alert("Please select the opening batters first.");
      setShowBatterModal(true);
      return;
    }

    // All validations passed
    onScore(eventData);
  };

  // Handle action buttons
  const handleActionClick = (label) => {
    if (label === "SELECT NEW BATTER") {
      setShowBatterModal(true);
    } else if (label === "CHANGE BOWLER") {
      setShowBowlerModal(true);
    } else if (label === "END OVER") {
      onEndOver?.();
    }
  };

  // Handle batter selection
  const handleBatterSelect = (batterEntry) => {
    onSelectNewBatter?.(batterEntry.batter);
    setShowBatterModal(false);
  };

  // Handle bowler selection
  const handleBowlerSelect = (bowler) => {
    onChangeBowler?.(bowler);
    setShowBowlerModal(false);
  };

  // Handle wicket submission
  const handleWicketSubmit = () => {
    onScore({
      eventType: "WICKET",
      dismissedType: wicketForm.dismissedType,
      fielderId: wicketForm.fielderId ? Number(wicketForm.fielderId) : null,
      runs: Number(wicketForm.runs),
      isWicket: true,
    });

    setShowWicketModal(false);

    // Reset form
    setWicketForm({
      dismissedType: "BOWLED",
      fielderId: "",
      runs: 0,
    });
  };

  return (
    <>
      <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-4 space-y-4">
        <div>
          <h3 className="font-semibold text-gray-800 text-sm mb-1">
            NEXT BALL
          </h3>
          <p className="text-xs text-gray-400">Select Run or Event</p>
        </div>

        {/* Run Buttons */}
        <div className="flex gap-3 flex-wrap">
          {runs.map((r) => (
            <div key={r.label} className="flex flex-col items-center gap-1">
              <button
                onClick={() => handleScoreClick(r.sub)}
                className={`w-14 h-14 rounded-full text-xl font-bold transition-all shadow-sm ${r.style}`}
              >
                {r.label}
              </button>
              <span className="text-[10px] text-gray-400">{r.sub}</span>
            </div>
          ))}
        </div>

        {/* Extra Buttons */}
        <div className="flex gap-3 flex-wrap">
          {extras.map((e) => (
            <div key={e.sub} className="flex flex-col items-center gap-1">
              <button
                onClick={() => {
                  if (e.sub === "Wicket") {
                    setShowWicketModal(true);
                  } else {
                    onScore(e.sub.toUpperCase().replace(" ", "_"));
                  }
                }}
                className={`w-14 h-10 rounded-md text-sm font-bold transition-all ${e.style}`}
              >
                {e.label}
              </button>
              <span className="text-[10px] text-gray-400">{e.sub}</span>
            </div>
          ))}
        </div>

        {/* Action Row */}
        <div className="flex gap-2 pt-1 flex-wrap">
          {actions.map((a) => (
            <button
              key={a.label}
              onClick={() => handleActionClick(a.label)}
              className={`flex-1 min-w-[140px] py-2 rounded-lg text-xs font-bold tracking-wide transition-all ${a.style}`}
            >
              {a.label}
            </button>
          ))}
        </div>
      </div>

      {/* Select New Batter Modal */}
      {showBatterModal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md p-4">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-lg font-semibold text-gray-800">
                Select New Batter
              </h2>
              <button
                onClick={() => setShowBatterModal(false)}
                className="text-gray-500 hover:text-gray-700 text-2xl"
              >
                ×
              </button>
            </div>

            {remainingBatters.length > 0 ? (
              <div className="space-y-2 max-h-80 overflow-y-auto">
                {remainingBatters.map((batterEntry) => (
                  <button
                    key={batterEntry.batter.playerId}
                    onClick={() => handleBatterSelect(batterEntry)}
                    className="w-full text-left px-4 py-3 rounded-lg border border-gray-200 hover:bg-orange-50 hover:border-orange-300 transition"
                  >
                    <div className="font-medium text-gray-800">
                      {batterEntry.batter.playerName}
                    </div>
                  </button>
                ))}
              </div>
            ) : (
              <p className="text-sm text-gray-500 text-center py-6">
                No players remaining to bat.
              </p>
            )}
          </div>
        </div>
      )}

      {/* Select Bowler Modal */}
      {showBowlerModal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md p-4">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-lg font-semibold text-gray-800">
                Select Bowler
              </h2>
              <button
                onClick={() => setShowBowlerModal(false)}
                className="text-gray-500 hover:text-gray-700 text-2xl"
              >
                ×
              </button>
            </div>

            {availableBowlers.length > 0 ? (
              <div className="space-y-2 max-h-80 overflow-y-auto">
                {availableBowlers.map((bowler) => (
                  <button
                    key={bowler.id}
                    onClick={() => handleBowlerSelect(bowler)}
                    className="w-full text-left px-4 py-3 rounded-lg border border-gray-200 hover:bg-purple-50 hover:border-purple-300 transition"
                  >
                    <div className="font-medium text-gray-800">
                      {bowler.fullName}
                    </div>
                  </button>
                ))}
              </div>
            ) : (
              <p className="text-sm text-gray-500 text-center py-6">
                No bowlers available.
              </p>
            )}
          </div>
        </div>
      )}

      {/* Wicket Modal */}
      {showWicketModal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md p-4">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-lg font-semibold text-gray-800">
                Wicket Details
              </h2>
              <button
                onClick={() => setShowWicketModal(false)}
                className="text-gray-500 hover:text-gray-700 text-2xl"
              >
                ×
              </button>
            </div>

            <div className="space-y-4">
              {/* How Out */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  How Out
                </label>
                <select
                  value={wicketForm.dismissedType}
                  onChange={(e) =>
                    setWicketForm({
                      ...wicketForm,
                      dismissedType: e.target.value,
                    })
                  }
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                >
                  {howOutOptions.map((option) => (
                    <option key={option} value={option}>
                      {option.replace(/_/g, " ")}
                    </option>
                  ))}
                </select>
              </div>

              {/* Fielder */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Fielder
                </label>
                <select
                  value={wicketForm.fielderId}
                  onChange={(e) =>
                    setWicketForm({
                      ...wicketForm,
                      fielderId: e.target.value,
                    })
                  }
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                >
                  <option value="">Select Fielder</option>
                  {fielders.map((player) => (
                    <option key={player.id} value={player.id}>
                      {player.fullName}
                    </option>
                  ))}
                </select>
              </div>

              {/* Runs */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Runs
                </label>
                <input
                  type="number"
                  min="0"
                  value={wicketForm.runs}
                  onChange={(e) =>
                    setWicketForm({
                      ...wicketForm,
                      runs: e.target.value,
                    })
                  }
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                />
              </div>

              {/* Submit */}
              <button
                onClick={handleWicketSubmit}
                className="w-full bg-red-500 hover:bg-red-600 text-white py-2 rounded-lg font-semibold"
              >
                Confirm Wicket
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
