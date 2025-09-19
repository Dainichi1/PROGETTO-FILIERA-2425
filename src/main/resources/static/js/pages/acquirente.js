// ================== IMPORT ==================
import {toggleUtils} from "../utils/toggle-utils.js";
import {modalUtils} from "../utils/modal-utils.js";
import {csrfUtils} from "../utils/csrf-utils.js";
import {crudUtils} from "../utils/crud-utils.js";

window.toggleUtils = toggleUtils;

// ================== VALIDAZIONE ==================
function clientValidateCartItem(quantita, allowZero = false) {
    if (Number.isNaN(quantita) || quantita < 0) {
        modalUtils.openModal("quantityErrorModal");
        return false;
    }
    if (!allowZero && quantita === 0) {
        modalUtils.openModal("quantityErrorModal");
        return false;
    }
    return true;
}

// ================== TOGGLE SEZIONI ==================
function initMarketplaceToggle() {
    const btnMarketplace = document.getElementById("btnVisualizzaMarketplace");
    const btnChiudi = document.getElementById("btnChiudiMarketplace");
    const section = document.getElementById("marketplaceSection");

    if (btnMarketplace && section) {
        btnMarketplace.addEventListener("click", () => {
            section.style.display = "block";
            initCheckboxActions();
        });
    }
    if (btnChiudi && section) {
        btnChiudi.addEventListener("click", () => {
            section.style.display = "none";
        });
    }
}

function initCartToggle() {
    const btnCarrello = document.getElementById("btnVisualizzaCarrello");
    const btnChiudi = document.getElementById("btnChiudiCarrello");
    const section = document.getElementById("cartSection");

    if (btnCarrello && section) {
        btnCarrello.addEventListener("click", () => {
            section.style.display = "block";
        });
    }
    if (btnChiudi && section) {
        btnChiudi.addEventListener("click", () => {
            section.style.display = "none";
        });
    }
}

// ================== MARKETPLACE: CHECKBOX ==================
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

// ================== MARKETPLACE: AGGIUNTA AL CARRELLO ==================
function attachAddToCartHandler(button) {
    button.addEventListener("click", () => {
        const row = button.closest("tr");
        const tipo = (row.dataset.tipo || "").trim().toUpperCase();
        const id = Number(row.dataset.id);
        const quantitaInput = row.querySelector(".quantita-input");
        const quantita = quantitaInput ? Number(quantitaInput.value) : 1;

        if (!clientValidateCartItem(quantita, false)) return;

        const csrf = csrfUtils.getCsrf();

        fetch("/carrello/aggiungi", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                [csrf.header]: csrf.token
            },
            body: JSON.stringify({tipo, id, quantita})
        })
            .then(r => r.json())
            .then(data => {
                if (data.success) {
                    updateCartTable(data.items, data.totali);
                    applyCartToMarketplace(data.items);

                    // Mostra modale di successo
                    const msgEl = document.getElementById("successAddMessage");
                    if (msgEl) {
                        msgEl.innerText = `✅ ${data.items[0].nome} (${data.items[0].quantita}) aggiunto al carrello!`;
                    }
                    modalUtils.openModal("successAddModal");

                } else {
                    handleCartError(data.message || "Errore generico");
                }
            })
            .catch(() => modalUtils.openModal("genericErrorModal"));
    });
}

// ================== CARRELLO: UPDATE TABELLA ==================
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
            <td>
                <input 
                    type="number" 
                    class="cart-quantity-input" 
                    value="${item.quantita}" 
                    min="0" 
                    max="${item.quantita + item.disponibilita}" 
                    style="width: 60px; text-align: center;" 
                />
            </td>
            <td>€ ${item.prezzoUnitario.toFixed(2)}</td>
            <td>€ ${item.totale.toFixed(2)}</td>
            <td><button class="btn-update">Aggiorna</button></td>
            <td><button class="btn-delete">Elimina</button></td>
        </tr>`;
    });

    attachCartHandlers();
}

// ================== CARRELLO: HANDLER AGGIORNA/ELIMINA ==================
function attachCartHandlers() {
    const tbody = document.querySelector("#cartSection table tbody");

    // Aggiorna
    tbody.querySelectorAll(".btn-update").forEach(btn => {
        btn.addEventListener("click", () => {
            const row = btn.closest("tr");
            const id = Number(row.dataset.id);
            const tipo = row.dataset.tipo;
            const input = row.querySelector(".cart-quantity-input");
            const nuovaQuantita = Number(input.value);

            if (!clientValidateCartItem(nuovaQuantita, true)) return;

            const csrf = csrfUtils.getCsrf();

            fetch("/carrello/aggiorna", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    [csrf.header]: csrf.token
                },
                body: JSON.stringify({id, tipo: tipo.toUpperCase(), nuovaQuantita})
            })
                .then(r => r.json())
                .then(data => {
                    if (data.success) {
                        updateCartTable(data.items, data.totali);
                        applyCartToMarketplace(data.items);

                        if (data.removedItem) {
                            updateMarketplaceRowRemoved(data.removedItem);
                        }
                    } else {
                        handleCartError(data.message || "Errore generico");
                    }
                })
                .catch(() => modalUtils.openModal("genericErrorModal"));
        });
    });

    // Elimina
    tbody.querySelectorAll(".btn-delete").forEach(btn => {
        btn.addEventListener("click", () => {
            const row = btn.closest("tr");
            const id = Number(row.dataset.id);
            const tipo = row.dataset.tipo;

            const csrf = csrfUtils.getCsrf();

            fetch("/carrello/rimuovi", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    [csrf.header]: csrf.token
                },
                body: JSON.stringify({id, tipo})
            })
                .then(r => r.json())
                .then(data => {
                    if (data.success) {
                        updateCartTable(data.items, data.totali);
                        applyCartToMarketplace(data.items);

                        if (data.removedItem) {
                            updateMarketplaceRowRemoved(data.removedItem);
                        }
                    } else {
                        handleCartError(data.message || "Errore generico");
                    }
                })
                .catch(() => modalUtils.openModal("genericErrorModal"));
        });
    });
}

// ================== RIPRISTINA RIGA MARKETPLACE (da removedItem) ==================
function updateMarketplaceRowRemoved(removedItem) {
    const row = document.querySelector(`tr[data-id="${removedItem.id}"][data-tipo="${removedItem.tipo}"]`);
    if (!row) return;

    const dispCell = row.querySelector("td:nth-child(6)");
    const cb = row.querySelector(".select-checkbox");
    const qtyInput = row.querySelector(".quantita-input");
    const actionCell = row.querySelector(".action-cell");

    if (dispCell) dispCell.innerText = removedItem.disponibilita;
    row.dataset.disponibilita = removedItem.disponibilita;

    if (removedItem.disponibilita > 0) {
        if (cb) cb.disabled = false;
        if (qtyInput) qtyInput.disabled = false;
        if (actionCell) {
            actionCell.innerHTML =
                '<button type="button" class="btn-add-cart">Aggiungi al carrello</button>';
            attachAddToCartHandler(actionCell.querySelector(".btn-add-cart"));
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

        if (dispCell) dispCell.innerText = i.disponibilita;
        row.dataset.disponibilita = i.disponibilita;

        if (i.disponibilita <= 0) {
            if (cb) { cb.checked = false; cb.disabled = true; }
            if (qtyInput) { qtyInput.value = 0; qtyInput.disabled = true; }
            if (actionCell) actionCell.innerHTML = '<span style="font-style:italic;">Esaurito</span>';
        } else {
            if (cb) cb.disabled = false;
            if (qtyInput) qtyInput.disabled = false;
            if (actionCell && !actionCell.querySelector(".btn-add-cart")) {
                actionCell.innerHTML =
                    '<button type="button" class="btn-add-cart">Aggiungi al carrello</button>';
                attachAddToCartHandler(actionCell.querySelector(".btn-add-cart"));
            }
        }
    });
}

// ================== CARICAMENTO INIZIALE ==================
function loadCart() {
    fetch("/carrello", {
        method: "GET",
        headers: {"Content-Type": "application/json"}
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
    if (msg.includes("disponibilità")) {
        modalUtils.openModal("availabilityErrorModal");
    } else {
        modalUtils.openModal("genericErrorModal");
    }
}

// ================== INIT ==================
document.addEventListener("DOMContentLoaded", () => {
    initMarketplaceToggle();
    initCartToggle();
    initCheckboxActions();
    loadCart();

    // Usa lo stesso social feed degli altri attori
    document.getElementById("btnSocialFeed")?.addEventListener("click", () => {
        crudUtils.openSocialFeed();
    });

    document.querySelectorAll(".btn-close-modal").forEach(btn => {
        btn.addEventListener("click", e => {
            e.stopPropagation();
            const target = btn.dataset.target;
            if (target) modalUtils.closeModal(target);
        });
    });
});
