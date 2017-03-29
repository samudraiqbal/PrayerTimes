package id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;

import java.util.List;
import java.util.Locale;

import id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes.list.FetchTimesTask;
import id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes.utils.Internet;
import id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes.utils.Localization;
import id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes.utils.Location;
import id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes.utils.LocationManager;

public class Home extends Activity implements
        LeftMenuFragment.NavigationDrawerCallbacks {
    private final Handler fragmentHandler = new Handler();
    /**
     * Fragment managing the behaviors, interactions and presentation of the
     * navigation drawer.
     */
    private LeftMenuFragment mNavigationDrawerFragment;
    /**
     * Used to store the last screen title. For use in
     * {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Localization localization = new Localization(this);
        localization.InitLocale();

        setContentView(R.layout.activity_home);

        mNavigationDrawerFragment = (LeftMenuFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getString(R.string.title_times);

        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        AsyncTask<Void, Void, Void> updateTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                updateTimes();
                return null;
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            updateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            updateTask.execute();
    }

    private boolean updateTimes() {
        if (Internet.hasInternetConnection(this)) {
            try {
                LocationManager locMan = new LocationManager(this);
                List<Location> locs = locMan.fetchLocations();
                if (!locs.isEmpty()) {
                    Location loc = locs.get(0);
                    loc.setPrayerTimes(new FetchTimesTask(loc, new Locale(getString(R.string.locale))).execute().get());
                    if (!loc.getPrayerTimes().getPrayerTimes().isEmpty()) {
                        return locMan.saveLocation(loc);
                    } else {
                        Log.v("UpdateTimes",
                                "Times couldn't be fetched. Update canceled.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        Fragment f = getFragmentManager().findFragmentByTag(
                LocationFragment.TAG);
        if (f != null) {
            LocationFragment locF = (LocationFragment) f;
            locF.switchList();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        final int pos = position;
        fragmentHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                final FragmentManager fragmentManager = getFragmentManager();
                switch (pos) {
                    case 0:
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_enter_anim, R.animator.fragment_exit_anim)
                                .replace(R.id.container, new HomeFragment()).commit();
                        mTitle = getString(R.string.title_times);
                        break;
                    case 1:
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_enter_anim, R.animator.fragment_exit_anim)
                                .replace(R.id.container, new MonthlyTimesFragment())
                                .commit();
                        mTitle = getString(R.string.title_ml_times);
                        break;
                    case 2:
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_enter_anim, R.animator.fragment_exit_anim)
                                .replace(R.id.container, new SettingsFragment())
                                .commit();
                        mTitle = getString(R.string.title_settings);
                        break;
                    case 3:
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_enter_anim, R.animator.fragment_exit_anim)
                                .replace(R.id.container, new AboutFragment()).commit();
                        mTitle = getString(R.string.title_about);
                        break;
                }
            }
        }, 300);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            restoreActionBar();
        }
        return super.onCreateOptionsMenu(menu);
    }
}
