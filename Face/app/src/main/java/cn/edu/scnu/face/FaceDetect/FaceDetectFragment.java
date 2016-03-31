package cn.edu.scnu.face.FaceDetect;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.easyandroidanimations.library.ScaleInAnimation;
import com.facepp.error.FaceppParseException;
import com.soundcloud.android.crop.Crop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

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
public class FaceDetectFragment extends Fragment implements ScreenShotable, View.OnClickListener
{
    // 上传图片的View
    private RelativeLayout uploadPhotoLayout;
    private ImageView photoImageView;
    private CircularProgressButton detectPhotoButton;

    // 显示结果的View
    private View containerView;
    private ImageView photoResultImageView;
    private TextView ageTextView;
    private TextView genderTextView;
    private TextView smileTextView;
    private TextView raceTextView;

    private Bitmap screenShotBitmap;            // Reveal动画所需要的截图

    private final int MSG_SUCESS = 0x111;
    private final int MSG_ERROR = 0x112;

    // 单例模式
    private static class FaceDetectFragmentHolder
    {
        private static final FaceDetectFragment instance = new FaceDetectFragment();
    }

    private FaceDetectFragment()
    {
    }

    public static final FaceDetectFragment getInstance()
    {
        return FaceDetectFragmentHolder.instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        // 恢复场景
        setDetectButtonState(DetectState.UnDetect);
        Bitmap photoBmp = ((GlobalPhotoApplication) getActivity().getApplication()).getPhotoBmp();
        if (photoBmp!=null) {
            photoImageView.setImageBitmap(photoBmp);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.face_detect, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
    }

    private void initViews(View view)
    {
        // 上传照片的View
        uploadPhotoLayout = (RelativeLayout) view.findViewById(R.id.upload_photo);
        detectPhotoButton = (CircularProgressButton) uploadPhotoLayout.findViewById(R.id.detect_photo_button);
        detectPhotoButton.setOnClickListener(this);
        photoImageView = (ImageView) view.findViewById(R.id.photo_img);
        photoImageView.setOnClickListener(this);

        // 显示结果的View
        this.containerView = view.findViewById(R.id.face_detect_container);
        photoResultImageView = (ImageView) view.findViewById(R.id.detect_result_img);
        photoResultImageView.setOnClickListener(this);
        ageTextView = (TextView) view.findViewById(R.id.age_text);
        genderTextView = (TextView) view.findViewById(R.id.gender_text);
        smileTextView = (TextView) view.findViewById(R.id.smile_text);
        raceTextView = (TextView) view.findViewById(R.id.race_text);
    }


    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.photo_img) {
            photoImageView.setClickable(false);
            Crop.pickImage(getActivity(), this);        // 选择图片
            setDetectButtonState(DetectState.UnDetect);
        } else if (v.getId() == R.id.detect_photo_button) {
            Bitmap photoBmp = ((GlobalPhotoApplication) getActivity().getApplication()).getPhotoBmp();
            if (photoBmp != null) {
                detectPhoto(photoBmp);          // 进行人脸检测
            } else {
                Toast.makeText(getActivity(), R.string.nullPhotoMessage, Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.detect_result_img) {
            showChangePhotoDialog();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Crop.REQUEST_PICK) {
            photoImageView.setClickable(true);
            if (resultCode == Activity.RESULT_OK) {
                // 选择图片后
                beginCrop(data.getData());
            }

        } else if (requestCode == Crop.REQUEST_CROP) {
            // 图片编辑完后
            handleCrop(resultCode, data);
        }
    }


    // 裁剪source指向的图片
    private void beginCrop(Uri source)
    {
        ContentResolver resolver = getActivity().getContentResolver();
        boolean isSupport = imageTypeIsSupport(resolver, source);
        if (isSupport) {
            Uri destination = Uri.fromFile(new File(getActivity().getCacheDir(), "cropped"));
            Crop.of(source, destination).withAspect(300, 250).start(getActivity(), this);
        } else {
            Toast.makeText(getActivity(), R.string.error_img_type, Toast.LENGTH_SHORT).show();
        }
    }

    // 将裁剪好的图片设为当前图片
    private void handleCrop(int resultCode, Intent result)
    {
        if (resultCode == Activity.RESULT_OK) {
            Uri photoUri = Crop.getOutput(result);
            File myFile = new File(photoUri.getPath());
            int degree = readPictureDegree(myFile.getAbsolutePath());
            Bitmap croppedBmp = getBitmapFromUri(getActivity().getContentResolver(), photoUri);
            Bitmap rotatedBitmap = rotateBitmapToCorrectDirection(croppedBmp, degree);  // 将需要旋转的Bitmap进行旋转

            photoImageView.setImageBitmap(null);
            photoImageView.setImageBitmap(rotatedBitmap);
            ((GlobalPhotoApplication) getActivity().getApplication()).setPhotoBmp(rotatedBitmap);   // 保存该bitmap
        } else if (resultCode == Crop.RESULT_ERROR) {
            Log.e("handleCrop Error", Crop.getError(result).getMessage());
        }
    }

    // 将photoUri的照片进行检测
    private void detectPhoto(Bitmap faceBmp)
    {
        setDetectButtonState(DetectState.Detecting);
        detectFace(faceBmp);       // 进行人脸检测
    }

    // 检测一张人脸
    private void detectFace(Bitmap faceBmp)
    {
        FaceppDetect FaceDetect = new FaceppDetect();
        FaceDetect.detect(faceBmp, new FaceppDetect.Callback()
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
            public void error(FaceppParseException exception)
            {
                Message msg = new Message();
                msg.what = MSG_ERROR;
                msg.obj = exception;
                mHandler.sendMessage(msg);
            }
        });
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
                    Log.d("JSONObject", rs.toString());
                    detectFaceSuccessful(rs);
                    break;
                case MSG_ERROR:
                    String errorMsg = msg.obj.toString();
                    Log.e("DetectError", errorMsg);
                    setDetectButtonState(DetectState.DetectError);
                    break;
            }

            super.handleMessage(msg);
        }
    };

    private void detectFaceSuccessful(JSONObject rs)
    {
        try {
            if (rs.getJSONArray("face").length() > 0) {
                showDetectResultView(rs);       // 显示检测结果的界面
                setDetectButtonState(DetectState.DetectSuccessful);
            } else {
                Toast.makeText(getActivity(), R.string.zero_face, Toast.LENGTH_SHORT).show();
                setDetectButtonState(DetectState.DetectError);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showDetectResultView(JSONObject rs)
    {
        uploadPhotoLayout.setVisibility(View.INVISIBLE);
        containerView.setVisibility(View.VISIBLE);

        photoResultImageView.setImageBitmap(null);
        Bitmap faceBmp = ((GlobalPhotoApplication) getActivity().getApplication()).getPhotoBmp();
        photoResultImageView.setImageBitmap(faceBmp);

        setResultTextViewsValues(rs);       // 获得各项信息并设置到对应的TextView
        showResultTextViewsAnimation();     // 动画效果显示结果的TextView
    }

    // 获得各项信息并设置到对应的TextView
    private void setResultTextViewsValues(JSONObject rs)
    {
        String age = FaceDetectResultValue.getValueFromFaceResult(rs, "age");
        ageTextView.setText(age);
        String gender = FaceDetectResultValue.getValueFromFaceResult(rs, "gender");
        genderTextView.setText(gender);
        String smile = FaceDetectResultValue.getValueFromFaceResult(rs, "smile");
        smileTextView.setText(smile);
        String race = FaceDetectResultValue.getValueFromFaceResult(rs, "race");
        raceTextView.setText(race);
    }

    // 动画效果显示结果的TextView
    private void showResultTextViewsAnimation()
    {
        new ScaleInAnimation(ageTextView).animate();
        new ScaleInAnimation(smileTextView).animate();
        new ScaleInAnimation(genderTextView).animate();
        new ScaleInAnimation(raceTextView).animate();
    }

    private void showChangePhotoDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog alert = builder
                .setMessage("是否更换照片")
                .setPositiveButton("更换照片", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        changePhoto();
                    }
                })
                .setNegativeButton("取消", null)
                .create();
        alert.show();
    }

    private void changePhoto()
    {
        containerView.setVisibility(View.INVISIBLE);
        setDetectButtonState(DetectState.UnDetect);
        uploadPhotoLayout.setVisibility(View.VISIBLE);

    }

    // 设置检测按钮的属性
    private void setDetectButtonState(DetectState state)
    {
        switch (state) {
            case UnDetect:
                detectPhotoButton.setProgress(0);
                break;
            case Detecting:
                detectPhotoButton.setIndeterminateProgressMode(true); // turn on indeterminate progress
                detectPhotoButton.setProgress(50);
                break;
            case DetectSuccessful:
                detectPhotoButton.setProgress(100);
                break;
            case DetectError:
                detectPhotoButton.setProgress(-1);
                break;
        }
    }


    // Reveal效果实现翻页动画的辅助函数：takeScreen获得屏幕快照，getBitmap返回该快照
    @Override
    public void takeScreenShot()
    {
        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                Bitmap screenShotBitmap = Bitmap.createBitmap(containerView.getWidth(),
                        containerView.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(screenShotBitmap);
                containerView.draw(canvas);
                FaceDetectFragment.this.screenShotBitmap = screenShotBitmap;
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
