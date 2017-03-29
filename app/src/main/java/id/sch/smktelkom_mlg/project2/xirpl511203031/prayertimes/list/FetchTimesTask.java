package id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes.list;

import android.os.AsyncTask;

import java.util.Locale;

import id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes.utils.DiyanetParser;
import id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes.utils.Location;
import id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes.utils.PrayerTimes;

public class FetchTimesTask extends AsyncTask<Void, Void, PrayerTimes> {
    private Location location;
    private Locale locale;

    public FetchTimesTask(Location loc) {
        this.location = loc;
        this.locale = Locale.ENGLISH;
    }

    public FetchTimesTask(Location loc, Locale locale) {
        this.location = loc;
        this.locale = locale;
    }

    @Override
    protected PrayerTimes doInBackground(Void... params) {
        return new DiyanetParser(locale).getPrayerTimes(this.location);
    }
}
