package pe.edu.vallegrande.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.user.dto.UserCreateDto;
import pe.edu.vallegrande.user.dto.UserDto;
import pe.edu.vallegrande.user.service.UserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // 🔍 Obtener todos los usuarios
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<UserDto> getAllUsers() {
        return userService.findAllUsers();
    }

    // 🔍 Obtener usuario por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<UserDto> getUserById(@PathVariable Integer id) {
        return userService.findById(id);
    }

    // 🔍 Obtener usuario por Email
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<UserDto> getUserByEmail(@PathVariable String email) {
        return userService.findByEmail(email);
    }

    // 🆕 Crear usuario en Firebase + BD
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<UserDto> createUser(@RequestBody UserCreateDto dto) {
        return userService.createUser(dto);
    }

    // ✏️ Actualizar usuario (excepto email y password)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<UserDto> updateUser(@PathVariable Integer id, @RequestBody UserDto dto) {
        return userService.updateUser(id, dto);
    }

    // 🗑️ Eliminar usuario de Firebase + BD
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable Integer id) {
        return userService.deleteUser(id)
                .thenReturn(ResponseEntity.noContent().<Void>build()) // 204 sin contenido
                .onErrorResume(e -> {
                    System.err.println("❌ Error al eliminar usuario: " + e.getMessage());
                    return Mono.just(ResponseEntity.status(400).<Void>build()); // 👈 forzamos tipo Void
                });
    }
}
