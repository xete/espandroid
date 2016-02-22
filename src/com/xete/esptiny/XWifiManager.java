package com.xete.esptiny;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;

public class XWifiManager {

	private final WifiManager mWifiManager;	

	public XWifiManager(Context context) {
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}

	public String getIpAddr() {
		WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
		return ipInt2Str(wifiInfo.getIpAddress());
	}

	private String ipInt2Str(int ipInt) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.valueOf(ipInt&0xff));
		sb.append(".");
		sb.append(String.valueOf((byte)(ipInt>>8)&0xff));
		sb.append(".");
		sb.append(String.valueOf((byte)((ipInt>>16)&0xff)));
		sb.append(".");
		sb.append(String.valueOf((byte)((ipInt>>24)&0xff)));
		return sb.toString();
	}
}
