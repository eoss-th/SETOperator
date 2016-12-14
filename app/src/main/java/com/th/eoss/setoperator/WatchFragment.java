package com.th.eoss.setoperator;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.os.Bundle;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.th.eoss.util.Formatter;
import com.th.eoss.util.SETDividend;
import com.th.eoss.util.SETQuote;

public class WatchFragment extends Fragment {


	SimpleAdapter adapter;

    EditText searchText;

    ListView stockListView;

	ToggleButton toggleSpeakButton;

    MainActivity mainActivity() {
        return (MainActivity) getActivity();
    }



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.watch_main, container, false);

		searchText = (EditText) rootView.findViewById(R.id.symbol);

		searchText.setImeOptions(EditorInfo.IME_ACTION_DONE);

		searchText.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

		searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

				if (actionId == EditorInfo.IME_ACTION_DONE) {
					String symbol = searchText.getText().toString().toUpperCase();
					addSymbol(symbol);
					searchText.setText("");
				}

				return false;
			}

		});

		stockListView = (ListView) rootView.findViewById(R.id.listView);

		toggleSpeakButton = (ToggleButton) rootView.findViewById(R.id.toggleButton1);

        toggleSpeakButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {

                if (toggleSpeakButton.isChecked()) {
                    mainActivity().speakBundle("greeting");
                }
            }
        });

        watchList = mainActivity().watchList;

        adapter = new StockAdapter(getActivity(), watchList, R.layout.watch_row, new String[]{"symbol", "price", "change", "dvd", "xd"}, new int[]{R.id.name, R.id.last, R.id.change, R.id.dvd, R.id.xdDate});

		stockListView.setAdapter(adapter);

		stockListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

				Map<String, String> set = watchList.get(i);
				((MainActivity)getActivity()).historical(set);
			}
		});

		stockListView.setKeepScreenOn(true);


		return rootView;
	}

    @Override
    public void onResume() {
        super.onResume();
        mainActivity().startTimer();
    }

    @Override
	public void onPause() {
		super.onPause();
		mainActivity().stopTimer();
	}

    void addSymbol(final String s) {

		if (s==null || s.trim().isEmpty()) return;

        final String symbol = s.trim();

		new Thread() {

			public void run() {
				try {

					for (Map<String, String> map: watchList) {
						if (map.get("symbol").equals(symbol)) {
							return;
						}
					}

					final SETQuote q = new SETQuote(symbol);

					final SETDividend d = new SETDividend(symbol);

                    SingleHandler.handler.post(new Runnable() {

                        @Override
                        public void run() {

                            synchronized (watchList) {
                                try {
                                    Map<String, String> map = new HashMap<String, String>();
                                    map.put("symbol", "" + symbol);
                                    map.put("price", "" + q.last);
                                    map.put("change", "" + q.chgPercent);

                                    map.put("dvd", d.value);
                                    map.put("xd", d.xd);

                                    watchList.add(map);
                                    adapter.notifyDataSetChanged();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
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
		public View getView(final int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);

			Button remove = (Button) v.findViewById(R.id.remove);

			TextView change = (TextView) v.findViewById(R.id.change);
			TextView last = (TextView) v.findViewById(R.id.last);
			TextView xdDate = (TextView) v.findViewById(R.id.xdDate);
			TextView dvd = (TextView) v.findViewById(R.id.dvd);
			TextView dvdPercent = (TextView) v.findViewById(R.id.dvdPercent);

			double val;
			try {
				val = Double.parseDouble("" + change.getText());
			} catch (Exception e) {
				val = 0;
			}

			if (val>0) {

				change.setTextColor(Theme.green);
				last.setTextColor(Theme.green);

			} else if (val<0) {

				change.setTextColor(Theme.red);
				last.setTextColor(Theme.red);

			} else {

				change.setTextColor(Theme.white);
				last.setTextColor(Theme.white);
			}

			if (isBeforeXD(xdDate.getText().toString())) {

				xdDate.setTextColor(Theme.green);

			} else {

				xdDate.setTextColor(Theme.red);

			}

            dvd.setTextColor(Theme.white);

			Map<String, String> map = watchList.get(position);
			String dvdText = map.get("dvd");

			try {
				double percent = Double.parseDouble(dvdText) / Double.parseDouble(last.getText().toString()) * 100;
				dvdPercent.setText(Formatter.decimalFormat.format(percent));
			} catch (Exception e) {
				dvdPercent.setText("");
			}

			dvdPercent.setTextColor(Theme.white);

			remove.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					synchronized (watchList) {
						watchList.remove(position);
						adapter.notifyDataSetChanged();						
					}
				}

			});
			return v;
		}

		DateFormat xdDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);

		boolean isBeforeXD(String dateString) {
			try {
				Calendar date = Calendar.getInstance(Locale.US);
				date.setTime(xdDateFormat.parse(dateString));

				return Calendar.getInstance(Locale.US).before(date);

			} catch (ParseException e) {
				//e.printStackTrace();
			}
			return false;
		}
	}

    @Override
    public void onStop() {
        super.onStop();
        saveWatchList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Don't forget to shutdown textToSpeech!
        stopTTS();
    }

}
