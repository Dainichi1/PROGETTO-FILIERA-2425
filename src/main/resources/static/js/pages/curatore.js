import { modalUtils } from "../utils/modal-utils.js";

document.addEventListener("DOMContentLoaded", () => {
    // === ELIMINA PROFILO ===
    const btnDeleteProfile = document.getElementById("btnDeleteProfile");
    if (btnDeleteProfile) {
        btnDeleteProfile.addEventListener("click", () => {
            modalUtils.openModal("deleteProfileModal");
        });
    }

    // Bottone "Sì" → invia la richiesta
    const confirmDeleteBtn = document.getElementById("confirmDeleteBtn");
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener("click", () => {
            console.log("Richiesta eliminazione profilo per Curatore");

            const token = document.querySelector("meta[name='_csrf']")?.content;
            const header = document.querySelector("meta[name='_csrf_header']")?.content;

            fetch("/curatore/richiesta-eliminazione", {
                method: "POST",
                credentials: "same-origin",
                headers: {
                    "Content-Type": "application/json",
                    ...(token && header ? { [header]: token } : {})
                }
            })
                .then(r => {
                    if (r.ok) return r.text();
                    if (r.status === 409) {
                        throw new Error("⚠ Esiste già una richiesta di eliminazione in attesa.");
                    }
                    if (r.status === 401 || r.status === 403) {
                        throw new Error("⚠ Non sei autorizzato ad eliminare il profilo.");
                    }
                    throw new Error("Errore HTTP " + r.status);
                })
                .then(msg => {
                    console.log("Risposta eliminazione:", msg);
                    modalUtils.closeModal("deleteProfileModal");
                    modalUtils.openModal("deleteProfileSuccessModal");
                })
                .catch(err => {
                    console.error("Errore eliminazione profilo:", err.message);
                    modalUtils.closeModal("deleteProfileModal");

                    const errorModal = document.getElementById("deleteProfileErrorModal");
                    if (errorModal) {
                        const msgEl = errorModal.querySelector(".error-message");
                        if (msgEl) msgEl.textContent = err.message;
                        modalUtils.openModal("deleteProfileErrorModal");
                    } else {
                        alert(err.message);
                    }
                });
        });
    }

    // Bottone "OK" → chiude la modale di successo e va in home
    const okDeleteBtn = document.getElementById("okDeleteBtn");
    if (okDeleteBtn) {
        okDeleteBtn.addEventListener("click", () => {
            modalUtils.closeModal("deleteProfileSuccessModal");
            window.location.href = "/";
        });
    }

    // === NOTIFICA PROFILO ELIMINATO ===
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

    // === POLLING STATO RICHIESTA ===
    function pollEliminazione() {
        fetch("/curatore/richiesta-eliminazione/stato", {
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

    // Bottone chiusura modali generiche
    document.querySelectorAll(".btn-close-modal").forEach(btn => {
        btn.addEventListener("click", e => {
            e.stopPropagation();
            const target = btn.dataset.target;
            if (target) modalUtils.closeModal(target);
        });
    });
});
