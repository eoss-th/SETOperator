package com.th.eoss.setoperator;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

/**
 * Created by eossth on 12/12/2016 AD.
 */

public class Theme {

    static int white, black, gray, red, green, blue, darkBlue, cyan, orange;

    public static void register(Context context) {

        white = ContextCompat.getColor(context, android.R.color.white);
        black = ContextCompat.getColor(context, android.R.color.black);
        gray = ContextCompat.getColor(context, android.R.color.darker_gray);
        red = ContextCompat.getColor(context, android.R.color.holo_red_light);
        green = ContextCompat.getColor(context, android.R.color.holo_green_light);
        blue = ContextCompat.getColor(context, android.R.color.holo_blue_light);
        darkBlue = ContextCompat.getColor(context, android.R.color.holo_blue_dark);
        cyan = ContextCompat.getColor(context, android.R.color.holo_blue_bright);
        orange = ContextCompat.getColor(context, android.R.color.holo_orange_light);
    }

}
