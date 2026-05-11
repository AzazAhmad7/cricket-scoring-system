export default function Squad({ matchData }) {
  const home = matchData?.teams?.homeTeam;
  const away = matchData?.teams?.awayTeam;
  const homePlayers = matchData?.squads?.homeTeamPlaying11?.players || [];
  const awayPlayers = matchData?.squads?.awayTeamPlaying11?.players || [];
  const homeBench = matchData?.squads?.homeTeamBench?.players || [];
  const awayBench = matchData?.squads?.awayTeamBench?.players || [];

  const TeamList = ({ title, players, bench }) => (
    <div>
      <h3 className="font-semibold text-gray-800 text-xs uppercase tracking-wide mb-2">{title} Squad</h3>
      <p className="text-[10px] text-gray-400 font-medium mb-1">Playing XI</p>
      <ol className="space-y-0.5">
        {players.map((p, idx) => (
          <li key={p.id} className="text-xs text-gray-700 flex items-start gap-1.5">
            <span className="text-gray-400 w-4 shrink-0">{idx + 1}.</span>
            <span>{p.fullName}</span>
          </li>
        ))}
      </ol>
      {bench?.length > 0 && (
        <>
          <p className="text-[10px] text-gray-400 font-medium mt-2 mb-1">Bench</p>
          {bench.map((p) => (
            <div key={p.id} className="text-xs text-gray-500">{p.fullName}</div>
          ))}
        </>
      )}
    </div>
  );

  return (
    <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-4 grid grid-cols-2 gap-4">
      <TeamList title={home?.name || "India"} players={homePlayers} bench={homeBench} />
      <TeamList title={away?.name || "Australia"} players={awayPlayers} bench={awayBench} />
    </div>
  );
}
