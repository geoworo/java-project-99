package hexlet.code.dto.status;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter

public class TaskStatusUpdateDTO {
    @NotNull
    @Column(unique = true)
    private JsonNullable<String> name;

    @NotNull
    @Column(unique = true)
    private JsonNullable<String> slug;
}
