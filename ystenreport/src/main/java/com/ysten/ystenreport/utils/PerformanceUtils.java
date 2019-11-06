package com.ysten.ystenreport.utils;

import android.app.ActivityManager;
import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

public class PerformanceUtils {
    private static final String TAG = "Report_PerformanceUtils";

    private static int sCoreNum = 0;
    private static long sTotalMemo = 0;

    private PerformanceUtils() {
        throw new InstantiationError("Must not instantiate this class");
    }

    /**
     * Get cpu core number
     *
     * @return int cpu core number
     */
    public static int getNumCores() {
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                return Pattern.matches("cpu[0-9]", pathname.getName());
            }
        }

        if (sCoreNum == 0) {
            try {
                // Get directory containing CPU info
                File dir = new File("/sys/devices/system/cpu/");
                // Filter to only list the devices we care about
                File[] files = dir.listFiles(new CpuFilter());
                // Return the number of cores (virtual CPU devices)
                sCoreNum = files.length;
            } catch (Exception e) {
                LogUtil.e(TAG, "getNumCores exception");
                sCoreNum = 1;
            }
        }
        return sCoreNum;
    }

    public static long getFreeMemory(Context appContext) {
        ActivityManager am = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem;
    }

    public static long getTotalMemory() {
        if (sTotalMemo == 0) {
            String str1 = "/proc/meminfo";
            String str2;
            String[] arrayOfString;
            long initial_memory = -1;
            FileReader localFileReader = null;
            try {
                localFileReader = new FileReader(str1);
                BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
                str2 = localBufferedReader.readLine();

                if (str2 != null) {
                    arrayOfString = str2.split("\\s+");
                    initial_memory = Integer.valueOf(arrayOfString[1]);
                }
                localBufferedReader.close();

            } catch (IOException e) {
                LogUtil.e(TAG, "getTotalMemory exception = ");
            } finally {
                if (localFileReader != null) {
                    try {
                        localFileReader.close();
                    } catch (IOException e) {
                        LogUtil.e(TAG, "close localFileReader exception = ");
                    }
                }
            }
            sTotalMemo = initial_memory * 1024;
        }
        
       
       
        return sTotalMemo;
    }
}