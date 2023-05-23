package module.domain.util;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import module.domain.enums.Sex;

import java.io.IOException;

public class SexDeserializer extends JsonDeserializer<Sex> {
    @Override
    public Sex deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        return Sex.valueOf(jsonParser.getText());
    }
}
