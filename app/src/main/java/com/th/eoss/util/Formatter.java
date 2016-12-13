package com.th.eoss.util;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by eossth on 12/13/2016 AD.
 */

public class Formatter {

    public static final NumberFormat decimalFormat = new DecimalFormat("0.00");

    public static final DateFormat xdDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);

}
