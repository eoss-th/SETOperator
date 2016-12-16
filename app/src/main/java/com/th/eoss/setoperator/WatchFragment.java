package com.th.eoss.setoperator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
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
import com.th.eoss.util.SET;
import com.th.eoss.util.SETDividend;
import com.th.eoss.util.SETQuote;

public class WatchFragment extends Fragment implements TextToSpeech.OnInitListener {

	List<Map<String, String>> watchList;

    TextToSpeech textToSpeech;

	Timer timer;

	SpeechBundle speechBundle;

	SimpleAdapter adapter;

    EditText searchText;

    ListView stockListView;

	ToggleButton toggleSpeakButton;

    MainActivity mainActivity() {
        return (MainActivity) getActivity();
    }

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

        Log.v("WATCH", "Attached");

        this.textToSpeech = new TextToSpeech(context, this, "com.google.android.textToSpeech");

        loadWatchList();
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
                    speechBundle.speak("greeting");
                }
            }
        });

        adapter = new StockAdapter(getActivity(), watchList, R.layout.watch_row, new String[]{"symbol", "price", "change", "dvd", "xd"}, new int[]{R.id.name, R.id.last, R.id.change, R.id.dvd, R.id.xdDate});

		stockListView.setAdapter(adapter);

		stockListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

				mainActivity().historical(watchList.get(i).get("symbol"));
			}
		});

		stockListView.setKeepScreenOn(true);


		return rootView;
	}

	@Override
	public void onInit(int status) {

		if (status == TextToSpeech.SUCCESS) {

			int result = textToSpeech.setLanguage(new Locale("th"));

			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {

				Log.e("TTS", "This Language is not supported");

				textToSpeech.setLanguage(Locale.US);
				speechBundle = SpeechBundle.create(textToSpeech, "en");

			} else {
				speechBundle = SpeechBundle.create(textToSpeech, "th");
			}

			textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {

				@Override
				public void onStart(String s) {
					SingleHandler.handler.post(new Runnable() {

						@Override
						public void run() {
							toggleSpeakButton.setEnabled(false);
							toggleSpeakButton.setBackgroundResource(R.drawable.bot_speaking);
						}

					});
				}

				@Override
				public void onDone(String s) {
					SingleHandler.handler.post(new Runnable() {

						@Override
						public void run() {
							toggleSpeakButton.setBackgroundResource(R.drawable.bot);
							toggleSpeakButton.setEnabled(true);
						}

					});
				}

				@Override
				public void onError(String s) {

				}
			});

		} else {
			Log.e("TTS", "Initilization Failed!");
			toggleSpeakButton.setEnabled(false);
		}
	}

	void startTimer() {

        if (timer==null) {
            timer = new Timer();

            timer.schedule(new TimerTask() {

                private boolean taskStarted;
                private boolean lastMarketStatusIsClosed;

                @Override
                public void run() {

                    List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();

                    synchronized (watchList) {

                        for (Map<String, String> s: watchList) {
                            resultList.add(s);
                        }
                    }

                    final StringBuilder sb = new StringBuilder();

                    boolean marketOpen = false;
                    for (Map<String, String> s:resultList) {

                        try {

                            SETQuote q = new SETQuote(s.get("symbol"));

                            marketOpen |= q.marketOpen;

                            if (q.last==0.0) continue; //Connection Error, Skip

                            s.put("price", "" + q.last);
                            s.put("change", Formatter.decimalFormat.format(q.chgPercent));

                            if (q.chg != 0) {

                                String symbol = s.get("symbol");

                                String t = "";
                                if (symbol.length() <= 3) {

                                    for (int i=0; i<symbol.length(); i++) {
                                        t += symbol.charAt(i) + "  ";
                                    }

                                } else {
                                    t = symbol;
                                }

                                if (q.chg > 0)
                                    sb.append(t + " " + speechBundle.get("up") + " " + q.chg + " " + speechBundle.get("bath") + ", " + speechBundle.get("price") + " " + q.last + " " + speechBundle.get("bath"));
                                else if (q.chg < 0)
                                    sb.append(t + " " + speechBundle.get("down") + " " + Math.abs(q.chg) + " " + speechBundle.get("bath") + ", " + speechBundle.get("price") + " " + q.last + " " + speechBundle.get("bath"));

                                sb.append(",  ");

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("Error", e.getMessage());
                            break;

                        }

                    }//End loop

                    final boolean isMarketClosed = !marketOpen;
                    SingleHandler.handler.post(new Runnable() {

                        @Override
                        public void run() {

                            adapter.notifyDataSetChanged();

                            //Wakeup when just open
                            if ((taskStarted==false || lastMarketStatusIsClosed) && isMarketClosed==false && toggleSpeakButton.isChecked()==false ) {

                                speechBundle.speak("wake");
                                toggleSpeakButton.setChecked(true);
                            }

                            if (isMarketClosed && toggleSpeakButton.isChecked()) {

                                speechBundle.speak("sleeping");
                                toggleSpeakButton.setChecked(false);
                            }

                            if ( toggleSpeakButton.isChecked() && sb.length()>0 ) {

                                speechBundle.speak(sb.toString());

                            }

                            //Goodbye when just closed
                            if (lastMarketStatusIsClosed==false && isMarketClosed && toggleSpeakButton.isChecked() ) {

                                speechBundle.speak("goodbye");
                                toggleSpeakButton.setChecked(false);
                            }

                            lastMarketStatusIsClosed = isMarketClosed;
                            taskStarted = true;
                        }

                    });

                }


            }, 0,  60 * 1000 * 1);
        }

	}

    @Override
    public void onResume() {
        super.onResume();
        startTimer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (timer!=null) {
            timer.cancel();
        }

        // Don't forget to shutdown textToSpeech!
        textToSpeech.stop();
        textToSpeech.shutdown();

        saveWatchList();
    }

    void addSymbol(final String s) {

		if (s==null || s.trim().isEmpty()) return;

		final String symbol = s.trim();

		if (SET.instance().getStock(s)==null) {
			mainActivity().toast("Symbol Not Found!");
			return;
		}

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

    void loadWatchList() {

        try {
            FileInputStream fi = getActivity().openFileInput("data.ser");

            ObjectInputStream oi = new ObjectInputStream(fi);
            watchList = (List<Map<String, String>>) oi.readObject();

        } catch (Exception e) {
            watchList =  new ArrayList<>();
        }
    }

    void saveWatchList() {

        try {

            FileOutputStream fout = getActivity().openFileOutput("data.ser", Activity.MODE_PRIVATE);

            ObjectOutputStream os = new ObjectOutputStream(fout);
            os.writeObject(watchList);

            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
