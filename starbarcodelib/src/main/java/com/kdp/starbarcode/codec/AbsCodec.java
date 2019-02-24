package com.kdp.starbarcode.codec;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/***
 * @author kdp
 * @date 2019/1/30 11:10
 * @description 继承此类来实现自己的编解码器
 */
public abstract class AbsCodec implements ICodec {

    @Override
    public String decodeBarcode(Bitmap bitmap) {

        if (bitmap == null)
            throw new NullPointerException("The bitmap cannot be empty");

        Result result;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];

        Map<DecodeHintType, Object> hintTypeMap = new EnumMap<>(DecodeHintType.class);

        hintTypeMap.put(DecodeHintType.CHARACTER_SET, "utf-8");
        hintTypeMap.put(DecodeHintType.POSSIBLE_FORMATS, getDecodeFormats());
        hintTypeMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        //先使用RGB解析，如果不行，再使用YUV
        result = decodeFromRGB(pixels, width, height, hintTypeMap);
        if (result == null)
            result = decodeFromYUV(pixels, width, height, hintTypeMap);
        if (result == null) return null;
        return result.getText();
    }


    @Override
    public Bitmap encodeBarcode(String content, int widthPixel, int heightPixel) {

        if (TextUtils.isEmpty(content))
            throw new NullPointerException("The content of the QR code image cannot be empty");
        Map<EncodeHintType, Object> hintTypeMap = new EnumMap<>(EncodeHintType.class);
        hintTypeMap.put(EncodeHintType.CHARACTER_SET, "utf-8");
        //空白边距的宽度
        hintTypeMap.put(EncodeHintType.MARGIN, 0);
        //容错级别
        hintTypeMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        try {
            //矩阵转换
            BitMatrix matrix = new MultiFormatWriter().encode(content, getEncodeFormat(), widthPixel, heightPixel, hintTypeMap);
            int[] pixels = new int[widthPixel * heightPixel];
            //使用二维码算法，逐个生成二维码的图片
            for (int y = 0; y < heightPixel; y++) {
                for (int x = 0; x < widthPixel; x++) {
                    if (matrix.get(x, y)) {
                        pixels[y * widthPixel + x] = 0xff000000; //前景色
                    } else {
                        pixels[y * widthPixel + x] = 0xffffffff; //背景色
                    }
                }
            }
            final Bitmap bitmap = Bitmap.createBitmap(widthPixel, heightPixel, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, widthPixel, 0, 0, widthPixel, heightPixel);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 使用RGB解析bitmap
     *
     * @param data
     * @param width
     * @param height
     * @param hintTypeMap
     * @return
     */
    private Result decodeFromRGB(int[] data, int width, int height, Map<DecodeHintType, ?> hintTypeMap) {
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, data);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            return new MultiFormatReader().decode(binaryBitmap, hintTypeMap);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 使用YUV解析bitmap
     *
     * @param data
     * @param width
     * @param height
     * @param hintTypeMap
     * @return
     */
    private Result decodeFromYUV(int[] data, int width, int height, Map<DecodeHintType, ?> hintTypeMap) {
        Result result = null;
        byte[] yuv = convertRGBToYuv(data, new byte[width * height], width, height);
        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(yuv, width, height, 0, 0, width, height, false);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            result = new MultiFormatReader().decode(binaryBitmap, hintTypeMap);
        } catch (NotFoundException e) {
            //使用GlobalHistogramBinarizer算法进行解析
            try {
                result = new MultiFormatReader().decode(new BinaryBitmap(new GlobalHistogramBinarizer(source)), hintTypeMap);
            } catch (NotFoundException e1) {
                e1.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 将rgb转为yuv
     *
     * @param rgb
     * @param yuv420sp
     * @param width
     * @param height
     * @return
     */
    private byte[] convertRGBToYuv(int[] rgb, byte[] yuv420sp, int width, int height) {
        for (int i = 0; i < width * height; i++) {
            float red = (rgb[i] >> 16) & 0xff;
            float green = (rgb[i] >> 8) & 0xff;
            float blue = (rgb[i]) & 0xff;
            int luminance = (int) ((0.257f * red) + (0.504f * green) + (0.098f * blue) + 16);
            yuv420sp[i] = (byte) (0xff & luminance);
        }
        return yuv420sp;
    }

    protected abstract BarcodeFormat getEncodeFormat();

    protected abstract Collection<BarcodeFormat> getDecodeFormats();
}
