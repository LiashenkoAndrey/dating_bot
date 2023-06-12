package module.domain.enums;

import lombok.Setter;
import module.domain.persistentEntities.UserFilter;

public enum FindBy {

    STATE,
    CITY,
    DISTINCT,
    VILLAGE,
    LOCALITY,
    TOWN,
    DISTANCE;

    public static String translate(UserFilter filter) {
        String res;
        switch (filter.getFind_by()) {
            case CITY -> res = "місту " + filter.getCity();
            case DISTINCT -> res = "області " + filter.getDistrict();
            case VILLAGE -> res = "селу " + filter.getVillage();
            case TOWN -> res = "смт. " + filter.getTown();
            case LOCALITY -> res = "місцю " + filter.getLocality();
            case DISTANCE -> res = "дистанції " + filter.getDistance() + " км.";

            default -> res = "-----";
        }
        return res;
    }
}