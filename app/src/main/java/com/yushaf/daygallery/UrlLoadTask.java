package com.yushaf.daygallery;

import android.os.AsyncTask;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class UrlLoadTask extends
        AsyncTask< // Самый простой вариант для загрузки xml со списком картинок.
                Void,
                ImageKit, // URL возвращаются как прогресс, чтобы сразу использовать.
                Exception // Возникшее исключение возвращается как результат.
                > {

    public interface User { // Интерфейс пользователя загрузчика для ухода от зависимостей.
        void handleUrl(ImageKit... urls); // Метод обработки прогресса - найденных адресов.
        void handleException(Exception exception); // Метод обработки завершения.
        String getUrl(); // Адрес для загрузки XML.
    }

    // Используемые фрагменты XML.
    private static final String imageTag = "entry", sizeTag = "f:img",
            sizeWidth = "width", sizeHeight = "height", sizeUrl = "href";

    private final WeakReference<User> userRef; // Слабая ссылка на случай уничтожения пользователя.
    private final String url;

    // Используется SAX в попытке меньше держать в памяти.
    private final DefaultHandler entryHandler = new DefaultHandler() { // Объект для SAX разбора.

        private boolean insideEntry = false;
        private ImageKit kit;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            checkCancel();
            if (qName.equals(imageTag)) {
                insideEntry = true;
                kit = new ImageKit();
            }
            else if (insideEntry && qName.equals(sizeTag)) {
                int width = Integer.parseInt(attributes.getValue(sizeWidth));
                int height = Integer.parseInt(attributes.getValue(sizeHeight));
                String url = attributes.getValue(sizeUrl);
                kit.add(width, height, url);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            checkCancel();
            if (qName.equals(imageTag))
                publishProgress(kit);
        }

        private void checkCancel() throws SAXException {
            if (isCancelled() || userRef.get() == null)
                // Остановка разбора через исключение при отмене задания или уничтожении пользователя.
                throw new SAXException();
        }

    };

    public UrlLoadTask(User user) {
        userRef = new WeakReference<>(user);
        url = user.getUrl();
    }

    @Override
    protected Exception doInBackground(Void... voids) {
        Exception result = null;
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = saxParserFactory.newSAXParser();
            parser.parse(url, entryHandler); // Проверка отмены внутри обработчика.
        } catch (ParserConfigurationException | SAXException | IOException e) {
            result = e;
        }
        return result;
    }

    @Override
    protected void onProgressUpdate(ImageKit... values) { // Передача адресов.
        super.onProgressUpdate(values);
        User user = userRef.get();
        if (user != null)
            user.handleUrl(values);
    }

    @Override
    protected void onPostExecute(Exception e) { // Передача исключения.
        super.onPostExecute(e);
        User user = userRef.get();
        if (user != null)
            user.handleException(e);
    }

    // При отмене делать ничего не надо.

}
