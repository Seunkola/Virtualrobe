package com.amazonaws.mobile.downloader.service;

/**
 * Known download response error codes.
 */
public enum DownloadError {    
    
    /**
     * This enum defines errors returned by DownloadService.
     * 
     * The enum value is set in DownloadService.EXTRA_DOWNLOAD_ERROR extra.
     */
    
    /** The no error name. */
    NO_ERROR("NoError"),
    
    /** The error name when destination provided is null. */
    BAD_DESTINATION("BadDestination"),
    
    /** The error name when destination provided has a null parent directory. */
    BAD_DIRECTORY("BadDirectory"),
    
    /** The error name when could not create requested directory. */
    COULDNT_MKDIR("CouldNotCreateDirectory"),
    
    /** The error name when Uri provided is null. */
    BAD_URI("BadUri"),
    
    /** The error name when Network is unavailable. */
    NO_NETWORK("NetworkUnavailable"),
    
    /** The error name when a policy specific error occurs. */
    POLICY_ERROR("DownloadPolicyError"),
    
    /** The error name for HTTP erros. */
    HTTP_ERROR("HttpError"),
    
    /** The error name for download error caused by IOException. */
    IO_EXCEPTION("IOException"),

    /** The error name for download error caused by IOException. */
    DOWNLOAD_INTERRUPTED("DownloadInterrupted"),
    
    /** The error name for user cancellation. */
    USER_CANCELED("UserCanceled"),

    /** The error name for user pause. */
    USER_PAUSED("UserPaused");


    /**
     * Instantiates a new download error enum object.
     * 
     * @param value
     *            the value of the enum
     */
    DownloadError(final String value) {
        this.value = value;
    }

    /** The value of the enum. */
    private final String value;

    /**
     * Returns the value of the enum on which it's called.
     * 
     * @return the value of the enum
     */
    public String getValue() {
        return value;
    }

}
