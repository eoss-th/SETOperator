package com.eoss.th.set.operator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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

import org.json.JSONObject;

import bean.Member;
import bean.StockQuote;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

public class RealtimeFragment extends Fragment implements
TextToSpeech.OnInitListener, OnUtteranceCompletedListener {

	static TextToSpeech tts;

	ListView stockListView;

	static List<Map<String, String>> stockList;

	static SimpleAdapter adapter;

	Timer timer;

	Handler handler = new Handler();

	NumberFormat decimalFormat = new DecimalFormat("0.00");

	ToggleButton toggleSpeakButton;

	EditText editSymbolText;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);		

		if (tts==null) {

			tts = new TextToSpeech(this.getActivity(), this);

			tts.setOnUtteranceCompletedListener(this);

		}

		load();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.operation_main, container, false);

		stockListView = (ListView) rootView.findViewById(R.id.listView);

		toggleSpeakButton = (ToggleButton) rootView.findViewById(R.id.toggleButton1);

		editSymbolText = (EditText) rootView.findViewById(R.id.symbol);	

		editSymbolText.setImeOptions(EditorInfo.IME_ACTION_DONE);		

		editSymbolText.setOnEditorActionListener(new TextView.OnEditorActionListener() { 
			@Override 
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) { 
				if (actionId == EditorInfo.IME_ACTION_DONE) { 

					addSymbol(v);

				} 
				return false; 
			}

		}); 		

		Button addSymbolButton = (Button) rootView.findViewById(R.id.addSymbolButton);

		addSymbolButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				addSymbol(v);

			}

		});

		if (adapter==null) {
			adapter = new StockAdapter(getActivity(), stockList, R.layout.stock_row, new String[]{"symbol", "price", "change", "xd"}, new int[]{R.id.name, R.id.last, R.id.change, R.id.xdDate});			
		}

		stockListView.setAdapter(adapter);

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
		// Don't forget to shutdown tts!
		stopTTS();
		
		serverUpdateSymbolList();		
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
					
					URL url = new URL("http://setoperator.appspot.com/member?email=" + Member.instance().getEmail() + sb.toString());
					BufferedReader br = new BufferedReader(
							new InputStreamReader(url.openStream()));

					StringBuilder buffer = new StringBuilder();

					String line;
					while (true) {
						line = br.readLine();
						if (line==null) break;
						buffer.append(line);
					}
					
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

		if (tts != null) {
			tts.stop();
			tts.shutdown();
			tts = null;
		}

	}

	public void stopTimer() {

		if (timer!=null) {
			timer.cancel();
			timer = null;			
		}

	}

	private JSONObject _load(String symbol) throws Exception {
		URL url = new URL("http://eoss-operator.appspot.com/quote?s=" + URLEncoder.encode(symbol, "UTF-8"));
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

		JSONObject obj = new JSONObject(br.readLine());
		return obj;
	}

	private StockQuote load(String symbol) throws Exception {
		return StockQuote.create(symbol);
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
					}
				}

				final StringBuilder sb = new StringBuilder();

				for (Map<String, String> s:resultList) {

					try {

						StockQuote q = load(s.get("symbol"));

						if (q.getLast()==0.0) continue; //Connection Error, Skip
						
						double last, change; 
						try {
							last = Double.parseDouble(s.get("price"));//Last Price
							
							change = q.getLast() - last;//Current - Previous
						} catch (Exception e) {
							change = q.getChangedValue();
						}
						
						s.put("price", "" + q.getLast());
						s.put("change", decimalFormat.format(change));

						if (change != 0) {

							String symbol = s.get("symbol");

							String t = "";
							if (symbol.length() < 5) {

								for (int i=0; i<symbol.length(); i++) {
									t += symbol.charAt(i) + "  ";
								}

							} else {
								t = symbol;
							}

							if (change > 0)
								sb.append(t + ", Up, " + s.get("price"));
							else
								sb.append(t + ", Down, " + s.get("price"));

							sb.append(", ");

						}

					} catch (Exception e) {
						e.printStackTrace();
						break;

					}

				}//End loop


				handler.post(new Runnable() {

					@Override
					public void run() {

						adapter.notifyDataSetChanged();

						if ( toggleSpeakButton.isChecked() && sb.length()>0 ) {
							toggleSpeakButton.setEnabled(false);
							toggleSpeakButton.setBackgroundResource(R.drawable.bot_speaking);

							HashMap<String, String> myHashAlarm = new HashMap<String, String>();

							myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_NOTIFICATION));
							myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "" + System.currentTimeMillis());
							tts.speak(sb.toString(), TextToSpeech.QUEUE_FLUSH, myHashAlarm);

						}
					}

				});	

			}


		}, 0,  60 * 1000);	

	}

	private void update(final String symbol, final StockQuote q, final StockQuote.XD xd) {

		handler.post(new Runnable() {

			@Override
			public void run() {

				synchronized (stockList) {
					try {
						Map<String, String> map = new HashMap<String, String>();
						map.put("symbol", "" + symbol);
						map.put("price", "" + q.getLast());
						map.put("change", "" + q.getChangedValue());

						if (xd!=null) {
							map.put("xd", xd.dateString);
							map.put("dvd", xd.dvd);										
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

					final StockQuote q = load(symbol);

					final StockQuote.XD xd = StockQuote.loadXD(symbol);

					update(symbol, q, xd);

				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		}.start();

	}

	public void addSymbol(View v) {

		final String symbol = editSymbolText.getText().toString().toUpperCase();
		editSymbolText.setText("");

		addSymbol(symbol);
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
		public View getView(final int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);

			Button remove = (Button) ((ViewGroup)v).findViewById(R.id.remove);

			TextView change = (TextView) ((ViewGroup)v).findViewById(R.id.change);
			TextView last = (TextView) ((ViewGroup)v).findViewById(R.id.last);
			TextView xdDate = (TextView) ((ViewGroup)v).findViewById(R.id.xdDate);
			TextView xdPercent = (TextView) ((ViewGroup)v).findViewById(R.id.xdPercent);

			double val;
			try {
				val = Double.parseDouble("" + change.getText());
			} catch (Exception e) {
				val = 0;
			}

			if (val>0) {

				change.setTextColor(green);
				last.setTextColor(green);

			} else if (val <0) {

				change.setTextColor(red);
				last.setTextColor(red);

			} else {

				change.setTextColor(white);
				last.setTextColor(white);
			}

			if (isBeforeXD(xdDate.getText().toString())) {

				xdDate.setTextColor(green);			

			} else {

				xdDate.setTextColor(red);			

			}

			Map<String, String> map = stockList.get(position);
			String dvd = map.get("dvd");
			try {
				double percent = Double.parseDouble(dvd) / Double.parseDouble(last.getText().toString()) * 100;  
				xdPercent.setText(decimalFormat.format(percent));
			} catch (Exception e) {
				xdPercent.setText(dvd);				
			}

			xdPercent.setTextColor(Color.parseColor("#FFFFFF"));			

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
		tts.speak("Hello, Welcome to Set Operator", TextToSpeech.QUEUE_FLUSH, myHashAlarm);
	}

	@Override
	public void onInit(int status) {

		if (status == TextToSpeech.SUCCESS) {

			int result = tts.setLanguage(Locale.US);

			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {

				Log.e("TTS", "This Language is not supported");
			} 

		} else {
			Log.e("TTS", "Initilization Failed!");
		}

	}

	@Override
	public void onUtteranceCompleted(String arg0) {

		handler.post(new Runnable() {

			@Override
			public void run() {
				toggleSpeakButton.setBackgroundResource(R.drawable.bot);
				toggleSpeakButton.setEnabled(true);
			}

		});

	}

}
