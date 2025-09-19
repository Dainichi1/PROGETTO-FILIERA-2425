package unicam.filiera.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ItemTipo {
    PRODOTTO,
    PACCHETTO,
    TRASFORMATO;

    @JsonCreator
    public static ItemTipo fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("ItemTipo nullo");
        }
        return ItemTipo.valueOf(value.trim().toUpperCase());
    }
}
