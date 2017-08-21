package vladyslavpohrebniakov.depressingthoughts.activities;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

import de.psdev.licensesdialog.LicensesDialog;
import vladyslavpohrebniakov.depressingthoughts.R;

import static android.support.v4.app.NavUtils.navigateUpFromSameTask;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    @Override
    public void onBackPressed() {
        navigateUpFromSameTask(this);
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            final String keyLicenses = getResources().getString(R.string.key_licenses_pref);

            Preference licensesPref = getPreferenceScreen().findPreference(keyLicenses);
            licensesPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new LicensesDialog.Builder(getActivity())
                            .setNotices(R.raw.notices)
                            .build()
                            .show();

                    return false;
                }
            });
        }
    }
}
