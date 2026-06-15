import { useNavigate } from "react-router-dom";
import { createPlayer } from "../services/api";
import PlayerForm from "../components/PlayerUI/PlayerForm";

export default function CreatePlayerPage() {
  const navigate = useNavigate();

  const handleCreate = async (playerData) => {
    try {
      console.log("PlayerData, ", playerData);
      await createPlayer(playerData);

      alert("Player created successfully");

      navigate("/players");
    } catch (err) {
      console.error(err);
      alert("Failed to create player");
    }
  };

  return (
    <div className="max-w-5xl mx-auto p-8">
      <h1 className="text-4xl font-bold mb-8">Create Player</h1>

      <PlayerForm
        onSubmit={handleCreate}
        submitText="Create Player"
        isEdit={false}
      />
    </div>
  );
}
