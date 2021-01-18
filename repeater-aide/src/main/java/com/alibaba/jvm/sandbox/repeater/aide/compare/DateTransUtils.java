package com.alibaba.jvm.sandbox.repeater.aide.compare;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTransUtils {

    public static final Map<Integer, Character> charMap = new HashMap<Integer, Character>();
    public static final Pattern p = Pattern.compile("^(\\d+)\\D*(\\d*)\\D*(\\d*)\\D*(\\d*)\\D*(\\d*)\\D*(\\d*)");

    static {
        charMap.put(1, 'y');
        charMap.put(2, 'M');
        charMap.put(3, 'd');
        charMap.put(4, 'H');
        charMap.put(5, 'm');
        charMap.put(6, 's');
    }

//    public static void main(String[] args) throws ParseException {
//        Date date = stringToDate("2020-10/11 02:32:23");
//        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        System.out.println(sf.format(date));
//    }

    /**
     * 任意日期字符串转换为Date，不包括无分割的纯数字（13位时间戳除外） ，日期时间为数字，年月日时分秒，但没有毫秒
     *
     * @param dateString 日期字符串
     * @return Date
     */
    public static Date stringToDate(String dateString) throws ParseException {
        if(StringUtils.isBlank(dateString)){
            return null;
        }
        dateString = dateString.trim().replaceAll("[a-zA-Z]"," ");
        if(Pattern.matches("^[-+]?\\d{13}$",dateString)) {//支持13位时间戳
            return new Date(Long.parseLong(dateString));
        }
        Matcher m = p.matcher(dateString);
        StringBuilder sb = new StringBuilder(dateString);
        //从被匹配的字符串中，充index = 0的下表开始查找能够匹配pattern的子字符串。m.matches()的意思是尝试将整个区域与模式匹配，不一样。
        if (m.find(0)) {
            int count = m.groupCount();
            for (int i = 1; i <= count; i++) {
                for (Map.Entry<Integer, Character> entry : charMap.entrySet()) {
                    if (entry.getKey() == i) {
                        sb.replace(m.start(i), m.end(i), replaceEachChar(m.group(i), entry.getValue()));
                    }
                }
            }
        } else {
            throw new ParseException("错误的日期格式", 0);
        }
        String format = sb.toString();
        SimpleDateFormat sf = new SimpleDateFormat(format);
        try {
            return sf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new ParseException("日期字符串转Date出错", 0);
        }
    }

    /**
     * 将指定字符串的所有字符替换成指定字符，跳过空白字符
     *
     * @param s 被替换字符串
     * @param c 字符
     * @return 新字符串
     */
    public static String replaceEachChar(String s, Character c) {
        StringBuilder sb = new StringBuilder("");
        for (Character c1 : s.toCharArray()) {
            if (c1 != ' ') {
                sb.append(String.valueOf(c));
            }
        }
        return sb.toString();
    }

}
