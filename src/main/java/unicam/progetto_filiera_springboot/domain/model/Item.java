package unicam.progetto_filiera_springboot.domain.model;

import java.math.BigDecimal;

public interface Item {
    Long getId();
    String getNome();
    String getDescrizione();
    BigDecimal getPrezzo();
    int getQuantita();
    String getIndirizzo();
    String getCertificati();
    String getFoto();
}
