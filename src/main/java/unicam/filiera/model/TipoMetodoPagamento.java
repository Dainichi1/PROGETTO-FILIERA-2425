package unicam.filiera.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoMetodoPagamento {
    CARTA_DI_CREDITO,
    BONIFICO,
    PAGAMENTO_ALLA_CONSEGNA;

    @JsonCreator
    public static TipoMetodoPagamento fromString(String value) {
        if (value == null) return null;
        String normalized = value.trim().toUpperCase().replace(" ", "_");
        System.out.println(">>> Mapping metodo pagamento ricevuto: " + normalized); // DEBUG

        return switch (normalized) {
            case "CARTA_DI_CREDITO" -> CARTA_DI_CREDITO;
            case "BONIFICO" -> BONIFICO;
            case "PAGAMENTO_ALLA_CONSEGNA" -> PAGAMENTO_ALLA_CONSEGNA;
            default -> throw new IllegalArgumentException("Metodo pagamento non valido: " + value);
        };
    }

    @JsonValue
    public String toJson() {
        return this.name();
    }
}
