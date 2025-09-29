// ================== IMPORT ==================
import {toggleUtils} from "../utils/toggle-utils.js";
import {modalUtils} from "../utils/modal-utils.js";
import {csrfUtils} from "../utils/csrf-utils.js";
import { crudUtils } from "../utils/crud-utils.js";

document.addEventListener("DOMContentLoaded", () => {
    const table = document.getElementById("contenutiTable");
    const tbody = table?.querySelector("tbody");
    const trasformatiCaricati = new Set();

    // cache indirizzi
    let indirizziCache = [];
    let mapInstance = null;
    let markersLayer = null;

    // ================== FUNZIONE POPOLA TABELLA INDIRIZZI ==================
    function popolaTabellaIndirizzi(data) {
        if (!tbody) return;
        tbody.innerHTML = "";
        if (Array.isArray(data) && data.length > 0) {
            data.forEach(item => {
                const tr = document.createElement("tr");
                tr.innerHTML = `
                    <td>${item.id}</td>
                    <td>${item.nome}</td>
                    <td>${item.tipo}</td>
                    <td>APPROVATO</td>
                    <td>${item.indirizzo}</td>
                `;
                tbody.appendChild(tr);
            });
        } else {
            tbody.innerHTML = `<tr><td colspan="5">Nessun indirizzo disponibile</td></tr>`;
        }
    }

    function mostraMappa(indirizziSelezionati) {
        modalUtils.openModal("mapModal");

        if (!mapInstance) {
            mapInstance = L.map("map").setView([41.8719, 12.5674], 6); // Italia
            L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
                attribution: "&copy; <a href='https://www.openstreetmap.org/'>OpenStreetMap</a> contributors"
            }).addTo(mapInstance);
            markersLayer = L.layerGroup().addTo(mapInstance);
        }

        // reset marker visivi
        markersLayer.clearLayers();

        // icone per tipologia
        const icons = {
            "Prodotto": new L.Icon({
                iconUrl: "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png",
                shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png",
                iconSize: [25, 41],
                iconAnchor: [12, 41],
                popupAnchor: [1, -34],
                shadowSize: [41, 41]
            }),
            "Pacchetto": new L.Icon({
                iconUrl: "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-blue.png",
                shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png",
                iconSize: [25, 41],
                iconAnchor: [12, 41],
                popupAnchor: [1, -34],
                shadowSize: [41, 41]
            }),
            "Fiera": new L.Icon({
                iconUrl: "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-green.png",
                shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png",
                iconSize: [25, 41],
                iconAnchor: [12, 41],
                popupAnchor: [1, -34],
                shadowSize: [41, 41]
            }),
            "Visita": new L.Icon({
                iconUrl: "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-orange.png",
                shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png",
                iconSize: [25, 41],
                iconAnchor: [12, 41],
                popupAnchor: [1, -34],
                shadowSize: [41, 41]
            }),
            "Prodotto Trasformato": new L.Icon({
                iconUrl: "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-violet.png",
                shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png",
                iconSize: [25, 41],
                iconAnchor: [12, 41],
                popupAnchor: [1, -34],
                shadowSize: [41, 41]
            })
        };

        // carico SEMPRE i marker persistenti dal backend
        fetch("/gestore/markers/api")
            .then(r => r.json())
            .then(savedMarkers => {
                // disegno marker già salvati
                savedMarkers.forEach(m => {
                    let tipo = "Prodotto";
                    const match = m.label.match(/\((.*?)\)$/);
                    if (match) tipo = match[1];

                    const icon = icons[tipo] || icons["Prodotto"];

                    L.marker([m.lat, m.lng], { icon })
                        .addTo(markersLayer)
                        .bindPopup(`<strong>${m.label}</strong>`)
                        .bindTooltip(tipo, {
                            permanent: true,
                            direction: "bottom",
                            offset: [0, 6],
                            className: "marker-label"
                        });
                });

                if (!indirizziSelezionati || indirizziSelezionati.length === 0) {
                    mapInstance.setView([41.8719, 12.5674], 6);
                    return;
                }

                const normaliDaSalvare = [];
                let bounds = [];

                // === Marker normali ===
                indirizziSelezionati.forEach(item => {
                    if (item.lat && item.lng) {
                        const duplicato = savedMarkers.find(m =>
                            m.label.includes(item.nome) && m.label.includes(item.tipo)
                        );
                        if (duplicato) return;

                        const icon = icons[item.tipo] || icons["Prodotto"];
                        L.marker([item.lat, item.lng], { icon })
                            .addTo(markersLayer)
                            .bindPopup(`<strong>${item.nome}</strong><br>${item.indirizzo}<br>(${item.tipo})`)
                            .bindTooltip(item.tipo, {
                                permanent: true,
                                direction: "bottom",
                                offset: [0, 6],
                                className: "marker-label"
                            });

                        if (item.tipo !== "Prodotto Trasformato") {
                            normaliDaSalvare.push(item);
                            bounds.push([item.lat, item.lng]);
                        }
                    }
                });

                // === Prodotti trasformati ===
                const trasformati = indirizziSelezionati.filter(i => i.tipo === "Prodotto Trasformato");

// preparo tutte le fetch in parallelo
                const trasformatiFetch = trasformati.map(t =>
                    fetch(`/gestore/prodotti-trasformati/api/${t.id}/path`)
                        .then(r => {
                            if (r.status === 400) {
                                const invalidMsg = document.getElementById("invalidAddressMessage");
                                if (invalidMsg) invalidMsg.innerText = `❌ Indirizzo non valido per prodotto trasformato: ${t.nome}`;
                                modalUtils.openModal("invalidAddressModal");
                                throw new Error("Indirizzo trasformato non valido");
                            }
                            return r.json();
                        })
                );

// aspetto tutte le risposte insieme
                Promise.all(trasformatiFetch)
                    .then(results => {
                        results.forEach(data => {
                            if (!data || !data.latTrasformato || !data.lngTrasformato) return;

                            const pathCoords = [];
                            const markersDaSalvare = [];

                            // marker trasformato
                            L.marker([data.latTrasformato, data.lngTrasformato], { icon: icons["Prodotto Trasformato"] })
                                .addTo(markersLayer)
                                .bindPopup(`<strong>${data.nomeTrasformato}</strong><br>Prodotto Trasformato`);
                            pathCoords.push([data.latTrasformato, data.lngTrasformato]);
                            bounds.push([data.latTrasformato, data.lngTrasformato]);

                            markersDaSalvare.push({
                                lat: data.latTrasformato,
                                lng: data.lngTrasformato,
                                label: `${data.nomeTrasformato} (Prodotto Trasformato)`,
                                color: "#800080"
                            });

                            // marker fasi
                            data.fasi.forEach(f => {
                                const nomeProdotto = f.nomeProdotto || f.nome || "Senza nome";
                                L.marker([f.lat, f.lng], { icon: icons["Prodotto"] })
                                    .addTo(markersLayer)
                                    .bindPopup(`<strong>${nomeProdotto}</strong><br>${f.descrizioneFase}`);
                                pathCoords.push([f.lat, f.lng]);
                                bounds.push([f.lat, f.lng]);

                                markersDaSalvare.push({
                                    lat: f.lat,
                                    lng: f.lng,
                                    label: `${nomeProdotto} (Fase Produzione)`,
                                    color: "#FF0000"
                                });
                            });

                            // linea tratteggiata viola
                            if (pathCoords.length > 1) {
                                L.polyline(pathCoords, { color: "purple", weight: 3, dashArray: "5,5" })
                                    .addTo(markersLayer);
                            }

                            // salvo marker
                            const csrf = csrfUtils.getCsrf();
                            fetch("/gestore/markers/api/batch", {
                                method: "POST",
                                credentials: "same-origin",
                                headers: {
                                    "Content-Type": "application/json",
                                    [csrf.header]: csrf.token
                                },
                                body: JSON.stringify(markersDaSalvare)
                            }).catch(err => console.error("❌ Errore salvataggio prodotti trasformati:", err));

                            // salvo anche il path
                            fetch("/gestore/paths/api", {
                                method: "POST",
                                credentials: "same-origin",
                                headers: {
                                    "Content-Type": "application/json",
                                    [csrf.header]: csrf.token
                                },
                                body: JSON.stringify({
                                    prodottoTrasformatoId: data.trasformatoId,
                                    coords: pathCoords
                                })
                            }).catch(err => console.error("❌ Errore salvataggio path:", err));
                        });

                        if (bounds.length > 0) {
                            mapInstance.fitBounds(bounds, { padding: [50, 50] });
                        }
                    })
                    .catch(err => console.error("❌ Errore caricamento prodotti trasformati:", err));

                // salvo marker normali
                if (normaliDaSalvare.length > 0) {
                    const csrf = csrfUtils.getCsrf();
                    fetch("/gestore/markers/api/batch", {
                        method: "POST",
                        credentials: "same-origin",
                        headers: {
                            "Content-Type": "application/json",
                            [csrf.header]: csrf.token
                        },
                        body: JSON.stringify(normaliDaSalvare.map(i => ({
                            lat: i.lat,
                            lng: i.lng,
                            label: `${i.nome} (${i.tipo})`,
                            color: "#FF0000"
                        })))
                    }).catch(err => console.error("❌ Errore salvataggio marker normali:", err));
                }

                if (bounds.length > 0) {
                    mapInstance.fitBounds(bounds, { padding: [50, 50] });
                }
            })
            .catch(err => console.error("❌ Errore caricamento marker persistenti:", err));

        // === carico anche i path già salvati per i prodotti trasformati ===
        fetch("/gestore/paths/api")
            .then(r => r.json())
            .then(savedPaths => {
                savedPaths.forEach(path => {
                    if (path.coords && path.coords.length > 1) {
                        L.polyline(path.coords, { color: "purple", weight: 3, dashArray: "5,5" })
                            .addTo(markersLayer);
                    }
                });
            })
            .catch(err => console.error("❌ Errore caricamento paths persistenti:", err));
    }





    function showDuplicateAddressModal(indirizzo) {
        const modal = document.getElementById("duplicateAddressModal");
        const msg = document.getElementById("duplicateAddressMessage");
        const okBtn = document.getElementById("duplicateAddressOk");

        if (msg) {
            msg.innerText = `⚠️ L’indirizzo "${indirizzo}" è già presente sulla mappa.`;
        }

        modalUtils.openModal("duplicateAddressModal");

        if (okBtn) {
            okBtn.onclick = () => {
                modalUtils.closeModal("duplicateAddressModal");
                window.location.href = "/gestore/dashboard"; // oppure solo chiudere la modale
            };
        }
    }



    // ================== FETCH INDIRIZZI (con cache) ==================
    function fetchIndirizzi(forceRefresh = false) {
        if (indirizziCache.length > 0 && !forceRefresh) {
            return Promise.resolve(indirizziCache);
        }

        const csrf = csrfUtils.getCsrf();
        return fetch("/gestore/indirizzi/api", {
            credentials: "same-origin",
            headers: { [csrf.header]: csrf.token }
        })
            .then(r => {
                if (!r.ok) throw new Error("Errore HTTP " + r.status);
                return r.json();
            })
            .then(data => {
                indirizziCache = Array.isArray(data) ? data : [];
                return indirizziCache;
            })
            .catch(err => {
                console.error("Errore fetch indirizzi:", err);
                return [];
            });
    }

    // ================== NUOVO BOTTONE "VISUALIZZA MAPPA" ==================
    const btnApriMappa = document.getElementById("btnApriMappa");
    if (btnApriMappa) {
        btnApriMappa.addEventListener("click", () => {
            // mappa vuota
            mostraMappa([]);
        });
    }

    // ================== GESTIONE INDIRIZZI MAPPA ==================
    const btnGestioneIndirizzi = document.getElementById("btnGestioneIndirizziMappa");
    if (btnGestioneIndirizzi) {
        btnGestioneIndirizzi.addEventListener("click", () => {
            modalUtils.openModal("mapAddressesModal");

            const container = document.getElementById("addressListContainer");
            if (container) container.innerHTML = "<p><i>Caricamento indirizzi...</i></p>";

            fetchIndirizzi().then(data => {
                if (container) {
                    container.innerHTML = "";
                    if (data.length > 0) {
                        data.forEach(item => {
                            const div = document.createElement("div");
                            div.innerHTML = `
                                <label>
                                    <input type="checkbox" value="${item.id}" 
                                           data-nome="${item.nome}" 
                                           data-indirizzo="${item.indirizzo}" 
                                           data-tipo="${item.tipo}" 
                                           data-lat="${item.lat}" 
                                           data-lng="${item.lng}">
                                    ${item.nome} – ${item.indirizzo} (${item.tipo})
                                </label>
                            `;
                            container.appendChild(div);
                        });
                    } else {
                        container.innerHTML = "<p><i>Nessun indirizzo disponibile.</i></p>";
                    }
                }
            });
        });
    }

    // ================== CONFERMA SELEZIONE INDIRIZZI ==================
    const btnConfirmAddresses = document.getElementById("btnConfirmAddresses");
    if (btnConfirmAddresses) {
        btnConfirmAddresses.addEventListener("click", () => {
            const selectedCheckboxes = document.querySelectorAll("#addressListContainer input[type=checkbox]:checked");
            let indirizziSelezionati = Array.from(selectedCheckboxes).map(cb => ({
                id: cb.value,
                nome: cb.dataset.nome,
                indirizzo: cb.dataset.indirizzo,
                tipo: cb.dataset.tipo
            }));

            modalUtils.closeModal("mapAddressesModal");

            if (indirizziSelezionati.length > 0) {
                const csrf = csrfUtils.getCsrf();

                // 🔎 controllo duplicati direttamente dal backend
                fetch("/gestore/markers/api", {
                    credentials: "same-origin",
                    headers: { [csrf.header]: csrf.token }
                })
                    .then(r => {
                        if (!r.ok) throw new Error("Errore HTTP " + r.status);
                        return r.json();
                    })
                    .then(savedMarkers => {
                        // confronto indirizzi selezionati con quelli già salvati
                        let duplicati = indirizziSelezionati.filter(sel =>
                            savedMarkers.some(m =>
                                m.label.includes(sel.nome) && m.label.includes(sel.tipo)
                            )
                        );

                        if (duplicati.length > 0) {
                            duplicati.forEach(d => {
                                showDuplicateAddressModal(d.indirizzo);

                                // deseleziono la checkbox duplicata
                                const cb = document.querySelector(`#addressListContainer input[value="${d.id}"]`);
                                if (cb) cb.checked = false;
                            });

                            // rimuovo i duplicati prima di inviare al backend
                            indirizziSelezionati = indirizziSelezionati.filter(sel =>
                                !duplicati.some(d => d.id === sel.id)
                            );

                            if (indirizziSelezionati.length === 0) return; // tutti duplicati → stop
                        }

                        // 🔹 proseguo con il geocoding
                        return fetch("/gestore/indirizzi/api/geocode", {
                            method: "POST",
                            credentials: "same-origin",
                            headers: {
                                "Content-Type": "application/json",
                                [csrf.header]: csrf.token
                            },
                            body: JSON.stringify(indirizziSelezionati)
                        });
                    })
                    .then(r => {
                        if (!r) return; // se fermato per duplicati
                        if (!r.ok) throw new Error("Errore HTTP " + r.status);
                        return r.json();
                    })
                    .then(data => {
                        if (!data) return;

                        if (!Array.isArray(data) || data.length === 0) {
                            showInvalidAddressModal("Indirizzo sconosciuto");
                            return;
                        }

                        const invalid = data.find(d => !d.lat || !d.lng);
                        if (invalid) {
                            showInvalidAddressModal(invalid.indirizzo);
                            return;
                        }

                        mostraMappa(data);
                    })
                    .catch(err => {
                        console.error("Errore nel controllo duplicati/geocoding:", err);
                        showInvalidAddressModal("Errore durante il geocoding");
                    });

            } else {
                mostraMappa([]); // niente selezione → mappa vuota
            }
        });
    }


    // ================== PRELOAD INDIRIZZI ==================
    fetchIndirizzi(true); // carico subito in background all'avvio

    // === MODALE INDIRIZZO NON VALIDO ===
    const invalidAddressModal = document.getElementById("invalidAddressModal");
    const invalidAddressMessage = document.getElementById("invalidAddressMessage");
    const btnInvalidAddressOk = document.getElementById("btnInvalidAddressOk");

    function showInvalidAddressModal(indirizzo) {
        if (invalidAddressMessage) {
            invalidAddressMessage.innerText =
                `❌ L’indirizzo "${indirizzo}" non è valido o non è stato trovato.`;
        }
        modalUtils.openModal("invalidAddressModal");
    }

    if (btnInvalidAddressOk) {
        btnInvalidAddressOk.addEventListener("click", () => {
            modalUtils.closeModal("invalidAddressModal");
            window.location.href = "/gestore/dashboard"; // torna alla home del gestore
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
            window.location.href = "/gestore/dashboard";
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
        fetch("/gestore/richiesta-eliminazione/stato", {
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

    const pollingInterval = setInterval(pollEliminazione, 5000); // ogni 5s


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

    // === VISUALIZZA SOCIAL FEED ===
    const btnSocialFeed = document.getElementById("btnSocialFeed");
    if (btnSocialFeed) {
        btnSocialFeed.addEventListener("click", () => {
            console.log("Caricamento Social Feed per Gestore");
            crudUtils.openSocialFeed();
        });
    }

});
