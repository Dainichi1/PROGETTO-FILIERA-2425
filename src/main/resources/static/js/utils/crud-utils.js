import { modalUtils } from "./modal-utils.js";
import { formUtilsCrud } from "./crud-form-utils.js";
import { deleteUtilsCrud } from "./crud-delete-utils.js";
import { editUtilsCrud } from "./crud-edit-utils.js";
import { socialUtilsCrud } from "./crud-social-utils.js";
import { prenotazioniUtils } from "./crud-prenotazioni-utils.js";
import { validationUtilsCrud } from "./crud-validation-utils.js";

/**
 * Orchestratore CRUD che coordina i vari sotto-utils
 */
export const crudUtils = (() => {
    let config = {};

    // ===== INIT PRINCIPALE =====
    function init(cfg) {
        config = cfg;

        // inizializza i moduli figli con la stessa config
        formUtilsCrud.init(cfg);
        deleteUtilsCrud.init(cfg);
        editUtilsCrud.init(cfg);
        socialUtilsCrud.init(cfg);

        // validazione (form e social live)
        validationUtilsCrud.attachLiveValidation(cfg.formId);
        validationUtilsCrud.attachSocialLiveValidation();

        // prenotazioni
        prenotazioniUtils.attachPrenotazioneVisitaValidation();

        // collega i bottoni submit
        attachConfirmButtons();
    }

    /**
     * Crea un'istanza legata a una configurazione specifica
     */
    function createInstance(cfg) {
        // inizializza subito tutti i moduli figli
        formUtilsCrud.init(cfg);
        deleteUtilsCrud.init(cfg);
        editUtilsCrud.init(cfg);
        socialUtilsCrud.init(cfg);

        const instance = { ...crudUtils, config: cfg };

        document.addEventListener("DOMContentLoaded", () => {
            validationUtilsCrud.attachLiveValidation(cfg.formId);
            validationUtilsCrud.attachSocialLiveValidation();
            prenotazioniUtils.attachPrenotazioneVisitaValidation();

            // collega i bottoni submit
            attachConfirmButtons(instance);
        });

        return instance;
    }

    // ===== COLLEGAMENTO BOTTONI CONFERMA =====
    function attachConfirmButtons(instanceRef) {
        const ctx = instanceRef || crudUtils;

        document.getElementById("btnConfirmCreate")?.addEventListener("click", () => {
            ctx.submitForm();
        });

        document.getElementById("btnConfirmUpdate")?.addEventListener("click", () => {
            ctx.submitForm();
        });
    }

    // ===== REDIRECT POST-SUCCESS =====
    document.addEventListener("click", e => {
        if (e.target && e.target.classList.contains("redirect-btn")) {
            [
                "createSuccessModal",
                "updateSuccessModal",
                "deleteSuccessModal",
                "socialSuccessModal",
                "prenotazioneDeleteSuccessModal"
            ].forEach(id => modalUtils.closeModal(id));

            const url = e.target.getAttribute("data-redirect");
            if (url) window.location.href = url;
        }
    });

    return {
        init,
        createInstance,

        // FORM
        toggleForm: (...args) => formUtilsCrud.toggleForm(...args),
        submitForm: (...args) => formUtilsCrud.submitForm(...args),
        setUpdateModeForm: (...args) => formUtilsCrud.setUpdateModeForm(...args),

        // DELETE
        handleDeleteClick: (...args) => deleteUtilsCrud.handleDeleteClick(...args),
        confirmDelete: (...args) => deleteUtilsCrud.confirmDelete(...args),

        // EDIT
        handleEditClick: (...args) => editUtilsCrud.handleEditClick(...args),

        // SOCIAL
        handleSocialClick: (...args) => socialUtilsCrud.handleSocialClick(...args),
        openSocialConfirm: (...args) => socialUtilsCrud.openSocialConfirm(...args),
        submitSocialPost: (...args) => socialUtilsCrud.submitSocialPost(...args),
        openSocialFeed: (...args) => socialUtilsCrud.openSocialFeed(...args),
        openSocialModal: (...args) => socialUtilsCrud.openSocialModal(...args),

        // PRENOTAZIONI
        openPrenotazioneVisitaModal: (...args) => prenotazioniUtils.openPrenotazioneVisitaModal(...args),
        attachPrenotazioneVisitaValidation: (...args) => prenotazioniUtils.attachPrenotazioneVisitaValidation(...args)
    };
})();
