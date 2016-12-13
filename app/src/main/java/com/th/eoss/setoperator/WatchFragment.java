package com.th.eoss.setoperator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URLEncoder;
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

import android.media.AudioManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.Fragment;
import android.util.Log;
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

import com.th.eoss.util.Formatter;
import com.th.eoss.util.SETDividend;
import com.th.eoss.util.SETQuote;
import com.th.eoss.util.SpeechBundle;

public class WatchFragment extends Fragment implements TextToSpeech.OnInitListener {

    static Timer timer;

	static TextToSpeech textToSpeech;

	static List<Map<String, String>> stockList;

	static SimpleAdapter adapter;

    SpeechBundle speechBundle;

	ListView stockListView;

	ToggleButton toggleSpeakButton;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		if (textToSpeech ==null) {

			textToSpeech = new TextToSpeech(context, this, "com.google.android.textToSpeech");

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

		}

		load();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.watch_main, container, false);

		stockListView = (ListView) rootView.findViewById(R.id.listView);

		toggleSpeakButton = (ToggleButton) rootView.findViewById(R.id.toggleButton1);

		if (adapter==null) {
			adapter = new StockAdapter(getActivity(), stockList, R.layout.watch_row, new String[]{"symbol", "price", "change", "dvd", "xd"}, new int[]{R.id.name, R.id.last, R.id.change, R.id.dvd, R.id.xdDate});
		}

		stockListView.setAdapter(adapter);

		stockListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

				Map<String, String> set = stockList.get(i);
				MainActivity.viewPager.setCurrentItem(3);
				MainActivity.historicalFragment.load(set);

			}
		});

		stockListView.setKeepScreenOn(true);

		return rootView;
	}

    @Override
	public void onPause() {
		super.onPause();
		stopTimer();		
	}

	@Override
	public void onResume() {
		super.onResume();
		startTimer();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Don't forget to shutdown textToSpeech!
		stopTTS();
	}
	
	private void serverUpdateSymbolList() {
		
		new Thread() {
			
			public void run() {
				
				try {
					
					StringBuilder sb = new StringBuilder("&");
					
					synchronized (stockList) {
						
						for (Map<String, String> m:stockList) {
							
							String symbol = m.get("symbol")	;
							
							sb.append("symbols=" + URLEncoder.encode(symbol, "UTF-8"));
							sb.append("&");
							
						}
						
					}

					//TODO: Update Statistics

				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			
		}.start();
		
	}

	@Override
	public void onStop() {
		super.onStop();
		save();		
	}

	private void load() {

		if (stockList==null) {

			try {

				FileInputStream fi = this.getActivity().openFileInput("data.ser");

				ObjectInputStream oi = new ObjectInputStream(fi);
				stockList = (List<Map<String, String>>) oi.readObject();

			} catch (Exception e) {
				stockList = new ArrayList<Map<String, String>>();
			}

		}

	}

	private void save() {

		try {

			FileOutputStream fout = getActivity().openFileOutput("data.ser", Activity.MODE_PRIVATE);

			ObjectOutputStream os = new ObjectOutputStream(fout);
			os.writeObject(stockList);

			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void stopTTS() {

		if (textToSpeech != null) {
			textToSpeech.stop();
			textToSpeech.shutdown();
			textToSpeech = null;
		}

	}

	public void stopTimer() {

		if (timer!=null) {
			timer.cancel();
			timer = null;			
		}

	}

	private SETQuote load(String symbol) throws Exception {
		return new SETQuote(symbol);
	}

	private SETDividend loadXD(String symbol) throws Exception {
		return new SETDividend(symbol);
	}

	public void startTimer() {

		timer = new Timer();

		timer.schedule(new TimerTask() {

			@Override
			public void run() {

				List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();

				synchronized (stockList) {

					for (Map<String, String> s:stockList) {
						resultList.add(s);
                        Log.i("Symbol", s.get("symbol"));
					}
				}

				final StringBuilder sb = new StringBuilder();

				for (Map<String, String> s:resultList) {

					try {

						SETQuote q = load(s.get("symbol"));

						if (q.last==0.0) continue; //Connection Error, Skip
						
						s.put("price", "" + q.last);
						s.put("change", Formatter.decimalFormat.format(q.chg));

						if (q.chg != 0) {

							String symbol = s.get("symbol");

							String t = "";
							if (symbol.length() < 5) {

								for (int i=0; i<symbol.length(); i++) {
									t += symbol.charAt(i) + "  ";
								}

							} else {
								t = symbol;
							}

							if (q.chg > 0)
    							sb.append(t + " " + speechBundle.get("up") + " " + s.get("price") + " " + speechBundle.get("bath"));
							else if (q.chg < 0)
                                sb.append(t + " " + speechBundle.get("down") + " " + s.get("price") + " " + speechBundle.get("bath"));

							sb.append(", ");

						}

					} catch (Exception e) {
						e.printStackTrace();
                        Log.e("Error", e.getMessage());
						break;

					}

				}//End loop

				SingleHandler.handler.post(new Runnable() {

					@Override
					public void run() {

						adapter.notifyDataSetChanged();

						if ( toggleSpeakButton.isChecked() && sb.length()>0 ) {

							/*
							HashMap<String, String> myHashAlarm = new HashMap<String, String>();
							myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_NOTIFICATION));
							myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "" + System.currentTimeMillis());
							textToSpeech.speak(sb.toString(), TextToSpeech.QUEUE_ADD, myHashAlarm);
							*/

							textToSpeech.speak(sb.toString(), TextToSpeech.QUEUE_ADD, null, "");

						}
					}

				});	

			}


		}, 0,  60 * 1000 * 1);

	}

	private void update(final String symbol, final SETQuote q, final SETDividend xd) {

        SingleHandler.handler.post(new Runnable() {

			@Override
			public void run() {

				synchronized (stockList) {
					try {
						Map<String, String> map = new HashMap<String, String>();
						map.put("symbol", "" + symbol);
						map.put("price", "" + q.last);
						map.put("change", "" + q.chg);

						if (xd!=null) {
                            map.put("dvd", xd.value);
							map.put("xd", xd.xd);
						}

						stockList.add(map);
						adapter.notifyDataSetChanged();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

	}

	public void addSymbol(final String s) {

		if (s==null || s.trim().isEmpty()) return;

        final String symbol = s.trim();

		new Thread() {

			public void run() {
				try {

					for (Map<String, String> map:stockList) {
						if (map.get("symbol").equals(symbol)) {
							return;
						}
					}

					final SETQuote q = load(symbol);

					final SETDividend d = loadXD(symbol);

					update(symbol, q, d);

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

			Map<String, String> map = stockList.get(position);
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

					synchronized (stockList) {
						stockList.remove(position);
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

	private void speakOut() {

		HashMap<String, String> myHashAlarm = new HashMap<String, String>();

		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_NOTIFICATION));
		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "" + System.currentTimeMillis());

		textToSpeech.speak(speechBundle.get("greeting"), TextToSpeech.QUEUE_ADD, null, "");
	}

	@Override
	public void onInit(int status) {

		if (status == TextToSpeech.SUCCESS) {

			int result = textToSpeech.setLanguage(new Locale("th"));

			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {

				Log.e("TTS", "This Language is not supported");

                textToSpeech.setLanguage(Locale.US);
                speechBundle = SpeechBundle.create("en");

			} else {
                speechBundle = SpeechBundle.create("th");
			}

            speakOut();

		} else {
			Log.e("TTS", "Initilization Failed!");
		}

	}

}
