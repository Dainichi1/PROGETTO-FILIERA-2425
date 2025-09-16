/**
 * Mostra una modale.
 */
function openModal(id) {
    const modal = document.getElementById(id);
    if (modal) {
        modal.classList.add("show");

        // chiudi al click esterno
        modal.addEventListener("click", function handler(e) {
            if (e.target === modal) {
                closeModal(id);
                modal.removeEventListener("click", handler);
            }
        });

        // chiudi con ESC
        function escHandler(ev) {
            if (ev.key === "Escape") {
                closeModal(id);
                document.removeEventListener("keydown", escHandler);
            }
        }
        document.addEventListener("keydown", escHandler);
    }
}

/**
 * Chiude una modale.
 */
function closeModal(id) {
    const modal = document.getElementById(id);
    if (modal) {
        modal.classList.remove("show");
    }
}

export const modalUtils = { openModal, closeModal };
