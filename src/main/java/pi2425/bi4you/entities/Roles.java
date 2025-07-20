package pi2425.bi4you.entities;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import pi2425.bi4you.enmus.ERole;


@Entity
@Table(name = "roles")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Roles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    ERole name;

    public Roles(ERole name) {
        this.name=name;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ERole getName() {
        return name;
    }

    public void setName(ERole name) {
        this.name = name;
    }

    public Roles(Integer id, ERole name) {
        this.id = id;
        this.name = name;
    }

    public Roles() {
    }
}
