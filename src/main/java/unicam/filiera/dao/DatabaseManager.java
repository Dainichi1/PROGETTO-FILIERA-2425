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
                            ruolo VARCHAR(30)
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
                            commento VARCHAR(255) DEFAULT NULL
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

            stmt.executeUpdate(utentiSql);
            stmt.executeUpdate(prodottiSql);
            stmt.executeUpdate(pacchettiSql);
            stmt.executeUpdate(fiereSql);
            stmt.executeUpdate(visiteSql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
