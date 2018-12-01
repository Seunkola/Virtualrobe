package com.amazonaws.mobile.downloader.service;

import android.util.Log;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class to create download threads with the lowest priority.
 */
/* package */ final class MinPriorityThreadFactory implements ThreadFactory {

    /** Our logger, for informational and error messages. */
    private static final String LOG_TAG = MinPriorityThreadFactory.class.getSimpleName();

    /**
     * Priority of thread set to be lowest.
     */
    private static final int PRIORITY = Thread.MIN_PRIORITY;
    /**
     * Thread counts.
     */
    private final AtomicInteger mCount = new AtomicInteger(1);
    /**
     * Prefix of thread's name.
     */
    private final String prefix;

    /**
     * Constructor.
     * 
     * @param prefix
     *            A String prefix
     */
    public MinPriorityThreadFactory(final String prefix) {
        Log.d(LOG_TAG, "MinPriorityThreadFactory is created");
        this.prefix = prefix;
    }

    /*
     * (non-Javadoc)
     * @see
     * java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
     */
    @Override
    public Thread newThread(final Runnable runnable) {
        final String name = prefix + " #" + mCount.getAndIncrement();
        final Thread thread = new Thread(runnable, name);
        try {
            thread.setPriority(PRIORITY);
        } catch (final SecurityException e) {
            Log.w(LOG_TAG, name
                + ": SecurityException caught, could not set thread priority to "
                + PRIORITY, e);
        } catch (final IllegalArgumentException e) {
            Log.w(LOG_TAG, name
                + ": IllegalArgumentException caught, could not set thread priority to "
                + PRIORITY,
                e);
        }
        return thread;
    }
}
