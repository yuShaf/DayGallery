package com.yushaf.daygallery;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;

public class StripActivity extends AppCompatActivity
        implements UrlLoadTask.User {

    // Методы жизненного цикла.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strip);

        // Рекомендуется использовать Toolbar вместо ActionBar для одинакового поведения.
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        initStrip(); // Настройка отображения картинок.
        restoreState(savedInstanceState); // Попытка восстановления состояния.
        initSwipeRefresh(); // Настройка обновления "тягой".
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /*
        Использование простой асинхронной задачи вроде бы не позволяет передать её в следующую жизнь,
        поэтому загрузка здесь останавливается, а при перезапуске будет начата заново.
        Расчёт на то, что загрузка на самом деле пройдёт быстро.
         */
        cancelLoad();
        saveState(outState); // Сохранение состояния.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelLoad();
    }

    // Состояние.

    private enum State {
        Load, // Загрузка адресов изображений в процессе.
        Show, // Изображения отображаются.
        Fail  // Загрузка адресов изображений не состоялась.
    };

    // Ключи для хранения данных в Bundle.
    private static final String stateKey = "STATE";
    private static final String urlKey = "URLS";

    private State state;
    private UrlLoadTask loadTask; // Загрузчик ссылок.

    private void saveState(Bundle bundle) {
        bundle.putSerializable(stateKey, state); // Сохранение состояния.
        switch (state) {
            case Load:
                break;
            case Show:
                String[] urls = new String[adapter.getCount()];
                for (int i = 0; i < urls.length; i++)
                    urls[i] = adapter.getItem(i);
                bundle.putSerializable(urlKey, urls); // Сохранение ссылок.
                break;
            case Fail:
                break;
        }
    }

    private void restoreState(Bundle bundle) {
        if (bundle == null) // Первая загрузка.
            state = State.Load;
        else { // Восстановление состояния.
            Object savedState = bundle.get(stateKey);
            state = savedState == null ? State.Load : (State) savedState;
        }
        switch (state) {
            case Load:
            case Fail:
                startLoad(); // Попытка новой загрузки.
                // Возможно, при Fail не стоит этого делать.
                break;
            case Show: // Восстановление ссылок.
                String[] urls = (String[]) bundle.getSerializable(urlKey);
                handleUrl(urls);
                break;
        }
    }

    private void startLoad() {
        // Проверка соединения.
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            // UI
            adapter.clear();
            toast(getString(R.string.stateLoad));
            setRefreshing(true);

            // Запуск задания.
            state = State.Load;
            loadTask = new UrlLoadTask(this);
            loadTask.execute();

        } else {
            state = State.Fail;
            setRefreshing(false);
            toast(getString(R.string.internetOff));
        }
    }

    private void cancelLoad() {
        if (loadTask != null) {
            loadTask.cancel(true);
            loadTask = null;
        }
    }

    // UI.

    private ArrayAdapter<String> adapter;

    private void initSwipeRefresh() {
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startLoad();
            }
        });
    }

    private void setRefreshing(boolean refreshing) {
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setRefreshing(refreshing);
    }

    private void initStrip() {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        GridView view = findViewById(R.id.stripGridView);
        view.setAdapter(adapter);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_refresh, menu); //
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result;
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                cancelLoad();
                startLoad();
                result = true;
                break;
            default:
                result = super.onOptionsItemSelected(item);
                break;
        }
        return result;
    }

    // Реализация интерфейса для загрузчика.

    @Override
    public void handleUrl(String... urls) {
        adapter.addAll(urls);
    }

    @Override
    public void handleException(Exception exception) {
        if (exception != null) { // Fail.
            state = State.Fail;
            // UI.
            toast(getString(R.string.stateFail));
        } else {
            state = State.Show;
            toast(getString(R.string.stateShow));
        }
        setRefreshing(false);
    }

    @Override
    public String getUrl() {
        return getString(R.string.apiFotki);
    }

    @Override
    public String getTag() {
        return getString(R.string.apiFotkiTag);
    }

    @Override
    public String getAttribute() {
        return getString(R.string.apiFotkiAttribute);
    }

}
