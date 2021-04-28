package com.clickbait.tflow.utilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.clickbait.tflow.config.ClickBaitModel;
import com.google.common.base.Strings;

import org.tensorflow.Tensor;

public class ClickBaitModelUtilities {

    private static ClickBaitModelUtilities sigleInstance = null;

    // DOCUMENT
    private final Pattern urlPattern = Pattern.compile("^[http:\\/\\/|https:\\/\\/].*\\/(?=[^/]*$)(.*?)(\\.|\\?|$)");
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
        this.clickBaitMapping = new Yaml(new Constructor(Map.class))
                                    .load(new FileInputStream(pr.getMappingPath()));
        this.tLength = pr.getMax1DTensorAxis0();
        this.prop = pr;
    }

    public Tensor<Integer> getUrl(String url) {
        int[] res = new int[tLength];
        StringBuilder str = new StringBuilder(url);
        // Pattern in click_bait_filter_be
        // api/url_get/getUrl
        // HTML Generators skipped
        replaceAll(str, "/\\?", "?");
        if (endsWith(str, '/')) {
            str.setLength(str.length() - 1);
        }

        Matcher ma = urlPattern.matcher(str);
        if (ma.find() && !Strings.isNullOrEmpty(ma.group(1)) && !ma.group(1).contains("=")) {
            String[] stringRes = findSplit(ma.group(1));
            if (stringRes != null) {
                int len = stringRes.length;
                int del = prop.isPostPadding() ? 0 : tLength - len;
                for (int i = 0; i < len; i++) {
                    res[del + i] = getEntry(stringRes, i);
                }
            }
        }

        return Tensor.create(new long[] { tLength }, IntBuffer.wrap(res));
    }

    private int getEntry(String[] stringRes, int i) {
        Integer r = clickBaitMapping.get(stringRes[i]);
        return r != null ? r : clickBaitMapping.get(prop.getNotFound());
    }

    private String[] findSplit(String a) {
        return Arrays.stream(splitOn)
                .map(x -> a.split(x))
                .map(x -> Arrays.stream(x)
                                .filter(y -> y.matches(word))
                                .toArray(String[]::new))
                .filter(x -> x.length > 3)
                .findFirst()
                .orElse(null);
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
