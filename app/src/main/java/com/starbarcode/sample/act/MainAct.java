package com.starbarcode.sample.act;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import com.starbarcode.sample.Const;
import com.starbarcode.sample.R;

public class MainAct extends AppCompatActivity implements View.OnClickListener {
    private CheckBox cb_autofocus,cb_disableContinuous,cb_zoom;
    private RadioGroup radioGroup;
    private Button btn_scan,btn_decode;
    private int checkedId;
    private int barCodeType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        btn_scan.setOnClickListener(this);
        btn_decode.setOnClickListener(this);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rb_all:
                        barCodeType = 0;
                        break;
                    case R.id.rb_one_d:
                        barCodeType = 1;
                        break;
                    case R.id.rb_two_d:
                        barCodeType = 2;
                        break;
                    case R.id.rb_qrcode:
                        barCodeType = 3;
                        break;
                    case R.id.rb_code128:
                        barCodeType = 4;
                        break;
                }
            }
        });
    }

    private void initViews() {
        btn_scan = findViewById(R.id.btn_scan);
        btn_decode = findViewById(R.id.btn_decode);
        cb_autofocus = findViewById(R.id.cb_autofocus);
        cb_disableContinuous = findViewById(R.id.cb_disableContinuous);
        cb_zoom = findViewById(R.id.cb_zoom);
        radioGroup = findViewById(R.id.rg_barcode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissions.length > 0 && isGrant(grantResults))
            if (requestCode == 0) startToScanAct();
            else startToQRCodeEncodeAct();
    }


    private boolean isGrant(int[] grantResults) {
        boolean isGrant = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                isGrant = false;
            }
        }
        return isGrant;
    }

    private boolean checkPermission(String[] permissions) {
        boolean isGrant = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                isGrant = false;
            }
        }

        return isGrant;
    }

    private void startToScanAct(){
        Intent intent = new Intent(this,BarCodeScanAct.class);
        intent.putExtra(Const.AUTO_FOCUS,cb_autofocus.isChecked());
        intent.putExtra(Const.DISABLECONTINUOUS,cb_disableContinuous.isChecked());
        intent.putExtra(Const.AUTO_ZOOM,cb_zoom.isChecked());
        intent.putExtra(Const.BARCODE_TYPE, barCodeType);
        startActivity(intent);
    }

    private void startToQRCodeEncodeAct() {
        startActivity(new Intent(MainAct.this, QRCodeCodecAct.class));
    }

    @Override
    public void onClick(View v) {
        if (v == btn_scan){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkPermission(new String[]{Manifest.permission.CAMERA})) {
                    startToScanAct();
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 0);
                }
            } else {
                startToScanAct();
            }
        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
                    startToQRCodeEncodeAct();
                } else {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            }
        }
    }
}
