package com.example.algorand.oracle.requestsprocessor.application;

import com.algorand.algosdk.account.Account;
import com.algorand.algosdk.transaction.SignedTransaction;
import com.algorand.algosdk.transaction.Transaction;
import com.algorand.algosdk.util.Encoder;
import com.algorand.algosdk.v2.client.common.AlgodClient;
import com.algorand.algosdk.v2.client.common.Response;
import com.algorand.algosdk.v2.client.model.Application;
import com.algorand.algosdk.v2.client.model.PendingTransactionResponse;
import com.algorand.algosdk.v2.client.model.PostTransactionsResponse;
import com.algorand.algosdk.v2.client.model.TransactionParametersResponse;
import com.example.algorand.oracle.requestsprocessor.domain.OracleRequest;
import com.example.algorand.oracle.requestsprocessor.domain.OracleResponse;
import com.example.algorand.oracle.requestsprocessor.domain.TransactionProcessingStatus;
import com.example.algorand.oracle.requestsprocessor.exceptions.AlgorandCallbackProcessingException;
import com.example.algorand.oracle.requestsprocessor.exceptions.AlgorandNetworkException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
@Slf4j
public class OracleRequestsProcessor implements Runnable {

    private final OracleRequest request;
    private final AlgodClient algodClient;
    private final OracleService oracleService;
    private final Account callbackSender;

    @Override
    public void run() {

        checkApplicationValidity();
        Integer exchangeRate = randomExchangeRate(); // should be expressed in micro algo
        List<byte[]> callbackArgs = buildCallbackArguments(exchangeRate);

        try {
            TransactionParametersResponse suggestedParams = algodClient.TransactionParams().execute().body();
            Transaction callbackTransaction = Transaction.ApplicationCallTransactionBuilder()
                    .sender(callbackSender.getAddress())
                    .suggestedParams(suggestedParams)
                    .applicationId(request.getCallbackApplicationId())
                    .args(callbackArgs)
                    .build();
            SignedTransaction signedTransaction = callbackSender.signTransaction(callbackTransaction);
            byte[] bytes = Encoder.encodeToMsgPack(signedTransaction);

            Response<PostTransactionsResponse> execute = algodClient.RawTransaction().rawtxn(bytes).execute();
            if (!execute.isSuccessful()) {
                String errMsg = String.format("A problem occurred while trying to call back app %d %s",
                        request.getCallbackApplicationId(), execute);
                setRequestFailedStatus(errMsg);
                throw new AlgorandCallbackProcessingException(errMsg);
            }
            waitForConfirmation(execute.body().txId);
            handleRequestSuccess(execute.body().txId, exchangeRate);

        } catch (Exception e) {
            throw new AlgorandCallbackProcessingException("and unexpected error occured ", e);
        }

    }

    private List<byte[]> buildCallbackArguments(Integer exchangeRate) {
        return Arrays.asList(
                request.getCallbackMethod().getBytes(StandardCharsets.UTF_8),
                request.getMarketRequested().getMarket().getBytes(StandardCharsets.UTF_8),
                ByteBuffer.allocate(4).putInt(exchangeRate).array());
    }

    private void checkApplicationValidity() {
        setRequestStatus(TransactionProcessingStatus.PROCESSING);
        try {
            Response<Application> getApplicationResponse = algodClient.GetApplicationByID(request.getCallbackApplicationId()).execute();
            if (!getApplicationResponse.isSuccessful()) {
                String errMsg = String.format("A problem occurred while trying to get application info for %d %s",
                        request.getCallbackApplicationId(), getApplicationResponse);
                setRequestFailedStatus(errMsg);
                throw new AlgorandCallbackProcessingException(errMsg);
            }
        } catch (Exception e) {
            setRequestFailedStatus(e.getMessage());
            throw new AlgorandCallbackProcessingException("Unable to get application info for " + request.getCallbackApplicationId(),
                    e);
        }
    }

    private void setRequestFailedStatus(String errMsg) {
        request.setProcessingErrorMessage(errMsg);
        setRequestStatus(TransactionProcessingStatus.FAILED);
    }

    private void setRequestStatus(TransactionProcessingStatus status) {
        request.setStatus(status);
        request.setProcessTime(LocalDateTime.now());
        oracleService.updateOracleRequest(request);
    }

    private void handleRequestSuccess(String txId, Integer exchangeRate) {
        OracleResponse oracleResponse = oracleService.createOracleResponse(request, txId, exchangeRate);
        request.setOracleResponseId(oracleResponse.getId());
        setRequestStatus(TransactionProcessingStatus.DONE);
    }

    private Integer randomExchangeRate() {
        return 1000000 + ((new Random().nextInt(100) + 1) * 1000);
    }

    public void waitForConfirmation(String txID) throws Exception {
        Long lastRound = algodClient.GetStatus().execute().body().lastRound;
        long waitUntilRound = lastRound + 10;
        while (lastRound <= waitUntilRound) {
            try {
                // Check the pending transactions
                Response<PendingTransactionResponse> pendingInfo = algodClient.PendingTransactionInformation(txID).execute();
                if (pendingInfo.body().confirmedRound != null && pendingInfo.body().confirmedRound > 0) {
                    // Got the completed Transaction
                    log.info(
                            "Transaction " + txID + " confirmed in round " + pendingInfo.body().confirmedRound);
                    break;
                }
                lastRound++;
                algodClient.WaitForBlock(lastRound).execute();
            } catch (Exception e) {
                throw (e);
            }
        }
        if (lastRound > waitUntilRound) {
            throw new AlgorandNetworkException("The transaction " + txID + " could not be confirmed");
        }
    }
}
