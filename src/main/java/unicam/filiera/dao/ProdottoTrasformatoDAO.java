package unicam.filiera.dao;

import unicam.filiera.model.ProdottoTrasformato;
import unicam.filiera.model.StatoProdotto;

import java.io.File;
import java.util.List;

public interface ProdottoTrasformatoDAO {
    boolean save(ProdottoTrasformato p, List<File> certificati, List<File> foto);
    boolean update(
            String nomeOriginale,
            String creatore,
            ProdottoTrasformato p,
            List<File> certificati,
            List<File> foto
    );
    boolean update(ProdottoTrasformato p);
    boolean deleteByNomeAndCreatore(String nome, String creatore);

    List<ProdottoTrasformato> findByCreatore(String creatore);
    List<ProdottoTrasformato> findByStato(StatoProdotto stato);
    List<ProdottoTrasformato> findAll();
    ProdottoTrasformato findByNome(String nome);
    ProdottoTrasformato findByNomeAndCreatore(String nome, String creatore);
}
