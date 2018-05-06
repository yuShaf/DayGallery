package com.yushaf.daygallery;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

public class StripAdapter extends ArrayAdapter<ImageKit> {

    private final ViewGroup.LayoutParams imageParams;
    private final int imageWidth, imageHeight; // Ожидаемые размеры одного изображения.

    public StripAdapter(@NonNull Context context, int resource, DisplayMetrics metrics) {
        super(context, resource);

        // Получить реальную ширину столбца можно только в API 16, поэтому...
        // Расчёт ожидаемых размеров одного изображения.
        float baseColumnWidth = // Исходная ширина столбца на конкретном экране.
                context.getResources().getDimension(R.dimen.stripWidth) +
                        context.getResources().getDimension(R.dimen.stripHSpace) * 2;
        float fullWidth = metrics.widthPixels; // Ширина конкретного экрана.
        int times = (int) (fullWidth / baseColumnWidth); // Количество столбцов.
        imageWidth = (int) (fullWidth / times); // Реальный размер столбца.
        imageHeight = // Высота строки через заданное в ресурсах соотношение сторон.
                (int) context.getResources().getFraction(R.fraction.stripRatio, imageWidth, imageWidth);

        imageParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, // Заполнить всю ширину столбца.
                imageHeight);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ImageView view;
        if (convertView == null) {
            view = new ImageView(getContext());
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            view.setLayoutParams(imageParams);
        } else {
            view = (ImageView) convertView;
        }
        String url = getItem(position).getBigger(imageWidth, imageHeight);
        // Расчёт на error в случае отсутствия адреса.
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.ic_crop_original_black_24dp)
                .error(R.drawable.ic_error_outline_black_24dp);
        Glide
                .with(getContext())
                .load(url)
                .apply(options)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(view);
        return view;
    }

}
