package id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes.list.FetchTimesTask;
import id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes.utils.AlarmReceiver;
import id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes.utils.DateFormatter;
import id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes.utils.Internet;
import id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes.utils.Location;
import id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes.utils.LocationManager;
import id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes.utils.PrayerTime;
import id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes.utils.PrayerTime.Type;
import id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes.utils.PrayerTimeTable;
import id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes.utils.PrayerTimes;

/**
 * A placeholder fragment containing a simple view.
 */
public class HomeFragment extends Fragment {
    private TextView remainingTime, remainingTimeType, locationName, dayText, dateText;
    private LocationManager locMan;
    private List<Location> locations;
    private View rootView;
    private AlertDialog dbExpireAlert;
    private Timer cdTimer;
    private RemainingPrayerTime cdTask;
    private AlarmReceiver alarmRec;
    private DateFormatter dateFormatter;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.rootView = inflater.inflate(R.layout.fragment_home, container, false);
        this.locMan = new LocationManager(getActivity());
        this.cdTimer = new Timer();
        this.dateFormatter = new DateFormatter(getString(R.string.locale).equals("en") ? Locale.ENGLISH : new Locale("id", "ID"));

        setHasOptionsMenu(true);

        init();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getMenuInflater().inflate(R.menu.home, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private MenuInflater getMenuInflater() {
        return getActivity().getMenuInflater();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_loc_add) {
            getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.animator.fragment_enter_anim, R.animator.fragment_exit_anim)
                    .replace(R.id.container, new LocationFragment(), LocationFragment.TAG)
                    .commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        this.remainingTimeType = (TextView) rootView.findViewById(R.id.prayerTimeLabel);
        this.remainingTimeType.setTextColor(Color.parseColor("#FFFFFF"));

        this.dayText = (TextView) rootView.findViewById(R.id.dayText);
        this.dayText.setTextColor(Color.parseColor("#FFFFFF"));

        this.dateText = (TextView) rootView.findViewById(R.id.dateText);
        this.dateText.setTextColor(Color.parseColor("#FFFFFF"));

        this.remainingTime = (TextView) rootView.findViewById(R.id.remainingTime);
        this.remainingTime.setTextColor(Color.parseColor("#FFFFFF"));

        this.locationName = (TextView) rootView.findViewById(R.id.locationText);
        this.locationName.setTextColor(Color.parseColor("#FFFFFF"));
    }

    private void setViews(Location loc) {
        PrayerTimes times = loc.getPrayerTimes();
        PrayerTime time = times.getPrayerTimesOfDay();
        if (time != null) {
            Type nextTimeType = time.getNextTime();
            if (nextTimeType == null) {
                nextTimeType = Type.IMSAK;
            }

            try {
                this.dayText.setText(dateFormatter.ToDay(time.getTime(Type.DATE), false));
                this.dateText.setText(dateFormatter.ToDate(time.getTime(Type.DATE)));
            } catch (ParseException e) {
                Log.d("prayerTimes", "Couldn't set long day and date. Error :" + e.getMessage());
            }

            this.remainingTimeType.setText(getActivity().getString((R.string.remaning_period_of_time))
                    + " " + getString(nextTimeType.toStringCode()));

            new PrayerTimeTable(getActivity(), rootView, R.id.prayerTimeTableLayout, times);

            String locName = loc.getName();
            locName = locName.substring(locName.indexOf(",") + 1);
            this.locationName.setText(locName);
        } else {
            Log.d("prayerTimes", "cannot find current day in prayer time table");
            dbExpireAlert.show();
        }
    }

    private void init() {
        locations = this.locMan.fetchLocations();
        if (locations.isEmpty()) {
            Log.i("init", "Locations are empty.");
            getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.animator.fragment_enter_anim, R.animator.fragment_exit_anim)
                    .replace(R.id.container, new LocationFragment(), LocationFragment.TAG)
                    .commit();
        } else {
            final Location loc = locations.get(0);
            initDbExpireAlert(loc);
            initViews();
            setViews(loc);
            startCountdownTimer(loc);
        }
    }

    private void InvalidDataAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.invalid_data_error);
        builder.setCancelable(false);
        builder.setMessage(R.string.detected_invalid_data);
        builder.setPositiveButton(R.string.select_loc, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                getFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.animator.fragment_enter_anim, R.animator.fragment_exit_anim)
                        .replace(R.id.container, new LocationFragment(), LocationFragment.TAG)
                        .commit();
            }
        });
        builder.create().show();
    }

    private boolean startCountdownTimer(Location loc) {
        PrayerTimes times = loc.getPrayerTimes();
        PrayerTime time = times.getPrayerTimesOfDay();
        if (time != null) {
            Type nextTimeType = time.getNextTime();
            String nextTime = time.getTime(Type.DATE) + " " + time.getTime(nextTimeType);

            if (nextTimeType == null) {
                try {
                    nextTimeType = Type.IMSAK;
                    PrayerTime tempT = times.getPrayerTimes().get(times.getPrayerTimes().indexOf(time) + 1);
                    nextTime = tempT.getTime(Type.DATE) + " " + tempT.getTime(nextTimeType);
                } catch (IndexOutOfBoundsException ex) {
                    ex.printStackTrace();
                    Log.d("prayerTimes", "cannot find next day in prayer time table");
                }
            }

            if (!nextTime.contains("null")) {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                Date nextPrayerTime = null;
                long diffMillis = 0;
                try {
                    nextPrayerTime = format.parse(nextTime);
                    diffMillis = nextPrayerTime.getTime() - cal.getTime().getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                    InvalidDataAlert();
                    return false;
                }
                this.cdTask = new RemainingPrayerTime(this.remainingTime, diffMillis / 1000, loc);
                this.cdTimer.scheduleAtFixedRate(this.cdTask, 0, 1000);

                this.alarmRec = new AlarmReceiver(getActivity());
                this.alarmRec.createAlarm(nextPrayerTime.getTime(), nextTimeType);

                return true;
            } else {
                Log.d("prayerTime", "cannot find next time in prayer times");
                dbExpireAlert.show();
            }
        } else {
            Log.d("prayerTimes", "cannot find current day in prayer time table");
            dbExpireAlert.show();
        }
        return false;
    }

    private boolean updateTimes(final Location loc) {
        try {
            loc.setPrayerTimes(new FetchTimesTask(loc).execute().get());
            if (!loc.getPrayerTimes().getPrayerTimes().isEmpty()) {
                Log.i("Update", "Updated....");
                return locMan.saveLocation(loc);
            } else {
                Log.v("UpdateTimes",
                        "Times couldn't be fetched. Update canceled.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void initDbExpireAlert(final Location loc) {

        final Activity activity = getActivity();
        this.dbExpireAlert = new AlertDialog.Builder(activity)
                .setTitle(R.string.times_expired)
                .setMessage(R.string.times_expired_msg)
                .setPositiveButton(R.string.alert_expire_db_yes, null)
                .setNegativeButton(R.string.alert_expire_db_no,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                activity.finish();
                            }
                        }).create();
        this.dbExpireAlert.setCancelable(false);
        this.dbExpireAlert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = dbExpireAlert.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            if (!Internet.hasInternetConnection(activity)) {
                                Toast.makeText(
                                        activity,
                                        R.string.please_check_internet_connetion,
                                        Toast.LENGTH_SHORT).show();
                            } else if (updateTimes(loc)) {
                                Toast.makeText(activity,
                                        R.string.times_updated_successfully,
                                        Toast.LENGTH_SHORT).show();
                                setViews(loc);
                                dbExpireAlert.dismiss();
                            } else {
                                Toast.makeText(
                                        activity,
                                        R.string.failed_to_update_times,
                                        Toast.LENGTH_SHORT).show();
                                dbExpireAlert.dismiss();
                            }
                        } catch (Exception ex) {
                            Log.e("PrayerTimes", "Db Expire Alert Failed : " + ex.getMessage());
                        }
                    }
                });
            }
        });

    }

    public class RemainingPrayerTime extends TimerTask {
        private TextView clock;
        private Location loc;
        private long durationSeconds;
        private Runnable action;

        public RemainingPrayerTime(final TextView clock, long startInSec, Location loc) {
            this.clock = clock;
            this.loc = loc;
            this.durationSeconds = startInSec;
            this.action = new Runnable() {
                @Override
                public void run() {
                    clock.setText(String.format(Locale.ENGLISH,
                            "%02d:%02d:%02d", durationSeconds / 3600,
                            (durationSeconds % 3600) / 60,
                            (durationSeconds % 60)));
                }
            };
        }

        @Override
        public void run() {
            if (durationSeconds > 0) {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(action);
                }
                --durationSeconds;
            } else {
                reinitializeTimer();
            }
        }

        private void reinitializeTimer() {
            PrayerTimes times = this.loc.getPrayerTimes();
            PrayerTime time = times.getPrayerTimesOfDay();
            if (time != null) {
                Type nextTimeType = time.getNextTime();
                String nextTime = time.getTime(Type.DATE) + " " + time.getTime(nextTimeType);

                if (nextTimeType == null) {
                    nextTimeType = Type.IMSAK;
                    PrayerTime tempT = times.getPrayerTimes().get(times.getPrayerTimes().indexOf(time) + 1);
                    nextTime = tempT.getTime(Type.DATE) + " " + tempT.getTime(nextTimeType);
                }
                if (!nextTime.contains("null")) {
                    Calendar cal = Calendar.getInstance();
                    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                    Date nextPrayerTime = null;
                    long diffMillis = 0;
                    try {
                        nextPrayerTime = format.parse(nextTime);
                        diffMillis = nextPrayerTime.getTime() - cal.getTime().getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    this.durationSeconds = diffMillis / 1000;

                    Activity activity = getActivity();
                    if (activity != null) {
                        final String nextTimeStr = activity.getString(nextTimeType.toStringCode());
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if ("tr".equals(getActivity().getString(R.string.locale))) {
                                    remainingTimeType.setText(nextTimeStr
                                            + " " + getActivity().getString((R.string.remaning_period_of_time)));
                                } else {
                                    remainingTimeType.setText(getActivity().getString((R.string.remaning_period_of_time))
                                            + " " + nextTimeStr);
                                }
                            }
                        });
                    }

                } else {
                    Log.d("prayerTime", "cannot find next time in prayer times");
                }
            } else {
                Log.d("prayerTimes", "cannot find current day in prayer time table");
                dbExpireAlert.show();
            }
        }

    }
}