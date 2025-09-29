package unicam.filiera.factory;

import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.model.Pacchetto;

public final class PacchettoFactory {
    private PacchettoFactory() { }

    public static Pacchetto creaPacchetto(PacchettoDto dto, String creatore) {
        return (Pacchetto) ItemFactory.creaItem(dto, creatore);
    }
}
