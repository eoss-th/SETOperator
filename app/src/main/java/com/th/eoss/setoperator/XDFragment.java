package com.th.eoss.setoperator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

import com.th.eoss.util.Formatter;

/**
 * 
 * @author eoss-th
 *
 *
 */
public class XDFragment extends Fragment implements SETOperatorListener {

	List<Map<String, String>> xdList = new ArrayList<>();

	Map<String, Calendar> xdMap = new HashMap<>();

	SimpleAdapter adapter;

	ListView stockListView;

	MainActivity mainActivity() {
		return (MainActivity) getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.xd_main, container, false);

		stockListView = (ListView) rootView.findViewById(R.id.listView);

		adapter = new StockAdapter(getActivity(), xdList, R.layout.xd_row, new String[] { "symbol", "date" }, new int[] { R.id.name, R.id.date});

		stockListView.setAdapter(adapter);

		stockListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                mainActivity().historical(xdList.get(i).get("symbol"));
            }
        });

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (xdList.isEmpty()) {
			
			load();
			
		}

	}

	@Override
	public void onPause() {
		super.onPause();

        xdList.clear();
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
						xdList.clear();
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
								date.setTime(Formatter.xdDateFormat.parse(map.get("date")));
								xdMap.put(map.get("symbol"), date);

								xdList.add(map);
								
							}
														
						}
						
						SingleHandler.handler.post(new Runnable() {

							@Override
							public void run() {

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

			Button plus = (Button) v.findViewById(R.id.plus);

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

					xd.setTextColor(Theme.green);

				} else {

					xd.setTextColor(Theme.red);

				}			

			}

			return v;
		}		

	}

	@Override
	public void onWatch(String symbol) {
        mainActivity().watch(symbol);
	}

	@Override
	public void onExpired() {

	}

}
