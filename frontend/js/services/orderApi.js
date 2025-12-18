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

export async function compOrder(orderId, pin, reason) {
  const res = await fetch(`${SERVICE_URL}/orders/${orderId}/comp`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ pin, reason })
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || "Failed to comp order");
  }

  return res.json();
}

export async function validateChiefPin(pin) {
  const res = await fetch(`${SERVICE_URL}/orders/comp/validate`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ pin })
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || "Den skal bruge chief kode");
  }
}

export async function compOrderItems(orderId, pin, reason, orderItemIds) {
  const res = await fetch(`${SERVICE_URL}/orders/${orderId}/items/comp`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ pin, reason, orderItemIds })
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || "Kunne ikke comp items");
  }

  return res.json(); 
}
