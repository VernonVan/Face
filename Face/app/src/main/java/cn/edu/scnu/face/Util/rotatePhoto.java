package cn.edu.scnu.face.Util;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.IOException;

/**
 * Created by Vernon on 2016/1/3.
 */
public class rotatePhoto
{
    public static int readPictureDegree(String path)
    {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation
                    = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static Bitmap rotateBitmapToCorrectDirection(Bitmap srcBmp, int degree)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        //   Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmapOrg,width,height,true);
        assert srcBmp != null;
        Bitmap rotatedBitmap = Bitmap.createBitmap(srcBmp, 0, 0, srcBmp.getWidth(),
                srcBmp.getHeight(), matrix, true);

        return rotatedBitmap;
    }
}
