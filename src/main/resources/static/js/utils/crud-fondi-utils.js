import { modalUtils } from "./modal-utils.js";
import { csrfUtils } from "./csrf-utils.js";

export const fondiUtils = {
    initFondiForm() {
        const form = document.getElementById("fondiForm");
        if (!form) return;

        form.addEventListener("submit", e => {
            e.preventDefault();
            const importoInput = document.getElementById("fondiInput");
            const importo = Number(importoInput.value);

            if (isNaN(importo) || importo <= 0) {
                const msgEl = document.getElementById("fondiErrorMessage");
                if (msgEl) {
                    msgEl.innerText = "⚠ L'importo deve essere maggiore di zero"; // stesso messaggio del Validator
                }
                modalUtils.openModal("fondiErrorModal");
                return;
            }

            const csrf = csrfUtils.getCsrf();

            fetch("/acquirente/update-fondi", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    [csrf.header]: csrf.token
                },
                body: JSON.stringify({ importo })
            })
                .then(r => r.json())
                .then(data => {
                    if (data.success) {
                        // Aggiorna fondi a schermo
                        const fondiEl = document.getElementById("fondiDisponibili");
                        if (fondiEl) {
                            fondiEl.innerText = `Fondi disponibili: ${data.nuoviFondi} €`;
                        }

                        const msgEl = document.getElementById("fondiSuccessMessage");
                        if (msgEl) {
                            msgEl.innerText = `✅ Fondi aggiornati: saldo attuale ${data.nuoviFondi} €`;
                        }

                        modalUtils.openModal("fondiSuccessModal");
                        form.reset();
                    } else {
                        const msgEl = document.getElementById("fondiErrorMessage");
                        if (msgEl) {
                            msgEl.innerText = data.message || "⚠ Inserisci un importo valido maggiore di zero.";
                        }
                        modalUtils.openModal("fondiErrorModal");
                    }
                })
                .catch(() => {
                    const msgEl = document.getElementById("fondiGenericErrorMessage");
                    if (msgEl) {
                        msgEl.innerText = "❌ Errore di sistema: impossibile aggiornare i fondi.";
                    }
                    modalUtils.openModal("fondiGenericErrorModal");
                });
        });
    }
};
