const orderTableInfoEl  = document.getElementById("order-table-info");
const orderStatusInfoEl = document.getElementById("order-status-info");
const orderMetaEl       = document.getElementById("order-meta");
const orderItemsListEl  = document.getElementById("order-items-list");
const categoryListEl    = document.getElementById("category-list");
const itemsTitleEl      = document.getElementById("items-title");
const itemsContainerEl  = document.getElementById("items-container");
const errorEl           = document.getElementById("order-error");
const backToOverviewBtn = document.getElementById("back-to-overview-btn");
const API_BASE = "http://localhost:8080";

let orderId = null;
let tableNumber = null;
let amountOfGuests = null;
let orderStatus = null;
let categories = [];
let selectedCategoryId = null;
let orderItems = [];

window.addEventListener("DOMContentLoaded", initOrderPage);

function initOrderPage(){
    const stored = sessionStorage.getItem("currentOrder");
    if(!stored){
        errorEl.textContent = "Ingen aktiv ordre funder. Gå tilbage til oversigen.";
        return;
    }

    try {
        const data = JSON.parse(stored);

        orderId        = data.orderId;
        tableNumber    = data.tableNumber;
        amountOfGuests = data.amountOfGuests;
        orderStatus    = data.orderStatus;
        categories     = data.categories || [];

        if (categories.length > 0){
            selectedCategoryId = categories[0].id;
        }

        orderItems = [];

        renderHeader();
        renderOrderMeta();
        renderOrderItems();
        renderCategories();
        renderItems();
    } catch (e) {
        console.error(e);
        errorEl.textContent = "Fejl ved indlæsning af ordreData."
    }
}

function renderHeader(){
    orderTableInfoEl.textContent = `Bord ${tableNumber}`;
    orderStatusInfoEl.textContent = `Status: ${orderStatus} - Gæster: ${amountOfGuests}`;
}

function renderOrderMeta(){
    orderMetaEl.textContent = `Ordre #${orderId} - bord ${tableNumber}`
}

function renderOrderItems(){
    orderItemsListEl.innerHTML = "";

    if(!orderItems || orderItems.length === 0){
        const li = document.createElement("li");
        li.textContent = "Ingen varer på ordren endnu.";
        orderItemsListEl.appendChild(li);
        return;
    }

    orderItems.forEach(item => {
        const li = document.createElement("li");

        const name = 
              (item.foodItem && item.foodItem.name) ||
              (item.drinkItem && item.drinkItem.name) ||
              "Ukendt vare";
        
        const leftSpan = document.createElement("span");
        leftSpan.textContent = `${item.quantity} x ${name}`;

        const rightSpan = document.createElement("span");
        rightSpan.className = "meta";
        if(item.meatTemperature){
            rightSpan.textContent = item.meatTemperature;
        }

        li.appendChild(leftSpan);
        li.appendChild(rightSpan);

        orderItemsListEl.appendChild(li);
    });
}

function renderCategories(){
    categoryListEl.innerHTML = "";
    if(!categories || categories.length === 0) return;

    categories.forEach(cat => {
        const btn = document.createElement("button");
        btn.className = "category-btn";
        if(cat.id === selectedCategoryId){
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

    const category = categories.find(c => c.id === selectedCategoryId);
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

        category.foodItems.forEach(food => {
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

        category.drinkItems.forEach(drink => {
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
        meatTemperature
    };

    const res = await fetch(`${API_BASE}/service/orders/${orderId}/items`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(requestBody)
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
    sessionStorage.setItem("currentOrder", JSON.stringify({
        orderId,
        tableNumber,
        amountOfGuests,
        orderStatus,
        categories
    }));

    return updatedOrder;
}




// ----- Back button -----

backToOverviewBtn.addEventListener("click", () => {
    window.location.href = "overview.html";
});