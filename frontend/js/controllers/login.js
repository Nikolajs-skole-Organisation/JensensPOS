import { loginWithPin } from "../services/loginApi.js";

const displayEl = document.getElementById("login-display");
const keypadEl  = document.getElementById("login-keypad");
const clearBtn  = document.getElementById("login-clear-btn");
const submitBtn = document.getElementById("login-submit-btn");
const errorEl   = document.getElementById("login-error");

let currentPin = "";

function updateDisplay() {
  displayEl.textContent = currentPin.padEnd(4, "•");
}

window.addEventListener("DOMContentLoaded", updateDisplay);

keypadEl.addEventListener("click", (e) => {
  const btn = e.target;
  if (!btn.classList.contains("key-btn")) return;

  const key = btn.dataset.key;
  if (!key || currentPin.length >= 4) return;

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
    const employee = await loginWithPin(currentPin);

    sessionStorage.setItem(
      "currentEmployee",
      JSON.stringify(employee)
    );

    window.location.href = "pages/service/overview.html";

  } catch (err) {
    errorEl.textContent = err.message || "Teknisk fejl ved login.";
    currentPin = "";
    updateDisplay();
  }
});
