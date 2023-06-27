package module.domain.persistentEntities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Getter
@AllArgsConstructor
@Immutable
@Table(name = "likes")
public class Like {

    public Like(Long like_from_id, Long like_to_id) {
        this.like_from_id = like_from_id;
        this.like_to_id = like_to_id;
    }



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private Long like_from_id;

    private Long like_to_id;

}
