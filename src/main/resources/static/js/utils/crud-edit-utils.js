import { modalUtils } from "./modal-utils.js";

/**
 * Utility per gestione edit/aggiornamento CRUD
 */
export const editUtilsCrud = (() => {
    let config = {};

    function init(cfg) {
        config = cfg;
    }

    /**
     * Gestione click sul pulsante "Modifica"
     */
    async function handleEditClick(button) {
        const stato = button.getAttribute("data-stato");
        const id = button.getAttribute("data-id");

        if (stato !== config.editableState) {
            modalUtils.openModal(config.editErrorModalId);
            return;
        }

        const form = document.getElementById(config.formId);
        if (stato === "RIFIUTATO") {
            form.setAttribute("action", config.updateRejectedAction);
        } else {
            form.setAttribute("action", config.updateAction);
        }

        setUpdateModeForm(id);

        if (typeof clearAllErrors === "function") {
            clearAllErrors();
        }

        try {
            const res = await fetch(config.fetchUrl(id), { credentials: "same-origin" });
            if (!res.ok) throw new Error(await res.text());

            const data = await res.json();
            config.prefillFormFn(data); // funzione di prefill passata in config
        } catch (e) {
            alert("Impossibile caricare: " + (e.message || "errore"));
        }
    }

    /**
     * Configura il form in modalità aggiornamento
     */
    function setUpdateModeForm(id) {
        document.getElementById(config.formTitleId).textContent = config.updateTitle;
        const form = document.getElementById(config.formId);
        form.setAttribute("action", config.updateAction);

        document.getElementById(config.itemIdField).value = id || "";
        document.getElementById(config.itemTypeField).value = config.itemType;

        document.getElementById(config.createButtonsId).style.display = "none";
        document.getElementById(config.updateButtonsId).style.display = "block";
        document.getElementById(config.formContainerId).style.display = "block";
    }

    return {
        init,
        handleEditClick,
        setUpdateModeForm
    };
})();
