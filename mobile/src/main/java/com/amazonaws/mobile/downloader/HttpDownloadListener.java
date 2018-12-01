package com.amazonaws.mobile.downloader;

import com.amazonaws.mobile.downloader.query.DownloadState;

public interface HttpDownloadListener {
    /**
     * Called when the state of the download is changed.
     *
     * @param id The id of the download record.
     * @param state The new state of the transfer.
     */
    void onStateChanged(long id, DownloadState state);

    /**
     * Called when more bytes are downloaded.
     *
     * @param id The id of the download record.
     * @param bytesCurrent Bytes transferred currently.
     * @param bytesTotal The total bytes to be transferred.
     */
    void onProgressChanged(long id, long bytesCurrent, long bytesTotal);

    /**
     * Called when an exception happens.
     *
     * @param id The id of the transfer record.
     */
    void onError(long id, HttpDownloadException ex);
}
