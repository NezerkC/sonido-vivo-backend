package cl.duoc.sonidovivo.infrastructure.adapter.in.rest;

import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.AuthResponseDTO;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.LoginRequestDTO;
import cl.duoc.sonidovivo.infrastructure.config.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inbound REST controller issuing stateless JWT tokens for admin authentication.
 *
 * <p>Responsable asignado: <strong>Dylan Sandoval (@dysandoval)</strong></p>
 * <p>Documentación y contrato: {@code docs/contracts/CONTRACT_FULLSTACK_REST.md}</p>
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        // TODO (@dysandoval - Dylan Sandoval):
        // 1. Autenticar credenciales con authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password())).
        // 2. Generar token firmado con jwtService.generateToken(authentication.getName(), authentication.getAuthorities()).
        // 3. Extraer lista de roles y retornar ResponseEntity.ok(new AuthResponseDTO(token, "Bearer", jwtService.getExpirationMs(), username, roles)).
        throw new UnsupportedOperationException("login pendiente de implementación por Dylan Sandoval (@dysandoval)");
    }
}
