import { createTicketBoard } from "./ticketBoard";
import {
  getKitchenUpdates,
  bumpKitchenTicket,
} from "../services/kitchenApi.js";

createTicketBoard({
  boardEl: document.getElementById("board"),
  getUpdates: getKitchenUpdates,
  bumpTicket: bumpKitchenTicket,
  filterItem: (dto) => dto.item?.foodItem != null,
});
