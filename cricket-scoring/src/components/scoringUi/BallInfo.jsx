export default function BallInfo({ matchData, matchState }) {
  const inning = matchState?.scoreCard?.innings?.find(
    (i) => i.inningNumber === matchState?.currentInningNumber,
  );

  const home = matchData?.teams?.homeTeam;
  const away = matchData?.teams?.awayTeam;

  const battingTeam = matchState.battingTeamId === home.id ? home : away;
  const bowlingTeam = matchState.battingTeamId === home.id ? away : home;

  console.log("Data ", matchData.rules.ballsPerOver);
  // Match rules
  const ballsPerOver = matchData?.rules?.ballsPerOver || 6;
  const totalOvers = matchData?.rules?.overs || 20;

  // Current score summary
  const scoreSummary = inning?.scoreSummary || {};

  // First innings total (used as target for second innings)
  const firstInning = matchState?.scoreCard?.innings?.find(
    (i) => i.inningNumber === 1,
  );

  const firstInningRuns = firstInning?.scoreSummary?.runs || 0;
  const target = firstInningRuns + 1;

  // Current second innings score
  const currentRuns = scoreSummary?.runs || 0;
  const currentWickets = scoreSummary?.wickets || 0;

  // Balls calculations
  const oversCompleted = scoreSummary?.overs || 0;
  const ballsBowled = scoreSummary?.overs * 6 + scoreSummary?.balls || 0;

  const totalBalls = totalOvers * ballsPerOver;
  const ballsRemaining = Math.max(totalBalls - ballsBowled, 0);

  // Chase calculations (only relevant in 2nd innings)
  const runsRequired = Math.max(target - currentRuns, 0);

  const requiredRunRate =
    ballsRemaining > 0 && runsRequired > 0
      ? ((runsRequired * 6) / ballsRemaining).toFixed(2)
      : "0.00";

  const currentRunRate =
    ballsBowled > 0 ? ((currentRuns * 6) / ballsBowled).toFixed(2) : "0.00";

  const equation =
    runsRequired === 0
      ? `${battingTeam.name || "Batting Team"} won by ${
          10 - currentWickets
        } wicket${10 - currentWickets !== 1 ? "s" : ""}`
      : ballsRemaining === 0
        ? currentRuns === firstInningRuns
          ? "Match tied"
          : `${bowlingTeam.name || "1st Innings Team"} won by ${
              firstInningRuns - currentRuns
            } run${firstInningRuns - currentRuns !== 1 ? "s" : ""}`
        : `${runsRequired} runs from ${ballsRemaining} balls`;

  // Base rows (always shown)
  const rows = [
    { label: "Total Overs", value: totalOvers ?? "-" },
    { label: "Ball Per Over", value: ballsPerOver ?? "-" },
    {
      label: "Innings",
      value:
        matchState?.currentInningNumber === 1 ? "1st Innings" : "2nd Innings",
    },
    {
      label: "Powerplay",
      value: `P (${matchData?.rules?.powerPlayStartOver}-${matchData?.rules?.powerPlayEndOver})`,
    },
  ];

  // Add chase information only in 2nd innings
  if (matchState?.currentInningNumber === 2) {
    rows.push(
      { label: "Target", value: target },
      { label: "Score", value: `${currentRuns}/${currentWickets}` },
      { label: "Runs Required", value: runsRequired },
      { label: "Balls Remaining", value: ballsRemaining },
      { label: "Required Run Rate", value: requiredRunRate },
      { label: "Equation", value: equation },
      { label: "Current Run Rate", value: currentRunRate },
    );
  }

  // Final rows
  rows.push(
    { label: "Field", value: matchData?.matchInfo?.fieldType || "Day" },
    { label: "Pitch", value: matchData?.matchInfo?.pitch || "Good" },
  );

  return (
    <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-4 h-full">
      <h3 className="font-semibold text-gray-800 text-sm mb-3 uppercase tracking-wide">
        Ball Info
      </h3>

      <div className="space-y-2">
        {rows.map((row) => (
          <div
            key={row.label}
            className="flex justify-between items-start text-xs gap-4"
          >
            <span className="text-gray-500">{row.label}</span>
            <span className="font-medium text-gray-800 text-right">
              {row.value}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
