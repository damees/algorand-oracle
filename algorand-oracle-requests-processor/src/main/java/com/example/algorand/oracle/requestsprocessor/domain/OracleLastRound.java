package com.example.algorand.oracle.requestsprocessor.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OracleLastRound {

    @Id
    private Integer id;

    @Column
    private Long lastProcessedRound;
}
