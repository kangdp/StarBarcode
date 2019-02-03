package com.starbarcode.sample.act;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import com.kdp.starbarcode.core.BarCodeType;
import com.kdp.starbarcode.core.BarCodeScanConfig;
import com.kdp.starbarcode.inter.OnBarCodeScanResultListener;
import com.kdp.starbarcode.view.BarCodePreview;

import com.starbarcode.sample.Const;
import com.starbarcode.sample.R;
import com.starbarcode.sample.view.ScanView;


/***
 * @author kdp
 * @date 2019/1/25 15:14
 * @description
 */
public class BarCodeScanAct extends AppCompatActivity implements View.OnClickListener {
    private BarCodePreview barCodePreview;
    private ScanView scanView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_qr);
        scanView = findViewById(R.id.scanview);
        barCodePreview = findViewById(R.id.barcodepreview);
        Button btn_start_recognize = findViewById(R.id.btn_start_recognize);
        Button btn_stop_recognize = findViewById(R.id.btn_stop_recognize);
        Button btn_turn_on_flash = findViewById(R.id.btn_turn_on_flash);
        Button btn_turn_off_flash = findViewById(R.id.btn_turn_off_flash);
        barCodePreview.setOnBarCodeScanResultListener(new OnBarCodeScanResultListener() {

            @Override
            public void onSuccess(String result) {
                vibrate();
                showSuccessDialog(result);
            }

            @Override
            public void onFailure() {
            }
        });
        btn_start_recognize.setOnClickListener(this);
        btn_stop_recognize.setOnClickListener(this);
        btn_turn_on_flash.setOnClickListener(this);
        btn_turn_off_flash.setOnClickListener(this);

        //扫描配置
        setScanConfig();
    }

    private void showSuccessDialog(String rawResult) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("识别结果")
                .setMessage(rawResult)
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        barCodePreview.startRecognize();
                    }
                });

        builder.create().show();
    }


    private void setScanConfig() {
        WindowManager windowManager = getWindowManager();
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        int scanWidth = screenWidth / 6 * 4;
        int scanHeight = screenHeight / 3;
        int left = (screenWidth - scanWidth) / 2;
        int top = (screenHeight - scanHeight) / 2;
        int right = scanWidth + left;
        int bottom = scanHeight + top;

        scanView.setBorder(new int[]{left,top,right,bottom});

        Intent intent = getIntent();
        boolean autofocus = intent.getBooleanExtra(Const.AUTO_FOCUS,true);
        boolean disableContinuous = intent.getBooleanExtra(Const.DISABLECONTINUOUS,true);
        boolean autoZoom = intent.getBooleanExtra(Const.AUTO_ZOOM,false);
        int barcodeType = intent.getIntExtra(Const.BARCODE_TYPE,0);
        //识别区域
        Rect rect = new Rect(left,top,right,bottom);
        BarCodeScanConfig barCodeScanConfig = new BarCodeScanConfig.Builder()
                .setROI(rect)//识别区域
                .setAutofocus(autofocus)//自动对焦，默认为true
                .setDisableContinuous(disableContinuous)//使用连续对焦，必须在Autofocus为true的前提下，该参数才有效;默认为true
                .setBarCodeType(BarCodeType.values()[barcodeType])//识别所有的条形码
//                .setBarCodeType(BarCodeType.ONE_D_CODE)//仅识别所有的一维条形码
//                .setBarCodeType(BarCodeType.TWO_D_CODE)//仅识别所有的二维条形码
//                .setBarCodeType(BarCodeType.QR_CODE)//仅识别二维码
//                .setBarCodeType(BarCodeType.CODE_128)//仅识别CODE 128码
//                .setBarCodeType(BarCodeType.CUSTOME)//自定义条码类型，必须指定自定义识别的条形码格式
//                .setBarcodeFormats(EnumSet.of(BarcodeFormat.QR_CODE,BarcodeFormat.CODE_128))//定义识别的条形码格式
                .setSupportAutoZoom(autoZoom)//当二维码图片较小时自动放大镜头(仅支持QR_CODE)
                .build();
        barCodePreview.setBarCodeScanConfig(barCodeScanConfig);
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    protected void onStart() {
        super.onStart();
        barCodePreview.openCamera();
        barCodePreview.startRecognize();
        //开启扫描动画
        scanView.startScan();
    }

    @Override
    protected void onStop() {
        super.onStop();
        barCodePreview.stopRecognize();
        barCodePreview.closeCamera();
        scanView.stopScan();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_start_recognize://开始识别
                barCodePreview.startRecognize();
                break;
            case R.id.btn_stop_recognize://停止识别
                barCodePreview.stopRecognize();
                break;
            case R.id.btn_turn_on_flash://打开闪光灯
                barCodePreview.turnOnFlashLight();
            break;
            case R.id.btn_turn_off_flash://关闭闪光灯
                barCodePreview.turnOffFlashLight();
                break;
        }
    }

}
