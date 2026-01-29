package beordie.cn.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Objects;

public class OverdueStatusCodeSerializer extends JsonSerializer<Task.OverdueStatus> {
    @Override
    public void serialize(Task.OverdueStatus value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumber(Objects.requireNonNullElse(value, Task.OverdueStatus.NORMAL).getCode());
    }
}
