const Input = ({ label, type = "text", placeholder }) => (
  <div>
    <label className="block text-sm font-medium text-slate-700 mb-2">
      {label}
    </label>
    <input
      type={type}
      placeholder={placeholder}
      className="w-full rounded-lg border border-slate-300 px-4 py-2.5 focus:outline-none focus:ring-2 focus:ring-indigo-500"
    />
  </div>
);

const Select = ({ label, options }) => (
  <div>
    <label className="block text-sm font-medium text-slate-700 mb-2">
      {label}
    </label>
    <select className="w-full rounded-lg border border-slate-300 px-4 py-2.5 focus:outline-none focus:ring-2 focus:ring-indigo-500">
      {options.map((option) => (
        <option key={option}>{option}</option>
      ))}
    </select>
  </div>
);

export default function MatchDetailsCard() {
  return (
    <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
      <h2 className="text-xl font-semibold text-slate-900 mb-6">
        Match Details
      </h2>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <Input label="Match Name" placeholder="India vs Australia" />
        <Select label="Match Type" options={["T20", "ODI", "Test", "Custom"]} />
        <Input label="Date" type="date" />

        <Input label="Venue" placeholder="Wankhede Stadium" />
        <Input label="City" placeholder="Mumbai" />
        <Input label="Overs Per Innings" type="number" placeholder="20" />

        <Select
          label="Pitch Type"
          options={["Balanced", "Batting", "Bowling", "Spin Friendly"]}
        />
        <Select
          label="Weather"
          options={["Sunny", "Cloudy", "Rainy", "Indoor"]}
        />
        <Select
          label="Ball Type"
          options={["Red Ball", "White Ball", "Pink Ball"]}
        />
      </div>
    </div>
  );
}
