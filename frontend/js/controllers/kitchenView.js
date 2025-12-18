import {
  getKitchenUpdates,
  bumpKitchenTicket,
} from "../services/kitchenApi.js";
const tableQueue = [];

const ticketsByTable = new Map();

let since = 0;
let lastId = 0;

const boardEl = document.getElementById("board");
const bumpPopover = document.getElementById("bump-popover");
const bumpAction = document.getElementById("bump-action");

let bumpTargetTable = null;

function displayName(dto) {
  const oi = dto.item;
  if (oi?.foodItem) return oi.foodItem.name;
  if (oi?.drinkItem) return oi.drinkItem.name;
  return "(unknown)";
}

function ageClass(createdAtMs) {
  const ageMin = (Date.now() - createdAtMs) / 60000;
  if (ageMin < 5) return "age-white";
  if (ageMin < 10) return "age-yellow";
  return "age-red";
}

function formatAge(createdAtMs) {
  const totalSec = Math.floor((Date.now() - createdAtMs) / 1000);
  const min = Math.floor(totalSec / 60);
  const sec = totalSec % 60;
  return `${min}m ${sec.toString().padStart(2, "0")}s`;
}

function displayMeta(dto) {
  const oi = dto.item;
  const parts = [];
  if (oi?.meatTemperature) parts.push(`Meat: ${oi.meatTemperature}`);
  return parts.join(" • ");
}

function renderItemList(items, itemsEl) {
  for (const dto of items) {
    const row = document.createElement("div");
    row.className = "item-row";

    const qty = document.createElement("div");
    qty.className = "qty";
    qty.textContent = `${dto.item.quantity}x`;

    const nameBlock = document.createElement("div");
    nameBlock.className = "nameBlock";

    const name = document.createElement("div");
    name.className = "name";
    name.textContent = displayName(dto);
    nameBlock.appendChild(name);

    const metaText = displayMeta(dto);
    if (metaText) {
      const meta = document.createElement("div");
      meta.className = "meta";
      meta.textContent = metaText;
      nameBlock.appendChild(meta);
    }

    row.appendChild(qty);
    row.appendChild(nameBlock);
    itemsEl.appendChild(row);
  }
}

function openBumpPopover(tableNumber, clickEvent) {
  bumpTargetTable = tableNumber;

  const padding = 8;
  const popW = 140;
  const popH = 50;

  let x = clickEvent.clientX;
  let y = clickEvent.clientY;

  x = Math.min(x, window.innerWidth - popW - padding);
  y = Math.min(y, window.innerHeight - popH - padding);

  bumpPopover.style.left = `${x}px`;
  bumpPopover.style.top = `${y}px`;
  bumpPopover.classList.remove("hidden");
}

function closeBumpPopover() {
  bumpTargetTable = null;
  bumpPopover.classList.add("hidden");
}

document.addEventListener("click", () => {
  closeBumpPopover();
});

bumpPopover.addEventListener("click", (e) => {
  e.stopPropagation();
});

bumpAction.addEventListener("click", async () => {
  if (bumpTargetTable == null) return;

  try {
    await bumpKitchenTicket(bumpTargetTable);

    ticketsByTable.delete(bumpTargetTable);
    const idx = tableQueue.indexOf(bumpTargetTable);
    if (idx !== -1) tableQueue.splice(idx, 1);

    closeBumpPopover();
    render();
  } catch (err) {
    console.error(err);
    alert("Could not bump ticket");
  }
});

function render() {
  boardEl.innerHTML = "";

  for (const tableNumber of tableQueue) {
    const t = ticketsByTable.get(tableNumber);
    if (!t) continue;

    const ticketEl = document.createElement("div");
    ticketEl.className = `ticket ${ageClass(t.createdAtMs)}`;

    ticketEl.style.cursor = "pointer";
    ticketEl.addEventListener("click", (e) => {
      e.stopPropagation();
      openBumpPopover(tableNumber, e);
    });

    const h2 = document.createElement("h2");

    const title = document.createElement("span");
    title.textContent = `Table ${tableNumber}`;

    const subtitle = document.createElement("span");
    subtitle.className = "subtitle";
    const totalCount =
      t.mainItems.length +
      t.addOnBatches.reduce((sum, b) => sum + b.items.length, 0);

    subtitle.textContent = `${totalCount} item${totalCount === 1 ? "" : "s"}`;

    const timer = document.createElement("span");
    timer.className = "timer";
    timer.textContent = formatAge(t.createdAtMs);

    const right = document.createElement("span");
    right.style.display = "flex";
    right.style.gap = "8px";
    right.style.alignItems = "baseline";
    right.appendChild(subtitle);
    right.appendChild(timer);

    h2.appendChild(title);
    h2.appendChild(right);
    ticketEl.appendChild(h2);

    const divider = document.createElement("div");
    divider.className = "divider";
    ticketEl.appendChild(divider);

    const itemsEl = document.createElement("div");
    itemsEl.className = "items";

    // MAIN
    renderItemList(t.mainItems, itemsEl);

    // ADD-ON
    if (t.addOnBatches.length > 0) {
      const addOnDivider = document.createElement("div");
      addOnDivider.className = "divider";
      addOnDivider.style.marginTop = "12px";
      itemsEl.appendChild(addOnDivider);

      const addOnTitle = document.createElement("div");
      addOnTitle.style.fontWeight = "800";
      addOnTitle.style.fontSize = "13px";
      addOnTitle.style.color = "#333";
      addOnTitle.textContent = "ADD-ON";
      itemsEl.appendChild(addOnTitle);

      for (const batch of t.addOnBatches) {
        const ts = new Date(batch.sentAtMs).toLocaleTimeString([], {
          hour: "2-digit",
          minute: "2-digit",
        });

        const batchLabel = document.createElement("div");
        batchLabel.style.fontSize = "12px";
        batchLabel.style.color = "#666";
        batchLabel.style.margin = "6px 0 4px";
        batchLabel.textContent = `Sent ${ts}`;
        itemsEl.appendChild(batchLabel);

        renderItemList(batch.items, itemsEl);
      }
    }

    ticketEl.appendChild(itemsEl);
    boardEl.appendChild(ticketEl);
  }
}

function addUpdatesToState(updates) {
  for (const dto of updates) {
    const tableNumber = dto.tableNumber;

    const nowMs = Date.now();
    const sentAtMs = Date.parse(dto.sentAt);
    const safeSentAtMs = Number.isNaN(sentAtMs) ? nowMs : sentAtMs;

    if (!ticketsByTable.has(tableNumber)) {
      ticketsByTable.set(tableNumber, {
        createdAtMs: safeSentAtMs,
        firstSentAtMs: safeSentAtMs,
        lastSentAtMs: safeSentAtMs,

        mainSentAtMs: safeSentAtMs,
        mainItems: [],
        addOnBatches: [],
      });
      tableQueue.push(tableNumber);
    }

    const ticket = ticketsByTable.get(tableNumber);

    if (safeSentAtMs < ticket.createdAtMs) ticket.createdAtMs = safeSentAtMs;
    if (safeSentAtMs < ticket.firstSentAtMs)
      ticket.firstSentAtMs = safeSentAtMs;
    if (safeSentAtMs > ticket.lastSentAtMs) ticket.lastSentAtMs = safeSentAtMs;

    if (safeSentAtMs < ticket.mainSentAtMs) {
      ticket.mainSentAtMs = safeSentAtMs;
    }

    if (safeSentAtMs === ticket.mainSentAtMs) {
      ticket.mainItems.push(dto);
    } else {
      let batch = ticket.addOnBatches.find((b) => b.sentAtMs === safeSentAtMs);
      if (!batch) {
        batch = { sentAtMs: safeSentAtMs, items: [] };
        ticket.addOnBatches.push(batch);
        ticket.addOnBatches.sort((a, b) => a.sentAtMs - b.sentAtMs);
      }
      batch.items.push(dto);
    }
  }
}

async function poll() {
  const updates = await getKitchenUpdates(since, lastId);

  if (updates.length === 0) return;

  addUpdatesToState(updates);

  for (const dto of updates) {
    const sentAtMs = Date.parse(dto.sentAt);
    if (sentAtMs > since) {
      since = sentAtMs;
      lastId = dto.item.id;
    } else if (sentAtMs === since && dto.item.id > lastId) {
      lastId = dto.item.id;
    }
  }

  render();
}

poll();
setInterval(poll, 10000);
setInterval(render, 1000);
