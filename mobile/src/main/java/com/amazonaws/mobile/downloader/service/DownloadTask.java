package com.amazonaws.mobile.downloader.service;

import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

import com.amazonaws.mobile.downloader.policy.DownloadPolicyProvider;

/**
 * A class to represent a single file to download.
 */
public final class DownloadTask implements Callable<Boolean> {
    /** Default success message. (be quiet for now) */
    public static final String MSG_OKAY = null;

    /** Message denoting bad destination argument. */
    public static final String MSG_BAD_DEST = "Destination provided was null.";
    
    /** Message denoting destination argument with null parent directory. */
    public static final String MSG_BAD_DIR =
        "Destination provided has a null parent directory.";

    /** Message denoting no network. */
    public static final String MSG_NO_NETWORK = "Network unavailable.";

    /** Message denoting bad URI argument. */
    public static final String MSG_BAD_URI = "URI provided was null.";

    /** Message denoting failure to create directory. */
    public static final String MSG_COULDNT_MKDIR =
        "Could not create requested directory.";

    /** Message denoting a vetoed download. */
    public static final String MSG_VETO = "Policy prohibits download: ";

    /** Message denoting a cancelled download. */
    public static final String MSG_CANCELED_DOWNLOAD = "Download is cancelled.";

    /** Message denoting a paused download. */
    public static final String MSG_PAUSED_DOWNLOAD = "Download is paused.";

    /** Prefix for exceptions. */
    public static final String MSG_UNEXPECTED_INTERRUPTION =
        "Download task was interrupted unexpectedly.";

    /** Prefix for HTTP status codes. */
    public static final String MSG_PREFIX_HTTP = "Unsuccessful response, HTTP status code: ";

    /** Our logger, for messages. */
    private static final String LOG_TAG = DownloadTask.class.getSimpleName();

    /** The Content-Length header. */
    private static final String HEADER_CONTENT_LENGTH = "Content-Length";

    /** The Content-Type header. */
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    /** The Range header. */
    private static final String HEADER_RANGE = "Range";

    /** The If-Range header. */
    private static final String IF_RANGE_HEADER = "If-Range";

    /** The format for a range header. */
    private static final String RANGE_FORMAT = "bytes=%d-";

    /** Default buffer size. */
    private static final int BUFFER_SIZE = 32 * 1024;

    /** The number of retries for IOExceptions. */
    private static final int NUM_RETRIES = 3;

    /** The ID we're downloading for. */
    private final long downloadId;

    /** The URI to download. */
    private final String uri;

    /** The destination to download to. */
    private final String destination;

    /** The eTag of this download. */
    private String downloadTag;

    /** The offset to start downloading at. */
    private long downloadOffset;

    /** The total size to be downloaded. */
    private long totalBytes;

    /** The amount read so far. */
    private long cumulativeBytesRead;

    /** Our download listener. */
    private final WeakReference<DownloadListener> listener;

    /** Our policy provider. */
    private final WeakReference<DownloadPolicyProvider> provider;

    /** Whether this task is (directly) on behalf of a user. */
    private final boolean forUser;

    /** Whether this task is silent (ie sends no progress). */
    private boolean isSilent;

    /** Whether this task may be downloaded over a cellular network. */
    private boolean isMobileNetworkProhibited;

    /** A failure message. */
    private String failureMessage;

    /** An object to help us know if the network is up. */
    private final NetworkStatusProvider networkStatusProvider;

    /** Whether this was auto-restarted. */
    private final boolean autoRestart;

    /** The error code for download errors. */
    private String downloadErrorCode;

    /** Flag indicating this task has been canceled by user request. */
    volatile TaskCancelReason cancelReason;

    /**
     * Create a new instance.
     * 
     * @param builder
     *            the builder from which to get values
     */
    private DownloadTask(final Builder builder) {
        downloadId = builder.id;
        uri = builder.downloadUri;
        destination = builder.dest;
        listener = new WeakReference<>(builder.listener);
        provider = new WeakReference<>(builder.provider);
        downloadTag = builder.eTag;
        downloadOffset = 0L;
        totalBytes = 0L;
        try {
            if (null != builder.offset) {
                downloadOffset = Long.parseLong(builder.offset);
            }
        } catch (final NumberFormatException ex) {
            Log.e(LOG_TAG, "Error trying to figure out offset to start download", ex);
        }

        try {
            if (null != builder.totalBytes) {
                totalBytes = Long.parseLong(builder.totalBytes);
            }
        } catch (final NumberFormatException ex) {
            Log.e(LOG_TAG, "Error trying to figure out totalBytes to start download", ex);
        }

        forUser = DownloadFlags.isUserRequestFlagSet(builder.downloadFlags);
        isSilent = DownloadFlags.isSilentFlagSet(builder.downloadFlags);
        isMobileNetworkProhibited = DownloadFlags.isCellNetworkProhibited(builder.downloadFlags);
        networkStatusProvider = builder.networkStatusProvider;
        autoRestart = builder.autoRestart;
        downloadErrorCode = DownloadError.NO_ERROR.getValue();
        cancelReason = TaskCancelReason.UNEXPECTED;
    }

    /**
     * Execute the task.
     * 
     * @return true if it works, false otherwise.
     */
    @Override
    public Boolean call() {
        final long start = System.nanoTime();
        CompletionStatus result = CompletionStatus.FAILED;
        try {
            Log.d(LOG_TAG, "Download Task started for download id = " + downloadId);
            failureMessage = MSG_OKAY;
            downloadErrorCode = DownloadError.NO_ERROR.getValue();

            if (!ensureFolderExists(destination)) {
                finish(CompletionStatus.FAILED, failureMessage, cumulativeBytesRead, totalBytes, autoRestart,
                    downloadErrorCode);
                return false;
            }

            if (null != downloadTag && 0L < downloadOffset) {
                if (!ensurePartialFilePresent(destination, downloadOffset)) {
                    // if we don't have the partial file any more, we have to start again
                    downloadOffset = 0L;
                }
            }

            result = readFromUri(destination);
            finish(result, failureMessage, cumulativeBytesRead, totalBytes, autoRestart, downloadErrorCode);
            return CompletionStatus.SUCCEEDED == result;
        } finally {
            // Make sure we release wifi lock (if any)
            networkStatusProvider.releaseWifiLock(downloadId);

            final long duration = System.nanoTime() - start;

            Log.d(LOG_TAG, String.format("Download result(%s) for download task with uri(%s) took %.3f seconds.",
                result.name(), uri, (double) duration / 1000000000));
        }
    }

    /**
     * Helper method to report back starting the download.
     */
    /* package */ void start() {
        final DownloadListener listenerObj = listener.get();
        if (null != listenerObj) {
            listenerObj.start(downloadId);
        }
    }

    /**
     * Helper method to report back our status if we still have a reference
     * to our listener Downloader.
     * 
     * @param withStatus
     *            whether to report we worked or failed
     * @param completionMessage
     *            a message to describe the state further
     * @param bytesRead
     *            the number of bytes successfully downloaded
     * @param total
     *            the supposed total size of the download
     * @param ars
     *            whether the task was a restart which was not initiated by user action
     * @param downloadError
     *            error code with a DownloadError enum value
     */
    /* package */ void finish(final CompletionStatus withStatus, final String completionMessage,
        final long bytesRead, final long total, final boolean ars, final String downloadError) {
        Log.d(LOG_TAG, String.format("finish(%s, %s) called.", withStatus, completionMessage));
        final DownloadListener listenerObj = listener.get();
        if (null != listenerObj) {
            listenerObj.finish(downloadId, withStatus, completionMessage, bytesRead, total, ars, downloadError);
        }
    }

    /**
     * Ensure that the destination folder exists.
     * 
     * @param dest
     *            the final file name desired.
     * @return true if we could ensure the prerequisites
     */
    /* package */ boolean ensureFolderExists(final String dest) {
        Log.d(LOG_TAG, "ensureFolderExists(" + dest + ") called");
        if (null == dest) {
            failureMessage = MSG_BAD_DEST;
            downloadErrorCode = DownloadError.BAD_DESTINATION.getValue();
            return false;
        }

        final File f = new File(dest);
        final File dir = f.getParentFile();
        
        // This can occur if the directory exists but another
        // process is using it.
        if (null == dir) {
            failureMessage = MSG_BAD_DIR;
            downloadErrorCode = DownloadError.BAD_DIRECTORY.getValue();
            Log.e(LOG_TAG, failureMessage);
            return false;
        }
        
        // mkdirs returns false if the directory already exists
        // so check here before calling it.
        if (dir.exists()) {
            return true;
        }

        final boolean result = dir.mkdirs();
        if (!result) {
            failureMessage = MSG_COULDNT_MKDIR;
            downloadErrorCode = DownloadError.COULDNT_MKDIR.getValue();
        }
        return result;
    }

    /**
     * Ensure that a partial download file exists.
     * 
     * @param dest
     *            the file to check
     * @param offset
     *            how big we think it should be
     * @return true if things look good
     */
    /* package */ boolean ensurePartialFilePresent(final String dest, final long offset) {
        if (null == dest) {
            failureMessage = MSG_BAD_DEST;
            downloadErrorCode = DownloadError.BAD_DESTINATION.getValue();
            return false;
        }

        final File f = new File(dest);
        final boolean result = f.exists() && f.length() >= offset;
        if (!result) {
            Log.d(LOG_TAG, "Bad partial file");
        }
        return result;
    }

    // CHECKSTYLE:SUPPRESS:MethodLength
    /**
     * Read the data from the URI.
     * 
     * @param dest
     *            where to write it to.
     * @return true if we read the entire stream.
     */
    /* package */ CompletionStatus readFromUri(final String dest) {
        cumulativeBytesRead = 0L;
        if (null == uri) {
            failureMessage = MSG_BAD_URI;
            downloadErrorCode = DownloadError.BAD_URI.getValue();
            return CompletionStatus.FAILED;
        }
        if (!haveNetwork()) {
            Log.i(LOG_TAG, "No network appears available, insta-pausing download");
            failureMessage = MSG_NO_NETWORK;
            downloadErrorCode = DownloadError.NO_NETWORK.getValue();
            return CompletionStatus.PAUSED;
        }

        // Give the download policy a chance to veto based only on the URI
        final DownloadPolicyProvider dpp = provider.get();
        if (null != dpp) {
            final DownloadPolicyProvider.Response policyResponse = dpp.mayStartDownload(forUser, uri);
            if (policyResponse != null) {
                final boolean policyResponseResponse = policyResponse.getResponse();
                if (!policyResponseResponse) {
                    failureMessage = MSG_VETO + policyResponse.getReason();
                    downloadErrorCode = DownloadError.POLICY_ERROR.getValue();
                    if (policyResponse.getShouldPause()) {
                        return CompletionStatus.PAUSED;
                    }
                    return CompletionStatus.FAILED;
                }
            } else {
                Log.w(LOG_TAG, "DownloadPolicyProvider response was null!");
            }
        }

        // Acquire a wifi lock if required.
        networkStatusProvider.acquireWifiLock(downloadId);
        int retryAttempt = 0;
        while (retryAttempt < NUM_RETRIES) {
            start();
            InputStream stream = null;
            RandomAccessFile output = null;
            try {
                output = new RandomAccessFile(dest, "rw");

                final URL url = new URL(uri);
                final HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                setupRequest(httpURLConnection);

                // Open connection and send headers.
                stream = httpURLConnection.getInputStream();

                // check response
                final int responseCode = httpURLConnection.getResponseCode();

                if (!gotSuccessResponse(responseCode)) {
                    Log.w(LOG_TAG, "Did not get a 2xx response code back from request.");
                    // Could use getErrorStream to read back error info.
                    failureMessage = MSG_PREFIX_HTTP + responseCode;
                    // get error code string
                    downloadErrorCode = DownloadError.HTTP_ERROR.getValue();
                    return CompletionStatus.FAILED;
                }

                if (!gotValidRangeResponse(responseCode)) {
                    // we didn't get a 206, but instead a 200 indicating to re-read the entire entity
                    Log.w(LOG_TAG, "Did not get a 206 response code back from request.");
                    downloadOffset = 0L;
                }

                // if we don't currently have an eTag for this download, try to set from the response headers.
                if (downloadTag == null) {
                    downloadTag = httpURLConnection.getHeaderField(Downloader.HEADER_ETAG);
                }

                // update the mime type and other fields in the content provider whether the actual
                // read is vetoed or not
                updateProviderFromHeaders(httpURLConnection);

                if (totalBytes == 0L) {
                    totalBytes = getContentLengthFromHeader(httpURLConnection) + downloadOffset;
                }
                final String mt = getMimeType(httpURLConnection);
                if (null != dpp) {
                    // Give the download policy a chance to veto based on the URI and file type and size
                    final DownloadPolicyProvider.Response policyResponse =
                        dpp.mayReadStream(forUser, uri, totalBytes, mt);
                    if (policyResponse != null) {
                        final boolean policyResponseResponse = policyResponse.getResponse();
                        if (!policyResponseResponse) {
                            // We are aborting the request, since the finally block closes the stream,
                            // there is nothing additional to do here.
                            failureMessage = MSG_VETO + policyResponse.getReason();
                            downloadErrorCode = DownloadError.POLICY_ERROR.getValue();
                            if (policyResponse.getShouldPause()) {
                                return CompletionStatus.PAUSED;
                            }
                            return CompletionStatus.FAILED;
                        }
                    } else {
                        Log.w(LOG_TAG, "DownloadPolicyProvider response was null!");
                    }
                }

                output.seek(downloadOffset);
                final byte[] buffer = new byte[BUFFER_SIZE];
                cumulativeBytesRead = downloadOffset;
                int bytesRead;
                boolean interrupted = false;

                while ((bytesRead = stream.read(buffer)) > 0) {
                    cumulativeBytesRead += bytesRead;
                    output.write(buffer, 0, bytesRead);

                    if (!isSilent) {
                        sendProgress(cumulativeBytesRead, totalBytes);
                    }

                    if (Thread.interrupted() || !TaskCancelReason.UNEXPECTED.equals(getCancelReason())) {
                        Log.i(LOG_TAG, "Download task is interrupted");
                        switch (getCancelReason()) {
                            case UNEXPECTED:
                                Log.w(LOG_TAG, "Unexpected interruption of download task.");
                                failureMessage = MSG_UNEXPECTED_INTERRUPTION;
                                downloadErrorCode = DownloadError.DOWNLOAD_INTERRUPTED.getValue();
                                retryAttempt = NUM_RETRIES;
                                break;
                            case PAUSED_BY_USER:
                                // Set the failure message since this gets passed back as a completion description.
                                failureMessage = MSG_PAUSED_DOWNLOAD;
                                downloadErrorCode = DownloadError.USER_PAUSED.getValue();
                                return CompletionStatus.PAUSED_BY_USER;
                            case CANCELED_BY_USER:
                                failureMessage = MSG_CANCELED_DOWNLOAD;
                                downloadErrorCode = DownloadError.USER_CANCELED.getValue();
                                interrupted = true;
                                retryAttempt = Integer.MAX_VALUE;
                                break;
                        }
                        break;
                    }
                }

                if (cumulativeBytesRead > totalBytes) {
                    Log.e(LOG_TAG,
                        String.format(
                            "Cumulative bytes read exceeded the total length of the file. read=%d expected=%d",
                            cumulativeBytesRead, totalBytes));
                    return CompletionStatus.FAILED;
                }

                if (!interrupted && (cumulativeBytesRead == totalBytes)) {
                    return CompletionStatus.SUCCEEDED;
                }
            } catch (final IOException ex) {
                Log.e(LOG_TAG, "Caught IO exception while downloading", ex);
                failureMessage = ex.getClass().getSimpleName() + ": " + ex.getMessage();
                downloadErrorCode = DownloadError.IO_EXCEPTION.getValue();
                retryAttempt++;
                downloadOffset = cumulativeBytesRead;
            } finally {
                safeClose(stream);
                safeClose(output);
            }

            // if we succeeded in getting any of the file, report a pause so that it can be resumed.
            if (haveDownloadProgress() && retryAttempt == NUM_RETRIES) {
                return CompletionStatus.PAUSED;
            }
        }
        return CompletionStatus.FAILED;
    }

    /** Setup the web request. */
    private void setupRequest(final HttpURLConnection httpURLConnection) {
        // Set default method to get
        //httpURLConnection.setRequestMethod("GET");
        //httpURLConnection.setReadTimeout(10000 /* milliseconds */);
        //httpURLConnection.setConnectTimeout(15000 /* milliseconds */);
        //httpURLConnection.setDoInput(true);

        if (null != downloadTag && 0L < downloadOffset) {
            httpURLConnection.setRequestProperty(HEADER_RANGE, String.format(RANGE_FORMAT, downloadOffset));
            httpURLConnection.setRequestProperty(IF_RANGE_HEADER, downloadTag);
        }
    }

    /**
     * Get the total length of the file, if known. If not, we return -1
     * 
     * @param connection the http url connection to use to examine relevant headers
     * @return the length or -1
     */
     /* package */ static long getContentLengthFromHeader(final HttpURLConnection connection) {
        long result = -1L;
        final String contentLength = connection.getHeaderField(HEADER_CONTENT_LENGTH);
        if (null != contentLength) {
            try {
                result = Long.parseLong(contentLength);
            } catch (final NumberFormatException ex) {
                Log.e(LOG_TAG, "Error trying to parse content length header.", ex);
            }
        }
        return result;
    }

    /**
     * Get the MIME type from the headers, if known. If not, we return null.
     * 
     * @param connection
     *            the HttpResponse to examine for the relevant headers
     * @return the MIME type or null
     */
    /* package */ String getMimeType(final HttpURLConnection connection) {
        return connection.getHeaderField(HEADER_CONTENT_TYPE);
    }

    /**
     * Request the downloader to update the content provider given some headers.
     * 
     * @param connection the http connection that received headers.
     */
    /* package */ void updateProviderFromHeaders(final HttpURLConnection connection) {
        // Might want to log response headers here by logging connection.getHeaderFields().
        final DownloadListener listenerObj = listener.get();
        if (null != listenerObj) {
            listenerObj.headersReceived(downloadId, connection);
        }
    }

    /**
     * Send a progress broadcast and persist the current state.
     * 
     * @param bytesRead the number of bytes read so far.
     * @param total the total number of bytes to read, or -1 if not known.
     */
    /* package */ void sendProgress(final long bytesRead, final long total) {
        final DownloadListener listenerObj = listener.get();
        if (null != listenerObj) {
            listenerObj.sendProgress(downloadId, bytesRead, total);
        }
    }

    /**
     * Check whether we got a 2xx response back from a request.
     * 
     * @param responseCode the http response code.
     * @return true if we got a 2xx
     */
    /* package */ boolean gotSuccessResponse(final int responseCode) {
        return responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE;
    }

    /**
     * Check whether we got a 206 response  back from a (potential) range request.
     * 
     * @param responseCode the http response code.
     * @return true if we specifically got a 206 response.
     */
    /* package */ boolean gotValidRangeResponse(final int responseCode) {
        return responseCode == HttpURLConnection.HTTP_PARTIAL;
    }

    /**
     * Close a closable if it is not null.
     * 
     * @param stream the stream.
     */
    /* package */ void safeClose(final Closeable stream) {
        if (null == stream) {
            return;
        }

        try {
            stream.close();
        } catch (final IOException ex) {
            Log.e(LOG_TAG, "Caught exception trying to close stream", ex);
        }
    }

    /**
     * @return true if we have actually downloaded anything.
     */
    private boolean haveDownloadProgress() {
        final File f = new File(destination);
        return f.exists() && f.isFile() && f.length() > 0L;
    }

    /**
     * Determine if we have the network available.
     * 
     * @return true if the network is there
     */
    private boolean haveNetwork() {
        if (null == networkStatusProvider) {
            Log.w(LOG_TAG, "haveNetwork, status provider is null.");
            return true; // fail open
        }
        Log.d(LOG_TAG, "haveNetwork, calling provider...");
        final boolean result = networkStatusProvider.isNetworkAvailable(isMobileNetworkProhibited);
        Log.d(LOG_TAG, "haveNetwork, returning " + result);
        return result;
    }

    /**
     * Call to set the reason for download task cancellation.
     * @param reason the reason the download is being canceled.
     */
    /* package */ void setCancelReason(final TaskCancelReason reason) {
        cancelReason = reason;
    }

    /* package */ TaskCancelReason getCancelReason() {
        return cancelReason;
    }

    /**
     * Possible reasons for task cancellation.
     */
    /* package */ enum TaskCancelReason {
        UNEXPECTED,
        PAUSED_BY_USER,
        CANCELED_BY_USER
    }

    /**
     * Builder class for DownloadTask.
     */
    public static final class Builder {
        /** the download ID. */
        private final long id;

        /** The URI. */
        private String downloadUri;

        /** The destination. */
        private String dest;

        /** The eTag, if any. */
        private String eTag;

        /** The offset, if any. */
        private String offset;

        /** The total bytes, if any. */
        private String totalBytes;

        /** Download flags. */
        private int downloadFlags;

        /** Our listener. */
        private DownloadTask.DownloadListener listener;

        /** Our policy. */
        private DownloadPolicyProvider provider;

        /** Our network status provider. */
        private NetworkStatusProvider networkStatusProvider;

        /** Whether we are an auto-restarted task. */
        private boolean autoRestart;

        /**
         * Create a new instance.
         * 
         * @param downloadId
         *            the id, which is required for all requests.
         */
        public Builder(final long downloadId) {
            this.id = downloadId;
        }

        /**
         * set the URI.
         * 
         * @param uri
         *            the URI to set.
         * @return the builder.
         */
        public Builder withUri(final String uri) {
            this.downloadUri = uri;
            return this;
        }

        /**
         * Set the destination.
         * 
         * @param destination
         *            the destination to set.
         * @return the builder
         */
        public Builder withDestination(final String destination) {
            this.dest = destination;
            return this;
        }

        /**
         * Set the eTag.
         * 
         * @param tag
         *            the tag to set.
         * @return the builder
         */
        public Builder withTag(final String tag) {
            this.eTag = tag;
            return this;
        }

        /**
         * Set the offset.
         * 
         * @param off
         *            the offset to set
         * @return the builder
         */
        public Builder withOffset(final String off) {
            this.offset = off;
            return this;
        }

        /**
         * Set the total bytes.
         * 
         * @param total
         *            the total bytes to set
         * @return the builder
         */
        public Builder withTotalBytes(final String total) {
            this.totalBytes = total;
            return this;
        }

        public Builder withDownloadFlags(final int downloadFlags) {
            this.downloadFlags = downloadFlags;
            return this;
        }

        /**
         * Set the listener.
         * 
         * @param listener
         *            the listener
         * @return the builder
         */
        public Builder withListener(final DownloadTask.DownloadListener listener) {
            this.listener = listener;
            return this;
        }

        /**
         * Set the policy provider.
         * 
         * @param provider
         *            the provider
         * @return the builder
         */
        public Builder withProvider(final DownloadPolicyProvider provider) {
            this.provider = provider;
            return this;
        }

        /**
         * Set the networkStatusProvider.
         * 
         * @param provider the network status provider.
         * @return the builder
         */
        public Builder withNetworkStatusProvider(final NetworkStatusProvider provider) {
            this.networkStatusProvider = provider;
            return this;
        }

        /**
         * Set whether this was auto-restarted.
         * 
         * @param ars true if auto restarted
         * @return the builder
         */
        public Builder withAutoRestart(final boolean ars) {
            this.autoRestart = ars;
            return this;
        }

        /**
         * Build a DownloadTask.
         * 
         * @return the task
         */
        public DownloadTask build() {
            return new DownloadTask(this);
        }

    }

    /**
     * An interface which can be implemented by whoever starts the download task,
     * to be notified of events.
     */
    public interface DownloadListener {
        /**
         * Report that a download was started.
         * 
         * @param downloadId
         *            the id of the download
         */
        void start(final long downloadId);

        /**
         * Report that the headers were read from the server.
         * 
         * @param downloadId
         *            the id of the download
         * @param connection
         *            the http connection containing the received headers.
         */
        void headersReceived(final long downloadId, final HttpURLConnection connection);

        /**
         * Report that the download progressed.
         * 
         * @param downloadId
         *            the id of the download
         * @param bytesRead
         *            the progress so far
         * @param totalBytes
         *            the total to be read (or -1 if not known)
         */
        void sendProgress(final long downloadId, final long bytesRead, final long totalBytes);

        /**
         * Report that a download task terminated, whether successfully or not.
         * 
         * @param downloadId the id of the download.
         * @param withStatus reports the state in which the download has been left.
         * @param completionMessage
         *            extra details to be passed back to the caller
         * @param bytesRead
         *            bytes read so far
         * @param totalBytes
         *            total size of requested download
         * @param autoRestart
         *            whether the task was a restart which was not initiated by user action
         * @param downloadError
         *            error code with a DownloadError enum value
         */
        void finish(final long downloadId, final CompletionStatus withStatus, final String completionMessage,
            final long bytesRead, final long totalBytes, final boolean autoRestart, final String downloadError);
    }

    /**
     * An interface to tell a download task whether the network is available and provide methods
     * to acquire and release wifi locks.
     */
    public interface NetworkStatusProvider {
        /**
         * @return true if the network is available.
         */
        boolean isNetworkAvailable(boolean isMobileNetworkProhibited);

        /**
         * Acquire a wifi lock and associate it with a download Id.
         * The wifi lock will be acquired only if it was specified as part of the request intent for that download Id.
         * 
         * @param longDownloadId the download id
         */
        void acquireWifiLock(final long longDownloadId);

        /**
         * Release the Wifi lock (if any) associated with a particular download ID.
         * 
         * @param longDownloadId
         *            the id
         */
        void releaseWifiLock(final long longDownloadId);
    }
}
