package com.hurence.logisland.processor;

import com.hurence.logisland.component.PropertyDescriptor;
import com.hurence.logisland.record.Field;
import com.hurence.logisland.record.FieldType;
import com.hurence.logisland.record.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;


public class EncryptField extends AbstractProcessor {

    private static final long serialVersionUID = -270933070438408174L;

    private static final Logger logger = LoggerFactory.getLogger(ModifyId.class);


    public static final String ENCRYPT_MODE = "Encrypt";
    public static final String DECRYPT_MODE = "Decrypt";
    public static final String AES = "AES";
    public static final String DES = "DES";

    public static final PropertyDescriptor MODE = new PropertyDescriptor.Builder()
            .name("Mode")
            .description("Specifies whether the content should be encrypted or decrypted")
            .required(true)
            .allowableValues(ENCRYPT_MODE, DECRYPT_MODE)
            .defaultValue(ENCRYPT_MODE)
            .build();

    public static final PropertyDescriptor ALGO = new PropertyDescriptor.Builder()
            .name("Algo")
            .description("Specifies the algorithm that the cipher will use")
            .required(true)
            .allowableValues(AES, DES)
            .defaultValue(AES)
            .build();

    public static final PropertyDescriptor KEY = new PropertyDescriptor.Builder()
            .name("Key")
            .description("Specifies the key to use")
            .required(true)
            .defaultValue("azerty1234567890")
            .build();

    @Override
    public void init(final ProcessContext context) {
        List<PropertyDescriptor> properties = new ArrayList<>();
        properties.add(MODE);
        properties.add(ALGO);
        properties.add(KEY);

        properties = Collections.unmodifiableList(properties);
    }

    @Override
    public List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        List<PropertyDescriptor> properties = new ArrayList<>();
        properties.add(MODE);
        properties.add(ALGO);
        properties.add(KEY);

        return Collections.unmodifiableList(properties);

    }

    public static boolean isAESAlgorithm(final String algorithm) {
        return algorithm.startsWith("A");
    }

    public class ExempleAES {

        private static final String ALGO ="AES";
        private byte[] keyValue;

        public ExempleAES(String key) {
            keyValue = key.getBytes();
        }

        public String encrypt (String Data) throws Exception{
            Key key = generateKey();
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.ENCRYPT_MODE, key);
            byte[] encVal = c.doFinal(Data.getBytes());
            String encryptedValue = new BASE64Encoder().encode(encVal);
            return  encryptedValue;
        }

        public String decrypt (String encryptedData) throws  Exception {
            Key key = generateKey();
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.DECRYPT_MODE, key);
            byte[] decodedValue = new BASE64Decoder().decodeBuffer(encryptedData);
            byte[] decValue = c.doFinal(decodedValue);
            String decryptedValue = new String(decValue);
            return decryptedValue;
        }

        private Key generateKey() throws Exception {
            Key key = new SecretKeySpec(keyValue, ALGO);
            return key;
        }

    }


    public class ExempleDES {

        private static final String UNICODE_FORMAT = "UTF8";
        public static final String DES_ENCRYPTION_SHEME = "DES";
        private KeySpec myKeySpec;
        private SecretKeyFactory mySecretKeyFactory;
        private Cipher cipher;
        byte[] keyAsBytes;
        private String myEncryptionKey;
        private String myEncryptionScheme;
        SecretKey key;

        public ExempleDES(String myEncKey) throws Exception {
            myEncryptionKey = myEncKey;
            myEncryptionScheme = DES_ENCRYPTION_SHEME;
            keyAsBytes = myEncryptionKey.getBytes(UNICODE_FORMAT);
            myKeySpec = new DESKeySpec(keyAsBytes);
            mySecretKeyFactory = SecretKeyFactory.getInstance(myEncryptionScheme);
            cipher = Cipher.getInstance(myEncryptionScheme);
            key = mySecretKeyFactory.generateSecret(myKeySpec);
        }

        public String encrypt (String unencryptedString) {
            String encryptedString = null;
            try {
                cipher.init(Cipher.ENCRYPT_MODE, key);
                byte[] plainText = unencryptedString.getBytes(UNICODE_FORMAT);
                byte[] encryptedText = cipher.doFinal(plainText);
                BASE64Encoder base64encoder = new BASE64Encoder();
                encryptedString= base64encoder.encode(encryptedText);
            } catch (InvalidKeyException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException e) {
            }
            return encryptedString;
        }

        public String decrypt (String encryptedString) {
            String decryptedText = null;
            try{
                cipher.init(Cipher.DECRYPT_MODE, key);
                BASE64Decoder base64decoder = new BASE64Decoder();
                byte[] encrypedText = base64decoder.decodeBuffer(encryptedString);
                byte[] plainText = cipher.doFinal(encrypedText);
                decryptedText = bytes2String(plainText);

            } catch (InvalidKeyException | IOException | IllegalBlockSizeException | BadPaddingException e) {
            }
            return decryptedText;
        }

        private String bytes2String(byte[] bytes) {
            StringBuilder stringBuffer = new StringBuilder();
            for (int i=0; i < bytes.length; i++) {
                stringBuffer.append((char) bytes[i]);
            }
            return stringBuffer.toString();
        }
    }

    


    @Override
    public Collection<Record> process(ProcessContext context, Collection<Record> records) {
        final boolean encrypt = context.getPropertyValue(MODE).toString().equalsIgnoreCase(ENCRYPT_MODE);
        try {
            init(context);
        } catch (Throwable t) {
            logger.error("error while initializing", t);
        }

        try {
            Collection<Field> allfieldsToEncrypt;

            for (Record record : records) {
                allfieldsToEncrypt = record.getAllFields();

                for (Field field : allfieldsToEncrypt) {

                    if (isAESAlgorithm(context.getProperty(ALGO))) {
                        ExempleAES encryptAES = new ExempleAES(context.getProperty(KEY));
                        if (encrypt) {
                            record.setField(field.getName(), FieldType.STRING, encryptAES.encrypt(field.asString()));
                        } else {
                            record.setField(field.getName(), FieldType.STRING, encryptAES.decrypt(field.asString()));
                        }

                    } else {
                        ExempleDES encryptDES = new ExempleDES(context.getProperty(KEY));
                        if (encrypt) {
                            record.setField(field.getName(), FieldType.STRING, encryptDES.encrypt(field.asString()));
                        } else {
                            record.setField(field.getName(), FieldType.STRING, encryptDES.decrypt(field.asString()));
                        }
                    }
                }
            }
        } catch (Throwable t) {
            logger.error("error while setting id for records", t);
        }
        return records;
    }
}
