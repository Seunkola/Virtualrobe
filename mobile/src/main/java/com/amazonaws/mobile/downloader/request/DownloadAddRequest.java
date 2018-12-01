package com.amazonaws.mobile.downloader.request;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.amazonaws.mobile.downloader.query.DownloadQueueProvider;
import com.amazonaws.mobile.downloader.service.DownloadFlags;
import com.amazonaws.mobile.downloader.service.DownloadService;


/**
 * A class to represent a request to start a download.
 */
public class DownloadAddRequest implements Serializable {
    /** Logger. */
    private static final String LOG_TAG = DownloadAddRequest.class.getSimpleName();

    /** A string for exceptions. */
    private static final String MSG = "Both URI and location are required.";

    /** Serialization id. */
    private static final long serialVersionUID = 2753521672655218349L;

    /** The URL to download from. */
    private final URI url;

    /** The description of this request if any. */
    private final String description;

    /** The file location to download to. */
    private final String fileLocation;

    /** The download flags. */
    private final int downloadFlags;

    /** The serialized intent URI that initiated the download (except excluding the DownloadService specific extras). */
    private final String intentURI;

    /** The title of this request if any. */
    private final String title;

    /**
     * Build a new instance from an intent.
     * throws IllegalArgumentException if the intent is not suitable
     * 
     * @param intent
     *            the Intent to use
     * @return the new instance
     */
    public static DownloadAddRequest fromIntent(final Intent intent) {
        final String url = intent.getStringExtra(DownloadService.EXTRA_URL);
        final String location = intent.getStringExtra(DownloadService.EXTRA_LOCATION);
        final String description = intent.getStringExtra(DownloadService.EXTRA_DESCRIPTION);
        final int downloadFlags = intent.getIntExtra(DownloadService.EXTRA_DOWNLOAD_FLAGS, 0);
        final String title = intent.getStringExtra(DownloadService.EXTRA_DOWNLOAD_TITLE);

        if (null == url || null == location) {
            throw new IllegalArgumentException(MSG);
        }

        URI urlObject;
        try {
            urlObject = new URI(url);
        } catch (final URISyntaxException ex) {
            throw new IllegalArgumentException(MSG, ex);
        }

        // Copy intent to remove extras for this service (DownloadService) that we are already persisting.
        final Intent intentCopy = (Intent) intent.clone();
        // don't serialize this as an explicit intent.
        intentCopy.setComponent(null);
        final Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                if (key.startsWith(DownloadService.EXTRA_PREFIX)) {
                    intentCopy.removeExtra(key);
                }
            }
        }
        // Serialize the intent
        final String intentURI = intentCopy.toUri(0);

        return new DownloadAddRequest(urlObject, description, location, intentURI, downloadFlags,
            title != null ? title : "");
    }

    /**
     * Create a new instance from a builder.
     * 
     * @param builder
     *            the builder
     */
    private DownloadAddRequest(final Builder builder) {
        this.url = builder.url;
        this.description = builder.description;
        this.fileLocation = builder.fileLocation;
        this.intentURI = builder.intentURI;
        int flags = 0;
        if (builder.isForeground) {
            flags |= DownloadFlags.FLAG_FOREGROUND;
        }
        if (builder.isSilent) {
            flags |= DownloadFlags.FLAG_SILENT;
        }
        if (builder.wifiLock) {
            flags |= DownloadFlags.FLAG_WIFI_LOCK;
        }
        if (builder.mobileNetworkProhibited) {
            flags |= DownloadFlags.FLAG_MOBILE_NETWORK_PROHIBITED;
        }

        downloadFlags = flags;
        this.title = builder.title;
    }

    /**
     * Create a new instance.
     *
     * @param url the url.
     * @param description the description.
     * @param fileLocation the file location.
     * @param intentURI an intent URI to persist along with this download.
     * @param downloadFlags the download flags.
     * @param title the title for the download.
     */
    private DownloadAddRequest(final URI url, final String description, final String fileLocation,
                               final String intentURI, final int downloadFlags, final String title) {
        this.url = url;
        this.description = description;
        this.fileLocation = fileLocation;
        this.intentURI = intentURI;
        this.downloadFlags = downloadFlags;
        this.title = title;
    }
    
    /**
     * Convert this request to an intent.
     * @param context context used to set class on intent.
     * @return the intent.
     */
    public Intent toIntent(final Context context) {
        Intent intent;

        if (this.intentURI == null) {
            intent = new Intent();
        } else {
            try {
                intent = Intent.parseUri(this.intentURI, 0);
            } catch (URISyntaxException ex) {
                Log.d(LOG_TAG, "Existing intent URI had invalid syntax.", ex);
                intent = new Intent();
            }
        }

        if (null != context) {
            intent.setClass(context, DownloadService.class);
        }
        intent.setAction(DownloadService.ACTION_REQUEST_DOWNLOAD);
        intent.putExtra(DownloadService.EXTRA_URL, url.toString());
        intent.putExtra(DownloadService.EXTRA_LOCATION, fileLocation);
        intent.putExtra(DownloadService.EXTRA_DOWNLOAD_FLAGS, downloadFlags);

        if (null != description) {
            intent.putExtra(DownloadService.EXTRA_DESCRIPTION, description);
        }
        if (title != null) {
            intent.putExtra(DownloadService.EXTRA_DOWNLOAD_TITLE, title);
        }

        return intent;
    }

    /** Expose the file location so it can be checked for duplicates.
     *
     * @return the file location for this download request.
     */
    public String getFileLocation() {
        return fileLocation;
    }

    /**
     * Convert to content values.
     * 
     * @return the content values.
     */
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(DownloadQueueProvider.COLUMN_DOWNLOAD_ID, (String) null);
        values.put(DownloadQueueProvider.COLUMN_DOWNLOAD_URL, url.toString());
        values.put(DownloadQueueProvider.COLUMN_DOWNLOAD_DESCRIPTION, description);
        values.put(DownloadQueueProvider.COLUMN_DOWNLOAD_FILE_LOCATION, fileLocation);
        values.put(DownloadQueueProvider.COLUMN_DOWNLOAD_TITLE, title);
        values.put(DownloadQueueProvider.COLUMN_DOWNLOAD_USER_FLAGS, downloadFlags);
        values.put(DownloadQueueProvider.COLUMN_DOWNLOAD_INTENT_URI, intentURI);
        return values;
    }

    /**
     * A class that can build a request.
     */
    public static class Builder {
        /** The url. */
        private final URI url;

        /** The description. */
        private String description = "";

        /** The file location. */
        private final String fileLocation;

        /** Whether this is a "foreground" request. */
        private boolean isForeground;

        /** Whether this is a "silent" request. */
        private boolean isSilent;

        /** Whether we need to acquire the wifi lock. */
        private boolean wifiLock;

        private boolean mobileNetworkProhibited;

        /** The initiating intent. */
        private String intentURI = null;

        /** The title. */
        private String title = "";

        /**
         * Create a new builder.
         * 
         * @param url
         *            the URL
         * @param fileLocation
         *            the file location.
         */
        public Builder(final URI url, final String fileLocation) {
            this.url = url;
            this.fileLocation = fileLocation;
        }

        /**
         * Set the description of the request.
         * 
         * @param newDescription
         *            the description.
         * @return returns this for fluent coding
         */
        public Builder setDescription(final String newDescription) {
            this.description = newDescription;
            return this;
        }

        /**
         * Set whether this is a foreground request.
         * 
         * @param foreground
         *            whether foreground
         * @return the builder.
         */
        public Builder setForeground(final boolean foreground) {
            this.isForeground = foreground;
            return this;
        }

        /**
         * Set whether this is a silent request.
         * 
         * @param silent
         *            whether silent
         * @return the builder.
         */
        public Builder setSilent(final boolean silent) {
            this.isSilent = silent;
            return this;
        }

        /**
         * Sets whether to acquire the wifi lock.
         * 
         * @param aWifiLock
         *            whether to acquire the lock or not
         * @return the builder
         */
        public Builder setWifiLock(final boolean aWifiLock) {
            this.wifiLock = aWifiLock;
            return this;
        }

        /**
         * Sets whether the download started by this request may use a Cellular network.
         * @param mobileNetworkProhibited should be passed as true if a cellular should not be used, otherwise false.
         * @return the builder.
         */
        public Builder setMobileNetworkProhibited(final boolean mobileNetworkProhibited) {
            this.mobileNetworkProhibited = mobileNetworkProhibited;
            return this;
        }

        /**
         * Set more extras of the request.
         * 
         * @param anIntentURI
         *            the serialized original intent
         * @return returns this for fluent coding
         */
        public Builder setIntentURI(final String anIntentURI) {
            this.intentURI = anIntentURI;
            return this;
        }

        /**
         * Set the title of the request.
         * 
         * @param newTitle
         *            the title.
         * @return returns this for fluent coding
         */
        public Builder setTitle(final String newTitle) {
            this.title = newTitle;
            return this;
        }

        /**
         * Build an actual request.
         * 
         * @return the request
         */
        public DownloadAddRequest build() {
            final DownloadAddRequest result = new DownloadAddRequest(this);
            return result;
        }
    }
}
