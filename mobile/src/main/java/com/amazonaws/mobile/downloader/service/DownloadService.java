package com.amazonaws.mobile.downloader.service;

import java.util.ArrayList;
import java.util.Collection;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseBooleanArray;

import com.amazonaws.mobile.downloader.query.DownloadState;
import com.amazonaws.mobile.downloader.query.DownloadQueueProvider;
import com.amazonaws.mobile.downloader.query.QueryHelper;
import com.amazonaws.mobile.downloader.request.DownloadAddRequest;
import com.amazonaws.mobile.downloader.request.DownloadRemoveRequest;

/**
 * Service to manage downloads.
 */
public class DownloadService extends Service {

    /** Our logger, for informational and error messages. */
    private static final String LOG_TAG = DownloadService.class.getSimpleName();

    /** The prefix of all extra keys. */
    public static final String ACTION_PREFIX = "amazonaws.mobile.downloadservice.";

    /** The intent to start the service for no operation. */
    public static final String ACTION_NO_OPERATION = ACTION_PREFIX + "NO_OPERATION";

    /** The intent to start the service. */
    public static final String ACTION_START_UP = ACTION_PREFIX + "START_UP";

    /** The intent action sent to the service when network is lost. */
    public static final String ACTION_NETWORK_LOST = ACTION_PREFIX + "NETWORK_LOST";

    /**
     * The intent action to remove a failed or completed download from the queue.
     * To cancel a running download, see {@link #ACTION_CANCEL_DOWNLOAD}.
     */
    public static final String ACTION_REMOVE_DOWNLOAD = ACTION_PREFIX + "REMOVE_DOWNLOAD";

    /** The intent action to request a download. */
    public static final String ACTION_REQUEST_DOWNLOAD = ACTION_PREFIX + "REQUEST_DOWNLOAD";

    /** The intent action to request a download be paused. */
    public static final String ACTION_REQUEST_PAUSE = ACTION_PREFIX + "REQUEST_PAUSE";

    /** The intent action to request a download be resumed. */
    public static final String ACTION_RESUME_DOWNLOAD = ACTION_PREFIX + "RESUME_DOWNLOAD";

    /** The intent action to request a download be canceled. */
    public static final String ACTION_CANCEL_DOWNLOAD = ACTION_PREFIX + "CANCEL_DOWNLOAD";

    /** The intent action to request clean up of the download queue. */
    public static final String ACTION_CLEANUP_DOWNLOAD_QUEUE = ACTION_PREFIX + "ACTION_CLEANUP_DOWNLOAD_QUEUE";

    /** The intent action that download is enqueued. */
    public static final String ACTION_DOWNLOAD_ENQUEUED = ACTION_PREFIX + "DOWNLOAD_ENQUEUED";

    /** The intent action that download is started. */
    public static final String ACTION_DOWNLOAD_STARTED = ACTION_PREFIX + "DOWNLOAD_STARTED";

    /** The intent action that download has progressed. */
    public static final String ACTION_DOWNLOAD_PROGRESS = ACTION_PREFIX + "DOWNLOAD_PROGRESS";

    /** The intent action that download failed to be enqueued. */
    public static final String ACTION_DOWNLOAD_ENQUEUE_FAILED = ACTION_PREFIX + "DOWNLOAD_ENQUEUE_FAILED";

    /** The intent action that a download is removed successfully. */
    public static final String ACTION_DOWNLOAD_REMOVED = ACTION_PREFIX + "DOWNLOAD_REMOVED";

    /** The intent action that a download failed to be removed. */
    public static final String ACTION_DOWNLOAD_REMOVE_FAILED = ACTION_PREFIX + "DOWNLOAD_REMOVE_FAILED";

    /** The intent action that a download was canceled. */
    public static final String ACTION_DOWNLOAD_CANCELED = ACTION_PREFIX + "DOWNLOAD_CANCELED";

    /** The intent action that a download was successfully paused. */
    public static final String ACTION_REQUEST_PAUSE_SUCCEEDED = ACTION_PREFIX + "REQUEST_PAUSE_SUCCEEDED";

    /** The intent action that a download was not successfully paused. */
    public static final String ACTION_REQUEST_PAUSE_FAILED = ACTION_PREFIX + "REQUEST_PAUSE_FAILED";

    /** The intent action that a download failed to be canceled. */
    public static final String ACTION_DOWNLOAD_CANCEL_FAILED = ACTION_PREFIX + "DOWNLOAD_CANCEL_FAILED";

    /** The intent action that a download was paused. */
    public static final String ACTION_DOWNLOAD_PAUSED = ACTION_PREFIX + "DOWNLOAD_PAUSED";

    /** The intent action that a download completed successfully. */
    public static final String ACTION_DOWNLOAD_COMPLETE = ACTION_PREFIX + "DOWNLOAD_COMPLETE";

    /** The intent action that a download completed successfully. */
    public static final String ACTION_DOWNLOAD_FAILED = ACTION_PREFIX + "DOWNLOAD_FAILED";

    /** The intent action that a download was successfully resumed. */
    public static final String ACTION_DOWNLOAD_RESUMED = ACTION_PREFIX + "DOWNLOAD_RESUMED";

    /** The intent action that a download was not successfully resumed. */
    public static final String ACTION_DOWNLOAD_RESUME_FAILED = ACTION_PREFIX + "DOWNLOAD_RESUME_FAILED";

    /** The prefix of all extra keys. */
    public static final String EXTRA_PREFIX = "amazonaws.mobile.downloadservice.";

    /** The intent extra key for download id. */
    public static final String EXTRA_LONG_ID = EXTRA_PREFIX + "downloadId";

    /** The intent extra key for download description. */
    public static final String EXTRA_DESCRIPTION = EXTRA_PREFIX + "description";

    /** The intent extra key for download uri. */
    public static final String EXTRA_URL = EXTRA_PREFIX + "downloadUrl";

    /** The intent extra key for the download location. */
    public static final String EXTRA_LOCATION = EXTRA_PREFIX + "location";

    /** The intent extra key for the download progress so far. */
    public static final String EXTRA_LONG_PROGRESS_CUMULATIVE = EXTRA_PREFIX + "cumulativeBytes";

    /** The intent extra key for the download start time. */
    public static final String EXTRA_LONG_START_TIME = EXTRA_PREFIX + "startTime";

    /** The intent extra key for the download end time. */
    public static final String EXTRA_LONG_END_TIME = EXTRA_PREFIX + "endTime";

    /** The intent extra key for the download duration. */
    public static final String EXTRA_LONG_DURATION = EXTRA_PREFIX + "duration";

    /** The intent extra key for the download total size. */
    public static final String EXTRA_LONG_PROGRESS_TOTAL_SIZE = EXTRA_PREFIX + "totalBytes";

    /** The intent extra key for a pause/resume that is by direct user request. */
    public static final String EXTRA_BOOL_BY_USER_REQUEST = EXTRA_PREFIX + "byUserRequest";

    /** The intent extra key for the mime type. */
    public static final String EXTRA_MIME_TYPE = EXTRA_PREFIX + "mimeType";

    /**
     * The intent extra key for the completion message. This may optionally be set to
     * describe an error that occurred, etc.
     */
    public static final String EXTRA_COMPLETION_MESSAGE = EXTRA_PREFIX + "completionMessage";

    public static final String EXTRA_DOWNLOAD_FLAGS = EXTRA_PREFIX + "downloadFlags";

    /** The intent extra key for whether this was an automatic restart of a download. */
    public static final String EXTRA_BOOL_AUTO_RESTART = EXTRA_PREFIX + "autoRestart";

    /** The intent extra key for download errors. */
    public static final String EXTRA_DOWNLOAD_ERROR = EXTRA_PREFIX + "downloadError";

    /** The intent extra key for the download title. */
    public static final String EXTRA_DOWNLOAD_TITLE = "title";

    /** A download ID that will never be valid. */
    public static final long INVALID_ID = -1;

    /** The amount of time after which a COMPLETED download can be cleanuped up from the queue. */
    public static final long COMPLETED_DOWNLOAD_CLEANUP_DELAY = 1000L * 60 * 60 * 24 * 7;

    /** The amount of time after which a FAILED download can be cleanuped up from the queue. */
    public static final long FAILED_DOWNLOAD_CLEANUP_DELAY = 1000L * 60 * 60 * 24 * 28;

    /** A set of known startIds. */
    private final SparseBooleanArray activeIds = new SparseBooleanArray();

    /** the largest seen startId. */
    private volatile int maxId;

    /** The Handler we'll use to handle incoming intents. */
    private volatile ServiceHandler intentHandler;

    /** Our downloader object (Store statically to avoid recreating each time the service starts). */
    private volatile static Downloader downloader;

    /** Secure broadcast manager. */
    protected LocalBroadcastManager localBroadcastManager;

    /** The content URI for the download queue. */
    private Uri downloadQueueContentUri;


    /** {@inheritDoc} */
    @Override
    public void onCreate() {
        downloadQueueContentUri = DownloadQueueProvider.getDownloadContentUri(this);
        // Create a local broadcast manager to send status updates.
        localBroadcastManager = LocalBroadcastManager.getInstance(this.getApplicationContext());
        final HandlerThread thread = new HandlerThread("DownloadService HandlerThread.");
        thread.start();
        final Looper intentLooper = thread.getLooper();
        intentHandler = new ServiceHandler(intentLooper);
    }

    /** {@inheritDoc} */
    /*
     * We handling start IDs ourselves instead of calling stopSelf(id). We'll only stop if we aren't downloading
     * AND have no pending unhandled intents.
     */
    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.d(LOG_TAG, "onStartCommand(" + startId + ") called!");

        if (downloader == null) {
            final String action = intent.getAction();
            // These actions should not start the service if it is not running.
            if (ACTION_NETWORK_LOST.equals(action)|| ACTION_DOWNLOAD_COMPLETE.equals(action)
                || ACTION_DOWNLOAD_PAUSED.equals(action)
                || ACTION_DOWNLOAD_FAILED.equals(action)) {
                stopSelfIfNoThreads();
                return START_NOT_STICKY;
            }
            // No download policy provider by default (passing null for the policy provider).
            downloader = new BasicDownloader(this.getApplicationContext(), null,
                new DownloadStatusUpdater(this, localBroadcastManager));
        }

        synchronized (activeIds) {
            activeIds.append(startId, true);
        }
        if (startId > maxId) {
            maxId = startId;
        }

        final Message message = intentHandler.obtainMessage();
        message.arg1 = startId;
        message.obj = intent;
        intentHandler.sendMessage(message);

        return START_NOT_STICKY;
    }

    /** {@inheritDoc} */
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void onDestroy() {
        try {
            if (null != downloader) {
                downloader.shutdownNow();
                downloader = null;
                maxId = -1;
            }
        } finally {
            intentHandler = null;
        }
    }

    /**
     * Record completion of a given intent, and stop the service if we're all done.
     * 
     * @param startId
     *            the id of the request that's done.
     */
    protected void stopSelfIfNoThreads(final int startId) {
        Log.d(LOG_TAG, "stopSelfIfNoThreads( " + startId + ") called");
        synchronized (activeIds) {
            activeIds.delete(startId);
        }
        stopSelfIfNoThreads();
    }

    /**
     * Stop the service if we're all done.
     */
    private void stopSelfIfNoThreads() {
        /** Holds a copy of maxId so that we don't end up stopping with wrong maxId due to a race condition. */
        final int maxIdCopy = maxId;
        final boolean activeIdsEmpty;

        synchronized (activeIds) {
            activeIdsEmpty = activeIds.size() == 0;
        }

        if (activeIdsEmpty && (downloader == null || downloader.isIdle())) {
            Log.d(LOG_TAG, String.format("No running threads, stopping self, max download id (%d).", maxIdCopy));
            // pass the maximum seen start ID, that way if there's another request on the looper but not
            // seen yet, we'll keep running.
            stopSelf(maxIdCopy);
        }
    }

    /**
     * Handle an intent.
     * 
     * @param intent the intent to handle
     */
    protected void onHandleIntent(final Intent intent) {
        Log.d(LOG_TAG, "onHandleIntent, action = " + intent.getAction());

        final String action = intent.getAction();
        if (action == null || action.equals("")) {
            throw new IllegalArgumentException("null or empty action");
        } else if (action.equals(ACTION_NETWORK_LOST)) {
            downloader.shutdownNow();
        } else if (action.equals(DownloadService.ACTION_REQUEST_DOWNLOAD)) {
            enqueueDownload(intent);
        } else if (action.equals(DownloadService.ACTION_REQUEST_PAUSE)) {
            pauseDownload(intent);
        } else if (action.equals(DownloadService.ACTION_RESUME_DOWNLOAD)) {
            resumeDownload(intent);
        } else if (action.equals(DownloadService.ACTION_CLEANUP_DOWNLOAD_QUEUE)) {
            cleanupDownloadQueue();
        } else if (action.equals(DownloadService.ACTION_DOWNLOAD_COMPLETE)
            || action.equals(DownloadService.ACTION_DOWNLOAD_PAUSED)
            || action.equals(DownloadService.ACTION_DOWNLOAD_FAILED)) {
            stopSelfIfNoThreads();
        } else if (action.equals(DownloadService.ACTION_REMOVE_DOWNLOAD)) {
            removeDownload(intent);
        } else if (action.equals(DownloadService.ACTION_CANCEL_DOWNLOAD)) {
            cancelDownload(intent);
        } else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)
            || Intent.ACTION_MEDIA_BAD_REMOVAL.equals(action)) {
            processMediaUnmountedRemoved(intent);
        } else if (action.equals(DownloadService.ACTION_START_UP)) {
            downloader.restartQueuedDownloads();
        } else if (action.equals(DownloadService.ACTION_NO_OPERATION)) {
            return;
        } else {
            throw new IllegalArgumentException("unknown action");
        }
    }

    /**
     * Requests a download be canceled.
     *
     * @param intent the intent representing the request.
     */
    private void cancelDownload(final Intent intent) {
        long downloadId = intent.getLongExtra(DownloadService.EXTRA_LONG_ID, DownloadService.INVALID_ID);

        if (downloadId == DownloadService.INVALID_ID) {
            Log.e(LOG_TAG, "Download ID must be supplied; ignoring the cancel request.");
            notifyDownloadCancelFailed(intent);
            return;
        }

        if (!downloader.cancelDownloadTask(downloadId)) {
            notifyDownloadCancelFailed(intent);
            return;
        }
        notifyDownloadCancelSucceeded(intent);
    }

    /**
     * Remove a download from the queue. If it is not complete, it will be cancelled.
     * 
     * @param intent The intent to remove a download.
     */
    private void removeDownload(final Intent intent) {
        final DownloadRemoveRequest request;
        try {
            request = DownloadRemoveRequest.fromIntent(intent);

            final long downloadId = request.getDownloadId();

            Log.d(LOG_TAG, String.format("Remove download for id (%d).", downloadId));

            final int rowDeleted = getContentResolver().delete(downloadQueueContentUri,
                DownloadQueueProvider.COLUMN_DOWNLOAD_ID + " = ? AND "
                    + DownloadQueueProvider.COLUMN_DOWNLOAD_STATUS + " in ('"
                    + DownloadState.FAILED.toString() + "', '"
                    + DownloadState.COMPLETE.toString() + "')",
                new String[]{
                    String.valueOf(downloadId)
                });
            if (rowDeleted > 0) {
                Log.i(LOG_TAG, String.format("Removed download with id (%d).",  downloadId));
                notifyDownloadRemoved(intent);
            } else {
                Log.i(LOG_TAG, String.format(
                    "Download %d doesn't exist or is in progress/paused/queued.", downloadId));
                notifyDownloadRemoveFailed(intent);
            }
        } catch (final IllegalArgumentException ex) {
            Log.e(LOG_TAG, "Invalid intent received", ex);
            notifyDownloadRemoveFailed(intent);
        }
    }

    private boolean isDuplicateDownloadLocation(final String downloadLocation) {
        final String[] projection = {
            DownloadQueueProvider.COLUMN_DOWNLOAD_ID
        };
        final String query =
            DownloadQueueProvider.COLUMN_DOWNLOAD_FILE_LOCATION + " = ?  AND " +
            DownloadQueueProvider.COLUMN_DOWNLOAD_STATUS + " not in (?, ?)";
        final String[] queryArgs = {
            downloadLocation,
            DownloadState.COMPLETE
                .toString(),
            DownloadState.FAILED
                .toString()
        };
        String[] results = QueryHelper.runDownloadQueryForRow(this, projection, query, queryArgs);
        if (results != null) {
            Log.e(LOG_TAG, "Duplicate download location detected with download id = " + results[0]);
            return true;
        }

        return false;
    }

    /**
     * Enqueue a download.
     * 
     * @param intent
     *            the intent to request a download.
     */
    private void enqueueDownload(final Intent intent) {
        final DownloadAddRequest request;
        try {
            request = DownloadAddRequest.fromIntent(intent);

            // Check that this file location is not already being downloaded.
            if (isDuplicateDownloadLocation(request.getFileLocation())) {
                notifyDownloadEnqueueFailed(intent);
                Log.e(LOG_TAG, "A download already exists with file location: " + request.getFileLocation());
                return;
            }

            final ContentValues values = request.toContentValues();
            values.put(DownloadQueueProvider.COLUMN_DOWNLOAD_STATUS,
                DownloadState.NOT_STARTED.toString());
            values.put(DownloadQueueProvider.COLUMN_DOWNLOAD_CREATE_TIMESTAMP,
                System.currentTimeMillis());

            final Uri uri = getContentResolver().insert(downloadQueueContentUri, values);
            if (null == uri) {
                notifyDownloadEnqueueFailed(intent);
                Log.e(LOG_TAG, "contentResolver.insert() returned null.");
                return;
            }

            final long downloadId = Long.parseLong(uri.getLastPathSegment());
            Log.i(LOG_TAG, "Enqueued download of package with downloadId " + downloadId);
            
            if (downloader.addDownloadTask(downloadId)) {
                notifyDownloadEnqueued(intent, downloadId);
            } else {
                // This should never happen for the BasicDownloader unless something is very badly
                // wrong with the database.
                Log.w(LOG_TAG, "Couldn't add a download task for a new download.");
                notifyDownloadEnqueueFailed(intent);
            }
        } catch (final IllegalArgumentException ex) {
            notifyDownloadEnqueueFailed(intent);
            Log.e(LOG_TAG, "Invalid intent received", ex);
        }
    }

    /**
     * Pause a download. This means if it is in progress, we will attempt
     * to interrupt the thread running it, and if it isn't up yet, it will be
     * removed from the queue. It will be returned to the queue at the next
     * time the service restarts, or if explicitly resumed.
     * 
     * @param intent
     *            the intent representing the request.
     */
    private void pauseDownload(final Intent intent) {
        final long id = intent.getLongExtra(EXTRA_LONG_ID, INVALID_ID);
        final boolean byUserRequest = intent.getBooleanExtra(EXTRA_BOOL_BY_USER_REQUEST, false);

        if (INVALID_ID == id) {
            Log.d(LOG_TAG, "Attempt to pause a download with an invlaid id.");
            notifyDownloadPauseFailed(intent);
            return;
        }

        final String strId = String.valueOf(id);
        final String[] cols = getStateAndFlags(strId);
        if (null == cols) {
            notifyDownloadPauseFailed(intent);
            return;
        }

        final String downloadState = cols[0];
        // If the state is failed or complete, we must fail the pause request.
        if (DownloadState.FAILED.toString().equals(downloadState) ||
            DownloadState.COMPLETE.toString().equals(downloadState)) {
            notifyDownloadPauseFailed(intent);
        }

        Log.d(LOG_TAG, "Pausing download with id = " + strId);
        final int currentFlags = DownloadFlags.parseUserFlags(cols[1]);
        final int newFlags;
        if (byUserRequest) {
            newFlags = currentFlags | DownloadFlags.FLAG_BY_USER_REQUEST;
        } else {
            newFlags = currentFlags & ~DownloadFlags.FLAG_BY_USER_REQUEST;
        }

        // Attempt to tell the downloader to pause the download task.
        final boolean couldPauseDownloadTask = downloader.pauseDownloadTask(id);
        // if the download task couldn't be paused, and the state was previously set to paused
        // by user request, but is now being requested to pause not by user request.
        if (!couldPauseDownloadTask && DownloadState.PAUSED.toString().equals(downloadState)
            && (DownloadFlags.isUserRequestFlagSet(currentFlags) && !byUserRequest)) {
            Log.w(LOG_TAG, "Ignoring attempt to downgrade a paused download task from being due to user request.");
        } else {
            // Set the user-request flag and update the state to paused.
            updateStateAndFlags(strId, DownloadState.PAUSED.toString(), newFlags);
        }

        if (!couldPauseDownloadTask && !DownloadState.PAUSED.toString().equals(downloadState)) {
            Log.w(LOG_TAG, "Fixed download state to Paused. No task was running, but one was expected.");
        }

        notifyDownloadPauseSucceeded(intent);
    }

    /**
     * Resume a download. If a download was paused "by user request" this
     * will only resume it if the resume is "by user request". Resuming
     * a download in any state other than PAUSED is a no-op.
     * 
     * @param intent
     *            the intent representing the request.
     */
    private void resumeDownload(final Intent intent) {
        final long id = intent.getLongExtra(EXTRA_LONG_ID, INVALID_ID);
        final boolean byUserRequest = intent.getBooleanExtra(EXTRA_BOOL_BY_USER_REQUEST, false);
        if (INVALID_ID == id) {
            Log.d(LOG_TAG, "Attempt to resume a download with an invlaid id.");
            notifyDownloadResumeFailed(intent);
            return;
        }

        final ContentResolver resolver = getContentResolver();

        // Ensure the database is locked to prevent the possibility of a download that completes while this
        // is being called from having its state erroneously changed back to in progress.
        resolver.query(DownloadQueueProvider.getDownloadOpenTransactionUri(this), null, null, null, null);
        try {
            final String strId = String.valueOf(id);
            final String[] cols = getStateAndFlags(strId);
            if (null == cols) {
                Log.d(LOG_TAG, String.format("Can't find download id (%d) in database.", id));
                notifyDownloadResumeFailed(intent);
                return;
            }

            final String downloadState = cols[0];
            // If the state is failed or complete, we must fail the resume request.
            if (DownloadState.FAILED.toString().equals(downloadState) ||
                DownloadState.COMPLETE.toString().equals(downloadState)) {
                notifyDownloadResumeFailed(intent);
                return;
            }

            final int currentFlags = DownloadFlags.parseUserFlags(cols[1]);
            if (DownloadFlags.isUserRequestFlagSet(currentFlags) && !byUserRequest) {
                // user-paused can only be resumed by user request
                notifyDownloadResumeFailed(intent);
                return;
            }

            // Clear the user-request flag.
            final int newFlags = currentFlags & ~DownloadFlags.FLAG_BY_USER_REQUEST;

            updateStateAndFlags(strId, DownloadState.IN_PROGRESS.toString(), newFlags);

            if (downloader.resumeDownloadTask(id)) {
                notifyDownloadResumeSucceeded(intent);
            } else {
                notifyDownloadResumeFailed(intent);
            }
        } finally {
            // Unlock the database
            resolver.query(DownloadQueueProvider.getDownloadCompleteTransactionUri(this), null, null, null, null);
        }
    }

    /**
     *
     * @param intent The intent to notify that media is unmounted.
     */
    private void processMediaUnmountedRemoved(final Intent intent) {
        final Uri unmountMedia = intent.getData();
        if (unmountMedia == null) {
            Log.d(LOG_TAG, "unable to get unmounted media URI");
            return;
        }

        Log.d(LOG_TAG, "unmountMedia " + unmountMedia.getPath());
        final Collection<Long> downloadIds = getDownloadIdsByPath(unmountMedia.getPath());
        for (final Long downloadId : downloadIds) {
            Log.i(LOG_TAG, "cancelling download " + downloadId + " due to media removal");
            final Intent removeDownloadIntent = new Intent(ACTION_CANCEL_DOWNLOAD);
            removeDownloadIntent.putExtras(intent);
            removeDownloadIntent.putExtra(DownloadService.EXTRA_LONG_ID, downloadId);
            cancelDownload(removeDownloadIntent);
        }
    }

    /**
     * Perform cleanup of the download queue.
     * This will delete download entries that COMPLETED more than 7 days ago or FAILED
     * more than 28 days ago.
     */
    private void cleanupDownloadQueue() {
        final long currentTime = System.currentTimeMillis();
        Log.d(LOG_TAG, "Cleaning up download queue.");
        final int rowDeleted =
            getContentResolver().delete(downloadQueueContentUri,
                "( ? - " + DownloadQueueProvider.COLUMN_DOWNLOAD_CREATE_TIMESTAMP + " >= "
                    + " CAST(? AS INTEGER) AND "
                    + DownloadQueueProvider.COLUMN_DOWNLOAD_STATUS + " =? " + ") OR ( ? - "
                    + DownloadQueueProvider.COLUMN_DOWNLOAD_CREATE_TIMESTAMP + " >= "
                    + " CAST(? AS INTEGER) AND "
                    + DownloadQueueProvider.COLUMN_DOWNLOAD_STATUS + " =? )",
                new String[]{
                    String.valueOf(currentTime),
                    String.valueOf(COMPLETED_DOWNLOAD_CLEANUP_DELAY),
                    DownloadState.COMPLETE.toString(),
                    String.valueOf(currentTime),
                    String.valueOf(FAILED_DOWNLOAD_CLEANUP_DELAY),
                    DownloadState.FAILED.toString()
                });
        Log.d(LOG_TAG, rowDeleted + " row(s) deleted during download queue cleanup.");

        downloader.onCleanupAction();
    }

    /**
     * Notify that a download was enqueued.
     * 
     * @param intent The intent to notify that a download request is enqueued.
     * @param downloadId The unique download id for this download request.
     */
    private void notifyDownloadEnqueued(final Intent intent, final long downloadId) {
        final Intent downloadEnqueuedIntent = createDownloadEnqueuedIntent(intent, downloadId);
        localBroadcastManager.sendBroadcast(downloadEnqueuedIntent);
    }

    /**
     * Notify that a download failed to be enqueued.
     * 
     * @param intent The intent to copy extras from.
     */
    private void notifyDownloadEnqueueFailed(final Intent intent) {
        notifyActionWithOriginalIntent(DownloadService.ACTION_DOWNLOAD_ENQUEUE_FAILED, intent);
    }

    /**
     * Notify that a request to remove a download succeeded.
     * 
     * @param intent the intent to copy extras from.
     */
    private void notifyDownloadRemoved(final Intent intent) {
        notifyActionWithOriginalIntent(DownloadService.ACTION_DOWNLOAD_REMOVED, intent);
    }

    /**
     * notify that a request to remove a download failed.
     * 
     * @param intent the intent to copy extras from.
     */
    private void notifyDownloadRemoveFailed(final Intent intent) {
        notifyActionWithOriginalIntent(DownloadService.ACTION_DOWNLOAD_REMOVE_FAILED, intent);
    }

    /**
     * Send a broadcast to say pausing a download failed.
     * 
     * @param intent the original intent to which we're responding.
     */
    private void notifyDownloadPauseFailed(final Intent intent) {
        notifyActionWithOriginalIntent(DownloadService.ACTION_REQUEST_PAUSE_FAILED, intent);
    }

    /**
     * Send a broadcast to say pausing a download succeeded.
     * 
     * @param intent the original intent to which we're responding.
     */
    private void notifyDownloadPauseSucceeded(final Intent intent) {
        notifyActionWithOriginalIntent(DownloadService.ACTION_REQUEST_PAUSE_SUCCEEDED, intent);
    }

    /**
     * Send a broadcast to say pausing a download failed.
     * 
     * @param intent the original intent to which we're responding.
     */
    private void notifyDownloadResumeFailed(final Intent intent) {
        notifyActionWithOriginalIntent(DownloadService.ACTION_DOWNLOAD_RESUME_FAILED, intent);
    }

    /**
     * Send a broadcast to say pausing a download succeeded.
     * 
     * @param intent the original intent to which we're responding.
     */
    private void notifyDownloadResumeSucceeded(final Intent intent) {
        notifyActionWithOriginalIntent(DownloadService.ACTION_DOWNLOAD_RESUMED, intent);
    }

    /**
     * Send a broadcast to say canceling a download failed.
     *
     * @param intent the original intent to which we're responding.
     */
    private void notifyDownloadCancelFailed(final Intent intent) {
        notifyActionWithOriginalIntent(DownloadService.ACTION_DOWNLOAD_CANCEL_FAILED, intent);
    }

    /**
     * Send a broadcast to say canceling a download succeeded.
     *
     * @param intent the original intent to which we're responding.
     */
    private void notifyDownloadCancelSucceeded(final Intent intent) {
        notifyActionWithOriginalIntent(DownloadService.ACTION_DOWNLOAD_CANCELED, intent);
    }

    /**
     * Send a broadcast for a given action from a given intent.
     * 
     * @param action the action to create the intent with
     * @param originalIntent the intent to copy extras from
     */
    private void notifyActionWithOriginalIntent(final String action, final Intent originalIntent) {
        final Intent intent = new Intent(action);
        final Bundle extras = originalIntent.getExtras();
        if (null != extras) {
            intent.putExtras(extras);
        }
        localBroadcastManager.sendBroadcast(intent);
    }

    /**
     * Update the state and flags for a given row.
     * 
     * @param id
     *            the row ID to update
     * @param state
     *            the new value for the state column
     * @param flags
     *            the new value for the flags column
     */
    private void updateStateAndFlags(final String id, final String state, final int flags) {
        final ContentValues values = new ContentValues();
        values.put(DownloadQueueProvider.COLUMN_DOWNLOAD_STATUS, state);
        values.put(DownloadQueueProvider.COLUMN_DOWNLOAD_USER_FLAGS, flags);

        final ContentResolver resolver = getContentResolver();
        resolver.update(downloadQueueContentUri, values,
            DownloadQueueProvider.COLUMN_DOWNLOAD_ID + " = ?", new String[] {
                id
            });
    }

    /**
     * Find download ids based on file path
     *
     * @param path
     *              the file path for download rows
     * @return
     *              download ids with file path started with path
     */
    private Collection<Long> getDownloadIdsByPath(final String path) {
        final ArrayList<Long> ids = new ArrayList<Long>();
        final String query = DownloadQueueProvider.COLUMN_DOWNLOAD_FILE_LOCATION + " like ? AND "
                + DownloadQueueProvider.COLUMN_DOWNLOAD_STATUS + " in (?, ?)";
        final Cursor cursor = QueryHelper.runQueryForDownloadRow(
                query,
                new String[] {
                        path + "%",
                        DownloadState.IN_PROGRESS.toString(),
                        DownloadState.NOT_STARTED.toString()
                },
                this,
                new String[] {
                        DownloadQueueProvider.COLUMN_DOWNLOAD_ID,
                        DownloadQueueProvider.COLUMN_DOWNLOAD_FILE_LOCATION
                }
        );
        if (cursor == null || !cursor.moveToFirst()) {
            if (cursor != null) {
                cursor.close();
            }
            return ids;
        }
        try {
            while (!cursor.isAfterLast()) {
                ids.add(cursor.getLong(0));
                Log.d(LOG_TAG, "found download id: " + cursor.getLong(0) + " path " + cursor.getString(1));
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return ids;
    }

    /**
     * Get the state and flags for a given row.
     * 
     * @param id
     *            the id of the row to get.
     * @return an array containing the status in element zero and the flags in element one
     */
    private String[] getStateAndFlags(final String id) {
        final String[] projection = {
            DownloadQueueProvider.COLUMN_DOWNLOAD_STATUS,
            DownloadQueueProvider.COLUMN_DOWNLOAD_USER_FLAGS,
        };
        final String query = "_id = ?";
        final String[] args = {
            id,
        };

        return QueryHelper.runDownloadQueryForRow(this, projection, query, args);
    }

    /**
     * @param intent
     *            The original intent to request a download.
     * @param downloadId
     *            The unique download id for this download request.
     * @return The intent to notify that a download request is enqueued.
     */
    private Intent createDownloadEnqueuedIntent(final Intent intent, final long downloadId) {
        final Intent downloadEnqueuedIntent = new Intent(DownloadService.ACTION_DOWNLOAD_ENQUEUED);
        // we know getExtras won't return null here
        downloadEnqueuedIntent.putExtras(intent.getExtras());
        downloadEnqueuedIntent.putExtra(DownloadService.EXTRA_LONG_ID,
            downloadId);
        return downloadEnqueuedIntent;
    }

    /**
     * A handler class for this service.
     */
    final class ServiceHandler extends Handler {

        /**
         * Create a new instance.
         * 
         * @param looper the looper to use.
         */
        public ServiceHandler(final Looper looper) {
            super(looper);
        }

        /**
         * Handle a message.
         * 
         * @param msg the message
         */
        @Override
        public void handleMessage(final Message msg) {
            onHandleIntent((Intent) msg.obj);
            stopSelfIfNoThreads(msg.arg1);
        }
    }
}
