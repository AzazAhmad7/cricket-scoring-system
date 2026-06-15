import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { getPlayerById, updatePlayer } from "../services/api";
import PlayerForm from "../components/PlayerUI/PlayerForm";

export default function EditPlayerPage() {
  const { id } = useParams();

  const navigate = useNavigate();

  const [player, setPlayer] = useState(null);

  useEffect(() => {
    loadPlayer();
  }, []);

  const loadPlayer = async () => {
    try {
      const res = await getPlayerById(id);
      console.log(res);
      setPlayer({
        ...res,
        // teamId: res.data.team?.id,
      });
    } catch (err) {
      console.error(err);
    }
  };

  const handleUpdate = async (playerData) => {
    try {
      await updatePlayer(id, playerData);

      alert("Player updated successfully");

      navigate("/players");
    } catch (err) {
      console.error(err);
      alert("Failed to update player");
    }
  };

  if (!player) {
    return <div>Loading...</div>;
  }

  return (
    <div className="max-w-5xl mx-auto p-8">
      <h1 className="text-4xl font-bold mb-8">Edit Player</h1>

      <PlayerForm
        initialValues={player}
        onSubmit={handleUpdate}
        submitText="Update Player"
        isEdit={true}
      />
    </div>
  );
}
