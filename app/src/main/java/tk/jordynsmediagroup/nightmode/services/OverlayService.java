package tk.jordynsmediagroup.nightmode.services;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import tk.jordynsmediagroup.nightmode.C;
import tk.jordynsmediagroup.nightmode.activity.MainActivity;
import tk.jordynsmediagroup.nightmode.R;
import tk.jordynsmediagroup.nightmode.utils.Settings;
import tk.jordynsmediagroup.nightmode.utils.Utility;

public class OverlayService extends Service {
    // Variables here
    private MaskBinder mBinder = new MaskBinder(); // Binder instance here
    private boolean isShowing = false; // Overlay is not enabled
    private WindowManager mWindowManager; // Window manager instance
    private NotificationManager mNotificationManager; // Notification instance
    private Notification mNoti;
    private Settings mSettings; // Settings instance
    private boolean enableOverlaySystem; // Enable overlay or not?
    private boolean enableOverlayStatusBar; // Enable status bar overlay or not?
    private LinearLayout mLayout; // Layout
    private WindowManager.LayoutParams mLayoutParams; // Layout parameters
    private static final int NOTIFICATION_NO = 1024;

    private void createNotification() {
        // Create notification here
        NotificationCompat.Builder builder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notifcation)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                                R.mipmap.ic_launcher))
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.noti_text));
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        builder.setOngoing(true); // Mark notification as "ongoing"
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    private void showNotification() {
        if (mNoti == null) {
            createNotification(); // Create notification if not already made
        }
        mNotificationManager.notify(NOTIFICATION_NO, mNoti);
    }

    private void cancelNotification() {
        try {
            mNotificationManager.cancel(0);
        } catch (Exception e) {
            e.printStackTrace(); // Print stacktrace upon Exception
            onDestroy(); // Call onDestroy
        }
    }
    public int onStartCommand(Intent intent, int flags, int arg) {
        if (intent != null) {
            String action = intent.getStringExtra(C.EXTRA_ACTION);
            float targetAlpha = (100 - intent.getIntExtra(C.EXTRA_BRIGHTNESS, 0)) * 0.01f;
            boolean temp = intent.getBooleanExtra(C.EXTRA_USE_OVERLAY_SYSTEM, false);
            switch (action) {
                case C.ACTION_START:
                    createNotification();
                        if (temp != enableOverlaySystem) {
                            mLayoutParams.type = !enableOverlaySystem ? WindowManager.LayoutParams.TYPE_TOAST : WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
                            enableOverlaySystem = temp;
                        }

                    isShowing = true;
                    mSettings.putBoolean(Settings.KEY_ALIVE, true);
                    mWindowManager.updateViewLayout(mLayout, mLayoutParams);
                    break;
                case C.ACTION_STOP:
                    cancelNotification();
                    isShowing = false;
                    this.onDestroy();
                    break;
                case C.ACTION_UPDATE:
                    isShowing = true;
                    if (temp != enableOverlaySystem) {
                        mLayoutParams.type = !enableOverlaySystem ? WindowManager.LayoutParams.TYPE_TOAST : WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
                        enableOverlaySystem = temp;
                        try {
                            mWindowManager.updateViewLayout(mLayout, mLayoutParams);
                        } catch (Exception e) {
                            // do nothing....
                        }
                    }

                    mSettings.putBoolean(Settings.KEY_ALIVE, true);
                    mLayout.setAlpha(targetAlpha);
                    break;
                case C.ACTION_CHECK:
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(MainActivity.class.getCanonicalName());
                    broadcastIntent.putExtra(C.EXTRA_EVENT_ID, C.EVENT_CHECK);
                    broadcastIntent.putExtra("isShowing", isShowing);
                    sendBroadcast(broadcastIntent);
                    break;
            }
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        mNotificationManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);

        mSettings = Settings.getInstance(getApplicationContext());
        enableOverlaySystem = mSettings.getBoolean(Settings.KEY_OVERLAY_SYSTEM, false);
        createOverlayView();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        this.registerReceiver(mBroadcastReceiver, filter);
    }
    /* On Binder bind */
    @Override
    public IBinder onBind(Intent intent) { return mBinder; }

    /* On Binder unbind */
    @Override
    public boolean onUnbind(Intent intent) { return super.onUnbind(intent); }

    /* Binder class */
    public class MaskBinder extends Binder {
        public boolean isMaskShowing() { return isShowing; }
    }

    /* Create Overlay Here */
    private void createOverlayView() {
        mLayoutParams = new WindowManager.LayoutParams();
        // These flags get set regardless of the settings.
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE  | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        // Set the type as either a toast or a system overlay depending on the settings
        mLayoutParams.type = !enableOverlaySystem ? WindowManager.LayoutParams.TYPE_TOAST : WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        mLayoutParams.format = PixelFormat.TRANSPARENT;
        int maxSize = Math.max(Utility.getTrueScreenHeight(getApplicationContext()), Utility.getTrueScreenWidth(getApplicationContext()));
        mLayoutParams.height = maxSize + 200;
        mLayoutParams.width = maxSize + 200;
        mLayoutParams.gravity = Gravity.CENTER;

        if (mLayout == null) {
            mLayout = new LinearLayout(this);
            mLayout.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    )
            );
            mLayout.setBackgroundColor(Color.parseColor(mSettings.getString(Settings.KEY_COLOR, "#000000")));
            mLayout.setAlpha(0f);
        }

        try {
            mWindowManager.addView(mLayout, mLayoutParams);
        } catch (Exception e) {
            e.printStackTrace();
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(MainActivity.class.getCanonicalName());
            broadcastIntent.putExtra(C.EXTRA_EVENT_ID, C.EVENT_CANNOT_START);
            sendBroadcast(broadcastIntent);
        }
    }

    /* Restart the overlay when the display is rotated */
    public BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent myIntent) {
            if ( myIntent.getAction() == "android.intent.action.CONFIGURATION_CHANGED" ) {
                if ( isShowing ) {
                    isShowing = false;
                    mWindowManager.removeViewImmediate(mLayout);
                    createOverlayView();
                }
            }
        }
    };

    /* Destroy Service */
    @Override
    public void onDestroy() {
        cancelNotification();
        super.onDestroy();
        mSettings.putBoolean(Settings.KEY_ALIVE, false);
        isShowing = false;
        mWindowManager.removeViewImmediate(mLayout);
        // Unregister Rotation receiver to avoid memory leaks
        this.unregisterReceiver(mBroadcastReceiver);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainActivity.class.getCanonicalName());
        broadcastIntent.putExtra(C.EXTRA_EVENT_ID, C.EVENT_DESTORY_SERVICE);
        sendBroadcast(broadcastIntent);

    }
}
