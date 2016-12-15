package com.th.eoss.util;

import android.support.v4.content.ParallelExecutorCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by eossth on 12/10/2016 AD.
 */

public class SET {

    private static final String CSV_URL = "http://eoss-setfin.appspot.com/csv?setOperator";

    private static SET set;

    private String asOfDate;

    private List<Map<String, String>> stockList, filteredList;

    private Map<String, Calendar> xdMap;

    private Map<String, Filter> filterMap = new HashMap<>();

    private SET() {
        stockList = new ArrayList<>();
        filteredList = new ArrayList<>();
        xdMap = new HashMap<>();
    }

    public static SET instance() {
        if (set==null) {
            set = new SET();
        }
        return set;
    }

    public void load () {

        if (stockList.isEmpty()) {
            try {
                URL url = new URL(CSV_URL);
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(url.openStream()));

                if (br.readLine()!=null) {

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

                        mapValue(row, "ROA %", tokens[12]);

                        mapValue(row, "ROE %", tokens[13]);

                        mapValue(row, "Price", tokens[15]);

                        mapValue(row, "P/E", tokens[16]);

                        mapValue(row, "P/BV", tokens[17]);

                        mapValue(row, "DVD %", tokens[18]);

                        xd = tokens[25];

                        try {

                            Calendar xdate = Calendar.getInstance(Locale.US);
                            xdate.setTime(Formatter.xdDateFormat.parse(xd));
                            xdMap.put(row.get("symbol"), xdate);

                        } catch (Exception e) {

                        }

                        if (tokens.length > 30) {
                            row.put("name", tokens[28]);
                            row.put("website", tokens[29]);
                            row.put("policy", tokens[30]);
                        }

                        stockList.add(row);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void mapValue(Map<String, String> row, String name, String value) {
        try {
            Float.parseFloat(value);
            row.put(name, value);
        } catch (Exception e) {
            row.put(name, "-");
        }
    }

    public List<Map<String, String>> applyFilter() {

        filteredList.clear();

        boolean match;

        Set<String> filterSet = filterMap.keySet();
        Filter filter;

        for (Map<String, String> map: stockList) {

            match = true;

            for (String name: filterSet) {

                filter = filterMap.get(name);
                try {

                    if (filter.opt.equals("=")) {

                        if (name.equals("Type") && filter.value.equals("Industrials")) {
                            match &= map.get(name).startsWith("Industrial");
                        } else {
                            match &= map.get(name).equals(filter.value);
                        }

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
                filteredList.add(map);
            }

        }

        return filteredList;
    }

    public List<Map<String, String>> filteredList() {
        return filteredList;
    }

    public boolean isXDAfterNow(String symbol) {
        Calendar xd = xdMap.get(symbol);

        if (xd!=null && xd.after(Calendar.getInstance(Locale.US))) {
            return true;
        }

        return false;
    }

    public Map<String, Filter> getFilterMap() {
        return filterMap;
    }

    public Filter getFilter(String name) {
        return filterMap.get(name);
    }

    public void addFilter(String filterName, Filter filter) {
        filterMap.put(filterName, filter);
    }

    public void removeFilter(String filterName) {
        filterMap.remove(filterName);
    }

    public Map<String, String> getStock(String symbol) {
        if (stockList!=null) {
            String val;
            for (Map<String, String> row:stockList) {
                val = row.get("symbol");
                if (val!=null && symbol.equals(val))
                    return row;
            }
        }

        return null;
    }

    public static class Filter {

        public static final int TYPE = 0;
        public static final int VALUE = 1;

        public int type;

        public String name;

        public String opt;

        public String value;

        public Filter (String name, String opt, String value) {
            this.opt = opt;
            this.value = value;
            this.name = name;
            this.type = name.equals("Type")?TYPE:VALUE;
        }
    }

}
