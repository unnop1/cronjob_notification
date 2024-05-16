package com.nt.cronjob_notification.client.handle;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
public class LineNotifyResponse {
    @JsonProperty("status")
    private int status;

    @JsonProperty("message")
    private String message;
}
