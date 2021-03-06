package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Log;
import com.android.server.wifi.hotspot2.anqp.NAIRealmData;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.List;

public class WifiConfigStoreUtils {
    private static final int AUTO_CONNECT_DISABLED = 0;
    private static final int AUTO_CONNECT_ENABLED = 1;
    private static final boolean isAutoConnectPropEnabled;

    static {
        boolean z = false;
        if (SystemProperties.getInt("ro.config.auto_connect_attwifi", 0) == 1) {
            z = true;
        }
        isAutoConnectPropEnabled = z;
    }

    public static void enableAllNetworks(WifiConfigManager store) {
    }

    public static void loadConfiguredNetworks(WifiConfigManager store) {
        store.loadFromStore();
    }

    public static void disableAllNetworks(WifiConfigManager store) {
    }

    public static void loadAndEnableAllNetworks(WifiConfigManager store) {
    }

    public static List<WifiConfiguration> getConfiguredNetworks(WifiConfigManager store) {
        return store.getSavedNetworks();
    }

    public static boolean isOpenType(WifiConfiguration config) {
        boolean z = false;
        if (config == null || config.allowedKeyManagement.cardinality() > 1) {
            return false;
        }
        if (config.getAuthType() == 0) {
            z = true;
        }
        return z;
    }

    private static WifiConfiguration getSavedWifiConfig(Context context, int networkId) {
        if (context == null || networkId == -1) {
            return null;
        }
        List<WifiConfiguration> savedNetworks = ((WifiManager) context.getSystemService("wifi")).getConfiguredNetworks();
        if (savedNetworks == null) {
            return null;
        }
        for (int i = 0; i < savedNetworks.size(); i++) {
            WifiConfiguration tmp = (WifiConfiguration) savedNetworks.get(i);
            if (tmp.networkId == networkId) {
                return tmp;
            }
        }
        return null;
    }

    public static boolean ignoreEnableNetwork(Context context, int networkId, WifiNative wifiNative) {
        WifiConfiguration savedConfig = getSavedWifiConfig(context, networkId);
        if (context == null || savedConfig == null) {
            return false;
        }
        return ignoreEnableNetwork(context, savedConfig, wifiNative);
    }

    public static boolean ignoreEnableNetwork(Context context, WifiConfiguration config, WifiNative wifiNative) {
        if (!WifiProCommonUtils.isWifiProSwitchOn(context) || config == null || ((!config.noInternetAccess || WifiProCommonUtils.allowWifiConfigRecovery(config.internetHistory)) && (!config.portalNetwork || !isOpenType(config) || config.internetHistory == null || !config.internetHistory.contains("2")))) {
            return false;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ignoreEnableNetwork, disable config network in supplicant which has no internet again explicitly, netid = ");
        stringBuilder.append(config.networkId);
        stringBuilder.append(", ssid = ");
        stringBuilder.append(config.SSID);
        Log.d("WifiConfigStoreUtils", stringBuilder.toString());
        return true;
    }

    private static String convertToQuotedString(String string) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\"");
        stringBuilder.append(string);
        stringBuilder.append("\"");
        return stringBuilder.toString();
    }

    private static boolean existWifiApInDatabase(Context context, WifiConfiguration config) {
        int i = 0;
        if (context == null || config == null) {
            return false;
        }
        String data = System.getString(context.getContentResolver(), "attwifi_hotspot");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Database wifiap = ");
        stringBuilder.append(data);
        Log.d("WifiConfigStoreUtils", stringBuilder.toString());
        boolean isDatabaseWifi = false;
        if (data != null && !"".equals(data)) {
            String[] mAllSpots = data.split(NAIRealmData.NAI_REALM_STRING_SEPARATOR);
            while (i < mAllSpots.length) {
                isDatabaseWifi = config.SSID.equals(convertToQuotedString(mAllSpots[i]));
                if (isDatabaseWifi) {
                    break;
                }
                i++;
            }
        }
        return isDatabaseWifi;
    }

    private static boolean isAutoConnect(Context context) {
        boolean z = true;
        if (context == null || !isAutoConnectPropEnabled) {
            return true;
        }
        if (System.getInt(context.getContentResolver(), "auto_connect_att", 1) != 1) {
            z = false;
        }
        return z;
    }

    public static boolean isSkipAutoConnect(Context context, WifiConfiguration config) {
        if (!existWifiApInDatabase(context, config) || isAutoConnect(context)) {
            return false;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("isSkipAutoConnect ssid = ");
        stringBuilder.append(config.SSID);
        Log.d("WifiConfigStoreUtils", stringBuilder.toString());
        return true;
    }

    public static boolean skipEnableNetwork(Context context, int networkId, WifiNative wifiNative) {
        WifiConfiguration savedConfig = getSavedWifiConfig(context, networkId);
        if (savedConfig == null || !existWifiApInDatabase(context, savedConfig) || isAutoConnect(context)) {
            return false;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("skipEnableNetwork ssid = ");
        stringBuilder.append(savedConfig.SSID);
        Log.d("WifiConfigStoreUtils", stringBuilder.toString());
        return true;
    }
}
