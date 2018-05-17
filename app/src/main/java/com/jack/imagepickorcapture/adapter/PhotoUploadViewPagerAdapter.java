package com.jack.imagepickorcapture.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.darsh.multipleimageselect.models.Image;
import com.jack.imagepickorcapture.R;

import java.io.File;
import java.util.List;

/*
 * Created by Jatin on 14-Jul-16.
 */

public class PhotoUploadViewPagerAdapter extends PagerAdapter {
    private List<Image> mData;
    private LayoutInflater inflater;
    private Context mContext;

    public PhotoUploadViewPagerAdapter(Context context,
                                       List<Image> mData) {
        this.mData = mData;
        this.inflater = LayoutInflater.from(context);
        this.mContext = context;
    }

    @Override
    public float getPageWidth(int position) {
        if (position != 0 && (position != (mData.size() - 1))) {
            return 1f;
        } else {
            return 1f;
        }
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return (view == o);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View v = null;
        final viewHolder holder;
        final Image data = mData.get(position);
        v = inflater.inflate(R.layout.item_photo_upload_show, null);

        holder = new viewHolder(v);

        File f = new File(data.path);
        Glide.with(mContext)
                .load(f)
                .into(holder.image);

        if (!data.caption.equals("")) holder.caption.setText(data.caption);

        holder.caption.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) data.caption = editable.toString();
            }
        });

        holder.caption.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    try {
                        hideKeyboard(holder.caption);
                        return true;
                    } catch (Exception e) {
                    }
                }
                return false;
            }
        });
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    hideKeyboard(holder.caption);
                } catch (Exception w) {
                }
            }
        });
        //add this view
        container.addView(v);
        return v;
    }

    public void hideKeyboard(EditText caption) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(caption.getWindowToken(), 0);
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }

    public class viewHolder {
        public EditText caption;
        public ImageView image;

        public viewHolder(View v) {
            caption = (EditText) v.findViewById(R.id.edt_caption_perticular);
            image = (ImageView) v.findViewById(R.id.img_row_phto_show);
        }
    }
}
