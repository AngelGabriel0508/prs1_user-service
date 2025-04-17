package pe.edu.vallegrande.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("users")
public class User {
    @Id
    private Integer id;
    @Column("firebase_uid")
    private String firebaseUid;
    @Column("name")
    private String name;
    @Column("last_name")
    private String lastName;
    @Column("document_type")
    private String documentType;
    @Column("document_number")
    private String documentNumber;
    @Column("cell_phone")
    private String cellPhone;
    @Column("email")
    private String email;
    @Column("password")
    private String password;
    @Column("role")
    private List<String> role;
    @Column("profile_image")
    private String profileImage;
}
