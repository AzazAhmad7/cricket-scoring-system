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
    sub: "WIDE",
    style:
      "bg-yellow-100 text-yellow-700 border border-yellow-300 hover:bg-yellow-200",
  },
  {
    label: "NB",
    sub: "NO_BALL",
    style: "bg-red-100 text-red-600 border border-red-300 hover:bg-red-200",
  },
  {
    label: "BY",
    sub: "BYE",
    style:
      "bg-amber-100 text-amber-700 border border-amber-300 hover:bg-amber-200",
  },
  {
    label: "LB",
    sub: "LEG_BYE",
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
    sub: "ANY_BALL",
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
  onNextInning,
}) {
  const [showBatterModal, setShowBatterModal] = useState(false);
  const [showBowlerModal, setShowBowlerModal] = useState(false);
  const [showWicketModal, setShowWicketModal] = useState(false);
  const [showAnyBallModal, setShowAnyBallModal] = useState(false);

  // ================= ADD THESE STATES INSIDE NextBallControls COMPONENT =================
  const [showImpactPlayerModal, setShowImpactPlayerModal] = useState(false);
  const [selectedImpactTeam, setSelectedImpactTeam] = useState("");
  const [impactIn, setImpactIn] = useState("");
  const [impactOut, setImpactOut] = useState("");

  const [anyBallForm, setAnyBallForm] = useState({
    isWide: false,
    isNoBall: false,
    isBye: false,
    isLegBye: false,
    runsOfBat: 0,
    runsOfBye: 0,
    dismissedType: "",
    fielderId: "",
    playerId: "",
  });

  const [wicketForm, setWicketForm] = useState({
    dismissedType: "BOWLED",
    dismissedPlayerId: "",
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

  const currentBatters =
    inning?.battingCard?.batters
      ?.filter(
        (batterEntry) =>
          batterEntry?.batter?.playerId === inning?.strikerId ||
          batterEntry?.batter?.playerId === inning?.nonStrikerId,
      )
      .map((batterEntry) => batterEntry.batter) || [];

  console.log("match data ", matchData);
  // Determine fielding team
  const fieldingTeam =
    matchData?.teams?.homeTeam?.id === matchState?.battingTeamId
      ? matchData?.teams?.awayTeam
      : matchData?.teams?.homeTeam;

  // Fielders / Bowlers
  const fielders = fieldingTeam?.players || [];

  // Available bowlers (exclude current bowler)
  const availableBowlers = fielders.filter(
    (player) => player.id !== matchState?.currentBowlerId,
  );
  console.log(availableBowlers);

  const handleAnyBallSubmit = () => {
    const event = {
      eventType: "ANY_BALL",

      // Extras
      isWide: anyBallForm.isWide,
      isNoBall: anyBallForm.isNoBall,
      isBye: anyBallForm.isBye,
      isLegBye: anyBallForm.isLegBye,

      // Runs
      runsOfBat: Number(anyBallForm.runsOfBat),
      runsOfBye: Number(anyBallForm.runsOfBye),

      // Wicket Details
      dismissedType: anyBallForm.dismissedType || null,
      fielderId: anyBallForm.fielderId ? Number(anyBallForm.fielderId) : null,
      playerId: anyBallForm.playerId ? Number(anyBallForm.playerId) : null,
      // Derived Flag
      isWicket: Boolean(anyBallForm.dismissedType),
    };

    // Send event to parent
    onScore(event);

    // Close modal
    setShowAnyBallModal(false);

    // Reset form
    setAnyBallForm({
      isWide: false,
      isNoBall: false,
      isBye: false,
      isLegBye: false,
      runsOfBat: 0,
      runsOfBye: 0,
      dismissedType: "",
      fielderId: "",
      playerId: "",
    });
  };

  // Add this function inside NextBallControls.jsx

  const handleScoreClick = (eventData) => {
    // Current selected players
    const currentBowler = inning?.currentBowlerId;
    const striker = inning?.strikerId;
    const nonStriker = inning?.nonStrikerId;

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
    } else if (label === "NEXT INNINGS") {
      onNextInning?.();
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
      dismissedPlayerId: wicketForm.dismissedPlayerId
        ? Number(wicketForm.dismissedPlayerId)
        : null,
      fielderId: wicketForm.fielderId ? Number(wicketForm.fielderId) : null,
      runs: Number(wicketForm.runs),
      isWicket: true,
    });

    setShowWicketModal(false);

    // Reset form
    setWicketForm({
      dismissedType: "BOWLED",
      dismissedPlayerId: "",
      fielderId: "",
      runs: 0,
    });
  };
  // ================= ADD THIS FUNCTION BELOW handleWicketSubmit =================
  // Submit Payload
  const handleImpactPlayerSubmit = () => {
    const payload = {
      teamId: Number(selectedImpactTeam),
      impactInPlayerId: Number(impactIn),
      impactOutPlayerId: Number(impactOut),
    };

    console.log(payload);
    setShowImpactPlayerModal(false);
    setSelectedImpactTeam("");
    setImpactIn("");
    setImpactOut("");
  };

  // Store TEAM ID in selectedImpactTeam instead of "HOME" or "AWAY"

  const impactSubstitutes =
    String(selectedImpactTeam) === String(matchData?.teams?.homeTeam?.id)
      ? (matchData?.squads?.homeTeamSubstitutes?.players ?? [])
      : String(selectedImpactTeam) === String(matchData?.teams?.awayTeam?.id)
        ? (matchData?.squads?.awayTeamSubstitutes?.players ?? [])
        : [];

  const impactPlaying11 =
    String(selectedImpactTeam) === String(matchData?.teams?.homeTeam?.id)
      ? (matchData?.squads?.homeTeamPlaying11?.players ?? [])
      : String(selectedImpactTeam) === String(matchData?.teams?.awayTeam?.id)
        ? (matchData?.squads?.awayTeamPlaying11?.players ?? [])
        : [];

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
                  } else if (e.sub === "ANY_BALL") {
                    setShowAnyBallModal(true);
                  } else {
                    onScore(e.sub);
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
        {/* Impact Player Button */}

        {matchData?.rules?.impactPlayerEnabled && (
          <button
            onClick={() => setShowImpactPlayerModal(true)}
            className="w-full bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700 text-white py-3 rounded-xl font-bold shadow-lg transition-all"
          >
            IMPACT PLAYER
          </button>
        )}
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

              {/* Dismissed player */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Dismissed Player
                </label>
                <select
                  value={wicketForm.dismissedPlayerId}
                  onChange={(e) =>
                    setWicketForm({
                      ...wicketForm,
                      dismissedPlayerId: e.target.value,
                    })
                  }
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                >
                  <option value="">Select Player</option>
                  {currentBatters.map((player) => (
                    <option key={player.playerId} value={player.playerId}>
                      {player.playerName}
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
      {showAnyBallModal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-lg p-6">
            {/* Header */}
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-lg font-semibold text-gray-800">
                Any Ball Event
              </h2>
              <button
                onClick={() => setShowAnyBallModal(false)}
                className="text-gray-500 hover:text-gray-700 text-2xl"
              >
                ×
              </button>
            </div>

            <div className="space-y-4">
              {/* Wide / No Ball */}
              <div className="grid grid-cols-2 gap-4">
                <label className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    checked={anyBallForm.isWide}
                    onChange={(e) =>
                      setAnyBallForm({
                        ...anyBallForm,
                        isWide: e.target.checked,
                      })
                    }
                  />
                  <span className="text-sm font-medium">Wide</span>
                </label>

                <label className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    checked={anyBallForm.isNoBall}
                    onChange={(e) =>
                      setAnyBallForm({
                        ...anyBallForm,
                        isNoBall: e.target.checked,
                      })
                    }
                  />
                  <span className="text-sm font-medium">No Ball</span>
                </label>
                <label className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    checked={anyBallForm.isBye}
                    onChange={(e) =>
                      setAnyBallForm({
                        ...anyBallForm,
                        isBye: e.target.checked,
                      })
                    }
                  />
                  <span className="text-sm font-medium">Bye</span>
                </label>
                <label className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    checked={anyBallForm.isLegBye}
                    onChange={(e) =>
                      setAnyBallForm({
                        ...anyBallForm,
                        isLegBye: e.target.checked,
                      })
                    }
                  />
                  <span className="text-sm font-medium">Leg Bye</span>
                </label>
              </div>

              {/* Runs of Bat */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Runs of Bat
                </label>
                <select
                  value={anyBallForm.runsOfBat}
                  onChange={(e) =>
                    setAnyBallForm({
                      ...anyBallForm,
                      runsOfBat: e.target.value,
                    })
                  }
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                >
                  {[0, 1, 2, 3, 4, 5, 6].map((run) => (
                    <option key={run} value={run}>
                      {run}
                    </option>
                  ))}
                </select>
              </div>

              {/* Runs of Bye */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Extra runs
                </label>
                <select
                  value={anyBallForm.runsOfBye}
                  onChange={(e) =>
                    setAnyBallForm({
                      ...anyBallForm,
                      runsOfBye: e.target.value,
                    })
                  }
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                >
                  {[0, 1, 2, 3, 4, 5, 6].map((run) => (
                    <option key={run} value={run}>
                      {run}
                    </option>
                  ))}
                </select>
              </div>

              {/* Dismissal Type */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Dismissal Type
                </label>
                <select
                  value={anyBallForm.dismissedType}
                  onChange={(e) =>
                    setAnyBallForm({
                      ...anyBallForm,
                      dismissedType: e.target.value,
                    })
                  }
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                >
                  <option value="">No Wicket</option>
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
                  value={anyBallForm.fielderId}
                  onChange={(e) =>
                    setAnyBallForm({
                      ...anyBallForm,
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

              {/* Player */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Dismissed Player
                </label>
                <select
                  value={anyBallForm.playerId}
                  onChange={(e) =>
                    setAnyBallForm({
                      ...anyBallForm,
                      playerId: e.target.value,
                    })
                  }
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                >
                  <option value="">Select Player</option>
                  {currentBatters.map((player) => (
                    <option key={player.playerId} value={player.playerId}>
                      {player.playerName}
                    </option>
                  ))}
                </select>
              </div>

              {/* Submit Button */}
              <button
                onClick={handleAnyBallSubmit}
                className="w-full bg-gray-800 hover:bg-gray-900 text-white py-2 rounded-lg font-semibold"
              >
                Log Event
              </button>
            </div>
          </div>
        </div>
      )}

      {showImpactPlayerModal && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-2xl p-6">
            {/* Header */}
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-bold text-gray-800">
                Impact Player Substitution
              </h2>
              <button
                onClick={() => setShowImpactPlayerModal(false)}
                className="text-gray-500 hover:text-red-500 text-3xl leading-none"
              >
                ×
              </button>
            </div>

            {/* Form */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {/* Team Selection */}
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-2">
                  Team
                </label>
                {/* Team Dropdown */}
                <select
                  value={selectedImpactTeam}
                  onChange={(e) => {
                    setSelectedImpactTeam(e.target.value); // Stores team ID as string
                    setImpactIn("");
                    setImpactOut("");
                  }}
                  className="w-full border border-gray-300 rounded-lg px-4 py-3"
                >
                  <option value="">Select Team</option>

                  <option value={matchData?.teams?.homeTeam?.id}>
                    {matchData?.teams?.homeTeam?.name}
                  </option>

                  <option value={matchData?.teams?.awayTeam?.id}>
                    {matchData?.teams?.awayTeam?.name}
                  </option>
                </select>
              </div>

              {/* Impact In (from substitutes) */}
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-2">
                  Impact In
                </label>
                {/* Impact In Dropdown */}
                <select
                  value={impactIn}
                  onChange={(e) => setImpactIn(e.target.value)}
                  disabled={!selectedImpactTeam}
                  className="w-full border border-gray-300 rounded-lg px-4 py-3"
                >
                  <option value="">Select Substitute</option>

                  {impactSubstitutes.map((player) => (
                    <option key={player.id} value={player.id}>
                      {player.fullName || player.name}
                    </option>
                  ))}
                </select>
              </div>

              {/* Impact Out (from Playing XI) */}
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-2">
                  Impact Out
                </label>
                {/* Impact Out Dropdown */}
                <select
                  value={impactOut}
                  onChange={(e) => setImpactOut(e.target.value)}
                  disabled={!selectedImpactTeam}
                  className="w-full border border-gray-300 rounded-lg px-4 py-3"
                >
                  <option value="">Select Playing XI Player</option>

                  {impactPlaying11.map((player) => (
                    <option key={player.id} value={player.id}>
                      {player.fullName || player.name}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Footer */}
            <div className="flex justify-end gap-3 mt-6">
              <button
                onClick={() => setShowImpactPlayerModal(false)}
                className="px-5 py-2.5 rounded-lg border border-gray-300 text-gray-700 hover:bg-gray-100 transition"
              >
                Cancel
              </button>

              <button
                onClick={handleImpactPlayerSubmit}
                disabled={!selectedImpactTeam || !impactIn || !impactOut}
                className="px-6 py-2.5 rounded-lg bg-gradient-to-r from-green-500 to-emerald-600 text-white font-semibold shadow hover:shadow-lg disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Submit
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
