package pe.edu.vallegrande.user.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.user.config.CustomAuthenticationToken;
import pe.edu.vallegrande.user.dto.UserDto;
import pe.edu.vallegrande.user.service.UserService;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    // 🔍 Obtener mis datos
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Mono<UserDto> getMyProfile(@AuthenticationPrincipal CustomAuthenticationToken auth) {
        String uid = auth.getName();
        return userService.findMyProfile(uid);
    }
    // Cambiar la contraseña
    @PutMapping("/password")
    public Mono<UserDto> changePassword(@AuthenticationPrincipal CustomAuthenticationToken auth,
                                        @RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");
        return userService.changePassword(auth.getName(), newPassword);
    }
    // Cambiar el correo
    @PutMapping("/email")
    public Mono<UserDto> changeEmail(@AuthenticationPrincipal CustomAuthenticationToken auth,
                                     @RequestBody Map<String, String> body) {
        String newEmail = body.get("newEmail");
        return userService.changeEmail(auth.getName(), newEmail);
    }

    // ✏️ Editar mis datos
    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Mono<Map<String, Object>> updateMyProfile(@AuthenticationPrincipal CustomAuthenticationToken auth,
                                                     @RequestBody Map<String, Object> payload) {
        String firebaseUid = auth.getName();
        if (payload.get("name") == null || payload.get("lastName") == null || payload.get("documentNumber") == null) {
            return Mono.just(Map.of("error", "Faltan campos obligatorios"));
        }
        UserDto dto = new UserDto();
        dto.setName((String) payload.get("name"));
        dto.setLastName((String) payload.get("lastName"));
        dto.setDocumentType((String) payload.get("documentType"));
        dto.setDocumentNumber((String) payload.get("documentNumber"));
        dto.setCellPhone((String) payload.get("cellPhone"));
        dto.setProfileImage((String) payload.get("profileImage"));
        return userService.updateMyProfile(firebaseUid, dto)
                .map(updated -> Map.of("message", "✅ Perfil actualizado correctamente", "user", updated))
                .onErrorResume(e -> Mono.just(Map.of("error", e.getMessage())));
    }
}
