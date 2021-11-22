package com.example.algorand.oracle.requestsprocessor.repository;

import com.example.algorand.oracle.requestsprocessor.domain.OracleRequest;
import com.example.algorand.oracle.requestsprocessor.domain.TransactionProcessingStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OracleRequestRepository extends CrudRepository<OracleRequest, Long> {

    List<OracleRequest> findAllByStatus(TransactionProcessingStatus status);
}
