const displayEl = document.getElementById("login-display");
const keypadEl  = document.getElementById("login-keypad");
const clearBtn  = document.getElementById("login-clear-btn");
const submitBtn = document.getElementById("login-submit-btn");
const errorEl   = document.getElementById("login-error");

const API_BASE = "http://localhost:8080";

let currentPin = "";

// Opdateret funktion: viser tal + prikker
function updateDisplay() {
  displayEl.textContent = currentPin.padEnd(4, "•");
}

window.addEventListener("DOMContentLoaded", () => {
  updateDisplay();
});

keypadEl.addEventListener("click", (e) => {
  const btn = e.target;
  if (!btn.classList.contains("key-btn")) return;

  const key = btn.getAttribute("data-key");
  if (!key) return;
  if (currentPin.length >= 4) return;

  currentPin += key;
  updateDisplay();
});

clearBtn.addEventListener("click", () => {
  currentPin = "";
  errorEl.textContent = "";
  updateDisplay();
});

submitBtn.addEventListener("click", async () => {
  if (currentPin.length !== 4) {
    errorEl.textContent = "Koden skal være præcis 4 cifre.";
    return;
  }

  try {
    const res = await fetch(`${API_BASE}/service/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ pinCode: currentPin })
    });

    if (!res.ok) {
      const text = await res.text();
      errorEl.textContent = text || "Forkert kode.";
      currentPin = "";
      updateDisplay();
      return;
    }

    const employee = await res.json();
    sessionStorage.setItem("currentEmployee", JSON.stringify(employee));

    window.location.href = "pages/service/overview.html";

  } catch (err) {
    console.error(err);
    errorEl.textContent = "Teknisk fejl ved login.";
  }
});
