const titleEl = document.getElementById("guest-title");
const displayEl = document.getElementById("guest-display");
const keypadEl = document.getElementById("guest-keypad");
const backBtn = document.getElementById("guest-back-btn");
const deleteBtn = document.getElementById("guest-delete-btn");
const submitBtn = document.getElementById("guest-submit-btn");
const errorEl = document.getElementById("guest-error");

let currentGuestInput = "0";
let tableNumber = null;

// Get the tableNumber from the url.
const params = new URLSearchParams(window.location.search);
if(params.has("tableNumber")){
    tableNumber = parseInt(params.get("tableNumber"), 10);
}
if(!tableNumber || tableNumber < 1){
    titleEl.textContent = "ukendt bord";
    submitBtn.disabled = true;
    errorEl.textContent = "Mangler bordnummer i URL'en (?tableNumber=...).";
} else {
    titleEl.textContent = `Bord ${tableNumber}`;
}

function updateDisplay() {
    displayEl.textContent = currentGuestInput;
}

// keypad click handling
keypadEl.addEventListener("click", (event) => {
    const btn = event.target;
    if(!btn.classList.contains("key-btn")) return;

    const key = btn.getAttribute("data-key");
    if(!key) return;

    if (currentGuestInput === "0"){
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
    if(currentGuestInput.length <= 1) {
        currentGuestInput = "0";
    } else {
        currentGuestInput = currentGuestInput.slice(0, -1);
    }
    updateDisplay();
    errorEl.textContent = "";
});

// "Fortsæt" = start order
submitBtn.addEventListener("click", async () => {
    if(!tableNumber || tableNumber < 1){
        errorEl.textContent = "Ugyldigt bordnummer.";
        return;
    }

    const guests = parseInt(currentGuestInput, 10);
    if(!guests || guests < 1) {
        errorEl.textContent = "Antal gæster skal være mindst 1."
        return;
    }

    errorEl.textContent = "";
    submitBtn.disabled = true;
    submitBtn.textContent = "Starter...";

    try{
        const API_BASE = "http://localhost:8080";

        const res = await fetch(`${API_BASE}/service/tableview/${tableNumber}?amountOfGuests=${guests}`, {
            method: "POST"
        });

        if(!res.ok){
            const text = await res.text();
            throw new Error(text || "Kunne ikke starte ordren");
        }

        const startOrderResponse = await res.json();
        console.log("StartOrderResponse", startOrderResponse);

        sessionStorage.setItem("currentOrder", JSON.stringify(startOrderResponse));

        window.location.href = `order.html?orderId=${startOrderResponse.orderId}`;

    } catch(e){
        console.error(e);
        errorEl.textContent = e.message || "Fejl ved start af ordre.";
        submitBtn.disabled = false;
        submitBtn.textContent = "Fortsæt";
    }
});