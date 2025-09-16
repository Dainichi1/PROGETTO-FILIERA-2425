// ================== IMPORT ==================
import {crudUtils} from "../utils/crud-utils.js";
import {toggleUtils} from "../utils/toggle-utils.js";
import {formUtils} from "../utils/form-utils.js";
import {modalUtils} from "../utils/modal-utils.js";
import {prenotazioniUtils} from "../utils/crud-prenotazioni-utils.js";

window.toggleUtils = toggleUtils;

// ================== VALIDAZIONE PRODOTTO ==================
function clientValidateProdotto() {
    formUtils.clearAllErrors("itemForm");

    const nome = document.getElementById("nome-prodottoDto");
    const descrizione = document.getElementById("descrizione-prodottoDto");
    const quantita = document.getElementById("quantita-prodottoDto");
    const prezzo = document.getElementById("prezzo-prodottoDto");
    const indirizzo = document.getElementById("indirizzo-prodottoDto");
    const certificati = document.getElementById("certificati-prodottoDto");
    const foto = document.getElementById("foto-prodottoDto");

    let ok = true;

    if (!nome.value.trim()) {
        formUtils.setFieldError("nome-prodottoDto", "⚠ Nome obbligatorio");
        ok = false;
    }
    if (!descrizione.value.trim()) {
        formUtils.setFieldError("descrizione-prodottoDto", "⚠ Descrizione obbligatoria");
        ok = false;
    }
    if (!indirizzo.value.trim()) {
        formUtils.setFieldError("indirizzo-prodottoDto", "⚠ Indirizzo obbligatorio");
        ok = false;
    }

    const q = quantita.value !== "" ? Number(quantita.value) : NaN;
    if (Number.isNaN(q) || q < 1) {
        formUtils.setFieldError("quantita-prodottoDto", "⚠ La quantità deve essere almeno 1");
        ok = false;
    }

    const p = prezzo.value !== "" ? Number(prezzo.value) : NaN;
    if (Number.isNaN(p) || p <= 0) {
        formUtils.setFieldError("prezzo-prodottoDto", "⚠ Il prezzo deve essere positivo");
        ok = false;
    }

    if (!certificati.files || certificati.files.length === 0) {
        formUtils.setFieldError("certificati-prodottoDto", "⚠ Devi caricare almeno un certificato");
        ok = false;
    }
    if (!foto.files || foto.files.length === 0) {
        formUtils.setFieldError("foto-prodottoDto", "⚠ Devi caricare almeno una foto");
        ok = false;
    }

    return ok;
}

// ================== CRUD PRODOTTO ==================
const prodottoCrud = crudUtils.createInstance({
    formId: "itemForm",
    formContainerId: "prodottoForm",
    formTitleId: "formTitle",
    itemIdField: "itemId",
    itemTypeField: "itemTipo",
    createButtonsId: "createButtons",
    updateButtonsId: "updateButtons",
    itemType: "PRODOTTO",
    createTitle: "Nuovo Prodotto",
    updateTitle: "Modifica Prodotto Rifiutato",
    createAction: "/produttore/crea",
    updateAction: "/venditore/item/modifica",
    updateRejectedAction: "/venditore/item/modifica-rifiutato",

    deleteUrl: id => `/venditore/item/elimina/${id}?tipo=PRODOTTO`,
    fetchUrl: id => `/venditore/item/fetch/${id}?tipo=PRODOTTO`,
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

    labels: {itemName: "Prodotto"},

    validateFn: clientValidateProdotto,

    prefillFormFn: p => {
        document.getElementById("nome-prodottoDto").value = p.nome ?? "";
        document.getElementById("descrizione-prodottoDto").value = p.descrizione ?? "";
        document.getElementById("quantita-prodottoDto").value = p.quantita ?? "";
        document.getElementById("prezzo-prodottoDto").value = p.prezzo ?? "";
        document.getElementById("indirizzo-prodottoDto").value = p.indirizzo ?? "";
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

    // Pulsanti tabella prodotti
    document.querySelectorAll(".btn-delete").forEach(btn => {
        btn.addEventListener("click", () => prodottoCrud.handleDeleteClick(btn));
    });

    document.querySelectorAll(".btn-edit").forEach(btn => {
        btn.addEventListener("click", () => prodottoCrud.handleEditClick(btn));
    });

    document.querySelectorAll(".btn-publish").forEach(btn => {
        btn.addEventListener("click", () => prodottoCrud.handleSocialClick(btn));
    });

// Prenotazione visite
    document.querySelectorAll(".btn-prenota-visita").forEach(btn => {
        btn.addEventListener("click", () => {
            prenotazioniUtils.openPrenotazioneVisitaModal(btn);
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

    // Bottone "Chiudi" nei form (nasconde il contenitore)
    document.querySelectorAll(".btn-toggle-form").forEach(btn => {
        btn.addEventListener("click", () => {
            const formContainer = btn.closest(".form-container");
            if (formContainer) {
                formContainer.style.display = "none";
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

    // Apertura modali di conferma (es. Invia/Aggiorna)
    document.querySelectorAll(".btn-open-modal").forEach(btn => {
        btn.addEventListener("click", () => {
            const target = btn.getAttribute("data-target");
            if (target) modalUtils.openModal(target);
        });
    });

    // Bottone "Crea Prodotto"
    document.getElementById("btnCreateProdotto")?.addEventListener("click", () => {
        prodottoCrud.toggleForm(true); // forza apertura
    });

    // Conferma eliminazione (bottone "Sì" nella modale)
    document.getElementById("btnConfirmDelete")?.addEventListener("click", () => {
        prodottoCrud.confirmDelete();
    });

    // Bottone OK nel form social (apre la conferma)
    document.getElementById("btnOkSocialPost")?.addEventListener("click", () => {
        prodottoCrud.openSocialConfirm();
    });
});
