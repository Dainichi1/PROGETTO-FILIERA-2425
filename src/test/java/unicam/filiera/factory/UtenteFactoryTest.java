package unicam.filiera.factory;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import unicam.filiera.model.*;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class UtenteFactoryTest {

    static Stream<Object[]> datiRuoli() {
        return Stream.of(
                new Object[]{"prod1", "Mario", "Rossi", Ruolo.PRODUTTORE, Produttore.class},
                new Object[]{"acq1", "Luca", "Bianchi", Ruolo.ACQUIRENTE, Acquirente.class},
                new Object[]{"cur1", "Giulia", "Verdi", Ruolo.CURATORE, Curatore.class},
                new Object[]{"ani1", "Paolo", "Neri", Ruolo.ANIMATORE, Animatore.class},
                new Object[]{"tra1", "Sara", "Blu", Ruolo.TRASFORMATORE, Trasformatore.class},
                new Object[]{"gest1", "Franco", "Viola", Ruolo.GESTORE_PIATTAFORMA, GestorePiattaforma.class},
                new Object[]{"dist1", "Anna", "Gialli", Ruolo.DISTRIBUTORE_TIPICITA, DistributoreTipicita.class}
        );
    }

    @ParameterizedTest
    @MethodSource("datiRuoli")
    void testCreaAttorePerOgniRuolo(String username, String nome, String cognome,
                                    Ruolo ruolo, Class<?> expectedClass) {

        double fondi = ruolo == Ruolo.ACQUIRENTE ? 50.0 : 0.0;

        Utente u = UtenteFactory.creaAttore(
                username, "pwd", nome, cognome, ruolo, fondi
        );

        // Verifica la classe
        assertThat(u).isInstanceOf(expectedClass);

        // Verifica attributi base
        assertThat(u.getUsername()).isEqualTo(username);
        assertThat(u.getNome()).isEqualTo(nome);
        assertThat(u.getCognome()).isEqualTo(cognome);

        // Solo Acquirente deve avere i fondi
        if (ruolo == Ruolo.ACQUIRENTE) {
            assertThat(((Acquirente) u).getFondi()).isEqualTo(50.0);
        }
    }
}