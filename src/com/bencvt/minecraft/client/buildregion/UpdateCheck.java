package com.bencvt.minecraft.client.buildregion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Check the remote website for updates.
 * 
 * @author bencvt
 */
public class UpdateCheck {
    public static final String MAIN_URL = "http://www.minecraftforum.net/topic/1514724-";
    public static final String SHORT_URL = "http://bit.ly/BuildRegion";
    public static final String SOURCE_URL = "https://github.com/bencvt/BuildRegion";
    public static final String UPDATE_URL_PREFIX = "http://update.bencvt.com/u/BuildRegion?v=";

    private String result;

    public UpdateCheck(final String curVersion) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String response = getUrlContents(UPDATE_URL_PREFIX + curVersion);
                if (response == null) {
                    return;
                }
                // Parse response and set updateCheckResult, which will be
                // consumed and output later in the main thread.
                String[] lines = response.replaceAll("\t", "  ").split("\n");
                if (lines[0].startsWith("{")) {
                    // In case we ever want to switch to JSON in the future
                    setResult(buildOutput(""));
                    return;
                }
                // The first line is simply the latest published version.
                if (lines[0].compareTo(curVersion) <= 0) {
                    return;
                }
                // If the response contains lines of text after the version,
                // that's what we'll output to the user.
                StringBuilder b = new StringBuilder();
                for (int i = 1; i < lines.length; i++) {
                    if (!lines[i].isEmpty()) {
                        if (b.length() > 0) {
                            b.append('\n');
                        }
                        b.append(lines[i]);
                    }
                }
                if (b.length() > 0) {
                    setResult(b.toString());
                } else {
                    // The response was just the version.
                    setResult(buildOutput(lines[0]));
                }
            }
            private String buildOutput(String newVersion) {
                return new StringBuilder().append("\u00a7c")
                        .append("BuildRegion is out of date. ")
                        .append(newVersion.isEmpty() ? "A new version" : "Version ")
                        .append(newVersion)
                        .append(" is available at\n  \u00a7c")
                        .append(SHORT_URL).toString();
            }
        }).start();
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    /**
     * Attempt to retrieve the contents at the specified URL as a UTF8-encoded,
     * newline-normalized string. No special handling for redirects or other
     * HTTP return codes; just a quick-and-dirty GET. Return null if anything
     * breaks.
     */
    private static String getUrlContents(String url) {
        try {
            StringBuilder result = new StringBuilder();
            URLConnection conn = new URL(url).openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF8"));
            String line;
            while ((line = in.readLine()) != null) {
                if (result.length() > 0) {
                    result.append('\n');
                }
                result.append(line);
            }
            in.close();
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
