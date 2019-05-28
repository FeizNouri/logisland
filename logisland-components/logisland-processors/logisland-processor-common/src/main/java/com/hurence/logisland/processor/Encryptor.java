package com.hurence.logisland.processor;

public interface Encryptor {
    StreamCallback getEncryptionCallback() throws Exception;

    StreamCallback getDecryptionCallback() throws Exception;
}
