package com.android.crypto;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.*;

public class ECUtil {

    static BigInteger BIG_TWO = new BigInteger("2");

    public static ECParameterSpec getCurveSpec(String name)
            throws NoSuchAlgorithmException, InvalidParameterSpecException {
        AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
        parameters.init(new ECGenParameterSpec(name));
        ECParameterSpec ecp = parameters.getParameterSpec(ECParameterSpec.class);
        return ecp;
    }

    public static BigInteger getModulus(EllipticCurve curve) throws InvalidParameterSpecException {
        ECField field = curve.getField();
        if (field instanceof ECFieldFp) {
            return ((ECFieldFp) field).getP();
        } else {
            throw new InvalidParameterSpecException("Only curves over prime order fields are supported");
        }
    }

    public static ECPoint doublePoint(final BigInteger p, final BigInteger a, final ECPoint R) {
        if (R.equals(ECPoint.POINT_INFINITY)) return R;
        BigInteger slope = (R.getAffineX().pow(2)).multiply(BigInteger.valueOf(3));
        slope = slope.add(a);
        slope = slope.multiply((R.getAffineY().multiply(BIG_TWO)).modInverse(p));
        final BigInteger Xout = slope.pow(2).subtract(R.getAffineX().multiply(BIG_TWO)).mod(p);
        final BigInteger Yout = (R.getAffineY().negate()).add(slope.multiply(R.getAffineX().subtract(Xout))).mod(p);
        return new ECPoint(Xout, Yout);
    }

    public static ECPoint addPoint   (final BigInteger p, final BigInteger a, final ECPoint r, final ECPoint g) {
        if (r.equals(ECPoint.POINT_INFINITY)) return g;
        if (g.equals(ECPoint.POINT_INFINITY)) return r;
        if (r==g || r.equals(g)) return doublePoint(p, a, r);
        final BigInteger gX    = g.getAffineX();
        final BigInteger sY    = g.getAffineY();
        final BigInteger rX    = r.getAffineX();
        final BigInteger rY    = r.getAffineY();
        final BigInteger slope = (rY.subtract(sY)).multiply(rX.subtract(gX).modInverse(p)).mod(p);
        final BigInteger Xout  = (slope.modPow(BIG_TWO, p).subtract(rX)).subtract(gX).mod(p);
        BigInteger Yout =   sY.negate().mod(p);
        Yout = Yout.add(slope.multiply(gX.subtract(Xout))).mod(p);
        return new ECPoint(Xout, Yout);
    }

    public static ECPoint scalarMult   (final EllipticCurve curve, final ECPoint po, final BigInteger kin) {
        final ECField         field    = curve.getField();
        if(!(field instanceof ECFieldFp)) throw new UnsupportedOperationException(field.getClass().getCanonicalName());
        final BigInteger p = ((ECFieldFp)field).getP();
        final BigInteger a = curve.getA();
        ECPoint R = ECPoint.POINT_INFINITY;
        BigInteger k = kin.mod(p);
        final int length = k.bitLength();
        final byte[] binarray = new byte[length];
        for(int i=0;i<=length-1;i++){
            binarray[i] = k.mod(BIG_TWO).byteValue();
            k = k.shiftRight(1);
        }
        for(int i = length-1;i >= 0;i--){
            R = doublePoint(p, a, R);
            if(binarray[i]== 1) R = addPoint(p, a, R, po);
        }
        return R;
    }


}
