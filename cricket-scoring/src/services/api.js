import axios from "axios";

const API = "http://localhost:8080";

//MATCH APIS
export const getMatchById = async (id) => {
  const res = await axios.get(`${API}/matches/${id}`);
  return res.data;
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
  return res.data;
};

export const changeInning = async (id) => {
  const res = await axios.post(`${API}/matches/${id.matchId}/innings`);
  return res.data;
};

export const updateMatchStatus = async (matchId, status) => {
  const payload = {
    matchStatus: status,
  };
  const res = await axios.put(`${API}/matches/${matchId}/status`, payload);
  return res.data;
};

//SCORING API
export const scoreBall = async (event) => {
  console.log("scoreEvent " + event);
  const res = await axios.post(`${API}/score/scoreBall`, event);
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
export const getAllTeams = async () => {
  const res = await axios.get(`${API}/teams`);
  return res.data.data ?? res.data;
};

export const getTeamById = async (id) => {
  const res = await axios.get(`${API}/teams/${id}`);
  return res.data.data ?? res.data;
};

// =========================
// VENUE APIs
// =========================
export const getAllVenues = async () => {
  const res = await axios.get(`${API}/venues`);
  return res.data.data ?? res.data;
};
