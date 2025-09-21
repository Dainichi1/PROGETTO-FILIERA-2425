import {modalUtils} from "./modal-utils.js";
import {csrfUtils} from "./csrf-utils.js";
import {formUtils} from "./form-utils.js";

/**
 * Utils per la gestione delle prenotazioni delle fiere
 */
export const prenotazioniFiereUtils = (() => {

    // ================== APERTURA MODALE PRENOTAZIONE ==================
    function openPrenotazioneFieraModal(button) {
        const fieraId = button.getAttribute("data-id");

        // set hidden id
        const inputHidden = document.getElementById("idFiera");
        if (inputHidden) inputHidden.value = fieraId;

        // reset numero persone
        const numeroInput = document.getElementById("numeroPersoneFiera");
        if (numeroInput) {
            numeroInput.value = "";
            formUtils.clearFieldError(numeroInput);
        }

        // reset error span
        const errorSpan = document.getElementById("numeroPersoneFieraError");
        if (errorSpan) errorSpan.textContent = "";

        modalUtils.openModal("prenotazioneFieraModal");
    }

    // ================== FORMATTAZIONE FONDI ==================
    function aggiornaFondi(nuoviFondi) {
        const fondiEl = document.getElementById("fondiDisponibili");
        if (fondiEl) {
            fondiEl.innerText = `Fondi disponibili: ${nuoviFondi.toLocaleString("it-IT", {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            })} €`;
        }
    }

    // ================== CREAZIONE PRENOTAZIONE ==================
    function attachPrenotazioneFieraValidation() {
        const form = document.getElementById("prenotazioneFieraForm");
        if (!form) return;

        form.setAttribute("novalidate", "true");

        const numeroInput = document.getElementById("numeroPersoneFiera");
        if (numeroInput) {
            numeroInput.addEventListener("input", () => formUtils.clearFieldError(numeroInput));
            numeroInput.addEventListener("change", () => formUtils.clearFieldError(numeroInput));
        }

        form.addEventListener("submit", async (e) => {
            e.preventDefault();

            const idFiera = document.getElementById("idFiera").value;
            const numeroPersone = numeroInput?.value;
            const errorSpan = document.getElementById("numeroPersoneFieraError");
            if (errorSpan) errorSpan.textContent = "";

            if (!numeroPersone || parseInt(numeroPersone) < 1) {
                if (errorSpan) errorSpan.textContent = "⚠ Devi inserire almeno 1 persona";
                else alert("⚠ Devi inserire almeno 1 persona");
                return;
            }

            const {header, token} = csrfUtils.getCsrf();

            try {
                const res = await fetch("/prenotazioni-fiere/prenota", {
                    method: "POST",
                    headers: {[header]: token},
                    body: new URLSearchParams({idFiera, numeroPersone}),
                    credentials: "same-origin"
                });

                modalUtils.closeModal("prenotazioneFieraModal");

                const data = await res.json();

                if (res.ok && data.success) {
                    document.getElementById("prenotazioneFieraSuccessMessage")
                        ?.replaceChildren(document.createTextNode(data.message || "✅ Prenotazione effettuata con successo!"));
                    modalUtils.openModal("prenotazioneFieraSuccessModal");

                    // aggiorna fondi
                    if (typeof data.nuoviFondi !== "undefined") {
                        aggiornaFondi(data.nuoviFondi);
                    }
                } else {
                    document.getElementById("prenotazioneFieraErrorMessage")
                        ?.replaceChildren(document.createTextNode(data.message || "❌ Errore durante la prenotazione."));
                    modalUtils.openModal("prenotazioneFieraErrorModal");
                }
            } catch (err) {
                modalUtils.closeModal("prenotazioneFieraModal");
                document.getElementById("prenotazioneFieraErrorMessage")
                    ?.replaceChildren(document.createTextNode("❌ Errore di rete: " + err.message));
                modalUtils.openModal("prenotazioneFieraErrorModal");
            }
        });
    }

    // ================== ELIMINAZIONE PRENOTAZIONE ==================
    function openDeletePrenotazioneFieraModal(id) {
        const inputHidden = document.getElementById("idPrenotazioneFieraDelete");
        if (inputHidden) inputHidden.value = id;
        modalUtils.openModal("prenotazioneFieraDeleteModal");
    }

    function attachPrenotazioneFieraDeleteHandler() {
        const form = document.getElementById("prenotazioneFieraDeleteForm");
        if (!form) return;

        form.addEventListener("submit", async (e) => {
            e.preventDefault();

            const id = document.getElementById("idPrenotazioneFieraDelete")?.value;
            if (!id) return;

            const {header, token} = csrfUtils.getCsrf();

            try {
                const res = await fetch(`/prenotazioni-fiere/${id}`, {
                    method: "DELETE",
                    headers: {[header]: token},
                    credentials: "same-origin"
                });

                modalUtils.closeModal("prenotazioneFieraDeleteModal");

                const data = await res.json();

                if (res.ok && data.success) {
                    const btn = document.querySelector(`.btn-delete-prenotazione-fiera[data-id="${id}"]`);
                    const row = btn?.closest("tr");
                    row?.remove();

                    // aggiorna fondi
                    if (typeof data.nuoviFondi !== "undefined") {
                        aggiornaFondi(data.nuoviFondi);
                    }

                    modalUtils.openModal("prenotazioneFieraDeleteSuccessModal");
                } else {
                    const msg = data?.message || "❌ Errore durante l'eliminazione della prenotazione fiera.";
                    document.getElementById("prenotazioneFieraDeleteErrorText")
                        ?.replaceChildren(document.createTextNode(msg));
                    modalUtils.openModal("prenotazioneFieraDeleteErrorModal");
                }
            } catch (err) {
                modalUtils.closeModal("prenotazioneFieraDeleteModal");
                document.getElementById("prenotazioneFieraDeleteErrorText")
                    ?.replaceChildren(document.createTextNode("❌ Errore di rete: " + err.message));
                modalUtils.openModal("prenotazioneFieraDeleteErrorModal");
            }
        });
    }

    return {
        openPrenotazioneFieraModal,
        attachPrenotazioneFieraValidation,
        openDeletePrenotazioneFieraModal,
        attachPrenotazioneFieraDeleteHandler
    };
})();
