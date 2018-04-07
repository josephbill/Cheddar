package com.example.josephbill.cheddar;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.josephbill.cheddar.provider.PlaceContract;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements

        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // constants
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final int  PERMISSIONS_REQUEST_FINE_LOCATION = 111;
    private static final int PLACE_PICKER_REQUEST = 1;


    //member variables
    private PlaceListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private  boolean mIsEnabled;
    private GoogleApiClient mClient;
    /**
     * called when the activity is starting
     *
//     *@param savedInsatanceState the bundle that contains data supplied in onSavestateinstance
     */

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set up the recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.places_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new PlaceListAdapter(this, null);
        mRecyclerView.setAdapter(mAdapter);

        mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();
    }
// called when the google api client is successfully connected
// @param connectionHint Bundle of data provided to clients by google play services

    @Override
    public void onConnected(@Nullable Bundle connectionHint){
        refreshPlacesData();
        Log.i(TAG, "API Client Connection Successfull");
    }

// called when the Google APi client is suspended
//@param cause cause the reason for disconnection . Defined by constants Cause

    @Override
    public void onConnectionSuspended(int cause){Log.i(TAG, "API Client Connection Suspended");}

//called when the google api client failed to connect to google play services
//@param result a connectionResult that can be used for resolving the error

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.e(TAG, "API Client connection failed!");
    }

    public void refreshPlacesData() {
        Uri uri = PlaceContract.PlaceEntry.CONTENT_URI;
        Cursor data = getContentResolver().query(
                uri,
                null,
                null,
                null,
                null

        );
        if (data == null || data.getCount() == 0) return;
        List<String> guids = new ArrayList<>();
        while (data.moveToNext()) {
            guids.add(data.getString(data.getColumnIndex(PlaceContract.PlaceEntry.COLUMN_PLACE_ID)));
        }
        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mClient,
                guids.toArray(new String[guids.size()]));
        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                mAdapter.swapPlaces(places);
            }
        });
    }

    public void onAddPlaceButtonClicked(View view){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "You need to enable location permission first", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            Intent i = builder.build(this);
            startActivityForResult(i, PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            Log.e(TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
        }catch (GooglePlayServicesNotAvailableException e){
            Log.e(TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
        }catch (Exception e){
            Log.e(TAG, String.format("PlacePicker Exception: %s ", e.getMessage()));
        }
    }

// called when the place picker activity returns back with a selected place or after cancelling
// @param requestCode the request code passed when calling startactivity for result
//    @param resultCode the result code specified by the second Activity
//    @param the intent that carries the result data

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK){
            Place place = PlacePicker.getPlace(this, data);
            if (place == null){
                Log.i(TAG, "No Place Selected");
                return;
            }
            String placeID = place.getId();

            //insert a new place into db here
            ContentValues contentValues = new ContentValues();
            contentValues.put(PlaceContract.PlaceEntry.COLUMN_PLACE_ID, placeID);
            getContentResolver().insert(PlaceContract.PlaceEntry.CONTENT_URI, contentValues);

            //get live data information
            refreshPlacesData();
        }
    }

}
