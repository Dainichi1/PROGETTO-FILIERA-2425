package unicam.filiera.util;

import unicam.filiera.dao.PacchettoDAO;
import unicam.filiera.dao.ProdottoDAO;
import unicam.filiera.dao.ProdottoTrasformatoDAO;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.ProdottoTrasformato;
import unicam.filiera.model.StatoProdotto;

public final class ValidatoreAnnuncioItem {
    private ValidatoreAnnuncioItem() {
    }

    public static void valida(String titolo,
                              String testo,
                              String nomeProdotto,
                              String username,
                              ProdottoDAO prodottoDAO) {
        if (isBlank(titolo)) throw new IllegalArgumentException("Il titolo è obbligatorio.");
        if (titolo.length() > 100) throw new IllegalArgumentException("Il titolo non può superare 100 caratteri.");
        if (isBlank(testo)) throw new IllegalArgumentException("Il testo è obbligatorio.");
        if (testo.length() > 1000) throw new IllegalArgumentException("Il testo non può superare 1000 caratteri.");
        if (isBlank(nomeProdotto)) throw new IllegalArgumentException("Nome prodotto mancante.");

        Prodotto p = prodottoDAO.findByNome(nomeProdotto);
        if (p == null) throw new IllegalArgumentException("Prodotto non trovato.");
        if (!username.equals(p.getCreatoDa()))
            throw new IllegalArgumentException("Non sei l'autore del prodotto selezionato.");
        if (p.getStato() != StatoProdotto.APPROVATO)
            throw new IllegalArgumentException("Il prodotto non è approvato, non può essere pubblicizzato.");
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static void validaProdottoTrasformato(
            String titolo,
            String testo,
            String nomeProdotto,
            String autoreUsername,
            ProdottoTrasformatoDAO dao
    ) {
        if (titolo == null || titolo.isBlank()) throw new IllegalArgumentException("Il titolo è obbligatorio.");
        if (titolo.length() > 100) throw new IllegalArgumentException("Il titolo non può superare 100 caratteri.");
        if (testo == null || testo.isBlank()) throw new IllegalArgumentException("Il testo è obbligatorio.");
        if (testo.length() > 1000) throw new IllegalArgumentException("Il testo non può superare 1000 caratteri.");
        if (nomeProdotto == null || nomeProdotto.isBlank())
            throw new IllegalArgumentException("Prodotto non selezionato.");

        ProdottoTrasformato p = dao.findByNome(nomeProdotto);
        if (p == null) throw new IllegalArgumentException("Prodotto trasformato non trovato.");
        if (!autoreUsername.equals(p.getCreatoDa()))
            throw new IllegalArgumentException("Non sei l'autore del prodotto selezionato.");
        if (p.getStato() != StatoProdotto.APPROVATO)
            throw new IllegalArgumentException("Il prodotto non è approvato sul Marketplace.");
    }

    public static void validaPacchetto(String titolo,
                                       String testo,
                                       String nomePacchetto,
                                       String usernameDistributore,
                                       PacchettoDAO pacchettoDAO) {
        if (titolo == null || titolo.isBlank() || testo == null || testo.isBlank())
            throw new IllegalArgumentException("Titolo e Testo sono obbligatori.");

        Pacchetto p = pacchettoDAO.findByNome(nomePacchetto);
        if (p == null)
            throw new IllegalArgumentException("Pacchetto non trovato.");
        if (!usernameDistributore.equalsIgnoreCase(p.getCreatoDa()))
            throw new IllegalArgumentException("Non sei l'autore del pacchetto selezionato.");
        if (p.getStato() != StatoProdotto.APPROVATO)
            throw new IllegalArgumentException("Puoi pubblicare solo pacchetti APPROVATI.");
    }

}
