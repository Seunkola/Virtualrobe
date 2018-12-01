package com.amazonaws.mobile.util;

/**
 * A helper class that handles S3 key with '/' as delimiter.
 */
public final class S3Utils {
    private static final char DELIMITER_CHARATER = '/';

    /**
     * Gets the base name of an S3 key, e.g. foo -> foo, foo/bar -> bar.
     *
     * @param key S3 key
     * @return base name of the key
     */
    public static String getBaseName(final String key) {
        int lastSlashIndex = key.lastIndexOf(DELIMITER_CHARATER);
        return lastSlashIndex == -1 ? key : key.substring(lastSlashIndex + 1);
    }

    /**
     * Gets the parent directory of the key, e.g. foo -> "", foo/bar -> foo/, foo/bar/ -> foo/.
     *
     * @param key S3 key
     * @return parent directory
     */
    public static String getParentDirectory(final String key) {
        String keyWithoutSlash = isDirectory(key) ? key.substring(0, key.length() - 1) : key;
        int lastSlashIndex = keyWithoutSlash.lastIndexOf(DELIMITER_CHARATER);
        return lastSlashIndex == -1 ? "" : keyWithoutSlash.substring(0, lastSlashIndex + 1);
    }

    /**
     * Determines whether the given S3 key is a directory, e.g. foo -> false, foo/bar/ -> true
     *
     * @param key S3 key
     * @return true if key ends with '/', false otherwise.
     */
    public static boolean isDirectory(final String key) {
        return key.charAt(key.length() - 1) == DELIMITER_CHARATER;
    }
}
