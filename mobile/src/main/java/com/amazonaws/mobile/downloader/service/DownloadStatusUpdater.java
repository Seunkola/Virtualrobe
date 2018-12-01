package com.amazonaws.mobile.downloader.service;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.amazonaws.mobile.downloader.query.BasicDownloadInfo;
import com.amazonaws.mobile.downloader.query.DownloadState;
import com.amazonaws.mobile.downloader.query.DownloadQueueProvider;

import java.net.HttpURLConnection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DownloadStatusUpdater {
    /** Our logger, for messages. */
    private static final String LOG_TAG = DownloadStatusUpdater.class.getSimpleName();

    /** An in-memory cache of download related information. */
    private final ConcurrentMap<String, DownloadStatus> downloadStatuses;

    /** When we last updated the database. */
    private static long lastDatabaseUpdate = 0;

    /** How many nanoseconds in a second. */
    private static final long NANOS_PER_SECOND = 1000000000;

    /** How frequently we're allowed to update the database. */
    private static final long MIN_UPDATE_TIME = 2 * NANOS_PER_SECOND;

    /**
     * The context.
     */
    private final Context context;

    /** Secure broadcast manager. */
    private final LocalBroadcastManager secureBroadcastManager;

    /** The content URI for the download queue. */
    private final Uri downloadQueueContentUri;

    /** Keeps track of downloads by ID for which we don't want to send progress. */
    private final HashSet<Long> mutedDownloadIds = new HashSet<Long>();

    /**
     * The constructor.
     * 
     * @param context
     *            Android context
     * @param localBroadcastManager the broadcast manager to send updates.
     */
    public DownloadStatusUpdater(final Context context, final LocalBroadcastManager localBroadcastManager) {
        this.context = context;
        this.secureBroadcastManager = localBroadcastManager;
        downloadQueueContentUri = DownloadQueueProvider.getDownloadContentUri(context);
        downloadStatuses = new ConcurrentHashMap<String, DownloadStatus>();
    }

    /**
     * Receive notification that a download started.
     * 
     * @param longDownloadId
     *            the ID
     */
    public void start(final long longDownloadId) {
        final String downloadId = String.valueOf(longDownloadId);

        final DownloadStatus downloadStatus = getDownloadStatus(downloadId);

        synchronized (downloadStatus.lock) {
            // update state in content provider
            final ContentValues values = new ContentValues();
            final String newState = DownloadState.IN_PROGRESS
                .toString();
            values.put(DownloadQueueProvider.COLUMN_DOWNLOAD_STATUS, newState);
            final ContentResolver resolver = context.getContentResolver();
            resolver.update(downloadQueueContentUri, values,
                DownloadQueueProvider.COLUMN_DOWNLOAD_ID + " = ?",
                new String[] {
                    downloadId
                });

            // broadcast a notification
            final Intent notification = getDownloadIntent(longDownloadId, downloadStatus);
            notification.setAction(DownloadService.ACTION_DOWNLOAD_STARTED);
            notification.putExtra(DownloadService.EXTRA_LONG_ID, longDownloadId);

            secureBroadcastManager.sendBroadcast(notification);
                // Mute this if silent? answer from patb for now is no.
        }
    }

    /**
     * Receive notification that we got headers etc. example headers: [ Date:
     * Thu, 01 Nov 2012 20:21:54 GMT, Server: Apache/2.2.22 (Fedora),
     * Last-Modified: Sun, 28 Oct 2012 19:38:20 GMT, ETag:
     * "75c317b-4fb985a-4cd23b1f4c300", Accept-Ranges: bytes, Content-Length:
     * 83597402, Keep-Alive: timeout=5, max=1000, Connection: Keep-Alive,
     * Content-Type: application/x-bzip2 ]
     * 
     * @param longDownloadId
     *            the ID
     * @param connection
     *            the http connection to use to check response headers.
     */
    public void headersReceived(final long longDownloadId,
        final HttpURLConnection connection) {
        final String downloadId = String.valueOf(longDownloadId);

        final DownloadStatus downloadStatus = getDownloadStatus(downloadId);

        synchronized (downloadStatus.lock) {
            final ContentValues values = new ContentValues();

            final String eTag = connection.getHeaderField(Downloader.HEADER_ETAG);
            if (null != eTag) {
                values.put(DownloadQueueProvider.COLUMN_DOWNLOAD_ETAG, eTag);
            }

            final String contentType = connection.getHeaderField(Downloader.HEADER_CONTENT_TYPE);
            if (null != contentType) {
                values.put(DownloadQueueProvider.COLUMN_DOWNLOAD_MIME_TYPE,
                    contentType);
            }

            if (0 < values.size()) {
                final ContentResolver resolver = context.getContentResolver();
                resolver.update(downloadQueueContentUri, values,
                    DownloadQueueProvider.COLUMN_DOWNLOAD_ID + " = ?",
                    new String[] {
                        downloadId
                    });
            }

            // Ensure that headers (such as mime type) will be updated from ContentResolver
            downloadStatus.setDownloadInfo(null);
        }
    }

    /***
     * Keep track of download IDs we want to not send progress broadcasts about.
     * @param downloadId the id of the download
     */
    public void addMutedDownloadId(final long downloadId) {
        mutedDownloadIds.add(downloadId);
    }

    /***
     * Remove download ID from our list of muted downloads, since it's now finished. Safe to use
     * even if this ID was not in the list
     * @param downloadId the id of the download
     */
    public void removeMutedDownloadId(final long downloadId) {
        mutedDownloadIds.remove(downloadId);
    }

    /**
     * Determine whether given download progress broadcast should be sent.  Initialize the progress
     * measurer encapusulated in the downloadStatus object, if necessary.
     *
     * @param downloadStatus
     *      the object tracking the download status of the given request.
     * @param longDownloadId
     *      download request id.
     * @param bytesRead
     *      the bytes successfully downloaded.
     * @param totalBytes
     *      total number of bytes if known, or -1 if not known.
     * @return
     *      true if progress broadcast should be sent, false otherwise.
     */
    private boolean shouldBroadcastProgress(final DownloadStatus downloadStatus, final long longDownloadId,
                                            final long bytesRead, final long totalBytes) {
        if (totalBytes < 0) {
            Log.d(LOG_TAG, "unknown total bytes.  Not sending progress broadcast.");
            return false;
        }

        if (mutedDownloadIds.contains(longDownloadId)) {
            return false;
        }

        DownloadSpeedMeasurer downloadSpeedMeasurer = downloadStatus.getProgressMeasurer();
        if (downloadSpeedMeasurer == null) {
            downloadSpeedMeasurer = new DownloadSpeedMeasurer(bytesRead, totalBytes);
            downloadStatus.setProgressMeasurer(downloadSpeedMeasurer);
            // Call to a newly created downloadSpeedMeasurer's updateProgress method will always
            // return false, but we do want to report progress in this case.
            return true;
        }
        return downloadSpeedMeasurer.updateProgress(bytesRead);
    }

    /**
     * Receive notification that progress happened. We send a progress
     * broadcast, and optionally update the content provider.
     * 
     * @param longDownloadId
     *            the task of which to update progress
     * @param bytesRead
     *            number of bytes transferred
     * @param totalBytes
     *            total number of bytes if known, or -1 if not known
     */
    public void sendProgress(final long longDownloadId, final long bytesRead,
        final long totalBytes) {
        final String downloadId = String.valueOf(longDownloadId);

        final DownloadStatus downloadStatus = getDownloadStatus(downloadId);

        synchronized (downloadStatus.lock) {
            long bytesDownloaded = bytesRead;
            // Make sure download progress doesn't exceed 100%
            if (bytesRead > totalBytes) {
                bytesDownloaded = totalBytes;
            }

            if (shouldBroadcastProgress(downloadStatus, longDownloadId, bytesRead, totalBytes)) {
                final Intent notification = getDownloadIntent(longDownloadId, downloadStatus);
                notification.setAction(DownloadService.ACTION_DOWNLOAD_PROGRESS);
                notification.putExtra(DownloadService.EXTRA_LONG_ID,
                        longDownloadId);
                notification.putExtra(
                        DownloadService.EXTRA_LONG_PROGRESS_CUMULATIVE,
                        bytesDownloaded);
                notification.putExtra(
                    DownloadService.EXTRA_LONG_PROGRESS_TOTAL_SIZE,
                    totalBytes);

                secureBroadcastManager.sendBroadcast(notification);
            }

            if (mayUpdateDatabase()) {
                final ContentValues values = new ContentValues();
                values.put(DownloadQueueProvider.COLUMN_DOWNLOAD_CURRENT_SIZE,
                    bytesDownloaded);
                values.put(DownloadQueueProvider.COLUMN_DOWNLOAD_TOTAL_SIZE,
                    totalBytes);
                final ContentResolver resolver = context.getContentResolver();
                resolver.update(downloadQueueContentUri,
                    values,
                    DownloadQueueProvider.COLUMN_DOWNLOAD_ID + " = ?",
                    new String[] {
                        downloadId
                    });

                // update the cache for the size of the download
                if (intentValuesAreCached(downloadStatus)) {
                    downloadStatus.setDownloadSize(totalBytes);
                }
            }
        }
    }

    /**
     * Receive notification that the download terminated, whether successfully or
     * not.
     * 
     * @param longDownloadId
     *            the id
     * @param withStatus
     *            whether it worked or not
     * @param completionMessage
     *            the completion message if any
     * @param bytesRead
     *            the bytes successfully downloaded
     * @param totalBytes
     *            the total size of the entity to download
     * @param autoRestart
     *            whether this was an automatic restart of something
     * @param downloadError
     *            error code with a DownloadError enum value
     */
    public void finish(final long longDownloadId, final CompletionStatus withStatus,
                       final String completionMessage, final long bytesRead, final long totalBytes,
                       final boolean autoRestart, final String downloadError) {
        final String downloadId = String.valueOf(longDownloadId);

        final DownloadStatus downloadStatus = getDownloadStatus(downloadId);

        synchronized (downloadStatus.lock) {
            Log.i(LOG_TAG, "downloadTaskComplete, id = " + downloadId + " status: " + withStatus);

            String newState = null;
            String oldState = null;
            String action = null;

            switch (withStatus) {
                case SUCCEEDED:
                    newState = DownloadState.COMPLETE.toString();
                    action = DownloadService.ACTION_DOWNLOAD_COMPLETE;
                    break;
                case PAUSED:
                case PAUSED_BY_USER:
                    newState = DownloadState.PAUSED.toString();
                    action = DownloadService.ACTION_DOWNLOAD_PAUSED;
                    break;
                case FAILED:
                    newState = DownloadState.FAILED.toString();
                    action = DownloadService.ACTION_DOWNLOAD_FAILED;
                    break;
            }

            // Do not send out notification if the value before and after update is the same
            // since we already sent the broadcast before.
            final ContentResolver resolver = context.getContentResolver();
            final Cursor cursor = resolver.query(downloadQueueContentUri,
                new String[] {
                    DownloadQueueProvider.COLUMN_DOWNLOAD_STATUS
                }, DownloadQueueProvider.COLUMN_DOWNLOAD_ID + " = ?",
                new String[] {
                    downloadId
                }, null);

            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        oldState = cursor.getString(0);
                    }
                } finally {
                    cursor.close();
                }
            }

            if (newState != null && newState.equals(oldState) &&
                    // Don't detect duplicate state for paused by user, since in
                    // that scenario the state is set to paused immediately.
                    CompletionStatus.PAUSED_BY_USER != withStatus) {
                Log.v(LOG_TAG, "Duplicate update request download state: " + oldState
                     + ". Skip update again for downloadId: " + downloadId);
                return;
            }

            // Update content provider status.
            final ContentValues values = new ContentValues();
            values.put(DownloadQueueProvider.COLUMN_DOWNLOAD_STATUS, newState);
            if (null != completionMessage) {
                values.put(DownloadQueueProvider.COLUMN_DOWNLOAD_STOP_REASON,
                    completionMessage);
            }

            resolver.update(downloadQueueContentUri, values,
                DownloadQueueProvider.COLUMN_DOWNLOAD_ID + " = ?",
                new String[] {
                    downloadId
                });

            // Broadcast a notification.
            final Intent notification = getDownloadIntent(longDownloadId, downloadStatus);
            notification.putExtra(DownloadService.EXTRA_LONG_ID, longDownloadId);
            if (null != completionMessage) {
                notification.putExtra(DownloadService.EXTRA_COMPLETION_MESSAGE,
                        completionMessage);
            }
            notification.putExtra(DownloadService.EXTRA_DOWNLOAD_ERROR,
                    downloadError);
            if (CompletionStatus.PAUSED == withStatus) {
                notification.putExtra(
                        DownloadService.EXTRA_LONG_PROGRESS_CUMULATIVE,
                        bytesRead);
                notification.putExtra(
                        DownloadService.EXTRA_LONG_PROGRESS_TOTAL_SIZE,
                        totalBytes);
            }
            notification.setAction(action);

            clearCachedValues(downloadId);

            if (autoRestart) {
                notification.putExtra(DownloadService.EXTRA_BOOL_AUTO_RESTART,
                    true);
            }

            secureBroadcastManager.sendBroadcast(notification);

            // Start DownloadService.
            final Intent notifyDownloadFinishedIntent = new Intent();
            notifyDownloadFinishedIntent.setAction(action);
            notifyDownloadFinishedIntent.setClass(context,
                DownloadService.class);
            context.startService(notifyDownloadFinishedIntent);

            // Remove muted download if appropriate (not bothering to check if it was a silent request
            // because this method will only remove if necessary)
            if (DownloadService.ACTION_DOWNLOAD_COMPLETE.equals(action)
                    || DownloadService.ACTION_DOWNLOAD_FAILED.equals(action)) {
                removeMutedDownloadId(longDownloadId);
            }

            Log.i(LOG_TAG, "done with downloadTaskComplete, id = " + downloadId);
        }
    }

    /**
     * Add all the serialized values to the intent. May use cached version is
     * available.
     *
     * @param longId
     *            the download ID
     * @param downloadStatus
     *            the download status
     * @return the download intent.
     */
    /* package */ Intent getDownloadIntent(final long longId,
                                          final DownloadStatus downloadStatus) {
        final Intent intent;
        if (!intentValuesAreCached(downloadStatus)) {
            cacheIntentValues(longId, downloadStatus);
        }

        final BasicDownloadInfo downloadInfo = downloadStatus.getDownloadInfo();
        if (null != downloadInfo) {
            intent = downloadInfo.getIntent();

            putExtra(intent, DownloadService.EXTRA_URL, downloadInfo.getDownloadUrl());
            putExtra(intent, DownloadService.EXTRA_LOCATION, downloadInfo.getDestinationFileUri());
            putExtra(intent, DownloadService.EXTRA_MIME_TYPE, downloadInfo.getMimeType());

            long startTime = downloadInfo.getCreationTimestamp();
            if (startTime != 0) {
                intent.putExtra(DownloadService.EXTRA_LONG_START_TIME, startTime);
            } else {
                Log.e(LOG_TAG, "Invalid start time was retrieved from the database.");
            }
            final long endTime = System.currentTimeMillis();

            intent.putExtra(DownloadService.EXTRA_LONG_DURATION, endTime - downloadStatus.getDownloadStartTime());
            intent.putExtra(DownloadService.EXTRA_LONG_END_TIME, endTime);

            final long totalSizeFromIntent = intent.getLongExtra(
                DownloadService.EXTRA_LONG_PROGRESS_TOTAL_SIZE, 0L);

            final long downloadSize = downloadStatus.getDownloadSize();
            // Make sure we don't override total file size if it's already set in the intent.
            if (totalSizeFromIntent == 0L && downloadSize > 0) {
                intent.putExtra(DownloadService.EXTRA_LONG_PROGRESS_TOTAL_SIZE, downloadSize);
            }
        } else {
            intent = new Intent();
        }
        return intent;
    }

    /**
     * Check whether values are cached for a download status object.
     *
     * @param downloadStatus
     *            the corresponding downloadStatus object
     * @return true if cached
     */
    private boolean intentValuesAreCached(final DownloadStatus downloadStatus) {
        return downloadStatus.getDownloadInfo() != null;
    }

    /**
     * Update the cache with values from the content provider for both extras
     * and categories.
     * 
     * @param downloadId
     *            the id
     * @param downloadStatus
     *            the download status
     */
    /* package */ void cacheIntentValues(final long downloadId, final DownloadStatus downloadStatus) {
        final String id = String.valueOf(downloadId);
        final BasicDownloadInfo basicDownloadInfo = BasicDownloadInfo.getNewDownloadInfo(context, id);
        downloadStatus.setDownloadInfo(basicDownloadInfo);
        if (basicDownloadInfo != null) {
            downloadStatus.setDownloadSize(basicDownloadInfo.getDownloadSize());
            downloadStatus.setDownloadStartTime(System.currentTimeMillis());
        }
    }

    /**
     * Put an extra into the intent if the value isn't null.
     * 
     * @param intent
     *            the intent to populate
     * @param key
     *            the key
     * @param value
     *            the value
     */
    private void putExtra(final Intent intent, final String key,
        final String value) {
        if (null != value) {
            intent.putExtra(key, value);
        }
    }

    /**
     * Remove all cached values to force a re-read from the provider.
     * 
     * @param id
     *            the ID to de-cache
     */
    private void clearCachedValues(final String id) {
        downloadStatuses.remove(id);
    }

    /**
     * Throttle our DB access using elapsed MONOTONIC time.
     * 
     * @return true if we should update, false otherwise
     */
    /* package */ static boolean mayUpdateDatabase() {
        final long gap = System.nanoTime() - lastDatabaseUpdate;
        if (gap < MIN_UPDATE_TIME && (gap > 0) ) {
            return false;
        }

        lastDatabaseUpdate = System.nanoTime();
        return true;
    }

    /**
     * This method helps to synchronize on the download id.
     *
     * @param downloadId 
     *      the downloadId this method will always return the same
     *      mutex for
     * @return mutex for this downloadId, or a new mutex if none exists yet
     */
    private DownloadStatus getDownloadStatus(final String downloadId) {
        this.downloadStatuses.putIfAbsent(downloadId, new DownloadStatus());

        return this.downloadStatuses.get(downloadId);
    }

    /**
     * Class to wrap download status information.
     */
    public static class DownloadStatus {
        /** Download Info from database. */
        private BasicDownloadInfo downloadInfo;

        /** Download start time. */
        private Long downloadStartTime;

        /** Download Size (This value may be updated in the db without updating the downloadInfo).  */
        private long downloadSize;

        /** DownloadSpeedMeasurer. */
        private DownloadSpeedMeasurer progressMeasurer;

        private final Object lock = new Object();

        /**
         * Sets the download info.
         * @param basicDownloadInfo the download info
         */
        public void setDownloadInfo(final BasicDownloadInfo basicDownloadInfo) {
            downloadInfo = basicDownloadInfo;
        }

        /**
         * Get the download info.
         * @return the new download info.
         */
        public BasicDownloadInfo getDownloadInfo() {
            return downloadInfo;
        }

        /**
         * Sets the download size.
         * @param size the new download size.
         */
        public void setDownloadSize(final long size) {
            downloadSize = size;
        }

        /**
         * Gets the download size.
         * @return the download size.
         */
        public long getDownloadSize() {
            return downloadSize;
        }

        /**
         * Gets the download start time.
         * @return the download start time
         */
        public Long getDownloadStartTime() {
            return downloadStartTime;
        }

        /**
         * Sets the download start time.
         * @param downloadStartTime the new download start time
         */
        public void setDownloadStartTime(final Long downloadStartTime) {
            this.downloadStartTime = downloadStartTime;
        }

        /**
         * Gets the progress measurer.
         * @return the progress measurer
         */
        public DownloadSpeedMeasurer getProgressMeasurer() {
            return progressMeasurer;
        }

        /**
         * Sets the progress measurer.
         * @param progressMeasurer the new progress measurer
         */
        public void setProgressMeasurer(final DownloadSpeedMeasurer progressMeasurer) {
            this.progressMeasurer = progressMeasurer;
        }
    }
}
