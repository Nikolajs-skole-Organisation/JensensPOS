import { createTicketBoard } from "./ticketBoard.js";
import { getBarUpdates, bumpBarTicket } from "../services/barApi.js";

createTicketBoard({
  boardEl: document.getElementById("board"),
  getUpdates: getBarUpdates,
  bumpTicket: bumpBarTicket,
  filterItem: (dto) => dto.item?.drinkItem != null,
});
