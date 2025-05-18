package unicam.filiera.service;

import unicam.filiera.dto.DatiAcquistoDto;
import unicam.filiera.model.StatoPagamento;

public interface PagamentoService {
    StatoPagamento effettuaPagamento(DatiAcquistoDto dati);
}


