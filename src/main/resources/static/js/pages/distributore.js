function clientValidatePacchetto() {
    formUtils.clearAllErrors("packageForm");

    // id reali con suffisso -pacchettoDto
    const nome = document.getElementById("nome-pacchettoDto");
    const descrizione = document.getElementById("descrizione-pacchettoDto");
    const indirizzo = document.getElementById("indirizzo-pacchettoDto");
    const quantita = document.getElementById("quantita-pacchettoDto");
    const prezzo = document.getElementById("prezzo-pacchettoDto");
    const certificati = document.getElementById("certificati-pacchettoDto");
    const foto = document.getElementById("foto-pacchettoDto");

    let ok = true;

    if (!nome.value.trim()) {
        formUtils.setFieldError("nome-pacchettoDto", "⚠ Nome obbligatorio");
        ok = false;
    }
    if (!descrizione.value.trim()) {
        formUtils.setFieldError("descrizione-pacchettoDto", "⚠ Descrizione obbligatoria");
        ok = false;
    }
    if (!indirizzo.value.trim()) {
        formUtils.setFieldError("indirizzo-pacchettoDto", "⚠ Indirizzo obbligatorio");
        ok = false;
    }

    const q = quantita.value !== '' ? Number(quantita.value) : NaN;
    if (Number.isNaN(q) || q < 1) {
        formUtils.setFieldError("quantita-pacchettoDto", "⚠ La quantità deve essere almeno 1");
        ok = false;
    }

    const p = prezzo.value !== '' ? Number(prezzo.value) : NaN;
    if (Number.isNaN(p) || p <= 0) {
        formUtils.setFieldError("prezzo-pacchettoDto", "⚠ Il prezzo deve essere positivo");
        ok = false;
    }

    if (!certificati.files || certificati.files.length === 0) {
        formUtils.setFieldError("certificati-pacchettoDto", "Devi caricare almeno un certificato");
        ok = false;
    }
    if (!foto.files || foto.files.length === 0) {
        formUtils.setFieldError("foto-pacchettoDto", "Devi caricare almeno una foto");
        ok = false;
    }

    const prodotti = document.querySelectorAll("input[name='prodottiSelezionati']:checked");
    if (prodotti.length < 2) {
        const span = document.querySelector(".checkbox-list + .error-message");
        if (span) span.textContent = "⚠ Devi selezionare almeno 2 prodotti approvati";
        ok = false;
    }

    return ok;
}

crudUtils.init({
    formId: "packageForm",
    formContainerId: "pacchettoForm",
    formTitleId: "formTitle",
    itemIdField: "itemId",
    itemTypeField: "itemTipo",
    createButtonsId: "createButtons",
    updateButtonsId: "updateButtons",
    itemType: "PACCHETTO",
    createTitle: "Nuovo Pacchetto",
    updateTitle: "Modifica Pacchetto Rifiutato",
    createAction: "/distributore/crea",
    updateAction: "/venditore/item/modifica",   // centralizzato

    deleteUrl: (id) => `/venditore/item/elimina/${id}?tipo=PACCHETTO`,
    fetchUrl: (id) => `/venditore/item/fetch/${id}?tipo=PACCHETTO`,
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

    labels: { itemName: "Pacchetto" },

    validateFn: clientValidatePacchetto,

    prefillFormFn: (p) => {
        document.getElementById("nome-pacchettoDto").value = p.nome ?? '';
        document.getElementById("descrizione-pacchettoDto").value = p.descrizione ?? '';
        document.getElementById("quantita-pacchettoDto").value = p.quantita ?? '';
        document.getElementById("prezzo-pacchettoDto").value = p.prezzo ?? '';
        document.getElementById("indirizzo-pacchettoDto").value = p.indirizzo ?? '';

        if (p.prodottiSelezionati && Array.isArray(p.prodottiSelezionati)) {
            document.querySelectorAll("input[name='prodottiSelezionati']").forEach(cb => {
                cb.checked = p.prodottiSelezionati.includes(Number(cb.value));
            });
        }
    }
});

// Riapri form in UPDATE dopo errori server
const UPDATE_MODE = /*[[${updateMode}]]*/ false;
const UPDATE_ID = /*[[${pacchettoDto != null ? pacchettoDto.id : null}]]*/ null;
if (UPDATE_MODE) {
    document.addEventListener('DOMContentLoaded', () => {
        crudUtils.toggleForm();
        document.getElementById("itemId").value = UPDATE_ID;
    });
}
