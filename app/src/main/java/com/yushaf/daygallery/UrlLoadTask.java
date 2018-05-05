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
                String, // URL возвращаются как прогресс, чтобы сразу использовать.
                Exception // Возникшее исключение возвращается как результат.
                > {

    public interface User { // Интерфейс пользователя загрузчика для ухода от зависимостей.
        void handleUrl(String... urls); // Метод обработки прогресса - найденных адресов.
        void handleException(Exception exception); // Метод обработки завершения.
        String getUrl(); // Адрес для загрузки XML.
        String getTag();
        String getAttribute();
    }

    private final WeakReference<User> userRef; // Слабая ссылка на случай уничтожения пользователя.
    private final String url, tag, attribute;

    // Используется SAX в попытке меньше держать в памяти.
    private final DefaultHandler entryHandler = new DefaultHandler() { // Объект для SAX разбора.
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            handleElement(qName, attributes);
        }
    };

    private void handleElement(String qName, Attributes attributes) // Метод для разбора XML.
            throws SAXException {
        if (qName.equals(tag)) { // Оригинальное изображение.
            String url = attributes.getValue(attribute); // Ссылка изображения.
            if (isCancelled() || userRef.get() == null)
                // Остановка разбора через исключение при отмене задания или уничтожении пользователя.
                throw new SAXException();
            else
                publishProgress(url); // Передача ссылки в UI поток.
        }
    }

    public UrlLoadTask(User user) {
        userRef = new WeakReference<>(user);
        url = user.getUrl();
        tag = user.getTag();
        attribute = user.getAttribute();
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
    protected void onProgressUpdate(String... values) { // Передача адресов.
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
