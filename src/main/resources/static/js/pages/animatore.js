// ================= VALIDAZIONE CLIENT =================

// --- VISITA ---
function clientValidateVisita() {
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

// --- FIERA ---
function clientValidateFiera() {
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

    // Prezzo
    const prezzoValue = prezzo?.value ? parseFloat(prezzo.value) : null;
    if (prezzoValue === null || isNaN(prezzoValue)) {
        formUtils.setFieldError("prezzo-fieraDto", "⚠ Prezzo obbligatorio");
        ok = false;
    } else if (prezzoValue < 0) {
        formUtils.setFieldError("prezzo-fieraDto", "⚠ Il prezzo non può essere negativo");
        ok = false;
    }

    // Date
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

// ================== TOGGLE PUBBLICATI ==================
window.togglePubblicati = function () {
    const container = document.getElementById("pubblicatiContainer");
    const btn = document.getElementById("togglePubblicatiBtn");
    if (!container || !btn) return;

    const isHidden = container.style.display === "none";
    container.style.display = isHidden ? "block" : "none";
    btn.textContent = isHidden ? "❌ Chiudi" : "📋 Visualizza visite/fiere pubblicate";
};

// bind evento al bottone
document.addEventListener("DOMContentLoaded", () => {
    const btn = document.getElementById("togglePubblicatiBtn");
    if (btn) btn.addEventListener("click", window.togglePubblicati);
});


// ================= INIZIALIZZAZIONI CRUD =================

// --- CRUD VISITA ---
const visitaCrud = crudUtils.createInstance({
    formId: "visitaForm",
    formContainerId: "visitaFormContainer",
    formTitleId: "formTitle",
    itemIdField: "visitaId",
    itemTypeField: "eventoTipo",
    createButtonsId: "createButtons",
    updateButtonsId: "updateButtons",
    itemType: "VISITA",
    createTitle: "Nuova Visita ad Invito",
    updateTitle: "Modifica Visita",
    createAction: "/animatore/crea-visita",
    updateAction: "/animatore/modifica-visita",
    labels: { itemName: "Visita ad invito" },
    validateFn: clientValidateVisita,
    confirmModalId: "createConfirmModalVisita",
    prefillFormFn: (v) => {
        document.getElementById("nome-visitaDto").value = v.nome ?? '';
        document.getElementById("descrizione-visitaDto").value = v.descrizione ?? '';
        document.getElementById("indirizzo-visitaDto").value = v.indirizzo ?? '';
        document.getElementById("dataInizio-visitaDto").value = v.dataInizio ?? '';
        document.getElementById("dataFine-visitaDto").value = v.dataFine ?? '';

        // Reset checkbox destinatari
        document.querySelectorAll("input[name='destinatari']").forEach(cb => cb.checked = false);

        // Ripristina destinatari salvati (lista di username)
        if (v.destinatari && Array.isArray(v.destinatari)) {
            v.destinatari.forEach(username => {
                const cb = document.querySelector(
                    `input[name='destinatari'][value='${username}']`
                );
                if (cb) cb.checked = true;
            });
        }
    },
    onCreateSuccess: () => modalUtils.openModal("createSuccessModalVisita"),
    onUpdateSuccess: () => modalUtils.openModal("updateSuccessModalVisita")
});

// --- CRUD FIERA ---
const fieraCrud = crudUtils.createInstance({
    formId: "fieraForm",
    formContainerId: "fieraFormContainer",
    formTitleId: "formTitleFiera",
    itemIdField: "fieraId",
    itemTypeField: "eventoTipoFiera",
    createButtonsId: "createButtonsFiera",
    updateButtonsId: "updateButtonsFiera",
    itemType: "FIERA",
    createTitle: "Nuova Fiera",
    updateTitle: "Modifica Fiera",
    createAction: "/animatore/crea-fiera",
    updateAction: "/animatore/modifica-fiera",
    labels: { itemName: "Fiera" },
    validateFn: clientValidateFiera,
    confirmModalId: "createConfirmModalFiera",
    prefillFormFn: (f) => {
        document.getElementById("nome-fieraDto").value = f.nome ?? '';
        document.getElementById("descrizione-fieraDto").value = f.descrizione ?? '';
        document.getElementById("indirizzo-fieraDto").value = f.indirizzo ?? '';
        document.getElementById("prezzo-fieraDto").value = f.prezzo ?? '';
        document.getElementById("dataInizio-fieraDto").value = f.dataInizio ?? '';
        document.getElementById("dataFine-fieraDto").value = f.dataFine ?? '';
    },
    onCreateSuccess: () => modalUtils.openModal("createSuccessModalFiera"),
    onUpdateSuccess: () => modalUtils.openModal("updateSuccessModalFiera")
});
