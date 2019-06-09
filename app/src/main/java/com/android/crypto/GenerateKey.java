package com.android.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.EllipticCurve;

public class GenerateKey {
    ECPublicKey pubKey;
    ECPrivateKey privKey;
    public GenerateKey() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {

        String curveName = "secp256k1";
        ECGenParameterSpec ecGenSpec = new ECGenParameterSpec(curveName);

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(ecGenSpec, new SecureRandom());
        KeyPair pair = keyPairGenerator.generateKeyPair();
        privKey = (ECPrivateKey) pair.getPrivate();
        pubKey = (ECPublicKey) pair.getPublic();
    }

    public GenerateKey(String curveName) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {


        ECGenParameterSpec ecGenSpec = new ECGenParameterSpec(curveName);

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(ecGenSpec, new SecureRandom());
        KeyPair pair = keyPairGenerator.generateKeyPair();
        privKey = (ECPrivateKey) pair.getPrivate();
        pubKey = (ECPublicKey) pair.getPublic();
    }
    public String getPublicKeyX(){
        return pubKey.getW().getAffineX().toString();
    }
    public String getPublicKeyY(){
        return pubKey.getW().getAffineY().toString();
    }
    public String getPrivateKeyS(){
        return    privKey.getS().toString();
    }

    public ECPrivateKey getPrivKey() {
        return privKey;
    }

    public ECPublicKey getPubKey() {
        return pubKey;
    }


}
