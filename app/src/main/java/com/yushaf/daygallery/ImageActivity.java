package com.yushaf.daygallery;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ImageActivity extends AppCompatActivity {

    /*

    Activity для вывода одного изображения в полноэкранном режиме.
    taskAffinity и launchMode (singleInstance) настроены для использования нового окна.

     */

    // Стандартный код шаблона полноэкранного режима.

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        mVisible = true;
        mContentView = findViewById(R.id.image);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Попытка восстановления из bundle и - при неудаче - разбор intent.
        if (!restore(savedInstanceState))
            handleIntent(getIntent());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        mShowPart2Runnable.run();
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    // Добавленный код.

    private static final String imageKey = "ImageKey"; // Ключ для bundle.
    private ImageKit imageKit; // Изображение.

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(imageKey, imageKit); // Сохранение изображения.
    }

    private boolean restore(Bundle bundle) { // Попытка восстановления состояния.
        boolean ok = false;
        if (bundle != null) {
            ImageKit kit = (ImageKit) bundle.getSerializable(imageKey);
            ok = kit != null;
            if (ok)
                showImage(kit);
        }
        return ok;
    }

    @Override
    protected void onNewIntent(Intent intent) { // Обработка новых intent (так как activity в единственном экземпляре).
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            ImageKit kit = (ImageKit) intent.getSerializableExtra(getString(R.string.intentDataKey));
            showImage(kit);
            setIntent(intent); // Не удалось установить сохраняется ли Intent, но на всякий случай...
        }
    }

    private void showImage(ImageKit kit) { // Сохранение адреса и вывод изображения.
        imageKit = kit;

        // Выбор варианта изображения.
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        String url = kit.getBigger(metrics.widthPixels, metrics.heightPixels);

        ImageView image = findViewById(R.id.image);
        RequestManager manager = Glide.with(this);
        RequestBuilder<Drawable> builder;
        if (url != null) {
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.ic_crop_white_24dp)
                    .error(R.drawable.ic_error_white_24dp);
            builder = manager
                    .load(url)
                    .apply(options);
        } else {
            builder = manager
                    .load(R.drawable.ic_error_white_24dp);
            Toast.makeText(this, getString(R.string.emptyData), Toast.LENGTH_LONG).show();
        }
        builder
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(image);
    }

}
