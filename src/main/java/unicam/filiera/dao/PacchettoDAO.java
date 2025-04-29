package unicam.filiera.dao;

import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PacchettoDAO {

    private static final String CERTIFICATI_DIR = "uploads/certificati_pacchetti";
    private static final String FOTO_DIR = "uploads/foto_pacchetti";

    public PacchettoDAO() {
        creaCartelleSeNonEsistono();
    }

    private void creaCartelleSeNonEsistono() {
        new File(CERTIFICATI_DIR).mkdirs();
        new File(FOTO_DIR).mkdirs();
    }

    private String copiaFile(File file, String destinazione) {
        try {
            Path destPath = Paths.get(destinazione, file.getName());
            Files.copy(file.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
            return file.getName();
        } catch (IOException e) {
            e.printStackTrace();
            return "errore_" + file.getName();
        }
    }

    public List<Pacchetto> getTuttiIPacchetti() {
        List<PacchettoRaw> rawList = new ArrayList<>();

        /* 1. — lettura pura — */
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM pacchetti");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                rawList.add(new PacchettoRaw(
                        rs.getString("nome"),
                        rs.getString("descrizione"),
                        rs.getString("indirizzo"),
                        rs.getDouble("prezzo_totale"),
                        rs.getString("prodotti"),
                        rs.getString("certificati"),
                        rs.getString("foto"),
                        rs.getString("creato_da"),
                        rs.getString("stato"),
                        rs.getString("commento")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        /* 2. — elaborazione — */
        ProdottoDAO prodottoDAO = new ProdottoDAO();
        List<Pacchetto> pacchetti = new ArrayList<>();

        for (PacchettoRaw r : rawList) {
            List<Prodotto> prodotti = recuperaProdotti(r.prodottiString, prodottoDAO);

            if (r.nome == null || r.nome.isBlank()
                    || r.descrizione == null || r.descrizione.isBlank()
                    || prodotti.size() < 2) {
                System.err.println("Pacchetto ignorato: dati insufficienti -> " + r.nome);
                continue;
            }

            pacchetti.add(
                    new Pacchetto.Builder()
                            .nome(r.nome)
                            .descrizione(r.descrizione)
                            .indirizzo(r.indirizzo)
                            .prezzoTotale(r.prezzoTotale)
                            .prodotti(prodotti)
                            .certificati(splitNonVuoto(r.certificati))
                            .foto(splitNonVuoto(r.foto))
                            .creatoDa(r.creatoDa)
                            .stato(StatoProdotto.valueOf(r.stato))
                            .commento(r.commento)
                            .build()
            );
        }

        return pacchetti;
    }

    /* ======================================================================
   QUERY mirata per i pacchetti di un singolo distributore
   ====================================================================== */
    public List<Pacchetto> getPacchettiByCreatore(String creatoDa) {
        return getPacchetti("WHERE creato_da = ?", creatoDa);
    }

    /* ======================================================================
       Inserisce/riporta il pacchetto in stato IN_ATTESA (lista approvazioni)
       ====================================================================== */
    public boolean aggiungiInListaApprovazioni(Pacchetto pacchetto) {
        return aggiornaStatoPacchetto(pacchetto, StatoProdotto.IN_ATTESA);
    }

    /* ----------------------------------------------------------------------
       Utility privata ri-usata da più query
       ---------------------------------------------------------------------- */
    private List<Pacchetto> getPacchetti(String clausolaWhere, String parametro) {
        List<PacchettoRaw> raws = new ArrayList<>();

        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement st = c.prepareStatement("SELECT * FROM pacchetti " + clausolaWhere)) {

            if (parametro != null) st.setString(1, parametro);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    raws.add(new PacchettoRaw(
                            rs.getString("nome"), rs.getString("descrizione"), rs.getString("indirizzo"),
                            rs.getDouble("prezzo_totale"), rs.getString("prodotti"),
                            rs.getString("certificati"), rs.getString("foto"),
                            rs.getString("creato_da"), rs.getString("stato"), rs.getString("commento")));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        /* trasformazione Raw → Pacchetto (riusa codice esistente) */
        ProdottoDAO pDao = new ProdottoDAO();
        List<Pacchetto> out = new ArrayList<>();
        for (PacchettoRaw r : raws) {
            List<Prodotto> prodotti = recuperaProdotti(r.prodottiString, pDao);
            if (prodotti.size() < 2) continue;         // scarto pacchetti incompleti
            out.add(new Pacchetto.Builder()
                    .nome(r.nome).descrizione(r.descrizione).indirizzo(r.indirizzo)
                    .prezzoTotale(r.prezzoTotale).prodotti(prodotti)
                    .certificati(splitNonVuoto(r.certificati))
                    .foto(splitNonVuoto(r.foto))
                    .creatoDa(r.creatoDa)
                    .stato(StatoProdotto.valueOf(r.stato))
                    .commento(r.commento)
                    .build());
        }
        return out;
    }



    public List<Pacchetto> getPacchettiByStato(StatoProdotto stato) {
        List<Pacchetto> pacchetti = new ArrayList<>();
        List<PacchettoRaw> pacchettiRaw = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM pacchetti WHERE stato = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, stato.name());
                try (ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        PacchettoRaw raw = new PacchettoRaw(
                                rs.getString("nome"),
                                rs.getString("descrizione"),
                                rs.getString("indirizzo"),
                                rs.getDouble("prezzo_totale"),
                                rs.getString("prodotti"),
                                rs.getString("certificati"),
                                rs.getString("foto"),
                                rs.getString("creato_da"),
                                rs.getString("stato"),
                                rs.getString("commento")
                        );
                        pacchettiRaw.add(raw);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Adesso il ResultSet è chiuso: posso aprire nuove connessioni senza errori
        ProdottoDAO prodottoDAO = new ProdottoDAO();

        for (PacchettoRaw raw : pacchettiRaw) {
            try {
                List<Prodotto> prodotti = recuperaProdotti(raw.prodottiString, prodottoDAO);

                // Validazione base del pacchetto
                if (raw.nome == null || raw.nome.isBlank() ||
                        raw.descrizione == null || raw.descrizione.isBlank() ||
                        prodotti.size() < 2) {
                    System.err.println("Pacchetto ignorato: dati insufficienti -> " + raw.nome);
                    continue;
                }



                Pacchetto pacchetto = new Pacchetto.Builder()
                        .nome(raw.nome)
                        .descrizione(raw.descrizione)
                        .indirizzo(raw.indirizzo)
                        .prezzoTotale(raw.prezzoTotale)
                        .prodotti(prodotti)
                        .certificati(List.of(raw.certificati.split(",")))
                        .foto(List.of(raw.foto.split(",")))
                        .creatoDa(raw.creatoDa)
                        .stato(StatoProdotto.valueOf(raw.stato))
                        .commento(raw.commento)
                        .build();

                pacchetti.add(pacchetto);

            } catch (Exception ex) {
                System.err.println("Errore caricamento pacchetto: " + ex.getMessage());
            }
        }

        return pacchetti;
    }

    private Pacchetto costruisciPacchettoDaResultSet(ResultSet rs) throws SQLException {

        /* — 1. prelevo tutti i valori PRIMA di eseguire altre query — */
        String nome          = rs.getString("nome");
        String descrizione   = rs.getString("descrizione");
        String indirizzo     = rs.getString("indirizzo");
        double prezzoTotale  = rs.getDouble("prezzo_totale");
        String prodottiStr   = rs.getString("prodotti");
        List<String> certif  = splitNonVuoto(rs.getString("certificati"));
        List<String> foto    = splitNonVuoto(rs.getString("foto"));
        String creatoDa      = rs.getString("creato_da");
        StatoProdotto stato  = StatoProdotto.valueOf(rs.getString("stato"));
        String commento      = rs.getString("commento");

        /* — 2. ora posso aprire altre query senza rischiare di chiudere rs — */
        ProdottoDAO prodottoDAO = new ProdottoDAO();
        List<Prodotto> prodotti  = recuperaProdotti(prodottiStr, prodottoDAO);

        /* — 3. build Pacchetto — */
        return new Pacchetto.Builder()
                .nome(nome)
                .descrizione(descrizione)
                .indirizzo(indirizzo)
                .prezzoTotale(prezzoTotale)
                .prodotti(prodotti)          // ← obbligatorio
                .certificati(certif)
                .foto(foto)
                .creatoDa(creatoDa)
                .stato(stato)
                .commento(commento)
                .build();
    }

    /** Evita liste con un elemento vuoto [""] quando il campo è NULL o "" */
    private static List<String> splitNonVuoto(String v) {
        return (v == null || v.isBlank()) ? List.of() : List.of(v.split(","));
    }



    private List<Prodotto> recuperaProdotti(String prodottiString, ProdottoDAO prodottoDAO) {
        List<Prodotto> prodotti = new ArrayList<>();

        if (prodottiString == null || prodottiString.isBlank()) {
            return prodotti;
        }

        String[] nomi = prodottiString.split(",");
        for (String nome : nomi) {
            Prodotto prodotto = prodottoDAO.getProdottoByNome(nome.trim());
            if (prodotto != null) {
                prodotti.add(prodotto);
            } else {
                System.err.println("Prodotto non trovato: " + nome.trim());
            }
        }

        return prodotti;
    }

    public boolean salvaDettagli(Pacchetto pacchetto) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = """
                    INSERT INTO pacchetti 
                        (nome, descrizione, indirizzo, prezzo_totale, prodotti, certificati, foto, creato_da, stato, commento)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, pacchetto.getNome());
                stmt.setString(2, pacchetto.getDescrizione());
                stmt.setString(3, pacchetto.getIndirizzo());
                stmt.setDouble(4, pacchetto.getPrezzoTotale());
                stmt.setString(5, pacchetto.getProdotti().stream()
                        .map(Prodotto::getNome)
                        .collect(Collectors.joining(",")));
                stmt.setString(6, "");
                stmt.setString(7, "");
                stmt.setString(8, pacchetto.getCreatoDa());
                stmt.setString(9, pacchetto.getStato().name());
                stmt.setNull(10, Types.VARCHAR);

                stmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Errore salvataggio pacchetto: " + e.getMessage());
            return false;
        }
    }

    public boolean salvaFile(List<File> certificati, List<File> foto, Pacchetto pacchetto) {
        try (Connection conn = DatabaseManager.getConnection()) {
            List<String> nomiCertificati = certificati.stream()
                    .map(file -> copiaFile(file, CERTIFICATI_DIR))
                    .collect(Collectors.toList());

            List<String> nomiFoto = foto.stream()
                    .map(file -> copiaFile(file, FOTO_DIR))
                    .collect(Collectors.toList());

            String sql = "UPDATE pacchetti SET certificati = ?, foto = ? WHERE nome = ? AND creato_da = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, String.join(",", nomiCertificati));
                stmt.setString(2, String.join(",", nomiFoto));
                stmt.setString(3, pacchetto.getNome());
                stmt.setString(4, pacchetto.getCreatoDa());
                stmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Errore salvataggio file pacchetto: " + e.getMessage());
            return false;
        }
    }

    public boolean aggiornaStatoPacchetto(Pacchetto pacchetto, StatoProdotto nuovoStato) {
        String sql = "UPDATE pacchetti SET stato = ? WHERE nome = ? AND creato_da = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuovoStato.name());
            stmt.setString(2, pacchetto.getNome());
            stmt.setString(3, pacchetto.getCreatoDa());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore aggiornamento stato pacchetto: " + e.getMessage());
            return false;
        }
    }

    public boolean aggiornaStatoECommentoPacchetto(Pacchetto pacchetto, StatoProdotto nuovoStato, String commento) {
        String sql = "UPDATE pacchetti SET stato = ?, commento = ? WHERE nome = ? AND creato_da = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuovoStato.name());
            stmt.setString(2, commento);
            stmt.setString(3, pacchetto.getNome());
            stmt.setString(4, pacchetto.getCreatoDa());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore aggiornamento stato/commento pacchetto: " + e.getMessage());
            return false;
        }
    }

    // Classe interna di supporto
    private static class PacchettoRaw {
        String nome, descrizione, indirizzo, prodottiString, certificati, foto, creatoDa, stato, commento;
        double prezzoTotale;

        PacchettoRaw(String nome, String descrizione, String indirizzo, double prezzoTotale,
                     String prodottiString, String certificati, String foto, String creatoDa,
                     String stato, String commento) {
            this.nome = nome;
            this.descrizione = descrizione;
            this.indirizzo = indirizzo;
            this.prezzoTotale = prezzoTotale;
            this.prodottiString = prodottiString;
            this.certificati = certificati;
            this.foto = foto;
            this.creatoDa = creatoDa;
            this.stato = stato;
            this.commento = commento;
        }
    }
}
