package de.larphelden.larp_app.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.beans.ConstructorProperties;

@Getter
@Setter

public class UserDto {


    private String username;
    private String password;
    private String email;
    private String role;
    private String photoPath;

    public UserDto() {
    }

    public UserDto(String username, String email) {
        this.username = username;
        this.email = email;

    }

}
