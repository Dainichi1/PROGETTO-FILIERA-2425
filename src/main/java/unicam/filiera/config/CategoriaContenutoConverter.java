package unicam.filiera.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import unicam.filiera.model.CategoriaContenuto;

@Component
public class CategoriaContenutoConverter implements Converter<String, CategoriaContenuto> {

    @Override
    public CategoriaContenuto convert(String source) {
        // Normalizza in maiuscolo e sostituisce eventuali spazi con underscore
        String normalized = source.trim().toUpperCase().replace(" ", "_");
        return CategoriaContenuto.valueOf(normalized);
    }
}
