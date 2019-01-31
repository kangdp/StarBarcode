package com.starbarcode.sample.act;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.kdp.starbarcode.codec.QRCodeCodec;
import com.starbarcode.sample.R;

/***
 * @author kdp
 * @date 2019/1/28 14:48
 * @description
 */
public class QRCodeCodecAct extends AppCompatActivity implements View.OnClickListener {
    private EditText etn_input_content;
    private TextView tv_result;
    private Button btn_encode,btn_decode;
    private ImageView iv_qrcode;
    private Bitmap qrBitmap;
    private QRCodeCodec qrCodeCodec;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_encode);
        etn_input_content = findViewById(R.id.etn_input_content);
        btn_encode = findViewById(R.id.btn_encode);
        btn_decode = findViewById(R.id.btn_decode);
        iv_qrcode = findViewById(R.id.iv_qrcode);
        tv_result =findViewById(R.id.tv_result);
        btn_encode.setOnClickListener(this);
        btn_decode.setOnClickListener(this);
        //实例化QRCode编解码器
        qrCodeCodec = new QRCodeCodec();
    }

    @Override
    public void onClick(View v) {
        if (v == btn_encode){
            doEncode();
        }else {
            doDecode();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void doDecode() {
        if (qrBitmap  == null) return;
        new AsyncTask<Bitmap,Integer,String>(){
            @Override
            protected String doInBackground(Bitmap... bitmaps) {
                return qrCodeCodec.decodeBarcode(bitmaps[0]);
            }
            @Override
            protected void onPostExecute(String content) {
                tv_result.setText(content);
            }
        }.execute(qrBitmap);
    }

    @SuppressLint("StaticFieldLeak")
    private void doEncode() {
        final Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.logo);
        new AsyncTask<Bitmap,Integer,Bitmap>(){
            @Override
            protected Bitmap doInBackground(Bitmap... bitmaps) {
                String content =etn_input_content.getText().toString();
                if (TextUtils.isEmpty(content)) return null;
                return qrCodeCodec.encodeQRCode(etn_input_content.getText().toString(),dip2px(QRCodeCodecAct.this,150),logoBitmap);
            }
            @Override
            protected void onPostExecute(Bitmap bitmap) {
                qrBitmap = bitmap;
                iv_qrcode.setImageBitmap(bitmap);
            }
        }.execute(logoBitmap);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
