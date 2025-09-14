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
    console.log("[DEBUG] Toggle pubblicati -> visibile:", isHidden);
};

// ================== PUBBLICA AVVISO SU SOCIAL ==================
document.addEventListener("DOMContentLoaded", () => {
    const btn = document.getElementById("togglePubblicatiBtn");
    if (btn) btn.addEventListener("click", window.togglePubblicati);

    const btnPubblica = document.getElementById("btnPubblicaAvviso");
    let selectedRow = null;

    // selezione riga tabella
    document.querySelectorAll(".selectable-row").forEach(row => {
        row.addEventListener("click", () => {
            document.querySelectorAll(".selectable-row").forEach(r => r.classList.remove("selected"));
            row.classList.add("selected");
            selectedRow = row;
            btnPubblica.disabled = false;

            console.log("[DEBUG] RIGA SELEZIONATA ->", row.outerHTML);
            console.log("[DEBUG] data-id:", row.getAttribute("data-id"), "| data-type:", row.getAttribute("data-type"));
        });
    });

    // apertura modale social
    if (btnPubblica) {
        btnPubblica.addEventListener("click", () => {
            if (!selectedRow) {
                console.error("[DEBUG] Nessuna riga selezionata!");
                return;
            }
            const id = selectedRow.getAttribute("data-id");
            const type = selectedRow.getAttribute("data-type");

            console.log("[DEBUG] CLICK PUBBLICA AVVISO -> id:", id, "type:", type);

            if (type === "VISITA") {
                window.currentCrud = visitaCrud;
            } else if (type === "FIERA") {
                window.currentCrud = fieraCrud;
            }

            if (window.currentCrud) {
                console.log("[DEBUG] Invoco openSocialModal con:", { id, type, crud: window.currentCrud });
                window.currentCrud.openSocialModal(id, type);
            } else {
                console.error("[DEBUG] Nessun crud trovato!");
            }
        });
    }

    // --- BOTTONE "OK" NEL FORM SOCIAL ---
    const btnOk = document.getElementById("btnOkSocialPost");
    if (btnOk) {
        btnOk.onclick = () => {
            const crud = window.currentCrud || crudUtils;
            if (crud && typeof crud.openSocialConfirm === "function") {
                console.log("[DEBUG] OK SocialPost → openSocialConfirm()");
                crud.openSocialConfirm();
            } else {
                console.error("[ERRORE] Nessuna funzione openSocialConfirm trovata!");
            }
        };
    }

    // --- BOTTONE "SÌ" NEL MODALE DI CONFERMA ---
    const btnConfirm = document.getElementById("btnConfirmSocialPost");
    if (btnConfirm) {
        btnConfirm.onclick = () => {
            const crud = window.currentCrud || crudUtils;
            if (crud && typeof crud.submitSocialPost === "function") {
                console.log("[DEBUG] Conferma social → submitSocialPost()");
                crud.submitSocialPost();
            } else {
                console.error("[ERRORE] Nessuna funzione submitSocialPost trovata!");
            }
        };
    }
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
    prefillFormFn: (v) => { /* ... come prima ... */ },
    onCreateSuccess: () => modalUtils.openModal("createSuccessModalVisita"),
    onUpdateSuccess: () => modalUtils.openModal("updateSuccessModalVisita"),
    socialPostModalId: "socialPostModal",
    socialConfirmModalId: "socialConfirmModal",
    socialSuccessModalId: "socialSuccessModal",
    postTitleId: "postTitle",
    postTextId: "postText"
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
    prefillFormFn: (f) => { /* ... come prima ... */ },
    onCreateSuccess: () => modalUtils.openModal("createSuccessModalFiera"),
    onUpdateSuccess: () => modalUtils.openModal("updateSuccessModalFiera"),
    socialPostModalId: "socialPostModal",
    socialConfirmModalId: "socialConfirmModal",
    socialSuccessModalId: "socialSuccessModal",
    postTitleId: "postTitle",
    postTextId: "postText"
});

// ================= PATCH PER LOG =================
if (visitaCrud.openSocialModal) {
    const oldOpenSocial = visitaCrud.openSocialModal;
    visitaCrud.openSocialModal = function(id, type) {
        console.log("[DEBUG] visitaCrud.openSocialModal chiamata con:", { id, type });
        return oldOpenSocial.call(this, id, type);
    };
}
if (fieraCrud.openSocialModal) {
    const oldOpenSocial = fieraCrud.openSocialModal;
    fieraCrud.openSocialModal = function(id, type) {
        console.log("[DEBUG] fieraCrud.openSocialModal chiamata con:", { id, type });
        return oldOpenSocial.call(this, id, type);
    };
}

// hook submitSocialPost per loggare i dati inviati
if (crudUtils.submitSocialPost) {
    const oldSubmitSocialPost = crudUtils.submitSocialPost;
    crudUtils.submitSocialPost = async function() {
        console.log("[DEBUG] submitSocialPost chiamata. currentItemId:", this.currentItemId, "tipo:", this.itemType);
        return await oldSubmitSocialPost.apply(this, arguments);
    };
}

