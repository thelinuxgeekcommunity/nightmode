package tk.jordynsmediagroup.nightmode.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import tk.jordynsmediagroup.nightmode.C;
import tk.jordynsmediagroup.nightmode.services.OverlayService;
import tk.jordynsmediagroup.nightmode.utils.Settings;

public class TileReceiver extends BroadcastReceiver {

    public static final String ACTION_UPDATE_STATUS = "tk.jordynsmediagroup.nightmode.ACTION_UPDATE_STATUS";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_UPDATE_STATUS.equals(intent.getAction())) {
            switch (intent.getStringExtra(C.EXTRA_ACTION)) {
                case C.ACTION_START:
                    Intent intent1 = new Intent(context, OverlayService.class);
                    intent1.putExtra(C.EXTRA_ACTION, C.ACTION_START);
                    intent1.putExtra(C.EXTRA_BRIGHTNESS, Settings.getInstance(context).getInt(Settings.KEY_BRIGHTNESS, 50));
                    context.startService(intent1);
                    break;
                case C.ACTION_STOP:
                    Intent intent2 = new Intent(context, OverlayService.class);
                    intent2.putExtra(C.EXTRA_ACTION, C.ACTION_STOP);
                    context.stopService(intent2);
                    break;
            }
        }
    }

}
