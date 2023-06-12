package module.domain.persistentEntities;

import lombok.*;

import module.domain.UserCash;
import module.domain.enums.FindBy;
import module.domain.enums.Sex;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static module.util.HibernateUtils.userCashMap;

@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@AllArgsConstructor
@Table(name = "users")
public class User implements Serializable {

    public User(Long telegram_id) {
        this.telegram_id = telegram_id;
    }

    public User(String name) {
        this.name = name;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(
            min = 4,
            max = 255,
            message = "Name must be in a bound from 2 to 255"
    )
    private String name;

    @NotNull
    @Size(
            min = 14,
            max = 60,
            message = "Age must be in bound a from 14 to 60"
    )
    private int age;


    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private UserLocation location;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private UserFilter userFilter;

    @NotNull
    private Long telegram_id;

    @NotNull
    private Long chat_id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Sex sex;

    @NotNull
    @Size(
            max = 100,
            message =  "Rating must be in a bound from 0 to 100"
    )
    private int rating;


    @NotNull
    private Date registered_on;

    @NotNull
    private String about;

    @OneToMany(cascade = CascadeType.ALL,  fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @Size(min = 3, max = 5)
    private List<UserPhoto> photos = new ArrayList<>();


    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private transient UserCash userCash;

    public UserCash getUserCash() {
        if (userCashMap.containsKey(this.telegram_id)) return userCashMap.get(this.telegram_id);
        else {
            UserCash userCash = new UserCash();
            userCashMap.put(this.telegram_id, userCash);
            return userCash;
        }
    }



}


