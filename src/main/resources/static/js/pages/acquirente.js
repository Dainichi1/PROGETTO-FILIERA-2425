// ================== IMPORT ==================
import {toggleUtils} from "../utils/toggle-utils.js";
import {modalUtils} from "../utils/modal-utils.js";
import {crudUtils} from "../utils/crud-utils.js";
import {cartUtils} from "../utils/crud-cart-utils.js";
import {fondiUtils} from "../utils/crud-fondi-utils.js";
import { csrfUtils } from "../utils/csrf-utils.js";

window.toggleUtils = toggleUtils;

// ================== INIT ==================
document.addEventListener("DOMContentLoaded", () => {
    // Carrello + Marketplace
    cartUtils.initMarketplaceToggle();
    cartUtils.initCartToggle();
    cartUtils.initCheckboxActions();
    cartUtils.loadCart();

    // Fondi
    fondiUtils.initFondiForm();

    // Social feed
    document.getElementById("btnSocialFeed")?.addEventListener("click", () => {
        crudUtils.openSocialFeed();
    });

    // ================== ACQUISTO ==================
    const btnAcquista = document.getElementById("btnAcquista");
    if (btnAcquista) {
        btnAcquista.addEventListener("click", () => {
            const selected = cartUtils.getSelectedCartItem();
            if (!selected) {
                modalUtils.openModal("noItemSelectedModal");
                return;
            }
            modalUtils.openModal("paymentChoiceModal");
        });
    }

    // Submit del form pagamento
    const paymentForm = document.getElementById("paymentForm");
    if (paymentForm) {
        paymentForm.addEventListener("submit", e => {
            e.preventDefault();
            const metodo = document.getElementById("paymentMethod").value;
            if (!metodo) {
                alert("⚠ Devi selezionare un metodo di pagamento");
                return;
            }

            const selected = cartUtils.getSelectedCartItem();
            if (!selected) {
                modalUtils.openModal("noItemSelectedModal");
                return;
            }

            modalUtils.closeModal("paymentChoiceModal");
            document.body.dataset.selectedPayment = metodo;

            const detailsEl = document.getElementById("confirmPurchaseDetails");
            if (detailsEl) {
                const row = document.querySelector(
                    `#cartSection tbody tr[data-id="${selected.id}"][data-tipo="${selected.tipo}"]`
                );
                if (row) {
                    const nome = row.children[1]?.innerText || "Item";
                    const quantita = row.querySelector(".cart-quantity-input")?.value || "1";
                    const totale = row.children[4]?.innerText || "€ 0.00";

                    detailsEl.innerText =
                        `Vuoi acquistare "${nome}" (x${quantita}) per un totale di ${totale} ` +
                        `usando ${metodo.replaceAll("_", " ").toLowerCase()}?`;
                }
            }

            modalUtils.openModal("confirmPurchaseModal");
        });
    }

    // ================== Conferma acquisto ==================
    const btnConfirmPurchase = document.getElementById("btnConfirmPurchase");
    if (btnConfirmPurchase) {
        btnConfirmPurchase.addEventListener("click", () => {
            const selected = cartUtils.getSelectedCartItem();
            const metodo = document.body.dataset.selectedPayment;

            if (!selected || !metodo) {
                modalUtils.openModal("purchaseErrorModal");
                return;
            }

            // Calcolo totale lato frontend
            const totale = selected.quantita * selected.prezzoUnitario;

            // Costruzione payload
            const payload = {
                totaleAcquisto: totale,
                tipoMetodoPagamento: metodo.toUpperCase(),
                items: [
                    {
                        id: selected.id,
                        tipo: selected.tipo.toUpperCase(),
                        nome: selected.nome,
                        quantita: selected.quantita,
                        prezzoUnitario: selected.prezzoUnitario,
                        totale: totale
                    }
                ]
            };

            // LOG per debug
            console.log(">>> Payload inviato al backend:", JSON.stringify(payload, null, 2));

            const csrf = csrfUtils.getCsrf();

            fetch("/acquirente/conferma-acquisto", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    [csrf.header]: csrf.token
                },
                body: JSON.stringify(payload)
            })
                .then(r => r.json())
                .then(data => {
                    modalUtils.closeModal("confirmPurchaseModal");

                    if (data.success) {
                        const msg = document.getElementById("purchaseSuccessMessage");
                        if (msg) msg.innerText = data.message || "✅ Acquisto completato con successo!";
                        modalUtils.openModal("purchaseSuccessModal");

                        // Svuota carrello frontend
                        cartUtils.updateCartTable([], {});
                        document.getElementById("btnAcquista")?.setAttribute("disabled", "disabled");

                        // (opzionale) ricarica marketplace dal backend
                        cartUtils.loadCart();
                    } else {
                        const msg = document.getElementById("purchaseErrorMessage");
                        if (msg) msg.innerText = data.message || "❌ Errore durante l’acquisto.";
                        modalUtils.openModal("purchaseErrorModal");
                    }
                })
                .catch(() => modalUtils.openModal("purchaseErrorModal"));
        });
    }

    // ================== Gestione errore pagamento ==================
    const purchaseErrorModal = document.getElementById("purchaseErrorModal");
    if (purchaseErrorModal) {
        purchaseErrorModal.addEventListener("hidden.bs.modal", () => {
            // Torna alla modale di scelta metodo pagamento
            modalUtils.openModal("paymentChoiceModal");
        });
    }

    // ================== Chiusura modali generiche ==================
    document.querySelectorAll(".btn-close-modal").forEach(btn => {
        btn.addEventListener("click", e => {
            e.stopPropagation();
            const target = btn.dataset.target;
            if (target) modalUtils.closeModal(target);
        });
    });
});
