export default function ScoreBug() {
  const data = {
    team: "BLASTERS",
    opponent: "ARCS",

    score: "142-9",
    overs: "19.5",

    striker: {
      name: "SAGARR",
      runs: 42,
      balls: 22,
    },

    nonStriker: {
      name: "KARSH",
      runs: 0,
      balls: 2,
    },

    speed: "116.5",
    match: "MATCH 16",

    thisOver: ["4", "6", "0", "6", "6"],
  };

  return (
    <div className="fixed bottom-5 left-5 right-5 z-50">
      <div className="grid h-24 grid-cols-[240px_180px_400px_220px_300px] overflow-hidden rounded-xl bg-black/85 shadow-2xl backdrop-blur-md">
        {/* TEAM */}
        <div className="relative flex flex-col justify-center bg-gradient-to-r from-cyan-500 to-blue-700 pl-6">
          <div
            className="absolute right-0 top-0 h-full w-8 bg-pink-500"
            style={{
              clipPath: "polygon(100% 0, 0 50%, 100% 100%)",
            }}
          />

          <h2 className="text-4xl font-black text-white">{data.team}</h2>

          <p className="text-lg font-semibold text-white/90">
            v {data.opponent}
          </p>
        </div>

        {/* SCORE */}
        <div className="flex flex-col items-center justify-center border-r border-cyan-400/30">
          <h1 className="text-6xl font-black text-pink-500">{data.score}</h1>

          <p className="text-xl text-white">{data.overs}</p>
        </div>

        {/* BATTERS */}
        <div className="flex flex-col justify-center px-8">
          <div className="mb-2 grid grid-cols-[1fr_60px_40px] text-white">
            <span className="font-bold text-pink-500">
              🏏 {data.nonStriker.name}
            </span>

            <span className="text-right text-4xl font-black">
              {data.nonStriker.runs}
            </span>

            <span className="text-right text-xl">{data.nonStriker.balls}</span>
          </div>

          <div className="grid grid-cols-[1fr_60px_40px] text-white">
            <span className="font-bold text-pink-500">
              🏏 {data.striker.name}
            </span>

            <span className="text-right text-4xl font-black">
              {data.striker.runs}
            </span>

            <span className="text-right text-xl">{data.striker.balls}</span>
          </div>
        </div>

        {/* MATCH */}
        <div className="flex items-center justify-center border-l border-r border-cyan-400/30">
          <h2 className="text-4xl font-black text-white">{data.match}</h2>
        </div>

        {/* SPEED + THIS OVER */}
        <div className="flex flex-col justify-center px-4">
          <div className="flex items-center gap-3">
            <span className="text-lg font-bold text-yellow-400">SPEED</span>

            <span className="text-5xl font-black text-yellow-300">
              {data.speed}
            </span>

            <span className="text-xl font-bold text-yellow-300">KPH</span>
          </div>

          <div className="mt-2 flex items-center gap-2">
            <span className="mr-2 text-sm font-semibold text-white">
              THIS OVER
            </span>

            {data.thisOver.map((ball, i) => (
              <div
                key={i}
                className="flex h-8 w-8 items-center justify-center rounded bg-cyan-500 font-bold text-white"
              >
                {ball}
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
