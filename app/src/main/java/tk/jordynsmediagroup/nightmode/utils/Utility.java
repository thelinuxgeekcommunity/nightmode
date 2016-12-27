package tk.jordynsmediagroup.nightmode.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;

import tk.jordynsmediagroup.nightmode.C;
import tk.jordynsmediagroup.nightmode.R;
import tk.jordynsmediagroup.nightmode.receiver.TileReceiver;
import tk.jordynsmediagroup.nightmode.services.OverlayService;

public class Utility {

    public static final int CM_TILE_CODE = 1001;

    public static int getTrueScreenHeight(Context context) {
        int dpi = 0;
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealMetrics(dm);
            dpi = dm.heightPixels;
        } else {
            try {
                Class c = Class.forName("android.view.Display");
                Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
                method.invoke(display, dm);
                dpi = dm.heightPixels;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return dpi;
    }

    public static int getTrueScreenWidth(Context context) {
        int dpi = 0;
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealMetrics(dm);
            dpi = dm.widthPixels;
        } else {
            try {
                Class c = Class.forName("android.view.Display");
                Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
                method.invoke(display, dm);
                dpi = dm.widthPixels;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return dpi;
    }

}
