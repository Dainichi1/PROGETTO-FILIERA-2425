import { modalUtils } from "./modal-utils.js";
import { crudUtils } from "./crud-core.js";

/**
 * Inizializzazione modali generici (delete, create/update, social, animatore).
 */
document.addEventListener("DOMContentLoaded", () => {
    // ================== DELETE ==================
    const btnConfirmDelete = document.getElementById("btnConfirmDelete");
    if (btnConfirmDelete) {
        btnConfirmDelete.addEventListener("click", () => crudUtils.confirmDelete());
    }

    const btnCloseDeleteSuccess = document.getElementById("btnCloseDeleteSuccess");
    if (btnCloseDeleteSuccess) {
        btnCloseDeleteSuccess.addEventListener("click", () =>
            modalUtils.closeModal("deleteSuccessModal")
        );
    }

    // ================== CREATE / UPDATE ==================
    const btnConfirmCreate = document.getElementById("btnConfirmCreate");
    if (btnConfirmCreate) {
        btnConfirmCreate.addEventListener("click", () => crudUtils.submitForm());
    }

    const btnConfirmUpdate = document.getElementById("btnConfirmUpdate");
    if (btnConfirmUpdate) {
        btnConfirmUpdate.addEventListener("click", () => crudUtils.submitForm());
    }

    // ================== SOCIAL ==================
    const btnOkSocial = document.getElementById("btnOkSocialPost");
    if (btnOkSocial) {
        btnOkSocial.addEventListener("click", () => {
            const crud = window.currentCrud || crudUtils;
            if (crud?.openSocialConfirm) {
                crud.openSocialConfirm();
            }
        });
    }

    const btnConfirmSocial = document.getElementById("btnConfirmSocialPost");
    if (btnConfirmSocial) {
        btnConfirmSocial.addEventListener("click", () => {
            const crud = window.currentCrud || crudUtils;
            if (crud?.submitSocialPost) {
                crud.submitSocialPost();
            }
        });
    }

    // ================== ANIMATORE: VISITA ==================
    const btnConfirmCreateVisita = document.getElementById("btnConfirmCreateVisita");
    if (btnConfirmCreateVisita && window.visitaCrud) {
        btnConfirmCreateVisita.addEventListener("click", () => visitaCrud.submitForm());
    }

    // ================== ANIMATORE: FIERA ==================
    const btnConfirmCreateFiera = document.getElementById("btnConfirmCreateFiera");
    if (btnConfirmCreateFiera && window.fieraCrud) {
        btnConfirmCreateFiera.addEventListener("click", () => fieraCrud.submitForm());
    }

    // ================== REDIRECT BUTTONS ==================
    document.addEventListener("click", e => {
        if (e.target && e.target.classList.contains("redirect-btn")) {
            const url = e.target.getAttribute("data-redirect");

            // Chiudo eventuali modali di successo ancora aperti
            [
                "createSuccessModal",
                "updateSuccessModal",
                "createSuccessModalVisita",
                "createSuccessModalFiera",
                "deleteSuccessModal",
                "socialSuccessModal",
                "prenotazioneSuccessModal",
                "prenotazioneGiaEffettuataModal"
            ].forEach(modalId => modalUtils.closeModal(modalId));

            if (url) window.location.href = url;
        }
    });
});
