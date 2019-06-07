package com.hurence.logisland.processor;

import com.hurence.logisland.annotation.behavior.DynamicProperty;
import com.hurence.logisland.component.AllowableValue;
import com.hurence.logisland.component.PropertyDescriptor;
import com.hurence.logisland.record.Field;
import com.hurence.logisland.record.FieldType;
import com.hurence.logisland.record.Record;
import com.hurence.logisland.validator.StandardValidators;
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
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


@DynamicProperty(name = "field to encrypt",
        supportsExpressionLanguage = true,
        value = "a default value",
        description = "encrypt the field value")


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



    private Collection<String> fieldTypes = null;

    @Override
    public void init(final ProcessContext context) {
        List<PropertyDescriptor> properties = new ArrayList<>();
        properties.add(MODE);
        properties.add(ALGO);
        properties.add(KEY);

        properties = Collections.unmodifiableList(properties);
        fieldTypes = getFieldsNameMapping(context);
    }

    @Override
    public List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        List<PropertyDescriptor> properties = new ArrayList<>();
        properties.add(MODE);
        properties.add(ALGO);
        properties.add(KEY);

        return Collections.unmodifiableList(properties);

    }

    @Override
    protected PropertyDescriptor getSupportedDynamicPropertyDescriptor(final String propertyDescriptorName) {
        return new PropertyDescriptor.Builder()
                .name(propertyDescriptorName)
                .expressionLanguageSupported(false)  // TODO understand what expressionLanguage is !!!
                .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
                .required(false)
                .dynamic(true)
                .build();
    }

    // check if the algorithm chosen is AES, otherwaie it is DES
    public static boolean isAESAlgorithm(final String algorithm) {
        return algorithm.startsWith("A");
    }

    // encrpyt or decript data with AES algo
    //if encrypt: input object / output byte[]
    //if decrypt: input field (the field will be in type FieldType.BYTES) / output object
    public class ExempleAES {

        private static final String ALGO ="AES";
        private byte[] keyValue;

        public ExempleAES(String key) {
            keyValue = key.getBytes();
        }

        public byte[] encrypt (Object Data) throws Exception{
            Key key = generateKey();
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.ENCRYPT_MODE, key);
            byte[] encVal = c.doFinal(toByteArray(Data));
            return  encVal;
        }

        public Object decrypt (Field encryptedData) throws  Exception {
            Key key = generateKey();
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.DECRYPT_MODE, key);
            byte[] encryptedDataBytes = toByteArray(encryptedData);
            byte[] decValue = c.doFinal(encryptedDataBytes);
            Object decryptedValue = toObject(decValue);
            return decryptedValue;
        }

        private Key generateKey() throws Exception {
            Key key = new SecretKeySpec(keyValue, ALGO);
            return key;
        }

    }

    // encrpyt or decript data with DES algo
    //if encrypt: input object / output byte[]
    //if decrypt: input field (the field will be in type FieldType.BYTES) / output object
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

        public byte[] encrypt (Object unencryptedString) {
            byte[] encryptedText = null;
            try {
                cipher.init(Cipher.ENCRYPT_MODE, key);
                byte[] plainText = toByteArray(unencryptedString);
                encryptedText = cipher.doFinal(plainText);
            } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | IOException  e) {
            }
            return encryptedText;
        }

        public Object decrypt (Field encryptedString) {
            Object decryptedText = null;
            try{
                cipher.init(Cipher.DECRYPT_MODE, key);
                byte[] encryptedStringBytes = toByteArray(encryptedString);
                byte[] plainText = cipher.doFinal(encryptedStringBytes);
                decryptedText = toObject(plainText);

            } catch (InvalidKeyException | IOException | IllegalBlockSizeException | BadPaddingException | ClassNotFoundException e) {
            }
            return decryptedText;
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
            Collection<Field> allfieldsToEncrypt = null;
            Collection<String> allfieldsToEncrypt_InString ;

            for (Record record : records) {
                // check if user choose some specific field or fields to encrypt, if don't we'll encrypt all fields.
                if (getFieldsNameMapping(context) == null) {
                    allfieldsToEncrypt = record.getAllFields();
                } else {
                    allfieldsToEncrypt_InString = getFieldsNameMapping(context);
                    for (String name : allfieldsToEncrypt_InString) {
                        allfieldsToEncrypt.add(record.getField(name));
                        /*final Object inputDateValue = context.getPropertyValue(name).evaluate(record);*/ // idea ! : we can take objects as input not fields!
                    }

                }

                for (Field field : allfieldsToEncrypt) {

                    if (isAESAlgorithm(context.getProperty(ALGO))) {
                        ExempleAES encryptAES = new ExempleAES(context.getProperty(KEY));
                        if (encrypt) {
                            record.setField(field.getName(), FieldType.BYTES, encryptAES.encrypt(field)); // is field an Object ??!!
                        } else {
                            record.setField(field.getName(), field.getType(), encryptAES.decrypt(field)); // !!!!!!!!!!! how to know the original type of the field before encrypting
                        }

                    } else {
                        ExempleDES encryptDES = new ExempleDES(context.getProperty(KEY));
                        if (encrypt) {
                            record.setField(field.getName(), FieldType.BYTES, encryptDES.encrypt(field));
                        } else {
                            record.setField(field.getName(), FieldType.STRING, encryptDES.decrypt(field));
                        }
                    }
                }

            }
        } catch (Throwable t) {
            logger.error("error while setting id for records", t);
        }
        return records;
    }
    // convert objects to byte array
    public static byte[] toByteArray(Object obj) throws IOException {
        byte[] bytes ;
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
        } finally {
            if (oos != null) {
                oos.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
        return bytes;
    }
    // convert byte array to object
    public static Object toObject(byte[] bytes) throws IOException, ClassNotFoundException {
        Object obj = null;
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            bis = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bis);
            obj = ois.readObject();
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (ois != null) {
                ois.close();
            }
        }
        return obj;
    }
    // get the specific field or fields to encrypt form the dynamic property
    private Collection<String> getFieldsNameMapping(ProcessContext context) {
        Collection<String> fieldsNameMappings = null;
        for (final Map.Entry<PropertyDescriptor, String> entry : context.getProperties().entrySet()) {
            if (!entry.getKey().isDynamic()) {
                continue;
            }
            final String fieldName = entry.getKey().getName();
            fieldsNameMappings.add(fieldName);
        }
        return fieldsNameMappings;
    }
}
