package com.th.eoss.setoperator;

import android.speech.tts.TextToSpeech;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by eossth on 12/12/2016 AD.
 */

public class SpeechBundle {

    static Map<String, Map<String, String>> dict = new HashMap<>();

    private TextToSpeech textToSpeech;

    private String locale;

    public static SpeechBundle create (TextToSpeech textToSpeech, String locale) {
        return new SpeechBundle(textToSpeech, locale);
    }

    private SpeechBundle(TextToSpeech textToSpeech, String locale) {

        Map<String, String> en = new HashMap<>();
        Map<String, String> th = new HashMap<>();

        en.put("up", "up");
        th.put("up", "ขึ้น");

        en.put("down", "down");
        th.put("down", "ลง");

        en.put("price", "Last");
        th.put("price", "ราคา");

        en.put("bath", "Bath");
        th.put("bath", "บาท");

        en.put("greeting", "Hello, Welcome to Set Operator");
        th.put("greeting", "Set Operator ยินดีรับใช้ค่ะ");

        en.put("sleeping", "Market is currently closed");
        th.put("sleeping", "ตลาดยังไม่เปิดทำการ");

        en.put("wake", "Market is open");
        th.put("wake", "ตลาดเปิดแล้วค่ะ");

        en.put("goodbye", "Market is closed, Goodbye!");
        th.put("goodbye", "ตลาดปิดแล้ว, แล้วพบกันใหม่ค่ะ");

        dict.put("en", en);
        dict.put("th", th);

        this.locale = locale;
        this.textToSpeech = textToSpeech;
    }

    String get (String key) {
        Map<String, String> map = dict.get(locale);
        if ( map!=null ) {
            String val = map.get(key);
            if (val!=null) return val;
        }
        return "";
    }

    public void speak (String key) {
        /*
        HashMap<String, String> myHashAlarm = new HashMap<String, String>();

        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_NOTIFICATION));
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "" + System.currentTimeMillis());
        */

        textToSpeech.speak(get(key), TextToSpeech.QUEUE_ADD, null, "");
    }

}
