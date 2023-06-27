package module.domain.persistentEntities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user_photos")
public class UserPhoto {

    public UserPhoto(User user, String photo_id) {
        this.user = user;
        this.photo_id = photo_id;
    }

    public UserPhoto(String photo_id) {
        this.photo_id = photo_id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(cascade = CascadeType.PERSIST)
    private User user;

    @NotNull
    private String photo_id;
}
