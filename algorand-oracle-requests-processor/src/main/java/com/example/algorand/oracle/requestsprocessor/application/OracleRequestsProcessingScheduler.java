package com.example.algorand.oracle.requestsprocessor.application;

import com.algorand.algosdk.account.Account;
import com.algorand.algosdk.v2.client.common.AlgodClient;
import com.example.algorand.oracle.requestsprocessor.domain.TransactionProcessingStatus;
import com.example.algorand.oracle.requestsprocessor.repository.OracleRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OracleRequestsProcessingScheduler {
    private final OracleRequestRepository oracleRequestRepository;
    private final TaskExecutor oracleCallbackExecutor;
    private final OracleService oracleService;
    private final AlgodClient algodClient;
    private final Account callbackSender;

    @Scheduled(fixedDelay = 4500)
    public void processRequests() {
        oracleRequestRepository.findAllByStatus(TransactionProcessingStatus.CREATED)
                .forEach(request -> {
                    log.info("going to process request {}", request.getId());
                    oracleCallbackExecutor.execute(
                            new OracleRequestsProcessor(request, algodClient, oracleService, callbackSender));
                });
    }
}
