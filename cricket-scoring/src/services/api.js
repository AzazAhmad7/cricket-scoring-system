import axios from "axios";

const API = "http://localhost:8080";

//MATCH APIS
export const getMatchById = async (id) => {
  const res = await axios.get(`${API}/matches/${id}`);
  return res.data;
};

export const getMatchSetup = async (matchId) => {
  return await axios.get(`${API}/matches/${matchId}/setup`);
};

export const getAllMatches = async () => {
  const res = await axios.get(`${API}/matches`);
  return res.data;
};

export const createMatch = async (createMatchRequest) => {
  const res = await axios.post(`${API}/matches/create`, createMatchRequest);
  return res.data;
};

export const getMatchAllData = async (id) => {
  const res = await axios.get(`${API}/matches/${id}/matchAllData`);
  console.log("DATA I GOT", res.data);
  return res.data;
};

export const changeInning = async (id) => {
  const res = await axios.post(`${API}/matches/${id.matchId}/innings`);
  return res.data;
};

export const resetMatch = async (id) => {
  const res = await axios.put(`${API}/matches/${id.matchId}/reset`);
  return res.data;
};

export const updateMatchStatus = async (matchId, status) => {
  const payload = {
    matchStatus: status,
  };
  const res = await axios.put(`${API}/matches/${matchId}/status`, payload);
  return res.data;
};
export const updateMatch = async (matchId, request) => {
  const res = await axios.put(`${API}/matches/${matchId}`, request);
  return res.data;
};

//SCORING API
export const rebuildMatch = async (id) => {
  const res = await axios.post(`${API}/score/${id.matchId}/rebuild`);
  return res.data;
};
export const swapStriker = async (matchState) => {
  console.log("STATE ", matchState);
  const res = await axios.post(
    `${API}/score/swapStriker`,
    matchState.matchState,
  );
  return res.data;
};
export const scoreBall = async (event) => {
  const res = await axios.post(`${API}/score/scoreBall`, event);
  return res.data;
};

export const impactPlayer = async (event) => {
  console.log("impact event " + event);
  const res = await axios.post(
    `${API}/score/impactPlayer/${event.matchId}`,
    event,
  );
  return res.data;
};

export const logNewBatter = async (event) => {
  const res = await axios.post(
    `${API}/score/selectBatter/${event.playerId}`,
    event,
  );
  return res.data;
};
export const logNewBowler = async (event) => {
  console.log(event);
  const res = await axios.post(
    `${API}/score/selectBowler/${event.playerId}`,
    event,
  );
  return res.data;
};

export const endOver = async (event) => {
  const res = await axios.post(`${API}/score/endOver`, event);
  return res.data;
};

// =========================
// TEAM APIs
// =========================

export const createTeam = async (request) => {
  console.log("request ", request);
  const res = await axios.post(`${API}/teams/create`, request);
  return res.data.data ?? res.data;
};
export const getAllTeams = async () => {
  const res = await axios.get(`${API}/teams`);
  return res.data.data ?? res.data;
};

export const deleteTeam = async (teamId) => {
  const res = await axios.delete(`${API}/teams/${teamId}`);
  return res.data.data ?? res.data;
};

export const updateTeam = async (teamId, request) => {
  const res = await axios.put(`${API}/teams/${teamId}`, request);
  return res.data.data ?? res.data;
};

export const getTeamById = async (id) => {
  const res = await axios.get(`${API}/teams/${id}`);
  return res.data.data ?? res.data;
};

//PLAYER APIS

export const getAllPlayers = async () => {
  const res = await axios.get(`${API}/players`);
  return res.data.data ?? res.data;
};

export const getPlayerById = async (id) => {
  const res = await axios.get(`${API}/players/${id}`);
  return res.data.data ?? res.data;
};

export const deletePlayer = async (id) => {
  const res = await axios.delete(`${API}/players/${id}`);
  return res.data.data ?? res.data;
};

export const createPlayer = async (player) => {
  const res = await axios.post(`${API}/players/create`, player);
  return res.data.data ?? res.data;
};
export const updatePlayer = async (playerId, request) => {
  console.log("update", request);
  const res = await axios.put(`${API}/players/${playerId}`, request);
  return res.data.data ?? res.data;
};

export const assignTeamToPlayer = async (playerId, teamId) => {
  const response = await axios.put(`${API}/players/${playerId}/teams/${teamId}`);

  return response.data;
};

// =========================
// VENUE APIs
// =========================
export const getAllVenues = async () => {
  const res = await axios.get(`${API}/venues`);
  return res.data.data ?? res.data;
};

//TOURNAMENTS
export const getAllTournaments = async () => {
  const res = await axios.get(`${API}/tournaments`);
  return res.data.data ?? res.data;
};

export const createTournament = async (request) => {
  const res = await axios.post(`${API}/tournaments/create`, request);
  return res.data;
};

export const deleteTournament = async (id) => {
  const res = await axios.delete(`${API}/tournaments/${id}`);
  return res.data;
};
export const getTournamentById = async (id) => {
  const res = await axios.get(`${API}/tournaments/${id}`);
  return res.data.data ?? res.data;
};

//tournamentteam apis
export const getAllTeamsOfTournament = async (id) => {
  const res = await axios.get(`${API}/tournamentTeams/${id}`);
  return res.data.data ?? res.data;
};

export const assignTeamsToTournament = async (tournamentId, teamIds) => {
  console.log("tournamentId ", tournamentId);
  console.log("teamId ", teamIds);
  const payload = {
    tournamentId: tournamentId,
    teamIds: teamIds,
  };
  console.log(payload);
  const res = await axios.post(`${API}/tournaments/teams`, payload);
};

//ERROR HANDLING

export const handleApiError = (error) => {
  console.error("Full Error:", error);
  console.error("Response:", error.response);
  console.error("Response Data:", error.response?.data.error);

  if (error.response) {
    const message =
      error.response.data?.error?.message ||
      error.response.data?.error ||
      `Request failed with status ${error.response.status}`;

    alert(message);
  } else if (error.request) {
    alert("Server is unreachable. Please try again.");
  } else {
    alert(error.message || "Something went wrong.");
  }
};
