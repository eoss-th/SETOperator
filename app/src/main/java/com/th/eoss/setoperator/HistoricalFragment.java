package com.th.eoss.setoperator;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.th.eoss.util.SET;
import com.th.eoss.util.SETHistorical;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author eoss-th
 *
 *
 */
public class HistoricalFragment extends Fragment {

	CombinedChart combinedChart;
    TextView website;
	TextView policyText;

    SETHistorical selectedHistorical;

    String selectedName, selectedWebsite, selectedPolicy;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.chart, container, false);

		combinedChart = (CombinedChart) rootView.findViewById(R.id.combinedChart);

		combinedChart.getAxisLeft().setDrawLabels(true);
		combinedChart.getAxisLeft().setDrawGridLines(false);
		combinedChart.getAxisLeft().setTextColor(Theme.white);

		combinedChart.getAxisRight().setDrawLabels(true);
		combinedChart.getAxisRight().setDrawGridLines(false);
		combinedChart.getAxisRight().setTextColor(Theme.white);

		combinedChart.getXAxis().setDrawGridLines(false);
		combinedChart.getXAxis().setTextColor(Theme.white);
		combinedChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

		combinedChart.getDescription().setTextColor(Theme.white);
        combinedChart.getDescription().setText("");
        combinedChart.setNoDataText("Please select the symbol.");
        combinedChart.setNoDataTextColor(Theme.cyan);

        website = (TextView) rootView.findViewById(R.id.website);
		policyText = (TextView) rootView.findViewById(R.id.dvdPolicyText);

        policyText.setText("");
        website.setText("EOSS-TH");
        website.setEnabled(false);
        website.setPaintFlags(website.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		reloadChart();
	}

	void load(final String symbol) {

        Map<String, String> stock = SET.instance().getStock(symbol);
        this.selectedName = stock.get("name");
        this.selectedWebsite = stock.get("website");
        this.selectedPolicy = stock.get("policy");

        combinedChart.setNoDataText("Loading...");

		new Thread() {

			public void run() {

                selectedHistorical = new SETHistorical(symbol);

                SingleHandler.handler.post(new Runnable() {

					@Override
					public void run() {
						reloadChart();
					}

				});

			}

		}.start();

	}

	private void reloadChart() {

		if (selectedHistorical!=null ) {

			combinedChart.clear();

			List<SETHistorical.Historical> historicals = selectedHistorical.historicals();

			final List<String> asOfDates = new ArrayList<>();
			List<BarEntry> priceEntries = new ArrayList<>();
			List<BarEntry> epsEntries = new ArrayList<>();
			List<BarEntry> dvdEntries = new ArrayList<>();
            List<BarEntry> dvdEpsEntries = new ArrayList<>();

			float x = 0;
			for (SETHistorical.Historical historical:historicals) {
				asOfDates.add(historical.getAsOfDate());
				priceEntries.add(new BarEntry(x, historical.getPrice()));
				epsEntries.add(new BarEntry(x, historical.getEps()));
				dvdEntries.add(new BarEntry(x, historical.getDvd()));
                dvdEpsEntries.add(new BarEntry(x, new float[]{historical.getDvd(), historical.getEps()}));
				x ++;
			}

			BarDataSet priceDataSet = new BarDataSet(priceEntries, "Price");
			priceDataSet.setColor(Theme.darkBlue);
			priceDataSet.setDrawValues(false);

			BarDataSet epsDataSet = new BarDataSet(epsEntries, "EPS");
			epsDataSet.setColor(Theme.cyan);
			epsDataSet.setDrawValues(false);

			BarDataSet dvdDataSet = new BarDataSet(dvdEntries, "DVD");
			dvdDataSet.setColor(Theme.orange);

            BarDataSet dvdEpsDataSet = new BarDataSet(dvdEpsEntries, "");
            dvdEpsDataSet.setStackLabels(new String[]{"DVD", "EPS"});
            dvdEpsDataSet.setColors(Theme.orange, Theme.cyan);

			BarData barData = new BarData();
            /*
			barData.addDataSet(priceDataSet);
			barData.addDataSet(epsDataSet);
			barData.addDataSet(dvdDataSet);
			*/
            barData.addDataSet(dvdEpsDataSet);
			barData.setBarWidth(0.5f);

			CombinedData data = new CombinedData();
			data.setData(barData);

			combinedChart.setData(data);

			combinedChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {

				@Override
				public String getFormattedValue(float value, AxisBase axis) {
					if ( value>=0 && value<asOfDates.size())
						return asOfDates.get((int) value);
					return "";
				}

			});

			combinedChart.getAxisLeft().setAxisMinimum(0);
			combinedChart.getAxisLeft().setAxisMaximum(barData.getYMax() * 1.1f);

			combinedChart.getXAxis().setAxisMinimum(-1);
			combinedChart.getXAxis().setAxisMaximum(asOfDates.size());

			combinedChart.getData().setValueTextColor(Theme.white);
			combinedChart.getLegend().setTextColor(Theme.white);

			combinedChart.invalidate();

            policyText.setText(selectedPolicy);

            final String url = selectedWebsite;
            website.setText(selectedName);
            website.setEnabled(url!=null && url.trim().isEmpty()==false);

            website.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String urlString="http://" + url.trim();

                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setPackage("com.android.chrome");
                    try {
                        getContext().startActivity(intent);
                    } catch (ActivityNotFoundException ex) {
                        intent.setPackage(null);
                        getContext().startActivity(intent);
                    }
                }
            });
		}

	}

}
