package cn.edu.scnu.face.Util;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.IOException;

/**
 * Created by Vernon on 2016/1/3.
 */
public class BitmapUri
{
    public static Bitmap getBitmapFromUri(ContentResolver contentResolver, Uri photoUri)
    {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, photoUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }
}
