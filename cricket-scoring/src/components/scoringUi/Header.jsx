import { useState, useEffect } from "react";
import { updateMatchStatus } from "../../services/api";

export default function Header({ data }) {
  const home = data?.teams?.homeTeam?.name || "Home";
  const away = data?.teams?.awayTeam?.name || "Away";
  const homeCode = data?.teams?.homeTeam?.shortName || "IND";
  const awayCode = data?.teams?.awayTeam?.shortName || "AUS";
  const toss = data?.toss?.winnerName || "";
  const elected = data?.toss?.tossDecision || "";

  // Match status options
  const MATCH_STATUS = [
    "SCHEDULED",
    "LIVE",
    "PAUSE",
    "INNINGS_BREAK",
    "COMPLETED",
  ];

  // Initial status from API, fallback to LIVE
  const [matchStatus, setMatchStatus] = useState(
    data?.matchInfo?.status || "LIVE",
  );

  const [seconds, setSeconds] = useState(0);
  const [updating, setUpdating] = useState(false);

  // Update status when data changes
  useEffect(() => {
    if (data?.matchInfo?.status) {
      setMatchStatus(data.matchInfo.status);
    }
  }, [data]);


  useEffect(() => {
    const interval = setInterval(() => setSeconds((p) => p + 1), 1000);
    return () => clearInterval(interval);
  }, []);

  const fmt = (s) => {
    const h = String(Math.floor(s / 3600)).padStart(2, "0");
    const m = String(Math.floor((s % 3600) / 60)).padStart(2, "0");
    const sec = String(s % 60).padStart(2, "0");
    return `${h}:${m}:${sec}`;
  };

  // Status badge colors
  const getStatusBadgeClass = (status) => {
    switch (status) {
      case "SCHEDULED":
        return "bg-blue-500";
      case "LIVE":
        return "bg-green-500";
      case "PAUSE":
        return "bg-yellow-500 text-black";
      case "INNINGS_BREAK":
        return "bg-yellow-500 text-black";
      case "COMPLETED":
        return "bg-gray-500";
      default:
        return "bg-green-500";
    }
  };

  // Format status text for display
  const formatStatus = (status) => {
    switch (status) {
      case "INNINGS_BREAK":
        return "INNINGS BREAK";
      default:
        return status;
    }
  };

  // Send updated status to backend
  const handleStatusChange = async (e) => {
    const newStatus = e.target.value;
    const previousStatus = matchStatus;
    const matchId = data.matchInfo.matchId;

    // Optimistic UI update
    setMatchStatus(newStatus);

    if (!matchId) return;

    try {
      setUpdating(true);
      console.log("new status",newStatus)
      await updateMatchStatus(matchId, newStatus);

      console.log("Match status updated successfully");
    } catch (error) {
      console.error("Failed to update match status:", error);

      // Revert if API fails
      setMatchStatus(previousStatus);

      alert("Failed to update match status");
    } finally {
      setUpdating(false);
    }
  };

  return (
    <div className="bg-[#0f172a] text-white px-4 py-2 flex justify-between items-center border-b border-slate-700">
      {/* Match Selector */}
      <select className="bg-slate-700 text-white rounded px-3 py-1 text-xs border border-slate-600 focus:outline-none">
        <option>Match {data?.matchInfo?.matchId || "25"} ▾</option>
      </select>

      {/* Center: Teams + Toss */}
      <div className="flex flex-col items-center gap-0.5">
        <div className="flex items-center gap-4 font-semibold text-sm">
          {/* Home Team */}
          <div className="flex items-center gap-2">
            <span className="text-lg">🇮🇳</span>
            <span>{home}</span>
            <span className="text-gray-400 text-xs">{homeCode}</span>
          </div>

          <span className="text-gray-400 font-normal">vs</span>

          {/* Away Team */}
          <div className="flex items-center gap-2">
            <span className="text-lg">🇦🇺</span>
            <span>{away}</span>
            <span className="text-gray-400 text-xs">{awayCode}</span>
          </div>
        </div>

        {/* Toss Info */}
        {toss && (
          <div className="text-[10px] text-gray-400 tracking-wider uppercase">
            Toss: <span className="text-gray-200 font-medium">{toss}</span>
            {elected && (
              <>
                &nbsp;&nbsp;·&nbsp;&nbsp;Elected to{" "}
                <span className="text-green-400 font-medium">{elected}</span>
              </>
            )}
          </div>
        )}
      </div>

      {/* Status + Dropdown + Timer + Settings */}
      <div className="flex items-center gap-3 text-xs">
        {/* Status Badge */}
        <span
          className={`${getStatusBadgeClass(
            matchStatus,
          )} text-white px-2 py-[2px] rounded font-bold tracking-wider text-[11px]`}
        >
          {formatStatus(matchStatus)}
        </span>

        {/* Status Dropdown (left to timer) */}
        <select
          value={matchStatus}
          onChange={handleStatusChange}
          disabled={updating}
          className="bg-slate-700 text-white rounded px-2 py-1 text-[11px] border border-slate-600 focus:outline-none"
        >
          {MATCH_STATUS.map((status) => (
            <option key={status} value={status}>
              {formatStatus(status)}
            </option>
          ))}
        </select>

        {/* Timer */}
        <span className="text-gray-300 font-mono">{fmt(seconds)}</span>

        {/* Settings */}
        <button className="text-gray-400 hover:text-white text-base">⚙</button>
      </div>
    </div>
  );
}
