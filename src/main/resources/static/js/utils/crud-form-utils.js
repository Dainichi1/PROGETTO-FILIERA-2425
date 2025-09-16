import { modalUtils } from "./modal-utils.js";
import { csrfUtils } from "./csrf-utils.js";

/**
 * Utilità per gestione form CRUD
 */
export const formUtilsCrud = (() => {
    let config = {};

    function init(cfg) {
        config = cfg;
    }

    // ===== TOGGLE FORM =====
    function toggleForm(forceShow = null) {
        if (!config.formContainerId || !config.formId) {
            console.error("crud-form-utils: config non inizializzato correttamente.");
            return;
        }

        const formContainer = document.getElementById(config.formContainerId);
        const form = document.getElementById(config.formId);

        if (!formContainer || !form) {
            console.error(
                `crud-form-utils: formContainer (#${config.formContainerId}) o form (#${config.formId}) non trovati.`
            );
            return;
        }

        // CHIUDI TUTTI GLI ALTRI FORM-CONTAINER
        document.querySelectorAll(".form-container").forEach(fc => {
            if (fc.id !== config.formContainerId) {
                fc.style.display = "none";
            }
        });

        // Se viene passato forceShow, ignora il toggle
        const shouldShow =
            forceShow !== null
                ? forceShow
                : formContainer.style.display === "none" || formContainer.style.display === "";

        formContainer.style.display = shouldShow ? "block" : "none";

        if (shouldShow) {
            // reset form per nuova creazione
            document.getElementById(config.formTitleId).textContent = config.createTitle;
            form.reset();
            form.setAttribute("action", config.createAction);

            const idField = document.getElementById(config.itemIdField);
            if (idField) idField.value = "";

            const typeField = document.getElementById(config.itemTypeField);
            if (typeField) typeField.value = config.itemType;

            document.getElementById(config.createButtonsId).style.display = "block";
            document.getElementById(config.updateButtonsId).style.display = "none";

            // Pulisci eventuali errori
            if (typeof config.clearAllErrors === "function") {
                config.clearAllErrors();
            }
        }
    }

    // ===== SUBMIT FORM =====
    async function submitForm() {
        const form = document.getElementById(config.formId);
        if (!form) {
            console.error(`crud-form-utils: form (#${config.formId}) non trovato.`);
            return;
        }

        const ok = config.validateFn ? config.validateFn() : true;
        if (!ok) {
            modalUtils.closeModal("createConfirmModal");
            modalUtils.closeModal("updateConfirmModal");
            return;
        }

        modalUtils.closeModal("createConfirmModal");
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
                const modalId = isUpdate ? "updateSuccessModal" : "createSuccessModal";

                const msg = `${config.labels.itemName} inviato al Curatore con successo!`;
                const span = document.querySelector(`#${modalId} p`);
                if (span) span.textContent = msg;
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

    // ===== UPDATE MODE =====
    function setUpdateModeForm(id) {
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
        init,
        toggleForm,
        submitForm,
        setUpdateModeForm
    };
})();
