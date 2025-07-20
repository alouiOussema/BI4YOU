package pi2425.bi4you.dtos.responses;

import lombok.*;
import lombok.experimental.FieldDefaults;


@Getter
@Setter
@FieldDefaults(level= AccessLevel.PRIVATE)
public class MessageResponse {
    String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MessageResponse(String message) {
        this.message = message;
    }
}
