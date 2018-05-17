package com.jack.imagepickorcapture;


public interface OnPhotoUpload {
    void onUploadSelect(int position);

    void onCancleSelect(int position);

    void onUploadStart(String msg, String Uri);

    void onUploadProgressChange(int percentage);

    void onCompleate(boolean iserror, String errormsg);
}
