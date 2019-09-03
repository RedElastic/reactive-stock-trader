package services;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import play.libs.Json;

class JavaJsonCustomObjectMapper {

    JavaJsonCustomObjectMapper() {
        ObjectMapper mapper = Json.newDefaultMapper()
                .enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        Json.setObjectMapper(mapper);
    }
}