package com.th.eoss.setoperator;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.th.eoss.util.SET;

import android.os.Bundle;
import android.os.Handler;
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

	SimpleAdapter adapter;

	Handler handler = new Handler();

	NumberFormat decimalFormat = new DecimalFormat("0.00");

	Map<String, SET.Filter> filterList = new HashMap<>();

	TextView  dateText;

	TextView typeText;	

	List<ToggleButton> buttons = new ArrayList<>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		SET.instance().init();

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

		adapter = new StockAdapter(getActivity(), SET.instance().resultList(),
					R.layout.basic_row, new String[] { "symbol", "ROA", "ROE",
					"P/E", "P/BV", "DVD", "Price" }, new int[] { R.id.name,
					R.id.roa, R.id.roe, R.id.pe, R.id.pbv, R.id.dvd,
					R.id.price });
		
		stockListView.setAdapter(adapter);
				
		return rootView;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		SET.instance().init();
	}

	@Override
	public void onResume() {
		super.onResume();
		
		if (SET.instance().resultList().isEmpty()) {
			
			load();
			
		}
		
	}

	@Override
	public void onPause() {
		super.onPause();

		for (ToggleButton tb:buttons) {
			tb.setChecked(false);
		}
		filterList.clear();
		SET.instance().clearResult();
		adapter.notifyDataSetChanged();
		
	}

	void load() {

		new Thread() {

			public void run() {

				try {

					final String asOfDate = SET.instance().load();

					handler.post(new Runnable() {

						@Override
						public void run() {

							dateText.setText(asOfDate);
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

			if (SET.instance().isXDAfterNow(symbol)) {

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

		SET.Filter filter = filterList.get(name);

		if (filter!=null) {

			tb.setChecked(true);
			new StockFilterDialog(this.getActivity(), tb, this, filter.opt, filter.value).show();				

		} else {

			tb.setChecked(false);
			new StockFilterDialog(this.getActivity(), tb, this, "", "").show();	

		}

	}

	void applyFilter() {

		SET.instance().clearResult();

		SET.instance().applyFilter(filterList);

		adapter.notifyDataSetChanged();
	}

	@Override
	public void onValue(String filterName, String opt, String value) {

		filterList.put(filterName, new SET.Filter(opt, value));

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
