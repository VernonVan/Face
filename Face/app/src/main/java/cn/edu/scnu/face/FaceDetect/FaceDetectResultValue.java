package cn.edu.scnu.face.FaceDetect;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Vernon on 2015/12/31.
 */
public class FaceDetectResultValue
{
    public static String getValueFromFaceResult(JSONObject result, String attrName)
    {
        String attrValue = "";
        try {
            switch (attrName) {
                case "age":
                    attrValue = String.valueOf(result.getJSONArray("face").getJSONObject(0).getJSONObject("attribute")
                            .getJSONObject("age").getInt("value"));
                    attrValue = transAgeValue(attrValue);
                    break;
                case "gender":
                    attrValue = result.getJSONArray("face").getJSONObject(0).getJSONObject("attribute")
                            .getJSONObject("gender").getString("value");
                    attrValue = transGenderValue(attrValue);
                    break;
                case "smile":
                    attrValue = String.valueOf(result.getJSONArray("face").getJSONObject(0).getJSONObject("attribute")
                            .getJSONObject("smiling").getDouble("value"));
                    attrValue = transSmileValue(attrValue);
                    break;
                case "race":
                    attrValue = result.getJSONArray("face").getJSONObject(0).getJSONObject("attribute")
                            .getJSONObject("race").getString("value");
                    attrValue = transRaceValue(attrValue);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return attrValue;
    }

    // 年龄信息
    private static String transAgeValue(String age)
    {
        return age + "岁";
    }

    // 性别信息
    private static String transGenderValue(String gender)
    {
        if (gender.equals("Male"))
            return "男性";
        return "女性";
    }

    // 笑的信息
    private static String transSmileValue(String smile)
    {
        double smileRate = Double.valueOf(smile);
        if (smileRate <= 30) {
            return "并没有笑";
        } else if (smileRate <= 50) {
            return "微笑";
        } else if (smileRate <= 90) {
            return "眉开眼笑";
        } else {
            return "开怀大笑";
        }
    }

    // 人种信息
    private static String transRaceValue(String race)
    {
        switch (race) {
            case "Asian":
                return "黄色人种";
            case "White":
                return "白色人种";
            case "Black":
                return "黑色人种";
            default:
                return "其他人种";
        }
    }
}
