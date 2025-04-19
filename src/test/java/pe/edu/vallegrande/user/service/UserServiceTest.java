package pe.edu.vallegrande.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.vallegrande.user.dto.UserDto;
import pe.edu.vallegrande.user.model.User;
import pe.edu.vallegrande.user.repository.UsersRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UsersRepository usersRepository;
    private PasswordEncoder passwordEncoder;
    private EmailService emailService;
    private SupabaseStorageService storageService; // ✅ nuevo mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        usersRepository = mock(UsersRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        emailService = mock(EmailService.class);
        storageService = mock(SupabaseStorageService.class); // ✅ instanciar mock

        userService = new UserService(usersRepository, passwordEncoder, emailService, storageService); // ✅ incluir
    }

    @Test
    void shouldReturnUserProfile_whenUidExists() {
        String uid = "abc123";
        User mockUser = new User();
        mockUser.setFirebaseUid(uid);
        mockUser.setEmail("test@email.com");

        when(usersRepository.findAll()).thenReturn(Mono.just(mockUser).flux());

        Mono<UserDto> result = userService.findMyProfile(uid);

        StepVerifier.create(result)
                .expectNextMatches(userDto -> userDto.getEmail().equals("test@email.com"))
                .verifyComplete();
    }
}
