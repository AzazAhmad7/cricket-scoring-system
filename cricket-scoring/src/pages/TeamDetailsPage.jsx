import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { FaEdit, FaUserShield } from "react-icons/fa";
import { getTeamById } from "../services/api";

import { FaCrown } from "react-icons/fa";
import { GiCricketBat } from "react-icons/gi";
import { PiCricketFill } from "react-icons/pi";
import { MdSportsCricket } from "react-icons/md";
import { TbHandStop } from "react-icons/tb";

export default function TeamDetailsPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [team, setTeam] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadTeam();
  }, []);

  const loadTeam = async () => {
    try {
      const res = await getTeamById(id);
      console.log(res);
      setTeam(res);
    } catch (err) {
      console.error(err);
      alert("Failed to load team");
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

  if (!team) {
    return (
      <div className="min-h-screen flex justify-center items-center bg-slate-100">
        <p className="text-red-500">Team not found</p>
      </div>
    );
  }
  const PlayerRoleIcon = (player) => {
    console.log("player ", player);
    if (player.captain) {
      return (
        <div className="relative">
          <GiCricketBat className="text-blue-600 text-2xl" />
          <FaCrown className="absolute -top-2 -right-2 text-yellow-500 text-sm" />
        </div>
      );
    }

    if (player.wicketKeeper) {
      return (
        <TbHandStop className="text-green-600 text-2xl" title="Wicket Keeper" />
      );
    }

    if (player.role === "BATTER") {
      return <GiCricketBat className="text-blue-600 text-2xl" title="Batter" />;
    }

    if (player.role === "BOWLER") {
      return <PiCricketFill className="text-red-600 text-2xl" title="Bowler" />;
    }

    if (player.role === "ALL_ROUNDER") {
      return (
        <div className="relative">
          <GiCricketBat className="text-purple-600 text-2xl" />
          <PiCricketFill className="absolute -bottom-1 -right-1 text-purple-600 text-lg" />
        </div>
      );
    }

    return <MdSportsCricket className="text-slate-500 text-2xl" />;
  };

  return (
    <div className="min-h-screen bg-slate-100 p-8">
      <div className="max-w-7xl mx-auto">
        {/* HEADER CARD */}
        <div className="bg-white rounded-3xl shadow-lg p-8 mb-8">
          <div className="flex flex-col lg:flex-row gap-8 items-center">
            {/* LOGO */}
            <div className="flex justify-center">
              {team.logoUrl ? (
                <img
                  src={team.logoUrl}
                  alt={team.name}
                  className="w-40 h-40 object-contain"
                />
              ) : (
                <div className="w-40 h-40 bg-slate-200 rounded-2xl flex items-center justify-center">
                  No Logo
                </div>
              )}
            </div>

            {/* TEAM INFO */}
            <div className="flex-1 w-full">
              <div className="flex justify-between items-start flex-wrap gap-4">
                <div>
                  <h1 className="text-4xl font-bold text-slate-800">
                    {team.name}
                  </h1>

                  <p className="text-slate-500 text-xl mt-2">
                    {team.shortName}
                  </p>
                </div>

                <button
                  onClick={() => navigate(`/teams/edit/${team.id}`)}
                  className="flex items-center gap-2 bg-blue-600 text-white px-5 py-3 rounded-xl hover:bg-blue-700 transition"
                >
                  <FaEdit />
                  Edit Team
                </button>
              </div>

              {/* DETAILS */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-8">
                <div>
                  <p className="text-slate-500">Country</p>

                  <p className="font-semibold text-lg">{team.country || "-"}</p>
                </div>

                <div>
                  <p className="text-slate-500">Nickname</p>

                  <p className="font-semibold text-lg">
                    {team.nickname || "-"}
                  </p>
                </div>

                <div>
                  <p className="text-slate-500">Coach</p>

                  <p className="font-semibold text-lg">
                    {team.coachName || "-"}
                  </p>
                </div>

                <div>
                  <p className="text-slate-500">Ranking</p>

                  <p className="font-semibold text-lg">{team.ranking || "-"}</p>
                </div>
              </div>

              {/* PRIMARY COLOR */}
              <div className="flex items-center gap-4 mt-8">
                <p className="text-slate-500">Primary Color</p>

                <div
                  className="w-10 h-10 rounded-full border-2 border-slate-300"
                  style={{
                    backgroundColor: team.primaryColor || "#e2e8f0",
                  }}
                />

                <span className="font-medium">{team.primaryColor || "-"}</span>
              </div>
            </div>
          </div>
        </div>

        {/* PLAYERS SECTION */}
        <div className="bg-white rounded-3xl shadow-lg p-8">
          <div className="flex justify-between items-center mb-8">
            <h2 className="text-2xl font-bold text-slate-800">
              Players ({team.players?.length || 0})
            </h2>
          </div>

          {team.players?.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {team.players?.map((player) => (
                <div
                  key={player.id}
                  className="bg-white border border-slate-200 rounded-2xl p-5 hover:shadow-lg hover:-translate-y-1 transition-all duration-200"
                >
                  <div className="flex items-center gap-3 mb-3">
                    <div className="w-12 h-12 rounded-full bg-slate-100 flex items-center justify-center">
                      {PlayerRoleIcon(player)}
                    </div>

                    <div>
                      <h3 className="font-semibold text-slate-800">
                        {player.fullName}
                      </h3>

                      <p className="text-sm text-slate-500">{player.role}</p>
                    </div>
                  </div>

                  <div className="flex flex-wrap gap-2 mt-4">
                    {player.captain && (
                      <span className="bg-yellow-100 text-yellow-700 px-3 py-1 rounded-full text-xs font-medium">
                        C
                      </span>
                    )}

                    {player.wicketKeeper && (
                      <span className="bg-green-100 text-green-700 px-3 py-1 rounded-full text-xs font-medium">
                        WK
                      </span>
                    )}

                    {player.role === "BATTER" && (
                      <span className="bg-blue-100 text-blue-700 px-3 py-1 rounded-full text-xs font-medium">
                        Batter
                      </span>
                    )}

                    {player.role === "BOWLER" && (
                      <span className="bg-red-100 text-red-700 px-3 py-1 rounded-full text-xs font-medium">
                        Bowler
                      </span>
                    )}

                    {player.role === "ALL_ROUNDER" && (
                      <span className="bg-purple-100 text-purple-700 px-3 py-1 rounded-full text-xs font-medium">
                        All Rounder
                      </span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-10 text-slate-500">
              No Players Found
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
