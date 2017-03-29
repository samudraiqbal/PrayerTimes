package id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import java.util.Locale;

import id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes.R;

public class Localization {
    private Context activity;

    public Localization(Context activity) {
        this.activity = activity;
    }

    public void InitLocale() {
        SharedPreferences sharePrefs = PreferenceManager.getDefaultSharedPreferences(this.activity);
        String lang = sharePrefs.getString("language", activity.getString(R.string.locale));
        ChangeLocale(new Locale(lang));
    }

    public void ChangeLocale(Locale locale) {
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        activity.getResources().updateConfiguration(config, activity.getResources().getDisplayMetrics());
    }
}
