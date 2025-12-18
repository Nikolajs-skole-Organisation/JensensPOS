import { BASE_URL } from "./apiConfig.js";

const BAR_UPDATES_URL = `${BASE_URL}/bar/updates`;
const BAR_BUMP_URL = `${BASE_URL}/bar/tables`;

export async function getBarUpdates(since, lastId) {
  const res = await fetch(
    `${BAR_UPDATES_URL}?since=${since}&lastId=${lastId}`,
    {
      cache: "no-store",
    }
  );
  if (!res.ok)
    throw new Error(`Failed to fetch bar updates: HTTP ${res.status}`);
  return res.json();
}

export async function bumpBarTicket(tableNumber) {
  const res = await fetch(`${BAR_BUMP_URL}/${tableNumber}/bump`, {
    method: "POST",
  });
  if (!res.ok) throw new Error(`Failed to bump bar ticket: HTTP ${res.status}`);
}
