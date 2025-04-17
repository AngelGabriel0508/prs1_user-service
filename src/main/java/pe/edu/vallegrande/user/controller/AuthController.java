package pe.edu.vallegrande.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.user.service.UserService;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // 🔹 Olvidé mi contraseña
    @PostMapping("/forgot-password")
    public Mono<ResponseEntity<Map<String, String>>> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");

        return userService.sendPasswordResetEmail(email)
                .map(msg -> ResponseEntity.ok(Map.of("message", msg)))
                .onErrorResume(e -> {
                    // Puedes devolver 400 (Bad Request) o 404 (Not Found)
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", e.getMessage())));
                });
    }

}
