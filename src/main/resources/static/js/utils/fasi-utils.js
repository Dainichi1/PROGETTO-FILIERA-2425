import { modalUtils } from "./modal-utils.js";
import { formUtils } from "./form-utils.js";

/**
 * Utility per gestione fasi di produzione (Trasformatore)
 */
export const fasiUtils = (() => {
    /**
     * Reindicizza le fasi di produzione
     */
    function reindexFasi() {
        const fasi = document.querySelectorAll("#fasiList li");
        fasi.forEach((li, index) => {
            li.querySelectorAll("input, select, textarea").forEach(el => {
                if (el.name) el.name = el.name.replace(/\[.*?\]/, `[${index}]`);
            });
        });
    }

    /**
     * Carica i prodotti approvati per un produttore
     */
    function caricaProdottiPerProduttore(username) {
        const select = document.getElementById("faseProdottoBase");
        select.innerHTML = "<option value=''>Caricamento...</option>";

        if (!username) {
            select.innerHTML = "<option value=''>-- Seleziona prima un produttore --</option>";
            return;
        }

        fetch(`/trasformatore/prodotti/${encodeURIComponent(username)}`, {
            headers: { "Accept": "application/json" }
        })
            .then(res => {
                if (!res.ok) throw new Error("Errore nella risposta del server");
                return res.json();
            })
            .then(prodotti => {
                if (!prodotti?.length) {
                    select.innerHTML = "<option value=''>-- Nessun prodotto approvato --</option>";
                } else {
                    select.innerHTML =
                        "<option value=''>-- Seleziona --</option>" +
                        prodotti.map(p => `<option value="${p.id}">${p.nome}</option>`).join("");
                }
            })
            .catch(err => {
                console.error("Errore caricamento prodotti:", err);
                select.innerHTML = "<option value=''>-- Errore caricamento --</option>";
            });
    }

    /**
     * Aggiunge una fase alla lista
     */
    function aggiungiFase() {
        const descrizioneEl = document.getElementById("faseDescrizione");
        const produttoreEl = document.getElementById("faseProduttore");
        const prodottoBaseEl = document.getElementById("faseProdottoBase");

        let ok = true;
        formUtils.clearFieldError(descrizioneEl);
        formUtils.clearFieldError(produttoreEl);
        formUtils.clearFieldError(prodottoBaseEl);

        if (!descrizioneEl.value.trim()) {
            formUtils.setFieldError(descrizioneEl, "⚠ Inserisci una descrizione");
            ok = false;
        }
        if (!produttoreEl.value) {
            formUtils.setFieldError(produttoreEl, "⚠ Seleziona un produttore");
            ok = false;
        }
        if (!prodottoBaseEl.value) {
            formUtils.setFieldError(prodottoBaseEl, "⚠ Seleziona un prodotto");
            ok = false;
        }

        if (!ok) return;

        const list = document.getElementById("fasiList");
        const index = list.querySelectorAll("li").length;

        const li = document.createElement("li");
        li.innerHTML = `
            <strong>${descrizioneEl.value}</strong> 
            (Produttore: ${produttoreEl.value}, Prodotto: ${prodottoBaseEl.options[prodottoBaseEl.selectedIndex].textContent})
            <button type="button" class="btn-remove-fase">❌ Rimuovi</button>

            <input type="hidden" name="fasiProduzione[${index}].descrizioneFase" value="${descrizioneEl.value}">
            <input type="hidden" name="fasiProduzione[${index}].produttoreUsername" value="${produttoreEl.value}">
            <input type="hidden" name="fasiProduzione[${index}].prodottoOrigineId" value="${prodottoBaseEl.value}">
        `;

        list.appendChild(li);

        // reset campi
        descrizioneEl.value = "";
        produttoreEl.value = "";
        prodottoBaseEl.innerHTML = "<option value=''>-- Seleziona prima un produttore --</option>";

        modalUtils.closeModal("faseModal");
    }

    /**
     * Aggiunge una fase al DOM (per prefill)
     */
    function appendFaseLi(index, descrizione, produttore, prodottoBase, produttoreLabel, prodottoLabel) {
        const list = document.getElementById("fasiList");

        const li = document.createElement("li");
        li.innerHTML = `
            <strong>${descrizione}</strong> 
            (Produttore: ${produttoreLabel}, Prodotto: ${prodottoLabel})
            <button type="button" class="btn-remove-fase">❌ Rimuovi</button>

            <input type="hidden" name="fasiProduzione[${index}].descrizioneFase" value="${descrizione}">
            <input type="hidden" name="fasiProduzione[${index}].produttoreUsername" value="${produttore}">
            <input type="hidden" name="fasiProduzione[${index}].prodottoOrigineId" value="${prodottoBase}">
        `;

        list.appendChild(li);
    }

    /**
     * Inizializza listener per la modale
     */
    function init() {
        const produttoreEl = document.getElementById("faseProduttore");
        const btnAdd = document.querySelector(".btn-add-fase");

        if (produttoreEl) {
            produttoreEl.addEventListener("change", () => caricaProdottiPerProduttore(produttoreEl.value));
        }
        if (btnAdd) {
            btnAdd.addEventListener("click", aggiungiFase);
        }

        document.addEventListener("click", e => {
            if (e.target.classList.contains("btn-remove-fase")) {
                e.target.parentElement.remove();
                reindexFasi();
            }
        });
    }

    return { reindexFasi, caricaProdottiPerProduttore, aggiungiFase, appendFaseLi, init };
})();
