import { getAllTables } from "../services/restaurantTableApi.js";

let dom;
let lastGrid = null;

window.addEventListener("DOMContentLoaded", initRestaurantTableController);

async function initRestaurantTableController() {
  const storedEmployee = sessionStorage.getItem("currentEmployee");
  if (!storedEmployee) {
    // ingen login -> send til login
    window.location.href = "login.html";
    return;
  }

  const employee = JSON.parse(storedEmployee);
  console.log("Logged in as:", employee.name, "role:", employee.role);

  dom = mapDomElements();
  await reloadAndRender();
}

function mapDomElements() {
  return {
    floorBody: document.querySelector("#floorBody"),
    floorViewport: document.querySelector("#floorViewport"),
  };
}

async function reloadAndRender() {
  const tables = await getAllTables();
  const grid = buildGrid(tables);
  lastGrid = grid;

  renderGrid(grid);
  updateCellSize(grid);
}

// ----- Grid building -----
function buildGrid(tables) {
  if ((!tables || tables.length === 0) && (!walls || walls.length === 0))
    return [];

  const maxRow = Math.max(
    ...(tables?.map((t) => t.rowStart + t.height) || [0]),
    ...(walls?.map((w) => w.row + 1) || [0])
  );

  const maxCol = Math.max(
    ...(tables?.map((t) => t.colStart + t.width) || [0]),
    ...(walls?.map((w) => w.col + 1) || [0])
  );

  const grid = Array.from({ length: maxRow }, () =>
    Array.from({ length: maxCol }, () => null)
  );

  // ----- Tables -----
  tables.forEach((t) => {
    for (let r = t.rowStart; r < t.rowStart + t.height; r++) {
      for (let c = t.colStart; c < t.colStart + t.width; c++) {
        if (r === t.rowStart && c === t.colStart) {
          grid[r][c] = { type: "table", table: t };
        } else {
          grid[r][c] = { type: "covered" };
        }
      }
    }
  });

  // ----- Walls (hardcoded) -----
  walls.forEach((w) => {
    if (!grid[w.row][w.col]) {
      grid[w.row][w.col] = {
        type: "wall",
        orientation: w.orientation,
      };
    }
  });

  return grid;
}

// ----- Rendering -----
function renderGrid(grid) {
  const tbody = dom.floorBody;
  tbody.innerHTML = "";

  if (!grid || grid.length === 0) {
    const tr = document.createElement("tr");
    const td = document.createElement("td");
    td.textContent = "Ingen borde.";
    td.classList.add("empty-cell");
    tr.appendChild(td);
    tbody.appendChild(tr);
    return;
  }

  for (let r = 0; r < grid.length; r++) {
    const tr = document.createElement("tr");

    for (let c = 0; c < grid[r].length; c++) {
      const cell = grid[r][c];

      if (cell && cell.type === "covered") continue;

      const td = document.createElement("td");

      // ----- TABLE -----
      if (cell && cell.type === "table") {
        const t = cell.table;
        const status = (t.status || "FREE").toUpperCase();

        if (status === "BLOCKED") {
          td.classList.add("blocked-cell");
        } else {
          td.textContent = t.tableNumber;
          td.classList.add("table-cell", "status-" + status.toLowerCase());
          td.addEventListener("click", () => handleTableClick(t));
        }

        td.rowSpan = t.height;
        td.colSpan = t.width;
      }

      // ----- WALL -----
      else if (cell && cell.type === "wall") {
        const o = cell.orientation.toLowerCase();
        td.classList.add("wall-cell");

        if (o === "vertical") td.classList.add("wall-vertical");
        else if (o === "horizontal") td.classList.add("wall-horizontal");
        else if (o === "tjunction") td.classList.add("wall-tjunction");
        else if (o === "t-up") td.classList.add("wall-t-up");
        else if (o === "t-down") td.classList.add("wall-t-down");
        else if (o === "t-left") td.classList.add("wall-t-left");
        else if (o === "t-right") td.classList.add("wall-t-right");
      }

      // ----- EMPTY -----
      else {
        td.classList.add("empty-cell");
      }

      tr.appendChild(td);
    }

    tbody.appendChild(tr);
  }
}

// ----- Dynamic cellesize -----
function updateCellSize(grid) {
  if (!grid || grid.length === 0) return;

  const rows = grid.length;
  const cols = grid[0].length || 1;

  const viewport = dom.floorViewport;
  if (!viewport) return;

  const rect = viewport.getBoundingClientRect();
  const maxCellWidth = rect.width / cols;
  const maxCellHeight = rect.height / rows;

  const cellSize = Math.floor(Math.min(maxCellWidth, maxCellHeight));

  document.documentElement.style.setProperty("--cell-size", `${cellSize}px`);
}

function handleTableClick(table) {
  console.log("Table clicked:", table.tableNumber, "status:", table.status);
  
  const status = (table.status || "FREE").toUpperCase();

  if(status === "BLOCKED"){
    alert("Dette bord er blokeret og kan ikke åbnet.")
    return;
  }

 window.location.href = `guest-selection.html?tableNumber=${table.tableNumber}`;

}

// ----- Hardcoded walls -----
const walls = [
  { row: 2, col: 16, orientation: "vertical" },
  { row: 3, col: 16, orientation: "t-up" },
  { row: 3, col: 14, orientation: "Horizontal" },
  { row: 3, col: 15, orientation: "Horizontal" },
  { row: 3, col: 17, orientation: "Horizontal" },
  { row: 3, col: 18, orientation: "Horizontal" },

  { row: 3, col: 26, orientation: "horizontal" },
  { row: 3, col: 27, orientation: "horizontal" },
  { row: 3, col: 28, orientation: "t-left" },
  { row: 2, col: 28, orientation: "vertical" },
  { row: 4, col: 28, orientation: "vertical" },
];
