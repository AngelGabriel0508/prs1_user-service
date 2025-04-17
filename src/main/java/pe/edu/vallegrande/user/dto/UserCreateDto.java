package pe.edu.vallegrande.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateDto {
    private String name;
    private String lastName;
    private String documentType;
    private String documentNumber;
    private String cellPhone;
    private String email;
    private String password;
    private List<String> role;
    private String profileImage;
}
