import {
  getReceipt,
  payOrder,
  compOrder,
  validateChiefPin,
} from "../services/orderApi.js";

const orderTableInfoEl = document.getElementById("order-table-info");
const orderStatusInfoEl = document.getElementById("order-status-info");
const orderMetaEl = document.getElementById("order-meta");
const orderItemsListEl = document.getElementById("order-items-list");
const categoryListEl = document.getElementById("category-list");
const itemsTitleEl = document.getElementById("items-title");
const itemsContainerEl = document.getElementById("items-container");
const errorEl = document.getElementById("order-error");
const backToOverviewBtn = document.getElementById("back-to-overview-btn");
const payBtn = document.getElementById("pay-btn");
const compBtn = document.getElementById("comp-btn");
const API_BASE = "http://localhost:8080";

let orderId = null;
let tableNumber = null;
let amountOfGuests = null;
let orderStatus = null;
let categories = [];
let selectedCategoryId = null;
let orderItems = [];

window.addEventListener("DOMContentLoaded", initOrderPage);

async function initOrderPage() {
  const storedEmployee = sessionStorage.getItem("currentEmployee");
  if (!storedEmployee) {
    window.location.href = "login.html";
    return;
  }
  const employee = JSON.parse(storedEmployee);
  console.log("Order page opened by:", employee.name, employee.role);

  const stored = sessionStorage.getItem("currentOrder");
  if (!stored) {
    errorEl.textContent =
      "Ingen aktiv ordre fundet. Gå tilbage til oversigten.";
    return;
  }

  try {
    const data = JSON.parse(stored);

    orderId = data.orderId;
    tableNumber = data.tableNumber;
    amountOfGuests = data.amountOfGuests;
    orderStatus = data.orderStatus;
    categories = data.categories || [];

    if (categories.length > 0) {
      selectedCategoryId = categories[0].id;
    }

    // 🔹 NEW: fetch order details (items) from backend
    const details = await fetchOrderDetails(orderId);
    orderItems = details.items || [];

    renderHeader();
    renderOrderMeta();
    renderOrderItems();
    renderCategories();
    renderItems();
  } catch (e) {
    console.error(e);
    errorEl.textContent = "Fejl ved indlæsning af ordredata.";
  }
}

function renderHeader() {
  orderTableInfoEl.textContent = `Bord ${tableNumber}`;
  orderStatusInfoEl.textContent = `Status: ${orderStatus} - Gæster: ${amountOfGuests}`;
}

function renderOrderMeta() {
  orderMetaEl.textContent = `Ordre #${orderId} - bord ${tableNumber}`;
}

function renderOrderItems() {
  orderItemsListEl.innerHTML = "";

  if (!orderItems || orderItems.length === 0) {
    const li = document.createElement("li");
    li.textContent = "Ingen varer på ordren endnu.";
    orderItemsListEl.appendChild(li);
    return;
  }

  orderItems.forEach((item) => {
    const li = document.createElement("li");

    const name =
      (item.foodItem && item.foodItem.name) ||
      (item.drinkItem && item.drinkItem.name) ||
      "Ukendt vare";

    const leftSpan = document.createElement("span");
    leftSpan.textContent = `${item.quantity} x ${name}`;

    const rightSpan = document.createElement("span");
    rightSpan.className = "meta";
    if (item.meatTemperature) {
      rightSpan.textContent = item.meatTemperature;
    }

    li.appendChild(leftSpan);
    li.appendChild(rightSpan);

    orderItemsListEl.appendChild(li);
  });
}

function renderCategories() {
  categoryListEl.innerHTML = "";
  if (!categories || categories.length === 0) return;

  categories.forEach((cat) => {
    const btn = document.createElement("button");
    btn.className = "category-btn";
    if (cat.id === selectedCategoryId) {
      btn.classList.add("active");
    }
    btn.textContent = cat.name;
    btn.addEventListener("click", () => {
      selectedCategoryId = cat.id;
      renderCategories();
      renderItems();
    });
    categoryListEl.appendChild(btn);
  });
}

function renderItems() {
  itemsContainerEl.innerHTML = "";

  if (!categories || categories.length === 0 || selectedCategoryId == null) {
    itemsTitleEl.textContent = "Vælg en kategori";
    return;
  }

  const category = categories.find((c) => c.id === selectedCategoryId);
  if (!category) {
    itemsTitleEl.textContent = "Vælg en kategori";
    return;
  }

  itemsTitleEl.textContent = category.name;

  // Food items
  if (category.foodItems && category.foodItems.length > 0) {
    const foodTitle = document.createElement("div");
    foodTitle.className = "item-section-title";
    foodTitle.textContent = "Mad";
    itemsContainerEl.appendChild(foodTitle);

    const foodGrid = document.createElement("div");
    foodGrid.className = "item-grid";

    category.foodItems.forEach((food) => {
      const btn = document.createElement("button");
      btn.className = "item-btn";
      btn.textContent = food.name;
      btn.addEventListener("click", () => handleFoodClick(food));
      foodGrid.appendChild(btn);
    });

    itemsContainerEl.appendChild(foodGrid);
  }

  // Drink items
  if (category.drinkItems && category.drinkItems.length > 0) {
    const drinkTitle = document.createElement("div");
    drinkTitle.className = "item-section-title";
    drinkTitle.textContent = "Drikkevarer";
    itemsContainerEl.appendChild(drinkTitle);

    const drinkGrid = document.createElement("div");
    drinkGrid.className = "item-grid";

    category.drinkItems.forEach((drink) => {
      const btn = document.createElement("button");
      btn.className = "item-btn";
      btn.textContent = drink.name;
      btn.addEventListener("click", () => handleDrinkClick(drink));
      drinkGrid.appendChild(btn);
    });

    itemsContainerEl.appendChild(drinkGrid);
  }
}

// ----- Item click handlers -----

function handleFoodClick(food) {
  if (!orderId) return;

  if (food.isMeat) {
    // simple approach: prompt for meat temperature
    const temp = window.prompt(
      "Vælg stegegrad (RØD, MEDIUM_RØD, MEDIUM, MEDIUM_WELL_DONE, WELL_DONE):",
      "MEDIUM"
    );
    if (!temp) return;

    addItemToOrder(food.id, "FOOD", temp);
  } else {
    addItemToOrder(food.id, "FOOD", null);
  }
}

function handleDrinkClick(drink) {
  if (!orderId) return;
  addItemToOrder(drink.id, "DRINK", null);
}

// ----- Call backend to add item -----

async function addItemToOrder(itemId, itemType, meatTemperature = null) {
  const requestBody = {
    itemId,
    itemType,
    meatTemperature,
  };

  const res = await fetch(`${API_BASE}/service/orders/${orderId}/items`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(requestBody),
  });

  if (!res.ok) {
    const text = await res.text();
    console.error("Backend returned error response:", text);
    throw new Error(text || "Kunne ikke tilføje vare til ordren.");
  }

  const updatedOrder = await res.json();
  console.log("Updated order:", updatedOrder);

  if (updatedOrder.items) {
    orderItems = updatedOrder.items;
    renderOrderItems();
  }

  // keep storing meta – items come from updatedOrder now
  sessionStorage.setItem(
    "currentOrder",
    JSON.stringify({
      orderId,
      tableNumber,
      amountOfGuests,
      orderStatus,
      categories,
    })
  );

  return updatedOrder;
}

// ----- Back button -----

backToOverviewBtn.addEventListener("click", () => {
  window.location.href = "overview.html";
});

// ----- Send button -----

const sendOrderBtn = document.getElementById("send-order-btn");

sendOrderBtn.addEventListener("click", () => {
  const sendOrderBtn = document.getElementById("send-order-btn");

  sendOrderBtn.addEventListener("click", async () => {
    if (!tableNumber) return;

    try {
      const res = await fetch(
        `${API_BASE}/service/tables/${tableNumber}/send`,
        {
          method: "POST",
        }
      );

      if (!res.ok) throw new Error(`HTTP ${res.status}`);

      alert("Ordre sendt til køkken og bar");

      // optional: refresh order details so UI stays in sync
      const details = await fetchOrderDetails(orderId);
      orderItems = details.items || [];
      renderOrderItems();
    } catch (e) {
      console.error(e);
      alert("Kunne ikke sende ordre: " + e.message);
    }
  });
});

async function fetchOrderDetails(orderId) {
  const res = await fetch(`${API_BASE}/service/orders/${orderId}`);
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || "Kunne ikke hente ordredata");
  }
  return await res.json(); // AddItemResponse
}

payBtn.addEventListener("click", async () => {
  if (!orderId) return;

  try {
    const receipt = await getReceipt(orderId);

    if (!receipt.lines || receipt.lines.length === 0) {
      alert("Ordren indeholder ingen varer.");
      return;
    }

    const receiptText =
      receipt.lines
        .map((l) => `${l.quantity} x ${l.name} = ${l.lineTotal} kr`)
        .join("\n") + `\n\nTOTAL: ${receipt.total} kr`;

    const confirmed = confirm(
      receiptText + "\n\nVil du gennemføre betalingen?"
    );

    if (!confirmed) return;

    await payOrder(orderId);

    alert("Betaling gennemført.");

    sessionStorage.removeItem("currentOrder");
    window.location.href = "overview.html";
  } catch (e) {
    console.error(e);
    alert("Fejl ved betaling: " + e.message);
  }
});

compBtn.addEventListener("click", async () => {
  if (!orderId) return;

  const pin = prompt("Indtast 4-cifret CHIEF-kode:");
  if (pin == null) return;

  if (!/^\d{4}$/.test(pin)) {
    alert("Koden skal være 4 cifre.");
    return;
  }

  try {
    await validateChiefPin(pin);
  } catch (e) {
    alert("Der skal bruges chief kode");
    return;
  }

  const reason = prompt("Angiv årsag til comp:");
  if (reason == null) return;

  if (!reason.trim()) {
    alert("Du skal angive en årsag.");
    return;
  }

  try {
    const receipt = await compOrder(orderId, pin, reason);
    alert(`Ordren er comp'et.\nTOTAL: ${receipt.total} kr`);

    sessionStorage.removeItem("currentOrder");
    window.location.href = "overview.html";
  } catch (e) {
    alert(e.message);
  }
});
