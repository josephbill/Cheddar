package com.example.josephbill.cheddar.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * Created by Joseph Bill on 4/7/2018.
 */

public class PlaceContentProvider extends ContentProvider {

    public static final int PLACES = 100;
    public static final int PLACE_WITH_ID = 101;

    //declare a static variable for the uri matcher that you construct
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final String TAG = PlaceContentProvider.class.getName();

    //define a static buildurimatcher method that associates URI's with their int match
    public static UriMatcher buildUriMatcher(){
        //intialize a urimatcher
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        //add uri matches
        uriMatcher.addURI(PlaceContract.AUTHORITY, PlaceContract.PATH_PLACES, PLACES);
        uriMatcher.addURI(PlaceContract.AUTHORITY, PlaceContract.PATH_PLACES + "/#", PLACE_WITH_ID);
        return uriMatcher;
    }

    //member variable for placeholder thats intialized in the onCreate() Method
    private PlaceDbHelper mPlaceDbHelper;

    @Override
    public boolean onCreate(){
        Context context = getContext();
        mPlaceDbHelper = new PlaceDbHelper(context);
        return true;
    }

    //handles request to insert a single new row of data
//    @param uri
//    @param Values
//    @return
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values){
        final SQLiteDatabase db = mPlaceDbHelper.getWritableDatabase();

        //write uri matching code to identify the match for the places directory
        int match = sUriMatcher.match(uri);
        Uri returnUri; //uri to be returned
        switch (match){
            case PLACES:
                //insert new values into database
                long id = db.insert(PlaceContract.PlaceEntry.TABLE_NAME, null, values);
                if (id > 0){
                    returnUri = ContentUris.withAppendedId(PlaceContract.PlaceEntry.CONTENT_URI, id);

                }else{
                    throw new android.database.SQLException("Failed to insert row into" + uri);
                }
                break;
            //default case throws an unsupportedOperationException
            default:
                throw new UnsupportedOperationException("Unknown uri: "+ uri);
        }

        //notify the resolver if the uri has been changed and return the newly inserted uri
        getContext().getContentResolver().notifyChange(uri, null);

        //return constructed uri (this points to the newly inserted row of data
        return returnUri;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder){
        //get access to underlying database (read -only for query)
        final SQLiteDatabase db = mPlaceDbHelper.getReadableDatabase();

        //write uri match code and set a variable to return a cursor
        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match){
            //query for the places directory
            case PLACES:
                retCursor = db.query(PlaceContract.PlaceEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            //default exception
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        //set a notification URI on the cursor and return that cursor
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        //return the desired cursor
        return retCursor;
    }
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs){
        //get access to the db and write the uri matching code to recognize a single item
        final SQLiteDatabase db = mPlaceDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        //keep track of number of deleted places
        int placesDeleted; // starts as 0
        switch (match){
            //handle the single item case , recognized by the id included in the uri path
            case PLACE_WITH_ID:
                //get the place id from the uri path
                String id = uri.getPathSegments().get(1);
                //use selections / selectionARGS to filter for this id
                placesDeleted = db.delete(PlaceContract.PlaceEntry.TABLE_NAME, "_id=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri:" + uri);
        }
        //notify the resolver of a change and return the number of items deleted
        if (placesDeleted != 0){
            // A place or more was deleted , set notification
            getContext().getContentResolver().notifyChange(uri, null);
        }
        //return the number of places deleted
        return placesDeleted;
    }

    @Override
    public int update (@NonNull Uri uri, ContentValues values, String selection,
                       String[] selectionArgs){
        //get access to underlying database
        final SQLiteDatabase db = mPlaceDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        //keep track of number of updated places
        int placesUpdated;

        switch (match){
            case PLACE_WITH_ID:
                //get place id from the uri path
                String id = uri.getPathSegments().get(1);
                //use selections / selectionArgs to filter for this ID
                placesUpdated = db.update(PlaceContract.PlaceEntry.TABLE_NAME, values,"_id?", new String[]{id});
                break;
            //default exception
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
        //notify the resolver of a change and return the number of items updated
        if (placesUpdated != 0 ){
            //a place or more was updated , set notification
            getContext().getContentResolver().notifyChange(uri, null);
        }
        //return the number of places updated
        return placesUpdated;


    }
    @Override
    public String getType(@NonNull Uri uri){
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
