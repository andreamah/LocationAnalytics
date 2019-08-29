package io.github.andreamah.locationtracker;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class LocationViewModel extends AndroidViewModel {
    private LocationRepository repository;
    private LiveData<List<LocationEntity>> allLocations;

    public LocationViewModel(@NonNull Application application) {
        super(application);
        repository = new LocationRepository(application);
        allLocations = repository.getAllLocations();
    }

    public void insert(LocationEntity location) {
        repository.insert(location);
    }
    public void update(LocationEntity location) {
        repository.update(location);
    }
    public void delete(LocationEntity location) {
        repository.delete(location);
    }
    public void deleteAllLocations(LocationEntity location) {
        repository.deleteAllLocations();
    }
    public LiveData<List<LocationEntity>> getAllLocations() {
        return allLocations;
    }
}
