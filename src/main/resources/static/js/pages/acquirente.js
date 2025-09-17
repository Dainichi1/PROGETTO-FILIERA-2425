// ================== IMPORT ==================
import {toggleUtils} from "../utils/toggle-utils.js";

window.toggleUtils = toggleUtils;

// ================== FUNZIONI SPECIFICHE ACQUIRENTE ==================

// Toggle della sezione marketplace (Visualizza / Chiudi Marketplace)
function initMarketplaceToggle() {
    const btnMarketplace = document.getElementById("btnVisualizzaMarketplace");
    const btnChiudi = document.getElementById("btnChiudiMarketplace");
    const marketplaceSection = document.getElementById("marketplaceSection");

    if (btnMarketplace && marketplaceSection) {
        btnMarketplace.addEventListener("click", () => {
            marketplaceSection.style.display = "block";
            initCheckboxActions();
        });
    }

    if (btnChiudi && marketplaceSection) {
        btnChiudi.addEventListener("click", () => {
            marketplaceSection.style.display = "none";
        });
    }
}

// Gestione checkbox → mostra bottone "Aggiungi al carrello"
function initCheckboxActions() {
    document.querySelectorAll(".select-checkbox").forEach(cb => {
        cb.addEventListener("change", function () {
            const actionCell = this.closest("tr").querySelector(".action-cell");
            if (this.checked) {
                actionCell.innerHTML = '<button type="button" class="btn-add-cart">Aggiungi al carrello</button>';
            } else {
                actionCell.innerHTML = '';
            }
        });
    });
}

// ================== INIZIALIZZAZIONE ==================
document.addEventListener("DOMContentLoaded", () => {
    initMarketplaceToggle();

    // se la tabella fosse già caricata e visibile
    initCheckboxActions();
});
