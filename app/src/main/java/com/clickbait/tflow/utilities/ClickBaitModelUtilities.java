package com.clickbait.tflow.utilities;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import org.tensorflow.Tensor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.clickbait.tflow.config.ClickBaitModel;
import org.tensorflow.ndarray.buffer.DataBuffers;
import org.tensorflow.ndarray.StdArrays;
import org.tensorflow.types.TFloat32;

public class ClickBaitModelUtilities {
    private static ClickBaitModelUtilities sigleInstance = null;
    // NO DUAL PURPOSE / VAL / MATCH
    private final Pattern urlPattern = Pattern.compile("(([^\\/|=|?|_|-]+)(?=(\\.\\w+$)|(\\/+$)|-|_))+");

    private final DecimalFormat df = new DecimalFormat("#.####");
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

    public Tensor getUrl(String url) {
        float[][] res = new float[1][tLength];
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
                res[0][del + i] = getEntry(stringRes.get(i));
            }
        }

        return TFloat32.tensorOf(StdArrays.ndCopyOf(res));
    }

    public Tensor getUrl(String[] urls) {
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

        return TFloat32.tensorOf(StdArrays.ndCopyOf(res_outer));
    }

    private int getEntry(String st) {
        Integer r = clickBaitMapping.get(st);
        return r != null ? r : clickBaitMapping.get(prop.getNotFound());
    }

    private String[] findSplit(String a) {
        return Arrays.stream(new String[] { "_", "-" }).map(x -> a.split(x))
                .map(x -> Arrays.stream(x).filter(y -> y.matches("^[a-z]+$")).toArray(String[]::new))
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

    public ByteBuffer getBuffer(Tensor tensor) {
        ByteBuffer bbuf = ByteBuffer.allocate((int) tensor.numBytes()).order(ByteOrder.nativeOrder());
        tensor.asRawTensor().data().copyTo(DataBuffers.of(bbuf), tensor.numBytes());
        return bbuf;
    }

    public String floatR(Float value) {
        return df.format(value);
    }
}
