package com.amazonaws.mobile.downloader.query;

import android.content.Context;
import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;

/**
 * This class has a utility methods for querying a row.
 */
public final class QueryHelper {

    /** Log tag. */
    private static final String LOG_TAG = QueryHelper.class.getSimpleName();

    /**
     * Run a query for a single row from the download queue.
     * Log a warning if multiple rows match, but position the cursor at the last row.
     * @param query the query to run.
     * @param queryArgs the paramaters for the query.
     * @param context the context.
     * @param columnNames the columns to retrieve.
     * @return open Cursor which must be closed by caller, or null on error.
     */
    public static Cursor runQueryForDownloadRow(final String query, final String[] queryArgs, final Context context,
                                                final String[] columnNames) {
        final ContentResolver resolver = context.getContentResolver();

        final Cursor rows = resolver.query(
                DownloadQueueProvider.getDownloadContentUri(context),
                columnNames,
                query,
                queryArgs,
                null
        );

        if (rows == null) {
            return null;
        }

        try {
            final int rowCount = rows.getCount();
            if (rowCount > 1) {
                Log.w(LOG_TAG, "Query for [" + query + ", " + queryArgs[0] + "] returned "
                    + rowCount + "rows, when only a single row was expected.");
            }
            if (rowCount == 0 || !rows.moveToLast()) {
                rows.close();
                return null;
            }
        } catch (RuntimeException ex) {
            rows.close();
            throw ex;
        }

        return rows;
    }

    /**
     * Run a query for a row from the download queue.
     * @param downloadId DownloadId to query for.
     * @param context the context.
     * @param columnNames the columns to retrieve.
     * @return open Cursor which must be closed by caller, or null on error.
     */
    public static Cursor runQueryForDownloadId(final String downloadId, final Context context,
                                               final String[] columnNames) {
        final String selectionCause = DownloadQueueProvider.COLUMN_DOWNLOAD_ID + " = ?";
        final String[] selectionArgs = {downloadId};
        return runQueryForDownloadRow(selectionCause, selectionArgs, context, columnNames);
    }

    /**
     * Run a database query against the download queue and return a String array of column values.
     * A warning is logged if multiple rows match, but the last row is returned.
     * @param context the context to use
     * @param projection the desired columns
     * @param query the query to run
     * @param params the parameters for the query
     * @return an array of strings representing the column values.
     */
    public static String[] runDownloadQueryForRow(final Context context, final String[] projection,
                                                  final String query, final String[] params) {
        String[] result = null;
        final Cursor row = runQueryForDownloadRow(query, params, context, projection);
        if (row == null) {
            return null;
        }

        try {
            result = new String[projection.length];
            for (int i = 0; i < projection.length; ++i) {
                result[i] = row.getString(i);
            }
        } finally {
            row.close();
        }
        return result;
    }

    /**
     * Construct a new instance.
     */
    private QueryHelper() {
    }
}
