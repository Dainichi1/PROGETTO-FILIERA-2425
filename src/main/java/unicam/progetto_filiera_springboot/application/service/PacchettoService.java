package unicam.progetto_filiera_springboot.application.service;

import org.springframework.web.multipart.MultipartFile;
import unicam.progetto_filiera_springboot.application.dto.PacchettoForm;
import unicam.progetto_filiera_springboot.application.dto.PacchettoResponse;
import unicam.progetto_filiera_springboot.application.dto.ProdottoResponse;

import java.util.List;
import java.util.Optional;

public interface PacchettoService {

    PacchettoResponse creaPacchettoConFile(PacchettoForm form,
                                           String usernameDistributore,
                                           List<MultipartFile> foto,
                                           List<MultipartFile> certificati);

    PacchettoResponse uploadFoto(Long pacchettoId, List<MultipartFile> files);

    PacchettoResponse uploadCertificati(Long pacchettoId, List<MultipartFile> files);

    List<PacchettoResponse> pacchettiDi(String usernameDistributore);

    List<PacchettoResponse> listInAttesa();

    void approve(Long id);

    void reject(Long id, Optional<String> commento);

    List<PacchettoResponse> listApprovati();

    boolean isEliminabile(Long id, String username);

    PacchettoResponse findByIdAndOwner(Long id, String username);

    /** Elimina definitivamente se consentito (controlli inclusi) */
    void elimina(Long id, String username);
}
