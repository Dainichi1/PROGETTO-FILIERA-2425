/**
 * Utils per toggle di sezioni UI
 */
export let toggleUtils = (() => {

    /**
     * Toggle generico di una sezione
     * @param {string} sectionId - id della sezione/elemento
     */
    function toggleSection(sectionId) {
        const section = document.getElementById(sectionId);
        if (!section) return;

        // Usa lo stile calcolato dal browser, non solo quello inline
        const currentDisplay = window.getComputedStyle(section).display;
        section.style.display = (currentDisplay === "none") ? "block" : "none";
    }

    /**
     * Toggle specifico per "pubblicati" (Animatore)
     */
    function togglePubblicati(containerId = "pubblicatiContainer", btnId = "togglePubblicatiBtn") {
        const container = document.getElementById(containerId);
        const btn = document.getElementById(btnId);
        if (!container || !btn) return;

        const currentDisplay = window.getComputedStyle(container).display;
        const isHidden = currentDisplay === "none";

        container.style.display = isHidden ? "block" : "none";
        btn.textContent = isHidden ? "❌ Chiudi" : "📋 Visualizza visite/fiere pubblicate";
    }

    /**
     * Alias semantici per le dashboard
     */
    function toggleVisite() {
        toggleSection("visiteSection");
    }

    function togglePrenotazioni() {
        toggleSection("prenotazioniSection");
    }

    return {
        toggleSection,
        togglePubblicati,
        toggleVisite,
        togglePrenotazioni
    };
})();
