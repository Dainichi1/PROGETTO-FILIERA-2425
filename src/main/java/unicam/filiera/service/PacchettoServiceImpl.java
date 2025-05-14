package unicam.filiera.service;

import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.model.observer.PacchettoNotifier;
import unicam.filiera.dao.PacchettoDAO;
import unicam.filiera.dao.ProdottoDAO;
import unicam.filiera.dao.JdbcPacchettoDAO;
import unicam.filiera.dao.JdbcProdottoDAO;
import unicam.filiera.util.ValidatorePacchetto;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class PacchettoServiceImpl implements PacchettoService {
    private final PacchettoDAO pacchettoDao;
    private final ProdottoDAO prodottoDao;
    private final PacchettoNotifier notifier;

    public PacchettoServiceImpl(PacchettoDAO pacchettoDao, ProdottoDAO prodottoDao) {
        this.pacchettoDao = pacchettoDao;
        this.prodottoDao = prodottoDao;
        this.notifier = PacchettoNotifier.getInstance();
    }

    public PacchettoServiceImpl() {
        this(JdbcPacchettoDAO.getInstance(), JdbcProdottoDAO.getInstance());
    }

    @Override
    public void creaPacchetto(PacchettoDto dto, String creatore) {
        // parsing controllato del prezzo totale
        double prezzoTotale;
        try {
            prezzoTotale = Double.parseDouble(dto.getPrezzoTxt());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("⚠ Prezzo totale non valido (deve essere un numero)");
        }

        // risoluzione e raccolta prodotti esistenti
        List<unicam.filiera.model.Prodotto> prodotti = dto.getNomiProdotti().stream()
                .map(String::trim)
                .map(prodottoDao::findByNome)
                .filter(p -> p != null)
                .collect(Collectors.toList());

        // 1) validazione di dominio
        ValidatorePacchetto.valida(
                dto.getNome(),
                dto.getDescrizione(),
                dto.getIndirizzo(),
                prezzoTotale,
                prodotti
        );
        ValidatorePacchetto.validaFileCaricati(
                dto.getCertificati().size(),
                dto.getFoto().size()
        );

        // 2) mapping DTO → domain
        Pacchetto p = new Pacchetto.Builder()
                .nome(dto.getNome())
                .descrizione(dto.getDescrizione())
                .indirizzo(dto.getIndirizzo())
                .prezzoTotale(prezzoTotale)
                .prodotti(prodotti)
                .certificati(dto.getCertificati().stream()
                        .map(File::getName)
                        .collect(Collectors.toList()))
                .foto(dto.getFoto().stream()
                        .map(File::getName)
                        .collect(Collectors.toList()))
                .creatoDa(creatore)
                .stato(StatoProdotto.IN_ATTESA)
                .build();

        // 3a) salva dettagli pacchetto
        if (!pacchettoDao.saveDetails(p)) {
            throw new RuntimeException("Errore salvataggio dettagli pacchetto");
        }
        // 3b) salva file pacchetto
        if (!pacchettoDao.saveFiles(p, dto.getCertificati(), dto.getFoto())) {
            throw new RuntimeException("Errore upload file pacchetto");
        }

        // 4) notifica observer
        notifier.notificaTutti(p, "NUOVO_PACCHETTO");
    }

    @Override
    public List<Pacchetto> getPacchettiCreatiDa(String creatore) {
        return pacchettoDao.findByCreatore(creatore);
    }

    @Override
    public List<Pacchetto> getPacchettiByStato(StatoProdotto stato) {
        return pacchettoDao.findByStato(stato);
    }

    @Override
    public void eliminaPacchetto(String nome, String creatore) {
        // 1. Recupera il pacchetto specifico
        List<Pacchetto> lista = pacchettoDao.findByCreatore(creatore);
        Pacchetto p = lista.stream()
                .filter(x -> x.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .orElse(null);

        // 2. Valida eliminazione
        ValidatorePacchetto.validaEliminazione(p);

        // 3. Esegui la cancellazione
        boolean ok = pacchettoDao.deleteByNomeAndCreatore(nome, creatore);
        if (!ok) {
            throw new RuntimeException("Errore durante l'eliminazione di \"" + nome + "\"");
        }

        // 4. Notifica gli observer
        notifier.notificaTutti(p, "ELIMINATO_PACCHETTO");
    }


}
