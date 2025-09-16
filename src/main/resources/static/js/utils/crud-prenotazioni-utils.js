import { modalUtils } from "./modal-utils.js";
import { csrfUtils } from "./csrf-utils.js";

/**
 * Utils per la gestione delle prenotazioni delle visite
 */
export const prenotazioniUtils = (() => {

    function openPrenotazioneVisitaModal(button) {
        const visitaId = button.getAttribute("data-id");
        const inputHidden = document.getElementById("idVisita");
        if (inputHidden) inputHidden.value = visitaId;

        const numeroInput = document.getElementById("numeroPersone");
        if (numeroInput) numeroInput.value = "";

        const errorSpan = document.getElementById("numeroPersoneError");
        if (errorSpan) errorSpan.textContent = "";

        modalUtils.openModal("prenotazioneVisitaModal");
    }

    // VALIDAZIONE + INVIO AJAX CREAZIONE
    function attachPrenotazioneVisitaValidation() {
        const form = document.getElementById("prenotazioneVisitaForm");
        if (!form) return;

        form.setAttribute("novalidate", "true");

        form.addEventListener("submit", async (e) => {
            e.preventDefault();

            const idVisita = document.getElementById("idVisita").value;
            const numeroPersone = document.getElementById("numeroPersone").value;
            const errorSpan = document.getElementById("numeroPersoneError");
            if (errorSpan) errorSpan.textContent = "";

            if (!numeroPersone || parseInt(numeroPersone) < 1) {
                if (errorSpan) errorSpan.textContent = "⚠ Devi inserire almeno 1 persona";
                else alert("⚠ Devi inserire almeno 1 persona");
                return;
            }

            const { header, token } = csrfUtils.getCsrf();

            try {
                const res = await fetch("/prenotazioni-visite/prenota", {
                    method: "POST",
                    headers: { [header]: token },
                    body: new URLSearchParams({ idVisita, numeroPersone }),
                    credentials: "same-origin"
                });

                modalUtils.closeModal("prenotazioneVisitaModal");

                if (res.ok) {
                    const msg = await res.text();
                    document.getElementById("prenotazioneSuccessMessage")?.replaceChildren(document.createTextNode(msg || "✅ Prenotazione effettuata con successo!"));
                    modalUtils.openModal("prenotazioneSuccessModal");
                } else {
                    const msg = await res.text();
                    document.getElementById("prenotazioneErrorMessage")?.replaceChildren(document.createTextNode(msg || "❌ Errore durante la prenotazione."));
                    modalUtils.openModal("prenotazioneErrorModal");
                }
            } catch (err) {
                modalUtils.closeModal("prenotazioneVisitaModal");
                document.getElementById("prenotazioneErrorMessage")?.replaceChildren(document.createTextNode("❌ Errore di rete: " + err.message));
                modalUtils.openModal("prenotazioneErrorModal");
            }
        });
    }

    /**
     * Apre la modale per eliminare una prenotazione
     */
    function openDeletePrenotazioneModal(id) {
        const inputHidden = document.getElementById("idPrenotazioneDelete");
        if (inputHidden) inputHidden.value = id;
        modalUtils.openModal("prenotazioneDeleteModal");
    }

    /**
     * Intercetta il submit del form di delete e chiama il vero endpoint DELETE
     */
    function attachPrenotazioneDeleteHandler() {
        const form = document.getElementById("prenotazioneDeleteForm");
        if (!form) return;

        form.addEventListener("submit", async (e) => {
            e.preventDefault(); // niente submit classico

            const id = document.getElementById("idPrenotazioneDelete")?.value;
            if (!id) return;

            const { header, token } = csrfUtils.getCsrf();

            try {
                const res = await fetch(`/prenotazioni-visite/${id}`, {
                    method: "DELETE",
                    headers: { [header]: token },
                    credentials: "same-origin"
                });

                modalUtils.closeModal("prenotazioneDeleteModal");

                if (res.ok) {
                    // Rimuovo la riga dalla tabella se presente
                    const btn = document.querySelector(`.btn-delete-prenotazione[data-id="${id}"]`);
                    const row = btn?.closest("tr");
                    row?.remove();

                    modalUtils.openModal("prenotazioneDeleteSuccessModal");
                } else {
                    const msg = await res.text();
                    document.getElementById("prenotazioneDeleteErrorText")?.replaceChildren(document.createTextNode(msg || "❌ Errore durante l'eliminazione della prenotazione."));
                    modalUtils.openModal("prenotazioneDeleteErrorModal");
                }
            } catch (err) {
                modalUtils.closeModal("prenotazioneDeleteModal");
                document.getElementById("prenotazioneDeleteErrorText")?.replaceChildren(document.createTextNode("❌ Errore di rete: " + err.message));
                modalUtils.openModal("prenotazioneDeleteErrorModal");
            }
        });
    }

    return {
        openPrenotazioneVisitaModal,
        attachPrenotazioneVisitaValidation,
        openDeletePrenotazioneModal,
        attachPrenotazioneDeleteHandler
    };
})();
