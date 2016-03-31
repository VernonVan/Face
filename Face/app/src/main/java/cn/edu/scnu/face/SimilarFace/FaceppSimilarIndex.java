package cn.edu.scnu.face.SimilarFace;

import android.graphics.Bitmap;
import android.util.Log;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import cn.edu.scnu.face.Constant;

/**
 * Created by Vernon on 2016/1/2.
 */
public class FaceppSimilarIndex
{
    public interface Callback
    {
        void success(JSONObject result);

        void error(Exception exception);
    }

    // 将两张照片进行检测，返回成功或者失败的结果
    public void compareTwoFace(final Bitmap face1, final Bitmap face2, final Callback callback)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    HttpRequests requests = new HttpRequests(Constant.API_KEY, Constant.API_SECRET, true, true);
                    Bitmap samllBmp1 = Bitmap.createBitmap(face1, 0, 0, face1.getWidth(), face1.getHeight());
                    ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
                    samllBmp1.compress(Bitmap.CompressFormat.JPEG, 70, stream1);
                    byte[] arrays = stream1.toByteArray();
                    JSONObject result = requests.detectionDetect(new PostParameters().setImg(arrays));
                    String faceID1 = result.getJSONArray("face").getJSONObject(0).getString("face_id"); // 获得第一张脸的face_id

                    Bitmap samllBmp2 = Bitmap.createBitmap(face2, 0, 0, face2.getWidth(), face2.getHeight());
                    ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                    samllBmp2.compress(Bitmap.CompressFormat.JPEG, 70, stream2);
                    byte[] arrays2 = stream2.toByteArray();
                    JSONObject result2 = requests.detectionDetect(new PostParameters().setImg(arrays2));
                    String faceID2 = result2.getJSONArray("face").getJSONObject(0).getString("face_id"); // 获得第一张脸的face_id

                    JSONObject jsonObject = requests.recognitionCompare(new PostParameters()
                            .setFaceId1(faceID1)
                            .setFaceId2(faceID2));

                    if (callback != null) {
                        callback.success(jsonObject);
                    }
                } catch (FaceppParseException e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.error(e);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.error(e);
                    }
                }
            }
        }).start();
    }
}
