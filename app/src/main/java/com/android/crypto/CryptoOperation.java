package com.android.crypto;


import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidParameterException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.*;

public class CryptoOperation {

    public static ECPublicKey getPublicKeyFromPrivate(final ECPrivateKey pk) throws GeneralSecurityException {
        final ECParameterSpec params = pk.getParams();
        final ECPoint w = ECUtil.scalarMult(params.getCurve(), params.getGenerator(), pk.getS());
        final KeyFactory kg = KeyFactory.getInstance("EC");
        return (ECPublicKey) kg.generatePublic(new ECPublicKeySpec(w, params));
    }

    public static ECPublicKey getPublicKeyFromPrivate(final BigInteger s, ECParameterSpec params) throws GeneralSecurityException {

        final ECPoint w = ECUtil.scalarMult(params.getCurve(), params.getGenerator(), s);
        final KeyFactory kg = KeyFactory.getInstance("EC");
        return (ECPublicKey) kg.generatePublic(new ECPublicKeySpec(w, params));
    }

    public static ECPublicKey getPublicKeyFromCoord(BigInteger x, BigInteger y, ECParameterSpec ECP) throws GeneralSecurityException {
        ECPoint ecPoint = new ECPoint(x, y);
        ECPublicKeySpec keySpec = new ECPublicKeySpec(ecPoint, ECP);
        KeyFactory kg = KeyFactory.getInstance("EC");
        ECPublicKey publicKey = (ECPublicKey) kg.generatePublic(keySpec);
        return publicKey;
    }

    public static ECPublicKey getPublicKeyFromCoord(ECPoint ecPoint,ECParameterSpec ECP) throws GeneralSecurityException {

        ECPublicKeySpec keySpec = new ECPublicKeySpec(ecPoint, ECP);
        KeyFactory kg = KeyFactory.getInstance("EC");
        ECPublicKey publicKey = (ECPublicKey)kg.generatePublic(keySpec);
        return publicKey;
    }




    public static BigInteger[] calcCiph(ECParameterSpec ecSpec, BigInteger[] pairPM, ECPoint kPK)throws InvalidParameterSpecException {

        EllipticCurve ec = ecSpec.getCurve();
        BigInteger pr = ECUtil.getModulus(ec);
        BigInteger a = ec.getA();
        BigInteger x1 = pairPM[0];
        BigInteger y1 = pairPM[1];
        BigInteger x2 = kPK.getAffineX();
        BigInteger y2 = kPK.getAffineY();
        ECPoint p1 = new ECPoint(x1,y1);
        ECPoint p2 = new ECPoint(x2,y2);
        ECPoint st = ECUtil.addPoint(pr,a,p1,p2);


        BigInteger nS = st.getAffineX();
        BigInteger nT = st.getAffineY();

        BigInteger[] pairPC=new BigInteger[2];
        pairPC[0]=nS;pairPC[1]=nT;
        return pairPC;
    }

    public static BigInteger[] calcDeciph(ECParameterSpec ecSpec,BigInteger[] pairPC, ECPoint kPK)throws InvalidParameterSpecException{

        EllipticCurve ec = ecSpec.getCurve();
        BigInteger pr = ECUtil.getModulus(ec);
        BigInteger a = ec.getA();
        BigInteger x1 = pairPC[0];
        BigInteger y1 = pairPC[1];
        BigInteger x2 = kPK.getAffineX();
        BigInteger y2 = kPK.getAffineY().negate();
        ECPoint p1 = new ECPoint(x1,y1);
        ECPoint p2 = new ECPoint(x2,y2);
        ECPoint p3 = ECUtil.addPoint(pr,a,p1,p2);

        BigInteger cC = p3.getAffineX();
        BigInteger dD = p3.getAffineY();
        BigInteger[] x1y1=new BigInteger[2];
        x1y1[0]=cC;x1y1[1]=dD;
        return x1y1;
    }

    public static MessageData msgEncrypt(String input, ECParameterSpec ECP, ECPublicKey pubKey) throws InvalidParameterSpecException {

        EllipticCurve curve = ECP.getCurve();
        ECField fieldCheck = curve.getField();
        ECFieldFp pField;
        if (fieldCheck instanceof ECFieldFp) {
            pField = (ECFieldFp)fieldCheck;
        } else {
            throw new InvalidParameterSpecException("Only curves over prime order fields are supported");
        }
        BigInteger p = pField.getP();
        BigInteger N = ECP.getOrder();
        ECPoint G = ECP.getGenerator();

        BigInteger bigTen = new BigInteger("10");
        //BigInteger k = MathUtil.randomBig((N.divide(bigTen.pow(65))).subtract(BigInteger.valueOf(1)));
        BigInteger k = BigInteger.valueOf(1870073435);
        int groupSize = (MathUtil.findNumberOfDigits(p.doubleValue(), 65536)-1);

        ECPoint kGpoint = ECUtil.scalarMult(curve,G,k);

        BigInteger kGX = kGpoint.getAffineX(); BigInteger kGY = kGpoint.getAffineY();
        BigInteger kG[] = {kGX,kGY};

        ECPoint kPK = ECUtil.scalarMult(curve,pubKey.getW(),k);
        int[] asciiVal = MathUtil.textToAsciiVal(input);
        int groupArr[][] = ArrUtil.splitArray(asciiVal,groupSize);

        BigInteger[] asciiBig;
        int ln;
        if(((groupArr.length)&1)==0){
            ln = groupArr.length;
            asciiBig = new BigInteger[ln];}
        else {ln = groupArr.length+1;
            asciiBig = new BigInteger[ln];asciiBig[groupArr.length]=BigInteger.valueOf(32);}

        for (int j = 0; j < groupArr.length; j++)
        {

            asciiBig[j]=MathUtil.convertAsBaseToDec(groupArr[j],65536);

        }

        BigInteger pairPM[][] = ArrUtil.splitArray(asciiBig,2);
        BigInteger pairPC [][]=pairPM;

        for (int i = 0; i < pairPM.length; i++)
        {
            pairPC[i]= calcCiph(ECP,pairPM[i],kPK);
        }

        String pairStr[][]=new String[pairPC.length][pairPC[0].length];

        for (int j = 0; j < pairStr.length; j++){
            for (int i = 0; i < pairStr[j].length; i++){
                pairStr[j][i] = pairPC[j][i].toString();
            }
        }
        String[] oneD = ArrUtil.TwoToOne(pairStr);

        MessageData cipherText = new MessageData(kG,oneD);

        return cipherText;
    }

    public static String msgDecrypt (MessageData cipherMsg, ECParameterSpec ECP, ECPrivateKey privKey) throws InvalidParameterSpecException {
        EllipticCurve curve = ECP.getCurve();

        ECField fieldCheck = curve.getField();
        ECFieldFp pField;
        if (fieldCheck instanceof ECFieldFp) {
            pField = (ECFieldFp)fieldCheck;
        } else {
            throw new InvalidParameterSpecException("Only curves over prime order fields are supported");
        }
        BigInteger p = pField.getP();

        BigInteger kgX =new BigInteger(cipherMsg.decapKG()[0]);BigInteger kgY = new BigInteger(cipherMsg.decapKG()[1]);
        ECPoint kG = new ECPoint(kgX,kgY);

        ECPoint kPK = ECUtil.scalarMult(curve,kG,privKey.getS());
        int groupSize = (MathUtil.findNumberOfDigits(p.doubleValue(), 65536)-1);

        String deciphStr[][] = ArrUtil.splitArray(cipherMsg.decapPC(),2);

        BigInteger[][] deciph = new BigInteger[deciphStr.length][deciphStr[0].length];

        for (int j = 0; j < deciphStr.length; j++){
            for (int i = 0; i < deciphStr[j].length; i++){
                deciph[j][i] = new BigInteger(deciphStr[j][i]);}
        }

        BigInteger[][] deciphResult = deciph;
        BigInteger[] deciphList = new BigInteger[(deciph.length)*2];

        for (int i = 0; i < deciphResult.length; i++) {

            deciphResult[i] = calcDeciph(ECP, deciph[i], kPK);
        }

        int ii=0;
        for (int i = 0; i < deciphResult.length; i++)
        {
            deciphList[i+ii]=deciph[i][0];
            deciphList[i+ii+1]=deciph[i][1];

            ii++;
        }

        int[][] asciiValArray= new int[(deciphList.length)][];

        for (int i = 0; i < deciphList.length; i++) {

            asciiValArray[i] = MathUtil.convertDecToBase(deciphList[i], 65536);

        }
        String stringList[]= new String[asciiValArray.length*2];

        for (int i = 0; i < asciiValArray.length; i++) {

            char[] charArr = new char[asciiValArray[i].length] ;
            for (int j = 0; j < asciiValArray[i].length; j++){
                charArr[j]=(char)asciiValArray[i][j];
            }
            stringList[i]=new String(charArr);

        }
        String msgString = ArrUtil.convertStrArrToString(stringList);
        return msgString;
    }
    public static MessageData msgEncrypt(String input, ECParameterSpec ECP, ECPoint nW)throws InvalidParameterSpecException {

        EllipticCurve curve = ECP.getCurve();
        ECField fieldCheck = curve.getField();
        ECFieldFp pField;
        if (fieldCheck instanceof ECFieldFp) {
            pField = (ECFieldFp)fieldCheck;
        } else {
            throw new InvalidParameterSpecException("Only curves over prime order fields are supported");
        }
        BigInteger p = pField.getP();
        BigInteger N = ECP.getOrder();
        ECPoint G = ECP.getGenerator();

        BigInteger bigTen = new BigInteger("10");
        BigInteger k = BigInteger.valueOf(1870073435);//MathUtil.randomBig((N.divide(bigTen.pow(67))).subtract(BigInteger.valueOf(1)));
        int groupSize = (MathUtil.findNumberOfDigits(p.doubleValue(), 65536)-1);

        ECPoint kGpoint = ECUtil.scalarMult(curve,G,k);

        BigInteger kGX = kGpoint.getAffineX(); BigInteger kGY = kGpoint.getAffineY();
        BigInteger kG[] = {kGX,kGY};

        ECPoint kPK = ECUtil.scalarMult(curve,nW,k);
        int[] asciiVal = MathUtil.textToAsciiVal(input);
        int groupArr[][] = ArrUtil.splitArray(asciiVal,groupSize);

        BigInteger[] asciiBig;
        int ln;
        if(((groupArr.length)&1)==0){
            ln = groupArr.length;
            asciiBig = new BigInteger[ln];}
        else {ln = groupArr.length+1;
            asciiBig = new BigInteger[ln];asciiBig[groupArr.length]=BigInteger.valueOf(32);}

        for (int j = 0; j < groupArr.length; j++)
        {

            asciiBig[j]=MathUtil.convertAsBaseToDec(groupArr[j],65536);

        }

        BigInteger pairPM[][] = ArrUtil.splitArray(asciiBig,2);
        BigInteger pairPC [][]=pairPM;

        for (int i = 0; i < pairPM.length; i++)
        {
            pairPC[i]= calcCiph(ECP,pairPM[i],kPK);
        }

        String pairStr[][]=new String[pairPC.length][pairPC[0].length];

        for (int j = 0; j < pairStr.length; j++){
            for (int i = 0; i < pairStr[j].length; i++){
                pairStr[j][i] = pairPC[j][i].toString();
            }
        }
        String[] oneD = ArrUtil.TwoToOne(pairStr);

        MessageData cipherText = new MessageData(kG,oneD);

        return cipherText;
    }

    public static String msgDecrypt (MessageData cipherMsg, ECParameterSpec ECP, BigInteger nS) throws InvalidParameterSpecException {
        EllipticCurve curve = ECP.getCurve();

        ECField fieldCheck = curve.getField();
        ECFieldFp pField;
        if (fieldCheck instanceof ECFieldFp) {
            pField = (ECFieldFp)fieldCheck;
        } else {
            throw new InvalidParameterSpecException("Only curves over prime order fields are supported");
        }
        BigInteger p = pField.getP();
        BigInteger kgX =new BigInteger(cipherMsg.decapKG()[0]);BigInteger kgY = new BigInteger(cipherMsg.decapKG()[1]);
        ECPoint kG = new ECPoint(kgX,kgY);

        ECPoint kPK = ECUtil.scalarMult(curve,kG,nS);
        int groupSize = (MathUtil.findNumberOfDigits(p.doubleValue(), 65536)-1);

        String deciphStr[][] = ArrUtil.splitArray(cipherMsg.decapPC(),2);

        BigInteger[][] deciph = new BigInteger[deciphStr.length][deciphStr[0].length];

        for (int j = 0; j < deciphStr.length; j++){
            for (int i = 0; i < deciphStr[j].length; i++){
                deciph[j][i] = new BigInteger(deciphStr[j][i]);}
        }

        BigInteger[][] deciphResult = deciph;
        BigInteger[] deciphList = new BigInteger[(deciph.length)*2];

        for (int i = 0; i < deciphResult.length; i++) {

            deciphResult[i] = calcDeciph(ECP, deciph[i], kPK);
        }

        int ii=0;
        for (int i = 0; i < deciphResult.length; i++)
        {
            deciphList[i+ii]=deciph[i][0];
            deciphList[i+ii+1]=deciph[i][1];

            ii++;
        }

        int[][] asciiValArray= new int[(deciphList.length)][];

        for (int i = 0; i < deciphList.length; i++) {

            asciiValArray[i] = MathUtil.convertDecToBase(deciphList[i], 65536);

        }
        String stringList[]= new String[asciiValArray.length*2];


        for (int i = 0; i < asciiValArray.length; i++) {

            char[] charArr = new char[asciiValArray[i].length] ;
            for (int j = 0; j < asciiValArray[i].length; j++){
                charArr[j]=(char)asciiValArray[i][j];
            }
            stringList[i]=new String(charArr);

        }
        String msgString = ArrUtil.convertStrArrToString(stringList);
        return msgString;
    }
}
