package app.com.example.android.sunshine.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class WeatherProvider extends ContentProvider
{
    private static final UriMatcher uriMatcher = buildUriMatcher();
    private WeatherDbHelper openHelper;

    static final int WEATHER = 100;
    static final int WEATHER_WITH_LOCATION = 101;
    static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    static final int LOCATION = 300;

    private static final SQLiteQueryBuilder weatherByLocationSettingQueryBuilder;
    static
    {
        weatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();

        //weather INNER JOIN location ON weather.location_id = location._id
        weatherByLocationSettingQueryBuilder.setTables(
                WeatherContract.WeatherEntry.TABLE_NAME + " INNER JOIN " +
                        WeatherContract.LocationEntry.TABLE_NAME + " ON " +
                        WeatherContract.WeatherEntry.TABLE_NAME + "." +
                        WeatherContract.WeatherEntry.COLUMN_LOCATION_KEY + " = " +
                        WeatherContract.LocationEntry.TABLE_NAME + "." +
                        WeatherContract.LocationEntry._ID);
    }

    //location.location_setting = ?
    private static final String locationSettingSelection =
            WeatherContract.LocationEntry.TABLE_NAME +
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? ";

    //location.location_setting = ? AND date >= ?
    private static final String locationSettingWithStartDateSelection =
            WeatherContract.LocationEntry.TABLE_NAME +
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATE + " >= ? ";

    //location.location_setting = ? AND date = ?
    private static final String locationSettingAndDaySelection =
            WeatherContract.LocationEntry.TABLE_NAME +
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATE + " = ? ";

    static UriMatcher buildUriMatcher()
    {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER
                , WEATHER);
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER
                + "/*", WEATHER_WITH_LOCATION);
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER
                + "/*/#", WEATHER_WITH_LOCATION_AND_DATE);
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_LOCATION,
                LOCATION);

        return  matcher;
    }

    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder)
    {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        long startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == 0)
        {
            selection = locationSettingSelection;
            selectionArgs = new String[]{locationSetting};
        }
        else
        {
            selection = locationSettingWithStartDateSelection;
            selectionArgs = new String[]{locationSetting, Long.toString(startDate)};
        }

        return weatherByLocationSettingQueryBuilder.query(openHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getWeatherByLocationSettingAndDate
            (Uri uri, String[] projection, String sortOrder)
    {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        long date = WeatherContract.WeatherEntry.getDateFromUri(uri);

        return weatherByLocationSettingQueryBuilder.query(openHelper.getReadableDatabase(),
                projection,
                locationSettingAndDaySelection,
                new String[]{locationSetting, Long.toString(date)},
                null,
                null,
                sortOrder
        );
    }

    private Cursor getWeather
            (String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        return this.openHelper.getReadableDatabase().query(
                WeatherContract.WeatherEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getLocation
            (String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        return this.openHelper.getReadableDatabase().query(
                WeatherContract.LocationEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public boolean onCreate()
    {
        openHelper = new WeatherDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri)
    {
        final int match = uriMatcher.match(uri);
        switch (match)
        {
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE;

            case WEATHER_WITH_LOCATION:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;

            case WEATHER:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;

            case LOCATION:
                return WeatherContract.LocationEntry.CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
             String sortOrder)
    {
        Cursor retCursor;

        switch (uriMatcher.match(uri))
        {
            case WEATHER_WITH_LOCATION_AND_DATE:
            {
                retCursor = getWeatherByLocationSettingAndDate(uri, projection, sortOrder);
                break;
            }

            case WEATHER_WITH_LOCATION:
            {
                retCursor = getWeatherByLocationSetting(uri, projection, sortOrder);
                break;
            }

            case WEATHER:
            {
                retCursor = getWeather(projection, selection, selectionArgs, sortOrder);
                break;
            }

            case LOCATION:
            {
                retCursor = getLocation(projection, selection, selectionArgs, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        final SQLiteDatabase database = openHelper.getWritableDatabase();

        Uri returnUri;
        switch (uriMatcher.match(uri))
        {
            case WEATHER:
            {
                long _id = database.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, values);

                if (_id > 0)
                {
                    returnUri = WeatherContract.WeatherEntry.buildWeatherUri(_id);
                }
                else
                {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }

            case LOCATION:
            {
                long _id = database.insert(WeatherContract.LocationEntry.TABLE_NAME, null, values);

                if(_id > 0)
                {
                    returnUri = WeatherContract.LocationEntry.buildLocationUri(_id);
                }
                else
                {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        this.getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        final SQLiteDatabase database = openHelper.getWritableDatabase();

        int rowsDeleted;
        switch (uriMatcher.match(uri))
        {
            case WEATHER:
                rowsDeleted = database.delete(WeatherContract.WeatherEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;

            case LOCATION:
                rowsDeleted = database.delete(WeatherContract.LocationEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if(rowsDeleted != 0)
        {
            this.getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        final SQLiteDatabase database = openHelper.getWritableDatabase();

        int rowsUpdated;
        switch (uriMatcher.match(uri))
        {
            case WEATHER:
                rowsUpdated = database.update(WeatherContract.WeatherEntry.TABLE_NAME, values,
                        selection, selectionArgs);
                break;

            case LOCATION:
                rowsUpdated = database.update(WeatherContract.LocationEntry.TABLE_NAME, values,
                        selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if(rowsUpdated != 0)
        {
            this.getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values)
    {
        final SQLiteDatabase database = openHelper.getWritableDatabase();

        switch (uriMatcher.match(uri))
        {
            case WEATHER:

                database.beginTransaction();

                int returnCount = 0;
                try
                {
                    for (ContentValues value : values)
                    {
                        long _id = database
                                .insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);
                        if (_id != -1)
                        {
                            returnCount++;
                        }
                    }

                    database.setTransactionSuccessful();
                }
                finally
                {
                    database.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);

                return returnCount;

            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    @TargetApi(11)
    public void shutdown()
    {
        openHelper.close();
        super.shutdown();
    }
}