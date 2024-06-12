package com.nt.cronjob_notification.entity;


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
@Table (name = "ORDERTYPE")
public class OrderTypeEntity {
        
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ordertype_seq")
        @SequenceGenerator(name = "ordertype_seq", allocationSize = 1)
        @Column(name = "ID")
        private Long ID = null;

        @Column(name = "SA_CHANNEL_CONNECT_ID", unique = false,nullable = true)
        private Long SA_CHANNEL_CONNECT_ID = null;

        @Column(name = "OrderType_Name", unique = false,nullable = true)
        private String OrderTypeName = null;

        @Column(name = "DESCRIPTION", unique = false,nullable = true)
        private String DESCRIPTION = null;

        @Column(name = "MESSAGE_EXPIRE", unique = false,nullable = true)
        private String MESSAGE_EXPIRE = null;

        @Column(name = "Is_Enable", unique = false,nullable = true)
        private Integer Is_Enable = 1;

        @Column(name = "Is_Delete", unique = false,nullable = true)
        private Integer Is_Delete = 0;

        @Column(name = "Is_Delete_By", unique = false,nullable = true)
        private String Is_Delete_By = null;

        @Column(name = "Is_Delete_Date", unique = false,nullable = true)
        private Timestamp Is_Delete_Date = null;

        @Column(name = "Created_Date", unique = false,nullable = true)
        private Timestamp Created_Date = null;

        @Column(name = "Created_By", unique = false,nullable = true)
        private String Created_By = null;

        @Column(name = "Updated_Date", unique = false,nullable = true)
        private Timestamp Updated_Date = null;

        @Column(name = "Updated_By", unique = false,nullable = true)
        private String Updated_By = null;
}
