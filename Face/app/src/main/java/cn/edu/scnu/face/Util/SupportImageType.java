package cn.edu.scnu.face.Util;

import android.content.ContentResolver;
import android.net.Uri;

/**
 * Created by Vernon on 2016/1/3.
 */
public class SupportImageType
{
    public static boolean imageTypeIsSupport(ContentResolver resolver, Uri imageUri)
    {
        String fileType = resolver.getType(imageUri);
        if (fileType.toLowerCase().contains("jpg") || fileType.toLowerCase().contains("png")
                || fileType.toLowerCase().contains("jpeg") ) {
            return true;
        }

        return false;
    }
}
