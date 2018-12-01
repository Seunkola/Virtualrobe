package com.amazonaws.mobile.downloader.policy;

/**
 * An interface which can be implemented by code which wishes to control the download process.
 */
public interface DownloadPolicyProvider {
    /**
     * Decide whether a download may be started. A network connection is guaranteed to exist.
     * @param forUser true if this request is the direct result of a user action
     * @param uri the URI we are going to attempt to download
     * @return true if the download is allowed
     */
    Response mayStartDownload(final boolean forUser, final String uri);

    /**
     * Decide whether content may actually be downloaded once the connection is opened.
     * @param forUser true if this request is the direct result of a user action
     * @param uri the URI we are going to attempt to download
     * @param contentLength the size to be downloaded, if known (or -1)
     * @param mimeType the type of content to be downloaded, if known (or null)
     * @return true if the download is allowed
     */
    Response mayReadStream(final boolean forUser, final String uri, final long contentLength, final String mimeType);

    /**
     * Class to encapsulate a true/false value and the reason for giving
     * that value (as a String).
     */
    class Response {
        /** The response. */
        private final boolean response;

        /** Whether to pause. */
        private final boolean pause;

        /** The reason. */
        private final String reason;

        /**
         * Create a new instance.
         * @param goahead whether the response is true or false
         * @param shouldPause whether to pause (instead of failing)
         * @param message the reason for giving the response.
         */
        public Response(final boolean goahead, final boolean shouldPause, final String message) {
            this.reason = message;
            this.response = goahead;
            this.pause = shouldPause;
        }

        /**
         * Get the response.
         * @return the response.
         */
        public boolean getResponse() {
            return response;
        }

        /** 
         * Get whether we should pause (as opposed to failing).
         * @return true if pause
         */
        public boolean getShouldPause() {
            return pause;
        }

        /**
         * Get the reason.
         * @return the reason
         */
        public String getReason() {
            return reason;
        }

        /** 
         * Factory method to create a new download pause response with a custom message.
         * @param message the reason for giving the response.
         * @return the response.
         */
        public static Response getPauseResponse(final String message) {
            return new Response(false, true, message);
        }

        /**
         * Factory method to create a new download fail response with a custom message.
         * @param message the reason for giving the response.
         * @return the response.
         */
        public static Response getFailResponse(final String message) {
            return new Response(false, false, message);
        }

        /**
         * Factory method to create a new success response with a custom message.
         * @param message the reason for giving the response.
         * @return the response.
         */
        public static Response getSuccessResponse(final String message) {
            return new Response(true, false, message);
        }
    }
}
