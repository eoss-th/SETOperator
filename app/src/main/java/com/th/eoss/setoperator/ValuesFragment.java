package com.th.eoss.setoperator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.th.eoss.util.SET;

import android.graphics.Color;
import android.os.Bundle;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

public class ValuesFragment extends Fragment implements OnClickListener, FilterListener, SETOperatorListener {

    Map<String, ToggleButton> toggleButtonMap;

	SimpleAdapter adapter;

	ListView stockListView;

	TextView  dateText;

	TextView typeText;

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);

        toggleButtonMap = new HashMap<>();
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.values_main, container, false);

        toggleButtonMap.clear();

		ToggleButton tb;
		tb = (ToggleButton) rootView.findViewById(R.id.type);
		tb.setOnClickListener(this);
        toggleButtonMap.put(tb.getTextOff().toString(), tb);

		tb = (ToggleButton) rootView.findViewById(R.id.roa);
		tb.setOnClickListener(this);
        toggleButtonMap.put(tb.getTextOff().toString(), tb);

		tb = (ToggleButton) rootView.findViewById(R.id.roe);
		tb.setOnClickListener(this);
        toggleButtonMap.put(tb.getTextOff().toString(), tb);

		tb = (ToggleButton) rootView.findViewById(R.id.pe);
		tb.setOnClickListener(this);
        toggleButtonMap.put(tb.getTextOff().toString(), tb);

		tb = (ToggleButton) rootView.findViewById(R.id.pbv);
		tb.setOnClickListener(this);
        toggleButtonMap.put(tb.getTextOff().toString(), tb);

		tb = (ToggleButton) rootView.findViewById(R.id.dvd);
		tb.setOnClickListener(this);
        toggleButtonMap.put(tb.getTextOff().toString(), tb);

		tb = (ToggleButton) rootView.findViewById(R.id.price);
		tb.setOnClickListener(this);
        toggleButtonMap.put(tb.getTextOff().toString(), tb);

		dateText = (TextView) rootView.findViewById(R.id.dateText);

		typeText = (TextView) rootView.findViewById(R.id.typeText);

		stockListView = (ListView) rootView.findViewById(R.id.listView);

        adapter = new StockAdapter(getActivity(), SET.instance().resultList(),
                R.layout.values_row, new String[] { "symbol", "ROA %", "ROE %",
                "P/E", "P/BV", "DVD %", "Price" }, new int[] { R.id.name,
                R.id.roa, R.id.roe, R.id.pe, R.id.pbv, R.id.dvd,
                R.id.price });

		stockListView.setAdapter(adapter);

        stockListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Map<String, String> set = SET.instance().resultList().get(i);
				((MainActivity)getActivity()).historical(set);

            }
        });

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
        load();
    }

	void load() {

		new Thread() {

			public void run() {

				try {

					final String asOfDate = SET.instance().load();

					SingleHandler.handler.post(new Runnable() {

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

				dvd.setTextColor(Theme.green);

			} else {

				dvd.setTextColor(Theme.red);

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

		SET.Filter filter = SET.instance().getFilter(name);

        if (filter==null) {
            filter = new SET.Filter(name, "", "");
        }

        new StockFilterDialog(this.getActivity(), filter, this).show();
    }

    private void updateTogglesText() {
        //Reset Type Text
        typeText.setText("All");

        //Reset All Buttons
        Set<String> keys = toggleButtonMap.keySet();
        ToggleButton toggleButton;
        for (String filterName:keys) {
            toggleButton = toggleButtonMap.get(filterName);

            int white = Color.parseColor("#FFFFFF");
            toggleButton.setTextColor(white);
            toggleButton.setChecked(false);
            toggleButton.setTextOn(toggleButton.getTextOff());
        }

        //Apply ToggleButton
        Map<String, SET.Filter> filterMap = SET.instance().getFilterMap();
        keys = filterMap.keySet();
        SET.Filter filter;
        for (String filterName:keys) {

            filter = filterMap.get(filterName);
            toggleButton = toggleButtonMap.get(filterName);

            if (toggleButton!=null) {

                if (filter.type==SET.Filter.VALUE) {
                    toggleButton.setTextOn(toggleButton.getTextOff()+"\n"
                            +filter.opt
                            +filter.value);
                }
                toggleButton.setChecked(true);
                //toggleButton.setTextColor(orange);
                //toggleButton.setTextColor(blue);
            }

            if (filterName.equals("Type")) {
                typeText.setText(filter.value);
            }
        }
    }

	void applyFilter() {

		SET.instance().applyFilter();

        updateTogglesText();

		adapter.notifyDataSetChanged();
	}

	@Override
	public void onValue(String filterName, String opt, String value) {

		SET.instance().addFilter(filterName, new SET.Filter(filterName, opt, value));

		applyFilter();

	}

	@Override
	public void onClearValue(String filterName) {

        SET.instance().removeFilter(filterName);

		applyFilter();

	}

	@Override
	public void onWatch(String symbol) {
        ((MainActivity)getActivity()).watch(symbol);
	}

	@Override
	public void onExpired() {
	}

}
