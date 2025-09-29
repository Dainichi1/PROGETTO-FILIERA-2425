package unicam.filiera.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleCheckingAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String selectedRole = request.getParameter("ruolo"); // dal form

        // ruolo è obbligatorio
        if (selectedRole == null || selectedRole.isBlank()) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            response.sendRedirect(request.getContextPath() + "/login?error=ruolo");
            return;
        }

        // Spring assegna authorities tipo "ROLE_PRODUTTORE"
        boolean matches = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + selectedRole));

        if (!matches) {
            // ruolo selezionato non coincide con quello dell'utente → errore
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            response.sendRedirect(request.getContextPath() + "/login?error=ruolo");
            return;
        }

        // tutto ok → vai alla dashboard
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}
