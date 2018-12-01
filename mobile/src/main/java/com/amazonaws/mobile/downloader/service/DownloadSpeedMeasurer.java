package com.amazonaws.mobile.downloader.service;

import android.util.Log;

/**
 * A class to measure the speed of a download and to decide whether to send progress.
 */
public class DownloadSpeedMeasurer {

    /** Log tag. */
    private static final String LOG_TAG = DownloadSpeedMeasurer.class.getSimpleName();

    /** The minimum progress percent change at which notification is allowed. */
    private static final int PERCENT_MUST_JUMP = 5;

    /** The minimum absolute size change at which notification is allowed.
     * should be a multiple of the download chunk size.
     */
    private static final long SIZE_MUST_JUMP = 96 * 1024;

    /** The minimum time which must elapse between progress broadcasts. */
    private static final long TIME_MUST_JUMP = 5000000000L; // 5 s in ns

    /** How many samples we should collect. */
    private static final int SPEED_SAMPLES_COUNT = 10;

    /** Minimum time between updates.  */
    private static final long MIN_TIME_BETWEEN_INTERVALS = 500000000L; // 500 ms in ns

    /** The constant one hundred. */
    private static final int HUNDRED_PERCENT = 100;

    /** Bytes per kilobyte. */
    private static final double BYTES_PER_KILOBYTE = 1024.0;

    /** Nanoseconds per second. */
    private static final double NANOS_PER_SECOND = 1e9;

    /** The total content length. */
    private long contentLength;

    /** The last value we reported for percent complete. */
    private int lastPercent = 0;

    /** The last size reported. */
    private long lastDownloadedSize = 0;

    /** The last time reported. */
    private long lastTime = 0;

    /** Samples for speed analysis. */
    private final double[] speedSamples = new double[SPEED_SAMPLES_COUNT];

    /** index into speed samples array. */
    private int index = 0;

    /** Size of accumulated samples. */
    private int size = 0;

    /**
     * Create a new instance.
     * @param startingOffset the amount downloaded in any previous requests.
     * @param contentLength the total amount to be downloaded.
     */
    public DownloadSpeedMeasurer(final long startingOffset, final long contentLength) {
        this.contentLength = contentLength;
        if (contentLength > 0) {
            lastPercent = (int) (HUNDRED_PERCENT * startingOffset / contentLength);
        } else {
            lastPercent = 0;
        }
        lastDownloadedSize = startingOffset;
        lastTime = System.nanoTime();
    }

    /**
     * Updates the amount downloaded. Returns true if the download has progressed enough
     * to report the progress, false otherwise.
     * 
     * @param downloaded the number of bytes downloaded so far
     * @return true if progress broadcast should be sent
     */
    public boolean updateProgress(final long downloaded) {
        int pct = 0;
        if (contentLength > 0) {
            pct = (int) (HUNDRED_PERCENT * downloaded / contentLength);
        }

        final long now = System.nanoTime();
        final boolean percentNotOkay = (pct - lastPercent < PERCENT_MUST_JUMP);
        final boolean sizeNotOkay = (downloaded - lastDownloadedSize < SIZE_MUST_JUMP);
        final boolean timeNotOkay = (now - lastTime < TIME_MUST_JUMP);

        if (percentNotOkay && sizeNotOkay && timeNotOkay) {
            return false;
        }

        // always wait at least this long
        if (now - lastTime < MIN_TIME_BETWEEN_INTERVALS) {
            return false;
        }

        final long sampleDuration = now - lastTime;
        final long sampleDownloaded = downloaded - lastDownloadedSize;
        speedSamples[index] = (sampleDownloaded / (sampleDuration / NANOS_PER_SECOND)) / BYTES_PER_KILOBYTE;
        index = (index + 1) % SPEED_SAMPLES_COUNT;
        size = Math.min(SPEED_SAMPLES_COUNT, size + 1);
        double average = 0.0;
        for (int i = 0; i < size; i++) {
            average += speedSamples[i];
        }
        average /= size;
        Log.v(LOG_TAG, String.format("updateProgress: %d/%d bytes, %f kB/s, %d%%", downloaded,
            contentLength, average, pct));

        lastPercent = pct;
        lastDownloadedSize = downloaded;
        lastTime = now;

        return true;
    }

}
