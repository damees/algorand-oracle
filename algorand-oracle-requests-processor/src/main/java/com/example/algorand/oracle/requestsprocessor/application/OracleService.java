package com.example.algorand.oracle.requestsprocessor.application;

import com.algorand.algosdk.v2.client.model.Transaction;
import com.example.algorand.oracle.requestsprocessor.domain.*;
import com.example.algorand.oracle.requestsprocessor.repository.OracleLastRoundRepository;
import com.example.algorand.oracle.requestsprocessor.repository.OracleRequestRepository;
import com.example.algorand.oracle.requestsprocessor.repository.OracleResponseRepository;
import com.example.algorand.oracle.requestsprocessor.utils.AlgorandHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OracleService {

    private final OracleRequestRepository oracleRequestRepository;
    private final OracleResponseRepository oracleResponseRepository;
    private final OracleLastRoundRepository oracleLastRoundRepository;

    @Transactional
    public void createOracleRequests(Collection<Transaction> oracleRequests) {
        oracleRequestRepository.saveAll(
                oracleRequests.stream().map(t ->
                            OracleRequest.builder().callerAddress(t.sender)
                                    .callbackApplicationId(AlgorandHelper.toLong(t.applicationTransaction.applicationArgs.get(2)))
                                    .creationTime(LocalDateTime.now())
                                    .marketRequested(SupportedMarket.fromMarketName(
                                            AlgorandHelper.decodeToString(t.applicationTransaction.applicationArgs.get(1))))
                                    .status(TransactionProcessingStatus.CREATED)
                                    .callbackMethod(AlgorandHelper.decodeToString(t.applicationTransaction.applicationArgs.get(3)))
                                    .build()
                    ).collect(Collectors.toList())
                );
    }

    @Transactional
    public void updateOracleRequest(OracleRequest oracleRequest) {
        oracleRequestRepository.save(oracleRequest);
    }

    @Transactional
    public OracleResponse createOracleResponse(OracleRequest oracleRequest, String txId, Integer returnedValue) {
        return oracleResponseRepository.save(OracleResponse.builder()
                .oracleRequestId(oracleRequest.getId())
                .callbackTime(LocalDateTime.now())
                .marketValueReturned(returnedValue)
                .callBackTransactionHash(txId).
                build());
    }

    @Transactional(readOnly = true)
    public Long getLastProcessedRound() {
        Optional<OracleLastRound> lastRound = oracleLastRoundRepository.findById(1);
        if (lastRound.isPresent()) {
            return lastRound.get().getLastProcessedRound();
        }
        return 0L;
    }

    @Transactional
    public void updateLastProcessedRound(Long lastProcessedRound) {
        Optional<OracleLastRound> lastRound = oracleLastRoundRepository.findById(1);
        if (lastRound.isPresent()) {
            lastRound.get().setLastProcessedRound(lastProcessedRound);
        } else {
            OracleLastRound o = new OracleLastRound();
            o.setLastProcessedRound(lastProcessedRound);
            o.setId(1);
            oracleLastRoundRepository.save(o);
        }
    }
}
