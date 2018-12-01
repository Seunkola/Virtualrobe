package com.amazonaws.mobile.downloader;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.amazonaws.mobile.downloader.query.DownloadQueueProvider;
import com.amazonaws.mobile.downloader.query.DownloadState;
import com.amazonaws.mobile.downloader.query.QueryHelper;
import com.amazonaws.mobile.downloader.service.DownloadService;

import java.util.ArrayList;
import java.util.List;

public class HttpDownloadObserver {
    private final Context context;

    /** Log tag. */
    private static final String LOG_TAG = HttpDownloadObserver.class.getSimpleName();

    /** The download Id. */
    private long id;

    /** Bytes transferred so far. */
    private long bytesTransferred;

    /** File size of the download in bytes. */
    private long totalBytes;

    /** Destination file location. */
    private String fileLocation;

    /** Download url. */
    private String downloadUrl;

    /** The state of the download the last time this object was refreshed. */
    private DownloadState downloadState;

    /** The time at which this download was created. */
    private long creationTimestamp;

    /** Mime type. */
    private String mimeType;

    /** Title. */
    private String title;

    /** Map of listeners registered to this download observer. */
    /* package */ HttpDownloadListener downloadListener;

    /***
     * This enum of column indexes of the fields of the request to the data source.
     */
    private enum ColumnType {
        /** ID. */
        COL_ID(DownloadQueueProvider.COLUMN_DOWNLOAD_ID),
        /** Download url. */
        COL_URL(DownloadQueueProvider.COLUMN_DOWNLOAD_URL),
        /** File location. */
        COL_FILE_LOCATION(DownloadQueueProvider.COLUMN_DOWNLOAD_FILE_LOCATION),
        /**  Mime type. */
        COL_MIME_TYPE(DownloadQueueProvider.COLUMN_DOWNLOAD_MIME_TYPE),
        /** Bytes Transferred. */
        COL_BYTES_TRANSFERRED(DownloadQueueProvider.COLUMN_DOWNLOAD_CURRENT_SIZE),
        /** Total file size. */
        COL_FILE_SIZE(DownloadQueueProvider.COLUMN_DOWNLOAD_TOTAL_SIZE),
        /** Creation Time Stamp. */
        COL_CREATION_TIME_STAMP(DownloadQueueProvider.COLUMN_DOWNLOAD_CREATE_TIMESTAMP),
        /** Download State. */
        COL_DOWNLOAD_STATE(DownloadQueueProvider.COLUMN_DOWNLOAD_STATUS),
        /** Intent. */
        COL_INTENT_URI(DownloadQueueProvider.COLUMN_DOWNLOAD_INTENT_URI),
        /** Title. */
        COL_TITLE(DownloadQueueProvider.COLUMN_DOWNLOAD_TITLE);


        /** name of the column. */
        private final String columnName;

        /**
         * Set column name.
         * @param s the column name.
         */
        private ColumnType(final String s) {
            columnName = s;
        }

        /** Array of column names for the db query. */
        private static final String[] COLUMNS;
        static {
            COLUMNS = new String[ColumnType.values().length];
            for (ColumnType columnType : ColumnType.values()) {
                COLUMNS[columnType.ordinal()] = columnType.columnName;
            }
        };

        /**
         * @return column names.
         */
        public static String[] getColumnNames() {
            return COLUMNS;
        }
    }

    private HttpDownloadObserver(final Context context, final long downloadId, final Cursor row) {
        this.context = context.getApplicationContext();
        id = downloadId;
        setFromCursor(row);
        downloadListener = null;
    }

    public void setDownloadListener(final HttpDownloadListener listener) {
        if (listener == null) {
            if (downloadListener != null) {
                cleanDownloadListener();
            }
            return;
        }
        HttpDownloadUtility.getInstance(context)
            .addDownloadListener(id, listener);
        downloadListener = listener;
    }

    public void cleanDownloadListener() {
        if (downloadListener != null) {
            HttpDownloadUtility.getInstance(context)
                .removeDownloadListener(id, downloadListener);
            downloadListener = null;
        }
    }

    public void setFromCursor(final Cursor row) {
        downloadUrl = row.getString(ColumnType.COL_URL.ordinal());
        fileLocation = row.getString(ColumnType.COL_FILE_LOCATION.ordinal());
        bytesTransferred = row.getLong(ColumnType.COL_BYTES_TRANSFERRED.ordinal());
        totalBytes = row.getLong(ColumnType.COL_FILE_SIZE.ordinal());
        downloadState = DownloadState.valueOf(row.getString(ColumnType.COL_DOWNLOAD_STATE.ordinal()));
        mimeType = row.getString(ColumnType.COL_MIME_TYPE.ordinal());
        long startTime;
        try {
            startTime = row.getLong(ColumnType.COL_CREATION_TIME_STAMP.ordinal());
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to parse download start time.", e);
            // mark user flags invalid (all false)
            startTime = 0L;
        }
        creationTimestamp = startTime;
        title = row.getString(ColumnType.COL_TITLE.ordinal());
    }

    private static Cursor queryForDownloadById(final Context context, final long id) {
        // run the database query
        final Cursor row = QueryHelper.runQueryForDownloadId(Long.toString(id),
            context, ColumnType.getColumnNames());
        return row;
    }

    /* package */ static HttpDownloadObserver getDownloadById(final Context context, final long id) {
        final Cursor row = queryForDownloadById(context, id);
        if (row == null) {
            return null;
        }
        try {
            return new HttpDownloadObserver(context, id, row);
        } finally {
            row.close();
        }
    }

    /* package */ static List<HttpDownloadObserver> getAllDownloadsByDownloadState(
                            final Context context, final DownloadState... downloadStates) {
        // build up the request and query for all downloads with in the specified states.
        return null;

    }

    /* package */ static List<HttpDownloadObserver> getAllDownloads(final Context context) {
        final ContentResolver resolver = context.getContentResolver();

        List<HttpDownloadObserver> downloadObservers = new ArrayList<>();

        final Cursor row = resolver.query(
            DownloadQueueProvider.getDownloadContentUri(context),
            ColumnType.getColumnNames(),
            null,
            null,
            null
        );

        if (row != null) {
            try {
                row.moveToFirst();
                while (!row.isAfterLast()) {
                    final long id = row.getLong(ColumnType.COL_ID.ordinal());
                    downloadObservers.add(new HttpDownloadObserver(context, id, row));
                    row.moveToNext();
                }
            } finally {
                row.close();
            }
        }

        return downloadObservers;
    }

    /**
     * Refresh fields from the database.
     */
    public boolean refresh() {
        final Cursor row;
        if (id != DownloadService.INVALID_ID) {
            row = queryForDownloadById(context, id);
            if (row == null) {
                return false;
            }
        } else {
            // query for download by file location.
            final String selectionCause = DownloadQueueProvider.COLUMN_DOWNLOAD_FILE_LOCATION + " = ?";
            final String[] selectionArgs = {fileLocation};
            row = QueryHelper.runQueryForDownloadRow(selectionCause, selectionArgs,
                context, ColumnType.getColumnNames());
            if (row == null) {
                return false;
            }
        }
        try {
            setFromCursor(row);
        } finally {
            row.close();
        }
        return true;
    }

    /**
     * @return The download id.
     */
    public long getId() {
        return id;
    }

    /**
     * @return The total bytes of the file being download.
     */
    public long getBytesTotal() {
        return totalBytes;
    }

    /**
     * @return The absolute path of the file transferred.
     */
    public String getAbsoluteFilePath() {
        return fileLocation;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    /**
     * @return The number of bytes currently downloaded.
     */
    public long getBytesTransferred() {
        return bytesTransferred;
    }

    /**
     * @return The state of the download.
     */
    public DownloadState getState() {
        return downloadState;
    }

    /**
     * @return The mime type of the download.
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @return The creation time stamp for download.
     */
    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    /**
     * @return The title of the download.
     */
    public String getTitle() {
        return title;
    }
}
