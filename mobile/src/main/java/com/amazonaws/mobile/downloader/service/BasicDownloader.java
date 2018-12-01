package com.amazonaws.mobile.downloader.service;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import com.amazonaws.mobile.downloader.policy.DownloadPolicyProvider;
import com.amazonaws.mobile.downloader.query.DownloadState;
import com.amazonaws.mobile.downloader.query.DownloadQueueProvider;
import com.amazonaws.mobile.downloader.query.QueryHelper;

/**
 * This classes handles downloading files using DownloadTasks
 */
public class BasicDownloader implements DownloadTask.DownloadListener,
    DownloadTask.NetworkStatusProvider, Downloader {

    /** Logging tag for this class. */
    private static final String LOG_TAG = BasicDownloader.class.getSimpleName();

    /** How many threads in the download pool by default. */
    private static final int MAX_DOWNLOAD_THREADS = 2;

    /** Minimum Android SDK that support WIFI_MODE_FULL_HIGH_PERF. */
    private static final int ANDROID_SDK_VERSION_12 = 12;

    /** Whether we're still reading in the downloads. */
    private boolean initializing = true;

    /** Whether the download queue has begun restoring previously started downloads. */
    private boolean hasStartedRestoringDownloadQueue = false;

    private boolean shuttingDown = false;

    /** The executor which is going to download the files for us. */
    private final ExecutorService downloader;

    /** The context to use. */
    private final Context context;

    /** The DownloadStatusUpdater to use. */
    private final DownloadStatusUpdater statusUpdater;

    /** The download policy which is in force. */
    private final DownloadPolicyProvider policyProvider;

    /** The in-memory list of downloads queued to run. The number of these tasks that are actually running
     * is limited by {@link #MAX_DOWNLOAD_THREADS} . */
    private final LongSparseArray<DLTaskInfo> runningDownloads;

    private final AtomicInteger queuedDownloads = new AtomicInteger(0);

    /** Keeps track of whether downloads are using wifi locks .*/
    private final LongSparseArray<Boolean> wifiLocks;

    /** Wifi Lock to use for downloads that require a wifi lock. */
    private final WifiLock wifiLock;

    private class DLTaskInfo {
        private final DownloadTask downloadTask;
        private final Future<Boolean> runningDownload;
        private DLTaskInfo(final DownloadTask downloadTask, final Future<Boolean> runningDownload) {
            this.downloadTask = downloadTask;
            this.runningDownload = runningDownload;
        }
    }

    /**
     * Create a new instance.
     * 
     * @param aContext
     *            the context to use for accessing the content provider etc
     * @param aPolicyProvider
     *            the download policy provider to use
     * @param aStatusUpdater
     *            the download status updater to use
     */
    public BasicDownloader(final Context aContext,
        final DownloadPolicyProvider aPolicyProvider,
        final DownloadStatusUpdater aStatusUpdater) {
        Log.d(LOG_TAG, "BasicDownloader()");

        downloader = Executors.newFixedThreadPool(MAX_DOWNLOAD_THREADS,
            new MinPriorityThreadFactory(this.getClass()
                .getSimpleName()));

        context = aContext;
        statusUpdater = aStatusUpdater;
        policyProvider = aPolicyProvider;

        runningDownloads = new LongSparseArray<>();
        wifiLocks = new LongSparseArray<>();
        final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int wifiMode = WifiManager.WIFI_MODE_FULL;
        if (android.os.Build.VERSION.SDK_INT >= ANDROID_SDK_VERSION_12) {
            wifiMode = WifiManager.WIFI_MODE_FULL_HIGH_PERF;
        }
        wifiLock = wifiManager.createWifiLock(wifiMode, this.getClass().getSimpleName());
    }

    /**
     * See {@link Downloader#isIdle()}
     */
    @Override
    public boolean isIdle() {
        final boolean result;
        synchronized (runningDownloads) {
            result = !initializing && 0 == runningDownloads.size();
        }
        Log.d(LOG_TAG, "isIdle() returning " + result);
        return result;
    }

    /**
     * See {@link Downloader#addDownloadTask(long)}
     */
    @Override
    public boolean addDownloadTask(final long id) {
        Log.d(LOG_TAG, String.format("Adding download task with id %d", id));
        synchronized (runningDownloads) {
            if (runningDownloads.get(id) != null) {
                // Consider adding the task to fail if already downloading.
                return false;
            }

            // Retrieve the download from the database.
            final DownloadTask downloadTask = createDownloadTask(id);

            // Fail fast if attempting to add a task that doesn't exist.
            if (downloadTask == null) {
                // Can't find the download task in the database.
                return false;
            }

            if (queuedDownloads.getAndIncrement() == 0) {
                // Keep track of the network state while there are downloads queued.
                NetworkStateListener.enable(context);
            }

            // If we are shutting down, don't attempt to create or add the task.
            if (shuttingDown) {
                return true;
            }

            try {
                final Future<Boolean> receipt = downloader.submit(downloadTask);
                runningDownloads.put(id, new DLTaskInfo(downloadTask, receipt));
            } catch (final RejectedExecutionException ex) {
                // Just log it, we won't lose the download task as it's still in the queue.
                Log.w(LOG_TAG, "Dropping the task since the downloader is shutting down.", ex);
            }
        }
        return true;
    }

    private void decrementQueuedDownloads() {
        if (queuedDownloads.decrementAndGet() == 0) {
            // Shut our network state listener off while not downloading and nothing is in the queue.
            NetworkStateListener.disable(context);
        }
    }
    private boolean stopDownloadTask(final long downloadId,
                                     final DownloadTask.TaskCancelReason cancelReason) {
        final DLTaskInfo dlTaskInfo;
        final Future<Boolean> receipt;
        synchronized (runningDownloads) {
            dlTaskInfo = runningDownloads.get(downloadId);
            if (dlTaskInfo == null) {
                // No download task was running for the specified id.
                return false;
            }
            receipt = dlTaskInfo.runningDownload;
            runningDownloads.delete(downloadId);
            decrementQueuedDownloads();
        }

        if (!receipt.isDone()) {
            dlTaskInfo.downloadTask.setCancelReason(cancelReason);
            final boolean result = receipt.cancel(true);
            Log.d(LOG_TAG, String.format("Cancelled task by pausing for id (%d) result = %s",
                downloadId, Boolean.toString(result)));
            return true;
        }

        // The download task already completed.
        return false;
    }

    /**
     * See {@link Downloader#pauseDownloadTask(long)}
     */
    @Override
    public boolean pauseDownloadTask(final long downloadId) {
        // If we're pausing the execution of a download, its task is no longer
        // relevant and can be canceled since a new task will be created when
        // the download is resumed.
        return stopDownloadTask(downloadId, DownloadTask.TaskCancelReason.PAUSED_BY_USER);
    }

    /**
     * See {@link Downloader#resumeDownloadTask(long)}
     */
    @Override
    public boolean resumeDownloadTask(final long downloadId) {
        return addDownloadTask(downloadId);
    }

    /**
     * See {@link Downloader#shutdownNow()}
     */
    @Override
    public void shutdownNow() {
        shuttingDown = true;
        // Shut the executor service down
        downloader.shutdownNow();
        synchronized (runningDownloads) {
            // Clear the runningDownloads map.
            runningDownloads.clear();

            // Set initializing to be false so that isIdle returns true
            initializing = false;
        }
    }

    /**
     * Set initializing to false.
     */
    public void doneInitializing() {
        synchronized (runningDownloads) {
            initializing = false;
        }
        notifyDownloadServiceInitializationComplete();
    }

    /**
     * Trigger DownloadService to check Downloader.isIdle() 
     * so that it may shutdown after initializing.
     */
    private void notifyDownloadServiceInitializationComplete() {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(DownloadService.ACTION_NO_OPERATION);
        context.startService(intent);
    }

    @Override
    public boolean cancelDownloadTask(final long downloadId) {
        return stopDownloadTask(downloadId,
            DownloadTask.TaskCancelReason.CANCELED_BY_USER);
    }

    /** Download Task column types for DB query. */
    private enum DownloadTaskColumns {
        /** url. */
        COL_URL(DownloadQueueProvider.COLUMN_DOWNLOAD_URL),

        /** file location. */
        COL_FILE_LOCATION(DownloadQueueProvider.COLUMN_DOWNLOAD_FILE_LOCATION),

        /** the download ETAG. */
        COL_TAG(DownloadQueueProvider.COLUMN_DOWNLOAD_ETAG),

        /** Current size.  */
        COL_CURRENT_SIZE(DownloadQueueProvider.COLUMN_DOWNLOAD_CURRENT_SIZE),

        /** user flags. */
        COL_USER_FLAGS(DownloadQueueProvider.COLUMN_DOWNLOAD_USER_FLAGS),

        /** download status. */
        COL_DOWNLOAD_STATUS(DownloadQueueProvider.COLUMN_DOWNLOAD_STATUS),

        /** Total size of download. */
        COL_TOTAL_SIZE(DownloadQueueProvider.COLUMN_DOWNLOAD_TOTAL_SIZE);

        /** Name of the column. */
        String columnName;

        /**
         * Set column name.
         * @param s the column name.
         */
        DownloadTaskColumns(final String s) {
            columnName = s;
        }
        /** Array of column names for db query. */
        private static final String[] COLUMNS;
        static {
            COLUMNS = new String[DownloadTaskColumns.values().length];
            for (DownloadTaskColumns colType : DownloadTaskColumns.values()) {
                COLUMNS[colType.ordinal()] = colType.columnName;
            }
        }

        /**
         * @return column names.
         */
        public static String[] getColumnNames() {
            return COLUMNS;
        }
    }
    /**
     * Get a callable to download a given ID.
     * 
     * @param id
     *            the ID to look up and create the runnable for
     * @return the callable.
     */
    public DownloadTask createDownloadTask(final long id) {
        Log.d(LOG_TAG, "createDownloadTask()");
            final String query = DownloadQueueProvider.COLUMN_DOWNLOAD_ID + " = ?";
        final String[] args = new String[] {String.valueOf(id)};

        final String[] cols = QueryHelper.runDownloadQueryForRow(context,
            DownloadTaskColumns.getColumnNames(), query, args);

        if (null != cols) {
            final int userFlags = DownloadFlags.parseUserFlags(
                cols[DownloadTaskColumns.COL_USER_FLAGS.ordinal()]);

            final boolean ars = DownloadState.PAUSED.toString()
                .equals(cols[DownloadTaskColumns.COL_DOWNLOAD_STATUS.ordinal()]);

            final DownloadTask.Builder builder = new DownloadTask.Builder(id)
                .withListener(this)
                .withNetworkStatusProvider(this)
                .withProvider(policyProvider)
                .withUri(cols[DownloadTaskColumns.COL_URL.ordinal()])
                .withDestination(cols[DownloadTaskColumns.COL_FILE_LOCATION.ordinal()])
                .withTag(cols[DownloadTaskColumns.COL_TAG.ordinal()])
                .withOffset(cols[DownloadTaskColumns.COL_CURRENT_SIZE.ordinal()])
                .withDownloadFlags(userFlags)
                .withAutoRestart(ars)
                .withTotalBytes(cols[DownloadTaskColumns.COL_TOTAL_SIZE.ordinal()]);

            return builder.build();
        }

        return null;
    }

    private int getFlagsForDownloadById(final long id) {
        final String[] projection = new String[] {
            DownloadQueueProvider.COLUMN_DOWNLOAD_USER_FLAGS
        };

        final String query = DownloadQueueProvider.COLUMN_DOWNLOAD_ID + " = ?";
        final String[] args = new String[] {
            Long.toString(id)
        };

        final String[] cols = QueryHelper.runDownloadQueryForRow(context, projection, query, args);
        // This query is expected to always succeed in finding a row.
        if (cols == null) {
            Log.e(LOG_TAG, String.format(
                "Couldn't find download id (%d) in the queue to check download flags.", id));
            return 0;
        }
        return DownloadFlags.parseUserFlags(cols[0]);
    }
    /**
     * Indicates whether a Wifi lock is required or not for a specific download request.
     * 
     * @param id the download request id.
     * @return true if wifi lock is required. False otherwise.
     */
    /* package */ boolean wifiLockRequired(final long id) {
        return DownloadFlags.isWifiLockFlagSet(getFlagsForDownloadById(id));
    }

    /* begin DownloadTask.listener implementation */
    /**
     * Receive notification that a download started.
     * 
     * @param longDownloadId
     *            the ID
     */
    @Override
    public void start(final long longDownloadId) {
        statusUpdater.start(longDownloadId);
    }

    /**
     * Receive notification that we got headers etc.
     * example headers: [
     * Date: Thu, 01 Nov 2012 20:21:54 GMT,
     * Server: Apache/2.2.22 (Fedora),
     * Last-Modified: Sun, 28 Oct 2012 19:38:20 GMT,
     * ETag: "75c317b-4fb985a-4cd23b1f4c300",
     * Accept-Ranges: bytes,
     * Content-Length: 83597402,
     * Keep-Alive: timeout=5, max=1000,
     * Connection: Keep-Alive,
     * Content-Type: application/x-bzip2
     * ]
     * 
     * @param longDownloadId
     *            the ID
     * @param connection
     *            the HttpUrlConnection containing response headers to examine.
     */
    @Override
    public void headersReceived(final long longDownloadId,
        final HttpURLConnection connection) {
        statusUpdater.headersReceived(longDownloadId, connection);
    }

    /**
     * Receive notification that progress happened.
     * We send a progress broadcast, and optionally update the content provider.
     * 
     * @param longDownloadId
     *            the task of which to update progress
     * @param bytesRead
     *            number of bytes transferred
     * @param totalBytes
     *            total number of bytes if known, or -1 if not known
     */
    @Override
    public void sendProgress(final long longDownloadId, final long bytesRead,
        final long totalBytes) {
        statusUpdater.sendProgress(longDownloadId, bytesRead, totalBytes);
    }

    /**
     * Receive notification that the download terminated, whether sucessfully or not.
     * 
     * @param downloadId
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
    @Override
    public void finish(final long downloadId, final CompletionStatus withStatus,
                       final String completionMessage, final long bytesRead, final long totalBytes,
                       final boolean autoRestart, final String downloadError) {

        Log.i(LOG_TAG, String.format("downloadTaskComplete, id = %d  withStatus = %s",
            downloadId, withStatus));

        // Remove task from our list.
        synchronized (runningDownloads) {
            runningDownloads.remove(downloadId);
        }
        if (withStatus != CompletionStatus.PAUSED) {
            // if the download was paused (not explicitly by the user),
            // it was probably due to network lost. Don't decrement the queued downloads.
            // Downloads paused by the user are not considered queued.
            decrementQueuedDownloads();
        }

        if (withStatus == CompletionStatus.FAILED) {
            Log.d(LOG_TAG, "cleanUpPartialFile for Failed Downloads");
            cleanUpPartialFile(downloadId);
        }

        final int downloadFlags = getFlagsForDownloadById(downloadId);
        // Check whether we still have network.
        if (!isNetworkAvailable(DownloadFlags.isCellNetworkProhibited(downloadFlags))) {
            shutdownNow();
        }

        statusUpdater.finish(downloadId, withStatus, completionMessage,
            bytesRead, totalBytes, autoRestart, downloadError);

        Log.d(LOG_TAG, "done with downloadTaskComplete, id = " + downloadId);
    }

    /**
     * If a download fails, cleanup the partial file to regain the space.
     * If there was any chance of success, then the download should have paused instead of failing.
     * 
     * @param downloadId long DownloadId.
     */
    private void cleanUpPartialFile(final long downloadId) {
        final Uri downloadQueueContentUri = DownloadQueueProvider.getDownloadContentUri(context);
        final Cursor downloadCursor = context.getContentResolver().query(downloadQueueContentUri,
            new String[]{
                DownloadQueueProvider.COLUMN_DOWNLOAD_FILE_LOCATION
            },
            DownloadQueueProvider.COLUMN_DOWNLOAD_ID
                + " = " + downloadId,
            null,
            null);
        if (downloadCursor != null) {
            downloadCursor.moveToFirst();
            if (!downloadCursor.isAfterLast()) {
                final String filePath = downloadCursor.getString(
                    downloadCursor.getColumnIndex(
                        DownloadQueueProvider.COLUMN_DOWNLOAD_FILE_LOCATION));
                File downloadedFile = new File(filePath);
                if (downloadedFile.exists()) {
                    Log.d(LOG_TAG, String.format("Cleaning up partial failed download: %s",
                        filePath));
                    if (!downloadedFile.delete()) {
                        Log.e(LOG_TAG, String.format(
                            "Unable to delete failed partially downloaded file: %s", filePath));
                    }
                }
            }
            downloadCursor.close();
        }
    }

    /* end DownloadTask.listener implementation */

    /* begin DownloadTask.NetworkStatusProvider implementation */

    /**
     * Report whether the network is available.
     * 
     * @return true if it is
     */
    @Override
    public boolean isNetworkAvailable(boolean isMobileNetworkProhibited) {
        final ConnectivityManager mgr =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == mgr) {
            return false;
        }

        final NetworkInfo networkInfo = mgr.getActiveNetworkInfo();

        return null != networkInfo && networkInfo.isConnected() && (!isMobileNetworkProhibited
            || networkInfo.getType() != ConnectivityManager.TYPE_MOBILE);
    }

    /**
     * Acquire a wifi lock and associate it with a download Id.
     * The wifi lock will be acquired only if it was specified as part of the request intent for that download Id.
     * 
     * @param downloadId the download id.
     */
    @Override
    public void acquireWifiLock(final long downloadId) {
        // Acquire a wifi lock if required
        if (wifiLockRequired(downloadId)) {
            synchronized (wifiLocks) {
                if (!wifiLock.isHeld()) {
                    if (wifiLocks.size() != 0) {
                        Log.e(LOG_TAG, String.format("Wifi lock not held, while %d locks exist in the lock list.",
                            wifiLocks.size()));
                    }
                    wifiLock.acquire();
                    Log.d(LOG_TAG, "Acquired wifi lock.");
                }
                // Keep track that a wifi lock was required for this download.
                wifiLocks.append(downloadId, true);
            }
            Log.d(LOG_TAG, String.format("Added download with id(%d) to wifi lock list.", downloadId));
        }
    }

    /**
     * This releases the wifi lock (if any) associated with a particular download ID.
     * 
     * @param downloadId the download id.
     */
    @Override
    public void releaseWifiLock(final long downloadId) {
        synchronized (wifiLocks) {
            final Boolean wifiLockRequired = wifiLocks.get(downloadId);
            if (wifiLockRequired != null) {
                wifiLocks.delete(downloadId);
                Log.d(LOG_TAG, String.format("Removed download id(%d) from the wifi lock list.", downloadId));
                if (!wifiLockRequired || wifiLocks.size() > 0) {
                    // No wifi lock was required or there are other wifi locks in the list, so return early.
                    return;
                }
                if (wifiLock.isHeld()) {
                    Log.d(LOG_TAG, "Released Wifi Lock.");
                    wifiLock.release();
                } else {
                    Log.e(LOG_TAG, String.format(
                        "Download with id(%d) expected the wifi lock to be held, but it wasn't.", downloadId));
                }
            }
        }
    }

    /* end DownloadTask.NetworkStatusProvider implementation */

    /**
     * Restart queued downloads on a separate thread.
     */
    private void startReadingQueueFromContentProvider() {
        Log.i(LOG_TAG, "Start reading queue");
        downloader.submit(new QueueReaderTask(this, this.context));
        Log.i(LOG_TAG, "Queue read job submitted");
    }

    /**
     * This should be called only from the download service when it first starts in order to restore
     * the downloads from the Queue.
     */
    public synchronized void restartQueuedDownloads() {
        if (!hasStartedRestoringDownloadQueue) {
            hasStartedRestoringDownloadQueue = true;
            startReadingQueueFromContentProvider();
        }
    }

    /**
     * @see Downloader#onCleanupAction()
     */
    @Override
    public void onCleanupAction() {
    }
}
