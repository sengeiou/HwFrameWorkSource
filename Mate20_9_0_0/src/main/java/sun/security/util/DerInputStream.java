package sun.security.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Date;
import java.util.Vector;

public class DerInputStream {
    DerInputBuffer buffer;
    public byte tag;

    public DerInputStream(byte[] data) throws IOException {
        init(data, 0, data.length, true);
    }

    public DerInputStream(byte[] data, int offset, int len) throws IOException {
        init(data, offset, len, true);
    }

    public DerInputStream(byte[] data, int offset, int len, boolean allowIndefiniteLength) throws IOException {
        init(data, offset, len, allowIndefiniteLength);
    }

    private void init(byte[] data, int offset, int len, boolean allowIndefiniteLength) throws IOException {
        if (offset + 2 > data.length || offset + len > data.length) {
            throw new IOException("Encoding bytes too short");
        }
        if (!DerIndefLenConverter.isIndefinite(data[offset + 1])) {
            this.buffer = new DerInputBuffer(data, offset, len);
        } else if (allowIndefiniteLength) {
            byte[] inData = new byte[len];
            System.arraycopy(data, offset, inData, 0, len);
            this.buffer = new DerInputBuffer(new DerIndefLenConverter().convert(inData));
        } else {
            throw new IOException("Indefinite length BER encoding found");
        }
        this.buffer.mark(Integer.MAX_VALUE);
    }

    DerInputStream(DerInputBuffer buf) {
        this.buffer = buf;
        this.buffer.mark(Integer.MAX_VALUE);
    }

    public DerInputStream subStream(int len, boolean do_skip) throws IOException {
        DerInputBuffer newbuf = this.buffer.dup();
        newbuf.truncate(len);
        if (do_skip) {
            this.buffer.skip((long) len);
        }
        return new DerInputStream(newbuf);
    }

    public byte[] toByteArray() {
        return this.buffer.toByteArray();
    }

    public int getInteger() throws IOException {
        if (this.buffer.read() == 2) {
            return this.buffer.getInteger(getLength(this.buffer));
        }
        throw new IOException("DER input, Integer tag error");
    }

    public BigInteger getBigInteger() throws IOException {
        if (this.buffer.read() == 2) {
            return this.buffer.getBigInteger(getLength(this.buffer), false);
        }
        throw new IOException("DER input, Integer tag error");
    }

    public BigInteger getPositiveBigInteger() throws IOException {
        if (this.buffer.read() == 2) {
            return this.buffer.getBigInteger(getLength(this.buffer), true);
        }
        throw new IOException("DER input, Integer tag error");
    }

    public int getEnumerated() throws IOException {
        if (this.buffer.read() == 10) {
            return this.buffer.getInteger(getLength(this.buffer));
        }
        throw new IOException("DER input, Enumerated tag error");
    }

    public byte[] getBitString() throws IOException {
        if (this.buffer.read() == 3) {
            return this.buffer.getBitString(getLength(this.buffer));
        }
        throw new IOException("DER input not an bit string");
    }

    public BitArray getUnalignedBitString() throws IOException {
        if (this.buffer.read() == 3) {
            int length = getLength(this.buffer) - 1;
            int excessBits = this.buffer.read();
            if (excessBits >= 0) {
                int validBits = (length * 8) - excessBits;
                if (validBits >= 0) {
                    byte[] repn = new byte[length];
                    if (length == 0 || this.buffer.read(repn) == length) {
                        return new BitArray(validBits, repn);
                    }
                    throw new IOException("Short read of DER bit string");
                }
                throw new IOException("Valid bits of bit string invalid");
            }
            throw new IOException("Unused bits of bit string invalid");
        }
        throw new IOException("DER input not a bit string");
    }

    public byte[] getOctetString() throws IOException {
        if (this.buffer.read() == 4) {
            int length = getLength(this.buffer);
            byte[] retval = new byte[length];
            if (length == 0 || this.buffer.read(retval) == length) {
                return retval;
            }
            throw new IOException("Short read of DER octet string");
        }
        throw new IOException("DER input not an octet string");
    }

    public void getBytes(byte[] val) throws IOException {
        if (val.length != 0 && this.buffer.read(val) != val.length) {
            throw new IOException("Short read of DER octet string");
        }
    }

    public void getNull() throws IOException {
        if (this.buffer.read() != 5 || this.buffer.read() != 0) {
            throw new IOException("getNull, bad data");
        }
    }

    public ObjectIdentifier getOID() throws IOException {
        return new ObjectIdentifier(this);
    }

    public DerValue[] getSequence(int startLen, boolean originalEncodedFormRetained) throws IOException {
        this.tag = (byte) this.buffer.read();
        if (this.tag == (byte) 48) {
            return readVector(startLen, originalEncodedFormRetained);
        }
        throw new IOException("Sequence tag error");
    }

    public DerValue[] getSequence(int startLen) throws IOException {
        return getSequence(startLen, false);
    }

    public DerValue[] getSet(int startLen) throws IOException {
        this.tag = (byte) this.buffer.read();
        if (this.tag == (byte) 49) {
            return readVector(startLen);
        }
        throw new IOException("Set tag error");
    }

    public DerValue[] getSet(int startLen, boolean implicit) throws IOException {
        return getSet(startLen, implicit, false);
    }

    public DerValue[] getSet(int startLen, boolean implicit, boolean originalEncodedFormRetained) throws IOException {
        this.tag = (byte) this.buffer.read();
        if (implicit || this.tag == (byte) 49) {
            return readVector(startLen, originalEncodedFormRetained);
        }
        throw new IOException("Set tag error");
    }

    protected DerValue[] readVector(int startLen) throws IOException {
        return readVector(startLen, false);
    }

    protected DerValue[] readVector(int startLen, boolean originalEncodedFormRetained) throws IOException {
        byte lenByte = (byte) this.buffer.read();
        int len = getLength(lenByte, this.buffer);
        int i = 0;
        if (len == -1) {
            int readLen = this.buffer.available();
            byte[] indefData = new byte[(readLen + 2)];
            indefData[0] = this.tag;
            indefData[1] = lenByte;
            DataInputStream dis = new DataInputStream(this.buffer);
            dis.readFully(indefData, 2, readLen);
            dis.close();
            this.buffer = new DerInputBuffer(new DerIndefLenConverter().convert(indefData));
            if (this.tag == this.buffer.read()) {
                len = getLength(this.buffer);
            } else {
                throw new IOException("Indefinite length encoding not supported");
            }
        }
        if (len == 0) {
            return new DerValue[0];
        }
        DerInputStream newstr;
        if (this.buffer.available() == len) {
            newstr = this;
        } else {
            newstr = subStream(len, true);
        }
        Vector<DerValue> vec = new Vector(startLen);
        do {
            vec.addElement(new DerValue(newstr.buffer, originalEncodedFormRetained));
        } while (newstr.available() > 0);
        if (newstr.available() == 0) {
            int max = vec.size();
            DerValue[] retval = new DerValue[max];
            while (i < max) {
                retval[i] = (DerValue) vec.elementAt(i);
                i++;
            }
            return retval;
        }
        throw new IOException("Extra data at end of vector");
    }

    public DerValue getDerValue() throws IOException {
        return new DerValue(this.buffer);
    }

    public String getUTF8String() throws IOException {
        return readString((byte) 12, "UTF-8", "UTF8");
    }

    public String getPrintableString() throws IOException {
        return readString((byte) 19, "Printable", "ASCII");
    }

    public String getT61String() throws IOException {
        return readString((byte) 20, "T61", "ISO-8859-1");
    }

    public String getIA5String() throws IOException {
        return readString((byte) 22, "IA5", "ASCII");
    }

    public String getBMPString() throws IOException {
        return readString((byte) 30, "BMP", "UnicodeBigUnmarked");
    }

    public String getGeneralString() throws IOException {
        return readString((byte) 27, "General", "ASCII");
    }

    private String readString(byte stringTag, String stringName, String enc) throws IOException {
        if (this.buffer.read() == stringTag) {
            int length = getLength(this.buffer);
            byte[] retval = new byte[length];
            if (length == 0 || this.buffer.read(retval) == length) {
                return new String(retval, enc);
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Short read of DER ");
            stringBuilder.append(stringName);
            stringBuilder.append(" string");
            throw new IOException(stringBuilder.toString());
        }
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("DER input not a ");
        stringBuilder2.append(stringName);
        stringBuilder2.append(" string");
        throw new IOException(stringBuilder2.toString());
    }

    public Date getUTCTime() throws IOException {
        if (this.buffer.read() == 23) {
            return this.buffer.getUTCTime(getLength(this.buffer));
        }
        throw new IOException("DER input, UTCtime tag invalid ");
    }

    public Date getGeneralizedTime() throws IOException {
        if (this.buffer.read() == 24) {
            return this.buffer.getGeneralizedTime(getLength(this.buffer));
        }
        throw new IOException("DER input, GeneralizedTime tag invalid ");
    }

    int getByte() throws IOException {
        return this.buffer.read() & 255;
    }

    public int peekByte() throws IOException {
        return this.buffer.peek();
    }

    int getLength() throws IOException {
        return getLength(this.buffer);
    }

    static int getLength(InputStream in) throws IOException {
        return getLength(in.read(), in);
    }

    static int getLength(int lenByte, InputStream in) throws IOException {
        if (lenByte != -1) {
            int value;
            String mdName = "DerInputStream.getLength(): ";
            int tmp = lenByte;
            if ((tmp & 128) == 0) {
                value = tmp;
            } else {
                tmp &= 127;
                if (tmp == 0) {
                    return -1;
                }
                if (tmp < 0 || tmp > 4) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(mdName);
                    stringBuilder.append("lengthTag=");
                    stringBuilder.append(tmp);
                    stringBuilder.append(", ");
                    stringBuilder.append(tmp < 0 ? "incorrect DER encoding." : "too big.");
                    throw new IOException(stringBuilder.toString());
                }
                int value2 = in.read() & 255;
                tmp--;
                StringBuilder stringBuilder2;
                if (value2 != 0) {
                    value = value2;
                    while (true) {
                        value2 = tmp - 1;
                        if (tmp <= 0) {
                            break;
                        }
                        value = (value << 8) + (in.read() & 255);
                        tmp = value2;
                    }
                    if (value < 0) {
                        stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(mdName);
                        stringBuilder2.append("Invalid length bytes");
                        throw new IOException(stringBuilder2.toString());
                    } else if (value > 127) {
                        tmp = value2;
                    } else {
                        stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(mdName);
                        stringBuilder2.append("Should use short form for length");
                        throw new IOException(stringBuilder2.toString());
                    }
                }
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append(mdName);
                stringBuilder2.append("Redundant length bytes found");
                throw new IOException(stringBuilder2.toString());
            }
            return value;
        }
        throw new IOException("Short read of DER length");
    }

    public void mark(int value) {
        this.buffer.mark(value);
    }

    public void reset() {
        this.buffer.reset();
    }

    public int available() {
        return this.buffer.available();
    }
}
