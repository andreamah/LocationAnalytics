package io.github.andreamah.locationtracker;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;

import androidx.lifecycle.LiveData;

public class LocationRepository {
    private LocationDao locationDao;
    private LiveData<List<LocationEntity>> allLocations;

    public LocationRepository(Application application) {
        LocationDatabase database = LocationDatabase.getInstance(application);
        locationDao = database.locationDao();
        allLocations = locationDao.getAllLocations();
    }

    public void insert(LocationEntity location) {
        new InsertLocationAsyncTask(locationDao).execute(location);
    }
    public void update(LocationEntity location) {
        new UpdateLocationAsyncTask(locationDao).execute(location);
    }
    public void delete(LocationEntity location) {
        new DeleteLocationAsyncTask(locationDao).execute(location);
    }
    public void deleteAllLocations() {
        new deleteAllLocationsAsyncTask(locationDao).execute();
    }

    public LiveData<List<LocationEntity>> getAllLocations(){
        return allLocations;
    }


    private static class InsertLocationAsyncTask extends AsyncTask<LocationEntity, Void, Void> {
        private LocationDao locationDao;
        private InsertLocationAsyncTask(LocationDao locationdao) {
            this.locationDao = locationdao;
        }


        @Override
        protected Void doInBackground(LocationEntity... locationEntities) {
            locationDao.insert(locationEntities[0]);
            return null;
        }
    }

    private static class UpdateLocationAsyncTask extends AsyncTask<LocationEntity, Void, Void> {
        private LocationDao locationDao;
        private UpdateLocationAsyncTask(LocationDao locationdao) {
            this.locationDao = locationDao;
        }


        @Override
        protected Void doInBackground(LocationEntity... locationEntities) {
            locationDao.insert(locationEntities[0]);
            return null;
        }
    }

    private static class DeleteLocationAsyncTask extends AsyncTask<LocationEntity, Void, Void> {
        private LocationDao locationDao;
        private DeleteLocationAsyncTask(LocationDao locationdao) {
            this.locationDao = locationDao;
        }


        @Override
        protected Void doInBackground(LocationEntity... locationEntities) {
            locationDao.insert(locationEntities[0]);
            return null;
        }
    }

    private static class deleteAllLocationsAsyncTask extends AsyncTask<LocationEntity, Void, Void> {
        private LocationDao locationDao;
        private deleteAllLocationsAsyncTask(LocationDao locationdao) {
            this.locationDao = locationDao;
        }


        @Override
        protected Void doInBackground(LocationEntity... locationEntities) {
            locationDao.deleteAllLocations();
            return null;
        }
    }
}
