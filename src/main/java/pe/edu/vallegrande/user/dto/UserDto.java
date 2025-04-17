package pe.edu.vallegrande.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.user.model.User;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Integer id;
    private String firebaseUid;
    private String name;
    private String lastName;
    private String documentType;
    private String documentNumber;
    private String cellPhone;
    private String email;
    private List<String> role;
    private String profileImage;

    // ✅ Método estático para convertir de Entity → DTO
    public static UserDto fromEntity(User user) {
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
