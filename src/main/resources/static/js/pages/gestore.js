import { toggleUtils } from "../utils/toggle-utils.js";

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
            if (row.querySelector("td[colspan]")) return; // ignora riga vuota/messaggio

            const text = row.innerText.toLowerCase();
            const statoCell = row.cells[3]?.innerText.toLowerCase() || "";

            const matchesSearch = text.includes(searchTerm);
            const matchesStato = stato === "" || statoCell.includes(stato);

            row.style.display = (matchesSearch && matchesStato) ? "" : "none";
        });
    }

    if (searchInput) searchInput.addEventListener("input", filterRows);
    if (statoSelect) statoSelect.addEventListener("change", filterRows);

    // ================== ORDINAMENTO ==================
    let sortDirection = 1; // 1 = asc, -1 = desc

    function sortRows() {
        if (!tbody) return;
        const rows = Array.from(tbody.querySelectorAll("tr"))
            .filter(r => !r.querySelector("td[colspan]")); // esclude riga "nessun contenuto"

        const criterion = sortSelect?.value;
        if (!criterion) return;

        let colIndex = 0;
        switch (criterion) {
            case "nome": colIndex = 1; break;
            case "tipo": colIndex = 2; break;
            case "stato": colIndex = 3; break;
            case "data": colIndex = 4; break;
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

    if (sortSelect) sortSelect.addEventListener("change", sortRows);

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
        // apri modale 1 se nessun contenuto
        toggleUtils.show(modalNoContent);

        // bottone OK della prima modale
        const btnOk = modalNoContent.querySelector(".btn-ok");
        if (btnOk) {
            btnOk.addEventListener("click", () => {
                toggleUtils.hide(modalNoContent);
                toggleUtils.show(modalChoice);
            });
        }

        // bottoni della seconda modale
        const btnRiepilogo = modalChoice.querySelector(".btn-riepilogo");
        const btnCategoria = modalChoice.querySelector(".btn-categoria");

        if (btnRiepilogo) {
            btnRiepilogo.addEventListener("click", () => {
                toggleUtils.hide(modalChoice);
                // Torna al riepilogo → resta nella pagina, non serve redirect
            });
        }

        if (btnCategoria) {
            btnCategoria.addEventListener("click", () => {
                toggleUtils.hide(modalChoice);
                // Seleziona un'altra categoria → resta nella pagina
            });
        }
    }
});
