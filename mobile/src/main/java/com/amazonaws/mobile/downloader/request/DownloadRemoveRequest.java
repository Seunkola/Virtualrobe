package com.amazonaws.mobile.downloader.request;

import java.io.Serializable;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.amazonaws.mobile.downloader.service.DownloadService;

/**
 * A class which represents a request to remove a download.
 */
public class DownloadRemoveRequest implements Serializable {

    /** A message thrown when there is no ID in the intent. */
    private static final String MSG_NO_ID = "A downloadId is required in the intent.";

    /** Serialization UID. */
    private static final long serialVersionUID = 5141378432218599173L;

    /** The download ID to remove. */
    private final long downloadId;

    /** our extras. */
    private final Bundle extras;

    /**
     * Build a new instance from an intent.
     * throws IllegalArgumentException if the intent is not suitable
     * @param intent the Intent to use
     * @return the new instance
     */
    public static DownloadRemoveRequest fromIntent(final Intent intent) {

        final long downloadId = intent.getLongExtra(DownloadService.EXTRA_LONG_ID, DownloadService.INVALID_ID);
        if (downloadId == DownloadService.INVALID_ID) {
            throw new IllegalArgumentException(MSG_NO_ID);
        }
        final Bundle extras = intent.getExtras();
        final DownloadRemoveRequest result =  new DownloadRemoveRequest(downloadId, extras);
        return result;
    }

    /**
     * Create a new instance.
     * @param downloadId the ID to remove.
     */
    public DownloadRemoveRequest(final long downloadId) {
        this.downloadId = downloadId;
        this.extras = new Bundle(); // empty bundle
    }

    /**
     * Create a new instance.
     * @param downloadId the ID to remove.
     * @param extras the extras to pass around.
     */
    public DownloadRemoveRequest(final long downloadId, final Bundle extras) {
        this.downloadId = downloadId;
        if (extras == null) {
            this.extras = new Bundle();
        } else {
            this.extras = extras;
        }
    }

    /**
     * Convert this object to an intent.
     * 
     * @return the intent
     * @deprecated Use toIntent(Context) instead.
     */
    @Deprecated
    public Intent toIntent() {
        return toIntent(null);
    }

    /**
     * Convert this object to an intent.
     * @param context context used to set class on intent.
     * @return the intent
     */
    public Intent toIntent(final Context context) {
        final Intent intent = new Intent();
        if (null != context) {
            intent.setClass(context, DownloadService.class);
        }
        intent.setAction(DownloadService.ACTION_REMOVE_DOWNLOAD);
        // extras are never null
        intent.putExtras(extras);
        intent.putExtra(DownloadService.EXTRA_LONG_ID, downloadId);
        return intent;
    }


    /**
     * @return the downloadId
     */
    public long getDownloadId() {
        return downloadId;
    }

    /**
     * @return the extras
     */
    public Bundle getExtras() {
        return extras;
    }

}
