import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import TeamForm from "../components/TeamUI/TeamForm";
import { getTeamById, updateTeam } from "../services/api";

export default function EditTeamPage() {
  const { id } = useParams();

  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);

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

  useEffect(() => {
    loadTeam();
  }, []);

  const loadTeam = async () => {
    try {
      const data = await getTeamById(id);

      setTeam({
        name: data.name || "",
        shortName: data.shortName || "",
        nickname: data.nickname || "",
        country: data.country || "",
        logoUrl: data.logoUrl || "",
        primaryColor: data.primaryColor || "",
        coachName: data.coachName || "",
        ranking: data.ranking || "",
      });
    } catch (error) {
      console.error(error);
      alert("Failed to load team");
      navigate("/teams");
    } finally {
      setLoading(false);
    }
  };

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

      await updateTeam(id, request);

      alert("Team updated successfully");

      navigate(`/teams/${id}`);
    } catch (error) {
      console.error(error);
      alert("Failed to update team");
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex justify-center items-center bg-slate-100">
        <p className="text-slate-500 text-lg">Loading Team...</p>
      </div>
    );
  }
  return (
    <TeamForm
      team={team}
      onChange={handleChange}
      onSubmit={handleSubmit}
      submitLabel="Update Team"
      title="Edit Team"
      loading={loading}
    />
  );
}
