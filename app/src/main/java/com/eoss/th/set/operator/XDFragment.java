package com.eoss.th.set.operator;

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

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * 
 * @author eoss-th
 *
 *
 */
public class XDFragment extends Fragment implements SETOperatorListener {

	ListView stockListView;

	List<Map<String, String>> stockList, resultList;

	SimpleAdapter adapter;

	Map<String, Calendar> xdMap = new HashMap<String, Calendar>();

	Handler handler = new Handler();

	DateFormat xdDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);

	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);

		stockList = new ArrayList<Map<String, String>>();
		
		resultList = new ArrayList<Map<String, String>>();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.xd_main, container, false);

		stockListView = (ListView) rootView.findViewById(R.id.listView);
		
		adapter = new StockAdapter(getActivity(), resultList, R.layout.xd_row, new String[] { "symbol", "date" }, new int[] { R.id.name, R.id.date});
			
		stockListView.setAdapter(adapter);
		
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (resultList.isEmpty()) {
			
			load();
			
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		
		resultList.clear();
		adapter.notifyDataSetChanged();
	}

	void load() {

		new Thread() {

			public void run() {

				try {

						URL url = new URL("http://feeds.feedburner.com/Setorth-XD-en");
						BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

						String line;
						Map<String, String> map;
						stockList.clear();
						xdMap.clear();

						while (true) {
							line = br.readLine();
							if (line==null) break;
							if (line.contains("<item>")) {
								br.readLine();
								line = br.readLine().trim();
								line = line.replace("<title>", "");
								line = line.replace("</title>", "");

								map = new HashMap<String, String>();
								map.put("symbol", line.substring(0, line.indexOf(" -")));
								map.put("date", line.substring(line.indexOf(": ") + 2));

								Calendar date = Calendar.getInstance(Locale.US);
								date.setTime(xdDateFormat.parse(map.get("date")));							
								xdMap.put(map.get("symbol"), date);

								stockList.add(map);
								
							}
														
						}
						
						handler.post(new Runnable() {

							@Override
							public void run() {

								resultList.clear();

								for (Map<String, String> map: stockList) {
									resultList.add(map);
								}
								
								adapter.notifyDataSetChanged();

							}

						});
						


				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}.start();

	}

	class StockAdapter extends SimpleAdapter {

		int red = getResources().getColor(android.R.color.holo_red_light);
		int green = getResources().getColor(android.R.color.holo_green_light);
		int white = Color.parseColor("#FFFFFF");
		int blue = getResources().getColor(android.R.color.holo_blue_light);

		public StockAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
						String[] from, int[] to) {			
			super(context, data, resource, from, to);
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			View v = super.getView(position, convertView, parent);

			TextView name = (TextView) v.findViewById(R.id.name);

			final String symbol = name.getText().toString();

			Button plus = (Button) ((ViewGroup) v).findViewById(R.id.plus);

			plus.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					onWatch(symbol);
				}

			});

			TextView xd = (TextView) v.findViewById(R.id.date);

			Calendar x = xdMap.get(symbol);

			if (x!=null) {

				if (x.after(Calendar.getInstance(Locale.US))) {

					xd.setTextColor(green);

				} else {

					xd.setTextColor(red);

				}			

			}

			return v;
		}		

	}

	@Override
	public void onWatch(String symbol) {
		MainActivity.actionBar.setSelectedNavigationItem(0);
		MainActivity.realtimeFragment.addSymbol(symbol);
	}

	@Override
	public void onExpired() {

	}

}
