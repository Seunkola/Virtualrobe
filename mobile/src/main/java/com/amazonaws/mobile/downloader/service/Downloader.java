package com.amazonaws.mobile.downloader.service;

import java.util.Arrays;
import java.util.List;

/**
 * A downloader that handles the actual downloading of files.
 */
public interface Downloader {
    /** The ETag header. */
    String HEADER_ETAG = "ETag";

    /** The content-type header. */
    String HEADER_CONTENT_TYPE = "Content-Type";

    /**
     * Report whether we are currently downloading anything.
     * 
     * @return false if downloading.
     */
    boolean isIdle();

    /**
     * Adds a download.
     * 
     * @param longId
     *            the identifier of the download.
     * @return true if the download task is added successfully.
     */
    boolean addDownloadTask(final long longId);

    /**
     * Request a download task by paused.
     * 
     * @param downloadId the identifier of the download.
     * @return true if the download task was successfully requested to pause.
     */
    boolean pauseDownloadTask(final long downloadId);
    
    /**
     * Request a download task be resumed.
     * 
     * @param downloadId the identifier of the download.
     * @return true if the download task was resumed, otherwise false.
     */
    boolean resumeDownloadTask(final long downloadId);

    /**
     * Shut down our executor service immediately.
     */
    void shutdownNow();

    /**
     * Set initializing to false.
     */
    void doneInitializing();

    /**
     * Request a download task be canceled.
     *
     * @param downloadId the identifier of the download.
     * @return true if the download task was successfully requested to pause.
     */
    boolean cancelDownloadTask(final long downloadId);

    /**
     * Handle cleanup action. This is called when DownloadService receives Cleanup action.
     */
    void onCleanupAction();

    /**
     * Handle restarting any downloads that had been queued previously.
     */
    void restartQueuedDownloads();
}
