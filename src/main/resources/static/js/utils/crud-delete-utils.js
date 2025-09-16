import { modalUtils } from "./modal-utils.js";
import { csrfUtils } from "./csrf-utils.js";

/**
 * Utility per gestione eliminazioni CRUD
 */
export const deleteUtilsCrud = (() => {
    let config = {};
    let toDelete = { id: null, nome: null };

    function init(cfg) {
        config = cfg;
    }

    // ===== PREPARA DELETE =====
    function handleDeleteClick(button) {
        const stato = button.getAttribute("data-stato");
        const id = button.getAttribute("data-id");
        const nome = button.getAttribute("data-nome");

        if (!config.deletableStates.includes(stato)) {
            modalUtils.openModal(config.deleteErrorModalId);
            return;
        }

        toDelete = { id, nome };
        document.getElementById(config.deleteConfirmMessageId).textContent =
            `Eliminare "${nome}"?`;
        modalUtils.openModal(config.deleteConfirmModalId);
    }

    // ===== CONFERMA DELETE =====
    async function confirmDelete() {
        modalUtils.closeModal(config.deleteConfirmModalId);

        const { header, token } = csrfUtils.getCsrf();

        try {
            const res = await fetch(config.deleteUrl(toDelete.id), {
                method: "DELETE",
                headers: { [header]: token },
                credentials: "same-origin"
            });

            const text = await res.text();
            if (res.ok) {
                document.querySelector(`tr[data-id="${toDelete.id}"]`)?.remove();

                document.getElementById(config.deleteSuccessMessageId).textContent =
                    text || `${config.labels.itemName} "${toDelete.nome}" eliminato con successo`;

                modalUtils.openModal(config.deleteSuccessModalId);
            } else {
                document.getElementById(config.deleteGenericErrorMessageId).textContent =
                    text || "Errore durante l'eliminazione.";
                modalUtils.openModal(config.deleteGenericErrorModalId);
            }
        } catch {
            document.getElementById(config.deleteGenericErrorMessageId).textContent =
                "Errore di rete durante l'eliminazione.";
            modalUtils.openModal(config.deleteGenericErrorModalId);
        }
    }

    return {
        init,
        handleDeleteClick,
        confirmDelete
    };
})();
