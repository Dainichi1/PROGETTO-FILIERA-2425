package unicam.filiera;

import unicam.filiera.dao.DatabaseManager;
import unicam.filiera.dao.JdbcUtenteDAO;
import unicam.filiera.dao.JdbcProdottoDAO;
import unicam.filiera.dao.JdbcPacchettoDAO;
import unicam.filiera.model.UtenteAutenticato;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.Pacchetto;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // 1) Inizializza il database: crea tabelle e aggiorna struttura se necessario
        DatabaseManager.initDatabase();
        DatabaseManager.checkAndUpdateDatabase();

        // 2) Stampa utenti
        System.out.println("Utenti registrati nel sistema:");
        for (UtenteAutenticato u : JdbcUtenteDAO.getInstance().findAll()) {
            System.out.println("- " + u);
        }

        // 3) Stampa prodotti
        System.out.println("\nProdotti registrati nel sistema:");
        for (Prodotto p : JdbcProdottoDAO.getInstance().findAll()) {
            System.out.println(p);
        }

        // 4) Stampa pacchetti
        System.out.println("\nPacchetti registrati nel sistema:");
        for (Pacchetto pk : JdbcPacchettoDAO.getInstance().findAll()) {
            System.out.println(pk);
        }

        // 5) Avvia la GUI
        SwingUtilities.invokeLater(() -> {
            unicam.filiera.view.MainWindow window = new unicam.filiera.view.MainWindow();
            window.setVisible(true);
        });
    }
}
