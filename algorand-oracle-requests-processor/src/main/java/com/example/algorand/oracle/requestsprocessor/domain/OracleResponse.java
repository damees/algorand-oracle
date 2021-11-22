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
public class OracleResponse {

    @Id
    @SequenceGenerator(name = "oracle_response_id_seq", sequenceName = "oracle_response_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "oracle_response_id_seq")
    private Long id;

    private Long oracleRequestId;

    private Integer marketValueReturned;

    private String callBackTransactionHash;

    private LocalDateTime callbackTime;
}
