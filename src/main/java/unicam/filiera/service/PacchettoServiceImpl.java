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
    private final PacchettoDAO     pacchettoDao;
    private final ProdottoDAO      prodottoDao;
    private final PacchettoNotifier notifier;

    public PacchettoServiceImpl(PacchettoDAO pacchettoDao, ProdottoDAO prodottoDao) {
        this.pacchettoDao = pacchettoDao;
        this.prodottoDao  = prodottoDao;
        this.notifier     = PacchettoNotifier.getInstance();
    }
    public PacchettoServiceImpl() {
        this(JdbcPacchettoDAO.getInstance(), JdbcProdottoDAO.getInstance());
    }

    @Override
    public void creaPacchetto(PacchettoDto dto, String creatore) {
        // 1) validazione
        double prezzoTotale = Double.parseDouble(dto.getPrezzoTxt());
        ValidatorePacchetto.valida(
                dto.getNome(), dto.getDescrizione(), dto.getIndirizzo(),
                prezzoTotale,
                dto.getNomiProdotti().stream()
                        .map(prodottoDao::findByNome)
                        .filter(p -> p != null)
                        .collect(Collectors.toList())
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
                .prodotti(dto.getNomiProdotti().stream()
                        .map(String::trim)
                        .map(prodottoDao::findByNome)
                        .collect(Collectors.toList()))
                .certificati(dto.getCertificati().stream()
                        .map(File::getName).collect(Collectors.toList()))
                .foto(dto.getFoto().stream()
                        .map(File::getName).collect(Collectors.toList()))
                .creatoDa(creatore)
                .stato(StatoProdotto.IN_ATTESA)
                .build();

        // 3a) salva i soli dettagli
        if (!pacchettoDao.saveDetails(p)) {
            throw new RuntimeException("Errore salvataggio dettagli pacchetto");
        }
        // 3b) salva certificati e foto
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
}
