package com.example.sks.voiceservice.util;
import android.os.Process;
import android.util.Log;

import java.io.FileInputStream;

public class ProcessNameUtil {
    private static final String CMDLINE_FORMAT_STRING = "/proc/%d/cmdline";
    private static final int MAX_PKGNAME_LEN = 128;
    private static final String TAG = "ProcessNameUtil";

    public static String getCurrentProcessName() {
        return getProcessName(Process.myPid());
    }

    public static String getProcessNameSuffix() {
        String processName = getCurrentProcessName();
        int index = processName.indexOf(":");
        if (index == -1 || ((index + 1) >= processName.length())) {
            return "";
        } else {
            return processName.substring(index + 1);
        }
    }

    public static String getProcessPackageName(int pid) {
        String processName = getProcessName(pid);
        return getPackageNameFromProcessName(processName);
    }

    public static String getPackageNameFromProcessName(String processName) {
        int index;
        if (processName == null || (index = processName.indexOf(':')) == -1) {
            return processName;
        }
        return processName.substring(0, index);
    }

    public static String getProcessName(int pid) {
        String cmdlinePath = String.format(CMDLINE_FORMAT_STRING, pid);
        FileInputStream fis = null;
        String pkgname = "";
        try {
            fis = new FileInputStream(cmdlinePath);

            byte[] buffer = new byte[MAX_PKGNAME_LEN];

            int len = 0;
            int b;
            while ((b = fis.read()) > 0 && len < buffer.length) {
                buffer[len++] = (byte) b;
            }
            if (len > 0) {
                pkgname = new String(buffer, 0, len, "UTF-8");
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception ignored) {
                }
            }
        }
        return pkgname;
    }

}
