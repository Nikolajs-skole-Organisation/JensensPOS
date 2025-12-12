import { BASE_URL } from "./apiConfig.js";

const AUTH_URL = `${BASE_URL}/service/auth`;

export async function loginWithPin(pinCode) {
  const res = await fetch(`${AUTH_URL}/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ pinCode })
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || "Forkert kode.");
  }

  return res.json();
}