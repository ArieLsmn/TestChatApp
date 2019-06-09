package com.android.crypto;

import java.math.BigInteger;

public class MessageData {
    private static String kG[];
    private static String pC[];

    public MessageData(BigInteger[] kG, String[] pC){
        this.kG = new String[2];
        this.kG[0] = kG[0].toString();
        this.kG[1] = kG[1].toString();
        this.pC = pC;
    }
    public MessageData(String[] kG, String[] pC){
        this.kG = kG;
        this.pC = pC;
    }

    public String[] decapKG(){

        return this.kG;
    }
    public String[]  decapPC(){
        return this.pC;
    }

}
