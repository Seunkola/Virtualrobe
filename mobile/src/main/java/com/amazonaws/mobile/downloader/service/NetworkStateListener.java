package com.amazonaws.mobile.downloader.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * A class to listen for network state changes.
 */
public class NetworkStateListener extends BroadcastReceiver {
    /** Log tag. */
    private static final String LOG_TAG = NetworkStateListener.class.getSimpleName();

    /**
     * receive an intent.
     * @param context the context
     * @param intent the intent
     */
    public void onReceive(final Context context, final Intent intent) {
        Log.i(LOG_TAG, "Received network connectivity changed.");
        final ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == mgr) {
            Log.e(LOG_TAG, "Couldn't retrieve the connectivity manager to get the network state.");
            return;
        }

        final NetworkInfo active = mgr.getActiveNetworkInfo();

        final Intent newIntent = new Intent();
        newIntent.setClass(context, DownloadService.class);
        if (active != null && active.isConnected()) {
            newIntent.setAction(DownloadService.ACTION_START_UP);
            Log.i(LOG_TAG, "Network connected, send DownloadService the start-up action.");
        } else {
            newIntent.setAction(DownloadService.ACTION_NETWORK_LOST);
            newIntent.setClass(context, DownloadService.class);
            Log.i(LOG_TAG, "Network lost, send DownloadService the network lost action.");
        }
        context.startService(newIntent);
    }

    /**
     * Set the state to enabled.
     * @param context the context to use.
     */
    public static void enable(final Context context) {
        setState(context, PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
    }

    /**
     * Set the state to disabled.
     * @param context the context to use
     */
    public static void disable(final Context context) {
        setState(context, PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
    }

    /**
     * Set the component state.
     * @param context the context
     * @param state the new state.
     */
    /* package */ static void setState(final Context context, final int state) {
        ComponentName receiver = new ComponentName(context, NetworkStateListener.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver, state, PackageManager.DONT_KILL_APP);
    }
}
