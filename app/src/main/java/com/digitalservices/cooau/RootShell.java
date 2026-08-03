package com.digitalservices.cooau;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

public class RootShell {

    public static boolean execRoot(String command) {
        if (command == null || command.contains(";") || command.contains("&&") || command.contains("||") || command.contains("`")) {
            return false;
        }
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isVlcVideoPlayerTopRoot(String targetPackage, String targetActivity) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("dumpsys activity top\n");
            os.writeBytes("exit\n");
            os.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(targetPackage) && line.contains(targetActivity)) {
                    return true;
                }
            }
            process.waitFor();
        } catch (Exception ignored) {
        }
        return false;
    }

    public static boolean isVlcRunningRoot() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("ps\n");
            os.writeBytes("exit\n");
            os.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("org.videolan.vlc")) {
                    return true;
                }
            }
            process.waitFor();
        } catch (Exception ignored) {
        }
        return false;
    }
}
