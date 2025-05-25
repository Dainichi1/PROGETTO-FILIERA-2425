package unicam.filiera.dao;

import java.sql.*;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:h2:./data/filiera;DB_CLOSE_DELAY=-1";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, "sa", "");
    }

    /**
     * Controlla e aggiorna il database:
     * - Aggiunge colonne mancanti in prodotti e fiere
     * - Crea la tabella visite_invito se non esiste
     */
    public static void checkAndUpdateDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // ** prodotti.indirizzo **
            ResultSet rs1 = conn.getMetaData()
                    .getColumns(null, null, "PRODOTTI", "INDIRIZZO");
            if (!rs1.next()) {
                System.out.println("[DB] Aggiungo colonna 'indirizzo' a prodotti");
                stmt.executeUpdate(
                        "ALTER TABLE prodotti ADD COLUMN indirizzo VARCHAR(255) DEFAULT NULL"
                );
            }

            // ** fiere.organizzatore **
            ResultSet rs2 = conn.getMetaData()
                    .getColumns(null, null, "FIERE", "ORGANIZZATORE");
            if (!rs2.next()) {
                System.out.println("[DB] Aggiungo colonna 'organizzatore' a fiere");
                stmt.executeUpdate(
                        "ALTER TABLE fiere ADD COLUMN organizzatore VARCHAR(50) DEFAULT NULL"
                );
            }

            // ** crea la tabella visite_invito se non esiste **
            ResultSet rs3 = conn.getMetaData()
                    .getTables(null, null, "VISITE_INVITO", new String[]{"TABLE"});
            if (!rs3.next()) {
                System.out.println("[DB] Creo tabella 'visite_invito'");
                stmt.executeUpdate(
                        """
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
                                """
                );
            }
            // ** utenti.fondi **
            ResultSet rs4 = conn.getMetaData()
                    .getColumns(null, null, "UTENTI", "FONDI");
            if (!rs4.next()) {
                System.out.println("[DB] Aggiungo colonna 'fondi' a utenti");
                stmt.executeUpdate(
                        "ALTER TABLE utenti ADD COLUMN fondi DOUBLE DEFAULT 0.0"
                );
            }

            ResultSet rsPacchettoQta = conn.getMetaData()
                    .getColumns(null, null, "PACCHETTI", "QUANTITA");
            if (!rsPacchettoQta.next()) {
                System.out.println("[DB] Aggiungo colonna 'quantita' a pacchetti");
                stmt.executeUpdate("ALTER TABLE pacchetti ADD COLUMN quantita INT DEFAULT 0");
            }


            // ** crea la tabella acquisti se non esiste **
            ResultSet rsAcquisti = conn.getMetaData()
                    .getTables(null, null, "ACQUISTI", new String[]{"TABLE"});
            if (!rsAcquisti.next()) {
                System.out.println("[DB] Creo tabella 'acquisti'");
                stmt.executeUpdate(
                        """
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
                                """
                );
            }

// ** crea la tabella acquisto_items se non esiste **
            ResultSet rsAcquistoItems = conn.getMetaData()
                    .getTables(null, null, "ACQUISTO_ITEMS", new String[]{"TABLE"});
            if (!rsAcquistoItems.next()) {
                System.out.println("[DB] Creo tabella 'acquisto_items'");
                stmt.executeUpdate(
                        """
                                CREATE TABLE acquisto_items (
                                    id_acquisto INT,
                                    nome_item VARCHAR(100),
                                    tipo_item VARCHAR(30),
                                    quantita INT,
                                    prezzo_unitario DOUBLE,
                                    totale DOUBLE,
                                    FOREIGN KEY (id_acquisto) REFERENCES acquisti(id)
                                );
                                """
                );
            }

            // ** crea la tabella prenotazioni_fiere se non esiste **
            ResultSet rsPrenotazioniFiere = conn.getMetaData()
                    .getTables(null, null, "PRENOTAZIONI_FIERE", new String[]{"TABLE"});
            if (!rsPrenotazioniFiere.next()) {
                System.out.println("[DB] Creo tabella 'prenotazioni_fiere'");
                stmt.executeUpdate(
                        """
                                CREATE TABLE prenotazioni_fiere (
                                    id IDENTITY PRIMARY KEY,
                                    id_fiera BIGINT NOT NULL,
                                    username_acquirente VARCHAR(50) NOT NULL,
                                    numero_persone INT NOT NULL,
                                    data_prenotazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    FOREIGN KEY (id_fiera) REFERENCES fiere(id),
                                    FOREIGN KEY (username_acquirente) REFERENCES utenti(username)
                                );
                                """
                );
            }

            // ** crea la tabella prenotazioni_visite se non esiste **
            ResultSet rsPrenotazioniVisite = conn.getMetaData()
                    .getTables(null, null, "PRENOTAZIONI_VISITE", new String[]{"TABLE"});
            if (!rsPrenotazioniVisite.next()) {
                System.out.println("[DB] Creo tabella 'prenotazioni_visite'");
                stmt.executeUpdate(
                        """
                                CREATE TABLE prenotazioni_visite (
                                    id IDENTITY PRIMARY KEY,
                                    id_visita BIGINT NOT NULL,
                                    username_venditore VARCHAR(50) NOT NULL,
                                    numero_persone INT NOT NULL,
                                    data_prenotazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    FOREIGN KEY (id_visita) REFERENCES visite_invito(id),
                                    FOREIGN KEY (username_venditore) REFERENCES utenti(username)
                                );
                                """
                );
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inizializza le tabelle del database se non esistono:
     * utenti, prodotti, pacchetti, fiere, visite_invito.
     */
    public static void initDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

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
                            elenco_item TEXT,   -- puoi serializzare in JSON o CSV
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
            stmt.executeUpdate(prenotazioniVisiteSql);


            stmt.executeUpdate(utentiSql);
            stmt.executeUpdate(prodottiSql);
            stmt.executeUpdate(pacchettiSql);
            stmt.executeUpdate(fiereSql);
            stmt.executeUpdate(visiteSql);
            stmt.executeUpdate(acquistiSql);
            stmt.executeUpdate(acquistoItemsSql);
            stmt.executeUpdate(prenotazioniFiereSql);
            stmt.executeUpdate(prenotazioniVisiteSql);


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
