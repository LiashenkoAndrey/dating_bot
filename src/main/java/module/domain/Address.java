package module.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {

    private String town;

    private String city;

    private String village;

    private String district;

    private String state;

    private String borough;


    public String toFormattedString() {
        var sb = new StringBuilder();

        if (town != null) sb.append(town);
        else if (city != null) sb.append(city);
        else if (village != null) sb.append(village);
        if (district != null) sb.append(" " +district);
        if (state != null) sb.append(" " + state);

        return sb.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(town, address.town) && Objects.equals(city, address.city) && Objects.equals(village, address.village) && Objects.equals(district, address.district) && Objects.equals(state, address.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(town, city, village, district, state);
    }
}