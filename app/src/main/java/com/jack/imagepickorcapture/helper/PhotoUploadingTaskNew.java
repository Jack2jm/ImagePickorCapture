package com.jack.imagepickorcapture.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;

import com.darsh.multipleimageselect.models.Image;
import com.jack.imagepickorcapture.OnPhotoUpload;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

/*
 * Performs photo upload using Apache Http Components Library
 * 
 * To Do: update this class code as MultipartEntity is now deprecated.
 */

public class PhotoUploadingTaskNew {
    private final String Email;
    private final String Event_id;
    private final ArrayList<Image> mSelectedImages;
    private final OnPhotoUpload uploadCallBack;

    public PhotoUploadingTaskNew(String event_id, String email, ArrayList<Image> mSelectedImages, OnPhotoUpload uploadCallBack) {
        this.Email = email;
        this.Event_id = event_id;
        this.mSelectedImages = mSelectedImages;
        this.uploadCallBack = uploadCallBack;

        // new photoUploadServer().execute();
        new photoUploadServer(Email, Event_id, mSelectedImages).execute();
    }

    private Bitmap resizeImage(Bitmap bitmap, int newSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int newWidth = 0;
        int newHeight = 0;

        if (width > height) {
            newWidth = newSize;
            newHeight = (newSize * height) / width;
        } else if (width < height) {
            newHeight = newSize;
            newWidth = (newSize * width) / height;
        } else if (width == height) {
            newHeight = newSize;
            newWidth = newSize;
        }

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        return Bitmap.createBitmap(bitmap, 0, 0, width, height,
                matrix, true);
    }

    private class photoUploadServer extends AsyncTask<String, Void, String> {
        private final String email;
        private final String eventid;

        public photoUploadServer(
                String email, String eventid, ArrayList<Image> mSelectedImages) {
            this.email = email;
            this.eventid = eventid;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            uploadCallBack.onUploadStart("uploading started", "Jatin mandanka");
        }

        @Override
        protected String doInBackground(String... params) {
            String upload_url = "https://allevents.in/api/index.php/users/social/post_image";
            // HttpPost httpPost = new
            // HttpPost(CONSTANT.URLNEW+"users/social/postsasad_image");

            for (int i = 0; i < mSelectedImages.size(); i++) {
                uploadCallBack.onUploadSelect(i);
                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                HttpPost httpPost = new HttpPost(upload_url);
                try {
                    CustomMultiPartEntity entity = new CustomMultiPartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"), new CustomMultiPartEntity.ProgressListener() {
                        @Override
                        public void transferred(long num) {
                            // update the progress bar or whatever else you might want to do
                            System.out.println("============== ");
                        }
                    });
                    /*MultipartEntity entity = new MultipartEntity(
                            HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));*/

                    final File imgUrl = new File(mSelectedImages.get(i).path);
                    entity.addPart("message", new StringBody(mSelectedImages.get(i).caption, Charset.forName("UTF-8")));
                    entity.addPart("email", new StringBody(email));
                    entity.addPart("event_id", new StringBody(eventid));

                    Bitmap bitmap1 = null;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    options.inSampleSize = 2;
                    options.inDither = false;
                    options.inPurgeable = true;
                    options.inTempStorage = new byte[16 * 1024];
                    // Bitmap bitmap = BitmapFactory.decodeFile(imageInSD, null,
                    // options);
                    try {
                        bitmap1 = BitmapFactory.decodeFile(imgUrl.getPath(), options);
                    } catch (Exception | OutOfMemoryError e) {
                        e.printStackTrace();
                    }

                    if (bitmap1 != null) {
                        if (bitmap1.getWidth() > 1000) {
                            // resize
                            bitmap1 = resizeImage(bitmap1, 1000);
                            ByteArrayOutputStream bao = new ByteArrayOutputStream();
                            // compression with 50% quality ?
                            bitmap1.compress(Bitmap.CompressFormat.JPEG, 50, bao);
                            byte[] data = bao.toByteArray();
                            entity.addPart("image", new ByteArrayBody(data,
                                    "imagefile.jpg"));
                            bao.flush();
                            bao.close();
                        } else {
                            ByteArrayOutputStream bao = new ByteArrayOutputStream();
                            bitmap1.compress(Bitmap.CompressFormat.JPEG, 50, bao);
                            byte[] data = bao.toByteArray();
                            entity.addPart("image", new ByteArrayBody(data,
                                    "imagefile.jpg"));
                            bao.flush();
                            bao.close();
                        }
                    } else {
                        entity.addPart("image", new FileBody(imgUrl));
                    }

                    httpPost.setEntity(entity);

                    HttpResponse response = httpClient.execute(httpPost,
                            localContext);

                    // convert response to string

                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(response.getEntity().getContent()));
                    StringBuffer stringBuffer = new StringBuffer("");
                    String line = "";
                    String LineSeparator = System.getProperty("line.separator");
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuffer.append(line + LineSeparator);
                    }
                    bufferedReader.close();
                    if (i == (mSelectedImages.size() - 1)) return stringBuffer.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //LOGD(TAG, result);
            System.out.println("============= photo uploading task result " + result);
            if (result != null) {
                try {
                    JSONObject photoID = new JSONObject(result);
                    String id = new JSONObject(photoID.get("data")
                            .toString()).get("id").toString();
                    if (id != null) {
                        uploadCallBack.onCompleate(false, null);

						/*// user initiated another photo upload while we were
                        // uploading current photo
						if (helper.getCount(eventid) > 0) {
							PhotoUploadObj puo = helper
									.getPhotoToUpload(eventid);
							new photoUploadServer(activity,
									"Photo uploading... "
											+ helper.getCount(eventid)
											+ " to go.", puo.getEmail(),
									puo.getEvent_id(), puo.getCaption(),
									puo.getPhoto_uri(), puo.getId()).execute();
						}*/
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    uploadCallBack.onCompleate(true, null);
                }
            } else {
                uploadCallBack.onCompleate(true, null);
            }
        }
    }

}
