package com.jack.imagepickorcapture.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.darsh.multipleimageselect.models.Image;
import com.jack.imagepickorcapture.R;

import java.io.File;
import java.util.ArrayList;

public class PhotoUploadGalleryAdapter extends BaseAdapter {

    private ArrayList<Image> urls;
    private LayoutInflater inflater;
    private Context mContext;

    public PhotoUploadGalleryAdapter(Context activity, ArrayList<Image> urls) {
        this.mContext = activity;
        this.urls = urls;
        this.inflater = LayoutInflater.from(activity);
    }

    public void SetNewData(ArrayList<Image> urls) {
        this.urls = urls;
    }

    @Override
    public int getCount() {
        return urls.size();
    }

    @Override
    public Object getItem(int position) {
        return urls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v ;
        viewHolder holder;

        if (convertView == null) {
            v = inflater.inflate(R.layout.item_photo_upload, null);
            holder = new viewHolder(v);
            v.setTag(holder);
        } else {
            v = convertView;
            holder = (viewHolder) convertView.getTag();
        }
        holder.txt_img_no.setText(position + 1 + "");

        File f = new File(urls.get(position).path);

        Glide.with(mContext)
                .load(f)
                .apply(new RequestOptions()
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(holder.Thumb);
        //Picasso.with(mContext).load(f).centerCrop().resize(600,600).into(holder.Thumb);

        return v;
    }

    public class viewHolder {
        public ImageView Thumb;
        public TextView txt_img_no;

        public viewHolder(View v) {
            txt_img_no = (TextView) v.findViewById(R.id.photo_upload_txt_num);
            Thumb = (ImageView) v.findViewById(R.id.image);
        }
    }
}
