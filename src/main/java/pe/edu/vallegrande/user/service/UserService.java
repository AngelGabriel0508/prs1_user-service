package pe.edu.vallegrande.user.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord.CreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

import pe.edu.vallegrande.user.dto.UserCreateDto;
import pe.edu.vallegrande.user.dto.UserDto;
import pe.edu.vallegrande.user.model.User;
import pe.edu.vallegrande.user.repository.UsersRepository;

import java.util.Map;


@Slf4j
@Service
public class UserService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SupabaseStorageService storageService;


    @Autowired
    public UserService(UsersRepository usersRepository, PasswordEncoder passwordEncoder,
                       EmailService emailService, SupabaseStorageService storageService) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.storageService = storageService;
    }

    /**
     * 🔹 Guardar nuevo usuario en firebase como en la bd
     */
    public Mono<UserDto> createUser(UserCreateDto dto) {
        return usersRepository.findByEmail(dto.getEmail())
                .flatMap(existing -> Mono.error(new IllegalArgumentException("El correo ya está en uso.")))
                .switchIfEmpty(Mono.defer(() -> {
                    // 🔐 Crear usuario en Firebase
                    CreateRequest request = new CreateRequest()
                            .setEmail(dto.getEmail())
                            .setPassword(dto.getPassword())
                            .setEmailVerified(false)
                            .setDisabled(false);

                    return Mono.fromCallable(() -> FirebaseAuth.getInstance().createUser(request))
                            .flatMap(firebaseUser -> {
                                String uid = firebaseUser.getUid();

                                // Asignar claim
                                String primaryRole = dto.getRole().isEmpty() ? "USER" : dto.getRole().get(0);
                                return Mono.fromCallable(() -> {
                                    FirebaseAuth.getInstance().setCustomUserClaims(uid, Map.of("role", primaryRole.toUpperCase()));
                                    return uid;
                                }).cast(String.class);
                            })
                            .flatMap(uid -> {
                                // Subir imagen a Supabase
                                return storageService.uploadBase64Image("users", dto.getProfileImage())
                                        .flatMap(imageUrl -> {
                                            // Guardar en BD
                                            User user = new User();
                                            user.setFirebaseUid(uid);
                                            user.setName(dto.getName());
                                            user.setLastName(dto.getLastName());
                                            user.setDocumentType(dto.getDocumentType());
                                            user.setDocumentNumber(dto.getDocumentNumber());
                                            user.setCellPhone(dto.getCellPhone());
                                            user.setEmail(dto.getEmail());
                                            user.setPassword(passwordEncoder.encode(dto.getPassword()));
                                            user.setRole(dto.getRole());
                                            user.setProfileImage(imageUrl); // Guardar URL de la imagen

                                            return usersRepository.save(user)
                                                    .map(this::toDto)
                                                    .cast(UserDto.class);
                                        });
                            });
                })).cast(UserDto.class);
    }

    /**
     * 🔹 Actualizar usuario
     */
    public Mono<UserDto> updateUser(Integer id, UserDto dto) {
        return usersRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Usuario no encontrado")))
                .flatMap(existing -> {
                    boolean roleChanged = !existing.getRole().equals(dto.getRole());

                    // Actualizar campos editables
                    existing.setName(dto.getName());
                    existing.setLastName(dto.getLastName());
                    existing.setDocumentType(dto.getDocumentType());
                    existing.setDocumentNumber(dto.getDocumentNumber());
                    existing.setCellPhone(dto.getCellPhone());
                    existing.setRole(dto.getRole());

                    // Si hay una nueva imagen de perfil
                    if (!dto.getProfileImage().isEmpty()) {
                        // Eliminar imagen anterior
                        if (existing.getProfileImage() != null) {
                            storageService.deleteImage(existing.getProfileImage()).subscribe();
                        }
                        // Subir nueva imagen
                        return storageService.uploadBase64Image("users", dto.getProfileImage())
                                .flatMap(imageUrl -> {
                                    existing.setProfileImage(imageUrl); // Actualizar URL de la imagen
                                    return usersRepository.save(existing)
                                            .map(this::toDto);
                                });
                    } else {
                        // Si no hay nueva imagen, solo guardamos
                        return usersRepository.save(existing)
                                .map(this::toDto);
                    }
                });
    }

    /**
     * 🔹 Obtener mis datos
     */
    public Mono<UserDto> findMyProfile(String firebaseUid) {
        return usersRepository.findAll()
                .filter(user -> firebaseUid.equals(user.getFirebaseUid()))
                .next()
                .map(this::toDto)
                .switchIfEmpty(Mono.error(new RuntimeException("Usuario no encontrado")));
    }

    /**
     * 🔹 Obtener todos los usuarios
     */
    public Flux<UserDto> findAllUsers() {
        return usersRepository.findAll()
                .map(this::toDto);
    }

    /**
     * 🔹 Buscar por ID
     */
    public Mono<UserDto> findById(Integer id) {
        return usersRepository.findById(id)
                .map(this::toDto);
    }

    /**
     * 🔹 Buscar por email
     */
    public Mono<UserDto> findByEmail(String email) {
        return usersRepository.findByEmail(email)
                .map(this::toDto);
    }

    /**
     * 🔹 Eliminar por ID
     */
    public Mono<Void> deleteUser(Integer id) {
        return usersRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Usuario no encontrado")))
                .flatMap(user -> {
                    // Eliminar imagen de Supabase
                    if (user.getProfileImage() != null) {
                        return storageService.deleteImage(user.getProfileImage())
                                .then(usersRepository.deleteById(user.getId()));
                    } else {
                        return usersRepository.deleteById(user.getId());
                    }
                });
    }

    /**
     * 🔹 Cambiar Email
     */
    public Mono<UserDto> changeEmail(String firebaseUid, String newEmail) {
        return usersRepository.findAll()
                .filter(user -> firebaseUid.equals(user.getFirebaseUid()))
                .next()
                .switchIfEmpty(Mono.error(new RuntimeException("Usuario no encontrado")))
                .flatMap(user -> usersRepository.findByEmail(newEmail)
                        .flatMap(conflict -> Mono.error(new RuntimeException("El correo ya está en uso")))
                        .switchIfEmpty(Mono.defer(() ->
                                // 🔐 Cambiar en Firebase
                                Mono.fromCallable(() -> {
                                    FirebaseAuth.getInstance().updateUser(
                                            new com.google.firebase.auth.UserRecord.UpdateRequest(firebaseUid)
                                                    .setEmail(newEmail)
                                    );
                                    return user;
                                })
                        ))
                )
                .flatMap(obj -> {
                    User user = (User) obj;
                    user.setEmail(newEmail);
                    return usersRepository.save(user).map(this::toDto);
                });

    }



    /**
     * 🔹 Cambiar Contraseña
     */
    public Mono<UserDto> changePassword(String firebaseUid, String newPassword) {
        return usersRepository.findAll()
                .filter(user -> firebaseUid.equals(user.getFirebaseUid()))
                .next()
                .switchIfEmpty(Mono.error(new RuntimeException("Usuario no encontrado")))
                .flatMap(user -> {
                    // 🔐 Cambiar en Firebase
                    return Mono.fromCallable(() -> {
                        FirebaseAuth.getInstance().updateUser(
                                new com.google.firebase.auth.UserRecord.UpdateRequest(firebaseUid)
                                        .setPassword(newPassword)
                        );
                        return user;
                    });
                })
                .flatMap(user -> {
                    // 🔄 Cambiar en BD
                    user.setPassword(passwordEncoder.encode(newPassword));
                    return usersRepository.save(user).map(this::toDto);
                });
    }

    /**
     * 🔹 Reestablecer Contraseña si te olvidaste
     */
    public Mono<String> sendPasswordResetEmail(String email) {
        return Mono.fromCallable(() -> FirebaseAuth.getInstance().getUserByEmail(email))
                .flatMap(userRecord ->
                        usersRepository.findByEmail(email) // ✅ valida también en tu BD
                                .switchIfEmpty(Mono.error(new RuntimeException("❌ El email no está registrado en el sistema.")))
                                .flatMap(user -> Mono.fromCallable(() -> {
                                    String link = FirebaseAuth.getInstance().generatePasswordResetLink(email);
                                    emailService.sendResetLink(email, link); // ✉️ Envía el correo
                                    return "✅ Enlace enviado correctamente a: " + email;
                                }))
                )
                .onErrorResume(e -> {
                    log.error("❌ Error real desde Firebase: ", e); // <-- importante
                    String msg = e.getMessage().contains("NOT_FOUND")
                            ? "❌ El correo no existe en Firebase"
                            : "⚠️ Error: " + e.getMessage();
                    return Mono.error(new RuntimeException(msg));
                });
    }

    /**
     * 🔄 Editar mis propios datos (sin cambiar email, password ni rol)
     */
    public Mono<UserDto> updateMyProfile(String uid, UserDto updatedData) {
        return usersRepository.findByFirebaseUid(uid)
                .switchIfEmpty(Mono.error(new RuntimeException("Usuario no encontrado")))
                .flatMap(existing -> {
                    existing.setName(updatedData.getName());
                    existing.setLastName(updatedData.getLastName());
                    existing.setDocumentType(updatedData.getDocumentType());
                    existing.setDocumentNumber(updatedData.getDocumentNumber());
                    existing.setCellPhone(updatedData.getCellPhone());
                    existing.setProfileImage(updatedData.getProfileImage());
                    return usersRepository.save(existing);
                })
                .map(UserDto::fromEntity); // o usar tu mapper si tienes uno
    }

    /**
     * 🔁 Método auxiliar: Entity → DTO
     */
    private UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getFirebaseUid(),
                user.getName(),
                user.getLastName(),
                user.getDocumentType(),
                user.getDocumentNumber(),
                user.getCellPhone(),
                user.getEmail(),
                user.getRole(),
                user.getProfileImage()
        );
    }
}
