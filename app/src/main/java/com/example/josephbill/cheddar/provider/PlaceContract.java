package com.example.josephbill.cheddar.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Joseph Bill on 4/7/2018.
 */

public class PlaceContract {
    //the authority which is how your code knows which content provider to access
    public static final String AUTHORITY = "com.example.josephbill.cheddar";

    //the base content uri = "content://" +<authority>
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
     public static final String PATH_PLACES = "places";
    public static final class PlaceEntry implements BaseColumns{
        //task entry content uri = base content uri  + path
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLACES).build();

        public static final String TABLE_NAME = "places";
        public static final String COLUMN_PLACE_ID = "placeID";
    }
}
