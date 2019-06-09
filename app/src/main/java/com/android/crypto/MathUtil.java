package com.android.crypto;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class MathUtil {
    public static int findNumberOfDigits(double n, int base)
    {

        // Calculating log using base changing
        // property and then taking it
        // floor and then adding 1.
        int dig = (int)(Math.floor(
                Math.log(n) / Math.log(base)));
        //+ 1);


        // printing output
        /*System.out.println("The Number of digits of Number "
                + n + " in base " + base
                + " is " + dig);*/
        return dig;
    }

    public static int[] textToAsciiVal(String inputStr){

        byte[] ascii = inputStr.getBytes(StandardCharsets.US_ASCII);
        int[] intArray = new int[ascii.length];
        for (int i = 0; i < ascii.length; intArray[i] = ascii[i++]);

        return intArray;

    }

    public static BigInteger convertAsBaseToDec (int[] arr, int base){
        BigInteger val[]= new BigInteger[arr.length];
        BigInteger sum= BigInteger.valueOf(0);
        BigInteger b = BigInteger.valueOf(base);
        int arrRev[] = ArrUtil.reverseArr(arr,arr.length);

        for (int i = 0; i < arrRev.length; i++) {
            double j = (double)i;

            BigInteger x = BigInteger.valueOf(arrRev[i]);
            BigInteger re = x.multiply(b.pow(i));
            val[i] = re;
            //System.out.println(val[i]);
        }
        for (int l=0;l<val.length;l++){
            sum = sum.add(val[l]);
        }
        return sum;
    }

    public static int[] convertDecToBase (BigInteger inp,int base){
        BigInteger val= inp;
        BigInteger test = BigInteger.valueOf(base);
        int comp1=0;

        int pow;
        BigInteger subtr;
        BigInteger hasil;
        int i=0;
        int j=0;
        int[] digits;
        for (int ii=0;comp1>=0;ii++){

            subtr = test.pow(ii);
            hasil = val.subtract(subtr);
            comp1 = hasil.compareTo(BigInteger.valueOf(0));
            i=ii;
        }

        digits = new int[i];
        --i;
        ///System.out.println("digits:"+i);
        BigInteger hasil2=val;


        for (int jj=0;i>=jj;jj++){

            int k=0;
            BigInteger powered;
            BigInteger sum;
            BigInteger hasil3=BigInteger.valueOf(0);
            powered = test.pow(i-jj);

            int comp2=0;

            for (int kk=1;comp2>=0;kk++) {

                subtr = powered.multiply(BigInteger.valueOf(kk));
                sum = hasil2.subtract(subtr);
                comp2 = sum.compareTo(BigInteger.valueOf(0));
                if(comp2==1){
                    hasil3=sum;
                }
                k=kk;
            }
            hasil2=hasil3;
            digits[jj]=k-1;

        }

        return digits;
    }

    public static BigInteger randomBig(BigInteger upperLim){
        BigInteger bigInteger = upperLim;// uper limit
        BigInteger min = new BigInteger("1");// lower limit
        BigInteger bigInteger1 = bigInteger.subtract(min);
        Random rnd = new Random();
        int maxNumBitLength = bigInteger.bitLength();
        BigInteger aRandomBigInt;

        aRandomBigInt = new BigInteger(maxNumBitLength, rnd);
        if (aRandomBigInt.compareTo(min) < 0)
            aRandomBigInt = aRandomBigInt.add(min);
        if (aRandomBigInt.compareTo(bigInteger) >= 0)
            aRandomBigInt = aRandomBigInt.mod(bigInteger1).add(min);
        return aRandomBigInt;

    }


}
