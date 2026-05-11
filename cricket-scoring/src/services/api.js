import axios from "axios";

const API = "http://localhost:8080";

export const getMatchState = async (id) => {
  const res = await axios.get(`${API}/matches/${id}`);
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
