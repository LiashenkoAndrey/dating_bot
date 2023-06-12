package module.domain.persistentEntities;


import lombok.*;
import module.domain.Address;
import module.domain.Location;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Represents user location.
 */
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "user_location")
public class UserLocation implements Serializable {

    public UserLocation(Location loc) {
        Address address = loc.getAddress();
        this.longitude = loc.getLon();
        this.latitude = loc.getLat();
        this.town = address.getTown();
        this.city = address.getCity();
        this.village = address.getVillage();
        this.district = address.getDistrict();

    }

    @Id
    private Long user_id;

    @OneToOne
    @MapsId
    private User user;

    private Double longitude;

    private Double latitude;

    private String district;
    private String city;
    private String town;

    private String village;

    private String locality;
}


