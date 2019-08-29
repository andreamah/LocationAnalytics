package io.github.andreamah.locationtracker;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;


@Database(entities = {LocationEntity.class}, version = 1)
public abstract class LocationDatabase extends RoomDatabase {

    private static LocationDatabase instance;
    public abstract LocationDao locationDao();

    public static synchronized  LocationDatabase getInstance(Context context) {
        if (instance ==null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    LocationDatabase.class, "location_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }
    };

    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private LocationDao locationDao;

        private PopulateDbAsyncTask(LocationDatabase db) {
            locationDao = db.locationDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }
    }
}
