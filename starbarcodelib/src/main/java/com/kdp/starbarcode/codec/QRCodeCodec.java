package com.kdp.starbarcode.codec;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import com.google.zxing.BarcodeFormat;
import java.util.Collection;
import java.util.EnumSet;


/***
 * @author kdp
 * @date 2019/1/30 11:33
 * @description 二维码编解码器
 */
public class QRCodeCodec extends AbsCodec {

    /**
     * 编码的条形码格式
     * @return 二维码格式
     */
    @Override
    protected BarcodeFormat getEncodeFormat() {
        return BarcodeFormat.QR_CODE;
    }

    /**
     * 解码的条形码格式
     * @return 二维码格式
     */
    @Override
    protected Collection<BarcodeFormat> getDecodeFormats() {
        return EnumSet.of(BarcodeFormat.QR_CODE);
    }

    /**
     * 解析二维码图片，该方法为耗时操作
     * @param bitmap 图片
     * @return 返回二维码图片中的内容
     */
    public String decodeQRCode(Bitmap bitmap) {
        return super.decodeBarcode(bitmap);
    }

    /**
     * 创建二维码图片，该方法为耗时操作
     * @param content 二维码图片中的内容
     * @param pixelSize 图片的宽高
     * @return 二维码图片
     */
    public Bitmap encodeQRCode(String content, int pixelSize) {
        return encodeQRCode(content,pixelSize,null);
    }
    /**
     *  创建二维码图片，该方法为耗时操作
     * @param content 二维码图片中的内容
     * @param pixelSize 图片的宽高
     * @return 带logo的二维码图片
     */
    public Bitmap encodeQRCode(String content, int pixelSize,Bitmap logo) {
        Bitmap bitmap = super.encodeBarcode(content,pixelSize,pixelSize);
        return addLogo(bitmap,logo);
    }


    /**
     * 添加logo到二维码图片上
     * @param src 二维码图片
     * @param logo logo
     * @return
     */
    private static Bitmap addLogo(Bitmap src, Bitmap logo) {
        if (src == null || logo == null) {
            return src;
        }
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }

        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }

        float scaleFactor = srcWidth  * 1.0f / 5 /logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth >> 1, srcHeight >> 1);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) >> 1, (srcHeight - logoHeight) >> 1, null);
            canvas.save();
            canvas.restore();
        } catch (Exception e) {
            e.printStackTrace();
            bitmap = null;
        }
        return bitmap;
    }

}
