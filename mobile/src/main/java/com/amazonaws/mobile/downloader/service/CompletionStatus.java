package com.amazonaws.mobile.downloader.service;

/** Completion codes. */
public enum CompletionStatus {
    /** Download worked. */
    SUCCEEDED,

    /** Download failed due to connectivity, and was therefore paused. */
    PAUSED,

    /** Download task was interrupted due to user pause request. */
    PAUSED_BY_USER,

    /** Download task was interrupted due to user cancel request. */
    CANCELED,

    /** Download failed for other reasons, and was marked as failed. */
    FAILED
}
