package unicam.filiera.model;

import java.util.Objects;

public final class AddressParts {
    // opzionale: se nel DB hai già una stringa completa
    private final String raw;               // p.es. "Via Roma 10, 00184 Roma RM, Italia"

    // campi atomici (tutti opzionali, usa quelli che hai)
    private final String street;            // "Via Roma"
    private final String houseNumber;       // "10"
    private final String postcode;          // "00184"
    private final String city;              // "Roma"
    private final String province;          // "RM" (preferibile la sigla)
    private final String region;            // opzionale
    private final String country;           // "Italia" (consigliato metterlo sempre)

    private AddressParts(Builder b) {
        this.raw         = trimOrNull(b.raw);
        this.street      = trimOrNull(b.street);
        this.houseNumber = trimOrNull(b.houseNumber);
        this.postcode    = trimOrNull(b.postcode);
        this.city        = trimOrNull(b.city);
        this.province    = trimOrNull(b.province);
        this.region      = trimOrNull(b.region);
        this.country     = trimOrNull(b.country);
    }

    public String toQueryString() {
        if (raw != null && !raw.isBlank()) {
            return raw;
        }
        String via = join(" ", street, houseNumber); // "Via Roma 10"
        // "Via Roma 10, 00184 Roma RM, Italia"
        return join(", ",
                via,
                join(" ", postcode, city, province),
                coalesce(country, "Italia") // default utile
        );
    }

    /** Abbastanza informazioni per tentare la geocodifica? */
    public boolean looksGeocodable() {
        // almeno via+civico+comune oppure una raw string
        if (raw != null && !raw.isBlank()) return true;
        boolean hasStreet = street != null && !street.isBlank();
        boolean hasCity   = city != null && !city.isBlank();
        return hasStreet && hasCity;
    }

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

        public Builder raw(String v)          { this.raw = v; return this; }
        public Builder street(String v)       { this.street = v; return this; }
        public Builder houseNumber(String v)  { this.houseNumber = v; return this; }
        public Builder postcode(String v)     { this.postcode = v; return this; }
        public Builder city(String v)         { this.city = v; return this; }
        public Builder province(String v)     { this.province = v; return this; }
        public Builder region(String v)       { this.region = v; return this; }
        public Builder country(String v)      { this.country = v; return this; }
        public AddressParts build()           { return new AddressParts(this); }
    }
}
