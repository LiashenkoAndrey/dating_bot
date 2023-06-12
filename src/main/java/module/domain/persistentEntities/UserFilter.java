package module.domain.persistentEntities;

import lombok.*;
import module.domain.enums.FindBy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Setter
@Getter
@ToString
@Table(name = "user_filter")
public class UserFilter {

    @Id
    private Long user_id;

    @OneToOne
    @MapsId
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    private FindBy find_by;

    private String district;

    private String city;

    private String town;

    private String village;

    private String locality;


    @Size(min = 13, max = 59)
    private Integer age_from;

    @Size(min = 14, max = 60)
    private Integer age_to;

    public static UserFilter buildDefaultFilter(User user) {
        UserFilter filter = new UserFilter();
        UserLocation location = user.getLocation();
        if (location.getVillage() != null) {
            filter.setFind_by(FindBy.VILLAGE);
            filter.setVillage(location.getVillage());
        }
        else if (location.getTown() != null) {
            filter.setFind_by(FindBy.TOWN);
            filter.setTown(location.getTown());
        }
        else if (location.getCity() != null) {
            filter.setFind_by(FindBy.CITY);
            filter.setCity(location.getCity());
        }
        else if (location.getDistrict() != null) {
            filter.setFind_by(FindBy.DISTINCT);
            filter.setDistrict(location.getDistrict());
        }
        else throw new IllegalArgumentException("User location has illegal fields. Can't set findBy");

        filter.setAge_from(user.getAge() - 1);
        filter.setAge_to(user.getAge() + 1);
        return filter;
    }

    private Double distance;
}
