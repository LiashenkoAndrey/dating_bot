package module.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import module.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class UserServiceUtils {

    public static final String HOST = "http://localhost:8081";

    private static final Logger logger = LoggerFactory.getLogger(UserServiceUtils.class);

    public static String doGetRequest(String url) {
        try {
            logger.info("Get request: " + url);
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            logger.info("doGetRequest: url: " + url +" Status: " + connection.getResponseCode());
            return getResponse(connection.getInputStream());
        } catch (Exception e) {
            logger.error("Can't do GET request url: " + url);
            e.printStackTrace();
            return null;
        }
    }

    public static String getResponse(InputStream inputStream) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            logger.info("Response: " + sb.toString());
            return sb.toString();
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public static User parseUserFromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
            return mapper.readValue(json, User.class);
        } catch (Exception ex) {
            logger.error("Can't parse json: " + json +" " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }


    public static String postRequest(String urlStr, String json , Map<String, String> headers) {
        try {
            URL url = new URL(HOST + urlStr);
            logger.info("URL: " + HOST + urlStr);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            headers.keySet().forEach(key -> connection.setRequestProperty(key, headers.get(key)));
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                os.write(bytes, 0, bytes.length);
            }

            return getResponse(connection.getInputStream());
        } catch (Exception ex) {
            logger.error("Can't do post request. Url: " + urlStr +", body:" + json);
            ex.printStackTrace();
            return null;
        }
    }

    public static String userToJson(User user) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
        return mapper.writeValueAsString(user);
    }
}
