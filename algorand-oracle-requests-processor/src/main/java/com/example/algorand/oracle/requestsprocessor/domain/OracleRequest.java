package com.example.algorand.oracle.requestsprocessor.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OracleRequest {

    @Id
    @SequenceGenerator(name = "oracle_request_id_seq", sequenceName = "oracle_request_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "oracle_request_id_seq")
    private Long id;

    private String callerAddress;

    private Long callbackApplicationId;

    private String callbackMethod;

    private SupportedMarket marketRequested;

    private TransactionProcessingStatus status;

    @Column(nullable = true)
    private String processingErrorMessage;

    private LocalDateTime creationTime;

    @Column(nullable = true)
    private Long oracleResponseId;

    @Column(nullable = true)
    private LocalDateTime processTime;
}
