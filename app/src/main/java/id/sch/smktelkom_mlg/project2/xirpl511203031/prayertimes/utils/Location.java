package id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes.utils;

import java.io.Serializable;

public class Location implements Serializable {

    private static final long serialVersionUID = -8762189767271852859L;
    private int countryId, stateId, cityId;
    private String name;
    private PrayerTimes prayerTimes;

    public Location() {
        this(0, 0, 0, "Unknown", new PrayerTimes());
    }

    public Location(int countryId, int stateId) {
        this(countryId, stateId, stateId, "Unknown", new PrayerTimes());
    }

    public Location(int countryId, int stateId, String name) {
        this(countryId, stateId, stateId, name, new PrayerTimes());
    }

    public Location(int countryId, int stateId, int cityId) {
        this(countryId, stateId, cityId, "Unknown", new PrayerTimes());
    }

    public Location(int countryId, int stateId, int cityId, String name) {
        this(countryId, stateId, cityId, name, new PrayerTimes());
    }

    public Location(int countryId, int stateId, int cityId, String name,
                    PrayerTimes prayerTimes) {
        super();
        this.countryId = countryId;
        this.stateId = stateId;
        this.cityId = cityId;
        this.name = name;
        this.prayerTimes = prayerTimes;
    }

    @Override
    public String toString() {
        return "Loc >" + countryId + "," + stateId + "," + cityId + "," + name
                + ", Times : " + prayerTimes.toString();
    }

    public PrayerTimes getPrayerTimes() {
        return prayerTimes;
    }

    public void setPrayerTimes(PrayerTimes prayerTimes) {
        this.prayerTimes = prayerTimes;
    }

    public int getCountryId() {
        return countryId;
    }

    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
