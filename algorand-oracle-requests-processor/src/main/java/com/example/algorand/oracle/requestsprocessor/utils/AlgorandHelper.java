package com.example.algorand.oracle.requestsprocessor.utils;

import com.algorand.algosdk.crypto.Address;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class AlgorandHelper {
    private AlgorandHelper(){}

    public static final boolean isValidAddress(String address) {
        try {
            new Address(address);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Long toLong(byte[] value) {
        try {
            return ByteBuffer.wrap(value).getLong();
        } catch (Exception n) {
            return null;
        }
    }

    public static String decodeToString(byte[] value) {
        return StringUtils.toEncodedString(value, StandardCharsets.UTF_8);
    }
}
