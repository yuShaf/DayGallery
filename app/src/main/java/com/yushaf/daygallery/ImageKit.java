package com.yushaf.daygallery;

import java.io.Serializable;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class ImageKit implements Serializable {
    // Класс для хранения данных одного изображения. Позволяет выбирать наиболее подходящее по размеру.

    public static class Size implements Serializable {
        // Класс для хранения одного варианта изображения.

        public int width, height;
        public String url;

        public Size(int width, int height, String url) {
            this.width = width;
            this.height = height;
            this.url = url;
        }
    }

    private NavigableMap<Integer, Size> urlByWidth = new TreeMap<>(); // Варианты, упорядоченные по возрастанию ширины.
    private NavigableMap<Integer, Size> urlByHeight = new TreeMap<>(); // Варианты, упорядоченные по возрастанию высоты.

    public void add(int width, int height, String url) {
        // Предполагается, что изображений с одинаковой шириной (высотой) не будет.
        Size size = new Size(width, height, url);
        urlByWidth.put(width, size);
        urlByHeight.put(height, size);
    }

    public String getBigger(int viewWidth, int viewHeight) {
        String url = null;
        if (!urlByWidth.isEmpty()) {
            Size size = getBigger(urlByWidth, viewWidth); // Подбор по ширине.
            if (size.height < viewHeight) // Если высота меньше требуемой, подбор по высоте.
                size = getBigger(urlByHeight, viewHeight);
            url = size.url;
        }
        return url;
    }

    private static Size getBigger(NavigableMap<Integer, Size> urlBySize, int size) {
        // Выбирается изображение с минимальным размером не меньше заданного.
        Map.Entry<Integer, Size> bySize = urlBySize.ceilingEntry(size);
        if (bySize == null) // Или самое большое.
            bySize = urlBySize.lastEntry();
        return bySize.getValue();
    }

}
