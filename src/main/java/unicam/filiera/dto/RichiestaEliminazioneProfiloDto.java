package unicam.filiera.dto;

public class RichiestaEliminazioneProfiloDto {
    private final String username;

    public RichiestaEliminazioneProfiloDto(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
