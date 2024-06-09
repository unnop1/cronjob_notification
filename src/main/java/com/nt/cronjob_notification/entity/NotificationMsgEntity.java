package com.nt.cronjob_notification.entity;


import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;

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
@Table(name = "notification_message")
public class NotificationMsgEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_message_seq")
    @SequenceGenerator(name = "notification_message_seq", allocationSize = 1)
    @Column(name = "ID")
    private Long ID;

    @Column(name = "action", nullable = true)
    private String action=null;

    @Column(name = "email", nullable = true)
    private String email=null;

    @Column(name = "message", nullable = true)
    private String message=null;

    @Column(name = "created_date", nullable = true)
    private Timestamp created_date=null;
}
