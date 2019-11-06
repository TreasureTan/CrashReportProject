package com.ysten.ystenreport.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by zyx on 2019/9/14.
 */
public class MacUtil {
    private static final String TAG = "MacUtil";
    private static final String FILENAME_ETH0_MAC_ADDRESS = "/sys/class/net/eth0/address";
    private static String mac;

    public static String getMac() {
        long startT = System.currentTimeMillis();
        if (!TextUtils.isEmpty(mac)) {
            Log.d(TAG, "getMac-useTime 已有mac->");
            return mac;
        }
        mac = getSystemProp("persist.sys.icntv.mac");
        if (!TextUtils.isEmpty(mac)) {
            Log.d(TAG, "getMacFromProp mac:" + mac);
            return mac;
        }
        mac = getEth0Mac();
//        mac = "0c:c6:55:8d:6e:d6";
        return mac;
    }

    private static String getSystemProp(String key) {
        InputStreamReader ir = null;
        BufferedReader input = null;
        try {
            Process process = Runtime.getRuntime().exec("getprop " + key);
            ir = new InputStreamReader(process.getInputStream(), "UTF-8");
            input = new BufferedReader(ir);
            String line;
            while ((line = input.readLine()) != null) {
                if (!TextUtils.isEmpty(line)) {
                    Log.d(TAG, "getSystemProp: " + key + " : " + line);
                    return line;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "getSystemProp: error .---> ", e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (ir != null) {
                try {
                    ir.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    private static String getEth0Mac() {
        String mac;
        mac = getEthernetMacAddress();
        if (TextUtils.isEmpty(mac)) {
            mac = getMacFromEth();
        }
        if (TextUtils.isEmpty(mac)) {
            mac = getMacFromNetworkInterface();
        }
        if (TextUtils.isEmpty(mac)) {
            mac = "00:00:00:00:00:00";
        }
        Log.d(TAG, "mac:" + mac);
        return mac.trim();
    }

    private static String getEthernetMacAddress() {
        BufferedReader reader = null;
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(new FileInputStream(new File(FILENAME_ETH0_MAC_ADDRESS)), "UTF-8");
            reader = new BufferedReader(isr, 256);
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }


    private static String getMacFromEth() {
        String mac = "";
        InputStreamReader ir = null;
        LineNumberReader input = null;
        try {
            Process process = Runtime.getRuntime().exec("busybox ifconfig eth0");
            ir = new InputStreamReader(process.getInputStream(), "UTF-8");
            input = new LineNumberReader(ir);
            String line;
            while ((line = input.readLine()) != null) {
                if (line.indexOf("HWaddr ") > 0) {
                    mac = line.substring(line.indexOf("HWaddr ") + 7);
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("IOException " + e.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (ir != null) {
                try {
                    ir.close();
                    ir = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (TextUtils.isEmpty(mac) || mac.length() < 10) {
            try {
                Process process = Runtime.getRuntime().exec("ifconfig eth0");
                ir = new InputStreamReader(process.getInputStream(), "UTF-8");
                input = new LineNumberReader(ir);
                String line;

                while ((line = input.readLine()) != null) {
                    if (line.indexOf("HWaddr ") > 0) {
                        mac = line.substring(line.indexOf("HWaddr ") + 7);
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("IOException " + e.getMessage());
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (ir != null) {
                    try {
                        ir.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return mac;
    }

    private static String getMacFromNetworkInterface() {
        Log.v(TAG, "start getMac2");
        String mac = "";
        try {
            Enumeration<?> localEnumeration = NetworkInterface.getNetworkInterfaces();
            while (localEnumeration.hasMoreElements()) {
                NetworkInterface localNetworkInterface = (NetworkInterface) localEnumeration.nextElement();
                String interfaceName = localNetworkInterface.getDisplayName();
                if (interfaceName == null) {
                    continue;
                }
                Log.i(TAG, "name=" + interfaceName);
                if (interfaceName.equals("eth0")) {
                    mac = convertMac(localNetworkInterface.getHardwareAddress());
                    if (mac.startsWith("0:")) {
                        mac = "0" + mac;
                    }
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "start end");
        return mac;
    }

    private static String convertMac(byte[] mac) {
        StringBuilder macBuilder = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            byte b = mac[i];
            int intValue;
            if (b >= 0)
                intValue = b;
            else
                intValue = 256 + b;
            macBuilder.append(Integer.toHexString(intValue));
            if (i != mac.length - 1)
                macBuilder.append(":");
        }
        return macBuilder.toString();
    }

    public static boolean verifyMac(String mac) {
        if (TextUtils.isEmpty(mac)) {
            return false;
        }

        String rules = "([A-Fa-f0-9]{2}:){5}[A-Fa-f0-9]{2}";
        if (mac.matches(rules)) {
            return true;
        } else {
            return false;
        }
    }
}
