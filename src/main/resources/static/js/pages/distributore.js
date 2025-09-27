// ================== IMPORT ==================
import {crudUtils} from "../utils/crud-utils.js";
import {toggleUtils} from "../utils/toggle-utils.js";
import {formUtils} from "../utils/form-utils.js";
import {modalUtils} from "../utils/modal-utils.js";
import {prenotazioniUtils} from "../utils/crud-prenotazioni-utils.js";
import {socialSelectionUtils} from "../utils/social-selection-utils.js";

window.toggleUtils = toggleUtils;

// ================== VALIDAZIONE PACCHETTO ==================
function clientValidatePacchetto() {
    formUtils.clearAllErrors("packageForm");

    const nome = document.getElementById("nome-pacchettoDto");
    const descrizione = document.getElementById("descrizione-pacchettoDto");
    const indirizzo = document.getElementById("indirizzo-pacchettoDto");
    const quantita = document.getElementById("quantita-pacchettoDto");
    const prezzo = document.getElementById("prezzo-pacchettoDto");
    const certificati = document.getElementById("certificati-pacchettoDto");
    const foto = document.getElementById("foto-pacchettoDto");

    let ok = true;

    if (!nome.value.trim()) {
        formUtils.setFieldError("nome-pacchettoDto", "⚠ Nome obbligatorio");
        ok = false;
    }
    if (!descrizione.value.trim()) {
        formUtils.setFieldError("descrizione-pacchettoDto", "⚠ Descrizione obbligatoria");
        ok = false;
    }
    if (!indirizzo.value.trim()) {
        formUtils.setFieldError("indirizzo-pacchettoDto", "⚠ Indirizzo obbligatorio");
        ok = false;
    }

    const q = quantita.value !== "" ? Number(quantita.value) : NaN;
    if (Number.isNaN(q) || q < 1) {
        formUtils.setFieldError("quantita-pacchettoDto", "⚠ La quantità deve essere almeno 1");
        ok = false;
    }

    const p = prezzo.value !== "" ? Number(prezzo.value) : NaN;
    if (Number.isNaN(p) || p <= 0) {
        formUtils.setFieldError("prezzo-pacchettoDto", "⚠ Il prezzo deve essere positivo");
        ok = false;
    }

    if (!certificati.files || certificati.files.length === 0) {
        formUtils.setFieldError("certificati-pacchettoDto", "⚠ Devi caricare almeno un certificato");
        ok = false;
    }
    if (!foto.files || foto.files.length === 0) {
        formUtils.setFieldError("foto-pacchettoDto", "⚠ Devi caricare almeno una foto");
        ok = false;
    }

    const prodotti = document.querySelectorAll("input[name='prodottiSelezionati']:checked");
    if (prodotti.length < 2) {
        const span = document.querySelector(".checkbox-list + .error-message");
        if (span) span.textContent = "⚠ Devi selezionare almeno 2 prodotti approvati";
        ok = false;
    }

    return ok;
}

// ================== CRUD PACCHETTO ==================
const pacchettoCrud = crudUtils.createInstance({
    formId: "packageForm",
    formContainerId: "pacchettoForm",
    formTitleId: "formTitle",
    itemIdField: "itemId",
    itemTypeField: "itemTipo",
    createButtonsId: "createButtons",
    updateButtonsId: "updateButtons",
    itemType: "PACCHETTO",
    createTitle: "Nuovo Pacchetto",
    updateTitle: "Modifica Pacchetto Rifiutato",
    createAction: "/distributore/crea",
    updateAction: "/venditore/item/modifica",
    updateRejectedAction: "/venditore/item/modifica-rifiutato",

    deleteUrl: id => `/venditore/item/elimina/${id}?tipo=PACCHETTO`,
    fetchUrl: id => `/venditore/item/fetch/${id}?tipo=PACCHETTO`,
    deletableStates: ["IN_ATTESA", "RIFIUTATO"],
    editableState: "RIFIUTATO",

    deleteErrorModalId: "deleteErrorModal",
    deleteConfirmModalId: "deleteConfirmModal",
    deleteSuccessModalId: "deleteSuccessModal",
    deleteGenericErrorModalId: "deleteGenericErrorModal",
    editErrorModalId: "editErrorModal",

    deleteConfirmMessageId: "deleteConfirmMessage",
    deleteSuccessMessageId: "deleteSuccessMessage",
    deleteGenericErrorMessageId: "deleteGenericErrorMessage",

    labels: {itemName: "Pacchetto"},

    validateFn: clientValidatePacchetto,

    prefillFormFn: p => {
        document.getElementById("nome-pacchettoDto").value = p.nome ?? "";
        document.getElementById("descrizione-pacchettoDto").value = p.descrizione ?? "";
        document.getElementById("quantita-pacchettoDto").value = p.quantita ?? "";
        document.getElementById("prezzo-pacchettoDto").value = p.prezzo ?? "";
        document.getElementById("indirizzo-pacchettoDto").value = p.indirizzo ?? "";

        if (Array.isArray(p.prodottiSelezionati)) {
            document.querySelectorAll("input[name='prodottiSelezionati']").forEach(cb => {
                cb.checked = p.prodottiSelezionati.includes(Number(cb.value));
            });
        }
    }
});

// ================== INIZIALIZZAZIONE ==================
document.addEventListener("DOMContentLoaded", () => {
    // Bottone Social Feed
    document.getElementById("btnSocialFeed")?.addEventListener("click", () => {
        crudUtils.openSocialFeed();
    });

    // Toggle sezioni
    document.getElementById("btnVisite")?.addEventListener("click", () => {
        toggleUtils.toggleSection("visiteSection");
    });
    document.getElementById("btnPrenotazioni")?.addEventListener("click", () => {
        toggleUtils.toggleSection("prenotazioniSection");
    });

    // Pulsanti tabella pacchetti
    document.querySelectorAll(".btn-delete").forEach(btn => {
        btn.addEventListener("click", () => pacchettoCrud.handleDeleteClick(btn));
    });
    document.querySelectorAll(".btn-edit").forEach(btn => {
        btn.addEventListener("click", () => pacchettoCrud.handleEditClick(btn));
    });
    document.querySelectorAll(".btn-publish").forEach(btn => {
        btn.addEventListener("click", () => pacchettoCrud.handleSocialClick(btn));
    });

    // Prenotazioni
    document.querySelectorAll(".btn-prenota-visita").forEach(btn => {
        btn.addEventListener("click", () => prenotazioniUtils.openPrenotazioneVisitaModal(btn));
    });
    document.querySelectorAll(".btn-delete-prenotazione").forEach(btn => {
        btn.addEventListener("click", () => {
            const id = btn.getAttribute("data-id");
            if (id) prenotazioniUtils.openDeletePrenotazioneModal(id);
        });
    });
    prenotazioniUtils.attachPrenotazioneDeleteHandler();

    // Chiusura modali generiche
    document.querySelectorAll(".btn-close-modal").forEach(btn => {
        btn.addEventListener("click", () => {
            const target = btn.getAttribute("data-target");
            if (target) modalUtils.closeModal(target);
        });
    });

    // Apertura modali generiche
    document.querySelectorAll(".btn-open-modal").forEach(btn => {
        btn.addEventListener("click", () => {
            const target = btn.getAttribute("data-target");
            if (target) modalUtils.openModal(target);
        });
    });

    // Crea pacchetto
    document.getElementById("btnCreatePacchetto")?.addEventListener("click", () => {
        pacchettoCrud.toggleForm(true);
    });

    // Conferme CRUD
    document.getElementById("btnConfirmDelete")?.addEventListener("click", () => {
        pacchettoCrud.confirmDelete();
    });
    document.getElementById("btnOkSocialPost")?.addEventListener("click", () => {
        pacchettoCrud.openSocialConfirm();
    });
    document.getElementById("btnConfirmSocialPost")?.addEventListener("click", () => {
        pacchettoCrud.submitSocialPost();
    });
    document.getElementById("btnConfirmCreate")?.addEventListener("click", () => {
        pacchettoCrud.confirmCreate();
    });
    document.getElementById("btnConfirmUpdate")?.addEventListener("click", () => {
        pacchettoCrud.confirmUpdate();
    });

    // ================== ELIMINA PROFILO ==================
    const btnDeleteProfile = document.getElementById("btnDeleteProfile");
    if (btnDeleteProfile) {
        btnDeleteProfile.addEventListener("click", () => {
            modalUtils.openModal("deleteProfileModal");
        });
    }

    const confirmDeleteBtn = document.getElementById("confirmDeleteBtn");
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener("click", () => {
            const csrfHeader = document.querySelector("meta[name='_csrf_header']").content;
            const csrfToken = document.querySelector("meta[name='_csrf']").content;

            fetch("/distributore/richiesta-eliminazione", {
                method: "POST",
                headers: {
                    [csrfHeader]: csrfToken
                }
            })
                .then(response => {
                    if (response.ok) {
                        modalUtils.closeModal("deleteProfileModal");
                        modalUtils.openModal("deleteProfileSuccessModal");
                    } else if (response.status === 409) {
                        modalUtils.closeModal("deleteProfileModal");
                        modalUtils.openModal("deleteProfileErrorModal");
                    } else {
                        throw new Error("Errore imprevisto");
                    }
                })
                .catch(err => {
                    console.error("Errore eliminazione profilo", err);
                    alert("Errore durante l'invio della richiesta.");
                });
        });
    }

    const okDeleteBtn = document.getElementById("okDeleteBtn");
    if (okDeleteBtn) {
        okDeleteBtn.addEventListener("click", () => {
            modalUtils.closeModal("deleteProfileSuccessModal");
        });
    }

    // ================== NOTIFICA ELIMINAZIONE PROFILO ==================
    const deletedProfileMessage = document.getElementById("deletedProfileMessage");
    const okProfileDeletedBtn = document.getElementById("okProfileDeletedBtn");

    function showProfileDeletedNotification(requestId) {
        let seconds = 30;

        function updateMessage() {
            if (deletedProfileMessage) {
                deletedProfileMessage.innerText =
                    `⚠️ Il tuo profilo è stato eliminato (richiesta ID ${requestId}). ` +
                    `Verrai disconnesso tra ${seconds} secondi...`;
            }
        }

        updateMessage();
        modalUtils.openModal("profileDeletedNotificationModal");

        const interval = setInterval(() => {
            seconds--;
            updateMessage();

            if (seconds <= 0) {
                clearInterval(interval);
                window.location.href = "/logout";
            }
        }, 1000);

        if (okProfileDeletedBtn) {
            okProfileDeletedBtn.onclick = () => {
                clearInterval(interval);
                modalUtils.closeModal("profileDeletedNotificationModal");
                window.location.href = "/logout";
            };
        }
    }

    // ================== POLLING STATO RICHIESTA ==================
    function pollEliminazione() {
        fetch("/distributore/richiesta-eliminazione/stato", {
            credentials: "same-origin"
        })
            .then(r => {
                if (!r.ok) throw new Error("Errore HTTP " + r.status);
                return r.text();
            })
            .then(resp => {
                if (resp.startsWith("APPROVATA:")) {
                    const id = resp.split(":")[1];
                    showProfileDeletedNotification(id);
                    clearInterval(pollingInterval);
                }
            })
            .catch(err => console.error("Errore polling eliminazione:", err));
    }

    const pollingInterval = setInterval(pollEliminazione, 5000);
});
