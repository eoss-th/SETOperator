package com.th.eoss.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by wisarut on 28/9/2559.
 */

public class SETQuote {

    public static final String URL = "http://marketdata.set.or.th/mkt/stockquotation.do?language=en&country=US&symbol=";

    public static final DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm:ss", Locale.US);

    public static final DateFormat dateFormat2 = new SimpleDateFormat("dd MMM yyyy", Locale.US);

    public static final NumberFormat numberFormat = new DecimalFormat("#,##0.00");

    public String symbol;

    public String date;

    public boolean marketOpen;

    public float prior;

    public float open;

    public float high;

    public float low;

    public float last;

    public float chg;

    public float chgPercent;

    public long volume;

    public SETQuote(String symbol) {
        this.symbol = symbol;

        symbol = symbol.replace(" ", "+");
        symbol = symbol.replace("&", "%26");

        try {
            Document doc = Jsoup.connect(URL+symbol).get();
            Element table = doc.select("table[class='table table-hover table-set col-3-center table-set-border-yellow']").first();
            if (table==null) return;
            Element head = table.child(0);
            Element body = table.child(1);
            try {
                this.date = dateFormat2.format(dateFormat.parse(head.child(0).child(0).text().replace("* Last Update", "").trim()));
            } catch (Exception e) {

            }

            try {
                String marketStatus = head.child(1).child(0).text().trim();
                marketOpen = marketStatus.contains("Open(I)") || marketStatus.contains("Open(II)");
            } catch (Exception e) {

            }

            try {
                this.prior = numberFormat.parse(body.child(4).child(1).text()).floatValue();
            } catch (Exception e) {

            }

            try {
                this.last = numberFormat.parse(body.child(1).child(1).text()).floatValue();
            } catch (Exception e) {
                this.last = this.prior;
            }

            try {
                this.open = numberFormat.parse(body.child(5).child(1).text()).floatValue();
            } catch (Exception e) {
                this.open = this.prior;
            }

            try {
                this.high = numberFormat.parse(body.child(6).child(1).text()).floatValue();
            } catch (Exception e) {

            }

            try {
                this.low = numberFormat.parse(body.child(7).child(1).text()).floatValue();
            } catch (Exception e) {

            }

            try {
                this.volume = numberFormat.parse(body.child(8).child(1).text()).longValue();
            } catch (Exception e) {

            }

            if (marketOpen) {

                this.chg = round(this.last - this.open);
                this.chgPercent = round(this.chg * 100 / this.open);

            } else {

                try {
                    this.chg = numberFormat.parse(body.child(2).child(1).text()).floatValue();
                } catch (Exception e) {

                }

                try {
                    this.chgPercent = numberFormat.parse(body.child(3).child(1).text()).floatValue();
                } catch (Exception e) {

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("\t" + this.symbol);
        }
    }

    private float round (float num) {
        return (float) (Math.round(num * 100.0)/100.0);
    }

    public String toString() {
        return String.format("%s %s open=%.2f\thigh=%.2f\tlow=%.2f\tclose=%.2f\tchg=%.2f\tvolume=%d",symbol,date,open,high,low,last,chg,volume);
    }

    public static void main(String[]args) {
        System.out.println(new SETQuote("INTUCH").marketOpen);
    }

}
