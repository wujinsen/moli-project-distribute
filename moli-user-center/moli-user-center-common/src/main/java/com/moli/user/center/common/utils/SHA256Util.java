package com.moli.user.center.common.utils;

import org.apache.shiro.crypto.hash.SimpleHash;

public class SHA256Util {

    private SHA256Util() {
    }

    public static final String HASH_ALGORITHM_NAME = "SHA-256";
    public static final int HASH_ITERATIONS = 15;
    public static final String SALT = "moli";

    public static String sha256(String password, String salt) {
        return new SimpleHash(HASH_ALGORITHM_NAME, password, salt, HASH_ITERATIONS).toString();
    }
}
