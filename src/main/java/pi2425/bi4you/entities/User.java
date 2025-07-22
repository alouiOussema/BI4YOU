package pi2425.bi4you.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        })
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotBlank
    @Size(min = 4, max = 20)
    String username;

    @NotBlank
    @Size(max = 50)
    @Email
    String email;

    @NotBlank
    @Size(min = 8)
    String password;

    @Size(max = 50)
    String firstName;

    @Size(max = 50)
    String lastName;

    @Column(nullable = false)
    boolean active = true;

    @Column(nullable = false)
    boolean firstLogin = true;

    @Temporal(TemporalType.TIMESTAMP)
    Date creationDate = new Date();

    @Column(columnDefinition = "TIMESTAMP")
    LocalDateTime lastLogin;

    String token;

    @Column(columnDefinition = "TIMESTAMP")
    LocalDateTime tokenCreationDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    Set<Roles> roles = new HashSet<>();

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.creationDate = new Date();
        this.active = true;
        this.firstLogin = true;
    }

    public User(String username, String email, String password, String firstName, String lastName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.creationDate = new Date();
        this.active = true;
        this.firstLogin = true;
    }
}

