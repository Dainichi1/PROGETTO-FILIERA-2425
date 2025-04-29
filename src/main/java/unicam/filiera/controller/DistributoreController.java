package unicam.filiera.controller;

import unicam.filiera.dao.PacchettoDAO;
import unicam.filiera.dao.ProdottoDAO;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.model.observer.PacchettoNotifier;
import unicam.filiera.util.ValidatorePacchetto;

import java.io.File;
import java.util.List;
import java.util.function.BiConsumer;

public class DistributoreController {

    /* ------------------------------------------------------------------ */
    /*  F I E L D S                                                       */
    /* ------------------------------------------------------------------ */
    private final String       username;
    private final PacchettoDAO pacchettoDAO = new PacchettoDAO();
    private final ProdottoDAO  prodottoDAO  = new ProdottoDAO();

    /* ------------------------------------------------------------------ */
    /*  C O N S T R U C T O R                                             */
    /* ------------------------------------------------------------------ */
    public DistributoreController(String username) {
        this.username = username;
    }

    /* ======================================================================
       SERVIZI DI SOLA-LETTURA (usati dalla view)
       ====================================================================== */
    /** Prodotti approvati e quindi visibili nel Marketplace. */
    public List<Prodotto> getProdottiMarketplace() {
        return prodottoDAO.getProdottiByStato(StatoProdotto.APPROVATO);
    }

    /** Pacchetti creati dallo stesso distributore. */
    public List<Pacchetto> getPacchettiCreatiDaMe() {
        return pacchettoDAO.getPacchettiByCreatore(username);
    }

    /* ======================================================================
       CASO D’USO “CREA & INVIA PACCHETTO”
       ====================================================================== */
    /**
     * Flusso completo: validazione, persistenza, upload file,
     * passaggio in stato IN_ATTESA e notifica al Curatore.
     * <p>
     * Il risultato viene ritornato tramite la callback,
     * così la view può aggiornarsi senza conoscere la logica.
     */
    public void inviaPacchetto(String nome,
                               String descrizione,
                               String indirizzo,
                               String prezzoStr,
                               List<Prodotto> prodotti,
                               List<File> certificati,
                               List<File> foto,
                               BiConsumer<Boolean,String> callback) {

        boolean ok  = false;
        String  msg;

        try {
            /* ----------------- VALIDAZIONE ----------------- */
            double prezzo = Double.parseDouble(prezzoStr.trim());
            ValidatorePacchetto.valida(
                    nome, descrizione, indirizzo, prezzo, prodotti);
            ValidatorePacchetto.validaFileCaricati(
                    certificati.size(), foto.size());

            /* ----------------- COSTRUZIONE ----------------- */
            Pacchetto pacchetto = new Pacchetto.Builder()
                    .nome(nome)
                    .descrizione(descrizione)
                    .indirizzo(indirizzo)
                    .prezzoTotale(prezzo)
                    .prodotti(prodotti)
                    .creatoDa(username)
                    .stato(StatoProdotto.IN_ATTESA)
                    .build();

            /* ----------------- PERSISTENZA ----------------- */
            boolean dett = pacchettoDAO.salvaDettagli(pacchetto);
            boolean file = pacchettoDAO.salvaFile(certificati, foto, pacchetto);
            boolean s1   = pacchettoDAO.aggiornaStatoPacchetto(
                    pacchetto, StatoProdotto.IN_ATTESA);
            boolean s2   = pacchettoDAO.aggiungiInListaApprovazioni(pacchetto);

            if (dett && file && s1 && s2) {
                PacchettoNotifier.getInstance()
                        .notificaTutti(pacchetto, "NUOVO_PACCHETTO");
                ok  = true;
                msg = "Pacchetto inviato al curatore per approvazione!";
            } else {
                msg = "Errore durante il caricamento del pacchetto.";
            }

        } catch (NumberFormatException nfe) {
            msg = "Il prezzo deve essere un numero valido.";
        } catch (IllegalArgumentException iae) {
            msg = iae.getMessage();
        } catch (Exception ex) {
            msg = "Errore inaspettato: " + ex.getMessage();
        }

        /* ----------------- CALLBACK UI-SAFE ----------------- */
        callback.accept(ok, msg);
    }
}
