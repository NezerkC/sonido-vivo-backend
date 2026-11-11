package cl.duoc.sonidovivo.infrastructure.adapter.in.rest;

import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.AuthResponseDTO;
import cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto.LoginRequestDTO;
import cl.duoc.sonidovivo.infrastructure.config.JwtService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inbound REST controller issuing stateless JWT tokens for admin authentication.
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
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        String token = jwtService.generateToken(
                authentication.getName(),
                authentication.getAuthorities()
        );
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return ResponseEntity.ok(new AuthResponseDTO(
                token,
                "Bearer",
                jwtService.getExpirationMs(),
                authentication.getName(),
                roles
        ));
    }
}
