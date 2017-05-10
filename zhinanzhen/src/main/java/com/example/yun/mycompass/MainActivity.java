package com.example.yun.mycompass;

import android.app.AlertDialog;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements SensorEventListener {
    ImageView mArrow, mFace;
    TextView mInfo, mAccuracy, mDirection;
    SensorManager mSensorManager;
    private float currentDegree = 0f;
    AlertDialog mDialog;
    boolean isVertical;
    SurfaceView mSurface;
    SurfaceHolder mHolder;
    Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mArrow = (ImageView) findViewById(R.id.arrow);
        mFace = (ImageView) findViewById(R.id.face);
        mInfo = (TextView) findViewById(R.id.tv_info);
        mAccuracy = (TextView) findViewById(R.id.tv_accuracy);
        mSensorManager = (SensorManager)
                getSystemService(SENSOR_SERVICE);

        mDialog = new AlertDialog.Builder(this).setTitle("提示")
                .setMessage("检测到周围有磁场干扰，请在空中划8字校准")
                .create();

        mSurface = (SurfaceView) findViewById(R.id.surface);
        mDirection = (TextView) findViewById(R.id.tv_direction);
        mHolder = mSurface.getHolder();
        mHolder.setKeepScreenOn(true);
        mCamera = Camera.open();
        mCamera.setDisplayOrientation(90);
        Camera.Parameters pa = mCamera.getParameters();
        pa.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        mCamera.setParameters(pa);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_UI);
    }


    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    float[] aValues, mValues;

    String[] accuracyList = new String[]{
            "不可信", "低", "中", "高"
    };

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                aValues = event.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mValues = event.values.clone();
                mAccuracy.setText("准确度：" + accuracyList[event.accuracy] + "\n");
                break;
        }
        float[] rMatrix = null;
        if (aValues != null && mValues != null) {
            rMatrix = new float[16];
            SensorManager.getRotationMatrix(rMatrix, null, aValues, mValues);
        }
        if (rMatrix != null) {
            float[] oValues = new float[3];
            SensorManager.getOrientation(rMatrix, oValues);
            double azimuth = Math.toDegrees(oValues[0]);
            double pitch = Math.toDegrees(oValues[1]);
            double roll = Math.toDegrees(oValues[2]);

            boolean isCurVertical;
            if (isVertical) { //之前是否竖立
                isCurVertical = Math.abs(pitch) > 40; //当前是否竖立
            } else {
                isCurVertical = Math.abs(pitch) > 50;
            }

            RotateAnimation ra = new RotateAnimation(
                    currentDegree, (float) -azimuth,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            ra.setDuration(200);
            if (!isCurVertical) {
                mArrow.startAnimation(ra);
            } else {
                mDirection.setText(getDerection(azimuth));
            }

            if (isCurVertical != isVertical) {
                AlphaAnimation aa = null;
                AlphaAnimation aa1 = new AlphaAnimation(0f, 1f);
                AlphaAnimation aa2 = new AlphaAnimation(1f, 0f);
                aa1.setDuration(200);
                aa2.setDuration(200);
                if (isVertical) {
                    mArrow.setVisibility(View.VISIBLE);
                    mFace.setVisibility(View.VISIBLE);
                    aa = aa1;

                    mSurface.setVisibility(View.GONE);
                    mDirection.setVisibility(View.GONE);
                    mCamera.stopPreview();
                    mSurface.setAnimation(aa2);
                    mDirection.setAnimation(aa2);
                } else {
                    mArrow.setVisibility(View.GONE);
                    mFace.setVisibility(View.GONE);
                    aa = aa2;

                    mSurface.setVisibility(View.VISIBLE);
                    mDirection.setVisibility(View.VISIBLE);
                    try {
                        mCamera.setPreviewDisplay(mHolder);
                        mCamera.startPreview();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mSurface.setAnimation(aa1);
                    mDirection.setAnimation(aa1);
                }
                AnimationSet set = new AnimationSet(false);
                if (ra != null) {
                    set.addAnimation(ra);
                }
                set.addAnimation(aa);
                mArrow.startAnimation(set);
                mFace.startAnimation(aa);
            }

            isVertical = isCurVertical;

            currentDegree = (float) -azimuth;
            StringBuffer sb = new StringBuffer();
            sb.append("方向角:").append(Math.round(azimuth))
                    .append("\n俯仰角：").append(Math.round(pitch))
                    .append("\n翻转角：").append(Math.round(roll));
            mInfo.setText(sb.toString());
        }
    }

    private String getDerection(double azimuth) {
        String direction = "";
        if (Math.abs(azimuth) <= 22.5) {
            direction = "北";
        } else if (azimuth >= 22.5 && azimuth <= 67.5) {
            direction = "东北";
        } else if (azimuth >= 67.5 && azimuth <= 112.5) {
            direction = "东";
        } else if (azimuth >= 112.5 && azimuth <= 157.5) {
            direction = "东南";
        } else if (Math.abs(azimuth) >= 157.5) {
            direction = "南";
        } else if (azimuth <= -112.5 && azimuth >= -157.5) {
            direction = "西南";
        } else if (azimuth <= -67.5 && azimuth >= -112.5) {
            direction = "西";
        } else if (azimuth <= -22.5 && azimuth >= -67.5) {
            direction = "西北";
        }
        return direction;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (accuracy <= SensorManager.
                SENSOR_STATUS_ACCURACY_LOW) {
            mDialog.show();
        } else if (accuracy >= SensorManager.
                SENSOR_STATUS_ACCURACY_MEDIUM) {
            mDialog.dismiss();
        }
    }
}
