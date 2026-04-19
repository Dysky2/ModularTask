package net.edu.modulartask.auth;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.stereotype.Service;
import io.github.cdimascio.dotenv.Dotenv;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class TwoFactorService {


    public GoogleAuthenticatorKey generate2FaKey() {
        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();

        return googleAuthenticator.createCredentials();
    }

    public SecretKey getMasterKey() {
        Dotenv dotenv = Dotenv.load();

        String masterKey = dotenv.get("TWOFA_KEY");

        if (masterKey == null) {
            throw new RuntimeException("Master key is null");
        }

        byte[] decodedKey = Base64.getDecoder().decode(masterKey);

        return new SecretKeySpec(decodedKey, "AES");
    }

    public String encrypt(String plainText) throws Exception {
        if (plainText == null) {
            return null;
        }

        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, getMasterKey(), spec);

        byte[] encryptedData = cipher.doFinal(plainText.getBytes());

        String ivBase64 = Base64.getEncoder().encodeToString(iv);
        String dataBase64 = Base64.getEncoder().encodeToString(encryptedData);

        return ivBase64 + ":" + dataBase64;
    }


    public String decrypt(String encryptedData) throws Exception {
        String[] encryptedKeys = encryptedData.split(":");

        if (encryptedKeys.length != 2) {
            throw new IllegalArgumentException("Bledny format");
        }

        byte[] iv = Base64.getDecoder().decode(encryptedKeys[0]);
        byte[] cipherText = Base64.getDecoder().decode(encryptedKeys[1]);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, getMasterKey(), spec);

        byte[] plainText = cipher.doFinal(cipherText);

        return new String(plainText);
    }


    public String generateQrCodeBase64(String otpAuthUrl) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            BitMatrix bitMatrix = qrCodeWriter.encode(otpAuthUrl, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

            return Base64.getEncoder().encodeToString(pngOutputStream.toByteArray());

        } catch(Exception e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

}
