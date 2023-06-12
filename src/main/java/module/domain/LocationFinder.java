package module.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import module.domain.enums.FindBy;
import module.exeptions.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static module.util.NetworkUtils.readResponse;

public class LocationFinder {

    private static final Logger logger = LoggerFactory.getLogger(LocationFinder.class);

    public List<Location> getMatchedLocations(String locName) throws IOException {
        String searchUrl = "https://nominatim.openstreetmap.org/search?format=json&addressdetails=1&countrycodes=UA&q=";
        URL url = new URL(searchUrl + locName.replaceAll(" ", "+"));
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        return new ObjectMapper().readValue(readResponse(con.getInputStream()), new TypeReference<>(){});
    }

    public Set<Address> filterLocations(String locName, FindBy findBy) {
        try {
            if (findBy.equals(FindBy.DISTANCE) || findBy.equals(FindBy.LOCALITY))
                throw new IllegalArgumentException("Please don't use enum types module.domain.enums.FindBy.DISTANCE, LOCALITY, STATE");

            Set<Address> filteredAddresses = new HashSet<>();
            getMatchedLocations(locName).forEach(location -> filteredAddresses.add(location.getAddress()));

            return filteredAddresses.stream()
                            .filter(address -> {
                                boolean present;
                                switch (findBy) {
                                    case DISTINCT -> present = address.getDistrict() != null || address.getBorough() != null;
                                    case CITY -> present = address.getCity() != null;
                                    case TOWN -> present = address.getTown() != null;
                                    case VILLAGE -> present = address.getVillage() != null;
                                    default -> present = false;
                                }
                                return present;
                            }).collect(Collectors.toUnmodifiableSet());

        } catch (IOException e) {
            logger.error(e.toString());
            throw new ServiceException(e);
        }
    }

}
