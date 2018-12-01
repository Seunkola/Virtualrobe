package com.amazonaws.mobile.downloader.service;

import android.util.Log;

/**
 * The utility class that computes user flag values.
 */
public class DownloadFlags {
    final static String LOG_TAG = DownloadFlags.class.getSimpleName();

    /** Flag to signify a request is being paused or resumed by user request. */
    public static final int FLAG_BY_USER_REQUEST = 0x1;

    /** Flag to signify a request originated from an explicit user action. */
    public static final int FLAG_FOREGROUND = 0x2;

    /** Flag to signify that no progress updates are desired. */
    public static final int FLAG_SILENT = 0x4;

    /** Flag to signify that a Wifi lock must be acquired when downloading. */
    public static final int FLAG_WIFI_LOCK = 0x8;

    /** The flag for mobile network type. */
    public static final int FLAG_MOBILE_NETWORK_PROHIBITED = 0x10;

    /**
     * Parse flags from a string field.
     * @param value flags encoded as a numerical string.
     * @return int flag value.
     */
    public static int parseUserFlags(final String value) {
        if (null != value) {
            try {
                return Integer.parseInt(value);
            } catch (final NumberFormatException ex) {
                Log.e(LOG_TAG, "Failed to parse download user flags (foreground, silent, etc.)", ex);
            }
        }
        return 0;
    }
    /**
     * Determine if a string represents an integer with the SILENT bit set.
     *
     * @param value
     *            the flags encoded as an integer.
     * @return true if the bit was set.
     */
    public static boolean isSilentFlagSet(final int value) {
        return checkArbitraryFlag(value, FLAG_SILENT);
    }

    public static boolean isUserRequestFlagSet(final int value) {
        return checkArbitraryFlag(value, FLAG_BY_USER_REQUEST);
    }
    /**
     * Determine if a string represents an integer with the FOREGROUND bit set.
     *
     * @param value
     *            the flags encoded as an integer.
     * @return true if the bit was set.
     */
    public static boolean isForegroundFlagSet(final int value) {
        return checkArbitraryFlag(value, FLAG_FOREGROUND);
    }

    /**
     * Determine if a string represents an integer with the WIFI_LOCK bit set.
     *
     * @param value
     *            the flags encoded as an integer.
     * @return true if the bit was set.
     */
    public static boolean isWifiLockFlagSet(final int value) {
        return checkArbitraryFlag(value, FLAG_WIFI_LOCK);
    }

    public static boolean isCellNetworkProhibited(final int value) {
        return checkArbitraryFlag(value, FLAG_MOBILE_NETWORK_PROHIBITED);
    }
    /**
     * Determine if a string represents an integer with a particular bit set.
     *
     * @param value
     *            the string
     * @param flag
     *            the bit to check
     * @return true if the bit was set.
     */
    public static boolean checkArbitraryFlag(final int value, final int flag) {
        return (value & flag) == flag;
    }

}
