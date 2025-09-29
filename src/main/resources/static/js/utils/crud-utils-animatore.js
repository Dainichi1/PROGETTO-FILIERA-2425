import { formUtilsCrudAnimatore } from "./crud-form-utils-animatore.js";
import { socialUtilsCrud } from "./crud-social-utils.js";
import { validationUtilsCrud } from "./crud-validation-utils.js";

export const crudUtilsAnimatore = (cfg = {}) => {
    const config = cfg;

    function togglePubblicati() {
        const container = document.getElementById("pubblicatiContainer");
        const btn = document.getElementById("togglePubblicatiBtn");
        if (!container || !btn) return;

        const isHidden = container.style.display === "none";
        container.style.display = isHidden ? "block" : "none";
        btn.textContent = isHidden ? "❌ Chiudi" : "📋 Visualizza visite/fiere pubblicate";
    }

    document.addEventListener("DOMContentLoaded", () => {
        if (config.formId) {
            validationUtilsCrud.attachLiveValidation(config.formId);
        }
    });

    return {
        toggleForm: (forceOpen) => formUtilsCrudAnimatore.toggleForm(config, forceOpen),
        submitForm: () => formUtilsCrudAnimatore.submitForm(config),

        openSocialModal: (id, type) => {
            config.currentItemId = id;
            config.itemType = type;
            socialUtilsCrud.openSocialModal(id, type);
        },

        openSocialConfirm: () => socialUtilsCrud.openSocialConfirm(),
        submitSocialPost: () => socialUtilsCrud.submitSocialPost(config),

        togglePubblicati,
        setUpdateModeForm: (id) => formUtilsCrudAnimatore.setUpdateModeForm(config, id)
    };
};
