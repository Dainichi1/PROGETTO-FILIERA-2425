// ================== IMPORT ==================
import {validationUtilsCrud} from "../utils/crud-validation-utils.js";
import {modalUtils} from "../utils/modal-utils.js";
import {crudUtilsAnimatore} from "../utils/crud-utils-animatore.js";
import {socialSelectionUtils} from "../utils/social-selection-utils.js";
import {crudUtils} from "../utils/crud-utils.js";

// ================== CRUD VISITA ==================
const visitaCrud = crudUtilsAnimatore({
    formId: "visitaForm",
    formContainerId: "visitaFormContainer",
    formTitleId: "formTitleVisita",
    itemIdField: "visitaId",
    itemTypeField: "eventoTipoVisita",
    createButtonsId: "createButtonsVisita",
    updateButtonsId: "updateButtonsVisita",
    itemType: "VISITA",
    createTitle: "Nuova Visita ad Invito",
    updateTitle: "Modifica Visita",
    createAction: "/animatore/crea-visita",
    updateAction: "/animatore/modifica-visita",
    labels: {itemName: "Visita ad invito"},
    validateFn: validationUtilsCrud.validateVisita,
    confirmModalId: "createConfirmModalVisita",
    createSuccessModalId: "createSuccessModalVisita",
    updateSuccessModalId: "updateSuccessModalVisita",
    prefillFormFn: v => {
        document.getElementById("nome-visitaDto").value = v.nome ?? "";
        document.getElementById("descrizione-visitaDto").value = v.descrizione ?? "";
        document.getElementById("indirizzo-visitaDto").value = v.indirizzo ?? "";
        document.getElementById("dataInizio-visitaDto").value = v.dataInizio?.split("T")[0] ?? "";
        document.getElementById("dataFine-visitaDto").value = v.dataFine?.split("T")[0] ?? "";
        // TODO: check destinatari con v.destinatari
    }
});

// ================== CRUD FIERA ==================
const fieraCrud = crudUtilsAnimatore({
    formId: "fieraForm",
    formContainerId: "fieraFormContainer",
    formTitleId: "formTitleFiera",
    itemIdField: "fieraId",
    itemTypeField: "eventoTipoFiera",
    createButtonsId: "createButtonsFiera",
    updateButtonsId: "updateButtonsFiera",
    itemType: "FIERA",
    createTitle: "Nuova Fiera",
    updateTitle: "Modifica Fiera",
    createAction: "/animatore/crea-fiera",
    updateAction: "/animatore/modifica-fiera",
    labels: {itemName: "Fiera"},
    validateFn: validationUtilsCrud.validateFiera,
    confirmModalId: "createConfirmModalFiera",
    createSuccessModalId: "createSuccessModalFiera",
    updateSuccessModalId: "updateSuccessModalFiera",
    prefillFormFn: f => {
        document.getElementById("nome-fieraDto").value = f.nome ?? "";
        document.getElementById("descrizione-fieraDto").value = f.descrizione ?? "";
        document.getElementById("indirizzo-fieraDto").value = f.indirizzo ?? "";
        document.getElementById("prezzo-fieraDto").value = f.prezzo ?? "";
        document.getElementById("dataInizio-fieraDto").value = f.dataInizio?.split("T")[0] ?? "";
        document.getElementById("dataFine-fieraDto").value = f.dataFine?.split("T")[0] ?? "";
    }
});

// ================== OGGETTO ANIMATORE ==================
const animatoreCrud = {
    visita: visitaCrud,
    fiera: fieraCrud
};

// ================== INIZIALIZZAZIONE ==================
document.addEventListener("DOMContentLoaded", () => {
    // Bottone Social Feed
    document.getElementById("btnSocialFeed")
        ?.addEventListener("click", () => crudUtils.openSocialFeed());

    // Toggle pubblicati
    document.getElementById("togglePubblicatiBtn")
        ?.addEventListener("click", () => visitaCrud.togglePubblicati());

    // Selezione riga + pubblicazione social
    socialSelectionUtils.init({
        rowSelector: ".selectable-row",
        btnPubblicaId: "btnPubblicaAvviso",
        crudMap: {
            VISITA: visitaCrud,
            FIERA: fieraCrud
        }
    });

    // === PUBBLICAZIONE VISITA ===
    document.getElementById("btnPubblicaVisita")
        ?.addEventListener("click", () => modalUtils.openModal("createConfirmModalVisita"));
    document.getElementById("btnConfirmCreateVisita")
        ?.addEventListener("click", () => visitaCrud.submitForm());
    document.getElementById("btnConfirmUpdateVisita")
        ?.addEventListener("click", () => visitaCrud.submitForm());
    document.getElementById("btnCancelVisita")
        ?.addEventListener("click", () => visitaCrud.toggleForm(false));

    // === PUBBLICAZIONE FIERA ===
    document.getElementById("btnPubblicaFiera")
        ?.addEventListener("click", () => modalUtils.openModal("createConfirmModalFiera"));
    document.getElementById("btnConfirmCreateFiera")
        ?.addEventListener("click", () => fieraCrud.submitForm());
    document.getElementById("btnConfirmUpdateFiera")
        ?.addEventListener("click", () => fieraCrud.submitForm());
    document.getElementById("btnCancelFiera")
        ?.addEventListener("click", () => fieraCrud.toggleForm(false));

    // Apertura form
    document.getElementById("btnOpenVisita")
        ?.addEventListener("click", () => visitaCrud.toggleForm(true));
    document.getElementById("btnOpenFiera")
        ?.addEventListener("click", () => fieraCrud.toggleForm(true));
// ================== CHIUSURA MODALI GENERICHE ==================
    document.querySelectorAll(".btn-close-modal").forEach(btn => {
        btn.addEventListener("click", e => {
            e.stopPropagation();
            const target = btn.getAttribute("data-target");
            if (target) modalUtils.closeModal(target);
        });
    });
});

// ================== ESPORTO GLOBALMENTE ==================
window.visitaCrud = visitaCrud;
window.fieraCrud = fieraCrud;
window.animatoreCrud = animatoreCrud;
