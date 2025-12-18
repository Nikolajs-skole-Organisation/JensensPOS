import { BASE_URL } from "./apiConfig.js";

const KITCHEN_URL = `${BASE_URL}/kitchen`;
const KITCHEN_UPDATES_URL = `${KITCHEN_URL}/updates`;

export async function getKitchenUpdates(since, lastId) {
  const res = await fetch(
    `${KITCHEN_UPDATES_URL}?since=${since}&lastId=${lastId}`,
    { cache: "no-store" }
  );

  if (!res.ok) {
    throw new Error(`Failed to fetch kitchen updates: HTTP ${res.status}`);
  }

  return res.json();
}

export async function bumpKitchenTicket(tableNumber) {
  const res = await fetch(`${KITCHEN_URL}/tables/${tableNumber}/bump`, {
    method: "POST",
  });

  if (!res.ok) {
    throw new Error(`Failed to bump ticket: HTTP ${res.status}`);
  }
}
