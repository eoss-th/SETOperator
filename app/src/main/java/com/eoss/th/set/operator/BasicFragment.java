package com.eoss.th.set.operator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import bean.Member;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

public class BasicFragment extends Fragment implements OnClickListener, FilterListener, SETOperatorListener {

	ListView stockListView;

	List<Map<String, String>> stockList, resultList;

	SimpleAdapter adapter;
	
	Map<String, Calendar> xdMap = new HashMap<String, Calendar>();

	Handler handler = new Handler();

	NumberFormat decimalFormat = new DecimalFormat("0.00");

	Map<String, Filter> filterList = new HashMap<String, Filter>();

	TextView  dateText;

	TextView typeText;	

	DateFormat xdDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);

	List<ToggleButton> buttons = new ArrayList<ToggleButton>();

	class Filter {

		String opt;

		String value;

		Filter (String opt, String value) {
			this.opt = opt;
			this.value = value;
		}
	}

	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);

		stockList = new ArrayList<Map<String, String>>();
		
		resultList = new ArrayList<Map<String, String>>();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View	rootView = inflater.inflate(R.layout.basic_main, container, false);

		buttons.clear();
		ToggleButton tb;

		tb = (ToggleButton) rootView.findViewById(R.id.type);
		buttons.add(tb);
		tb.setOnClickListener(this);

		tb = (ToggleButton) rootView.findViewById(R.id.roa);
		buttons.add(tb);
		tb.setOnClickListener(this);

		tb = (ToggleButton) rootView.findViewById(R.id.roe);
		buttons.add(tb);
		tb.setOnClickListener(this);

		tb = (ToggleButton) rootView.findViewById(R.id.pe);
		buttons.add(tb);
		tb.setOnClickListener(this);

		tb = (ToggleButton) rootView.findViewById(R.id.pbv);
		buttons.add(tb);
		tb.setOnClickListener(this);

		tb = (ToggleButton) rootView.findViewById(R.id.dvd);
		buttons.add(tb);
		tb.setOnClickListener(this);

		tb = (ToggleButton) rootView.findViewById(R.id.price);
		buttons.add(tb);
		tb.setOnClickListener(this);

		dateText = (TextView) rootView.findViewById(R.id.dateText);

		typeText = (TextView) rootView.findViewById(R.id.typeText);

		stockListView = (ListView) rootView.findViewById(R.id.listView);

		adapter = new StockAdapter(getActivity(), resultList,
					R.layout.basic_row, new String[] { "symbol", "ROA", "ROE",
					"P/E", "P/BV", "DVD", "Price" }, new int[] { R.id.name,
					R.id.roa, R.id.roe, R.id.pe, R.id.pbv, R.id.dvd,
					R.id.price });
		
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

	private String loadFake()  throws Exception {

		Scanner sc = new Scanner(getActivity().getResources().openRawResource(R.raw.m));

		StringBuilder buffer = new StringBuilder();

		while (sc.hasNextLine()) {
			buffer.append(sc.nextLine());
		}

		return buffer.toString();
	}

	private String loadURL(String location) throws Exception {
		URL url = new URL(location);
		BufferedReader br = new BufferedReader(
				new InputStreamReader(url.openStream()));

		StringBuilder buffer = new StringBuilder();

		String line;
		while (true) {
			line = br.readLine();
			if (line==null) break;
			buffer.append(line);
		}
		return buffer.toString();
	}



	@Override
	public void onPause() {
		super.onPause();

		for (ToggleButton tb:buttons) {
			tb.setChecked(false);
		}
		filterList.clear();
		resultList.clear();
		adapter.notifyDataSetChanged();
		
	}

	void load() {

		new Thread() {

			public void run() {

				try {

					String buffer = loadURL("http://setoperator.appspot.com/snapshot?email=" + Member.instance().getEmail());

					if (buffer.equals("expired") || buffer.equals("invalid")) {

						handler.post(new Runnable() {

							@Override
							public void run() {
								onExpired();
							}

						});
						return;
					}
					//String buffer = loadFake();

					JSONObject obj = new JSONObject(buffer);

					String date = obj.getString("date");
					final String dateString = "As Of " + date;


					JSONArray data = obj.getJSONArray("data");

					int len = data.length();

					JSONArray row;
					Map<String, String> map;
					String tmp;

					stockList.clear();
					xdMap.clear();

					for (int i = 0; i < len; i++) {

						row = data.getJSONArray(i);

						map = new HashMap<String, String>();
						map.put("symbol", row.getString(0));
						map.put("Type", row.getString(1));

						tmp = row.getString(2);
						if (tmp.equals("NaN"))
							map.put("ROA", "-");
						else
							map.put("ROA", tmp);

						tmp = row.getString(3);
						if (tmp.equals("NaN"))
							map.put("ROE", "-");
						else
							map.put("ROE", tmp);

						tmp = row.getString(4);
						if (tmp.equals("NaN"))
							map.put("P/E", "-");
						else
							map.put("P/E", tmp);

						tmp = row.getString(5);
						if (tmp.equals("NaN"))
							map.put("P/BV", "-");
						else
							map.put("P/BV", tmp);

						tmp = row.getString(6);
						if (tmp.equals("NaN"))
							map.put("DVD", "-");
						else
							map.put("DVD", tmp);

						tmp = row.getString(7);
						if (tmp.equals("NaN"))
							map.put("Price", "-");
						else
							map.put("Price", tmp);

						tmp = row.getString(8);

						if (tmp!=null && tmp.equals("null") == false) {

							Calendar xdate = Calendar.getInstance(Locale.US);
							xdate.setTime(xdDateFormat.parse(tmp));							
							xdMap.put(map.get("symbol"), xdate);
						}

						stockList.add(map);
					}

					handler.post(new Runnable() {

						@Override
						public void run() {

							dateText.setText(dateString);							
							applyFilter();
								
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

		public StockAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
						String[] from, int[] to) {
			super(context, data, resource, from, to);
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			View v = super.getView(position, convertView, parent);

			TextView tv = (TextView) v.findViewById(R.id.name);

			final String symbol = tv.getText().toString();

			TextView dvd = (TextView) v.findViewById(R.id.dvd);

			Calendar xd = xdMap.get(symbol);

			if (xd!=null && xd.after(Calendar.getInstance(Locale.US))) {

				dvd.setTextColor(green);

			} else {

				dvd.setTextColor(red);

			}			

			Button b = (Button) ((ViewGroup) v).findViewById(R.id.plus);

			b.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					onWatch(symbol);

				}

			});
			return v;
		}

	}

	@Override
	public void onClick(View v) {

		ToggleButton tb = (ToggleButton) v;

		String name = tb.getTextOff().toString();

		Filter filter = filterList.get(name);

		if (filter!=null) {

			tb.setChecked(true);
			new StockFilterDialog(this.getActivity(), tb, this, filter.opt, filter.value).show();				

		} else {

			tb.setChecked(false);
			new StockFilterDialog(this.getActivity(), tb, this, "", "").show();	

		}

	}

	void applyFilter() {

		boolean match;

		Set<String> filterSet = filterList.keySet();
		Filter filter;
		
		resultList.clear();

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

		adapter.notifyDataSetChanged();
	}

	@Override
	public void onValue(String filterName, String opt, String value) {

		filterList.put(filterName, new Filter(opt, value));

		applyFilter();

		if (filterName.equals("Type")) {
			typeText.setText(value);
		}

	}

	@Override
	public void onClearValue(String filterName) {

		filterList.remove(filterName);

		applyFilter();

		if (filterName.equals("Type")) {
			typeText.setText("All");
		}

	}

	@Override
	public void onWatch(String symbol) {
		MainActivity.actionBar.setSelectedNavigationItem(0);
		MainActivity.realtimeFragment.addSymbol(symbol);
	}

	@Override
	public void onExpired() {
		MainActivity.actionBar.setSelectedNavigationItem(3);
	}

	private void popup(String message) {

		AlertDialog.Builder altDialog= new AlertDialog.Builder(getActivity());
		altDialog.setMessage(message);
		altDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		altDialog.show();						

	}


}
