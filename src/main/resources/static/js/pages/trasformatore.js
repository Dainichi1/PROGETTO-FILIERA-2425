// ================== IMPORT ==================
import { crudUtils } from "../utils/crud-utils.js";
import { toggleUtils } from "../utils/toggle-utils.js";
import { formUtils } from "../utils/form-utils.js";
import { modalUtils } from "../utils/modal-utils.js";
import { prenotazioniUtils } from "../utils/crud-prenotazioni-utils.js";
import { socialSelectionUtils } from "../utils/social-selection-utils.js";

// ================== VALIDAZIONE TRASFORMATO ==================
function clientValidateTrasformato() {
    formUtils.clearAllErrors("itemForm");

    const nome = document.getElementById("nome-trasformatoDto");
    const descrizione = document.getElementById("descrizione-trasformatoDto");
    const indirizzo = document.getElementById("indirizzo-trasformatoDto");
    const quantita = document.getElementById("quantita-trasformatoDto");
    const prezzo = document.getElementById("prezzo-trasformatoDto");
    const certificati = document.getElementById("certificati-trasformatoDto");
    const foto = document.getElementById("foto-trasformatoDto");

    let ok = true;

    if (!nome.value.trim()) { formUtils.setFieldError("nome-trasformatoDto","⚠ Nome obbligatorio"); ok = false; }
    if (!descrizione.value.trim()) { formUtils.setFieldError("descrizione-trasformatoDto","⚠ Descrizione obbligatoria"); ok = false; }
    if (!indirizzo.value.trim()) { formUtils.setFieldError("indirizzo-trasformatoDto","⚠ Indirizzo obbligatorio"); ok = false; }

    const q = quantita.value !== "" ? Number(quantita.value) : NaN;
    if (Number.isNaN(q) || q < 1) { formUtils.setFieldError("quantita-trasformatoDto","⚠ La quantità deve essere almeno 1"); ok = false; }

    const p = prezzo.value !== "" ? Number(prezzo.value) : NaN;
    if (Number.isNaN(p) || p <= 0) { formUtils.setFieldError("prezzo-trasformatoDto","⚠ Il prezzo deve essere positivo"); ok = false; }

    if (!certificati.files?.length) { formUtils.setFieldError("certificati-trasformatoDto","⚠ Devi caricare almeno un certificato"); ok = false; }
    if (!foto.files?.length) { formUtils.setFieldError("foto-trasformatoDto","⚠ Devi caricare almeno una foto"); ok = false; }

    // Aggiornato: usa l'id fasiListErr
    const numFasi = document.querySelectorAll("#fasiList li").length;
    const fasiErr = document.getElementById("fasiListErr");
    if (numFasi < 2) {
        if (fasiErr) fasiErr.textContent = "⚠ Devi inserire almeno 2 fasi di produzione";
        ok = false;
    } else if (fasiErr) {
        fasiErr.textContent = "";
    }

    return ok;
}

// ================== CRUD TRASFORMATO ==================
const trasformatoCrud = crudUtils.createInstance({
    formId: "itemForm",
    formContainerId: "trasformatoForm",
    formTitleId: "formTitle",
    itemIdField: "itemId",
    itemTypeField: "itemTipo",
    createButtonsId: "createButtons",
    updateButtonsId: "updateButtons",
    itemType: "TRASFORMATO",
    createTitle: "Nuovo Prodotto Trasformato",
    updateTitle: "Modifica Prodotto Trasformato Rifiutato",
    createAction: "/trasformatore/crea",
    updateAction: "/venditore/item/modifica",
    updateRejectedAction: "/venditore/item/modifica-rifiutato",

    deleteUrl: id => `/venditore/item/elimina/${id}?tipo=TRASFORMATO`,
    fetchUrl: id => `/venditore/item/fetch/${id}?tipo=TRASFORMATO`,
    deletableStates: ["IN_ATTESA","RIFIUTATO"],
    editableState: "RIFIUTATO",

    deleteErrorModalId: "deleteErrorModal",
    deleteConfirmModalId: "deleteConfirmModal",
    deleteSuccessModalId: "deleteSuccessModal",
    deleteGenericErrorModalId: "deleteGenericErrorModal",
    editErrorModalId: "editErrorModal",

    deleteConfirmMessageId: "deleteConfirmMessage",
    deleteSuccessMessageId: "deleteSuccessMessage",
    deleteGenericErrorMessageId: "deleteGenericErrorMessage",

    labels: { itemName: "Prodotto Trasformato" },

    validateFn: () => clientValidateTrasformato(),

    prefillFormFn: t => {
        document.getElementById("nome-trasformatoDto").value = t.nome ?? "";
        document.getElementById("descrizione-trasformatoDto").value = t.descrizione ?? "";
        document.getElementById("quantita-trasformatoDto").value = t.quantita ?? "";
        document.getElementById("prezzo-trasformatoDto").value = t.prezzo ?? "";
        document.getElementById("indirizzo-trasformatoDto").value = t.indirizzo ?? "";

        const list = document.getElementById("fasiList");
        list.innerHTML = "";
        (t.fasiProduzione || []).forEach((fase) =>
            appendFaseLi(fase.descrizioneFase, fase.produttoreUsername, fase.prodottoOrigineId)
        );
        reindexFasi();
    }
});

// Override: pubblicazione social solo se APPROVATO
trasformatoCrud.handleSocialClick = (button) => {
    const stato = (button.getAttribute("data-stato") || "").trim().toUpperCase();
    const type = button.getAttribute("data-type");
    const id = button.getAttribute("data-id");

    console.log("DEBUG → click su publish", { stato, type, id });

    if (stato !== "APPROVATO") {
        const msg = `⚠ Puoi pubblicare solo ${type} con stato APPROVATO.`;
        console.log("DEBUG → apro socialNotAvailableModal con messaggio:", msg);
        const msgEl = document.getElementById("socialNotAvailableMessage");
        if (msgEl) msgEl.innerText = msg;
        modalUtils.openModal("socialNotAvailableModal");
        return;
    }

    console.log("DEBUG → apro socialPostModal standard", { id, type });
    trasformatoCrud.openSocialModal(id, type);
};


// ================== FASI: helpers minimi ==================
function appendFaseLi(descrizione, produttore, prodottoId) {
    const ul = document.getElementById("fasiList");
    const li = document.createElement("li");
    li.className = "fase-item";
    li.innerHTML = `
    <span class="fase-text">${descrizione} — ${produttore} — #${prodottoId}</span>
    <button type="button" class="btn-small btn-remove-fase">Rimuovi</button>
    <input type="hidden" name="" data-field="descrizioneFase" value="${descrizione}">
    <input type="hidden" name="" data-field="produttoreUsername" value="${produttore}">
    <input type="hidden" name="" data-field="prodottoOrigineId" value="${prodottoId}">
  `;
    ul.appendChild(li);
}

function reindexFasi() {
    document.querySelectorAll("#fasiList li").forEach((li, idx) => {
        li.querySelectorAll('input[type="hidden"]').forEach(h => {
            const field = h.getAttribute("data-field");
            h.name = `fasiProduzione[${idx}].${field}`;
        });
    });
}

// ================== INIT ==================
document.addEventListener("DOMContentLoaded", () => {
    // Social feed
    document.getElementById("btnSocialFeed")?.addEventListener("click", () => {
        // se hai un helper globale: crudUtils.openSocialFeed();
        // altrimenti apri la modale:
        document.getElementById("socialFeedModal") && modalUtils.openModal("socialFeedModal");
    });

    // Toggle sezioni
    document.getElementById("btnVisite")?.addEventListener("click", () => toggleUtils.toggleSection("visiteSection"));
    document.getElementById("btnPrenotazioni")?.addEventListener("click", () => toggleUtils.toggleSection("prenotazioniSection"));
    document.getElementById("btnCloseVisite")?.addEventListener("click", () => toggleUtils.toggleSection("visiteSection"));
    document.getElementById("btnClosePrenotazioni")?.addEventListener("click", () => toggleUtils.toggleSection("prenotazioniSection"));
// Bottone Social Feed
    document.getElementById("btnSocialFeed")?.addEventListener("click", () => {
        crudUtils.openSocialFeed();
    });

    // Pulsanti tabella
    document.querySelectorAll(".btn-delete").forEach(btn => btn.addEventListener("click", () => trasformatoCrud.handleDeleteClick(btn)));
    document.querySelectorAll(".btn-edit").forEach(btn => btn.addEventListener("click", () => trasformatoCrud.handleEditClick(btn)));
    document.querySelectorAll(".btn-publish").forEach(btn => btn.addEventListener("click", () => trasformatoCrud.handleSocialClick(btn)));

    // Prenotazioni visite
    document.querySelectorAll(".btn-prenota-visita").forEach(btn => {
        btn.addEventListener("click", () => prenotazioniUtils.openPrenotazioneVisitaModal(btn));
    });
    document.querySelectorAll(".btn-delete-prenotazione").forEach(btn => {
        btn.addEventListener("click", () => {
            const id = btn.getAttribute("data-id");
            if (id) prenotazioniUtils.openDeletePrenotazioneModal(id);
        });
    });
    prenotazioniUtils.attachPrenotazioneDeleteHandler();

    // Modali open/close generiche
    document.querySelectorAll(".btn-open-modal").forEach(btn => {
        btn.addEventListener("click", () => {
            const target = btn.getAttribute("data-target");
            if (target) modalUtils.openModal(target);
        });
    });
    document.querySelectorAll(".btn-close-modal").forEach(btn => {
        btn.addEventListener("click", () => {
            const target = btn.getAttribute("data-target");
            if (target) modalUtils.closeModal(target);
        });
    });

// Bottone "Chiudi" nei form (nasconde il contenitore)
    document.querySelectorAll(".btn-toggle-form").forEach(btn => {
        btn.addEventListener("click", () => {
            const formContainer = btn.closest(".form-container");
            if (formContainer) {
                formContainer.style.display = "none";
            }
        });
    });

    // Form Trasformato
    document.getElementById("btnCreateTrasformato")?.addEventListener("click", () => trasformatoCrud.toggleForm(true));
    document.getElementById("btnConfirmDelete")?.addEventListener("click", () => trasformatoCrud.confirmDelete());
    document.getElementById("btnOkSocialPost")?.addEventListener("click", () => trasformatoCrud.openSocialConfirm());
    document.getElementById("btnConfirmSocialPost")?.addEventListener("click", () => trasformatoCrud.submitSocialPost());
    document.getElementById("btnConfirmCreate")?.addEventListener("click", () => { reindexFasi(); trasformatoCrud.confirmCreate(); });
    document.getElementById("btnConfirmUpdate")?.addEventListener("click", () => { reindexFasi(); trasformatoCrud.confirmUpdate(); });

    // FASI: validazione modale + aggiunta
    document.querySelector(".btn-add-fase")?.addEventListener("click", () => {
        // pulizia errori
        document.getElementById("faseDescrizioneErr").textContent = "";
        document.getElementById("faseProduttoreErr").textContent = "";
        document.getElementById("faseProdottoBaseErr").textContent = "";

        const desc = document.getElementById("faseDescrizione").value.trim();
        const prod = document.getElementById("faseProduttore").value;
        const prodottoId = document.getElementById("faseProdottoBase").value;

        let ok = true;
        if (!desc) { document.getElementById("faseDescrizioneErr").textContent = "⚠ Descrizione obbligatoria"; ok = false; }
        if (!prod) { document.getElementById("faseProduttoreErr").textContent = "⚠ Seleziona un produttore"; ok = false; }
        if (!prodottoId) { document.getElementById("faseProdottoBaseErr").textContent = "⚠ Seleziona un prodotto base"; ok = false; }

        if (!ok) return;

        appendFaseLi(desc, prod, prodottoId);
        reindexFasi();

        // reset campi modale e chiudi
        document.getElementById("faseDescrizione").value = "";
        document.getElementById("faseProduttore").value = "";
        document.getElementById("faseProdottoBase").innerHTML = `<option value="">-- Seleziona prima un produttore --</option>`;
        modalUtils.closeModal("faseModal");
    });

    // Rimozione fase
    document.getElementById("fasiList")?.addEventListener("click", (e) => {
        if (e.target.classList.contains("btn-remove-fase")) {
            e.target.closest("li")?.remove();
            reindexFasi();
        }
    });

    // Caricamento dinamico prodotti base al cambio produttore
    document.getElementById("faseProduttore")?.addEventListener("change", async (e) => {
        const sel = document.getElementById("faseProdottoBase");
        sel.innerHTML = `<option value="">Caricamento...</option>`;
        const username = e.target.value;
        if (!username) {
            sel.innerHTML = `<option value="">-- Seleziona prima un produttore --</option>`;
            return;
        }
        try {
            const res = await fetch(`/trasformatore/prodotti/${encodeURIComponent(username)}`, { credentials: "same-origin" });
            if (!res.ok) throw new Error("Errore risposta server");
            const items = await res.json();
            sel.innerHTML = items.length
                ? `<option value="">-- Seleziona --</option>` + items.map(p => `<option value="${p.id}">${p.nome}</option>`).join("")
                : `<option value="">-- Nessun prodotto approvato --</option>`;
        } catch {
            sel.innerHTML = `<option value="">(Errore nel caricamento)</option>`;
        }
    });

    // Selezione social (multi-riga)
    socialSelectionUtils.init({
        rowSelector: ".selectable-row",
        btnPubblicaId: "btnPubblicaAvviso",
        crudMap: { TRASFORMATO: trasformatoCrud }
    });
});
