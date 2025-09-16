import { crudUtils } from "./crud-core.js";

/**
 * Utils generici per selezione riga e pubblicazione social
 * Compatibile con tutti gli attori (curatore, admin, ente, animatore).
 */
export const socialSelectionUtils = (() => {
    let selectedRow = null;
    let selectedId = null;
    let selectedType = null;
    let currentCrud = null;

    /**
     * Inizializza la selezione riga e la pubblicazione social
     * @param {Object} options
     * @param {string} options.rowSelector - selettore CSS per le righe (es: ".selectable-row")
     * @param {string} options.btnPubblicaId - id del bottone "Pubblica"
     * @param {Object} options.crudMap - mappa { tipo -> crudInstance }
     */
    function init({ rowSelector, btnPubblicaId, crudMap }) {
        const btnPubblica = document.getElementById(btnPubblicaId);
        if (!btnPubblica) return;

        btnPubblica.disabled = true; // inizialmente disabilitato

        // selezione riga
        document.querySelectorAll(rowSelector).forEach(row => {
            row.addEventListener("click", () => {
                document.querySelectorAll(rowSelector).forEach(r => r.classList.remove("selected"));
                row.classList.add("selected");

                selectedRow = row;
                selectedId = row.getAttribute("data-id");
                selectedType = row.getAttribute("data-type")?.trim().toUpperCase();

                currentCrud = crudMap?.[selectedType] || null;
                btnPubblica.disabled = !currentCrud;

                if (!currentCrud) {
                    console.warn(`[socialSelectionUtils] Nessun CRUD trovato per type: ${selectedType}`);
                }
            });
        });

        // pubblicazione social
        btnPubblica.addEventListener("click", () => {
            if (!selectedRow || !currentCrud) return;

            // === Caso attori standard (admin/curatore/ente) ===
            if (typeof currentCrud.openSocialModal === "function") {
                window.currentCrud = currentCrud;
                currentCrud.openSocialModal(selectedId, selectedType);
                return;
            }

            // === Caso Animatore ===
            if (typeof currentCrud.openSocialConfirm === "function") {
                window.currentCrud = currentCrud;
                currentCrud.openSocialConfirm(selectedId);
                return;
            }

            console.error(`[socialSelectionUtils] Nessun metodo social valido per type: ${selectedType}`);
        });

        // bottone "OK" nella modale social (per attori standard)
        const btnOk = document.getElementById("btnOkSocialPost");
        if (btnOk) {
            btnOk.onclick = () => {
                const crud = window.currentCrud || crudUtils;
                if (crud?.openSocialConfirm) {
                    crud.openSocialConfirm(selectedId);
                } else {
                    console.error("[socialSelectionUtils] Nessuna funzione openSocialConfirm trovata");
                }
            };
        }

        // bottone "Sì" nella modale di conferma
        const btnConfirm = document.getElementById("btnConfirmSocialPost");
        if (btnConfirm) {
            btnConfirm.onclick = () => {
                const crud = window.currentCrud || crudUtils;
                if (crud?.submitSocialPost) {
                    crud.submitSocialPost();
                } else {
                    console.error("[socialSelectionUtils] Nessuna funzione submitSocialPost trovata");
                }
            };
        }
    }

    return { init };
})();
