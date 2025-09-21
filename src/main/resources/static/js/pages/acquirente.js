// ================== IMPORT ==================
import { toggleUtils } from "../utils/toggle-utils.js";
import { modalUtils } from "../utils/modal-utils.js";
import { crudUtils } from "../utils/crud-utils.js";
import { cartUtils } from "../utils/crud-cart-utils.js";
import { fondiUtils } from "../utils/crud-fondi-utils.js";
import { csrfUtils } from "../utils/csrf-utils.js";
import { formUtils } from "../utils/form-utils.js";
import { prenotazioniFiereUtils } from "../utils/crud-prenotazioni-fiere-utils.js";

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
        btnVisualizzaAcquisti.addEventListener("click", () => toggleUtils.show(acquistiSection));
    }
    if (btnChiudiAcquisti && acquistiSection) {
        btnChiudiAcquisti.addEventListener("click", () => toggleUtils.hide(acquistiSection));
    }

    // ================== FIERE DISPONIBILI ==================
    const fiereSection = document.getElementById("fiereSection");
    const btnVisualizzaFiere = document.getElementById("btnVisualizzaFiere");
    const btnChiudiFiere = document.getElementById("btnChiudiFiere");

    if (btnVisualizzaFiere && fiereSection) {
        btnVisualizzaFiere.addEventListener("click", () => {
            toggleUtils.show(fiereSection);

            // attiva/disattiva bottoni prenota
            const radios = document.querySelectorAll(".radio-fiera");
            const buttons = document.querySelectorAll(".btn-prenota");

            radios.forEach(radio => {
                radio.addEventListener("change", () => {
                    buttons.forEach(btn => btn.setAttribute("disabled", "disabled"));
                    const row = radio.closest("tr");
                    const btn = row.querySelector(".btn-prenota");
                    if (btn) btn.removeAttribute("disabled");
                });
            });

            // apertura modale
            buttons.forEach(btn => {
                btn.addEventListener("click", () => {
                    window.prenotazioniFiereUtils.openPrenotazioneFieraModal(btn);
                });
            });

            // attach validation al form
            window.prenotazioniFiereUtils.attachPrenotazioneFieraValidation();
        });
    }

    if (btnChiudiFiere && fiereSection) {
        btnChiudiFiere.addEventListener("click", () => {
            toggleUtils.hide(fiereSection);
        });
    }

    // ================== PRENOTAZIONE FIERE ==================
    document.querySelectorAll(".btn-prenota").forEach(btn => {
        btn.addEventListener("click", () => prenotazioniFiereUtils.openPrenotazioneFieraModal(btn));
    });
    prenotazioniFiereUtils.attachPrenotazioneFieraValidation();

    // ================== PRENOTAZIONI FIERE ELIMINAZIONE ==================
    const prenotazioniFiereSection = document.getElementById("prenotazioniFiereSection");
    const btnVisualizzaPrenotazioni = document.getElementById("btnVisualizzaPrenotazioniFiere");
    const btnChiudiPrenotazioni = document.getElementById("btnChiudiPrenotazioniFiere");

    if (btnVisualizzaPrenotazioni && prenotazioniFiereSection) {
        btnVisualizzaPrenotazioni.addEventListener("click", () => toggleUtils.show(prenotazioniFiereSection));
    }
    if (btnChiudiPrenotazioni && prenotazioniFiereSection) {
        btnChiudiPrenotazioni.addEventListener("click", () => toggleUtils.hide(prenotazioniFiereSection));
    }

    // listener per i bottoni elimina in tabella
    document.querySelectorAll(".btn-delete-prenotazione-fiera").forEach(btn => {
        btn.addEventListener("click", () => {
            const id = btn.dataset.id;
            prenotazioniFiereUtils.openDeletePrenotazioneFieraModal(id);
        });
    });

    // attach gestione eliminazione prenotazioni fiere (con update fondi lato client)
    prenotazioniFiereUtils.attachPrenotazioneFieraDeleteHandler();

    // ================== PULSANTE RECENSIONE ==================
    document.querySelectorAll(".btn-pubblica-recensione").forEach(btn => {
        btn.addEventListener("click", () => {
            const id = btn.dataset.id;
            const stato = btn.dataset.stato;

            document.body.dataset.recensioneId = id;
            document.body.dataset.recensioneStato = stato;

            const titleEl = document.getElementById("recensioneTitle");
            const textEl = document.getElementById("recensioneText");
            if (titleEl) { titleEl.value = ""; formUtils.clearFieldError(titleEl); }
            if (textEl) { textEl.value = ""; formUtils.clearFieldError(textEl); }

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

        document.getElementById("recensioneConfirmMessage").innerText =
            "Sei sicuro di voler pubblicare la recensione sull’acquisto?";

        modalUtils.closeModal("recensionePostModal");
        modalUtils.openModal("recensioneConfirmModal");
    });

    document.getElementById("btnConfirmRecensionePost")?.addEventListener("click", () => {
        const acquistoId = document.body.dataset.recensioneId || null;
        const titolo = document.getElementById("recensioneTitle")?.value.trim();
        const testo = document.getElementById("recensioneText")?.value.trim();

        if (!acquistoId) {
            document.getElementById("recensioneErrorMessage").innerText =
                "❌ Errore: ID acquisto non trovato.";
            modalUtils.openModal("recensioneErrorModal");
            return;
        }

        const csrf = csrfUtils.getCsrf();
        const payload = { titolo, testo };

        fetch(`/api/social/pubblica-recensione/${acquistoId}`, {
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
                    document.getElementById("recensioneSuccessMessage").innerText =
                        "✅ Recensione pubblicata con successo!";
                    modalUtils.openModal("recensioneSuccessModal");
                } else {
                    document.getElementById("recensioneErrorMessage").innerText =
                        "❌ Impossibile pubblicare la recensione.";
                    modalUtils.openModal("recensioneErrorModal");
                }
            })
            .catch(err => {
                document.getElementById("recensioneErrorMessage").innerText =
                    "❌ Errore imprevisto: " + err.message;
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

            const row = document.querySelector(
                `#cartSection tbody tr[data-id="${selected.id}"][data-tipo="${selected.tipo}"]`
            );
            const nome = row?.children[1]?.innerText || "Item";
            const quantita = row?.querySelector(".cart-quantity-input")?.value || "1";
            const totale = row?.children[4]?.innerText || "€ 0.00";

            document.getElementById("confirmPurchaseDetails").innerText =
                `Vuoi acquistare "${nome}" (x${quantita}) per un totale di ${totale} ` +
                `usando ${metodo.replaceAll("_", " ").toLowerCase()}?`;

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
                    { ...selected, totale }
                ]
            };

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
                        document.getElementById("purchaseSuccessMessage").innerText =
                            data.message || "✅ Acquisto completato con successo!";
                        modalUtils.openModal("purchaseSuccessModal");
                        cartUtils.updateCartTable([], {});
                        document.getElementById("btnAcquista")?.setAttribute("disabled", "disabled");
                        cartUtils.loadCart();
                    } else {
                        document.getElementById("purchaseErrorMessage").innerText =
                            data.message || "❌ Errore durante l’acquisto.";
                        modalUtils.openModal("purchaseErrorModal");
                    }
                })
                .catch(() => modalUtils.openModal("purchaseErrorModal"));
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
