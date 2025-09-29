// ================== COMMON MAP UTILS ==================
import { modalUtils } from "./modal-utils.js";

let mapInstance = null;
let markersLayer = null;

export const commonMapUtils = {
    mostraMappa: () => {
        modalUtils.openModal("mapModal");

        if (!mapInstance) {
            mapInstance = L.map("map").setView([41.8719, 12.5674], 6); // Italia
            L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
                attribution: "&copy; <a href='https://www.openstreetmap.org/'>OpenStreetMap</a> contributors"
            }).addTo(mapInstance);
            markersLayer = L.layerGroup().addTo(mapInstance);
        }

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

        // === Carico marker dal backend ===
        fetch("/gestore/markers/api")
            .then(r => r.json())
            .then(savedMarkers => {
                let bounds = [];

                // Marker salvati
                savedMarkers.forEach(m => {
                    let tipo = "Prodotto";
                    const match = m.label.match(/\((.*?)\)$/);
                    if (match) tipo = match[1];

                    const icon = icons[tipo] || icons["Prodotto"];
                    L.marker([m.lat, m.lng], { icon })
                        .addTo(markersLayer)
                        .bindPopup(`<strong>${m.label}</strong>`);
                    bounds.push([m.lat, m.lng]);
                });

                // === Carico paths dei prodotti trasformati ===
                fetch("/gestore/paths/api")
                    .then(r => r.json())
                    .then(savedPaths => {
                        savedPaths.forEach(path => {
                            if (path.coords && path.coords.length > 1) {
                                L.polyline(path.coords, {
                                    color: "purple",
                                    weight: 3,
                                    dashArray: "5,5"
                                }).addTo(markersLayer);
                            }
                        });
                    });

                if (bounds.length > 0) {
                    mapInstance.fitBounds(bounds, { padding: [50, 50] });
                } else {
                    mapInstance.setView([41.8719, 12.5674], 6);
                }
            })
            .catch(err => console.error("❌ Errore caricamento marker:", err));
    }
};
