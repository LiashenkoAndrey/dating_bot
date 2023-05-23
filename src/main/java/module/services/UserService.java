package module.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import module.domain.User;
import module.domain.enums.Sex;
import org.open.cdi.annotations.DIBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

import static module.services.UserServiceUtils.*;

@DIBean
@NoArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private static final String HOST = "http://localhost:8081";

    private static final Map<String, String> jsonHeaders = Map.of("Content-Type", "application/json",
            "Accept", "application/json");
    public void save(User user) {
        logger.info("Save user: " + user);
        try {
            String userJson = userToJson(user);
            String resultJson = postRequest("http://localhost:8081/api/user/new", userJson, jsonHeaders);
            logger.info("Save user responce is: " + resultJson);

        } catch (Exception e) {
            logger.error("Can't save user: " + user.getName() + ", telegram id: " + user.getTelegram_id());
            e.printStackTrace();
        }
    }

    public String getIdByTelegramId() {
            String responce = doGetRequest("/api/user/id?telegram_id=");
            System.out.println(responce);
            return responce;
    }

    public static void main(String[] args) {
        UserService service = new UserService();
        service.getIdByTelegramId();
    }


    public User getProfileByTelegramId(Long id) {
        try {
            String responseJson = doGetRequest("/api/user/id/" + id);
            return parseUserFromJson(responseJson);
        } catch (Exception e) {
            logger.error("Can't get profile by telegram id:" + id);
            e.printStackTrace();
            return null;
        }
    }


    public User getRandomUserFromPool(Sex sex) {
        try {
            String responseJson = doGetRequest("/api/user/random?sex=" + sex.toString());
            ObjectMapper mapper = new ObjectMapper();
            Map<String, ?> userMap =  mapper.readValue(responseJson,  new TypeReference<>() {});
            String user = (String) userMap.get("user");
            mapper.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);

            return mapper.readValue(user, User.class);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public void registerInPool(User user) {
        try {
            String json = userToJson(user);
            String url = "/api/user/register_in_pool?sex=" + user.getSex().toString();
            String resultJson = postRequest(url, json, jsonHeaders);

            logger.info("RegisterInPool responce is: " + resultJson);
            user.setActive(true);
        } catch (Exception e) {
            logger.error("Can't register user: " + user.getName() + ", id: " + user.getTelegram_id() +" in active user pool");
            e.printStackTrace();
        }
    }

    public void updateUser(User user) {
        try {
            String json = userToJson(user);
            String url = "/api/user/update";
            String resultJson = postRequest(url, json, jsonHeaders);

            logger.info("RegisterInPool responce is: " + resultJson);
        } catch (Exception ex) {
            logger.error("Can't update user: " + user);
            ex.printStackTrace();
        }
    }
}
