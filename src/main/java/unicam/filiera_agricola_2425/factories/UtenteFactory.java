package unicam.filiera_agricola_2425.factories;

import unicam.filiera_agricola_2425.models.UtenteAutenticato;

public interface UtenteFactory {
    UtenteAutenticato creaUtente(String nome, String username, String password);
}
