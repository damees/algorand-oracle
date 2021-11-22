package com.example.algorand.oracle.requestsprocessor.repository;

import com.example.algorand.oracle.requestsprocessor.domain.OracleRequest;
import com.example.algorand.oracle.requestsprocessor.domain.OracleResponse;
import org.springframework.data.repository.CrudRepository;

public interface OracleResponseRepository  extends CrudRepository<OracleResponse, Long> {
}
