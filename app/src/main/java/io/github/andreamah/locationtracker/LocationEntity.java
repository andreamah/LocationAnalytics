package io.github.andreamah.locationtracker;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName="location_table")
public class LocationEntity {

    @PrimaryKey
    @NonNull
    @TypeConverters({TimestampConverter.class})
    private Date date;

    private double latitude;
    private double longitude;
    private int weight;

    public LocationEntity(double latitude, double longitude, Date date, int weight) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.weight = weight;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Date getDate() {
        return date;
    }

    public int getWeight() {
        return weight;
    }
}
