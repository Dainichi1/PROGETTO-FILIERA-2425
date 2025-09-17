// ================== IMPORT ==================
import { toggleUtils } from "../utils/toggle-utils.js";
import { modalUtils } from "../utils/modal-utils.js";
import { csrfUtils } from "../utils/csrf-utils.js";

window.toggleUtils = toggleUtils;

// ================== VALIDAZIONE ITEM CARRELLO ==================
function clientValidateCartItem(quantita, disponibilita) {
    if (Number.isNaN(quantita) || quantita <= 0) {
        modalUtils.openModal("quantityErrorModal");
        return false;
    }
    if (disponibilita <= 0) {
        modalUtils.openModal("availabilityErrorModal");
        return false;
    }
    if (quantita > disponibilita) {
        modalUtils.openModal("availabilityErrorModal");
        return false;
    }
    return true;
}

// ================== FUNZIONI SPECIFICHE ACQUIRENTE ==================

// Toggle sezione marketplace
function initMarketplaceToggle() {
    const btnMarketplace = document.getElementById("btnVisualizzaMarketplace");
    const btnChiudi = document.getElementById("btnChiudiMarketplace");
    const marketplaceSection = document.getElementById("marketplaceSection");

    if (btnMarketplace && marketplaceSection) {
        btnMarketplace.addEventListener("click", () => {
            marketplaceSection.style.display = "block";
            initCheckboxActions();
        });
    }

    if (btnChiudi && marketplaceSection) {
        btnChiudi.addEventListener("click", () => {
            marketplaceSection.style.display = "none";
        });
    }
}

// Toggle sezione carrello
function initCartToggle() {
    const btnCarrello = document.getElementById("btnVisualizzaCarrello");
    const btnChiudi = document.getElementById("btnChiudiCarrello");
    const cartSection = document.getElementById("cartSection");

    if (btnCarrello && cartSection) {
        btnCarrello.addEventListener("click", () => {
            cartSection.style.display = "block";
        });
    }

    if (btnChiudi && cartSection) {
        btnChiudi.addEventListener("click", () => {
            cartSection.style.display = "none";
        });
    }
}

// Gestione checkbox → mostra bottone "Aggiungi al carrello"
function initCheckboxActions() {
    document.querySelectorAll(".select-checkbox").forEach(cb => {
        cb.addEventListener("change", function () {
            const row = this.closest("tr");
            const actionCell = row.querySelector(".action-cell");

            if (this.checked) {
                actionCell.innerHTML =
                    '<button type="button" class="btn-add-cart">Aggiungi al carrello</button>';
                attachAddToCartHandler(actionCell.querySelector(".btn-add-cart"));
            } else {
                actionCell.innerHTML = "";
            }
        });
    });
}

// ================== GESTIONE "AGGIUNGI AL CARRELLO" ==================
function attachAddToCartHandler(button) {
    button.addEventListener("click", () => {
        const row = button.closest("tr");
        const tipo = (row.getAttribute("data-tipo") || "").trim().toUpperCase();
        const id = Number(row.getAttribute("data-id"));
        const disponibilita = Number(row.getAttribute("data-disponibilita"));
        const quantitaInput = row.querySelector(".quantita-input");
        const quantita = quantitaInput ? Number(quantitaInput.value) : 1;

        if (!clientValidateCartItem(quantita, disponibilita)) {
            return;
        }

        const csrf = csrfUtils.getCsrf();

        fetch("/carrello/aggiungi", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                [csrf.header]: csrf.token
            },
            body: JSON.stringify({ tipo, id, quantita })
        })
            .then(r => r.json())
            .then(data => {
                if (data.success) {
                    console.log("✅ Item aggiunto al carrello", data.items);
                    updateCartTable(data.items, data.totali);

                    // Modale di successo
                    const successMsg = document.getElementById("successAddMessage");
                    if (successMsg) {
                        successMsg.innerText = data.message || "✅ Item aggiunto al carrello!";
                    }
                    modalUtils.openModal("successAddModal");

                    // Aggiorna disponibilità nel marketplace
                    const updatedItem = data.items.find(i => i.id === id && i.tipo === tipo);
                    if (updatedItem) {
                        const marketRow = document.querySelector(
                            `tr[data-id="${id}"][data-tipo="${tipo}"]`
                        );
                        if (marketRow) {
                            const dispCell = marketRow.querySelector("td:nth-child(6)");
                            const cb = marketRow.querySelector(".select-checkbox");
                            const qtyInput = marketRow.querySelector(".quantita-input");
                            const actionCell = marketRow.querySelector(".action-cell");

                            if (dispCell) {
                                dispCell.innerText = updatedItem.disponibilita;
                            }
                            marketRow.setAttribute(
                                "data-disponibilita",
                                updatedItem.disponibilita
                            );

                            if (updatedItem.disponibilita <= 0) {
                                if (cb) { cb.checked = false; cb.disabled = true; }
                                if (qtyInput) { qtyInput.value = 0; qtyInput.disabled = true; }
                                if (actionCell) { actionCell.innerHTML = '<span style="font-style:italic;">Esaurito</span>'; }
                            }
                        }
                    }

                } else {
                    handleCartError(data.message || "Errore generico");
                }
            })
            .catch(() => modalUtils.openModal("genericErrorModal"));
    });
}

// ================== UPDATE TABELLA CARRELLO ==================
function updateCartTable(items, totali) {
    const tbody = document.querySelector("#cartSection table tbody");
    if (!tbody) return;

    tbody.innerHTML = "";

    if (!items || items.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" style="text-align:center; font-style: italic;">
                    Il carrello è vuoto.
                </td>
            </tr>`;
        return;
    }

    items.forEach(item => {
        tbody.innerHTML += `
            <tr data-id="${item.id}" data-tipo="${item.tipo}" data-disponibilita="${item.disponibilita}">
                <td>${item.tipo}</td>
                <td>${item.nome}</td>
                <td>${item.quantita}</td>
                <td>€ ${item.prezzoUnitario.toFixed(2)}</td>
                <td>€ ${item.totale.toFixed(2)}</td>
                <td><button class="btn-update">Aggiorna</button></td>
                <td><button class="btn-delete">Elimina</button></td>
            </tr>`;
    });

    // Handler aggiorna
    tbody.querySelectorAll(".btn-update").forEach(btn => {
        btn.addEventListener("click", () => {
            const row = btn.closest("tr");
            console.log("TODO: aggiorna item", row.dataset);
            // TODO: fetch POST /carrello/aggiorna
        });
    });

    // Handler elimina
    tbody.querySelectorAll(".btn-delete").forEach(btn => {
        btn.addEventListener("click", () => {
            const row = btn.closest("tr");
            console.log("TODO: elimina item", row.dataset);
            // TODO: fetch POST /carrello/rimuovi
        });
    });

    // Aggiorna totali
    if (totali) {
        const totEl = document.getElementById("cartTotals");
        if (totEl) {
            totEl.innerText = `Totale articoli: ${totali.totaleArticoli}, Costo: € ${totali.costoTotale.toFixed(2)}`;
        }
    }
}

// ================== SYNC CARRELLO -> MARKETPLACE ==================
function applyCartToMarketplace(items) {
    items.forEach(i => {
        const row = document.querySelector(`tr[data-id="${i.id}"][data-tipo="${i.tipo}"]`);
        if (!row) return;

        const dispCell = row.querySelector("td:nth-child(6)");
        const cb = row.querySelector(".select-checkbox");
        const qtyInput = row.querySelector(".quantita-input");
        const actionCell = row.querySelector(".action-cell");

        if (dispCell) {
            dispCell.innerText = i.disponibilita;
        }
        row.setAttribute("data-disponibilita", i.disponibilita);

        if (i.disponibilita <= 0) {
            if (cb) { cb.checked = false; cb.disabled = true; }
            if (qtyInput) { qtyInput.value = 0; qtyInput.disabled = true; }
            if (actionCell) { actionCell.innerHTML = '<span style="font-style:italic;">Esaurito</span>'; }
        }
    });
}

// ================== CARICAMENTO CARRELLO ALL'AVVIO ==================
function loadCart() {
    fetch("/carrello", {
        method: "GET",
        headers: { "Content-Type": "application/json" }
    })
        .then(r => r.json())
        .then(data => {
            if (data.success) {
                updateCartTable(data.items, data.totali);
                applyCartToMarketplace(data.items);
            }
        })
        .catch(() => console.error("Errore durante il caricamento del carrello"));
}

// ================== ERROR HANDLER ==================
function handleCartError(msg) {
    if (!msg) {
        modalUtils.openModal("genericErrorModal");
        return;
    }
    if (msg.includes("maggiore di 0")) {
        modalUtils.openModal("quantityErrorModal");
    } else if (msg.includes("superiore alla disponibilità")) {
        modalUtils.openModal("availabilityErrorModal");
    } else {
        modalUtils.openModal("genericErrorModal");
    }
}

// ================== INIZIALIZZAZIONE ==================
document.addEventListener("DOMContentLoaded", () => {
    initMarketplaceToggle();
    initCartToggle();
    initCheckboxActions();
    loadCart(); // carica il carrello già presente in sessione

    // Gestione chiusura modali
    document.querySelectorAll(".btn-close-modal").forEach(btn => {
        btn.addEventListener("click", e => {
            e.stopPropagation();
            const target = btn.getAttribute("data-target");
            if (target) {
                modalUtils.closeModal(target);
            }
        });
    });
});
