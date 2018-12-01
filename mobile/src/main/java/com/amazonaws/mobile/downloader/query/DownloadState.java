package com.amazonaws.mobile.downloader.query;

/** An enum of possible states. */
public enum DownloadState {
    /** Download task for the download is queued and waiting to start. */
    NOT_STARTED,

    /** Download in progress. */
    IN_PROGRESS,

    /** Download paused either by the user request or network lost. */
    PAUSED,

    /** Download failed and the downloading file was removed. */
    FAILED,

    /** Completed successfully. */
    COMPLETE
}
