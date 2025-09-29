// ================== IMPORT ==================
import {validationUtilsCrud} from "../utils/crud-validation-utils.js";
import {modalUtils} from "../utils/modal-utils.js";
import {crudUtilsAnimatore} from "../utils/crud-utils-animatore.js";
import {socialSelectionUtils} from "../utils/social-selection-utils.js";
import {crudUtils} from "../utils/crud-utils.js";
import {csrfUtils} from "../utils/csrf-utils.js";
import {commonMapUtils} from "../utils/common-map-utils.js";

// ================== CRUD VISITA ==================
const visitaCrud = crudUtilsAnimatore({
    formId: "visitaForm",
    formContainerId: "visitaFormContainer",
    formTitleId: "formTitleVisita",
    itemIdField: "visitaId",
    itemTypeField: "eventoTipoVisita",
    createButtonsId: "createButtonsVisita",
    updateButtonsId: "updateButtonsVisita",
    itemType: "VISITA",
    createTitle: "Nuova Visita ad Invito",
    updateTitle: "Modifica Visita",
    createAction: "/animatore/crea-visita",
    updateAction: "/animatore/modifica-visita",
    labels: {itemName: "Visita ad invito"},
    validateFn: validationUtilsCrud.validateVisita,
    confirmModalId: "createConfirmModalVisita",
    createSuccessModalId: "createSuccessModalVisita",
    updateSuccessModalId: "updateSuccessModalVisita",
    prefillFormFn: v => {
        document.getElementById("nome-visitaDto").value = v.nome ?? "";
        document.getElementById("descrizione-visitaDto").value = v.descrizione ?? "";
        document.getElementById("indirizzo-visitaDto").value = v.indirizzo ?? "";
        document.getElementById("dataInizio-visitaDto").value = v.dataInizio?.split("T")[0] ?? "";
        document.getElementById("dataFine-visitaDto").value = v.dataFine?.split("T")[0] ?? "";
    }
});

// ================== CRUD FIERA ==================
const fieraCrud = crudUtilsAnimatore({
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
    labels: {itemName: "Fiera"},
    validateFn: validationUtilsCrud.validateFiera,
    confirmModalId: "createConfirmModalFiera",
    createSuccessModalId: "createSuccessModalFiera",
    updateSuccessModalId: "updateSuccessModalFiera",
    prefillFormFn: f => {
        document.getElementById("nome-fieraDto").value = f.nome ?? "";
        document.getElementById("descrizione-fieraDto").value = f.descrizione ?? "";
        document.getElementById("indirizzo-fieraDto").value = f.indirizzo ?? "";
        document.getElementById("prezzo-fieraDto").value = f.prezzo ?? "";
        document.getElementById("dataInizio-fieraDto").value = f.dataInizio?.split("T")[0] ?? "";
        document.getElementById("dataFine-fieraDto").value = f.dataFine?.split("T")[0] ?? "";
    }
});

// ================== OGGETTO ANIMATORE ==================
const animatoreCrud = {
    visita: visitaCrud,
    fiera: fieraCrud
};

// ================== INIZIALIZZAZIONE ==================
document.addEventListener("DOMContentLoaded", () => {
    // Social Feed
    document.getElementById("btnSocialFeed")
        ?.addEventListener("click", () => crudUtils.openSocialFeed());

    // Toggle pubblicati
    document.getElementById("togglePubblicatiBtn")
        ?.addEventListener("click", () => visitaCrud.togglePubblicati());

    // Selezione riga + pubblicazione social
    socialSelectionUtils.init({
        rowSelector: ".selectable-row",
        btnPubblicaId: "btnPubblicaAvviso",
        crudMap: {
            VISITA: visitaCrud,
            FIERA: fieraCrud
        }
    });

    // === VISITA ===
    document.getElementById("btnPubblicaVisita")
        ?.addEventListener("click", () => modalUtils.openModal("createConfirmModalVisita"));
    document.getElementById("btnConfirmCreateVisita")
        ?.addEventListener("click", () => visitaCrud.submitForm());
    document.getElementById("btnConfirmUpdateVisita")
        ?.addEventListener("click", () => visitaCrud.submitForm());
    document.getElementById("btnCancelVisita")
        ?.addEventListener("click", () => visitaCrud.toggleForm(false));

    // === FIERA ===
    document.getElementById("btnPubblicaFiera")
        ?.addEventListener("click", () => modalUtils.openModal("createConfirmModalFiera"));
    document.getElementById("btnConfirmCreateFiera")
        ?.addEventListener("click", () => fieraCrud.submitForm());
    document.getElementById("btnConfirmUpdateFiera")
        ?.addEventListener("click", () => fieraCrud.submitForm());
    document.getElementById("btnCancelFiera")
        ?.addEventListener("click", () => fieraCrud.toggleForm(false));

    // Apertura form
    document.getElementById("btnOpenVisita")
        ?.addEventListener("click", () => visitaCrud.toggleForm(true));
    document.getElementById("btnOpenFiera")
        ?.addEventListener("click", () => fieraCrud.toggleForm(true));

    // Bottone mappa
    document.getElementById("btnApriMappa")
        ?.addEventListener("click", () => {
            console.log("🌍 Apertura mappa Produttore");
            commonMapUtils.mostraMappa();
        });
    // ================== ELIMINA PROFILO ==================
    const btnDeleteProfile = document.getElementById("btnDeleteProfile");
    if (btnDeleteProfile) {
        btnDeleteProfile.addEventListener("click", () => {
            modalUtils.openModal("deleteProfileModal");
        });
    }

    const confirmDeleteBtn = document.getElementById("confirmDeleteBtn");
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener("click", () => {
            const csrf = csrfUtils.getCsrf();

            fetch("/animatore/richiesta-eliminazione", {
                method: "POST",
                headers: { [csrf.header]: csrf.token },
                credentials: "same-origin"
            })
                .then(r => {
                    if (r.status === 200) {
                        modalUtils.closeModal("deleteProfileModal");
                        modalUtils.openModal("deleteProfileSuccessModal");
                    } else if (r.status === 409) {
                        modalUtils.closeModal("deleteProfileModal");
                        modalUtils.openModal("deleteProfileErrorModal");
                    } else {
                        throw new Error("Errore generico");
                    }
                })
                .catch(err => {
                    alert("❌ Errore durante l’eliminazione del profilo: " + err.message);
                });
        });
    }

    const okDeleteBtn = document.getElementById("okDeleteBtn");
    if (okDeleteBtn) {
        okDeleteBtn.addEventListener("click", () => {
            modalUtils.closeModal("deleteProfileSuccessModal");
        });
    }

    // ================== NOTIFICA ELIMINAZIONE PROFILO ==================
    const deletedProfileMessage = document.getElementById("deletedProfileMessage");
    const okProfileDeletedBtn = document.getElementById("okProfileDeletedBtn");

    function showProfileDeletedNotification(requestId) {
        let seconds = 30;

        function updateMessage() {
            if (deletedProfileMessage) {
                deletedProfileMessage.innerText =
                    `⚠️ Il tuo profilo è stato eliminato (richiesta ID ${requestId}). ` +
                    `Verrai disconnesso tra ${seconds} secondi...`;
            }
        }

        updateMessage();
        modalUtils.openModal("profileDeletedNotificationModal");

        const interval = setInterval(() => {
            seconds--;
            updateMessage();

            if (seconds <= 0) {
                clearInterval(interval);
                window.location.href = "/logout";
            }
        }, 1000);

        if (okProfileDeletedBtn) {
            okProfileDeletedBtn.onclick = () => {
                clearInterval(interval);
                modalUtils.closeModal("profileDeletedNotificationModal");
                window.location.href = "/logout";
            };
        }
    }

    // ================== POLLING STATO RICHIESTA ==================
    function pollEliminazione() {
        fetch("/animatore/richiesta-eliminazione/stato", {
            credentials: "same-origin"
        })
            .then(r => {
                if (!r.ok) throw new Error("Errore HTTP " + r.status);
                return r.text();
            })
            .then(resp => {
                if (resp.startsWith("APPROVATA:")) {
                    const id = resp.split(":")[1];
                    showProfileDeletedNotification(id);
                    clearInterval(pollingInterval);
                }
            })
            .catch(err => console.error("Errore polling eliminazione:", err));
    }

    const pollingInterval = setInterval(pollEliminazione, 5000);
});

// ================== ESPORTO GLOBALMENTE ==================
window.visitaCrud = visitaCrud;
window.fieraCrud = fieraCrud;
window.animatoreCrud = animatoreCrud;
