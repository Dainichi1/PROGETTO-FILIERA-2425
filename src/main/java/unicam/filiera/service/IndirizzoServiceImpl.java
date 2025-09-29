package unicam.filiera.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import unicam.filiera.dto.*;
import unicam.filiera.model.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IndirizzoServiceImpl implements IndirizzoService {

    private final ProdottoService prodottoService;
    private final PacchettoService pacchettoService;
    private final FieraService fieraService;
    private final VisitaInvitoService visitaInvitoService;
    private final ProdottoTrasformatoService prodottoTrasformatoService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public IndirizzoServiceImpl(ProdottoService prodottoService,
                                PacchettoService pacchettoService,
                                FieraService fieraService,
                                VisitaInvitoService visitaInvitoService,
                                ProdottoTrasformatoService prodottoTrasformatoService) {
        this.prodottoService = prodottoService;
        this.pacchettoService = pacchettoService;
        this.fieraService = fieraService;
        this.visitaInvitoService = visitaInvitoService;
        this.prodottoTrasformatoService = prodottoTrasformatoService;
    }

    @Override
    public List<IndirizzoDto> getIndirizziDisponibili() {
        List<IndirizzoDto> result = new ArrayList<>();

        // Prodotti APPROVATI
        result.addAll(prodottoService.getProdottiByStato(StatoProdotto.APPROVATO).stream()
                .map(p -> new IndirizzoDto(p.getId(), p.getIndirizzo(), "Prodotto", p.getNome()))
                .collect(Collectors.toList()));

        // Pacchetti APPROVATI
        result.addAll(pacchettoService.getPacchettiByStato(StatoProdotto.APPROVATO).stream()
                .map(p -> new IndirizzoDto(p.getId(), p.getIndirizzo(), "Pacchetto", p.getNome()))
                .collect(Collectors.toList()));

        // Fiere PUBBLICATE
        result.addAll(fieraService.getFiereByStato(StatoEvento.PUBBLICATA).stream()
                .map(f -> new IndirizzoDto(f.getId(), f.getIndirizzo(), "Fiera", f.getNome()))
                .toList());

        // Visite PUBBLICATE (usa DTO!)
        result.addAll(visitaInvitoService.getVisiteByStato(StatoEvento.PUBBLICATA).stream()
                .map(v -> new IndirizzoDto(v.getId(), v.getIndirizzo(), "Visita", v.getNome()))
                .collect(Collectors.toList()));

        // Prodotti Trasformati APPROVATI (usa DTO!)
        result.addAll(prodottoTrasformatoService.getProdottiTrasformatiByStato(StatoProdotto.APPROVATO).stream()
                .map(pt -> new IndirizzoDto(pt.getId(), pt.getIndirizzo(), "Prodotto Trasformato", pt.getNome()))
                .collect(Collectors.toList()));

        return result;
    }

    @Override
    public GeocodedIndirizzoDto geocodeIndirizzo(IndirizzoDto indirizzo) {
        double[] coords = geocode(indirizzo.getIndirizzo());

        if (coords == null) {
            return new GeocodedIndirizzoDto(
                    indirizzo.getId(),
                    indirizzo.getIndirizzo(),
                    indirizzo.getTipo(),
                    indirizzo.getNome(),
                    null,
                    null
            );
        }

        return new GeocodedIndirizzoDto(
                indirizzo.getId(),
                indirizzo.getIndirizzo(),
                indirizzo.getTipo(),
                indirizzo.getNome(),
                coords[0],
                coords[1]
        );
    }

    /**
     * Geocoding via Nominatim con AddressParts e varianti intelligenti.
     */
    private double[] geocode(String indirizzo) {
        try {
            Set<String> varianti = new LinkedHashSet<>();

            // Creo sempre un AddressParts con raw
            AddressParts parts = new AddressParts.Builder()
                    .raw(indirizzo)
                    .country("Italia")
                    .build();

            // Variante completa
            String base = parts.toQueryString();
            varianti.add(base);

            // Variante senza CAP
            String noCap = base.replaceAll("\\b[0-9]{5}\\b", "").trim();
            if (!noCap.equals(base)) varianti.add(noCap);

            // Variante senza sigla provincia (es. AN, TO, BS)
            String noProvincia = base.replaceAll("\\b[A-Z]{2}\\b", "").trim();
            if (!noProvincia.equals(base)) varianti.add(noProvincia);

            // Variante solo città + Italia
            if (parts.looksGeocodable() && parts.toQueryString().contains(",")) {
                String[] split = parts.toQueryString().split(",");
                String city = split.length > 1 ? split[split.length - 2].trim() : null;
                if (city != null && !city.isBlank()) {
                    varianti.add(city + ", Italia");
                }
            }

            // Variante solo raw + Italia
            if (indirizzo != null && !indirizzo.isBlank()) {
                varianti.add(indirizzo + ", Italia");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "filiera-agricola/1.0 (torquati79@yahoo.it)");
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            for (String variante : varianti) {
                String url = "https://nominatim.openstreetmap.org/search?q=" +
                        URLEncoder.encode(variante, StandardCharsets.UTF_8) +
                        "&format=json&limit=1";

                ResponseEntity<List<Map<String, Object>>> response =
                        restTemplate.exchange(url, HttpMethod.GET, requestEntity,
                                new ParameterizedTypeReference<>() {});

                if (response.getBody() != null && !response.getBody().isEmpty()) {
                    Map<String, Object> location = response.getBody().get(0);
                    double lat = Double.parseDouble((String) location.get("lat"));
                    double lon = Double.parseDouble((String) location.get("lon"));

                    System.out.println("✅ Geocoding OK per [" + indirizzo + "] → " + lat + "," + lon + " con variante: " + variante);
                    return new double[]{lat, lon};
                } else {
                    System.out.println("⚠️ Nessun risultato per variante: " + variante);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Errore geocoding per [" + indirizzo + "]: " + e.getMessage());
        }

        return null;
    }

}
