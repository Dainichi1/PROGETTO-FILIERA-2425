package unicam.filiera.controller;

import unicam.filiera.dao.DatabaseManager;
import unicam.filiera.dto.ElementoPiattaformaDto;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class GestoreContenutiController {

    public List<CategoriaContenuto> getCategorieContenuti() {
        return Arrays.asList(
                CategoriaContenuto.UTENTI,
                CategoriaContenuto.PRODOTTI,
                CategoriaContenuto.PACCHETTI,
                CategoriaContenuto.PRODOTTI_TRASFORMATI,
                CategoriaContenuto.FIERE,
                CategoriaContenuto.VISITE_INVITO,
                CategoriaContenuto.ACQUISTI,
                CategoriaContenuto.PRENOTAZIONI_FIERE,
                CategoriaContenuto.PRENOTAZIONI_VISITE,
                CategoriaContenuto.SOCIAL_POSTS
        );
    }

    /** Carica gli elementi della categoria. */
    public List<ElementoPiattaformaDto> getContenutiCategoria(CategoriaContenuto cat) {
        try (Connection c = DatabaseManager.getConnection()) {
            switch (cat) {
                case UTENTI -> {
                    String q = "SELECT username, ruolo, fondi, nome, cognome FROM utenti";
                    try (PreparedStatement ps = c.prepareStatement(q);
                         ResultSet rs = ps.executeQuery()) {
                        List<ElementoPiattaformaDto> out = new ArrayList<>();
                        while (rs.next()) {
                            String id   = rs.getString("username");
                            String nome = rs.getString("nome") + " " + rs.getString("cognome");
                            String extra= "Fondi: " + rs.getDouble("fondi");
                            out.add(new ElementoPiattaformaDto(id, nome, "Utente",
                                    rs.getString("ruolo"), null, extra));
                        }
                        return out;
                    }
                }
                case PRODOTTI -> {
                    String q = "SELECT id, nome, stato, prezzo, indirizzo FROM prodotti";
                    return fetchSimple(c, q, "Prodotto");
                }
                case PACCHETTI -> {
                    String q = "SELECT id, nome, stato, prezzo_totale AS prezzo, indirizzo FROM pacchetti";
                    return fetchSimple(c, q, "Pacchetto");
                }
                case PRODOTTI_TRASFORMATI -> {
                    String q = "SELECT id, nome, stato, prezzo, indirizzo FROM prodotti_trasformati";
                    return fetchSimple(c, q, "Prodotto trasformato");
                }
                case FIERE -> {
                    String q = """
                            SELECT id, descrizione, stato, data_inizio, organizzatore
                            FROM fiere
                            """;
                    try (PreparedStatement ps = c.prepareStatement(q);
                         ResultSet rs = ps.executeQuery()) {
                        List<ElementoPiattaformaDto> out = new ArrayList<>();
                        while (rs.next()) {
                            out.add(new ElementoPiattaformaDto(
                                    String.valueOf(rs.getLong("id")),
                                    trunca(rs.getString("descrizione")),
                                    "Fiera",
                                    rs.getString("stato"),
                                    rs.getTimestamp("data_inizio"),
                                    "Org: " + Objects.toString(rs.getString("organizzatore"), "-")
                            ));
                        }
                        return out;
                    }
                }
                case VISITE_INVITO -> {
                    String q = """
                            SELECT id, descrizione, stato, data_inizio, organizzatore
                            FROM visite_invito
                            """;
                    try (PreparedStatement ps = c.prepareStatement(q);
                         ResultSet rs = ps.executeQuery()) {
                        List<ElementoPiattaformaDto> out = new ArrayList<>();
                        while (rs.next()) {
                            out.add(new ElementoPiattaformaDto(
                                    String.valueOf(rs.getLong("id")),
                                    trunca(rs.getString("descrizione")),
                                    "Visita su invito",
                                    rs.getString("stato"),
                                    rs.getTimestamp("data_inizio"),
                                    "Org: " + Objects.toString(rs.getString("organizzatore"), "-")
                            ));
                        }
                        return out;
                    }
                }
                case ACQUISTI -> {
                    String q = """
                            SELECT id, username_acquirente, stato_pagamento, totale, data_ora
                            FROM acquisti
                            """;
                    try (PreparedStatement ps = c.prepareStatement(q);
                         ResultSet rs = ps.executeQuery()) {
                        List<ElementoPiattaformaDto> out = new ArrayList<>();
                        while (rs.next()) {
                            out.add(new ElementoPiattaformaDto(
                                    String.valueOf(rs.getLong("id")),
                                    "Acquirente: " + rs.getString("username_acquirente"),
                                    "Acquisto",
                                    rs.getString("stato_pagamento"),
                                    rs.getTimestamp("data_ora"),
                                    "Totale: " + rs.getDouble("totale")
                            ));
                        }
                        return out;
                    }
                }
                case PRENOTAZIONI_FIERE -> {
                    String q = """
                            SELECT id, username_acquirente, numero_persone, data_prenotazione
                            FROM prenotazioni_fiere
                            """;
                    try (PreparedStatement ps = c.prepareStatement(q);
                         ResultSet rs = ps.executeQuery()) {
                        List<ElementoPiattaformaDto> out = new ArrayList<>();
                        while (rs.next()) {
                            out.add(new ElementoPiattaformaDto(
                                    String.valueOf(rs.getLong("id")),
                                    "Acquirente: " + rs.getString("username_acquirente"),
                                    "Prenotazione fiera",
                                    "-",
                                    rs.getTimestamp("data_prenotazione"),
                                    "Persone: " + rs.getInt("numero_persone")
                            ));
                        }
                        return out;
                    }
                }
                case PRENOTAZIONI_VISITE -> {
                    String q = """
                            SELECT id, username_venditore, numero_persone, data_prenotazione
                            FROM prenotazioni_visite
                            """;
                    try (PreparedStatement ps = c.prepareStatement(q);
                         ResultSet rs = ps.executeQuery()) {
                        List<ElementoPiattaformaDto> out = new ArrayList<>();
                        while (rs.next()) {
                            out.add(new ElementoPiattaformaDto(
                                    String.valueOf(rs.getLong("id")),
                                    "Venditore: " + rs.getString("username_venditore"),
                                    "Prenotazione visita",
                                    "-",
                                    rs.getTimestamp("data_prenotazione"),
                                    "Persone: " + rs.getInt("numero_persone")
                            ));
                        }
                        return out;
                    }
                }
                case SOCIAL_POSTS -> {
                    String q = """
                            SELECT id, titolo, testo, autore_username, created_at
                            FROM social_posts
                            ORDER BY created_at DESC
                            """;
                    try (PreparedStatement ps = c.prepareStatement(q);
                         ResultSet rs = ps.executeQuery()) {
                        List<ElementoPiattaformaDto> out = new ArrayList<>();
                        while (rs.next()) {
                            String titolo = rs.getString("titolo");
                            if (titolo == null || titolo.isBlank())
                                titolo = trunca(rs.getString("testo"));
                            out.add(new ElementoPiattaformaDto(
                                    String.valueOf(rs.getLong("id")),
                                    titolo,
                                    "Post",
                                    "Pubblicato",
                                    rs.getTimestamp("created_at"),
                                    "Autore: " + rs.getString("autore_username")
                            ));
                        }
                        return out;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return List.of();
    }

    /** Filtra/ordina in memoria la lista corrente secondo i criteri. */
    public List<ElementoPiattaformaDto> filtraOrdinaLista(List<ElementoPiattaformaDto> src, CriteriRicerca c) {
        if (src == null) return List.of();
        var stream = src.stream();

        if (c != null) {
            if (c.testo != null && !c.testo.isBlank()) {
                final String t = c.testo.toLowerCase();
                stream = stream.filter(e ->
                        (e.getNome() != null && e.getNome().toLowerCase().contains(t)) ||
                                (e.getExtra() != null && e.getExtra().toLowerCase().contains(t))
                );
            }
            if (c.stato != null && !c.stato.isBlank() && !"Tutti".equalsIgnoreCase(c.stato)) {
                final String s = c.stato.toLowerCase();
                stream = stream.filter(e -> e.getStato() != null && e.getStato().toLowerCase().contains(s));
            }
        }

        Comparator<ElementoPiattaformaDto> cmp = Comparator.comparing(ElementoPiattaformaDto::getId);
        if (c != null && c.orderBy != null) {
            switch (c.orderBy) {
                case "DATA" -> cmp = Comparator.comparing(ElementoPiattaformaDto::getData,
                        Comparator.nullsLast(Comparator.naturalOrder()));
                case "NOME" -> cmp = Comparator.comparing(ElementoPiattaformaDto::getNome,
                        Comparator.nullsLast(String::compareToIgnoreCase));
                case "STATO" -> cmp = Comparator.comparing(ElementoPiattaformaDto::getStato,
                        Comparator.nullsLast(String::compareToIgnoreCase));
            }
        }
        if (c == null || c.asc) {
            return stream.sorted(cmp).collect(Collectors.toList());
        } else {
            return stream.sorted(cmp.reversed()).collect(Collectors.toList());
        }
    }

    /** Stati disponibili per la combo, per dare un minimo di UX nei filtri. */
    public String[] getPossibiliStati(CategoriaContenuto cat) {
        return switch (cat) {
            case PRODOTTI, PACCHETTI, PRODOTTI_TRASFORMATI -> new String[]{"Tutti", "IN_ATTESA", "APPROVATO", "RIFIUTATO"};
            case FIERE, VISITE_INVITO -> new String[]{"Tutti", "IN_PREPARAZIONE", "APERTO", "CONCLUSO"};
            case ACQUISTI -> new String[]{"Tutti", "PAGATO", "FALLITO", "ANNULLATO"};
            default -> new String[]{"Tutti"};
        };
    }

    // ---- helpers ----
    private static List<ElementoPiattaformaDto> fetchSimple(Connection c, String q, String tipo) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(q);
             ResultSet rs = ps.executeQuery()) {
            List<ElementoPiattaformaDto> out = new ArrayList<>();
            while (rs.next()) {
                String extra = "Prezzo: " + rs.getDouble("prezzo");
                String indirizzo = rs.getString("indirizzo");
                if (indirizzo != null && !indirizzo.isBlank()) extra += " | " + indirizzo;
                out.add(new ElementoPiattaformaDto(
                        String.valueOf(rs.getLong("id")),
                        rs.getString("nome"),
                        tipo,
                        rs.getString("stato"),
                        null,
                        extra
                ));
            }
            return out;
        }
    }

    private static String trunca(String s){
        if (s == null) return "";
        return s.length() > 60 ? s.substring(0, 57) + "..." : s;
    }
}
