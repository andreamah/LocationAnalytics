package io.github.andreamah.locationtracker;


import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface LocationDao {

    @Insert
    void insert(LocationEntity location);

    @Update
    void update(LocationEntity location);

    @Delete
    void delete(LocationEntity location);

    @Query("DELETE FROM location_table")
    void deleteAllLocations();

    @Query("SELECT * FROM location_table ORDER BY date DESC")
    LiveData<List<LocationEntity>> getAllLocations();
}
