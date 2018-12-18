import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import play.libs.Json;


public class JavaJsonCustomObjectMapper {

    JavaJsonCustomObjectMapper() {
        ObjectMapper mapper = Json.newDefaultMapper();
        mapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        Json.setObjectMapper(mapper);
    }
}