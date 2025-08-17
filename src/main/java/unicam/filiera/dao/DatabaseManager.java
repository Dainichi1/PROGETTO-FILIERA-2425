package unicam.filiera.dao;

import java.sql.*;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:h2:./data/filiera;DB_CLOSE_DELAY=-1";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, "sa", "");
    }

    /**
     * Verifica e migra lo schema quando serve.
     */
    public static void checkAndUpdateDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            // ---- prodotti.indirizzo ----
            ResultSet rs1 = conn.getMetaData().getColumns(null, null, "PRODOTTI", "INDIRIZZO");
            if (!rs1.next()) {
                System.out.println("[DB] Aggiungo colonna 'indirizzo' a prodotti");
                stmt.executeUpdate("ALTER TABLE prodotti ADD COLUMN indirizzo VARCHAR(255) DEFAULT NULL");
            }

            // ---- fiere.organizzatore ----
            ResultSet rs2 = conn.getMetaData().getColumns(null, null, "FIERE", "ORGANIZZATORE");
            if (!rs2.next()) {
                System.out.println("[DB] Aggiungo colonna 'organizzatore' a fiere");
                stmt.executeUpdate("ALTER TABLE fiere ADD COLUMN organizzatore VARCHAR(50) DEFAULT NULL");
            }

            // ---- visite_invito (se manca) ----
            ResultSet rs3 = conn.getMetaData().getTables(null, null, "VISITE_INVITO", new String[]{"TABLE"});
            if (!rs3.next()) {
                System.out.println("[DB] Creo tabella 'visite_invito'");
                stmt.executeUpdate("""
                            CREATE TABLE visite_invito (
                              id IDENTITY PRIMARY KEY,
                              data_inizio TIMESTAMP,
                              data_fine   TIMESTAMP,
                              prezzo      DOUBLE,
                              descrizione VARCHAR(500),
                              indirizzo   VARCHAR(255),
                              numero_min_partecipanti INT,
                              organizzatore VARCHAR(50),
                              destinatari TEXT,
                              stato VARCHAR(20) DEFAULT 'IN_PREPARAZIONE'
                            );
                        """);
            }

            // ---- utenti.fondi ----
            ResultSet rs4 = conn.getMetaData().getColumns(null, null, "UTENTI", "FONDI");
            if (!rs4.next()) {
                System.out.println("[DB] Aggiungo colonna 'fondi' a utenti");
                stmt.executeUpdate("ALTER TABLE utenti ADD COLUMN fondi DOUBLE DEFAULT 0.0");
            }

            // ---- pacchetti.quantita ----
            ResultSet rsPacchettoQta = conn.getMetaData().getColumns(null, null, "PACCHETTI", "QUANTITA");
            if (!rsPacchettoQta.next()) {
                System.out.println("[DB] Aggiungo colonna 'quantita' a pacchetti");
                stmt.executeUpdate("ALTER TABLE pacchetti ADD COLUMN quantita INT DEFAULT 0");
            }

            // ---- social_posts (se manca) ----
            ResultSet rsSocial = conn.getMetaData().getTables(null, null, "SOCIAL_POSTS", new String[]{"TABLE"});
            if (!rsSocial.next()) {
                System.out.println("[DB] Creo tabella 'social_posts'");
                stmt.executeUpdate("""
                            CREATE TABLE social_posts (
                              id IDENTITY PRIMARY KEY,
                              autore_username VARCHAR(50) NOT NULL,
                              id_acquisto INT,
                              nome_item VARCHAR(100) NOT NULL,
                              tipo_item VARCHAR(30) NOT NULL,
                              titolo VARCHAR(100),
                              testo VARCHAR(1000) NOT NULL,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (autore_username) REFERENCES utenti(username),
                              FOREIGN KEY (id_acquisto) REFERENCES acquisti(id)
                            );
                        """);
            }

            // ---- acquisti (se manca) ----
            ResultSet rsAcquisti = conn.getMetaData().getTables(null, null, "ACQUISTI", new String[]{"TABLE"});
            if (!rsAcquisti.next()) {
                System.out.println("[DB] Creo tabella 'acquisti'");
                stmt.executeUpdate("""
                            CREATE TABLE acquisti (
                                id IDENTITY PRIMARY KEY,
                                username_acquirente VARCHAR(50) NOT NULL,
                                totale DOUBLE NOT NULL,
                                stato_pagamento VARCHAR(20) NOT NULL,
                                tipo_metodo_pagamento VARCHAR(30) NOT NULL,
                                data_ora TIMESTAMP NOT NULL,
                                fondi_pre_acquisto DOUBLE,
                                fondi_post_acquisto DOUBLE,
                                elenco_item TEXT,
                                FOREIGN KEY (username_acquirente) REFERENCES utenti(username)
                            );
                        """);
            }

            // ---- acquisto_items (se manca) ----
            ResultSet rsAcquistoItems = conn.getMetaData().getTables(null, null, "ACQUISTO_ITEMS", new String[]{"TABLE"});
            if (!rsAcquistoItems.next()) {
                System.out.println("[DB] Creo tabella 'acquisto_items'");
                stmt.executeUpdate("""
                            CREATE TABLE acquisto_items (
                                id_acquisto INT,
                                nome_item VARCHAR(100),
                                tipo_item VARCHAR(30),
                                quantita INT,
                                prezzo_unitario DOUBLE,
                                totale DOUBLE,
                                FOREIGN KEY (id_acquisto) REFERENCES acquisti(id)
                            );
                        """);
            }

            // ---- prenotazioni_fiere (se manca) ----
            ResultSet rsPrenFiere = conn.getMetaData().getTables(null, null, "PRENOTAZIONI_FIERE", new String[]{"TABLE"});
            if (!rsPrenFiere.next()) {
                System.out.println("[DB] Creo tabella 'prenotazioni_fiere'");
                stmt.executeUpdate("""
                            CREATE TABLE prenotazioni_fiere (
                                id IDENTITY PRIMARY KEY,
                                id_fiera BIGINT NOT NULL,
                                username_acquirente VARCHAR(50) NOT NULL,
                                numero_persone INT NOT NULL,
                                data_prenotazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                FOREIGN KEY (id_fiera) REFERENCES fiere(id),
                                FOREIGN KEY (username_acquirente) REFERENCES utenti(username)
                            );
                        """);
            }

            // ---- prenotazioni_visite (se manca) ----
            ResultSet rsPrenVisite = conn.getMetaData().getTables(null, null, "PRENOTAZIONI_VISITE", new String[]{"TABLE"});
            if (!rsPrenVisite.next()) {
                System.out.println("[DB] Creo tabella 'prenotazioni_visite'");
                stmt.executeUpdate("""
                            CREATE TABLE prenotazioni_visite (
                                id IDENTITY PRIMARY KEY,
                                id_visita BIGINT NOT NULL,
                                username_venditore VARCHAR(50) NOT NULL,
                                numero_persone INT NOT NULL,
                                data_prenotazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                FOREIGN KEY (id_visita) REFERENCES visite_invito(id),
                                FOREIGN KEY (username_venditore) REFERENCES utenti(username)
                            );
                        """);
            }

            // ---- prodotti_trasformati (se manca) ----
            ResultSet rsPT = conn.getMetaData().getTables(null, null, "PRODOTTI_TRASFORMATI", new String[]{"TABLE"});
            if (!rsPT.next()) {
                System.out.println("[DB] Creo tabella 'prodotti_trasformati'");
                stmt.executeUpdate("""
                            CREATE TABLE prodotti_trasformati (
                                id IDENTITY PRIMARY KEY,
                                nome VARCHAR(100),
                                descrizione VARCHAR(500),
                                quantita INT,
                                prezzo DOUBLE,
                                indirizzo VARCHAR(255),
                                certificati TEXT,
                                foto TEXT,
                                creato_da VARCHAR(50),
                                stato VARCHAR(20) DEFAULT 'IN_ATTESA',
                                commento VARCHAR(255) DEFAULT NULL
                            );
                        """);
            }

            // ---- fasi_produzione (se manca) ----
            ResultSet rsFP = conn.getMetaData().getTables(null, null, "FASI_PRODUZIONE", new String[]{"TABLE"});
            if (!rsFP.next()) {
                System.out.println("[DB] Creo tabella 'fasi_produzione'");
                stmt.executeUpdate("""
                            CREATE TABLE fasi_produzione (
                                id IDENTITY PRIMARY KEY,
                                prodotto_trasformato_id INT NOT NULL,
                                descrizione VARCHAR(255),
                                produttore VARCHAR(50),
                                prodotto_origine VARCHAR(100),
                                FOREIGN KEY (prodotto_trasformato_id) REFERENCES prodotti_trasformati(id)
                            );
                        """);
            }

            // ===== richieste_eliminazione_profilo (storico, senza FK) =====
            ResultSet rsREP = conn.getMetaData()
                    .getTables(null, null, "RICHIESTE_ELIMINAZIONE_PROFILO", new String[]{"TABLE"});
            if (!rsREP.next()) {
                System.out.println("[DB] Creo tabella 'richieste_eliminazione_profilo' (senza FK)");
                stmt.executeUpdate("""
                            CREATE TABLE richieste_eliminazione_profilo (
                                id IDENTITY PRIMARY KEY,
                                username VARCHAR(50) NOT NULL,
                                stato VARCHAR(20) NOT NULL,
                                data_richiesta TIMESTAMP NOT NULL
                            );
                        """);
            } else {
                // Migrazione: rimuovi eventuale FK esistente sull'username
                try (Statement s2 = conn.createStatement()) {
                    ResultSet fks = conn.getMetaData().getImportedKeys(null, null, "RICHIESTE_ELIMINAZIONE_PROFILO");
                    while (fks.next()) {
                        String fkName = fks.getString("FK_NAME");
                        if (fkName != null && !fkName.isBlank()) {
                            try {
                                s2.executeUpdate("ALTER TABLE richieste_eliminazione_profilo DROP CONSTRAINT " + fkName);
                                System.out.println("[DB] Droppato vincolo " + fkName + " su richieste_eliminazione_profilo");
                            } catch (SQLException ignore) {
                            }
                        }
                    }
                }
                // Rimuovi eventuale colonna obsoleta
                ResultSet rsColId = conn.getMetaData()
                        .getColumns(null, null, "RICHIESTE_ELIMINAZIONE_PROFILO", "UTENTE_ID");
                if (rsColId.next()) {
                    System.out.println("[DB] Rimuovo colonna obsoleta 'utente_id'");
                    stmt.executeUpdate("ALTER TABLE richieste_eliminazione_profilo DROP COLUMN utente_id");
                }

                ResultSet rsMarkers = conn.getMetaData().getTables(null, null, "MARKERS", new String[]{"TABLE"});
                if (!rsMarkers.next()) {
                    System.out.println("[DB] Creo tabella 'markers'");
                    stmt.executeUpdate("""
                                CREATE TABLE markers (
                                    id IDENTITY PRIMARY KEY,
                                    lat DOUBLE,
                                    lon DOUBLE,
                                    label VARCHAR(255),
                                    color VARCHAR(16) -- es: #ffbb00
                                );
                            """);
                }


            }

            // Indici utili
            try (Statement s3 = conn.createStatement()) {
                s3.executeUpdate("CREATE INDEX IF NOT EXISTS idx_rep_username ON richieste_eliminazione_profilo(username)");
                s3.executeUpdate("CREATE INDEX IF NOT EXISTS idx_rep_stato ON richieste_eliminazione_profilo(stato)");
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creazione tabelle base (bootstrap).
     */
    public static void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            String utentiSql = """
                        CREATE TABLE IF NOT EXISTS utenti (
                            username VARCHAR(50) PRIMARY KEY,
                            password VARCHAR(50),
                            nome VARCHAR(50),
                            cognome VARCHAR(50),
                            ruolo VARCHAR(30),
                            fondi DOUBLE DEFAULT 0.0
                        );
                    """;

            String prodottiSql = """
                        CREATE TABLE IF NOT EXISTS prodotti (
                            id IDENTITY PRIMARY KEY,
                            nome VARCHAR(100),
                            descrizione VARCHAR(500),
                            quantita INT,
                            prezzo DOUBLE,
                            indirizzo VARCHAR(255),
                            certificati TEXT,
                            foto TEXT,
                            creato_da VARCHAR(50),
                            stato VARCHAR(20) DEFAULT 'IN_ATTESA',
                            commento VARCHAR(255) DEFAULT NULL
                        );
                    """;

            String pacchettiSql = """
                        CREATE TABLE IF NOT EXISTS pacchetti (
                            id IDENTITY PRIMARY KEY,
                            nome VARCHAR(100),
                            descrizione VARCHAR(500),
                            indirizzo VARCHAR(255),
                            prezzo_totale DOUBLE,
                            prodotti TEXT,
                            certificati TEXT,
                            foto TEXT,
                            creato_da VARCHAR(50),
                            stato VARCHAR(20) DEFAULT 'IN_ATTESA',
                            commento VARCHAR(255) DEFAULT NULL,
                            quantita INT DEFAULT 0
                        );
                    """;

            String fiereSql = """
                        CREATE TABLE IF NOT EXISTS fiere (
                            id IDENTITY PRIMARY KEY,
                            data_inizio TIMESTAMP,
                            data_fine   TIMESTAMP,
                            prezzo      DOUBLE,
                            descrizione VARCHAR(500),
                            indirizzo   VARCHAR(255),
                            numero_min_partecipanti INT,
                            organizzatore VARCHAR(50),
                            stato VARCHAR(20) DEFAULT 'IN_PREPARAZIONE'
                        );
                    """;

            String visiteSql = """
                        CREATE TABLE IF NOT EXISTS visite_invito (
                            id IDENTITY PRIMARY KEY,
                            data_inizio TIMESTAMP,
                            data_fine   TIMESTAMP,
                            prezzo      DOUBLE,
                            descrizione VARCHAR(500),
                            indirizzo   VARCHAR(255),
                            numero_min_partecipanti INT,
                            organizzatore VARCHAR(50),
                            destinatari TEXT,
                            stato VARCHAR(20) DEFAULT 'IN_PREPARAZIONE'
                        );
                    """;

            String acquistiSql = """
                        CREATE TABLE IF NOT EXISTS acquisti (
                            id IDENTITY PRIMARY KEY,
                            username_acquirente VARCHAR(50) NOT NULL,
                            totale DOUBLE NOT NULL,
                            stato_pagamento VARCHAR(20) NOT NULL,
                            tipo_metodo_pagamento VARCHAR(30) NOT NULL,
                            data_ora TIMESTAMP NOT NULL,
                            fondi_pre_acquisto DOUBLE,
                            fondi_post_acquisto DOUBLE,
                            elenco_item TEXT,
                            FOREIGN KEY (username_acquirente) REFERENCES utenti(username)
                        );
                    """;

            String acquistoItemsSql = """
                        CREATE TABLE IF NOT EXISTS acquisto_items (
                            id_acquisto INT,
                            nome_item VARCHAR(100),
                            tipo_item VARCHAR(30),
                            quantita INT,
                            prezzo_unitario DOUBLE,
                            totale DOUBLE,
                            FOREIGN KEY (id_acquisto) REFERENCES acquisti(id)
                        );
                    """;

            String prenotazioniFiereSql = """
                        CREATE TABLE IF NOT EXISTS prenotazioni_fiere (
                            id IDENTITY PRIMARY KEY,
                            id_fiera BIGINT NOT NULL,
                            username_acquirente VARCHAR(50) NOT NULL,
                            numero_persone INT NOT NULL,
                            data_prenotazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (id_fiera) REFERENCES fiere(id),
                            FOREIGN KEY (username_acquirente) REFERENCES utenti(username)
                        );
                    """;

            String prenotazioniVisiteSql = """
                        CREATE TABLE IF NOT EXISTS prenotazioni_visite (
                            id IDENTITY PRIMARY KEY,
                            id_visita BIGINT NOT NULL,
                            username_venditore VARCHAR(50) NOT NULL,
                            numero_persone INT NOT NULL,
                            data_prenotazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (id_visita) REFERENCES visite_invito(id),
                            FOREIGN KEY (username_venditore) REFERENCES utenti(username)
                        );
                    """;

            String prodottiTrasformatiSql = """
                        CREATE TABLE IF NOT EXISTS prodotti_trasformati (
                            id IDENTITY PRIMARY KEY,
                            nome VARCHAR(100),
                            descrizione VARCHAR(500),
                            quantita INT,
                            prezzo DOUBLE,
                            indirizzo VARCHAR(255),
                            certificati TEXT,
                            foto TEXT,
                            creato_da VARCHAR(50),
                            stato VARCHAR(20) DEFAULT 'IN_ATTESA',
                            commento VARCHAR(255) DEFAULT NULL
                        );
                    """;

            String fasiProduzioneSql = """
                        CREATE TABLE IF NOT EXISTS fasi_produzione (
                            id IDENTITY PRIMARY KEY,
                            prodotto_trasformato_id INT NOT NULL,
                            descrizione VARCHAR(255),
                            produttore VARCHAR(50),
                            prodotto_origine VARCHAR(100),
                            FOREIGN KEY (prodotto_trasformato_id) REFERENCES prodotti_trasformati(id)
                        );
                    """;

            // >>> storico richieste: nessun FK su username <<<
            String richiesteEliminaProfiloSql = """
                        CREATE TABLE IF NOT EXISTS richieste_eliminazione_profilo (
                            id IDENTITY PRIMARY KEY,
                            username VARCHAR(50) NOT NULL,
                            stato VARCHAR(20) NOT NULL,
                            data_richiesta TIMESTAMP NOT NULL
                        );
                    """;

            String socialPostsSql = """
                        CREATE TABLE IF NOT EXISTS social_posts (
                          id IDENTITY PRIMARY KEY,
                          autore_username VARCHAR(50) NOT NULL,
                          id_acquisto INT,
                          nome_item VARCHAR(100) NOT NULL,
                          tipo_item VARCHAR(30) NOT NULL,
                          titolo VARCHAR(100),
                          testo VARCHAR(1000) NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (autore_username) REFERENCES utenti(username),
                          FOREIGN KEY (id_acquisto) REFERENCES acquisti(id)
                        );
                    """;

            String markersSql = """
                        CREATE TABLE IF NOT EXISTS markers (
                            id IDENTITY PRIMARY KEY,
                            lat DOUBLE,
                            lon DOUBLE,
                            label VARCHAR(255),
                            color VARCHAR(16) 
                        );
                    """;

            stmt.executeUpdate(markersSql);
            stmt.executeUpdate(utentiSql);
            stmt.executeUpdate(prodottiSql);
            stmt.executeUpdate(pacchettiSql);
            stmt.executeUpdate(fiereSql);
            stmt.executeUpdate(visiteSql);
            stmt.executeUpdate(acquistiSql);
            stmt.executeUpdate(acquistoItemsSql);
            stmt.executeUpdate(prenotazioniFiereSql);
            stmt.executeUpdate(prenotazioniVisiteSql);
            stmt.executeUpdate(prodottiTrasformatiSql);
            stmt.executeUpdate(fasiProduzioneSql);
            stmt.executeUpdate(richiesteEliminaProfiloSql);
            stmt.executeUpdate(socialPostsSql);

            // indici per richieste (se DB appena creato)
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_rep_username ON richieste_eliminazione_profilo(username)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_rep_stato ON richieste_eliminazione_profilo(stato)");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
