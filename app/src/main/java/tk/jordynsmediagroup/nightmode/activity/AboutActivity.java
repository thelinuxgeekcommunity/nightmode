package tk.jordynsmediagroup.nightmode.activity;

import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import tk.jordynsmediagroup.nightmode.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        try
        {
            // Set the context of the textview to the app VerionName
            TextView version = (TextView)findViewById(R.id.nameView);
            String app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
            version.setText(getString(R.string.app_name) + " Version" + " " + app_ver);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            // This should never happen
            Log.e("NightMode/AboutActivity", e.getMessage());
        }
        TextView licenseDetails = (TextView)findViewById(R.id.licenceText);
        licenseDetails.setText(Html.fromHtml(getString(R.string.licence_info)));
    }
}
