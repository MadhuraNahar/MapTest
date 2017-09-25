package com.parkman.maptest.utils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by DELL on 9/21/2017.
 */
public class Utils {

    public static String loadJSONFromAsset (String filename, Context context) throws IOException {

        String jsonString;
        AssetManager manager = context.getAssets();
        InputStream file = manager.open(filename);
        byte[] formArray = new byte[file.available()];
        file.read(formArray);
        file.close();
        jsonString = new String(formArray, "UTF-8");
        return jsonString;
    }
}
