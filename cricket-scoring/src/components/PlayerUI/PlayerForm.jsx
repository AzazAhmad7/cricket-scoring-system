import { useEffect, useState } from "react";
import { getAllTeams } from "../../services/api";

const PLAYER_ROLES = ["BATTER", "BOWLER", "ALL_ROUNDER", "WICKET_KEEPER"];

const BATTING_STYLES = ["RIGHT_HAND_BAT", "LEFT_HAND_BAT"];

const BOWLING_STYLES = [
  "RIGHT_ARM_FAST",
  "RIGHT_ARM_MEDIUM",
  "LEFT_ARM_FAST",
  "LEFT_ARM_SPIN",
  "RIGHT_ARM_OFFBREAK",
  "RIGHT_ARM_LEGBREAK",
  "LEFT_ARM_ORTHODOX",
  "RIGHT_ARM_MEDIUM_FAST",
];

const defaultPlayer = {
  externalPlayerId: null,
  fullName: null,
  shortName: null,
  jerseyNumber: null,
  dateOfBirth: null,
  nationality: null,
  role: null,
  battingStyle: null,
  bowlingStyle: null,
  captain: false,
  wicketKeeper: false,
  battingOrder: null,
  active: true,
  teamId: null,

  matchesPlayed: 0,
  runs: 0,
  wickets: 0,
  battingAverage: 0,
  bowlingAverage: 0,
};

export default function PlayerForm({
  initialValues = defaultPlayer,
  onSubmit,
  submitText = "Save Player",
  isEdit = false,
}) {
  const [teams, setTeams] = useState([]);
  const [formData, setFormData] = useState(initialValues);

  useEffect(() => {
    loadTeams();
  }, []);

  useEffect(() => {
    setFormData(initialValues);
  }, [initialValues]);

  const loadTeams = async () => {
    try {
      const res = await getAllTeams();
      setTeams(res || []);
    } catch (err) {
      console.error(err);
    }
  };

  const handleChange = (e) => {
    const { name, value, checked, type } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(formData);
  };

  return (
    <form onSubmit={handleSubmit} className="grid md:grid-cols-2 gap-6">
      <Input
        label="Full Name"
        name="fullName"
        value={formData.fullName}
        onChange={handleChange}
        required
      />

      <Input
        label="Short Name"
        name="shortName"
        value={formData.shortName}
        onChange={handleChange}
      />

      <Input
        label="External Player ID"
        name="externalPlayerId"
        value={formData.externalPlayerId}
        onChange={handleChange}
      />

      <Input
        label="Jersey Number"
        name="jerseyNumber"
        type="number"
        value={formData.jerseyNumber}
        onChange={handleChange}
      />

      <Input
        label="Date Of Birth"
        name="dateOfBirth"
        type="date"
        value={formData.dateOfBirth}
        onChange={handleChange}
      />

      <Input
        label="Nationality"
        name="nationality"
        value={formData.nationality}
        onChange={handleChange}
      />

      <Select
        label="Role"
        name="role"
        value={formData.role}
        onChange={handleChange}
        options={PLAYER_ROLES}
      />

      <Select
        label="Batting Style"
        name="battingStyle"
        value={formData.battingStyle}
        onChange={handleChange}
        options={BATTING_STYLES}
      />

      <Select
        label="Bowling Style"
        name="bowlingStyle"
        value={formData.bowlingStyle}
        onChange={handleChange}
        options={BOWLING_STYLES}
      />

      <div>
        <label className="block mb-2 font-medium">Team</label>

        <select
          name="teamId"
          value={formData.teamId}
          onChange={handleChange}
          disabled={isEdit}
          className={`w-full border rounded-xl p-3 ${
            isEdit ? "bg-slate-100 text-slate-500 cursor-not-allowed" : ""
          }`}
        >
          <option value="">Select Team</option>

          {teams.map((team) => (
            <option key={team.id} value={team.id}>
              {team.name}
            </option>
          ))}
        </select>
      </div>

      <Input
        label="Batting Order"
        name="battingOrder"
        type="number"
        value={formData.battingOrder}
        onChange={handleChange}
      />

      <div className="col-span-2 flex gap-8">
        <Checkbox
          label="Captain"
          name="captain"
          checked={formData.captain}
          onChange={handleChange}
        />

        <Checkbox
          label="Wicket Keeper"
          name="wicketKeeper"
          checked={formData.wicketKeeper}
          onChange={handleChange}
        />

        <Checkbox
          label="Active"
          name="active"
          checked={formData.active}
          onChange={handleChange}
        />
      </div>

      <div className="col-span-2">
        <button
          type="submit"
          className="bg-blue-600 text-white px-8 py-3 rounded-xl hover:bg-blue-700"
        >
          {submitText}
        </button>
      </div>
    </form>
  );
}

function Input(props) {
  return (
    <div>
      <label className="block mb-2 font-medium">{props.label}</label>

      <input {...props} className="w-full border rounded-xl p-3" />
    </div>
  );
}

function Select({ label, options, ...props }) {
  return (
    <div>
      <label className="block mb-2 font-medium">{label}</label>

      <select {...props} className="w-full border rounded-xl p-3">
        <option value="">Select</option>

        {options.map((option) => (
          <option key={option} value={option}>
            {option}
          </option>
        ))}
      </select>
    </div>
  );
}

function Checkbox({ label, ...props }) {
  return (
    <label className="flex items-center gap-2">
      <input type="checkbox" {...props} />
      {label}
    </label>
  );
}
