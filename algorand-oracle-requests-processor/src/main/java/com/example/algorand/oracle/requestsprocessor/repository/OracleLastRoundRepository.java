package com.example.algorand.oracle.requestsprocessor.repository;

import com.example.algorand.oracle.requestsprocessor.domain.OracleLastRound;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface OracleLastRoundRepository extends CrudRepository<OracleLastRound, Integer> {

    @Query("SELECT o from OracleLastRound o")
    Optional<OracleLastRound> findLastRound();
}
