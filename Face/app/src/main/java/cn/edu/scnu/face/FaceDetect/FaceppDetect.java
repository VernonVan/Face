package cn.edu.scnu.face.FaceDetect;

import android.graphics.Bitmap;
import android.util.Log;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import cn.edu.scnu.face.Constant;

/**
 * Created by Vernon on 2015/11/21.
 */
public class FaceppDetect
{
    public interface Callback
    {
        void success(JSONObject result);

        void error(FaceppParseException exception);
    }

    // 将照片bmp进行检测，返回成功或者失败的结果
    public void detect(final Bitmap bmp, final Callback callback)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    HttpRequests requests = new HttpRequests(Constant.API_KEY, Constant.API_SECRET, true, true);
                    Bitmap bmpSamll = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight());
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmpSamll.compress(Bitmap.CompressFormat.JPEG, 70, stream);      // 压缩
                    byte[] arrays = stream.toByteArray();

                    PostParameters params = new PostParameters();
                    params.setImg(arrays);

                    // 人脸检测
                    JSONObject jsonObject = requests.detectionDetect(params);
                    Log.d("TAG", jsonObject.toString());
                    if (callback != null) {
                        callback.success(jsonObject);
                    }
                } catch (FaceppParseException e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.error(e);
                    }
                }
            }
        }).start();
    }
}
