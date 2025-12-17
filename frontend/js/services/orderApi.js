const BASE_URL = "http://localhost:8080";
const SERVICE_URL = `${BASE_URL}/service`;

export async function getReceipt(orderId) {
  const res = await fetch(`${SERVICE_URL}/orders/${orderId}/receipt`);
  if (!res.ok) throw new Error("Failed to fetch receipt");
  return res.json();
}

export async function payOrder(orderId) {
  const res = await fetch(`${SERVICE_URL}/orders/${orderId}/pay`, {
    method: "POST"
  });
  if (!res.ok) throw new Error("Failed to pay order");
  return res.json();
}