// ================== IMPORT ==================
import {toggleUtils} from "../utils/toggle-utils.js";
import {modalUtils} from "../utils/modal-utils.js";
import {crudUtils} from "../utils/crud-utils.js";
import {cartUtils} from "../utils/crud-cart-utils.js";
import {fondiUtils} from "../utils/crud-fondi-utils.js";

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

            // Chiudi modale scelta
            modalUtils.closeModal("paymentChoiceModal");

            // Salva metodo scelto in dataset globale
            document.body.dataset.selectedPayment = metodo;

            // Apri modale conferma
            modalUtils.openModal("confirmPurchaseModal");
        });
    }

    // Chiusura modali generiche
    document.querySelectorAll(".btn-close-modal").forEach(btn => {
        btn.addEventListener("click", e => {
            e.stopPropagation();
            const target = btn.dataset.target;
            if (target) modalUtils.closeModal(target);
        });
    });
});
