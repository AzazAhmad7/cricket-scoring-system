import { useState } from "react";
import { useNavigate } from "react-router-dom";
import TeamForm from "../components/TeamUI/TeamForm";
import { createTeam } from "../services/api";

export default function CreateTeamPage() {
  const navigate = useNavigate();

  const [loading, setLoading] = useState(false);

  const [team, setTeam] = useState({
    name: "",
    shortName: "",
    nickname: "",
    country: "",
    logoUrl: "",
    primaryColor: "",
    coachName: "",
    ranking: "",
  });

  const handleChange = (e) => {
    const { name, value } = e.target;

    setTeam((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      setLoading(true);

      const request = {
        ...team,
        ranking: team.ranking ? Number(team.ranking) : null,
      };

      await createTeam(request);

      alert("Team created successfully");

      navigate("/teams");
    } catch (error) {
      console.error(error);
      alert("Failed to create team");
    } finally {
      setLoading(false);
    }
  };
  return (
    <>
      <TeamForm
        team={team}
        onChange={handleChange}
        onSubmit={handleSubmit}
        submitLabel="Create Team"
        title="Create Team"
        loading={loading}
      />
    </>
  );
}
