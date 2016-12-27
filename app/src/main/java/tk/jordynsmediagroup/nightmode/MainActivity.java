package tk.jordynsmediagroup.nightmode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import tk.jordynsmediagroup.nightmode.activity.AboutActivity;
import tk.jordynsmediagroup.nightmode.receiver.TileReceiver;
import tk.jordynsmediagroup.nightmode.services.OverlayService;
import tk.jordynsmediagroup.nightmode.utils.Settings;

public class MainActivity extends AppCompatActivity {
    private SeekBar brightness = null;     // Initialize the brightness value here
    private Settings mSettings; // Settings instance
    private MessageReceiver mReceiver; // Message receiver here
    private boolean isRunning = false; // Turn off overlay here
    private static final int OVERLAY_PERMISSION_REQ_CODE = 1001; // Permission code for total screen overlay
    private Menu mMenu; // Menu instance

    /* On create */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSettings = Settings.getInstance(getApplicationContext()); // Settings instance for onCreate

        /* Standard onCreate code here */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Start Overlay service here */
        Intent i = new Intent(this, OverlayService.class);
        i.putExtra(C.EXTRA_ACTION, C.ACTION_CHECK);
        startService(i);

        /* Setup the toggle button */
        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        toggle.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Tell the Overlay Service to enable the overlay
                    Intent intent = new Intent();
                    intent.setAction(TileReceiver.ACTION_UPDATE_STATUS);
                    intent.putExtra(C.EXTRA_ACTION, C.ACTION_START);
                    sendBroadcast(intent);
                    isRunning = true;
                    // Set the Overlay brightness from the SeekBar
                    Intent intent2 = new Intent(MainActivity.this, OverlayService.class);
                    intent2.putExtra(C.EXTRA_ACTION, C.ACTION_UPDATE);
                    intent2.putExtra(C.EXTRA_BRIGHTNESS, brightness.getProgress());
                    startService(intent2);
                } else {
                    // Tell the Overlay Service to disable the overlay
                    Intent intent = new Intent(MainActivity.this, OverlayService.class);
                    intent.putExtra(C.EXTRA_ACTION, C.ACTION_STOP);
                    stopService(intent);
                    isRunning = false;
                }
            }
        });

        final TextView textView = (TextView) findViewById(R.id.textView2); // Find the TextView here
        brightness = (SeekBar) findViewById(R.id.seekBar); // Find the SeekBar here
        brightness.setProgress(mSettings.getInt(Settings.KEY_BRIGHTNESS, 50)); // Set the SeekBar brightness here
        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        int v = -1; // Initialize the variable 'v' here

            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                v = value; // Set the brightness value
                textView.setText(getString(R.string.brightness) + " " + v); // Set the brightness text
                if (isRunning) {
                    // If the Overlay Service is running, then notify it of the brightness change here
                    Intent intent = new Intent(MainActivity.this, OverlayService.class);
                    intent.putExtra(C.EXTRA_ACTION, C.ACTION_UPDATE);
                    intent.putExtra(C.EXTRA_BRIGHTNESS, brightness.getProgress());
                    startService(intent);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No implantation :)
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (v != -1) {
                    // When the user stops dragging the brightness bar, set the brightness.
                    mSettings.putInt(Settings.KEY_BRIGHTNESS, v);
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart(); // Call onStart on the superclass
        if (mReceiver == null) { mReceiver = new MessageReceiver(); } // Start a new message receiver, if not already started.
        IntentFilter filter = new IntentFilter(); // Register it with the IntentFilter
        filter.addAction(MainActivity.class.getCanonicalName());
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onResume() {
        super.onResume();
        final TextView textView = (TextView) findViewById(R.id.textView2); // Find the TextView here
        textView.setText(getString(R.string.brightness) + " " + mSettings.getInt(Settings.KEY_BRIGHTNESS, 50)); // Set the brightness text
    }
    private class MessageReceiver extends BroadcastReceiver {
        /* Setup the toggle button */
        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);

        @Override
        public void onReceive(Context context, Intent intent) {
            int eventId = intent.getIntExtra(C.EXTRA_EVENT_ID, -1);
            switch (eventId) {
                case C.EVENT_CANNOT_START:
                    // Receive a error from OverlayService
                    mSettings.putBoolean(Settings.KEY_ALIVE, false);
                    isRunning = false;
                    toggle.toggle();
                    finish();
                    break;
                case C.EVENT_DESTORY_SERVICE:
                    if (isRunning) {
                        // Destroy OverlayService here
                        mSettings.putBoolean(Settings.KEY_ALIVE, false);
                        toggle.toggle();
                        isRunning = false;
                    }
                    break;
                case C.EVENT_CHECK:
                    // Check the OverlayService here
                    isRunning = intent.getBooleanExtra("isShowing", false);
                    if (isRunning) {
                        // If I don't use postDelayed, Switch may cause a NPE because its animator wasn't initialized.
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                toggle.toggle();
                            }
                        }, 100);
                    }
                    break;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause(); // Call onPause from the superclass
        mSettings.putInt(Settings.KEY_BRIGHTNESS, brightness.getProgress()); // Save the brightness
    }

    @Override
    public void onStop() {
        mSettings.putInt(Settings.KEY_BRIGHTNESS, brightness.getProgress()); // Save the brightness
        super.onStop(); // Call onStop from the superclass
        unregisterReceiver(mReceiver); // Unregister mReceiver from the IntentFilter
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the Action Bar menu here
        getMenuInflater().inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.action_about) {
            // Start the AboutActivity here
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        } else if (id == R.id.action_overlay_system) {
            // Enable System Overlay here
            if ( menuItem.isChecked() ) {
                mSettings.putBoolean(Settings.KEY_OVERLAY_SYSTEM, false);
                menuItem.setChecked(false);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!android.provider.Settings.canDrawOverlays(this)) {
                        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
                    }
                } else {
                    mSettings.putBoolean(Settings.KEY_OVERLAY_SYSTEM, true);
                    menuItem.setChecked(true);
                }
            }
            return true;
        }
        return false;
    }
}
