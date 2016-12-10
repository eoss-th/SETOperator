package com.th.eoss.util;

import android.app.Activity;

import com.th.eoss.setoperator.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by eossth on 12/10/2016 AD.
 */

public class SET {

    private static final String CSV_URL = "http://eoss-setfin.appspot.com/csv";

    private static SET set;

    private String asOfDate;

    private List<Map<String, String>> stockList, resultList;

    private Map<String, Calendar> xdMap = new HashMap<String, Calendar>();

    private DateFormat xdDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);

    public static SET instance () {
        if (set==null) {
            set = new SET();
        }
        return set;
    }

    private String loadFake(Activity activity)  throws Exception {

        Scanner sc = new Scanner(activity.getResources().openRawResource(R.raw.m));

        StringBuilder buffer = new StringBuilder();

        while (sc.hasNextLine()) {
            buffer.append(sc.nextLine());
        }

        return buffer.toString();
    }

    public void init() {
        stockList = new ArrayList<>();
        resultList = new ArrayList<>();
    }

    private void mapValue(Map<String, String> row, String name, String value) {
        try {
            Float.parseFloat(value);
            row.put(name, value);
        } catch (Exception e) {
            row.put(name, "-");
        }
    }

    public String load() throws Exception {

        URL url = new URL(CSV_URL);
        BufferedReader br = new BufferedReader(
                new InputStreamReader(url.openStream()));

        if (br.readLine()!=null) {

            stockList.clear();
            xdMap.clear();

            String line, xd;
            String [] tokens;
            Map<String, String> row;
            while (true) {

                line = br.readLine();
                if (line==null) break;

                tokens = line.split(",");

                row = new HashMap<String, String>();

                row.put("Type", tokens[0]);

                row.put("symbol", tokens[2]);

                mapValue(row, "ROA", tokens[12]);

                mapValue(row, "ROE", tokens[13]);

                mapValue(row, "Price", tokens[15]);

                mapValue(row, "P/E", tokens[16]);

                mapValue(row, "P/BV", tokens[17]);

                mapValue(row, "DVD", tokens[18]);

                xd = tokens[25];

                try {

                    Calendar xdate = Calendar.getInstance(Locale.US);
                    xdate.setTime(xdDateFormat.parse(xd));
                    xdMap.put(row.get("symbol"), xdate);

                } catch (Exception e) {

                }

                stockList.add(row);
            }

        }
        return asOfDate;
    }

    public void applyFilter(Map<String, Filter> filterList) {

        boolean match;

        Set<String> filterSet = filterList.keySet();
        Filter filter;

        for (Map<String, String> map: stockList) {

            match = true;

            for (String name: filterSet) {

                filter = filterList.get(name);
                try {

                    if (filter.opt.equals("=")) {

                        match &= map.get(name).equals(filter.value);

                    } if (filter.opt.equals("<")) {

                        match &= Double.parseDouble(map.get(name)) <= Double.parseDouble(filter.value);

                    } else if (filter.opt.equals(">")) {

                        match &= Double.parseDouble(map.get(name)) >= Double.parseDouble(filter.value);
                    }

                } catch (Exception e) {
                    match &= false;
                }

            }

            if (match) {
                resultList.add(map);
            }

        }
    }

    public List<Map<String, String>> resultList() {
        return resultList;
    }

    public void clearResult() {
        resultList.clear();
    }

    public boolean isXDAfterNow(String symbol) {
        Calendar xd = xdMap.get(symbol);

        if (xd!=null && xd.after(Calendar.getInstance(Locale.US))) {
            return true;
        }

        return false;
    }

    public DateFormat xdDateFormat() {
        return xdDateFormat;
    }

    public static class Filter {

        public String opt;

        public String value;

        public Filter (String opt, String value) {
            this.opt = opt;
            this.value = value;
        }
    }

}
