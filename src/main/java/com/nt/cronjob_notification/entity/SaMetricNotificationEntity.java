package com.nt.cronjob_notification.entity;

import java.sql.Clob;
import java.sql.Timestamp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table (name = "SA_METRIC_NOTIFICATION")
public class SaMetricNotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sa_metric_notification_seq")
    @SequenceGenerator(name = "sa_metric_notification_seq", allocationSize = 1)
    @Column(name = "ID")
    private Long ID = null;

    @Column(name = "EMAIL", unique = false,nullable = true)
    private String email = null;

    @Column(name = "OM_NOT_CONNECT", unique = false,nullable = true)
    private Integer OM_NOT_CONNECT = 0;

    @Column(name = "DB_OM_NOT_CONNECT", unique = false,nullable = true)
    private Integer DB_OM_NOT_CONNECT = 0;

    @Column(name = "TOPUP_NOT_CONNECT", unique = false,nullable = true)
    private Integer TOPUP_NOT_CONNECT = 0;

    @Column(name = "TRIGGER_NOTI_JSON", unique = false,nullable = true)
    private String TRIGGER_NOTI_JSON = null;

    @Column(name = "TRIGGER_IS_ACTIVE", unique = false,nullable = true)
    private Integer TRIGGER_IS_ACTIVE = 0;

    @Column(name = "UPDATED_DATE", unique = false,nullable = true)
    private Timestamp UPDATED_DATE = null;

    @Column(name = "UPDATED_By", unique = false,nullable = true)
    private String UPDATED_By = null;

    @Column(name = "LINE_IS_ACTIVE", unique = false,nullable = true)
    private Integer LINE_IS_ACTIVE = 0;

    @Column(name = "LINE_TOKEN", unique = false,nullable = true)
    private String LINE_TOKEN = null;
}
