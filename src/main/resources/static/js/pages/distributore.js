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

    // Bottone toggle visite
    document.getElementById("btnVisite")?.addEventListener("click", () => {
        toggleUtils.toggleSection("visiteSection");
    });

    // Bottone toggle prenotazioni
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

    // Prenotazione visite
    document.querySelectorAll(".btn-prenota-visita").forEach(btn => {
        btn.addEventListener("click", () => {
            prenotazioniUtils.openPrenotazioneVisitaModal(btn);
        });
    });

    // Bottone "Chiudi" nei form (nasconde il contenitore)
    document.querySelectorAll(".btn-toggle-form").forEach(btn => {
        btn.addEventListener("click", () => {
            const formContainer = btn.closest(".form-container");
            if (formContainer) {
                formContainer.style.display = "none";
            }
        });
    });

    // Eliminazione prenotazioni
    document.querySelectorAll(".btn-delete-prenotazione").forEach(btn => {
        btn.addEventListener("click", () => {
            const id = btn.getAttribute("data-id");
            if (id) {
                prenotazioniUtils.openDeletePrenotazioneModal(id);
            }
        });
    });

    // Attacca handler al form di delete
    prenotazioniUtils.attachPrenotazioneDeleteHandler();

    // Gestione chiusura modali
    document.querySelectorAll(".btn-close-modal").forEach(btn => {
        btn.addEventListener("click", () => {
            const target = btn.getAttribute("data-target");
            if (target) modalUtils.closeModal(target);
        });
    });

    // Bottone "Crea Pacchetto"
    document.getElementById("btnCreatePacchetto")?.addEventListener("click", () => {
        pacchettoCrud.toggleForm(true); // forza apertura
    });

    // Conferma eliminazione
    document.getElementById("btnConfirmDelete")?.addEventListener("click", () => {
        pacchettoCrud.confirmDelete();
    });

    // Social post modale
    document.getElementById("btnOkSocialPost")?.addEventListener("click", () => {
        pacchettoCrud.openSocialConfirm();
    });
    document.getElementById("btnConfirmSocialPost")?.addEventListener("click", () => {
        pacchettoCrud.submitSocialPost();
    });

    // Conferma creazione (bottone "Sì" nella modale)
    document.getElementById("btnConfirmCreate")?.addEventListener("click", () => {
        pacchettoCrud.confirmCreate();
    });

    // Conferma aggiornamento (bottone "Sì" nella modale)
    document.getElementById("btnConfirmUpdate")?.addEventListener("click", () => {
        pacchettoCrud.confirmUpdate();
    });

    // Apertura modali di conferma (es. Invia/Aggiorna)
    document.querySelectorAll(".btn-open-modal").forEach(btn => {
        btn.addEventListener("click", () => {
            const target = btn.getAttribute("data-target");
            if (target) modalUtils.openModal(target);
        });
    });

    // Bottone chiudi visite
    document.getElementById("btnCloseVisite")?.addEventListener("click", () => {
        toggleUtils.toggleSection("visiteSection");
    });

    // Bottone chiudi prenotazioni
    document.getElementById("btnClosePrenotazioni")?.addEventListener("click", () => {
        toggleUtils.toggleSection("prenotazioniSection");
    });

    // Inizializza selezione social
    socialSelectionUtils.init({
        rowSelector: ".selectable-row",
        btnPubblicaId: "btnPubblicaAvviso",
        crudMap: {PACCHETTO: pacchettoCrud}
    });
});
