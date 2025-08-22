document.addEventListener('DOMContentLoaded', () => {
    const toggleBtn = document.getElementById('toggleFormBtn');
    const card = document.getElementById('createPackageCard');
    const form = document.getElementById('createPackageForm');

    const modal = document.getElementById('confirmModal');
    const modalYes = document.getElementById('modalYes');
    const modalNo  = document.getElementById('modalNo');

    const certificatiInput = document.getElementById('certificati');
    const certificatiInfo  = document.getElementById('certificatiInfo');
    const fotoInput        = document.getElementById('foto');
    const fotoInfo         = document.getElementById('fotoInfo');

    // mostra i NOME dei file come nel produttore
    const updateFileInfo = (input, infoEl) => {
        infoEl.textContent = input?.files?.length > 0
            ? Array.from(input.files).map(f => f.name).join(', ')
            : 'Nessun file selezionato';
    };
    certificatiInput?.addEventListener('change', () => updateFileInfo(certificatiInput, certificatiInfo));
    fotoInput?.addEventListener('change', () => updateFileInfo(fotoInput, fotoInfo));

    // toggle form
    toggleBtn?.addEventListener('click', () => {
        const visible = card.style.display !== 'none';
        card.style.display = visible ? 'none' : 'block';
        toggleBtn.textContent = visible ? 'Crea Pacchetto' : 'Chiudi Form';
    });

    // apri SEMPRE la modale prima dell'invio
    form?.addEventListener('submit', e => {
        if (form.dataset.confirmed === 'true') return; // già confermato
        e.preventDefault();
        modal?.classList.add('open');
    });

    // conferma invio → invia (validazione lato server)
    modalYes?.addEventListener('click', () => {
        modal.classList.remove('open');
        form.dataset.confirmed = 'true';
        form.submit();
    });

    // annulla → torna all'area distributore (come produttore)
    modalNo?.addEventListener('click', () => {
        modal.classList.remove('open');
        window.location.href = '/distributore';
    });

    // chiusura modale con ESC / click overlay
    document.addEventListener('keydown', (ev) => {
        if (ev.key === 'Escape' && modal.classList.contains('open')) modal.classList.remove('open');
    });
    modal?.addEventListener('click', (ev) => {
        if (ev.target === modal) modal.classList.remove('open');
    });
});
