package cn.edu.scnu.face.FunnyChartlet;

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
 * Created by Vernon on 2016/1/1.
 */
public class FaceppLandmark
{
    public interface Callback
    {
        void success(JSONObject result);

        void error(Exception exception);
    }

    // 将照片bmp进行关键点，返回成功或者失败的结果
    public void landmark(final Bitmap bmp, final Callback callback)
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
                    bmpSamll.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                    byte[] arrays = stream.toByteArray();

                    // 人脸检测
                    JSONObject result = requests.detectionDetect(new PostParameters().setImg(arrays));
                    // 获得face_id
                    String faceID = result.getJSONArray("face").getJSONObject(0).getString("face_id");
                    // 人脸关键点检测(25个关键点)
                    JSONObject jsonObject = requests.detectionLandmark(new PostParameters()
                            .setFaceId(faceID)
                            .setType("25p"));

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

    // 获取特定关键点的坐标值
    public static double getLandmarkValue(JSONObject landmarkResult, String cornerName)
    {
        double value = 0.0;
        try {
            switch (cornerName) {
                case "left_eye_left_corner_x":
                    value = landmarkResult.getJSONArray("result").getJSONObject(0).getJSONObject("landmark")
                            .getJSONObject("left_eye_left_corner").getDouble("x");      // 左眼的左点的X
                    break;
                case "left_eye_right_corner_x":
                    value = landmarkResult.getJSONArray("result").getJSONObject(0).getJSONObject("landmark")
                            .getJSONObject("left_eye_right_corner").getDouble("x");     // 左眼的右点的X
                    break;
                case "left_eye_center_x":
                    value = landmarkResult.getJSONArray("result").getJSONObject(0).getJSONObject("landmark")
                            .getJSONObject("left_eye_center").getDouble("x");       // 左眼的中点的x
                    break;
                case "left_eye_center_y":
                    value = landmarkResult.getJSONArray("result").getJSONObject(0).getJSONObject("landmark")
                            .getJSONObject("left_eye_center").getDouble("y");       // 左眼的中点的y
                    break;

                case "right_eye_left_corner_x":
                    value = landmarkResult.getJSONArray("result").getJSONObject(0).getJSONObject("landmark")
                            .getJSONObject("right_eye_left_corner").getDouble("x");     // 右眼的左点的x
                    break;
                case "right_eye_right_corner_x":
                    value = landmarkResult.getJSONArray("result").getJSONObject(0).getJSONObject("landmark")
                            .getJSONObject("right_eye_right_corner").getDouble("x");    // 右眼的右点的x
                    break;
                case "right_eye_center_x":
                    value = landmarkResult.getJSONArray("result").getJSONObject(0).getJSONObject("landmark")
                            .getJSONObject("right_eye_center").getDouble("x");      // 右眼的中点的x
                    break;
                case "right_eye_center_y":
                    value = landmarkResult.getJSONArray("result").getJSONObject(0).getJSONObject("landmark")
                            .getJSONObject("right_eye_center").getDouble("y");      // 右眼的中点的y
                    break;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return value;
    }
}
