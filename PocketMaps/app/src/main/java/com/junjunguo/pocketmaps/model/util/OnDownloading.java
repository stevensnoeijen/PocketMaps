package com.junjunguo.pocketmaps.model.util;

import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 09, 2015.
 */
public class OnDownloading {
    private static OnDownloading onDownloading;

    public static OnDownloading getOnDownloading() {
        if (onDownloading == null) {
            onDownloading = new OnDownloading();
        }
        return onDownloading;
    }

    private OnDownloading() {
        this.downloadingProgressBar = null;
    }

    private ProgressBar downloadingProgressBar;
    private OnDownloadingListener listener;

    /**
     * can only get once, will sett til null after a get
     *
     * @return
     */
    public ProgressBar getDownloadingProgressBar() {
        ProgressBar pb = downloadingProgressBar;
        //        setDownloadingProgressBar(null);
        return pb;
    }

    public void setDownloadingProgressBar(TextView downloadStatus, ProgressBar downloadingProgressBar) {
        System.out.println("***** set downloding progress bar Vaiable *****" + downloadingProgressBar);
        this.downloadingProgressBar = downloadingProgressBar;
        listener.progressbarReady(downloadStatus,downloadingProgressBar);
    }

    public void setListener(OnDownloadingListener listener) {
        this.listener = listener;
    }
}
