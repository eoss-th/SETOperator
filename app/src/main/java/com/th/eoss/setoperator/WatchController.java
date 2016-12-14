package com.th.eoss.setoperator;

import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.th.eoss.util.Formatter;
import com.th.eoss.util.SETQuote;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by eossth on 12/14/2016 AD.
 */

public class WatchController implements TextToSpeech.OnInitListener {

    Activity activity;
    List<Map<String, String>> watchList;
    WatchFragment watchFragment;
    Timer timer;
    SpeechBundle speechBundle;
    TextToSpeech textToSpeech;

    public WatchController(List<Map<String, String>> watchList, WatchFragment watchFragment) {
        this.watchList = watchList;
        this.watchFragment = watchFragment;
        this.activity = watchFragment.getActivity();

        textToSpeech = new TextToSpeech(this.activity, this, "com.google.android.textToSpeech");
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
                            watchFragment.toggleSpeakButton.setEnabled(false);
                            watchFragment.toggleSpeakButton.setBackgroundResource(R.drawable.bot_speaking);
                        }

                    });
                }

                @Override
                public void onDone(String s) {
                    SingleHandler.handler.post(new Runnable() {

                        @Override
                        public void run() {
                            watchFragment.toggleSpeakButton.setBackgroundResource(R.drawable.bot);
                            watchFragment.toggleSpeakButton.setEnabled(true);
                        }

                    });
                }

                @Override
                public void onError(String s) {

                }
            });

        } else {
            Log.e("TTS", "Initilization Failed!");
            watchFragment.toggleSpeakButton.setEnabled(false);
        }
    }

    void speak(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, "");
    }

    void speakBundle(String key) {
        speechBundle.speak(key);
    }

    void loadWatchList() {

        if (watchList ==null) {

            try {

                FileInputStream fi = activity.openFileInput("data.ser");

                ObjectInputStream oi = new ObjectInputStream(fi);
                watchList = (List<Map<String, String>>) oi.readObject();

            } catch (Exception e) {
                watchList = new ArrayList<Map<String, String>>();
            }

        }

    }

    void saveWatchList() {

        try {

            FileOutputStream fout = activity.openFileOutput("data.ser", Activity.MODE_PRIVATE);

            ObjectOutputStream os = new ObjectOutputStream(fout);
            os.writeObject(watchList);

            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void startTimer() {

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

                        watchFragment.adapter.notifyDataSetChanged();

                        //Wakeup when just open
                        if ((taskStarted==false || lastMarketStatusIsClosed) && isMarketClosed==false && watchFragment.toggleSpeakButton.isChecked()==false ) {

                            speakBundle("wake");
                            watchFragment.toggleSpeakButton.setChecked(true);
                        }

                        if (isMarketClosed && watchFragment.toggleSpeakButton.isChecked()) {

                            speakBundle("sleeping");
                            watchFragment.toggleSpeakButton.setChecked(false);
                        }

                        if ( watchFragment.toggleSpeakButton.isChecked() && sb.length()>0 ) {

                            speak(sb.toString());

                        }

                        //Goodbye when just closed
                        if (lastMarketStatusIsClosed==false && isMarketClosed && watchFragment.toggleSpeakButton.isChecked() ) {

                            speakBundle("goodbye");
                            watchFragment.toggleSpeakButton.setChecked(false);
                        }

                        lastMarketStatusIsClosed = isMarketClosed;
                        taskStarted = true;
                    }

                });

            }


        }, 0,  60 * 1000 * 9);

    }

    public void stopTTS() {

        textToSpeech.stop();
        textToSpeech.shutdown();

    }

    public void stopTimer() {

        timer.cancel();
    }
}
