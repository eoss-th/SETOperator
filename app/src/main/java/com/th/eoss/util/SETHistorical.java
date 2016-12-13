package com.th.eoss.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by eossth on 12/12/2016 AD.
 */

public class SETHistorical {

    private static final String CSV_URL = "http://eoss-setfin.appspot.com/his?s=";

    public String getSymbol() {
        return symbol;
    }

    public static class Historical {
        private String asOfDate;
        private float price;
        private float eps;
        private float dvd;

        public Historical(String asOfDate, float price, float eps, float dvd) {
            this.asOfDate = asOfDate;
            this.price = price;
            this.eps = eps;
            this.dvd = dvd;
        }

        public String getAsOfDate() {
            return asOfDate;
        }

        public float getPrice() {
            return price;
        }

        public float getEps() {
            return eps;
        }

        public float getDvd() {
            return dvd;
        }
    }

    private String symbol;

    private List<Historical> historicals;

    public SETHistorical (String symbol) {

        historicals = new ArrayList<>();

        this.symbol = symbol;

        symbol = symbol.replace(" ", "+");
        symbol = symbol.replace("&", "%26");

        try {
            URL url = new URL(CSV_URL + symbol);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(url.openStream()));

            if (br.readLine()!=null) {

                String line;
                String[] tokens;
                Historical historical;
                String asOfDate;
                float price, eps, dvd, dvdPercent;
                while (true) {
                    line = br.readLine();
                    if (line == null) break;
                    tokens = line.split(",");

                    asOfDate = tokens[0];

                    try {
                        price = Float.parseFloat(tokens[11]);
                    } catch (Exception e) {
                        price = 0;
                    }

                    try {
                        eps = Float.parseFloat(tokens[7]);
                    } catch (Exception e) {
                        eps = 0;
                    }

                    try {
                        dvdPercent = Float.parseFloat(tokens[12]);
                        dvd = round((dvdPercent * price) / 100);
                    } catch (Exception e) {
                        dvd = 0;
                    }

                    historicals.add(new Historical(asOfDate, price, eps, dvd));
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    private float round (float num) {
        return (float) (Math.round(num * 100.0)/100.0);
    }

    public List<Historical> historicals() {
        return historicals;
    }

    public String toString() {

        StringBuilder sb = new StringBuilder(symbol+"\n");

        for (Historical historical:historicals) {
            sb.append(historical.asOfDate);
            sb.append("\t");
            sb.append(historical.price);
            sb.append("\t");
            sb.append(historical.eps);
            sb.append("\t");
            sb.append(historical.dvd);
            sb.append("\n");
        }

        return sb.toString();
    }

    public static void main(String[]args) {
        System.out.println(new SETHistorical("JAS"));
        System.out.println();
        System.out.println(new SETHistorical("INTUCH"));
    }

}
