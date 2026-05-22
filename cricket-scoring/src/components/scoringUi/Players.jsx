export default function Players({ matchData }) {
  // Backend structure:
  // matchData.teams.homeTeam
  // matchData.teams.awayTeam
  // matchData.squad.homeTeamPlaying11.players
  // matchData.squad.homeTeamSubstitutes.players
  // matchData.squad.homeTeamBenchPlayers.players
  // matchData.squad.awayTeamPlaying11.players
  // matchData.squad.awayTeamSubstitutes.players
  // matchData.squad.awayTeamBenchPlayers.players

  const teams = matchData?.teams || {};
  const squad = matchData?.squads || {};

  const homeTeam = teams?.homeTeam || {};
  const awayTeam = teams?.awayTeam || {};

  // Helper function to combine all players and remove duplicates
  const getAllPlayers = (teamType) => {
    const isHome = teamType === "home";

    const playing11 =
      (isHome
        ? squad?.homeTeamPlaying11?.players
        : squad?.awayTeamPlaying11?.players) || [];

    const substitutes =
      (isHome
        ? squad?.homeTeamSubstitutes?.players
        : squad?.awayTeamSubstitutes?.players) || [];

    const benchPlayers =
      (isHome
        ? squad?.homeTeamBenchPlayers?.players
        : squad?.awayTeamBenchPlayers?.players) || [];

    // Combine all players
    const allPlayers = [...playing11, ...substitutes, ...benchPlayers];

    // Remove duplicates using playerId/id
    const uniquePlayers = [];
    const seen = new Set();

    console.log(allPlayers);
    allPlayers.forEach((player) => {
      if (!player) return;

      const playerId = player?.playerId || player?.id;

      if (!seen.has(playerId)) {
        seen.add(playerId);
        uniquePlayers.push(player);
      }
    });

    return uniquePlayers;
  };

  // Determine player status
  const getPlayerStatus = (player, teamType) => {
    const isHome = teamType === "home";
    const playerId = player?.playerId || player?.id;

    const playing11 =
      (isHome
        ? squad?.homeTeamPlaying11?.players
        : squad?.awayTeamPlaying11?.players) || [];

    const substitutes =
      (isHome
        ? squad?.homeTeamSubstitutes?.players
        : squad?.awayTeamSubstitutes?.players) || [];

    const benchPlayers =
      (isHome
        ? squad?.homeTeamBenchPlayers?.players
        : squad?.awayTeamBenchPlayers?.players) || [];

    if (playing11.some((p) => (p?.playerId || p?.id) === playerId)) {
      return "Playing XI";
    }

    if (substitutes.some((p) => (p?.playerId || p?.id) === playerId)) {
      return "Substitute";
    }

    if (benchPlayers.some((p) => (p?.playerId || p?.id) === playerId)) {
      return "Bench";
    }

    return "Squad";
  };

  // Render a player card
  const renderPlayerCard = (player, teamType, index) => {
    const status = getPlayerStatus(player, teamType);

    const playerId = player?.playerId || player?.id || index;
    const playerName =
      player?.fullName || player?.shortName || "Unknown Player";
    const role =
      player?.role || player?.playerRole || player?.speciality || "Player";

    const badgeStyles = {
      "Playing XI": "bg-green-100 text-green-700",
      Substitute: "bg-blue-100 text-blue-700",
      Bench: "bg-amber-100 text-amber-700",
      Squad: "bg-gray-100 text-gray-700",
    };

    return (
      <div
        key={playerId}
        className="bg-white rounded-xl border border-gray-200 shadow-sm p-4 hover:shadow-md transition-all duration-200"
      >
        {/* Top Section */}
        <div className="flex items-start gap-3">
          {/* Avatar */}
          <div className="w-12 h-12 rounded-full bg-gradient-to-br from-slate-600 to-slate-800 text-white flex items-center justify-center font-bold text-lg shrink-0">
            {playerName.charAt(0).toUpperCase()}
          </div>

          {/* Name and Role */}
          <div className="flex-1 min-w-0">
            <h4 className="font-semibold text-gray-800 text-sm truncate">
              {playerName}
            </h4>
            <p className="text-xs text-gray-500 mt-0.5">{role}</p>
          </div>
        </div>

        {/* Status Badge */}
        <div className="mt-4">
          <span
            className={`inline-flex px-2.5 py-1 rounded-full text-[10px] font-semibold uppercase tracking-wide ${
              badgeStyles[status] || badgeStyles.Squad
            }`}
          >
            {status}
          </span>
        </div>
      </div>
    );
  };

  // Render a team section
  const renderTeamSection = (team, players, teamType, headerClasses) => {
    const teamName = team?.name || "Team";

    return (
      <div className="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
        {/* Header */}
        <div className={`${headerClasses} px-4 py-3`}>
          <div className="flex items-center justify-between">
            <h3 className="text-white font-semibold text-sm uppercase tracking-wide">
              👥 {teamName}
            </h3>
            <span className="text-white/80 text-xs">
              {players.length} Players
            </span>
          </div>
        </div>

        {/* Players Grid */}
        <div className="p-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {players.length > 0 ? (
              players.map((player, index) =>
                renderPlayerCard(player, teamType, index),
              )
            ) : (
              <p className="text-sm text-gray-500 col-span-full">
                No players available.
              </p>
            )}
          </div>
        </div>
      </div>
    );
  };

  // Get all players
  const homePlayers = getAllPlayers("home");
  const awayPlayers = getAllPlayers("away");

  return (
    <div className="space-y-4">
      {renderTeamSection(
        homeTeam,
        homePlayers,
        "home",
        "bg-gradient-to-r from-blue-600 to-blue-700",
      )}

      {renderTeamSection(
        awayTeam,
        awayPlayers,
        "away",
        "bg-gradient-to-r from-purple-600 to-purple-700",
      )}
    </div>
  );
}
