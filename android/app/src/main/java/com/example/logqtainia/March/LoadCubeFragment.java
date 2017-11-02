package com.example.logqtainia.March;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

import static com.example.logqtainia.March.ColorDetector.getColorName;
import static com.example.logqtainia.March.ColorDetector.getColorName2;


public class LoadCubeFragment extends Fragment {
    //View
    private RelativeLayout guideRelativeLayout;
    private Button btnTake;
    private Camera camera = null;
    private TextView[][] tvCapturedSquares = new TextView[MainActivity.SIZE][MainActivity.SIZE];
    private TextView tvCurrentFace;

    //Data
    private Bitmap bitmap;
    private int gap;            //采样点间距
    private int startX, startY; //采样初始点，左上角
    private int len = 10;            //采样边长
    private FaceColor[][] capturedFaces = new FaceColor[7][9];
    private int currentFace = 0;
    private Toast toast;
    private CameraPreview mPreview;
    public static final String FACES_ORDER = "UDFBLR"; //扫描顺序


    View view;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle saveInstanceState) {
        view = inflater.inflate(R.layout.fragment_load_cube, container, false);

        mPreview = new CameraPreview(getActivity());
        FrameLayout preview = (FrameLayout) view.findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        mPreview.getCameraInstance().setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                bitmap = decodeToBitMap(camera, data);

                //data的格式为NV21,下面的函数不管用
//                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                for (int i = 0; i < MainActivity.SIZE; i++) {
                    for (int j = 0; j < MainActivity.SIZE; j++) {
                        Bitmap roi = bitmap.createBitmap(bitmap,
                                startX + j * gap - len / 2, startY + i * gap - len / 2,
                                len, len);
                        float[] hsv = ColorDetector.averageColor(roi);
                        capturedFaces[currentFace][i * 3 + j] =
                                new FaceColor(hsv, currentFace * 10 + i * 3 + j);
//                            String colorName = ColorDetector.getColorName(hsv);
//                            state[i * 3 + j] = colorName;

//                            float[] hsv = new float[3];
//                            Color.RGBToHSV(color[0], color[1], color[2], hsv);
//                            tvCapturedSquares[i][j].setText("#" + colorToHex(color) + "\n" + "HSV:" + "\n" + hsv[0] + "\n" + hsv[1] + "\n" + hsv[2]);
//                            tvCapturedSquares[i][j].setTextColor(Color.rgb(color[0], color[1],color[2]));
                        tvCapturedSquares[i][j].setText("H: " + (int) (hsv[0] * 10) / 10.0 +
                                "\nS: " + (int) (hsv[1] * 10) / 10.0 +
                                "\nV: " + (int) (hsv[2] * 10) / 10.0);
                        tvCapturedSquares[i][j].setBackgroundColor(
                                Color.HSVToColor(new float[]{hsv[0], hsv[1], hsv[2]}));
                    }
                }
            }
        });

        guideRelativeLayout = (RelativeLayout) view.findViewById(R.id.guide_relative_layout);
        btnTake = (Button) view.findViewById(R.id.btn_take);
        if (((MainActivity) getActivity()).getAutoMode())
            btnTake.setText("AUTO TAKE");
        btnTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((MainActivity) getActivity()).getAutoMode())
                    new Thread(new Runnable() {
                        public void run() {
                            for (int i = 0; i < 7; i++) {
                                handler.sendEmptyMessage(0);
                                SystemClock.sleep(2000);
                            }
                        }
                    }).start();
//                for (int i = 0; i < 7; i++) {
//                    takePicture(v);
//                    Log.i("get color", currentFace + "");
//                    SystemClock.sleep(2000);
//                }
                else
                    takePicture(v);
            }
        });

        setSquares(view);
        setGuide();
        setSampleLocation();

//        DisplayMetrics dm = new DisplayMetrics();
//        // 获取屏幕信息
//        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
//        int screenWidth = dm.widthPixels;
//        int screenHeigh = dm.heightPixels;
//        Log.v("获取屏幕宽度", "宽度:" + screenWidth + ",高度:" + screenHeigh);

        return view;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                takePicture(getView());
            }
//                        super.handleMessage(msg);
        }
    };


    public void takePicture(View view) {
        if (currentFace > 5) {
            setResult();
            if (((MainActivity) getActivity()).getBTHelper() != null)
                ((MainActivity) getActivity()).getBTHelper().send(
                        (":Solve").getBytes());

            FragmentManager fm = getFragmentManager();
            FragmentTransaction tx = fm.beginTransaction();
            tx.remove(fm.findFragmentByTag("ShowCube"));
            tx.replace(R.id.container, new ShowCubeFragment(), "ShowCube"); 
            tx.commit();
//            getFragmentManager().popBackStack();
            return;
        }

        if (((MainActivity) getActivity()).getBTHelper() != null)
            ((MainActivity) getActivity()).getBTHelper().send(
                    ("Rot").getBytes());

//        for (int i = 0; i < MainActivity.SIZE * MainActivity.SIZE; i++) {
//            capturedFaces[currentFace][i] = state[i];
//        }
        showToast("Face " + MainActivity.FACES_ORDER.charAt(currentFace) + " is taken.");
        currentFace++;
        if (currentFace <= 5)
            tvCurrentFace.setText("current face: " + MainActivity.FACES_ORDER.charAt(currentFace));
        else {
            tvCurrentFace.setText("all faces are finished");
            btnTake.setText("Finish");
        }
    }

    private void setResult() {
//        Intent intent = new Intent();
//        StringBuilder[] data = getColorName(capturedFaces);
//        for (int i = 0; i < 6; i++) {
////            intent.putExtra(MainActivity.FACES_ORDER.charAt(i) + "", capturedFaces[i][4]);
//            intent.putExtra(MainActivity.FACES_ORDER.charAt(i) + "", data[i].toString());
//        }
//        intent.putExtra("state", data[6].toString());
//        return intent;

        StringBuilder[] data = getColorName2(capturedFaces);
        String[] colorName = new String[6];
        String cubeString;
        for (int i = 0; i < 6; i++) colorName[i] = data[i].toString();
        cubeString = data[6].toString();
        ((MainActivity) getActivity()).setColorName(colorName);
        ((MainActivity) getActivity()).setCubeString(cubeString);
    }

    private void showToast(String text) {
        if (toast == null) {
            toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            toast.setText(text);
            toast.show();
        }
    }

    private void setSampleLocation() {
//        Camera.Size size = camera.getParameters().getPreviewSize();
        Camera.Size previewSize = mPreview.getCameraInstance().getParameters().getPreviewSize();
//        Log.i("carmera parameter", size.width + " " + size.height);
        int h = previewSize.width; //相机是横着的
        int w = previewSize.height;
        gap = Math.min(w, h) / 4;
        int cX = w / 2;
        int cY = gap * 3 / 2;
        startX = cX - gap;
        startY = cY - gap;
    }


    public Bitmap decodeToBitMap(Camera camera, byte[] data) {
        Camera.Size size = camera.getParameters().getPreviewSize();
        Log.i("decode", size.width + " " + size.height);
//        int w = guideRelativeLayout.getRight();
//        int h = guideRelativeLayout.getBottom();
//        size.width = w;
//        size.height= h;
        try {
            YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width,
                    size.height, null);
            if (image != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compressToJpeg(new Rect(0, 0, size.width, size.height),
                        80, stream);

//                File file = new File(Environment.getExternalStorageDirectory(), "out.jpg");
//                FileOutputStream filecon = new FileOutputStream(file);
//                image.compressToJpeg(new Rect(0, 0, size.width, size.height),
//                        100, filecon);

                Bitmap bmp = BitmapFactory.decodeByteArray(
                        stream.toByteArray(), 0, stream.size());

                //rotate the bitmap
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0,
                        bmp.getWidth(), bmp.getHeight(), matrix, true);

                stream.close();
                return rotatedBitmap;
            }
        } catch (Exception ex) {
            Log.e("Sys", "Error:" + ex.getMessage());
        }
        return null;
    }

    private void setSquares(View view) {
        // Setup squares at the top right corner
        int s = dpToPx(150) / MainActivity.SIZE;
//        LinearLayout cpw = (LinearLayout) findViewById(R.id.colorPreviewWrapper);
        LinearLayout cpw = (LinearLayout) view.findViewById(R.id.square_linear_layout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(s, s);
        int margin = (int) (0.1 * s);
        lp.setMargins(margin, margin, margin, margin);
        for (int i = 0; i < MainActivity.SIZE; i++) {
            LinearLayout l = new LinearLayout(view.getContext());
            l.setOrientation(LinearLayout.HORIZONTAL);
            for (int j = 0; j < MainActivity.SIZE; j++) {
                TextView tv = new TextView(view.getContext());
                tv.setTextSize(10);
                tv.setLayoutParams(lp);
                l.addView(tv);
                tvCapturedSquares[i][j] = tv;
                tvCapturedSquares[i][j].setBackgroundColor(0xFFFFFFFF);
            }
            cpw.addView(l);
        }
//        TextView oriGuide = new TextView(this);
//        oriGuide.setText("TL");
//        cpw.addView(oriGuide);
        TextView tv = new TextView(view.getContext());
        tv.setText("current face: " + MainActivity.FACES_ORDER.charAt(currentFace));
        tv.setPadding(margin, margin, margin, margin);
        tv.setBackgroundColor(0xF0FFFFFF);
//        tv.setTextColor(0xFFFFFF00);
        tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        cpw.addView(tv);
        tvCurrentFace = tv;
    }

    public int dpToPx(int dp) {
//        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
//        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, getResources().getDisplayMetrics());
        return px;
    }

    //用来设置定位的九个点
    private void setGuide() {
        //Setup guide squares
//        float endX = surfaceView.getRight();
//        float endY = surfaceView.getBottom();

//        gap = (int) Math.min(endX, endY) / 4;
//        int centerX = (int) (endX / 2);
//        int centerY = (int) (endY - gap * 3 / 2);
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeigh = dm.heightPixels;

        int h = screenHeigh;
        int w = screenWidth;
        Log.i("guide square", h + " " + w);
        int guideGap = Math.min(w, h) / 4;
        int cX = w / 2;
        int cY = guideGap * 3 / 2;
        int guideStartX = cX - guideGap;
        int guideStartY = cY - guideGap;

        len = dpToPx(10);
//        System.out.print(endX + '\n' + endY);
//        Log.i("Tag_demo", endX - guideRelativeLayout.getLeft() + " " + (endY - guideRelativeLayout.getTop()));
//        Log.i("Tag_demo", guideRelativeLayout.getScaleX() + "");
//        Log.i("Tag_demo", guideRelativeLayout.getY() + "");
//        Log.i("Tag_demo", guideRelativeLayout.getHeight() + "");
//        Log.i("Tag_demo", surfaceView.getWidth() + "");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                TextView tv = new TextView(view.getContext());
//                tv.setTextSize(10);
//                tv.setText("ere");
                tv.setX(guideStartX + guideGap * i - len / 2);
                tv.setY(guideStartY + guideGap * j - len / 2);
                tv.setWidth(len);
                tv.setHeight(len);
                tv.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
//                tv.setLayoutParams(lp);
                guideRelativeLayout.addView(tv);
            }
        }
    }
}
