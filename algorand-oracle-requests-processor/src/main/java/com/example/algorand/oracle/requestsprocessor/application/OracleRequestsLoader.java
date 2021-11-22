package com.example.algorand.oracle.requestsprocessor.application;

import com.algorand.algosdk.v2.client.common.AlgodClient;
import com.algorand.algosdk.v2.client.common.IndexerClient;
import com.algorand.algosdk.v2.client.common.Response;
import com.algorand.algosdk.v2.client.indexer.SearchForTransactions;
import com.algorand.algosdk.v2.client.model.Enums;
import com.algorand.algosdk.v2.client.model.Transaction;
import com.algorand.algosdk.v2.client.model.TransactionsResponse;
import com.example.algorand.oracle.requestsprocessor.domain.SupportedMarket;
import com.example.algorand.oracle.requestsprocessor.exceptions.AlgorandNetworkException;
import com.example.algorand.oracle.requestsprocessor.utils.AlgorandHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OracleRequestsLoader {
    private static byte[] NOTE_PREFIX = "algo-oracle-app-4".getBytes(StandardCharsets.UTF_8);

    private final IndexerClient indexerClient;
    private final AlgodClient algodClient;
    private final OracleService oracleService;

    @Value("${algorand.contract.oracle.id}")
    private Long applicationId;

    @Scheduled(fixedDelay = 4500)
    public void triggerRequestLoading() {
        log.info("Starting loading oracle requests from blockchain");
        Long lastRound = oracleService.getLastProcessedRound();
        log.info("Last round processed was {}", lastRound);
        lastRound = loadOracleRequestsSentToApplicationFromRound(lastRound + 1);
        log.info("Loaded new requests until round {}", lastRound);
        oracleService.updateLastProcessedRound(lastRound);
    }

    public long loadOracleRequestsSentToApplicationFromRound( long round) {
        String nextToken = "";
        long lastIndexedRound = 0;
        try {
            lastIndexedRound = this.indexerClient.makeHealthCheck().execute().body().round;
        } catch (Exception e) {
            throw new AlgorandNetworkException("could not get last indexed round", e);
        }
        while (nextToken != null) {
            SearchForTransactions searchForTransactions = this.indexerClient.searchForTransactions()
                    .notePrefix(NOTE_PREFIX)
                    .minRound(round)
                    .maxRound(lastIndexedRound);
            if (nextToken != null) {
                searchForTransactions.next(nextToken);
            }
            Response<TransactionsResponse> response = null;
            try {
                response = searchForTransactions.execute();
            } catch (Exception e) {
                throw new AlgorandNetworkException("An unexpected error occured while trying to read transactions on the blockchain", e);
            }
            if (!response.isSuccessful()) {
                throw new AlgorandNetworkException(response.message());
            }
            TransactionsResponse transactionsResponse = response.body();
            List<Transaction> oracleRequests = transactionsResponse.transactions.stream()
                    .filter(this::isSupportedTransaction)
                    .collect(Collectors.toList());
            oracleService.createOracleRequests(oracleRequests);

            if (StringUtils.isNotBlank(transactionsResponse.nextToken)) {
                nextToken = transactionsResponse.nextToken;
            } else {
                nextToken = null;
            }
        }
        return lastIndexedRound;
    }

    private boolean isSupportedTransaction(Transaction transaction) {

        boolean supported =
                // the transaction is a noop application call
                transaction.txType.equals(Enums.TxType.APPL)
                && transaction.applicationTransaction.onCompletion.equals(Enums.OnCompletion.NOOP)
                // we check that the application called is our smart contract
                && transaction.applicationTransaction.applicationId.equals(applicationId)
                // we expect 4 arguments
                && transaction.applicationTransaction.applicationArgs().size() == 4
                // the first one must be "get_market_exchange_rate"
                && AlgorandHelper.decodeToString(transaction.applicationTransaction.applicationArgs.get(0)).equals("get_market_exchange_rate")
                // the second one must be a currencies pair supported by the oracle
                && SupportedMarket.fromMarketName(AlgorandHelper.decodeToString(transaction.applicationTransaction.applicationArgs.get(1))) != null
                // the third one should be the callback application id (a long value)
                && AlgorandHelper.toLong(transaction.applicationTransaction.applicationArgs.get(2)) != null
                //and finally, we expect a String value to send back later as first argument to the callback application
                && StringUtils.isNotBlank(AlgorandHelper.decodeToString(transaction.applicationTransaction.applicationArgs.get(3)));

        log.info("Found " + (supported ? "": "not" ) + " supported transaction {}", transaction);
        return supported;
    }


}
