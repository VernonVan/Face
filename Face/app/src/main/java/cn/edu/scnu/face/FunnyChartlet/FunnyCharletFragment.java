package cn.edu.scnu.face.FunnyChartlet;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.facepp.error.FaceppParseException;
import com.soundcloud.android.crop.Crop;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import cn.edu.scnu.face.FaceDetect.DetectState;
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
public class FunnyCharletFragment extends Fragment implements ScreenShotable, View.OnClickListener
{
    // 上传照片的View
    private RelativeLayout uploadPhotoLayout;
    private ImageView photoImageView;
    //  private Button selectPhotoButton;
    private CircularProgressButton detectPhotoButton;

    // 趣味贴图的View
    private View containerView;
    private ImageView funnyChartletPhoto;
    private ImageButton eye1Button;
    private ImageButton eye2Button;
    private ImageButton eye3Button;
    private ImageButton eye4Button;
    private ImageButton eye5Button;
    private ImageButton eye6Button;
    private ImageButton eye7Button;
    private ImageButton eye8Button;
    private ImageButton eye9Button;

    private Bitmap screenShotBitmap;
    private JSONObject landmarkResult;      // 保存人脸关键点检测的结果

    private final int MSG_SUCESS = 0x111;
    private final int MSG_ERROR = 0x112;

    // 单例模式
    private static class FunnyCharletFragmentHolder
    {
        private static final FunnyCharletFragment instance = new FunnyCharletFragment();
    }

    private FunnyCharletFragment()
    {
    }

    public static final FunnyCharletFragment getInstance()
    {
        return FunnyCharletFragmentHolder.instance;
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
        detectPhotoButton.setText(R.string.funnyChartlet);
        Bitmap photoBmp = ((GlobalPhotoApplication) getActivity().getApplication()).getPhotoBmp();
        if (photoBmp!=null) {
            photoImageView.setImageBitmap(photoBmp);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.funny_chartlet, container, false);
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
        detectPhotoButton.setText(R.string.funnyChartlet);
        detectPhotoButton.setOnClickListener(this);
        photoImageView = (ImageView) view.findViewById(R.id.photo_img);
        photoImageView.setOnClickListener(this);

        // 趣味贴图的View
        this.containerView = view.findViewById(R.id.funny_chartlet_container);
        funnyChartletPhoto = (ImageView) view.findViewById(R.id.funny_chartlet_photo);
        funnyChartletPhoto.setOnClickListener(this);
        eye1Button = (ImageButton) view.findViewById(R.id.eye1);
        eye1Button.setOnClickListener(this);
        eye2Button = (ImageButton) view.findViewById(R.id.eye2);
        eye2Button.setOnClickListener(this);
        eye3Button = (ImageButton) view.findViewById(R.id.eye3);
        eye3Button.setOnClickListener(this);
        eye4Button = (ImageButton) view.findViewById(R.id.eye4);
        eye4Button.setOnClickListener(this);
        eye5Button = (ImageButton) view.findViewById(R.id.eye5);
        eye5Button.setOnClickListener(this);
        eye6Button = (ImageButton) view.findViewById(R.id.eye6);
        eye6Button.setOnClickListener(this);
        eye7Button = (ImageButton) view.findViewById(R.id.eye7);
        eye7Button.setOnClickListener(this);
        eye8Button = (ImageButton) view.findViewById(R.id.eye8);
        eye8Button.setOnClickListener(this);
        eye9Button = (ImageButton) view.findViewById(R.id.eye9);
        eye9Button.setOnClickListener(this);
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
                detectPhoto(photoBmp);          // 进行人脸关键点检测
            } else {
                Toast.makeText(getActivity(), R.string.nullPhotoMessage, Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.funny_chartlet_photo) {
            showChangePhotoDialog();
        } else {
            String eyeName = v.getTag().toString();
            Bitmap photoBmp = ((GlobalPhotoApplication) getActivity().getApplication()).getPhotoBmp();

            funnyChartletPhoto.setImageBitmap(null);
            Bitmap bmpWithEyes = addEyesOntoPhoto(photoBmp, eyeName);       // 将照片贴上趣味眼睛
            funnyChartletPhoto.setImageBitmap(bmpWithEyes);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Crop.REQUEST_PICK) {
            photoImageView.setClickable(true);
            if (resultCode == Activity.RESULT_OK){
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
            Log.d("degree", String.valueOf(degree));
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
        detectFaceLandmark(faceBmp);       // 进行人脸关键点检测
    }

    // 获取一张人脸的25个关键点信息
    private void detectFaceLandmark(Bitmap faceBmp)
    {
        FaceppLandmark faceLandmark = new FaceppLandmark();
        faceLandmark.landmark(faceBmp, new FaceppLandmark.Callback()
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
                    Log.d("Landmark: ", rs.toString());
                    setDetectButtonState(DetectState.DetectSuccessful);
                    landmarkResult = rs;
                    showFunnyChartletView();       // 显示趣味贴图的界面
                    break;
                case MSG_ERROR:
                    String errorMsg = msg.obj.toString();
                    Log.e("Detect Landmark Error!", errorMsg);
                    setDetectButtonState(DetectState.DetectError);
                    break;
            }

            super.handleMessage(msg);
        }
    };

    // 显示趣味贴图的界面
    private void showFunnyChartletView()
    {
        uploadPhotoLayout.setVisibility(View.INVISIBLE);
        containerView.setVisibility(View.VISIBLE);

        funnyChartletPhoto.setImageBitmap(null);
        Bitmap faceBmp = ((GlobalPhotoApplication) getActivity().getApplication()).getPhotoBmp();
        funnyChartletPhoto.setImageBitmap(faceBmp);
    }

    // 在人脸上添加趣味眼睛
    private Bitmap addEyesOntoPhoto(Bitmap photoBmp, String eyeName)
    {
        Bitmap desBitmap = photoBmp.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(desBitmap);
        JSONObject rs = landmarkResult;

        // 添加左眼
        double leftEyeWidth = FaceppLandmark.getLandmarkValue(rs, "left_eye_right_corner_x")
                - FaceppLandmark.getLandmarkValue(rs, "left_eye_left_corner_x");    // 图上的左眼的宽度
        int desLeftEyeWidth = (int) (desBitmap.getWidth() / 100 * leftEyeWidth * 2.0);  // 趣味左眼的宽度
        int leftEyeResId = getActivity().getResources()
                .getIdentifier(eyeName + "_left", "mipmap", getActivity().getPackageName());
        Bitmap leftEyeBmp = BitmapFactory.decodeResource(getResources(), leftEyeResId);
        //缩放趣味左眼
        Bitmap scaledLeftEyeBmp = Bitmap.createScaledBitmap(leftEyeBmp, desLeftEyeWidth, desLeftEyeWidth, false);
        double leftEyeCenterX = FaceppLandmark.getLandmarkValue(rs, "left_eye_center_x");
        double leftEyeCenterY = FaceppLandmark.getLandmarkValue(rs, "left_eye_center_y");
        float pointX = (float) (canvas.getWidth() / 100 * leftEyeCenterX * 1.04 - scaledLeftEyeBmp.getWidth() / 2);
        float pointY = (float) (canvas.getHeight() / 100 * leftEyeCenterY * 1.02 - scaledLeftEyeBmp.getHeight() / 2);
        canvas.drawBitmap(scaledLeftEyeBmp, pointX, pointY, null);      // 画上左眼

        // 添加右眼
        double rigthEyeWidth = FaceppLandmark.getLandmarkValue(rs, "right_eye_right_corner_x")
                - FaceppLandmark.getLandmarkValue(rs, "right_eye_left_corner_x");    // 图上的左眼的宽度
        int desRightEyeWidth = (int) (desBitmap.getWidth() / 100 * rigthEyeWidth * 2.0);  // 趣味右眼的宽度
        int rightEyeResId = getActivity().getResources()
                .getIdentifier(eyeName + "_right", "mipmap", getActivity().getPackageName());
        Bitmap rigthEyeBmp = BitmapFactory.decodeResource(getResources(), rightEyeResId);
        //缩放趣味右眼
        Bitmap scaledRightEyeBmp = Bitmap.createScaledBitmap(rigthEyeBmp, desRightEyeWidth, desRightEyeWidth, false);
        double rightEyeCenterX = FaceppLandmark.getLandmarkValue(rs, "right_eye_center_x");
        double rightEyeCenterY = FaceppLandmark.getLandmarkValue(rs, "right_eye_center_y");
        float rightPointX = (float) (canvas.getWidth() / 100 * rightEyeCenterX * 1.04 - scaledRightEyeBmp.getWidth() / 2);
        float rightPointY = (float) (canvas.getHeight() / 100 * rightEyeCenterY * 1.02 - scaledRightEyeBmp.getHeight() / 2);
        canvas.drawBitmap(scaledRightEyeBmp, rightPointX, rightPointY, null);      // 画上右眼

        canvas.save(Canvas.ALL_SAVE_FLAG);//保存
        canvas.restore();       //存储

        return desBitmap;
    }

    // 更换照片
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
                FunnyCharletFragment.this.screenShotBitmap = screenShotBitmap;
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
