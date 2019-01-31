package com.kdp.starbarcode.codec;

import android.graphics.Bitmap;


/***
 * @author kdp
 * @date 2019/1/30 11:06
 * @description
 */
public interface ICodec {
    String decodeBarcode(Bitmap bitmap);
    Bitmap encodeBarcode(String content,int widthPixel,int heightPixel);
}
