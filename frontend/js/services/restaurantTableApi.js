const BASE_URL = "http://localhost:8080";
const TABLE_URL = `${BASE_URL}/service/overview`;

export async function getAllTables() {
  const res = await fetch(TABLE_URL);
  if (!res.ok) throw new Error("Failed to fetch tables");
  return res.json();
}