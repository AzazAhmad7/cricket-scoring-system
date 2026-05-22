import TeamSquadCard from "./TeamSquadCard";

const teamAPlayers = [
  "Rohit Sharma",
  "Virat Kohli",
  "Shubman Gill",
  "KL Rahul",
  "Hardik Pandya",
  "Ravindra Jadeja",
  "Kuldeep Yadav",
  "Mohammed Shami",
  "Jasprit Bumrah",
  "Mohammed Siraj",
  "Suryakumar Yadav",
];

const teamBPlayers = [
  "David Warner",
  "Travis Head",
  "Steve Smith",
  "Marnus Labuschagne",
  "Glenn Maxwell",
  "Marcus Stoinis",
  "Pat Cummins",
  "Mitchell Starc",
  "Josh Hazlewood",
  "Adam Zampa",
  "Alex Carey",
];

export default function TeamsAndSquads() {
  return (
    <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
      <h2 className="text-xl font-semibold text-slate-900 mb-6">
        Teams & Squads
      </h2>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <TeamSquadCard
          teamName="Team A"
          defaultName="India"
          players={teamAPlayers}
        />
        <TeamSquadCard
          teamName="Team B"
          defaultName="Australia"
          players={teamBPlayers}
        />
      </div>
    </div>
  );
}
