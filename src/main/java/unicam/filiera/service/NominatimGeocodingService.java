package unicam.filiera.service;

import unicam.filiera.model.GeocodedAddress;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class NominatimGeocodingService {
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

    public Optional<GeocodedAddress> geocode(String address) {
        try {
            String q = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = NOMINATIM_URL + "?format=json&limit=1&q=" + q;
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("User-Agent", "FilieraApp/1.0 (torquati79@yahoo.it)");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            try (InputStream in = conn.getInputStream()) {
                String body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                int latIdx = body.indexOf("\"lat\":\"");
                int lonIdx = body.indexOf("\"lon\":\"");
                if (latIdx < 0 || lonIdx < 0) return Optional.empty();
                int latEnd = body.indexOf('"', latIdx + 7);
                int lonEnd = body.indexOf('"', lonIdx + 7);
                double lat = Double.parseDouble(body.substring(latIdx + 7, latEnd));
                double lon = Double.parseDouble(body.substring(lonIdx + 7, lonEnd));
                return Optional.of(new GeocodedAddress(address, lat, lon));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
