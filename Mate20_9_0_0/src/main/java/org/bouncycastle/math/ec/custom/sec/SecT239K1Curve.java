package org.bouncycastle.math.ec.custom.sec;

import java.math.BigInteger;
import org.bouncycastle.crypto.tls.CipherSuite;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECCurve.AbstractF2m;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECLookupTable;
import org.bouncycastle.math.ec.ECMultiplier;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.WTauNafMultiplier;
import org.bouncycastle.math.raw.Nat256;
import org.bouncycastle.util.encoders.Hex;

public class SecT239K1Curve extends AbstractF2m {
    private static final int SecT239K1_DEFAULT_COORDS = 6;
    protected SecT239K1Point infinity;

    public SecT239K1Curve() {
        super(239, CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256, 0, 0);
        this.infinity = new SecT239K1Point(this, null, null);
        this.a = fromBigInteger(BigInteger.valueOf(0));
        this.b = fromBigInteger(BigInteger.valueOf(1));
        this.order = new BigInteger(1, Hex.decode("2000000000000000000000000000005A79FEC67CB6E91F1C1DA800E478A5"));
        this.cofactor = BigInteger.valueOf(4);
        this.coord = 6;
    }

    protected ECCurve cloneCurve() {
        return new SecT239K1Curve();
    }

    public ECLookupTable createCacheSafeLookupTable(ECPoint[] eCPointArr, int i, final int i2) {
        final long[] jArr = new long[((i2 * 4) * 2)];
        int i3 = 0;
        int i4 = i3;
        while (i3 < i2) {
            ECPoint eCPoint = eCPointArr[i + i3];
            Nat256.copy64(((SecT239FieldElement) eCPoint.getRawXCoord()).x, 0, jArr, i4);
            i4 += 4;
            Nat256.copy64(((SecT239FieldElement) eCPoint.getRawYCoord()).x, 0, jArr, i4);
            i4 += 4;
            i3++;
        }
        return new ECLookupTable() {
            public int getSize() {
                return i2;
            }

            public ECPoint lookup(int i) {
                long[] create64 = Nat256.create64();
                long[] create642 = Nat256.create64();
                int i2 = 0;
                int i3 = i2;
                while (i2 < i2) {
                    long j = (long) (((i2 ^ i) - 1) >> 31);
                    for (int i4 = 0; i4 < 4; i4++) {
                        create64[i4] = create64[i4] ^ (jArr[i3 + i4] & j);
                        create642[i4] = create642[i4] ^ (jArr[(i3 + 4) + i4] & j);
                    }
                    i3 += 8;
                    i2++;
                }
                return SecT239K1Curve.this.createRawPoint(new SecT239FieldElement(create64), new SecT239FieldElement(create642), false);
            }
        };
    }

    protected ECMultiplier createDefaultMultiplier() {
        return new WTauNafMultiplier();
    }

    protected ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, boolean z) {
        return new SecT239K1Point(this, eCFieldElement, eCFieldElement2, z);
    }

    protected ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement[] eCFieldElementArr, boolean z) {
        return new SecT239K1Point(this, eCFieldElement, eCFieldElement2, eCFieldElementArr, z);
    }

    public ECFieldElement fromBigInteger(BigInteger bigInteger) {
        return new SecT239FieldElement(bigInteger);
    }

    public int getFieldSize() {
        return 239;
    }

    public ECPoint getInfinity() {
        return this.infinity;
    }

    public int getK1() {
        return CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256;
    }

    public int getK2() {
        return 0;
    }

    public int getK3() {
        return 0;
    }

    public int getM() {
        return 239;
    }

    public boolean isKoblitz() {
        return true;
    }

    public boolean isTrinomial() {
        return true;
    }

    public boolean supportsCoordinateSystem(int i) {
        return i == 6;
    }
}
