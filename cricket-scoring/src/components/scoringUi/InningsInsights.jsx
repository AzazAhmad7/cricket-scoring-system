export default function InningsInsights({ matchData, matchState }) {
  const home = matchData?.teams?.homeTeam;
  const away = matchData?.teams?.awayTeam;

  // Get both innings
  const firstInning = matchState?.scoreCard?.innings?.find(
    (i) => i.inningNumber === 1,
  );
  const firstInningBattingTeam = firstInning.teamId === home.id ? home : away;
  const secondInningBattingTeam = firstInning.teamId === home.id ? away : home;
  console.log(firstInningBattingTeam);

  const secondInning = matchState?.scoreCard?.innings?.find(
    (i) => i.inningNumber === 2,
  );

  // Helper function to safely extract stats
  const getStats = (inning) => {
    const control = inning?.controlMetrics || {};
    const extras = inning?.extras || {};
    const summary = inning?.scoreSummary || {};

    const fours = control.fours || 0;
    const sixes = control.sixes || 0;
    const boundaries = control.boundaries;
    const dotBalls = control.dots || 0;
    const balls = summary.balls + summary.overs * 6 || 0;
    // const runs = summary.runs || 0;

    return {
      // Basic Stats
      extras: extras.total || 0,
      fours,
      sixes,
      boundaries,
      dotBalls,
      singles: control.singles || 0,
      twos: control.doubles || 0,
      threes: control.threes || 0,

      // Innings Progress (Overs)
      //   overs: summary.overs || "0.0",

      // Run Rate
      //   runRate: balls > 0 ? ((runs * 6) / balls).toFixed(2) : "0.00",

      // Boundary Percentage
      boundaryPercentage:
        boundaries > 0 ? `${control.boundaryPercentage}%` : "0.00%",

      // Dot Ball Percentage
      dotBallPercentage:
        balls > 0 ? `${((dotBalls / balls) * 100).toFixed(2)}%` : "0.00%",
    };
  };

  // Stats for both innings
  const team1 = getStats(firstInning);
  const team2 = getStats(secondInning);

  // Rows to compare both innings side by side
  const rows = [
    { label: "Extras", left: team1.extras, right: team2.extras },
    { label: "Fours", left: team1.fours, right: team2.fours },
    { label: "Sixes", left: team1.sixes, right: team2.sixes },

    { label: "Dot Balls", left: team1.dotBalls, right: team2.dotBalls },

    { label: "Singles", left: team1.singles, right: team2.singles },
    { label: "Twos", left: team1.twos, right: team2.twos },
    { label: "Threes", left: team1.threes, right: team2.threes },
    {
      label: "Boundary %",
      left: team1.boundaryPercentage,
      right: team2.boundaryPercentage,
    },
    {
      label: "Dot Ball %",
      left: team1.dotBallPercentage,
      right: team2.dotBallPercentage,
    },
  ];

  return (
    <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-4 h-full">
      <h3 className="font-semibold text-gray-800 text-sm mb-3 uppercase tracking-wide">
        Innings Insights
      </h3>

      {/* Team Names */}
      <div className="grid grid-cols-3 items-center mb-3 pb-2 border-b border-gray-100">
        {/* First Innings Team */}
        <span className="text-xs font-semibold text-gray-800 text-left truncate">
          {firstInningBattingTeam.name || "Team 1"}
        </span>

        {/* Center Header */}
        <span className="text-[10px] font-medium text-gray-400 text-center uppercase">
          Stats
        </span>

        {/* Second Innings Team */}
        <span className="text-xs font-semibold text-gray-800 text-right truncate">
          {secondInningBattingTeam.name || "Team 2"}
        </span>
      </div>

      {/* Stats Rows */}
      <div className="space-y-2">
        {rows.map((row) => (
          <div
            key={row.label}
            className="grid grid-cols-3 items-center text-xs gap-2"
          >
            {/* Left Value */}
            <span className="font-medium text-gray-800 text-left">
              {row.left}
            </span>

            {/* Center Label */}
            <span className="text-gray-500 text-center font-medium">
              {row.label}
            </span>

            {/* Right Value */}
            <span className="font-medium text-gray-800 text-right">
              {row.right}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
