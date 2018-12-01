package com.virtualrobe.virtualrobe.virtualrobe_app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";
    private static final String CHANNEL_ID_WELCOME = "welcome_new_user";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {

            /*Pop up notifaction from virtualrobe to users on new features*/
            if (remoteMessage.getData().get("message")!=null) {
                //message will contain the Push Message
                String message = remoteMessage.getData().get("message");
                //imageUri will contain URL of the image to be displayed with Notification
                String imageUri = remoteMessage.getData().get("image");
                String TrueOrFlase = remoteMessage.getData().get("AnotherActivity");
                String outfit = remoteMessage.getData().get("Outfit");
                String logo = remoteMessage.getData().get("logo");

                //To get a Bitmap image from the URL received
                Bitmap bitmap = getBitmapfromUrl(imageUri);
                Bitmap logo_image = getBitmapfromUrl(logo);

                sendNotification(message, bitmap, TrueOrFlase, logo_image, outfit);
            }

            /*Welcome new users*/
            if (remoteMessage.getData().get("title")!=null){
                showWelcomeNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("author"));
            }

        }
    }

    /*Welcome notification for new users*/
    private void showWelcomeNotification(String title, String author) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,CHANNEL_ID_WELCOME)
                .setContentTitle("Welcome " + title + " to Virtualrobe Community")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("Lets begin to style " + author + " outfits")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void sendNotification(String messageBody, Bitmap image, String TrueOrFlase,Bitmap logo,String outfit) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("AnotherActivity", TrueOrFlase);
        intent.putExtra("Outfit",outfit);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setLargeIcon(image)/*Notification icon image*/
                .setContentTitle(messageBody)
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(image))/*Notification with Image*/
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setSmallIcon(R.drawable.ic_stat_logo_transparent2);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notificationBuilder.setColor(getResources().getColor(R.color.second,getTheme()));
            }
            else {
                notificationBuilder.setColor(getResources().getColor(R.color.second));
            }
        }else {
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    /*
    *To get a Bitmap image from the URL received
    * */
    public Bitmap getBitmapfromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;

        }
    }


    /*Create notification channel*/
    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationManager
                mNotificationManager =
                (NotificationManager) getApplicationContext()
                        .getSystemService(Context.NOTIFICATION_SERVICE);
        // The id of the channel.
        String id = CHANNEL_ID_WELCOME;
        // The user-visible name of the channel.
        CharSequence name = "Welcome Notification";
        // The user-visible description of the channel.
        String description = "welcome notification for new users";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.setShowBadge(false);
        mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        mNotificationManager.createNotificationChannel(mChannel);
    }


}