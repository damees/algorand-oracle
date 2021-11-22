package com.example.algorand.oracle.requestsprocessor.config;

import com.algorand.algosdk.account.Account;
import com.algorand.algosdk.v2.client.common.AlgodClient;
import com.algorand.algosdk.v2.client.common.IndexerClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.security.GeneralSecurityException;

@Configuration
public class AlgorandOracleConfiguration {

    @Value("${algorand.node.indexer.url}")
    private String indexerUrl;

    @Value("${algorand.node.indexer.port}")
    private int indexerPort;

    @Value("${algorand.node.algod.url}")
    private String algodUrl;

    @Value("${algorand.node.algod.port}")
    private int algodPort;

    @Value("${algorand.node.algod.token}")
    private String algodToken;

    @Value("${algorand.account.oracle.callback.mnemonic:}")
    private String callbackAccountMnemonic;

    @Bean
    public IndexerClient indexerClient() {
        return new IndexerClient(indexerUrl, indexerPort, algodToken);
    }

    @Bean
    public AlgodClient agodClient() {
        return new AlgodClient(algodUrl, algodPort, algodToken);
    }

    @Bean
    public Account oracleCallbackSender() {
        try {
            if (StringUtils.isBlank(callbackAccountMnemonic)) {
                return new Account();
            }
            return new Account(callbackAccountMnemonic);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public TaskExecutor oracleCallbackExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setAllowCoreThreadTimeOut(true);
        return executor;
    }
}
