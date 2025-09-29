import { formUtils } from "./form-utils.js";

/**
 * Utility di validazione (live e custom)
 */
export const validationUtilsCrud = (() => {

    // ===== LIVE VALIDATION =====
    function attachLiveValidation(formId) {
        if (!formId) return;
        const form = document.getElementById(formId);
        if (!form) return;

        form.querySelectorAll("input, textarea, select").forEach(f => {
            f.addEventListener("input", () => formUtils.clearFieldError(f));
            f.addEventListener("change", () => formUtils.clearFieldError(f));
        });
    }

    function attachSocialLiveValidation() {
        ["postTitle", "postText"].forEach(id => {
            const field = document.getElementById(id);
            if (field) {
                field.addEventListener("input", () => formUtils.clearFieldError(field));
                field.addEventListener("change", () => formUtils.clearFieldError(field));
            }
        });
    }

    function attachPrenotazioneVisitaValidation() {
        const form = document.getElementById("prenotazioneVisitaForm");
        if (!form) return;

        form.setAttribute("novalidate", "true"); // disattiva messaggi standard del browser

        form.addEventListener("submit", (e) => {
            const input = document.getElementById("numeroPersone");
            const errorSpan = document.getElementById("numeroPersoneError");

            if (errorSpan) errorSpan.textContent = ""; // reset messaggi

            if (!input.value || parseInt(input.value) < 1) {
                e.preventDefault(); // blocca invio
                if (errorSpan) {
                    errorSpan.textContent = "⚠ Devi inserire almeno 1 persona";
                } else {
                    alert("⚠ Devi inserire almeno 1 persona");
                }
            }
        });
    }

    // ===== VALIDAZIONE SPECIFICA VISITA =====
    function validateVisita() {
        formUtils.clearAllErrors("visitaForm");

        const nome = document.getElementById("nome-visitaDto");
        const descrizione = document.getElementById("descrizione-visitaDto");
        const indirizzo = document.getElementById("indirizzo-visitaDto");
        const dataInizio = document.getElementById("dataInizio-visitaDto");
        const dataFine = document.getElementById("dataFine-visitaDto");
        const destinatari = document.querySelectorAll("input[name='destinatari']:checked");

        let ok = true;

        if (!nome?.value.trim()) {
            formUtils.setFieldError("nome-visitaDto", "⚠ Nome obbligatorio");
            ok = false;
        }
        if (!descrizione?.value.trim()) {
            formUtils.setFieldError("descrizione-visitaDto", "⚠ Descrizione obbligatoria");
            ok = false;
        }
        if (!indirizzo?.value.trim()) {
            formUtils.setFieldError("indirizzo-visitaDto", "⚠ Indirizzo obbligatorio");
            ok = false;
        }

        const dInizio = dataInizio?.value ? new Date(dataInizio.value) : null;
        const dFine = dataFine?.value ? new Date(dataFine.value) : null;

        if (!dInizio) {
            formUtils.setFieldError("dataInizio-visitaDto", "⚠ Data inizio obbligatoria");
            ok = false;
        }
        if (!dFine) {
            formUtils.setFieldError("dataFine-visitaDto", "⚠ Data fine obbligatoria");
            ok = false;
        }
        if (dInizio && dFine && dFine <= dInizio) {
            formUtils.setFieldError("dataFine-visitaDto", "⚠ La data di fine deve essere successiva a quella di inizio");
            ok = false;
        }

        if (!destinatari || destinatari.length === 0) {
            formUtils.setFieldError("destinatari-container", "⚠ Devi selezionare almeno un destinatario");
            ok = false;
        }

        return ok;
    }

    // ===== VALIDAZIONE SPECIFICA FIERA =====
    function validateFiera() {
        formUtils.clearAllErrors("fieraForm");

        const nome = document.getElementById("nome-fieraDto");
        const descrizione = document.getElementById("descrizione-fieraDto");
        const indirizzo = document.getElementById("indirizzo-fieraDto");
        const prezzo = document.getElementById("prezzo-fieraDto");
        const dataInizio = document.getElementById("dataInizio-fieraDto");
        const dataFine = document.getElementById("dataFine-fieraDto");

        let ok = true;

        if (!nome?.value.trim()) {
            formUtils.setFieldError("nome-fieraDto", "⚠ Nome obbligatorio");
            ok = false;
        }
        if (!descrizione?.value.trim()) {
            formUtils.setFieldError("descrizione-fieraDto", "⚠ Descrizione obbligatoria");
            ok = false;
        }
        if (!indirizzo?.value.trim()) {
            formUtils.setFieldError("indirizzo-fieraDto", "⚠ Indirizzo obbligatorio");
            ok = false;
        }

        const prezzoValue = prezzo?.value ? parseFloat(prezzo.value) : null;
        if (prezzoValue === null || isNaN(prezzoValue)) {
            formUtils.setFieldError("prezzo-fieraDto", "⚠ Prezzo obbligatorio");
            ok = false;
        } else if (prezzoValue < 0) {
            formUtils.setFieldError("prezzo-fieraDto", "⚠ Il prezzo non può essere negativo");
            ok = false;
        }

        const dInizio = dataInizio?.value ? new Date(dataInizio.value) : null;
        const dFine = dataFine?.value ? new Date(dataFine.value) : null;

        if (!dInizio) {
            formUtils.setFieldError("dataInizio-fieraDto", "⚠ Data inizio obbligatoria");
            ok = false;
        }
        if (!dFine) {
            formUtils.setFieldError("dataFine-fieraDto", "⚠ Data fine obbligatoria");
            ok = false;
        }
        if (dInizio && dFine && dFine <= dInizio) {
            formUtils.setFieldError("dataFine-fieraDto", "⚠ La data di fine deve essere successiva a quella di inizio");
            ok = false;
        }

        return ok;
    }

    return {
        attachLiveValidation,
        attachSocialLiveValidation,
        attachPrenotazioneVisitaValidation,
        validateVisita,
        validateFiera
    };
})();
