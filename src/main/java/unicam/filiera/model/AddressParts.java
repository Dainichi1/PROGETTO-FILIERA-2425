package unicam.filiera.model;

/**
 * Rappresenta un indirizzo suddiviso in parti (via, civico, CAP, città, provincia, paese).
 * Utile per normalizzare gli indirizzi prima del geocoding.
 */
public final class AddressParts {

    private final String raw;        // Indirizzo completo grezzo
    private final String street;     // Via / Corso / Piazza
    private final String houseNumber;// Numero civico
    private final String postcode;   // CAP
    private final String city;       // Città
    private final String province;   // Provincia (sigla es. TO, AN)
    private final String region;     // Regione (opzionale)
    private final String country;    // Paese (default Italia)

    private AddressParts(Builder b) {
        this.raw = trimOrNull(b.raw);
        this.street = trimOrNull(b.street);
        this.houseNumber = trimOrNull(b.houseNumber);
        this.postcode = trimOrNull(b.postcode);
        this.city = trimOrNull(b.city);
        this.province = trimOrNull(b.province);
        this.region = trimOrNull(b.region);
        this.country = coalesce(b.country, "Italia"); // default Italia
    }

    /**
     * Genera una stringa indirizzo da usare nel geocoding.
     * Priorità:
     * 1) Se via + città → costruisco un indirizzo pulito
     * 2) Altrimenti uso il raw
     * 3) Fallback: solo paese
     */
    public String toQueryString() {
        boolean hasStreet = street != null && !street.isBlank();
        boolean hasCity = city != null && !city.isBlank();

        if (hasStreet || hasCity) {
            String via = join(" ", street, houseNumber); // es. "Via Roma 10"
            return join(", ",
                    via,
                    join(" ", postcode, city, province),
                    join(" ", region),
                    country
            );
        }

        if (raw != null && !raw.isBlank()) {
            return raw + ", " + country;
        }

        return country; // ultima difesa
    }

    /**
     * Controlla se ci sono informazioni minime per tentare un geocoding.
     */
    public boolean looksGeocodable() {
        boolean hasStreet = street != null && !street.isBlank();
        boolean hasCity = city != null && !city.isBlank();
        return (raw != null && !raw.isBlank()) || hasStreet || hasCity;
    }

    // === Utility interne ===
    private static String trimOrNull(String s) {
        return (s == null) ? null : s.trim();
    }

    private static String coalesce(String v, String def) {
        return (v == null || v.isBlank()) ? def : v;
    }

    private static String join(String sep, String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p == null || p.isBlank()) continue;
            if (sb.length() > 0) sb.append(sep);
            sb.append(p.trim());
        }
        return sb.toString();
    }

    // === Builder ===
    public static class Builder {
        private String raw, street, houseNumber, postcode, city, province, region, country;

        public Builder raw(String v)        { this.raw = v; return this; }
        public Builder street(String v)     { this.street = v; return this; }
        public Builder houseNumber(String v){ this.houseNumber = v; return this; }
        public Builder postcode(String v)   { this.postcode = v; return this; }
        public Builder city(String v)       { this.city = v; return this; }
        public Builder province(String v)   { this.province = v; return this; }
        public Builder region(String v)     { this.region = v; return this; }
        public Builder country(String v)    { this.country = v; return this; }

        public AddressParts build() {
            return new AddressParts(this);
        }
    }
}
