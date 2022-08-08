package com.multimidia.arduino;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.io.Closer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Terminal {
    static public String exec(String command) {
        String retour = "";
        try {
            Runtime runtime = Runtime.getRuntime();

            Process p = runtime.exec(command);

            java.io.BufferedReader standardIn = new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getInputStream()));
            java.io.BufferedReader errorIn = new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getErrorStream()));
            String line = "";
            while ((line = standardIn.readLine()) != null) {
                retour += line + "\n";
            }
            while ((line = errorIn.readLine()) != null) {
                retour += line + "\n";
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        return retour;
    }
}
