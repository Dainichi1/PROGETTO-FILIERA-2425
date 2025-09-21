// ================== IMPORT ==================
import { toggleUtils } from "../utils/toggle-utils.js";
import { modalUtils } from "../utils/modal-utils.js";
import { crudUtils } from "../utils/crud-utils.js";
import { cartUtils } from "../utils/crud-cart-utils.js";
import { fondiUtils } from "../utils/crud-fondi-utils.js";
import { csrfUtils } from "../utils/csrf-utils.js";
import { formUtils } from "../utils/form-utils.js";

window.toggleUtils = toggleUtils;

// ================== INIT ==================
document.addEventListener("DOMContentLoaded", () => {

    // ================== CARRELLO + MARKETPLACE ==================
    cartUtils.initMarketplaceToggle();
    cartUtils.initCartToggle();
    cartUtils.initCheckboxActions();
    cartUtils.loadCart();

    // ================== FONDI ==================
    fondiUtils.initFondiForm();

    // ================== SOCIAL FEED ==================
    document.getElementById("btnSocialFeed")?.addEventListener("click", () => {
        crudUtils.openSocialFeed();
    });

    // ================== ACQUISTI ==================
    const acquistiSection = document.getElementById("acquistiSection");
    const btnVisualizzaAcquisti = document.getElementById("btnVisualizzaAcquisti");
    const btnChiudiAcquisti = document.getElementById("btnChiudiAcquisti");

    if (btnVisualizzaAcquisti && acquistiSection) {
        btnVisualizzaAcquisti.addEventListener("click", () => {
            toggleUtils.show(acquistiSection);
        });
    }

    if (btnChiudiAcquisti && acquistiSection) {
        btnChiudiAcquisti.addEventListener("click", () => {
            toggleUtils.hide(acquistiSection);
        });
    }

    // ================== PULSANTE RECENSIONE ==================
    document.querySelectorAll(".btn-pubblica-recensione").forEach(btn => {
        btn.addEventListener("click", () => {
            const id = btn.dataset.id;      // acquistoId
            const stato = btn.dataset.stato;

            console.log(">>> Pubblica recensione per acquisto ID:", id, "stato:", stato);

            // Salvo i dati nel body
            document.body.dataset.recensioneId = id;
            document.body.dataset.recensioneStato = stato;

            // Reset campi
            const titleEl = document.getElementById("recensioneTitle");
            const textEl = document.getElementById("recensioneText");
            if (titleEl) {
                titleEl.value = "";
                formUtils.clearFieldError(titleEl);
            }
            if (textEl) {
                textEl.value = "";
                formUtils.clearFieldError(textEl);
            }

            // Apro la modale recensione
            modalUtils.openModal("recensionePostModal");
        });
    });

    // ================== PUBBLICAZIONE RECENSIONI ==================
    document.getElementById("btnOkRecensionePost")?.addEventListener("click", () => {
        const titleEl = document.getElementById("recensioneTitle");
        const textEl = document.getElementById("recensioneText");

        let ok = true;
        formUtils.clearFieldError(titleEl);
        formUtils.clearFieldError(textEl);

        if (!titleEl.value.trim()) {
            formUtils.setFieldError("recensioneTitle", "⚠ Il titolo è obbligatorio");
            ok = false;
        }
        if (!textEl.value.trim()) {
            formUtils.setFieldError("recensioneText", "⚠ Il testo è obbligatorio");
            ok = false;
        }

        if (!ok) return;

        // messaggio di conferma
        const confirmMsg = document.getElementById("recensioneConfirmMessage");
        if (confirmMsg) {
            confirmMsg.innerText = "Sei sicuro di voler pubblicare la recensione sull’acquisto?";
        }

        modalUtils.closeModal("recensionePostModal");
        modalUtils.openModal("recensioneConfirmModal");
    });

    document.getElementById("btnConfirmRecensionePost")?.addEventListener("click", () => {
        const acquistoId = document.body.dataset.recensioneId || null;
        const titolo = document.getElementById("recensioneTitle")?.value.trim();
        const testo = document.getElementById("recensioneText")?.value.trim();

        if (!acquistoId) {
            const errMsg = document.getElementById("recensioneErrorMessage");
            if (errMsg) errMsg.innerText = "❌ Errore: ID acquisto non trovato.";
            modalUtils.openModal("recensioneErrorModal");
            return;
        }

        const csrf = csrfUtils.getCsrf();
        const payload = { titolo, testo };

        const url = `/api/social/pubblica-recensione/${acquistoId}`;
        console.log(">>> Invio recensione al backend:", payload, "URL:", url);

        fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                [csrf.header]: csrf.token
            },
            body: JSON.stringify(payload)
        })
            .then(r => {
                if (!r.ok) throw new Error("Errore HTTP " + r.status);
                return r.json();
            })
            .then(data => {
                modalUtils.closeModal("recensioneConfirmModal");

                if (data && data.id) {
                    const successMsg = document.getElementById("recensioneSuccessMessage");
                    if (successMsg) {
                        successMsg.innerText = "✅ Recensione pubblicata con successo!";
                    }
                    modalUtils.openModal("recensioneSuccessModal");
                } else {
                    const errMsg = document.getElementById("recensioneErrorMessage");
                    if (errMsg) {
                        errMsg.innerText = "❌ Impossibile pubblicare la recensione.";
                    }
                    modalUtils.openModal("recensioneErrorModal");
                }
            })
            .catch(err => {
                const errMsg = document.getElementById("recensioneErrorMessage");
                if (errMsg) {
                    errMsg.innerText = "❌ Errore imprevisto: " + err.message;
                }
                modalUtils.openModal("recensioneErrorModal");
            });
    });

    // ================== LIVE VALIDATION RECENSIONI ==================
    ["recensioneTitle", "recensioneText"].forEach(id => {
        const field = document.getElementById(id);
        if (field) {
            field.addEventListener("input", () => formUtils.clearFieldError(field));
            field.addEventListener("change", () => formUtils.clearFieldError(field));
        }
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

    // ================== SUBMIT PAGAMENTO ==================
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

    // ================== CONFERMA ACQUISTO ==================
    const btnConfirmPurchase = document.getElementById("btnConfirmPurchase");
    if (btnConfirmPurchase) {
        btnConfirmPurchase.addEventListener("click", () => {
            const selected = cartUtils.getSelectedCartItem();
            const metodo = document.body.dataset.selectedPayment;

            if (!selected || !metodo) {
                modalUtils.openModal("purchaseErrorModal");
                return;
            }

            const totale = selected.quantita * selected.prezzoUnitario;

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

            console.log(">>> Payload acquisto:", JSON.stringify(payload, null, 2));

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

                        cartUtils.updateCartTable([], {});
                        document.getElementById("btnAcquista")?.setAttribute("disabled", "disabled");

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

    // ================== GESTIONE ERRORE PAGAMENTO ==================
    const purchaseErrorModal = document.getElementById("purchaseErrorModal");
    if (purchaseErrorModal) {
        purchaseErrorModal.addEventListener("hidden.bs.modal", () => {
            modalUtils.openModal("paymentChoiceModal");
        });
    }

    // ================== CHIUSURA MODALI GENERICHE ==================
    document.querySelectorAll(".btn-close-modal").forEach(btn => {
        btn.addEventListener("click", e => {
            e.stopPropagation();
            const target = btn.dataset.target;
            if (target) modalUtils.closeModal(target);
        });
    });
});
