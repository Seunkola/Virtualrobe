package com.amazonaws.mobile.downloader.query;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import java.net.URISyntaxException;

/**
 * Helper Class for retrieving the data needed by BasicDownloader from the download queue database.
 */
public final class BasicDownloadInfo {
    /** Log tag. */
    private static final String LOG_TAG = BasicDownloadInfo.class.getSimpleName();

    /** Custom extras. */
    private final Intent intent;

    /** download url. */
    private final String downloadUrl;

    /** destination file location. */
    private final String destinationFileUri;

    /** mime type. */
    private final String mimeType;

    /** download size. */
    private final long downloadSize;

    /** creation time stamp. */
    private final long creationTimestamp;

    /***
     * This enum of column indexes of the fields of the request to the data source.
     */
    private enum ColumnType {
        /** Intent. */
        COL_INTENT_URI(DownloadQueueProvider.COLUMN_DOWNLOAD_INTENT_URI),
        /** Download url. */
        COL_URL(DownloadQueueProvider.COLUMN_DOWNLOAD_URL),
        /** File location. */
        COL_FILE_LOCATION(DownloadQueueProvider.COLUMN_DOWNLOAD_FILE_LOCATION),
        /**  Mime type. */
        COL_MIME_TYPE(DownloadQueueProvider.COLUMN_DOWNLOAD_MIME_TYPE),
        /** Total file size. */
        COL_FILE_SIZE(DownloadQueueProvider.COLUMN_DOWNLOAD_TOTAL_SIZE),
        /** Creation Time Stamp. */
        COL_CREATION_TIME_STAMP(DownloadQueueProvider.COLUMN_DOWNLOAD_CREATE_TIMESTAMP);

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

    /**
     * Constructs download info from Cursor row in the database for the specified localDownloadId.
     * @param context the context.
     * @param localDownloadId ID of download.
     * @param row Row containing download info for this download Id
     */
    private BasicDownloadInfo(final Context context, final String localDownloadId, final Cursor row) {
        final String intentURI = row.getString(ColumnType.COL_INTENT_URI.ordinal());
        Intent deserialzedIntent;
        try {
            deserialzedIntent = Intent.parseUri(intentURI, 0);
        } catch (URISyntaxException ex) {
            Log.e(LOG_TAG, String.format(
                "Could not deserialize intent for download with localDownloadId = %s. Using new intent.",
                localDownloadId), ex);
            deserialzedIntent = new Intent();
        }
        this.intent = deserialzedIntent;


        downloadUrl = row.getString(ColumnType.COL_URL.ordinal());
        destinationFileUri = row.getString(ColumnType.COL_FILE_LOCATION.ordinal());
        mimeType = row.getString(ColumnType.COL_MIME_TYPE.ordinal());
        long fileSize;
        try {
            fileSize = row.getLong(ColumnType.COL_FILE_SIZE.ordinal());
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to parse file size.", e);
            // mark user flags invalid (all false)
            fileSize = 0L;
        }
        downloadSize = fileSize;
        long startTime;
        try {
            startTime = row.getLong(ColumnType.COL_CREATION_TIME_STAMP.ordinal());
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to parse download start time.", e);
            // mark user flags invalid (all false)
            startTime = 0L;
        }
        creationTimestamp = startTime;
    }

    /**
     * Queries the database and retrieves the download info for the localDownloadId.
     * @param context Context for the Download Queue Provider.
     * @param localDownloadId ID of download for looking up in the db.
     * @return new AmazonDownloadInfo or null if the query fails.
     */
    public static BasicDownloadInfo getNewDownloadInfo(final Context context, final String localDownloadId) {
        final BasicDownloadInfo downloadInfo;
        final Cursor row = QueryHelper.runQueryForDownloadId(localDownloadId, context, ColumnType.getColumnNames());
        if (row == null) {
            return null;
        }
        try {
            downloadInfo = new BasicDownloadInfo(context, localDownloadId, row);
        } finally {
            row.close();
        }
        return downloadInfo;
    }

    /**
     * Get intent retrieved from the db.
     * @return the intent
     */
    public Intent getIntent() {
        return intent;
    }

    /**
     * @return the download url.
     */
    public String getDownloadUrl() {
        return downloadUrl;
    }

    /**
     * @return the destination file location.
     */
    public String getDestinationFileUri() {
        return destinationFileUri;
    }

    /**
     * @return the mime type.
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @return the download size.
     */
    public long getDownloadSize() {
        return downloadSize;
    }

    /**
     * @return the creation time stamp.
     */
    public long getCreationTimestamp() {
        return creationTimestamp;
    }
}
