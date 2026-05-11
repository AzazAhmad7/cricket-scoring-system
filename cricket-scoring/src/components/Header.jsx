import { useState, useEffect } from "react";

export default function Header({ data }) {
  const home = data?.teams?.homeTeam?.name || "Home";
  const away = data?.teams?.awayTeam?.name || "Away";
  const homeCode = data?.teams?.homeTeam?.shortName || "IND";
  const awayCode = data?.teams?.awayTeam?.shortName || "AUS";
  const toss = data?.toss?.winnerName || "";
  const elected = data?.toss?.tossDecision || "";

  const [seconds, setSeconds] = useState(0);

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

      {/* Status + Timer + Settings */}
      <div className="flex items-center gap-3 text-xs">
        <span className="bg-green-500 text-white px-2 py-[2px] rounded font-bold tracking-wider text-[11px]">
          LIVE
        </span>
        <span className="text-gray-300 font-mono">{fmt(seconds)}</span>
        <button className="text-gray-400 hover:text-white text-base">⚙</button>
      </div>
    </div>
  );
}
