package com.yushaf.daygallery;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bumptech.glide.Glide;

public class StripAdapter extends ArrayAdapter<String> {

    private final ViewGroup.LayoutParams imageParams;

    public StripAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        imageParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, // Заполнить всю ширину столбца.
                // Высота фиксирована. (Получить реальную ширину столбца можно только в API 16.)
                (int) context.getResources().getDimension(R.dimen.stripHeight));
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
        Glide
                .with(getContext())
                .load(getItem(position))
                .into(view);
        return view;
    }

}
