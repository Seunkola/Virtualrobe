package com.amazonaws.mobile.downloader.service;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.amazonaws.mobile.downloader.query.DownloadState;
import com.amazonaws.mobile.downloader.query.DownloadQueueProvider;

/**
 * A class to represent the task of initializing the queue from the content provider.
 */
/* package */ final class QueueReaderTask implements Callable<Integer> {

    /** Our logger, for informational and error messages. */
    private static final String LOG_TAG = QueueReaderTask.class.getSimpleName();

    /** Our parent downloader. */
    private final WeakReference<Downloader> parent;

    /** Cursor with query response of the existing downloads (not completed or failed). */
    private final Cursor existingDownloads;

    /**
     * Create a new instance.
     * 
     * @param downloader the parent Downloader that created this task.
     * @param context the android context.
     */
    /* package */QueueReaderTask(final Downloader downloader,
        final Context context) {
        this.parent = new WeakReference<Downloader>(downloader);
        final ContentResolver resolver = context.getContentResolver();

        // Get all the rows that aren't complete or failed. This needs to be called from the context of the service
        // to prevent the possibility that a new download is being added while attempting to read the existing
        // downloads to restore the queue.  This is achieved by having this called as the start-up action that is
        // the first intent sent to download service.
        this.existingDownloads = resolver.query(DownloadQueueProvider.getDownloadContentUri(context),
            new String[] {
                DownloadQueueProvider.COLUMN_DOWNLOAD_ID,
                DownloadQueueProvider.COLUMN_DOWNLOAD_STATUS,
                DownloadQueueProvider.COLUMN_DOWNLOAD_USER_FLAGS
            },
            DownloadQueueProvider.COLUMN_DOWNLOAD_STATUS + " not in (?, ?)",
            new String[] {
                DownloadState.COMPLETE
                    .toString(),
                DownloadState.FAILED
                    .toString(),
            },
            null);
    }

    /**
     * Perform the task.
     *
     * @return the number of rows that were read from the content provider.
     */
    @Override
    public Integer call() {
        Log.d(LOG_TAG, "initializing the download queue.");

        // Iterate through rows, adding them as download tasks.
        int count = 0;
        if (existingDownloads != null) {
            try {
                final Downloader parentDownloader = parent.get();
                if (null == parentDownloader) {
                    Log.e(LOG_TAG, "Can't obtain reference to parent downloader.");
                    return 0;
                }

                if (existingDownloads.moveToFirst()) {
                    do {
                        Log.i(LOG_TAG, "Processing an existing download row!");

                        // If a request is paused by user request, it can only be restarted by user request.
                        // If it got paused for some other reason, it can be re-queued.
                        final int flags = existingDownloads.getInt(2);
                        if (DownloadState.PAUSED.toString().equals(existingDownloads.getString(1))
                            && DownloadFlags.isUserRequestFlagSet(flags)) {
                            continue;
                        }

                        // Add the qualifying row.
                        parentDownloader.addDownloadTask(existingDownloads.getLong(0));
                        ++count;
                        Log.i(LOG_TAG, "Done processing an existing download row!");
                    } while (existingDownloads.moveToNext());
                }
            } finally {
                existingDownloads.close();
            }
        }
        Log.i(LOG_TAG, count + " existing download rows read.");

        // We're no longer initializing, so if we're still empty, it's okay to finish the service.
        final Downloader parentObj = parent.get();
        if (null != parentObj) {
            parentObj.doneInitializing();
        }

        return count;
    }
}
