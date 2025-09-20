import { modalUtils } from "./modal-utils.js";
import { csrfUtils } from "./csrf-utils.js";

/**
 * Versione indipendente di formUtilsCrud per Animatore
 * (non usa init, riceve la config direttamente).
 */
export const formUtilsCrudAnimatore = (() => {

    function toggleForm(config, forceShow = null) {
        if (!config.formContainerId || !config.formId) {
            console.error("crud-form-utils-animatore: config non inizializzato correttamente.");
            return;
        }

        const formContainer = document.getElementById(config.formContainerId);
        const form = document.getElementById(config.formId);

        if (!formContainer || !form) {
            console.error(
                `crud-form-utils-animatore: formContainer (#${config.formContainerId}) o form (#${config.formId}) non trovati.`
            );
            return;
        }

        // Chiudi gli altri form-container
        document.querySelectorAll(".form-container").forEach(fc => {
            if (fc.id !== config.formContainerId) {
                fc.style.display = "none";
            }
        });

        // Toggle o forza apertura
        const shouldShow =
            forceShow !== null
                ? forceShow
                : formContainer.style.display === "none" || formContainer.style.display === "";

        formContainer.style.display = shouldShow ? "block" : "none";

        if (shouldShow) {
            // Reset form per nuova creazione
            document.getElementById(config.formTitleId).textContent = config.createTitle;
            form.reset();
            form.setAttribute("action", config.createAction);

            const idField = document.getElementById(config.itemIdField);
            if (idField) idField.value = "";

            const typeField = document.getElementById(config.itemTypeField);
            if (typeField) typeField.value = config.itemType;

            document.getElementById(config.createButtonsId).style.display = "block";
            document.getElementById(config.updateButtonsId).style.display = "none";

            if (typeof config.clearAllErrors === "function") {
                config.clearAllErrors(config.formId);
            }
        }
    }

    async function submitForm(config) {
        const form = document.getElementById(config.formId);
        if (!form) {
            console.error(`crud-form-utils-animatore: form (#${config.formId}) non trovato.`);
            return;
        }

        const ok = config.validateFn ? config.validateFn() : true;
        if (!ok) {
            modalUtils.closeModal(config.confirmModalId || "createConfirmModal");
            modalUtils.closeModal("updateConfirmModal");
            return;
        }

        modalUtils.closeModal(config.confirmModalId || "createConfirmModal");
        modalUtils.closeModal("updateConfirmModal");

        const formData = new FormData(form);
        const { header, token } = csrfUtils.getCsrf();

        try {
            const res = await fetch(form.action, {
                method: form.method || "POST",
                body: formData,
                headers: { [header]: token },
                credentials: "same-origin"
            });

            if (res.ok) {
                const isUpdate = form.action.includes(config.updateAction);
                const modalId = isUpdate
                    ? (config.updateSuccessModalId || "updateSuccessModal")
                    : (config.createSuccessModalId || "createSuccessModal");

                // Apre la modale giusta senza sovrascrivere il testo
                modalUtils.openModal(modalId);

                if (isUpdate && typeof config.onUpdateSuccess === "function") {
                    config.onUpdateSuccess();
                } else if (!isUpdate && typeof config.onCreateSuccess === "function") {
                    config.onCreateSuccess();
                }
            } else {
                alert("Errore durante l'invio: " + (await res.text()));
            }
        } catch (e) {
            alert("Errore di rete durante l'invio: " + e.message);
        }
    }

    function setUpdateModeForm(config, id) {
        if (!config.formId || !config.formContainerId) return;

        document.getElementById(config.formTitleId).textContent = config.updateTitle;
        const form = document.getElementById(config.formId);
        if (!form) return;

        form.setAttribute("action", config.updateAction);

        const idField = document.getElementById(config.itemIdField);
        if (idField) idField.value = id || "";

        const typeField = document.getElementById(config.itemTypeField);
        if (typeField) typeField.value = config.itemType;

        document.getElementById(config.createButtonsId).style.display = "none";
        document.getElementById(config.updateButtonsId).style.display = "block";
        document.getElementById(config.formContainerId).style.display = "block";
    }

    return {
        toggleForm,
        submitForm,
        setUpdateModeForm
    };
})();
