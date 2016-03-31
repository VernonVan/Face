package cn.edu.scnu.face;

import android.app.Application;
import android.graphics.Bitmap;

/**
 * Created by Vernon on 2015/12/28.
 */
public class GlobalPhotoApplication extends Application
{
    Bitmap photoBmp = null;

    public Bitmap getPhotoBmp()
    {
        return photoBmp;
    }

    public void setPhotoBmp(Bitmap photoBmp)
    {
        this.photoBmp = photoBmp;
    }
}
