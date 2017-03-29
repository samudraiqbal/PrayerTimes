package id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes.utils;

import java.util.Map;

public interface Parser {
    Map<String, Integer> fetchCities(int cityId);

    Map<String, Integer> fetchStates(int stateId);

    Map<String, Integer> fetchCountries();

    PrayerTimes getPrayerTimes(Location loc);

}
