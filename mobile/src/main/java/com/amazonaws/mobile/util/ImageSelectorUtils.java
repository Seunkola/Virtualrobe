package com.amazonaws.mobile.util;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

/**
 * A utility class that extracts file path from a Uri.
 */
@SuppressLint("NewApi")
public final class ImageSelectorUtils {

    // Uri authority of Media Provider
    private static final String MEDIA_PROVIDER_URI = "com.android.providers.media.documents";

    // Uri authority of Downloads Provider
    private static final String DOWNLOADS_PROVIDER_URI = "com.android.providers.downloads.documents";

    // Uri authority of External Storage Provider
    private static final String EXTERNAL_STORAGE_PROVIDER_URI = "com.android.externalstorage.documents";

    /**
     * Gets the file path of the given Uri.
     */
    public static String getFilePathFromUri(final Context context, final Uri uri,String uri_type) {
        Uri queryUri = uri;
        String selection = null;
        String[] selectionArgs = null;
        String path;
        // Android API level 19 (KitKat) introduces Document. The Uri has
        // a new format and needs special handling in order to extract
        // the real file path out of it.
        if (!uri_type.equals("virtualrobe_items")) {
            if (Build.VERSION.SDK_INT >= 19) {
                final String providerUri = uri.getAuthority();
                final String docId = DocumentsContract.getDocumentId(uri);
                if (EXTERNAL_STORAGE_PROVIDER_URI.equals(providerUri)) {
                    final String[] split = docId.split(":");
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                if (MEDIA_PROVIDER_URI.equals(providerUri)) {
                    final String[] split = docId.split(":");
                    selection = "_id=?";
                    selectionArgs = new String[]{split[1]};
                    queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if (DOWNLOADS_PROVIDER_URI.equals(providerUri)) {
                    queryUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                } else {
                    throw new IllegalArgumentException("Unsupported provider: " + providerUri);
                }
            }


            final String[] projection = {MediaStore.Images.Media.DATA};
            final Cursor cursor = context.getContentResolver()
                    .query(queryUri, projection, selection, selectionArgs, null);
            final int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            path = null;
            if (cursor.moveToFirst()) {
                path = cursor.getString(column_index);
            }
            cursor.close();
        }
        else {
            path = queryUri.toString();
        }

        return path;
    }

    public static Intent getImageSelectionIntent() {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= 19) {
            // For Android versions of KitKat or later, we use a
            // different intent to ensure
            // we can get the file path from the returned intent URI
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        } else {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        }
        intent.setType("image/*");
        return intent;
    }
}
