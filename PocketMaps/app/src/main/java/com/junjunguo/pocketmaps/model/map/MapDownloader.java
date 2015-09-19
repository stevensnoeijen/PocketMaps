package com.junjunguo.pocketmaps.model.map;

import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.junjunguo.pocketmaps.model.listeners.MapDownloadListener;
import com.junjunguo.pocketmaps.model.util.Constant;
import com.junjunguo.pocketmaps.model.util.MyApp;
import com.junjunguo.pocketmaps.model.util.Variable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This file is part of PocketMaps
 * <p>
 * Created by GuoJunjun <junjunguo.com> on September 19, 2015.
 */
public class MapDownloader {
    private int timeout;
    private File downloadedFile;
    private boolean startNewDownload;
    /**
     * total length of the file
     */
    private long fileLength = 0;

    public MapDownloader() {
        timeout = 9000;
        startNewDownload = true;
    }

    /**
     * @param urlStr           downloadFile url
     * @param toFile           downloadedFile path
     * @param downloadListener downloadFile progress listener
     * @throws IOException
     */
    public void downloadFile(String urlStr, String toFile, String mapName, MapDownloadListener downloadListener)
            throws IOException {
        prepareDownload(urlStr, toFile);
        HttpURLConnection connection = createConnection(urlStr);
        Variable.getVariable().setDownloadStatus(Constant.DOWNLOADING);
        Variable.getVariable().setPausedMapName(mapName);
        if (!startNewDownload) {
            connection.setRequestProperty("Range", "bytes=" + downloadedFile.length() + "-");
        }

//        log("ResponseCode: " + connection.getResponseCode() + "; file length:" + fileLength + "\nResponseMessage: " +
//                connection.getResponseMessage());

        InputStream in = new BufferedInputStream(connection.getInputStream(), Constant.BUFFER_SIZE);
        FileOutputStream writer;
        long progressLength = 0;
        if (!startNewDownload) {
            progressLength += downloadedFile.length();
            // append to exist downloadedFile
            writer = new FileOutputStream(toFile, true);
        } else {
            writer = new FileOutputStream(toFile);
            // save remote last modified data to local
            Variable.getVariable().setMapLastModified(connection.getHeaderField("Last-Modified"));
        }
        try {
            byte[] buffer = new byte[Constant.BUFFER_SIZE];
            int count;
            while (Variable.getVariable().getDownloadStatus() == Constant.DOWNLOADING &&
                    (count = in.read(buffer)) != -1) {
                progressLength += count;
                writer.write(buffer, 0, count);
                // progress....
                downloadListener.progressUpdate((int) (progressLength * 100 / fileLength));
            }
            if (progressLength >= fileLength) {
                Variable.getVariable().setDownloadStatus(Constant.COMPLETE);
                Variable.getVariable().setPausedMapName("");
                new MapUnzip().unzip(toFile,
                        new File(Variable.getVariable().getMapsFolder(), mapName + "-gh").getAbsolutePath());
                downloadListener.downloadFinished();
            }

        } catch (Exception e) {
            MyApp.tracker().send(new HitBuilders.ExceptionBuilder()
                    .setDescription(this.getClass().getSimpleName() + e.getMessage()).setFatal(false).build());
        } finally {
            writer.close();
            in.close();
            connection.disconnect();
        }
    }

    /**
     * rend a request to server & decide to start a new download or not
     *
     * @param urlStr string url
     * @param toFile to file path
     * @throws IOException
     */
    private void prepareDownload(String urlStr, String toFile) throws IOException {
        HttpURLConnection conn = createConnection(urlStr);
        downloadedFile = new File(toFile);
        String remoteLastModified = conn.getHeaderField("Last-Modified");
        fileLength = conn.getContentLength();

        startNewDownload = (!downloadedFile.exists() || downloadedFile.length() >= fileLength ||
                !remoteLastModified.equalsIgnoreCase(Variable.getVariable().getMapLastModified()));
        conn.disconnect();
    }

    /**
     * @param urlStr url string
     * @return An URLConnection for HTTP
     * @throws IOException
     */
    private HttpURLConnection createConnection(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // Open connection to URL.
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setReadTimeout(timeout);
        conn.setConnectTimeout(timeout);
        return conn;
    }

    public void log(String s) {
        Log.i(this.getClass().getSimpleName(), "----" + s);
    }
}
