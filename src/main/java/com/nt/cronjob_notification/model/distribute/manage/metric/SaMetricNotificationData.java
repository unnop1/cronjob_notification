package com.nt.cronjob_notification.model.distribute.manage.metric;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SaMetricNotificationData {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("email")
    private String email;

    @JsonProperty("om_NOT_CONNECT")
    private Integer omNotConnect;

    @JsonProperty("db_OM_NOT_CONNECT")
    private Integer dbOmNotConnect;

    @JsonProperty("topup_NOT_CONNECT")
    private Integer topupNotConnect;

    @JsonProperty("trigger_NOTI_JSON")
    private String triggerNotiJson;

    @JsonProperty("updated_DATE")
    private Timestamp updatedDate;

    @JsonProperty("updated_By")
    private String updatedBy;

    @JsonProperty("line_IS_ACTIVE")
    private Integer lineIsActive;

    @JsonProperty("line_TOKEN")
    private String lineToken;
    
}