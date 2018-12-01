package com.amazonaws.mobile.downloader.query;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import android.util.Log;

/**
 * A class which implements a content provider that gives access to
 * the download queue. Note that we only support querying. Updates,
 * deletes, inserts must all happen by firing intents at the download
 * service.
 */
public class DownloadQueueProvider extends ContentProvider {
    /** The authority for download content provider. */
    private static String authority = null;

    /**
     * Gets the authority for the download queue content provider.
     * @param context a context
     * @return the authority for the download queue content provider.
     */
    public static synchronized String getAuthority(final Context context) {
        if (authority == null) {
            authority = "com.amazonaws.mobile.downloader-" + context.getPackageName();
        }
        return authority;
    }

    /** The base path for download content provider's content Uri. */
    public static final String BASE_PATH = "Downloads";

    public static final String OPEN_TRANSACTION_PATH = "Downloads/StartTransaction";
    public static final String COMPLETE_TRANSACTION_PATH = "Downloads/CompleteTransaction";

    /** The download content Uri. */
    private static volatile Uri downloadContentUri = null;
    private static volatile Uri downloadOpenTransactionUri = null;
    private static volatile Uri downloadCompleteTransactionUri = null;

    /**
     * Gets the uri for the download queue content provider.
     * @param context a context
     * @return the uri for the download queue content provider.
     */
    public static synchronized Uri getDownloadContentUri(final Context context) {
        if (downloadContentUri == null) {
            downloadContentUri = Uri.parse("content://" + getAuthority(context) + "/" + BASE_PATH);
        }
        return downloadContentUri;
    }

    public static synchronized Uri getDownloadOpenTransactionUri(final Context context) {
        if (downloadOpenTransactionUri == null) {
            downloadOpenTransactionUri = Uri.parse("content://" + getAuthority(context) + "/" + OPEN_TRANSACTION_PATH);
        }
        return downloadOpenTransactionUri;
    }

    public static synchronized Uri getDownloadCompleteTransactionUri(final Context context) {
        if (downloadCompleteTransactionUri == null) {
            downloadCompleteTransactionUri = Uri.parse("content://" + getAuthority(context) + "/" + COMPLETE_TRANSACTION_PATH);
        }
        return downloadCompleteTransactionUri;
    }

    /** Our MIME type for single rows. */
    public static final String ROW_MIME_TYPE =
        "vnd.android.cursor.item/vnd.com.amazonaws.mobile.downloader." + BASE_PATH;

    /** Our MIME type for sets of rows. */
    public static final String TABLE_MIME_TYPE =
        "vnd.android.cursor.dir/vnd.com.amazonaws.mobile.downloader." + BASE_PATH;

    /** The table name. */
    public static final String TABLE_NAME = "download_queue";

    /** The column containing download id. */
    public static final String COLUMN_DOWNLOAD_ID = "_id";

    /** The column containing the download file location. */
    public static final String COLUMN_DOWNLOAD_FILE_LOCATION = "fileLocation";

    /** The column containing the download description. */
    public static final String COLUMN_DOWNLOAD_DESCRIPTION = "description";

    /** The column containing the download Uri. */
    public static final String COLUMN_DOWNLOAD_URL = "url";

    /** The column containing the user interaction flags. */
    public static final String COLUMN_DOWNLOAD_USER_FLAGS = "userFlags";

    /** The column containing the MIME type. */
    public static final String COLUMN_DOWNLOAD_MIME_TYPE = "mimeType";

    /** The column containing the serialized intent passed in. */
    public static final String COLUMN_DOWNLOAD_INTENT_URI = "intentURI";

    /** The column containing the eTag. */
    public static final String COLUMN_DOWNLOAD_ETAG = "eTag";

    /** The column containing the status enum. */
    public static final String COLUMN_DOWNLOAD_STATUS = "status";

    /** The column containing the size of the thing being downloaded. */
    public static final String COLUMN_DOWNLOAD_TOTAL_SIZE = "size";

    /** The column containing the amount downloaded so far. */
    public static final String COLUMN_DOWNLOAD_CURRENT_SIZE = "bytesDownloaded";

    /** The column containing the pause/fail reason. */
    public static final String COLUMN_DOWNLOAD_STOP_REASON = "stoppedBecause";

    /** The column containing the timestamp of when the status was last modified (in milliseconds). */
    public static final String COLUMN_DOWNLOAD_CREATE_TIMESTAMP = "createTimestamp";

    /** The column containing the download title. */
    public static final String COLUMN_DOWNLOAD_TITLE = "title";

    /** An instance of our helper class used to access the DB. */
    private DatabaseHelper helper;

    /**
     * Called when a the content provider is created by the Android system.
     *
     * @return true if initialization successful
     */
    @Override
    public boolean onCreate() {
        helper = new DatabaseHelper(getContext());
        return true;
    }

    /**
     * Called to make a query to the content provider.
     *
     * @param uri
     *            the URI to query
     * @param projection
     *            the columns desired in the output
     * @param selection
     *            the "where" clause to use
     * @param selectionArgs
     *            variables to insert into the ? in the "where" clause
     * @param sortOrder
     *            the "order by" clause
     * @return the cursor with the results in it
     */
    @Override
    public Cursor query(final Uri uri, final String[] projection,
                        final String selection, final String[] selectionArgs, final String sortOrder) {
        if (uri.equals(downloadContentUri)) {
            final SQLiteDatabase db = helper.getReadableDatabase();
            return db.query(TABLE_NAME, projection, selection, selectionArgs,
                null, null, sortOrder, null);
        }
        final SQLiteDatabase db = helper.getWritableDatabase();

        if (uri.equals(downloadOpenTransactionUri)) {
            db.beginTransaction();
        } else if (uri.equals(downloadCompleteTransactionUri)) {
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        return null;
    }

    /**
     * Called to insert an item.
     *
     * @param uri
     *            the URI to insert to
     * @param values
     *            the values to insert.
     * @return the URI for the new row
     */
    @Override
    public Uri insert(final Uri uri, final ContentValues values) {
        final SQLiteDatabase db = helper.getWritableDatabase();
        final long id = db.insert(TABLE_NAME, null, values);
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Called to update an item.
     *
     * @param uri
     *            the URI to update
     * @param values
     *            the values to change
     * @param selection
     *            the "where" clause
     * @param selectionArgs
     *            the variables to be inserted into the ? of the "where" clause
     * @return the count of rows changed
     */
    @Override
    public int update(final Uri uri, final ContentValues values,
                      final String selection, final String[] selectionArgs) {
        final SQLiteDatabase db = helper.getWritableDatabase();
        return db.update(TABLE_NAME, values, selection, selectionArgs);
    }

    /**
     * Delete an item.
     *
     * @param uri
     *            the URI to update
     * @param selection
     *            the "where" clause
     * @param selectionArgs
     *            the variables to be inserted into the ? of the "where" clause
     * @return the count of rows deleted.
     */
    @Override
    public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
        final SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(TABLE_NAME, selection, selectionArgs);
    }

    /**
     * Get the mime type of this content.
     *
     * @param uri
     *            the URI to query
     * @return the mime type
     */
    @Override
    public String getType(final Uri uri) {
        final String result;
        final String query = uri.getQuery();
        if (query.startsWith("id=")) {
            result = ROW_MIME_TYPE;
        } else {
            result = TABLE_MIME_TYPE;
        }
        return result;
    }

    /**
     * A class to help us manage our SQLite database.
     */
    /* package */static final class DatabaseHelper extends SQLiteOpenHelper {

        /** Log Tag. */
        private static final String LOG_TAG = DatabaseHelper.class.getSimpleName();

        /** Current database version. */
        private static final int VERSION = 1;

        /** Database name. */
        private static final String NAME = "downloadQueue";

        /** SQL to create table. */
        public static final String CREATE_TABLE = "CREATE TABLE "
            + DownloadQueueProvider.TABLE_NAME
            + "("
            + DownloadQueueProvider.COLUMN_DOWNLOAD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DownloadQueueProvider.COLUMN_DOWNLOAD_URL + " TEXT, "
            + DownloadQueueProvider.COLUMN_DOWNLOAD_FILE_LOCATION + " TEXT, "
            + DownloadQueueProvider.COLUMN_DOWNLOAD_DESCRIPTION + " TEXT, "
            + DownloadQueueProvider.COLUMN_DOWNLOAD_USER_FLAGS + " INTEGER, "
            + DownloadQueueProvider.COLUMN_DOWNLOAD_MIME_TYPE + " TEXT, "
            + DownloadQueueProvider.COLUMN_DOWNLOAD_INTENT_URI + " TEXT, "
            + DownloadQueueProvider.COLUMN_DOWNLOAD_ETAG + " TEXT, "
            + DownloadQueueProvider.COLUMN_DOWNLOAD_STATUS + " TEXT, "
            + DownloadQueueProvider.COLUMN_DOWNLOAD_TOTAL_SIZE + " INTEGER, "
            + DownloadQueueProvider.COLUMN_DOWNLOAD_CURRENT_SIZE + " INTEGER, "
            + DownloadQueueProvider.COLUMN_DOWNLOAD_STOP_REASON + " TEXT, "
            + DownloadQueueProvider.COLUMN_DOWNLOAD_CREATE_TIMESTAMP + " INTEGER, "
            + DownloadQueueProvider.COLUMN_DOWNLOAD_TITLE + " TEXT "
            + ")";

        /**
         * Create a new instance.
         *
         * @param context
         *            the Context to be used.
         */
        /* package */DatabaseHelper(final Context context) {
            super(context, NAME, null, VERSION);
        }

        /**
         * Create a database schema.
         *
         * @param db
         *            the database in which to create the schema.
         */
        @Override
        public void onCreate(final SQLiteDatabase db) {
            try {
                db.execSQL(CREATE_TABLE);
            } catch (SQLiteException e) {
                Log.e(LOG_TAG, "Error trying to create table", e);
            }
        }

        /**
         * Upgrade a database schema.
         *
         * @param db
         *            the database to upgrade.
         * @param oldVersion
         *            the current version
         * @param newVersion
         *            the version to upgrade to.
         */
        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            Log.i(LOG_TAG, "upgrading db from v" + oldVersion + " to v" + newVersion);

            // No versions yet except version 1.
        }
    }
}
