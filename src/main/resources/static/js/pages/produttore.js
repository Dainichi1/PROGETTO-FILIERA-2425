function clientValidateProdotto() {
    formUtils.clearAllErrors("itemForm");

    // Usa gli id reali generati dal fragment
    const nome = document.getElementById("nome-prodottoDto");
    const descrizione = document.getElementById("descrizione-prodottoDto");
    const quantita = document.getElementById("quantita-prodottoDto");
    const prezzo = document.getElementById("prezzo-prodottoDto");
    const indirizzo = document.getElementById("indirizzo-prodottoDto");
    const certificati = document.getElementById("certificati-prodottoDto");
    const foto = document.getElementById("foto-prodottoDto");

    let ok = true;

    if (!nome.value.trim()) {
        formUtils.setFieldError("nome-prodottoDto", "⚠ Nome obbligatorio");
        ok = false;
    }
    if (!descrizione.value.trim()) {
        formUtils.setFieldError("descrizione-prodottoDto", "⚠ Descrizione obbligatoria");
        ok = false;
    }
    if (!indirizzo.value.trim()) {
        formUtils.setFieldError("indirizzo-prodottoDto", "⚠ Indirizzo obbligatorio");
        ok = false;
    }

    const q = quantita.value !== '' ? Number(quantita.value) : NaN;
    if (Number.isNaN(q) || q < 1) {
        formUtils.setFieldError("quantita-prodottoDto", "⚠ La quantità deve essere almeno 1");
        ok = false;
    }

    const p = prezzo.value !== '' ? Number(prezzo.value) : NaN;
    if (Number.isNaN(p) || p <= 0) {
        formUtils.setFieldError("prezzo-prodottoDto", "⚠ Il prezzo deve essere positivo");
        ok = false;
    }

    if (!certificati.files || certificati.files.length === 0) {
        formUtils.setFieldError("certificati-prodottoDto", "Devi caricare almeno un certificato");
        ok = false;
    }
    if (!foto.files || foto.files.length === 0) {
        formUtils.setFieldError("foto-prodottoDto", "Devi caricare almeno una foto");
        ok = false;
    }

    return ok;
}

crudUtils.init({
    formId: "itemForm",
    formContainerId: "prodottoForm",
    formTitleId: "formTitle",
    itemIdField: "itemId",
    itemTypeField: "itemTipo",
    createButtonsId: "createButtons",
    updateButtonsId: "updateButtons",
    itemType: "PRODOTTO",
    createTitle: "Nuovo Prodotto",
    updateTitle: "Modifica Prodotto Rifiutato",
    createAction: "/produttore/crea",
    updateAction: "/venditore/item/modifica",   // centralizzato

    deleteUrl: (id) => `/venditore/item/elimina/${id}?tipo=PRODOTTO`,
    fetchUrl: (id) => `/venditore/item/fetch/${id}?tipo=PRODOTTO`,
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

    labels: { itemName: "Prodotto" },

    validateFn: clientValidateProdotto,

    prefillFormFn: (p) => {
        document.getElementById("nome-prodottoDto").value = p.nome ?? '';
        document.getElementById("descrizione-prodottoDto").value = p.descrizione ?? '';
        document.getElementById("quantita-prodottoDto").value = p.quantita ?? '';
        document.getElementById("prezzo-prodottoDto").value = p.prezzo ?? '';
        document.getElementById("indirizzo-prodottoDto").value = p.indirizzo ?? '';
    }
});
