export function createTicketBoard({
  boardEl,
  getUpdates, // (since, lastId) => Promise<dto[]>
  bumpTicket, // (tableNumber) => Promise<void>
  filterItem, // dto => boolean   (food vs drink)
  headerTitle = "Board",
}) {
  const tableQueue = [];
  const ticketsByTable = new Map();
  let since = 0;
  let lastId = 0;

  // popover
  const popover = document.getElementById("bump-popover");
  const bumpBtn = document.getElementById("bump-action");
  let bumpTargetTable = null;

  function displayName(dto) {
    const oi = dto.item;
    if (oi?.foodItem) return oi.foodItem.name;
    if (oi?.drinkItem) return oi.drinkItem.name;
    return "(unknown)";
  }

  function displayMeta(dto) {
    const oi = dto.item;
    const parts = [];
    if (oi?.meatTemperature) parts.push(`Meat: ${oi.meatTemperature}`);
    return parts.join(" • ");
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

  function openPopover(tableNumber, e) {
    bumpTargetTable = tableNumber;

    const padding = 8;
    const popW = 140;
    const popH = 50;

    let x = Math.min(e.clientX, window.innerWidth - popW - padding);
    let y = Math.min(e.clientY, window.innerHeight - popH - padding);

    popover.style.left = `${x}px`;
    popover.style.top = `${y}px`;
    popover.classList.remove("hidden");
  }

  function closePopover() {
    bumpTargetTable = null;
    popover.classList.add("hidden");
  }

  if (popover && bumpBtn) {
    document.addEventListener("click", closePopover);
    popover.addEventListener("click", (e) => e.stopPropagation());

    bumpBtn.addEventListener("click", async () => {
      if (bumpTargetTable == null) return;
      await bumpTicket(bumpTargetTable);

      ticketsByTable.delete(bumpTargetTable);
      const idx = tableQueue.indexOf(bumpTargetTable);
      if (idx !== -1) tableQueue.splice(idx, 1);

      closePopover();
      render();
    });
  }

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
        openPopover(tableNumber, e);
      });

      const h2 = document.createElement("h2");
      const title = document.createElement("span");
      title.textContent = `Table ${tableNumber}`;

      const subtitle = document.createElement("span");
      subtitle.className = "subtitle";
      const totalCount =
        t.mainItems.length +
        t.addOnBatches.reduce((s, b) => s + b.items.length, 0);
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

      renderItemList(t.mainItems, itemsEl);

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
      if (!filterItem(dto)) continue;

      const tableNumber = dto.tableNumber;
      const nowMs = Date.now();
      const sentAtMs = Date.parse(dto.sentAt);
      const safeSentAtMs = Number.isNaN(sentAtMs) ? nowMs : sentAtMs;

      if (!ticketsByTable.has(tableNumber)) {
        ticketsByTable.set(tableNumber, {
          createdAtMs: safeSentAtMs,
          mainSentAtMs: safeSentAtMs,
          mainItems: [],
          addOnBatches: [],
        });
        tableQueue.push(tableNumber);
      }

      const ticket = ticketsByTable.get(tableNumber);

      if (safeSentAtMs < ticket.createdAtMs) ticket.createdAtMs = safeSentAtMs;
      if (safeSentAtMs < ticket.mainSentAtMs)
        ticket.mainSentAtMs = safeSentAtMs;

      if (safeSentAtMs === ticket.mainSentAtMs) {
        ticket.mainItems.push(dto);
      } else {
        let batch = ticket.addOnBatches.find(
          (b) => b.sentAtMs === safeSentAtMs
        );
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
    const updates = await getUpdates(since, lastId);
    if (!updates || updates.length === 0) return;

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

  // start
  poll();
  setInterval(poll, 10000);
  setInterval(render, 1000);
}
