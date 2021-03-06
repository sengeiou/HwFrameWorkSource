package android.telephony.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.provider.Telephony.CarrierId.All;
import android.provider.Telephony.Carriers;
import android.telephony.ServiceState;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class ApnSetting implements Parcelable {
    private static final Map<Integer, String> APN_TYPE_INT_MAP = new ArrayMap();
    private static final Map<String, Integer> APN_TYPE_STRING_MAP = new ArrayMap();
    public static final int AUTH_TYPE_CHAP = 2;
    public static final int AUTH_TYPE_NONE = 0;
    public static final int AUTH_TYPE_PAP = 1;
    public static final int AUTH_TYPE_PAP_OR_CHAP = 3;
    public static final Creator<ApnSetting> CREATOR = new Creator<ApnSetting>() {
        public ApnSetting createFromParcel(Parcel in) {
            return ApnSetting.readFromParcel(in);
        }

        public ApnSetting[] newArray(int size) {
            return new ApnSetting[size];
        }
    };
    private static final String LOG_TAG = "ApnSetting";
    public static final int MVNO_TYPE_GID = 2;
    public static final int MVNO_TYPE_ICCID = 3;
    public static final int MVNO_TYPE_IMSI = 1;
    private static final Map<Integer, String> MVNO_TYPE_INT_MAP = new ArrayMap();
    public static final int MVNO_TYPE_SPN = 0;
    private static final Map<String, Integer> MVNO_TYPE_STRING_MAP = new ArrayMap();
    private static final int NOT_IN_MAP_INT = -1;
    private static final int NO_PORT_SPECIFIED = -1;
    private static final Map<Integer, String> PROTOCOL_INT_MAP = new ArrayMap();
    public static final int PROTOCOL_IP = 0;
    public static final int PROTOCOL_IPV4V6 = 2;
    public static final int PROTOCOL_IPV6 = 1;
    public static final int PROTOCOL_PPP = 3;
    private static final Map<String, Integer> PROTOCOL_STRING_MAP = new ArrayMap();
    private static final int TYPE_ALL_BUT_IA = 767;
    public static final int TYPE_CBS = 128;
    public static final int TYPE_DEFAULT = 17;
    public static final int TYPE_DUN = 8;
    public static final int TYPE_EMERGENCY = 512;
    public static final int TYPE_FOTA = 32;
    public static final int TYPE_HIPRI = 16;
    public static final int TYPE_IA = 256;
    public static final int TYPE_IMS = 64;
    public static final int TYPE_MMS = 2;
    public static final int TYPE_SUPL = 4;
    private static final boolean VDBG = false;
    private final String mApnName;
    private final int mApnTypeBitmask;
    private final int mAuthType;
    private final boolean mCarrierEnabled;
    private final String mEntryName;
    private final int mId;
    private final int mMaxConns;
    private final int mMaxConnsTime;
    private final InetAddress mMmsProxyAddress;
    private final int mMmsProxyPort;
    private final Uri mMmsc;
    private final boolean mModemCognitive;
    private final int mMtu;
    private final String mMvnoMatchData;
    private final int mMvnoType;
    private final int mNetworkTypeBitmask;
    private final String mOperatorNumeric;
    private final String mPassword;
    private boolean mPermanentFailed;
    private final int mProfileId;
    private final int mProtocol;
    private final InetAddress mProxyAddress;
    private final int mProxyPort;
    private final int mRoamingProtocol;
    private final String mUser;
    private final int mWaitTime;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ApnType {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface AuthType {
    }

    public static class Builder {
        private String mApnName;
        private int mApnTypeBitmask;
        private int mAuthType;
        private boolean mCarrierEnabled;
        private String mEntryName;
        private int mId;
        private int mMaxConns;
        private int mMaxConnsTime;
        private InetAddress mMmsProxyAddress;
        private int mMmsProxyPort = -1;
        private Uri mMmsc;
        private boolean mModemCognitive;
        private int mMtu;
        private String mMvnoMatchData;
        private int mMvnoType = -1;
        private int mNetworkTypeBitmask;
        private String mOperatorNumeric;
        private String mPassword;
        private int mProfileId;
        private int mProtocol = -1;
        private InetAddress mProxyAddress;
        private int mProxyPort = -1;
        private int mRoamingProtocol = -1;
        private String mUser;
        private int mWaitTime;

        private Builder setId(int id) {
            this.mId = id;
            return this;
        }

        public Builder setMtu(int mtu) {
            this.mMtu = mtu;
            return this;
        }

        public Builder setProfileId(int profileId) {
            this.mProfileId = profileId;
            return this;
        }

        public Builder setModemCognitive(boolean modemCognitive) {
            this.mModemCognitive = modemCognitive;
            return this;
        }

        public Builder setMaxConns(int maxConns) {
            this.mMaxConns = maxConns;
            return this;
        }

        public Builder setWaitTime(int waitTime) {
            this.mWaitTime = waitTime;
            return this;
        }

        public Builder setMaxConnsTime(int maxConnsTime) {
            this.mMaxConnsTime = maxConnsTime;
            return this;
        }

        public Builder setMvnoMatchData(String mvnoMatchData) {
            this.mMvnoMatchData = mvnoMatchData;
            return this;
        }

        public Builder setEntryName(String entryName) {
            this.mEntryName = entryName;
            return this;
        }

        public Builder setApnName(String apnName) {
            this.mApnName = apnName;
            return this;
        }

        public Builder setProxyAddress(InetAddress proxy) {
            this.mProxyAddress = proxy;
            return this;
        }

        public Builder setProxyPort(int port) {
            this.mProxyPort = port;
            return this;
        }

        public Builder setMmsc(Uri mmsc) {
            this.mMmsc = mmsc;
            return this;
        }

        public Builder setMmsProxyAddress(InetAddress mmsProxy) {
            this.mMmsProxyAddress = mmsProxy;
            return this;
        }

        public Builder setMmsProxyPort(int mmsPort) {
            this.mMmsProxyPort = mmsPort;
            return this;
        }

        public Builder setUser(String user) {
            this.mUser = user;
            return this;
        }

        public Builder setPassword(String password) {
            this.mPassword = password;
            return this;
        }

        public Builder setAuthType(int authType) {
            this.mAuthType = authType;
            return this;
        }

        public Builder setApnTypeBitmask(int apnTypeBitmask) {
            this.mApnTypeBitmask = apnTypeBitmask;
            return this;
        }

        public Builder setOperatorNumeric(String operatorNumeric) {
            this.mOperatorNumeric = operatorNumeric;
            return this;
        }

        public Builder setProtocol(int protocol) {
            this.mProtocol = protocol;
            return this;
        }

        public Builder setRoamingProtocol(int roamingProtocol) {
            this.mRoamingProtocol = roamingProtocol;
            return this;
        }

        public Builder setCarrierEnabled(boolean carrierEnabled) {
            this.mCarrierEnabled = carrierEnabled;
            return this;
        }

        public Builder setNetworkTypeBitmask(int networkTypeBitmask) {
            this.mNetworkTypeBitmask = networkTypeBitmask;
            return this;
        }

        public Builder setMvnoType(int mvnoType) {
            this.mMvnoType = mvnoType;
            return this;
        }

        public ApnSetting build() {
            if ((this.mApnTypeBitmask & 1023) == 0 || TextUtils.isEmpty(this.mApnName) || TextUtils.isEmpty(this.mEntryName)) {
                return null;
            }
            return new ApnSetting(this, null);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface MvnoType {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ProtocolType {
    }

    /* synthetic */ ApnSetting(Builder x0, AnonymousClass1 x1) {
        this(x0);
    }

    static {
        APN_TYPE_STRING_MAP.put("*", Integer.valueOf(TYPE_ALL_BUT_IA));
        APN_TYPE_STRING_MAP.put("default", Integer.valueOf(17));
        APN_TYPE_STRING_MAP.put("mms", Integer.valueOf(2));
        APN_TYPE_STRING_MAP.put("supl", Integer.valueOf(4));
        APN_TYPE_STRING_MAP.put("dun", Integer.valueOf(8));
        APN_TYPE_STRING_MAP.put("hipri", Integer.valueOf(16));
        APN_TYPE_STRING_MAP.put("fota", Integer.valueOf(32));
        APN_TYPE_STRING_MAP.put("ims", Integer.valueOf(64));
        APN_TYPE_STRING_MAP.put("cbs", Integer.valueOf(128));
        APN_TYPE_STRING_MAP.put("ia", Integer.valueOf(256));
        APN_TYPE_STRING_MAP.put("emergency", Integer.valueOf(512));
        APN_TYPE_INT_MAP.put(Integer.valueOf(17), "default");
        APN_TYPE_INT_MAP.put(Integer.valueOf(2), "mms");
        APN_TYPE_INT_MAP.put(Integer.valueOf(4), "supl");
        APN_TYPE_INT_MAP.put(Integer.valueOf(8), "dun");
        APN_TYPE_INT_MAP.put(Integer.valueOf(16), "hipri");
        APN_TYPE_INT_MAP.put(Integer.valueOf(32), "fota");
        APN_TYPE_INT_MAP.put(Integer.valueOf(64), "ims");
        APN_TYPE_INT_MAP.put(Integer.valueOf(128), "cbs");
        APN_TYPE_INT_MAP.put(Integer.valueOf(256), "ia");
        APN_TYPE_INT_MAP.put(Integer.valueOf(512), "emergency");
        PROTOCOL_STRING_MAP.put("IP", Integer.valueOf(0));
        PROTOCOL_STRING_MAP.put("IPV6", Integer.valueOf(1));
        PROTOCOL_STRING_MAP.put("IPV4V6", Integer.valueOf(2));
        PROTOCOL_STRING_MAP.put("PPP", Integer.valueOf(3));
        PROTOCOL_INT_MAP.put(Integer.valueOf(0), "IP");
        PROTOCOL_INT_MAP.put(Integer.valueOf(1), "IPV6");
        PROTOCOL_INT_MAP.put(Integer.valueOf(2), "IPV4V6");
        PROTOCOL_INT_MAP.put(Integer.valueOf(3), "PPP");
        MVNO_TYPE_STRING_MAP.put(All.SPN, Integer.valueOf(0));
        MVNO_TYPE_STRING_MAP.put("imsi", Integer.valueOf(1));
        MVNO_TYPE_STRING_MAP.put("gid", Integer.valueOf(2));
        MVNO_TYPE_STRING_MAP.put("iccid", Integer.valueOf(3));
        MVNO_TYPE_INT_MAP.put(Integer.valueOf(0), All.SPN);
        MVNO_TYPE_INT_MAP.put(Integer.valueOf(1), "imsi");
        MVNO_TYPE_INT_MAP.put(Integer.valueOf(2), "gid");
        MVNO_TYPE_INT_MAP.put(Integer.valueOf(3), "iccid");
    }

    public int getMtu() {
        return this.mMtu;
    }

    public int getProfileId() {
        return this.mProfileId;
    }

    public boolean getModemCognitive() {
        return this.mModemCognitive;
    }

    public int getMaxConns() {
        return this.mMaxConns;
    }

    public int getWaitTime() {
        return this.mWaitTime;
    }

    public int getMaxConnsTime() {
        return this.mMaxConnsTime;
    }

    public String getMvnoMatchData() {
        return this.mMvnoMatchData;
    }

    public boolean getPermanentFailed() {
        return this.mPermanentFailed;
    }

    public void setPermanentFailed(boolean permanentFailed) {
        this.mPermanentFailed = permanentFailed;
    }

    public String getEntryName() {
        return this.mEntryName;
    }

    public String getApnName() {
        return this.mApnName;
    }

    public InetAddress getProxyAddress() {
        return this.mProxyAddress;
    }

    public int getProxyPort() {
        return this.mProxyPort;
    }

    public Uri getMmsc() {
        return this.mMmsc;
    }

    public InetAddress getMmsProxyAddress() {
        return this.mMmsProxyAddress;
    }

    public int getMmsProxyPort() {
        return this.mMmsProxyPort;
    }

    public String getUser() {
        return this.mUser;
    }

    public String getPassword() {
        return this.mPassword;
    }

    public int getAuthType() {
        return this.mAuthType;
    }

    public int getApnTypeBitmask() {
        return this.mApnTypeBitmask;
    }

    public int getId() {
        return this.mId;
    }

    public String getOperatorNumeric() {
        return this.mOperatorNumeric;
    }

    public int getProtocol() {
        return this.mProtocol;
    }

    public int getRoamingProtocol() {
        return this.mRoamingProtocol;
    }

    public boolean isEnabled() {
        return this.mCarrierEnabled;
    }

    public int getNetworkTypeBitmask() {
        return this.mNetworkTypeBitmask;
    }

    public int getMvnoType() {
        return this.mMvnoType;
    }

    private ApnSetting(Builder builder) {
        this.mPermanentFailed = false;
        this.mEntryName = builder.mEntryName;
        this.mApnName = builder.mApnName;
        this.mProxyAddress = builder.mProxyAddress;
        this.mProxyPort = builder.mProxyPort;
        this.mMmsc = builder.mMmsc;
        this.mMmsProxyAddress = builder.mMmsProxyAddress;
        this.mMmsProxyPort = builder.mMmsProxyPort;
        this.mUser = builder.mUser;
        this.mPassword = builder.mPassword;
        this.mAuthType = builder.mAuthType;
        this.mApnTypeBitmask = builder.mApnTypeBitmask;
        this.mId = builder.mId;
        this.mOperatorNumeric = builder.mOperatorNumeric;
        this.mProtocol = builder.mProtocol;
        this.mRoamingProtocol = builder.mRoamingProtocol;
        this.mMtu = builder.mMtu;
        this.mCarrierEnabled = builder.mCarrierEnabled;
        this.mNetworkTypeBitmask = builder.mNetworkTypeBitmask;
        this.mProfileId = builder.mProfileId;
        this.mModemCognitive = builder.mModemCognitive;
        this.mMaxConns = builder.mMaxConns;
        this.mWaitTime = builder.mWaitTime;
        this.mMaxConnsTime = builder.mMaxConnsTime;
        this.mMvnoType = builder.mMvnoType;
        this.mMvnoMatchData = builder.mMvnoMatchData;
    }

    public static ApnSetting makeApnSetting(int id, String operatorNumeric, String entryName, String apnName, InetAddress proxy, int port, Uri mmsc, InetAddress mmsProxy, int mmsPort, String user, String password, int authType, int mApnTypeBitmask, int protocol, int roamingProtocol, boolean carrierEnabled, int networkTypeBitmask, int profileId, boolean modemCognitive, int maxConns, int waitTime, int maxConnsTime, int mtu, int mvnoType, String mvnoMatchData) {
        return new Builder().setId(id).setOperatorNumeric(operatorNumeric).setEntryName(entryName).setApnName(apnName).setProxyAddress(proxy).setProxyPort(port).setMmsc(mmsc).setMmsProxyAddress(mmsProxy).setMmsProxyPort(mmsPort).setUser(user).setPassword(password).setAuthType(authType).setApnTypeBitmask(mApnTypeBitmask).setProtocol(protocol).setRoamingProtocol(roamingProtocol).setCarrierEnabled(carrierEnabled).setNetworkTypeBitmask(networkTypeBitmask).setProfileId(profileId).setModemCognitive(modemCognitive).setMaxConns(maxConns).setWaitTime(waitTime).setMaxConnsTime(maxConnsTime).setMtu(mtu).setMvnoType(mvnoType).setMvnoMatchData(mvnoMatchData).build();
    }

    public static ApnSetting makeApnSetting(Cursor cursor) {
        Cursor cursor2 = cursor;
        int apnTypesBitmask = parseTypes(cursor2.getString(cursor2.getColumnIndexOrThrow("type")));
        int networkTypeBitmask = cursor2.getInt(cursor2.getColumnIndexOrThrow(Carriers.NETWORK_TYPE_BITMASK));
        if (networkTypeBitmask == 0) {
            networkTypeBitmask = ServiceState.convertBearerBitmaskToNetworkTypeBitmask(cursor2.getInt(cursor2.getColumnIndexOrThrow(Carriers.BEARER_BITMASK)));
        }
        return makeApnSetting(cursor2.getInt(cursor2.getColumnIndexOrThrow("_id")), cursor2.getString(cursor2.getColumnIndexOrThrow("numeric")), cursor2.getString(cursor2.getColumnIndexOrThrow("name")), cursor2.getString(cursor2.getColumnIndexOrThrow("apn")), inetAddressFromString(cursor2.getString(cursor2.getColumnIndexOrThrow(Carriers.PROXY))), portFromString(cursor2.getString(cursor2.getColumnIndexOrThrow(Carriers.PORT))), UriFromString(cursor2.getString(cursor2.getColumnIndexOrThrow(Carriers.MMSC))), inetAddressFromString(cursor2.getString(cursor2.getColumnIndexOrThrow(Carriers.MMSPROXY))), portFromString(cursor2.getString(cursor2.getColumnIndexOrThrow(Carriers.MMSPORT))), cursor2.getString(cursor2.getColumnIndexOrThrow("user")), cursor2.getString(cursor2.getColumnIndexOrThrow("password")), cursor2.getInt(cursor2.getColumnIndexOrThrow(Carriers.AUTH_TYPE)), apnTypesBitmask, nullToNotInMapInt((Integer) PROTOCOL_STRING_MAP.get(cursor2.getString(cursor2.getColumnIndexOrThrow("protocol")))), nullToNotInMapInt((Integer) PROTOCOL_STRING_MAP.get(cursor2.getString(cursor2.getColumnIndexOrThrow(Carriers.ROAMING_PROTOCOL)))), cursor2.getInt(cursor2.getColumnIndexOrThrow(Carriers.CARRIER_ENABLED)) == 1, networkTypeBitmask, cursor2.getInt(cursor2.getColumnIndexOrThrow(Carriers.PROFILE_ID)), cursor2.getInt(cursor2.getColumnIndexOrThrow(Carriers.MODEM_COGNITIVE)) == 1, cursor2.getInt(cursor2.getColumnIndexOrThrow(Carriers.MAX_CONNS)), cursor2.getInt(cursor2.getColumnIndexOrThrow(Carriers.WAIT_TIME)), cursor2.getInt(cursor2.getColumnIndexOrThrow(Carriers.MAX_CONNS_TIME)), cursor2.getInt(cursor2.getColumnIndexOrThrow("mtu")), nullToNotInMapInt((Integer) MVNO_TYPE_STRING_MAP.get(cursor2.getString(cursor2.getColumnIndexOrThrow("mvno_type")))), cursor2.getString(cursor2.getColumnIndexOrThrow("mvno_match_data")));
    }

    public static ApnSetting makeApnSetting(ApnSetting apn) {
        ApnSetting apnSetting = apn;
        int i = apnSetting.mId;
        String str = apnSetting.mOperatorNumeric;
        String str2 = apnSetting.mEntryName;
        String str3 = apnSetting.mApnName;
        InetAddress inetAddress = apnSetting.mProxyAddress;
        int i2 = apnSetting.mProxyPort;
        Uri uri = apnSetting.mMmsc;
        InetAddress inetAddress2 = apnSetting.mMmsProxyAddress;
        int i3 = apnSetting.mMmsProxyPort;
        String str4 = apnSetting.mUser;
        String str5 = apnSetting.mPassword;
        int i4 = apnSetting.mAuthType;
        int i5 = apnSetting.mApnTypeBitmask;
        int i6 = apnSetting.mProtocol;
        int i7 = apnSetting.mRoamingProtocol;
        return makeApnSetting(i, str, str2, str3, inetAddress, i2, uri, inetAddress2, i3, str4, str5, i4, i5, i6, i7, apnSetting.mCarrierEnabled, apnSetting.mNetworkTypeBitmask, apnSetting.mProfileId, apnSetting.mModemCognitive, apnSetting.mMaxConns, apnSetting.mWaitTime, apnSetting.mMaxConnsTime, apnSetting.mMtu, apnSetting.mMvnoType, apnSetting.mMvnoMatchData);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ApnSettingV4] ");
        sb.append(this.mEntryName);
        sb.append(", ");
        sb.append(this.mId);
        sb.append(", ");
        sb.append(this.mOperatorNumeric);
        sb.append(", ");
        sb.append(this.mApnName);
        sb.append(", ");
        sb.append(inetAddressToString(this.mProxyAddress));
        sb.append(", ");
        sb.append(UriToString(this.mMmsc));
        sb.append(", ");
        sb.append(inetAddressToString(this.mMmsProxyAddress));
        sb.append(", ");
        sb.append(portToString(this.mMmsProxyPort));
        sb.append(", ");
        sb.append(portToString(this.mProxyPort));
        sb.append(", ");
        sb.append(this.mAuthType);
        sb.append(", ");
        sb.append(TextUtils.join((CharSequence) " | ", deParseTypes(this.mApnTypeBitmask).split(",")));
        sb.append(", ");
        sb.append(", ");
        sb.append(this.mProtocol);
        sb.append(", ");
        sb.append(this.mRoamingProtocol);
        sb.append(", ");
        sb.append(this.mCarrierEnabled);
        sb.append(", ");
        sb.append(this.mProfileId);
        sb.append(", ");
        sb.append(this.mModemCognitive);
        sb.append(", ");
        sb.append(this.mMaxConns);
        sb.append(", ");
        sb.append(this.mWaitTime);
        sb.append(", ");
        sb.append(this.mMaxConnsTime);
        sb.append(", ");
        sb.append(this.mMtu);
        sb.append(", ");
        sb.append(this.mMvnoType);
        sb.append(", ");
        sb.append(this.mMvnoMatchData);
        sb.append(", ");
        sb.append(this.mPermanentFailed);
        sb.append(", ");
        sb.append(this.mNetworkTypeBitmask);
        return sb.toString();
    }

    public boolean hasMvnoParams() {
        return (this.mMvnoType == -1 || TextUtils.isEmpty(this.mMvnoMatchData)) ? false : true;
    }

    public boolean canHandleType(int type) {
        return this.mCarrierEnabled && (this.mApnTypeBitmask & type) == type;
    }

    private boolean typeSameAny(ApnSetting first, ApnSetting second) {
        if ((first.mApnTypeBitmask & second.mApnTypeBitmask) != 0) {
            return true;
        }
        return false;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof ApnSetting)) {
            return false;
        }
        ApnSetting other = (ApnSetting) o;
        if (this.mEntryName.equals(other.mEntryName) && Objects.equals(Integer.valueOf(this.mId), Integer.valueOf(other.mId)) && Objects.equals(this.mOperatorNumeric, other.mOperatorNumeric) && Objects.equals(this.mApnName, other.mApnName) && Objects.equals(this.mProxyAddress, other.mProxyAddress) && Objects.equals(this.mMmsc, other.mMmsc) && Objects.equals(this.mMmsProxyAddress, other.mMmsProxyAddress) && Objects.equals(Integer.valueOf(this.mMmsProxyPort), Integer.valueOf(other.mMmsProxyPort)) && Objects.equals(Integer.valueOf(this.mProxyPort), Integer.valueOf(other.mProxyPort)) && Objects.equals(this.mUser, other.mUser) && Objects.equals(this.mPassword, other.mPassword) && Objects.equals(Integer.valueOf(this.mAuthType), Integer.valueOf(other.mAuthType)) && Objects.equals(Integer.valueOf(this.mApnTypeBitmask), Integer.valueOf(other.mApnTypeBitmask)) && Objects.equals(Integer.valueOf(this.mProtocol), Integer.valueOf(other.mProtocol)) && Objects.equals(Integer.valueOf(this.mRoamingProtocol), Integer.valueOf(other.mRoamingProtocol)) && Objects.equals(Boolean.valueOf(this.mCarrierEnabled), Boolean.valueOf(other.mCarrierEnabled)) && Objects.equals(Integer.valueOf(this.mProfileId), Integer.valueOf(other.mProfileId)) && Objects.equals(Boolean.valueOf(this.mModemCognitive), Boolean.valueOf(other.mModemCognitive)) && Objects.equals(Integer.valueOf(this.mMaxConns), Integer.valueOf(other.mMaxConns)) && Objects.equals(Integer.valueOf(this.mWaitTime), Integer.valueOf(other.mWaitTime)) && Objects.equals(Integer.valueOf(this.mMaxConnsTime), Integer.valueOf(other.mMaxConnsTime)) && Objects.equals(Integer.valueOf(this.mMtu), Integer.valueOf(other.mMtu)) && Objects.equals(Integer.valueOf(this.mMvnoType), Integer.valueOf(other.mMvnoType)) && Objects.equals(this.mMvnoMatchData, other.mMvnoMatchData) && Objects.equals(Integer.valueOf(this.mNetworkTypeBitmask), Integer.valueOf(other.mNetworkTypeBitmask))) {
            z = true;
        }
        return z;
    }

    public boolean equals(Object o, boolean isDataRoaming) {
        boolean z = false;
        if (!(o instanceof ApnSetting)) {
            return false;
        }
        ApnSetting other = (ApnSetting) o;
        if (this.mEntryName.equals(other.mEntryName) && Objects.equals(this.mOperatorNumeric, other.mOperatorNumeric) && Objects.equals(this.mApnName, other.mApnName) && Objects.equals(this.mProxyAddress, other.mProxyAddress) && Objects.equals(this.mMmsc, other.mMmsc) && Objects.equals(this.mMmsProxyAddress, other.mMmsProxyAddress) && Objects.equals(Integer.valueOf(this.mMmsProxyPort), Integer.valueOf(other.mMmsProxyPort)) && Objects.equals(Integer.valueOf(this.mProxyPort), Integer.valueOf(other.mProxyPort)) && Objects.equals(this.mUser, other.mUser) && Objects.equals(this.mPassword, other.mPassword) && Objects.equals(Integer.valueOf(this.mAuthType), Integer.valueOf(other.mAuthType)) && Objects.equals(Integer.valueOf(this.mApnTypeBitmask), Integer.valueOf(other.mApnTypeBitmask)) && ((isDataRoaming || Objects.equals(Integer.valueOf(this.mProtocol), Integer.valueOf(other.mProtocol))) && ((!isDataRoaming || Objects.equals(Integer.valueOf(this.mRoamingProtocol), Integer.valueOf(other.mRoamingProtocol))) && Objects.equals(Boolean.valueOf(this.mCarrierEnabled), Boolean.valueOf(other.mCarrierEnabled)) && Objects.equals(Integer.valueOf(this.mProfileId), Integer.valueOf(other.mProfileId)) && Objects.equals(Boolean.valueOf(this.mModemCognitive), Boolean.valueOf(other.mModemCognitive)) && Objects.equals(Integer.valueOf(this.mMaxConns), Integer.valueOf(other.mMaxConns)) && Objects.equals(Integer.valueOf(this.mWaitTime), Integer.valueOf(other.mWaitTime)) && Objects.equals(Integer.valueOf(this.mMaxConnsTime), Integer.valueOf(other.mMaxConnsTime)) && Objects.equals(Integer.valueOf(this.mMtu), Integer.valueOf(other.mMtu)) && Objects.equals(Integer.valueOf(this.mMvnoType), Integer.valueOf(other.mMvnoType)) && Objects.equals(this.mMvnoMatchData, other.mMvnoMatchData)))) {
            z = true;
        }
        return z;
    }

    public boolean similar(ApnSetting other) {
        return !canHandleType(8) && !other.canHandleType(8) && Objects.equals(this.mApnName, other.mApnName) && !typeSameAny(this, other) && xorEquals(this.mProxyAddress, other.mProxyAddress) && xorEqualsPort(this.mProxyPort, other.mProxyPort) && xorEquals(Integer.valueOf(this.mProtocol), Integer.valueOf(other.mProtocol)) && xorEquals(Integer.valueOf(this.mRoamingProtocol), Integer.valueOf(other.mRoamingProtocol)) && Objects.equals(Boolean.valueOf(this.mCarrierEnabled), Boolean.valueOf(other.mCarrierEnabled)) && Objects.equals(Integer.valueOf(this.mProfileId), Integer.valueOf(other.mProfileId)) && Objects.equals(Integer.valueOf(this.mMvnoType), Integer.valueOf(other.mMvnoType)) && Objects.equals(this.mMvnoMatchData, other.mMvnoMatchData) && xorEquals(this.mMmsc, other.mMmsc) && xorEquals(this.mMmsProxyAddress, other.mMmsProxyAddress) && xorEqualsPort(this.mMmsProxyPort, other.mMmsProxyPort) && Objects.equals(Integer.valueOf(this.mNetworkTypeBitmask), Integer.valueOf(other.mNetworkTypeBitmask));
    }

    private boolean xorEquals(String first, String second) {
        return Objects.equals(first, second) || TextUtils.isEmpty(first) || TextUtils.isEmpty(second);
    }

    private boolean xorEquals(Object first, Object second) {
        return first == null || second == null || first.equals(second);
    }

    private boolean xorEqualsPort(int first, int second) {
        return first == -1 || second == -1 || Objects.equals(Integer.valueOf(first), Integer.valueOf(second));
    }

    private String deParseTypes(int apnTypeBitmask) {
        Iterable types = new ArrayList();
        for (Integer type : APN_TYPE_INT_MAP.keySet()) {
            if ((type.intValue() & apnTypeBitmask) == type.intValue()) {
                types.add((String) APN_TYPE_INT_MAP.get(type));
            }
        }
        return TextUtils.join((CharSequence) ",", types);
    }

    private String nullToEmpty(String stringValue) {
        return stringValue == null ? "" : stringValue;
    }

    public ContentValues toContentValues() {
        String str;
        ContentValues apnValue = new ContentValues();
        apnValue.put("numeric", nullToEmpty(this.mOperatorNumeric));
        apnValue.put("name", nullToEmpty(this.mEntryName));
        apnValue.put("apn", nullToEmpty(this.mApnName));
        String str2 = Carriers.PROXY;
        if (this.mProxyAddress == null) {
            str = "";
        } else {
            str = inetAddressToString(this.mProxyAddress);
        }
        apnValue.put(str2, str);
        apnValue.put(Carriers.PORT, portToString(this.mProxyPort));
        apnValue.put(Carriers.MMSC, this.mMmsc == null ? "" : UriToString(this.mMmsc));
        apnValue.put(Carriers.MMSPORT, portToString(this.mMmsProxyPort));
        apnValue.put(Carriers.MMSPROXY, this.mMmsProxyAddress == null ? "" : inetAddressToString(this.mMmsProxyAddress));
        apnValue.put("user", nullToEmpty(this.mUser));
        apnValue.put("password", nullToEmpty(this.mPassword));
        apnValue.put(Carriers.AUTH_TYPE, Integer.valueOf(this.mAuthType));
        apnValue.put("type", nullToEmpty(deParseTypes(this.mApnTypeBitmask)));
        apnValue.put("protocol", nullToEmpty((String) PROTOCOL_INT_MAP.get(Integer.valueOf(this.mProtocol))));
        apnValue.put(Carriers.ROAMING_PROTOCOL, nullToEmpty((String) PROTOCOL_INT_MAP.get(Integer.valueOf(this.mRoamingProtocol))));
        apnValue.put(Carriers.CARRIER_ENABLED, Boolean.valueOf(this.mCarrierEnabled));
        apnValue.put("mvno_type", nullToEmpty((String) MVNO_TYPE_INT_MAP.get(Integer.valueOf(this.mMvnoType))));
        apnValue.put(Carriers.NETWORK_TYPE_BITMASK, Integer.valueOf(this.mNetworkTypeBitmask));
        return apnValue;
    }

    public static int parseTypes(String types) {
        if (TextUtils.isEmpty(types)) {
            return TYPE_ALL_BUT_IA;
        }
        int result = 0;
        for (String str : types.split(",")) {
            Integer type = (Integer) APN_TYPE_STRING_MAP.get(str);
            if (type != null) {
                result |= type.intValue();
            }
        }
        return result;
    }

    private static Uri UriFromString(String uri) {
        return TextUtils.isEmpty(uri) ? null : Uri.parse(uri);
    }

    private static String UriToString(Uri uri) {
        return uri == null ? "" : uri.toString();
    }

    private static InetAddress inetAddressFromString(String inetAddress) {
        if (TextUtils.isEmpty(inetAddress)) {
            return null;
        }
        try {
            return InetAddress.getByName(inetAddress);
        } catch (UnknownHostException e) {
            Log.e(LOG_TAG, "Can't parse InetAddress from string: unknown host.");
            return null;
        }
    }

    private static String inetAddressToString(InetAddress inetAddress) {
        if (inetAddress == null) {
            return null;
        }
        String inetAddressString = inetAddress.toString();
        if (TextUtils.isEmpty(inetAddressString)) {
            return null;
        }
        String hostName = inetAddressString.substring(null, inetAddressString.indexOf("/"));
        String address = inetAddressString.substring(inetAddressString.indexOf("/") + 1);
        if (TextUtils.isEmpty(hostName) && TextUtils.isEmpty(address)) {
            return null;
        }
        return TextUtils.isEmpty(hostName) ? address : hostName;
    }

    private static int portFromString(String strPort) {
        if (TextUtils.isEmpty(strPort)) {
            return -1;
        }
        try {
            return Integer.parseInt(strPort);
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "Can't parse port from String");
            return -1;
        }
    }

    private static String portToString(int port) {
        return port == -1 ? "" : Integer.toString(port);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mId);
        dest.writeString(this.mOperatorNumeric);
        dest.writeString(this.mEntryName);
        dest.writeString(this.mApnName);
        dest.writeValue(this.mProxyAddress);
        dest.writeInt(this.mProxyPort);
        dest.writeValue(this.mMmsc);
        dest.writeValue(this.mMmsProxyAddress);
        dest.writeInt(this.mMmsProxyPort);
        dest.writeString(this.mUser);
        dest.writeString(this.mPassword);
        dest.writeInt(this.mAuthType);
        dest.writeInt(this.mApnTypeBitmask);
        dest.writeInt(this.mProtocol);
        dest.writeInt(this.mRoamingProtocol);
        dest.writeInt(this.mCarrierEnabled);
        dest.writeInt(this.mMvnoType);
        dest.writeInt(this.mNetworkTypeBitmask);
    }

    private static ApnSetting readFromParcel(Parcel in) {
        Parcel parcel = in;
        return makeApnSetting(in.readInt(), in.readString(), in.readString(), in.readString(), (InetAddress) parcel.readValue(InetAddress.class.getClassLoader()), in.readInt(), (Uri) parcel.readValue(Uri.class.getClassLoader()), (InetAddress) parcel.readValue(InetAddress.class.getClassLoader()), in.readInt(), in.readString(), in.readString(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt() > 0, in.readInt(), 0, false, 0, 0, 0, 0, in.readInt(), null);
    }

    private static int nullToNotInMapInt(Integer value) {
        return value == null ? -1 : value.intValue();
    }
}
