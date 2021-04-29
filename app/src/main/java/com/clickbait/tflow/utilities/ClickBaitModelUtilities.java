package com.clickbait.tflow.utilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.clickbait.tflow.config.ClickBaitModel;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import org.tensorflow.Tensor;

public class ClickBaitModelUtilities {

    private static ClickBaitModelUtilities sigleInstance = null;
    // NO DUAL PURPOSE / VAL / MATCH
    private final Pattern urlPattern = Pattern.compile("(([^\\/|=|?|_|-]+)(?=(\\.\\w+$)|(/+$)|-|_))+");

    private final String[] splitOn = new String[] { "_", "-" };
    private final String word = "^[a-z]+$";

    private final Map<String, Integer> clickBaitMapping;
    private final ClickBaitModel prop;
    private final int tLength;

    public static ClickBaitModelUtilities get(ClickBaitModel pr) throws FileNotFoundException {
        if (sigleInstance == null) {
            sigleInstance = new ClickBaitModelUtilities(pr);
        }
        return sigleInstance;
    }

    public static ClickBaitModelUtilities get() {
        return sigleInstance;
    }

    private ClickBaitModelUtilities(ClickBaitModel pr) throws FileNotFoundException {
        this.clickBaitMapping = new Yaml(new Constructor(Map.class)).load(new FileInputStream(pr.getMappingPath()));
        this.tLength = pr.getMax1DTensorAxis0();
        this.prop = pr;
    }

    public Tensor<Float> getUrl(String url) {
        int[] res = new int[tLength];
        StringBuilder str = new StringBuilder(url);

        Matcher ma = urlPattern.matcher(str);
        List<String> stringRes = new ArrayList<String>();

        while (ma.find()) {
            stringRes.add(ma.group(1));
        }
        if (stringRes.size() > 2) {
            int len = stringRes.size();
            int del = prop.isPostPadding() ? 0 : tLength - len;
            for (int i = 0; i < len; i++) {
                res[del + i] = getEntry(stringRes.get(i));
            }
        }

        return Tensor.create(new long[] { 1, tLength }, FloatBuffer.wrap(Floats.toArray(Ints.asList(res))));
    }

    public Tensor<Float> getUrl(String[] urls) {
        float[][] res_outer = new float[urls.length][tLength];

        for (int i = 0; i < urls.length; i++) {
            float[] res_inner = new float[tLength];
            StringBuilder str = new StringBuilder(urls[i]);

            Matcher ma = urlPattern.matcher(str);
            List<String> stringRes = new ArrayList<String>();

            while (ma.find()) {
                stringRes.add(ma.group(1));
            }
            if (stringRes.size() > 2) {
                int len = stringRes.size();
                int del = prop.isPostPadding() ? 0 : tLength - len;
                for (int n = 0; n < len; n++) {
                    res_inner[del + n] = getEntry(stringRes.get(n));
                }
            }
            res_outer[i] = res_inner;
        }

        return Tensor.create(res_outer, Float.class);
    }

    private int getEntry(String st) {
        Integer r = clickBaitMapping.get(st);
        return r != null ? r : clickBaitMapping.get(prop.getNotFound());
    }

    private String[] findSplit(String a) {
        return Arrays.stream(splitOn).map(x -> a.split(x))
                .map(x -> Arrays.stream(x).filter(y -> y.matches(word)).toArray(String[]::new))
                .filter(x -> x.length > 3).findFirst().orElse(null);
    }

    private void replaceAll(StringBuilder sb, String pattern, String replacement) {
        Matcher m = Pattern.compile(pattern).matcher(sb);
        int start = 0;
        while (m.find(start)) {
            sb.replace(m.start(), m.end(), replacement);
            start = m.start() + replacement.length();
        }
    }

    private boolean endsWith(StringBuilder sb, String a) {
        return sb.lastIndexOf(a) == sb.length() - 1;
    }

    private boolean endsWith(StringBuilder sb, char a) {
        return sb.charAt(sb.length() - 1) == a;
    }
}
