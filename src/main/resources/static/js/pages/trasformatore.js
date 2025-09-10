function clientValidateTrasformato() {
    formUtils.clearAllErrors("itemForm");

    // id reali con suffisso -trasformatoDto
    const nome = document.getElementById("nome-trasformatoDto");
    const descrizione = document.getElementById("descrizione-trasformatoDto");
    const quantita = document.getElementById("quantita-trasformatoDto");
    const prezzo = document.getElementById("prezzo-trasformatoDto");
    const indirizzo = document.getElementById("indirizzo-trasformatoDto");
    const certificati = document.getElementById("certificati-trasformatoDto");
    const foto = document.getElementById("foto-trasformatoDto");

    let ok = true;

    // campi base
    if (!nome.value.trim()) {
        formUtils.setFieldError("nome-trasformatoDto", "⚠ Nome obbligatorio");
        ok = false;
    }
    if (!descrizione.value.trim()) {
        formUtils.setFieldError("descrizione-trasformatoDto", "⚠ Descrizione obbligatoria");
        ok = false;
    }
    if (!indirizzo.value.trim()) {
        formUtils.setFieldError("indirizzo-trasformatoDto", "⚠ Indirizzo obbligatorio");
        ok = false;
    }

    const q = quantita.value !== '' ? Number(quantita.value) : NaN;
    if (Number.isNaN(q) || q < 1) {
        formUtils.setFieldError("quantita-trasformatoDto", "⚠ La quantità deve essere almeno 1");
        ok = false;
    }

    const p = prezzo.value !== '' ? Number(prezzo.value) : NaN;
    if (Number.isNaN(p) || p <= 0) {
        formUtils.setFieldError("prezzo-trasformatoDto", "⚠ Il prezzo deve essere positivo");
        ok = false;
    }

    // file obbligatori solo in creazione
    if (!document.getElementById("itemId").value) {
        if (!certificati.files || certificati.files.length === 0) {
            formUtils.setFieldError("certificati-trasformatoDto", "⚠ Devi caricare almeno un certificato");
            ok = false;
        }
        if (!foto.files || foto.files.length === 0) {
            formUtils.setFieldError("foto-trasformatoDto", "⚠ Devi caricare almeno una foto");
            ok = false;
        }
    }

    // validazione fasi (>= 2)
    const numFasi = document.querySelectorAll("#fasiList li").length;
    const fasiErrorSpan = document.querySelector("#fasiList").nextElementSibling;
    if (numFasi < 2) {
        if (fasiErrorSpan) fasiErrorSpan.textContent = "⚠ Devi inserire almeno 2 fasi di produzione";
        ok = false;
    } else {
        if (fasiErrorSpan) fasiErrorSpan.textContent = "";
    }

    return ok;
}

/**
 * Aggiorna gli indici delle fasi nel form.
 * Serve per il binding della lista fasiProduzione lato server.
 */
function reindexFasi() {
    const fasi = document.querySelectorAll("#fasiList li");
    fasi.forEach((li, index) => {
        li.querySelectorAll("input, select, textarea").forEach(el => {
            if (el.name) {
                el.name = el.name.replace(/\[.*?\]/, `[${index}]`);
            }
        });
    });
}

/**
 * Popola la select "faseProdottoBase" con i prodotti approvati del produttore scelto
 */
/**
 * Popola la select "faseProdottoBase" con i prodotti approvati del produttore scelto
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
            if (!prodotti || prodotti.length === 0) {
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
 * Aggiunge una nuova fase alla lista #fasiList
 */
function aggiungiFase() {
    const descrizione = document.getElementById("faseDescrizione").value.trim();
    const produttore = document.getElementById("faseProduttore").value;
    const prodottoBase = document.getElementById("faseProdottoBase").value;

    if (!descrizione || !produttore || !prodottoBase) {
        alert("⚠ Devi compilare tutti i campi della fase");
        return;
    }

    const list = document.getElementById("fasiList");

    const index = list.querySelectorAll("li").length;

    const li = document.createElement("li");
    li.innerHTML = `
        <strong>${descrizione}</strong> 
        (Produttore: ${produttore}, Prodotto: ${document.querySelector("#faseProdottoBase option:checked").textContent})
        <button type="button" onclick="this.parentElement.remove(); reindexFasi();">❌ Rimuovi</button>
        
        <input type="hidden" name="fasiProduzione[${index}].descrizioneFase" value="${descrizione}">
        <input type="hidden" name="fasiProduzione[${index}].produttoreUsername" value="${produttore}">
        <input type="hidden" name="fasiProduzione[${index}].prodottoOrigineId" value="${prodottoBase}">
    `;

    list.appendChild(li);

    // reset campi modale
    document.getElementById("faseDescrizione").value = "";
    document.getElementById("faseProduttore").value = "";
    document.getElementById("faseProdottoBase").innerHTML = "<option value=''>-- Seleziona prima un produttore --</option>";

    modalUtils.closeModal("faseModal");
}

// esporta globalmente
window.caricaProdottiPerProduttore = caricaProdottiPerProduttore;
window.aggiungiFase = aggiungiFase;


crudUtils.init({
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
    updateAction: "/venditore/item/modifica", // centralizzato

    deleteUrl: (id) => `/venditore/item/elimina/${id}?tipo=TRASFORMATO`,
    fetchUrl: (id) => `/venditore/item/fetch/${id}?tipo=TRASFORMATO`,
    deletableStates: ["IN_ATTESA", "RIFIUTATO"],
    editableState: "RIFIUTATO",

    deleteErrorModalId: "deleteErrorModal",
    deleteConfirmModalId: "deleteConfirmModal",
    deleteSuccessModalId: "deleteSuccessModal",
    deleteGenericErrorModalId: "deleteGenericErrorModal",
    editErrorModalId: "editErrorModal",

    deleteConfirmMessageId: "deleteConfirmMessage",
    deleteSuccessMessageId: "deleteSuccessMessage",
    deleteGenericErrorMessageId: "deleteGenericErrorMessage",

    labels: { itemName: "Prodotto trasformato" },

    validateFn: () => {
        reindexFasi();
        return clientValidateTrasformato();
    },

    prefillFormFn: (t) => {
        document.getElementById("nome-trasformatoDto").value = t.nome ?? '';
        document.getElementById("descrizione-trasformatoDto").value = t.descrizione ?? '';
        document.getElementById("quantita-trasformatoDto").value = t.quantita ?? '';
        document.getElementById("prezzo-trasformatoDto").value = t.prezzo ?? '';
        document.getElementById("indirizzo-trasformatoDto").value = t.indirizzo ?? '';

        const list = document.getElementById("fasiList");
        list.innerHTML = "";
        (t.fasiProduzione || []).forEach((fase, index) =>
            appendFaseLi(
                index,
                fase.descrizioneFase,
                fase.produttoreUsername,
                fase.prodottoOrigineId,
                fase.produttoreUsername,
                fase.prodottoOrigineId
            )
        );
    }
});
