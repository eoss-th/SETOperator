package com.th.eoss.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by eossth on 12/12/2016 AD.
 */

public class SpeechBundle {

    static Map<String, Map<String, String>> dict = new HashMap<>();

    private String locale;

    public static SpeechBundle create (String locale) {
        return new SpeechBundle(locale);
    }

    private SpeechBundle(String locale) {
        Map<String, String> en = new HashMap<>();
        Map<String, String> th = new HashMap<>();

        en.put("up", "up");
        th.put("up", "ขึ้น");

        en.put("down", "down");
        th.put("down", "ลง");

        en.put("bath", "Bath");
        th.put("bath", "บาท");

        en.put("greeting", "Hello, Welcome to Set Operator");
        th.put("greeting", "Set Operator ยินดีรับใช้ค่ะ");

        dict.put("en", en);
        dict.put("th", th);

        this.locale = locale;
    }

    public String get (String key) {
        Map<String, String> map = dict.get(locale);
        if ( map!=null ) {
            String val = map.get(key);
            if (val!=null) return val;
        }
        return "";
    }

}
