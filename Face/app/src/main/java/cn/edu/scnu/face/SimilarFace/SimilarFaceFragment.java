package cn.edu.scnu.face.SimilarFace;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.easyandroidanimations.library.PuffInAnimation;
import com.hanks.htextview.HTextView;
import com.hanks.htextview.HTextViewType;
import com.soundcloud.android.crop.Crop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import cn.edu.scnu.face.FaceDetect.DetectState;
import cn.edu.scnu.face.FunnyChartlet.FaceppLandmark;
import cn.edu.scnu.face.GlobalPhotoApplication;
import cn.edu.scnu.face.R;
import yalantis.com.sidemenu.interfaces.ScreenShotable;

import static cn.edu.scnu.face.Util.BitmapUri.getBitmapFromUri;
import static cn.edu.scnu.face.Util.SupportImageType.imageTypeIsSupport;
import static cn.edu.scnu.face.Util.rotatePhoto.readPictureDegree;
import static cn.edu.scnu.face.Util.rotatePhoto.rotateBitmapToCorrectDirection;

/**
 * Created by Vernon on 2015/12/23.
 */
public class SimilarFaceFragment extends Fragment implements ScreenShotable, View.OnClickListener
{
    private HTextView similarResultTextView;
    private View containerView;
    private ImageView similarFace1ImageView;
    private ImageView similarFace2ImageView;
    private CircularProgressButton getSimilarIndexButton;

    private boolean hasFace1 = false;
    private boolean hasFace2 = false;
    private Bitmap screenShotBitmap;

    private final int MSG_SUCESS = 0x111;
    private final int MSG_ERROR = 0x112;
    private static final int PICK_FACE1_REQUEST = 0x113;
    private static final int CROP_FACE1_REQUEST = 0x114;
    private static final int PICK_FACE2_REQUEST = 0x115;
    private static final int CROP_FACE2_REQUEST = 0x116;

    // 单例模式
    private static class SimilarFaceFragmentHolder
    {
        private static final SimilarFaceFragment instance = new SimilarFaceFragment();
    }

    private SimilarFaceFragment()
    {
    }

    public static final SimilarFaceFragment getInstance()
    {
        return SimilarFaceFragmentHolder.instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.similar_face, container, false);
        return rootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        similarResultTextView.setText("");
        setDetectButtonState(DetectState.UnDetect);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
    }


    private void initViews(View view)
    {
        this.containerView = view.findViewById(R.id.similar_face_container);
        similarResultTextView = (HTextView) view.findViewById(R.id.similar_result_text);
        similarFace1ImageView = (ImageView) view.findViewById(R.id.similar_face_img1);
        similarFace1ImageView.setOnClickListener(this);
        similarFace2ImageView = (ImageView) view.findViewById(R.id.similar_face_img2);
        similarFace2ImageView.setOnClickListener(this);
        getSimilarIndexButton = (CircularProgressButton) view.findViewById(R.id.similar_index_button);
        getSimilarIndexButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        similarResultTextView.animateText("");
        setDetectButtonState(DetectState.UnDetect);

        switch (v.getId()) {
            case R.id.similar_index_button:
                getSimilarIndexResult();      // 获取两张人脸的比对结果
                break;
            case R.id.similar_face_img1:
                similarFace1ImageView.setClickable(false);
                Crop.pickImage(getActivity(), this, PICK_FACE1_REQUEST);     // 选择图片
                break;
            case R.id.similar_face_img2:
                similarFace1ImageView.setClickable(false);
                Crop.pickImage(getActivity(), this, PICK_FACE2_REQUEST);     // 选择图片
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == PICK_FACE1_REQUEST || requestCode == PICK_FACE2_REQUEST)) {
            similarFace1ImageView.setClickable(true);
            similarFace2ImageView.setClickable(true);
            if (resultCode == Activity.RESULT_OK) {
                // 选择图片后
                beginCrop(data.getData(), requestCode);
            }
        } else if (requestCode == CROP_FACE1_REQUEST || requestCode == CROP_FACE2_REQUEST) {
            // 图片编辑完后
            handleCrop(resultCode, requestCode, data);
        }
    }

    // 裁剪source指向的图片
    private void beginCrop(Uri source, int requestCode)
    {

        ContentResolver resolver = getActivity().getContentResolver();
        boolean isSupport = imageTypeIsSupport(resolver, source);
        if (isSupport) {
            Uri destination = Uri.fromFile(new File(getActivity().getCacheDir(), String.valueOf(requestCode)));
            if (requestCode == PICK_FACE1_REQUEST) {
                Crop.of(source, destination).withAspect(160, 240).start(getActivity(), this, CROP_FACE1_REQUEST);
            } else if (requestCode == PICK_FACE2_REQUEST) {
                Crop.of(source, destination).withAspect(160, 240).start(getActivity(), this, CROP_FACE2_REQUEST);
            }
        } else {
            Toast.makeText(getActivity(), R.string.error_img_type, Toast.LENGTH_SHORT).show();
        }
    }

    // 将裁剪好的图片设为当前图片
    private void handleCrop(int resultCode, int requestCode, Intent result)
    {
        if (resultCode == Activity.RESULT_OK) {
            Uri photoUri = Crop.getOutput(result);
            File myFile = new File(photoUri.getPath());
            int degree = readPictureDegree(myFile.getAbsolutePath());
            Bitmap croppedBmp = getBitmapFromUri(getActivity().getContentResolver(), photoUri);
            Bitmap rotatedBitmap = rotateBitmapToCorrectDirection(croppedBmp, degree);  // 将需要旋转的Bitmap进行旋转

            if (requestCode == CROP_FACE1_REQUEST) {
                hasFace1 = true;
                similarFace1ImageView.setImageBitmap(null);
                similarFace1ImageView.setImageBitmap(rotatedBitmap);
            } else if (requestCode == CROP_FACE2_REQUEST) {
                hasFace2 = true;
                similarFace2ImageView.setImageBitmap(null);
                similarFace2ImageView.setImageBitmap(rotatedBitmap);
            }
        } else if (resultCode == Crop.RESULT_ERROR) {
            Log.e("handleCrop Error", Crop.getError(result).getMessage());
        }

    }

    private void getSimilarIndexResult()
    {
        if (!hasFace1 || !hasFace2) {
            Toast.makeText(getActivity(), R.string.nullPhotoMessage, Toast.LENGTH_SHORT).show();
            return;
        }

        setDetectButtonState(DetectState.Detecting);
        try {
            FaceppSimilarIndex similarIndex = new FaceppSimilarIndex();
            Bitmap face1Bmp = ((BitmapDrawable) similarFace1ImageView.getDrawable()).getBitmap();
            Bitmap face2Bmp = ((BitmapDrawable) similarFace2ImageView.getDrawable()).getBitmap();
            similarIndex.compareTwoFace(face1Bmp, face2Bmp, new FaceppSimilarIndex.Callback()
            {
                @Override
                public void success(JSONObject result)
                {
                    Message msg = new Message();
                    msg.what = MSG_SUCESS;
                    msg.obj = result;
                    mHandler.sendMessage(msg);
                }

                @Override
                public void error(Exception exception)
                {
                    Message msg = new Message();
                    msg.what = MSG_ERROR;
                    msg.obj = exception;
                    mHandler.sendMessage(msg);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 检测成功或者失败
    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
                case MSG_SUCESS:
                    JSONObject rs = (JSONObject) msg.obj;
                    Log.d("similarity: ", rs.toString());
                    setDetectButtonState(DetectState.UnDetect);
                    showSimilarResult(rs);        // 展示相似指数比对结果
                    break;
                case MSG_ERROR:
                    String errorMsg = msg.obj.toString();
                    Log.e("get similarity Error!", errorMsg);
                    setDetectButtonState(DetectState.DetectError);
                    break;
            }

            super.handleMessage(msg);
        }
    };

    private void showSimilarResult(JSONObject similarityResult)
    {
        try {
            String similarIndex = String.valueOf((int) (similarityResult.getDouble("similarity")));
            new PuffInAnimation(similarResultTextView).setDuration(1000).animate();
            similarResultTextView.setAnimateType(HTextViewType.RAINBOW);
            similarResultTextView.animateText("相似度" + similarIndex + "%");
            Log.d("similarIndex", similarIndex);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    // 设置检测按钮的属性
    private void setDetectButtonState(DetectState state)
    {
        switch (state) {
            case UnDetect:
                getSimilarIndexButton.setProgress(0);
                break;
            case Detecting:
                getSimilarIndexButton.setIndeterminateProgressMode(true); // turn on indeterminate progress
                getSimilarIndexButton.setProgress(50);
                break;
            case DetectSuccessful:
                getSimilarIndexButton.setProgress(100);
                break;
            case DetectError:
                getSimilarIndexButton.setProgress(-1);
                break;
        }
    }

    @Override
    public void takeScreenShot()
    {
        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                Bitmap bitmap = Bitmap.createBitmap(containerView.getWidth(),
                        containerView.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                containerView.draw(canvas);
                SimilarFaceFragment.this.screenShotBitmap = bitmap;
            }
        };

        thread.start();

    }

    @Override
    public Bitmap getBitmap()
    {
        return screenShotBitmap;
    }
}
