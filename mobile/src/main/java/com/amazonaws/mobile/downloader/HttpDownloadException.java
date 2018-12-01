package com.amazonaws.mobile.downloader;

import java.io.IOException;

public class HttpDownloadException extends IOException {
    private static final long serialVersionUID = 2184676744021940936L;
    
    /** Error code from {@link com.amazonaws.mobile.downloader.service.DownloadError} */
    final String errorCode;

    /**
     * Constructs a new instance with the given detail message.
     */
    public HttpDownloadException(final String errorCode, final String errorDescription) {
        super(errorDescription);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new instance with the given detail message.
     */
    public HttpDownloadException(final String errorCode, final String errorDescription, final Throwable cause) {
        super(errorDescription, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
