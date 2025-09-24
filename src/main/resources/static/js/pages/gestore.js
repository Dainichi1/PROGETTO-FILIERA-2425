// ================== IMPORT ==================
import {toggleUtils} from "../utils/toggle-utils.js";
import {modalUtils} from "../utils/modal-utils.js";
import {csrfUtils} from "../utils/csrf-utils.js";

document.addEventListener("DOMContentLoaded", () => {
    const searchInput = document.getElementById("searchInput");
    const statoSelect = document.getElementById("statoSelect");
    const sortSelect = document.getElementById("sortSelect");
    const table = document.getElementById("contenutiTable");
    const tbody = table?.querySelector("tbody");

    // ================== FILTRO COMBINATO ==================
    function filterRows() {
        if (!tbody) return;
        const rows = tbody.querySelectorAll("tr");
        const searchTerm = searchInput?.value.toLowerCase() || "";
        const stato = statoSelect?.value.toLowerCase() || "";

        rows.forEach(row => {
            if (row.querySelector("td[colspan]")) return; // ignora messaggi

            const text = row.innerText.toLowerCase();
            const statoCell = row.cells[3]?.innerText.toLowerCase() || "";

            const matchesSearch = text.includes(searchTerm);
            const matchesStato = stato === "" || statoCell.includes(stato);

            row.style.display = (matchesSearch && matchesStato) ? "" : "none";
        });
    }

    searchInput?.addEventListener("input", filterRows);
    statoSelect?.addEventListener("change", filterRows);

    // ================== ORDINAMENTO ==================
    let sortDirection = 1; // 1 = asc, -1 = desc

    function sortRows() {
        if (!tbody) return;
        const rows = Array.from(tbody.querySelectorAll("tr"))
            .filter(r => !r.querySelector("td[colspan]")); // esclude riga "vuota"

        const criterion = sortSelect?.value;
        if (!criterion) return;

        let colIndex = 0;
        switch (criterion) {
            case "nome":
                colIndex = 1;
                break;
            case "tipo":
                colIndex = 2;
                break;
            case "stato":
                colIndex = 3;
                break;
            case "data":
                colIndex = 4;
                break;
        }

        rows.sort((a, b) => {
            const aText = a.cells[colIndex]?.innerText.trim().toLowerCase() || "";
            const bText = b.cells[colIndex]?.innerText.trim().toLowerCase() || "";

            if (criterion === "data") {
                const aTime = Date.parse(aText) || 0;
                const bTime = Date.parse(bText) || 0;
                return (aTime - bTime) * sortDirection;
            }

            return aText.localeCompare(bText) * sortDirection;
        });

        rows.forEach(row => tbody.appendChild(row));
        sortDirection *= -1;
    }

    sortSelect?.addEventListener("change", sortRows);

    // ================== EVIDENZIAZIONE SIDEBAR ==================
    document.querySelectorAll(".sidebar a").forEach(link => {
        link.addEventListener("click", () => {
            document.querySelectorAll(".sidebar a").forEach(a => a.classList.remove("active"));
            link.classList.add("active");
        });
    });

    // ================== MODALI: Nessun contenuto ==================
    const noContentRow = document.getElementById("noContentRow");
    const modalNoContent = document.getElementById("modalNoContent");
    const modalChoice = document.getElementById("modalChoice");

    if (noContentRow && modalNoContent && modalChoice) {
        toggleUtils.show(modalNoContent);

        modalNoContent.querySelector(".btn-ok")?.addEventListener("click", () => {
            toggleUtils.hide(modalNoContent);
            toggleUtils.show(modalChoice);
        });

        modalChoice.querySelector(".btn-riepilogo")?.addEventListener("click", () => {
            toggleUtils.hide(modalChoice);
        });

        modalChoice.querySelector(".btn-categoria")?.addEventListener("click", () => {
            toggleUtils.hide(modalChoice);
        });
    }

    // ================== VISUALIZZA RICHIESTE ELIMINAZIONE PROFILO ==================
    const btnViewDeletionRequests = document.getElementById("btnViewDeletionRequests");
    if (btnViewDeletionRequests) {
        btnViewDeletionRequests.addEventListener("click", () => {
            const csrf = csrfUtils.getCsrf();

            fetch("/gestore/richieste/api", {
                credentials: "same-origin",
                headers: {
                    [csrf.header]: csrf.token
                }
            })
                .then(r => {
                    if (!r.ok) throw new Error("Errore HTTP " + r.status);
                    return r.json();
                })
                .then(data => {
                    const tbody = document.querySelector("#deletionRequestsTable tbody");
                    if (tbody) {
                        tbody.innerHTML = "";
                        if (Array.isArray(data) && data.length > 0) {
                            data.forEach(req => {
                                const tr = document.createElement("tr");
                                tr.classList.add("selectable-row"); // <--- aggiungo la classe
                                tr.innerHTML = `
                                    <td>${req.id}</td>
                                    <td>${req.username}</td>
                                    <td>${req.dataRichiesta ? new Date(req.dataRichiesta).toLocaleString("it-IT") : ""}</td>
                                    <td>${req.stato}</td>
                                `;
                                tbody.appendChild(tr);
                            });

                        } else {
                            const tr = document.createElement("tr");
                            tr.innerHTML = `<td colspan="4">Nessuna richiesta di eliminazione in attesa</td>`;
                            tbody.appendChild(tr);
                        }
                    }
                    modalUtils.openModal("deletionRequestsModal");
                })
                .catch(err => {
                    console.error("Errore caricamento richieste eliminazione:", err);
                });
        });
    }

    // ================== SELEZIONE RIGA TABELLA RICHIESTE ==================
    const deletionTable = document.getElementById("deletionRequestsTable");
    if (deletionTable) {
        deletionTable.addEventListener("click", e => {
            const row = e.target.closest("tr");
            if (!row || row.parentNode.tagName === "THEAD") return;

            deletionTable.querySelectorAll("tr").forEach(r => r.classList.remove("selected"));
            row.classList.add("selected");
        });
    }

    // ================== APRI DETTAGLI RICHIESTA ==================
    const btnOpenDetails = document.getElementById("btnOpenDetails");
    if (btnOpenDetails) {
        btnOpenDetails.addEventListener("click", () => {
            const selected = deletionTable?.querySelector("tr.selected");
            if (!selected) {
                modalUtils.openModal("noSelectionModal");
                return;
            }

            // prendo i dati dalle celle
            const cells = selected.querySelectorAll("td");
            const id = cells[0]?.innerText || "";
            const username = cells[1]?.innerText || "";
            const dataRichiesta = cells[2]?.innerText || "";
            const stato = cells[3]?.innerText || "";

            // popolo la modale dei dettagli
            document.getElementById("detailId").innerText = id;
            document.getElementById("detailUsername").innerText = username;
            document.getElementById("detailDataRichiesta").innerText = dataRichiesta;
            document.getElementById("detailStato").innerText = stato;

            modalUtils.openModal("requestDetailsModal");
        });
    }

// ================== RIFIUTA RICHIESTA ==================
    const btnReject = document.getElementById("btnRejectRequest");
    if (btnReject) {
        btnReject.addEventListener("click", () => {
            modalUtils.openModal("confirmRejectModal");
        });
    }

    const btnRejectNo = document.getElementById("btnRejectNo");
    if (btnRejectNo) {
        btnRejectNo.addEventListener("click", () => {
            modalUtils.closeModal("confirmRejectModal");
            window.location.href = "/gestore/dashboard";
        });
    }

    const btnRejectYes = document.getElementById("btnRejectYes");
    if (btnRejectYes) {
        btnRejectYes.addEventListener("click", () => {
            modalUtils.closeModal("confirmRejectModal");
            window.location.href = "/gestore/dashboard";
            const id = document.getElementById("detailId").innerText;
            const csrf = csrfUtils.getCsrf();

            fetch(`/gestore/richieste/${id}/rifiuta`, {
                method: "POST",
                credentials: "same-origin",
                headers: {
                    "Content-Type": "application/json",
                    [csrf.header]: csrf.token
                }
            })
                .then(r => {
                    if (!r.ok) throw new Error("Errore HTTP " + r.status);
                    return r.text();
                })
                .then(() => {
                    modalUtils.openModal("rejectedModal");
                })
                .catch(err => {
                    console.error("Errore rifiuto richiesta:", err);
                });
        });
    }

    const btnRejectedOk = document.getElementById("btnRejectedOk");
    if (btnRejectedOk) {
        btnRejectedOk.addEventListener("click", () => {
            modalUtils.closeModal("rejectedModal");
            window.location.href = "/gestore";
        });
    }

    // ================== ACCETTA RICHIESTA ==================
    const btnAccept = document.getElementById("btnAcceptRequest");
    if (btnAccept) {
        btnAccept.addEventListener("click", () => {
            const username = document.getElementById("detailUsername").innerText;
            document.getElementById("acceptUsername").innerText = username;
            modalUtils.openModal("confirmAcceptModal");
        });
    }

    const btnAcceptNo = document.getElementById("btnAcceptNo");
    if (btnAcceptNo) {
        btnAcceptNo.addEventListener("click", () => {
            modalUtils.closeModal("confirmAcceptModal");
            window.location.href = "/gestore/dashboard"; // Torna dashboard
        });
    }

    const btnAcceptYes = document.getElementById("btnAcceptYes");
    if (btnAcceptYes) {
        btnAcceptYes.addEventListener("click", () => {
            modalUtils.closeModal("confirmAcceptModal");

            const id = document.getElementById("detailId").innerText;
            const csrf = csrfUtils.getCsrf();

            fetch(`/gestore/richieste/${id}/accetta`, {
                method: "POST",
                credentials: "same-origin",
                headers: {
                    "Content-Type": "application/json",
                    [csrf.header]: csrf.token
                }
            })
                .then(r => {
                    if (!r.ok) throw new Error("Errore HTTP " + r.status);
                    return r.text();
                })
                .then(() => {
                    modalUtils.openModal("acceptedModal");
                })
                .catch(err => {
                    console.error("Errore accettazione richiesta:", err);
                });
        });
    }

    const btnAcceptedOk = document.getElementById("btnAcceptedOk");
    if (btnAcceptedOk) {
        btnAcceptedOk.addEventListener("click", () => {
            modalUtils.closeModal("acceptedModal");
            window.location.href = "/gestore/dashboard";
        });
    }




    // ================== ELIMINA PROFILO (GESTORE) ==================
    const btnDeleteProfile = document.getElementById("btnDeleteProfile");
    if (btnDeleteProfile) {
        btnDeleteProfile.addEventListener("click", () => {
            modalUtils.openModal("deleteProfileModal");
        });
    }

    const confirmDeleteBtn = document.getElementById("confirmDeleteBtn");
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener("click", () => {
            console.log("Richiesta eliminazione profilo per Gestore");

            const csrf = csrfUtils.getCsrf();

            fetch("/gestore/richiesta-eliminazione", {
                method: "POST",
                credentials: "same-origin",
                headers: {
                    "Content-Type": "application/json",
                    [csrf.header]: csrf.token
                }
            })
                .then(r => {
                    if (r.status === 409) {
                        modalUtils.closeModal("deleteProfileModal");
                        modalUtils.openModal("deleteProfileErrorModal");
                        return null;
                    }
                    if (!r.ok) throw new Error("Errore HTTP " + r.status);
                    return r.text();
                })
                .then(resp => {
                    if (resp) {
                        modalUtils.closeModal("deleteProfileModal");
                        modalUtils.openModal("deleteProfileSuccessModal");
                    }
                })
                .catch(err => {
                    console.error("Errore eliminazione profilo gestore:", err);
                    modalUtils.closeModal("deleteProfileModal");
                    modalUtils.openModal("deleteProfileErrorModal");
                });
        });
    }

    const okDeleteBtn = document.getElementById("okDeleteBtn");
    if (okDeleteBtn) {
        okDeleteBtn.addEventListener("click", () => {
            modalUtils.closeModal("deleteProfileSuccessModal");
            window.location.href = "/";
        });
    }

    // ================== CHIUSURA MODALI GENERICHE ==================
    document.querySelectorAll(".btn-close-modal").forEach(btn => {
        btn.addEventListener("click", e => {
            e.stopPropagation();
            const target = btn.getAttribute("data-target");
            if (target) modalUtils.closeModal(target);
        });
    });
});
