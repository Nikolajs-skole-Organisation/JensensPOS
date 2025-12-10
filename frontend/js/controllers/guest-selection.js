const titleEl   = document.getElementById("guest-title");
const displayEl = document.getElementById("guest-display");
const keypadEl  = document.getElementById("guest-keypad");
const backBtn   = document.getElementById("guest-back-btn");
const deleteBtn = document.getElementById("guest-delete-btn");
const submitBtn = document.getElementById("guest-submit-btn");
const errorEl   = document.getElementById("guest-error");

const API_BASE = "http://localhost:8080";

let currentGuestInput = "0";
let tableNumber = null;

function updateDisplay() {
    displayEl.textContent = currentGuestInput;
}

window.addEventListener("DOMContentLoaded", async () => {
    // --- 1. Find tableNumber from URL ---
    const params = new URLSearchParams(window.location.search);
    if (params.has("tableNumber")) {
        tableNumber = parseInt(params.get("tableNumber"), 10);
    }

    if (!tableNumber || tableNumber < 1) {
        titleEl.textContent = "Ukendt bord";
        submitBtn.disabled = true;
        errorEl.textContent = "Mangler bordnummer i URL'en (?tableNumber=...).";
        return;
    } else {
        titleEl.textContent = `Bord ${tableNumber}`;
    }

    updateDisplay();

    // --- 2. Check if there is already an open order for this table ---
    try {
        const res = await fetch(`${API_BASE}/service/tables/${tableNumber}/open-order`);

        if (res.ok) {
            // There *is* an open order
            const order = await res.json(); // This is AddItemResponse

            // We still need the menu/categories -> reuse startOrder endpoint
            const menuRes = await fetch(
                `${API_BASE}/service/tableview/${tableNumber}?amountOfGuests=${order.amountOfGuests}`,
                { method: "POST" }
            );

            if (!menuRes.ok) {
                console.warn("Kunne ikke hente menu til eksisterende ordre", menuRes.status);
            }

            const menuData = await menuRes.json(); // StartOrderResponse (with categories)

            // Store everything order.html needs
            sessionStorage.setItem("currentOrder", JSON.stringify({
                orderId:       order.orderId,      // make sure AddItemResponse uses this name
                tableNumber:   order.tableNumber,
                amountOfGuests: order.amountOfGuests,
                orderStatus:   order.orderStatus,
                categories:    menuData.categories,
                items:         order.items        // so order.js can show existing items
            }));

            // Go directly to the order view
            window.location.href = `order.html?orderId=${order.orderId}`;
            return;
        }

        if (res.status === 404) {
            // No open order -> stay on keypad, normal flow
            console.log("Ingen åben ordre for bord", tableNumber);
        } else {
            console.warn("Fejl ved check af åben ordre", res.status);
        }
    } catch (err) {
        console.error("Fejl ved check af åben ordre:", err);
        // If something goes wrong, we just fall back to keypad
    }

    // If we reach here, user will use keypad + submit to create a new order
});

// ----- Keypad click handling -----

keypadEl.addEventListener("click", (event) => {
    const btn = event.target;
    if (!btn.classList.contains("key-btn")) return;

    const key = btn.getAttribute("data-key");
    if (!key) return;

    if (currentGuestInput === "0") {
        currentGuestInput = key;
    } else {
        currentGuestInput += key;
    }
    updateDisplay();
});

// "Tilbage" = go back to previous view
backBtn.addEventListener("click", () => {
    window.history.back();
});

// "Slet" = delete last digit
deleteBtn.addEventListener("click", () => {
    if (currentGuestInput.length <= 1) {
        currentGuestInput = "0";
    } else {
        currentGuestInput = currentGuestInput.slice(0, -1);
    }
    updateDisplay();
    errorEl.textContent = "";
});

// "Fortsæt" = start order
submitBtn.addEventListener("click", async () => {
    if (!tableNumber || tableNumber < 1) {
        errorEl.textContent = "Ugyldigt bordnummer.";
        return;
    }

    const guests = parseInt(currentGuestInput, 10);
    if (!guests || guests < 1) {
        errorEl.textContent = "Antal gæster skal være mindst 1.";
        return;
    }

    errorEl.textContent = "";
    submitBtn.disabled = true;
    submitBtn.textContent = "Starter...";

    try {
        const res = await fetch(
            `${API_BASE}/service/tableview/${tableNumber}?amountOfGuests=${guests}`,
            { method: "POST" }
        );

        if (!res.ok) {
            const text = await res.text();
            throw new Error(text || "Kunne ikke starte ordren");
        }

        const startOrderResponse = await res.json();
        console.log("StartOrderResponse", startOrderResponse);

        // This already contains orderId, tableNumber, amountOfGuests, orderStatus, categories
        sessionStorage.setItem("currentOrder", JSON.stringify(startOrderResponse));

        window.location.href = `order.html?orderId=${startOrderResponse.orderId}`;
    } catch (e) {
        console.error(e);
        errorEl.textContent = e.message || "Fejl ved start af ordre.";
        submitBtn.disabled = false;
        submitBtn.textContent = "Fortsæt";
    }
});
