package pe.edu.vallegrande.user.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import pe.edu.vallegrande.user.model.User;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public interface UsersRepository extends ReactiveCrudRepository<User, Integer> {

    // Método para buscar un usuario por su correo electrónico
    Mono<User> findByEmail(String email);
    Mono<User> findByFirebaseUid(String firebaseUid);

}
