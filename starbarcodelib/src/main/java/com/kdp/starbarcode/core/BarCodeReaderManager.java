package com.kdp.starbarcode.core;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

/***
 * @author kdp
 * @date 2019/1/15 11:52
 */
class BarCodeReaderManager {
    private final MultiFormatReader multiFormatReader;
    private Map<DecodeHintType, Object> hintTypeMap;
    private Collection<BarcodeFormat> decodeFormats;

    BarCodeReaderManager() {
        multiFormatReader = new MultiFormatReader();
        hintTypeMap = new EnumMap<>(DecodeHintType.class);
        decodeFormats = EnumSet.noneOf(BarcodeFormat.class);
    }

    Map<DecodeHintType, Object> getHintTypeMap() {
        return hintTypeMap;
    }

    /**
     * 解码条形码
     *
     * @param bitmap
     */
    Result decodeWithState(BinaryBitmap bitmap) {
        try {
            return multiFormatReader.decodeWithState(bitmap);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } finally {
            multiFormatReader.reset();
        }
        return null;
    }


    /**
     * 添加所有条码格式
     */
    void addAllBarCodeFormat() {
        decodeFormats.addAll(BarCodeFormatManager.ALL_FORMATS);
        hintTypeMap.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        hintTypeMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hintTypeMap.put(DecodeHintType.CHARACTER_SET, "utf-8");
        multiFormatReader.setHints(hintTypeMap);
    }

    /**
     * 添加所有的一维条形码格式
     */
    void addOneDBarCodeFormat() {
        decodeFormats.addAll(BarCodeFormatManager.ONE_D_FORMATS);
        hintTypeMap.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        hintTypeMap.put(DecodeHintType.CHARACTER_SET, "utf-8");
        multiFormatReader.setHints(hintTypeMap);
    }

    /**
     * 添加所有的二维条形码格式
     */
    void addTwoDBarCodeForamt() {
        decodeFormats.addAll(BarCodeFormatManager.TWO_D_FORMATS);
        hintTypeMap.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        hintTypeMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hintTypeMap.put(DecodeHintType.CHARACTER_SET, "utf-8");
        multiFormatReader.setHints(hintTypeMap);
    }

    /**
     * 添加二维码QR_CODE格式
     */
    void addQRBarCode() {
        decodeFormats.addAll(BarCodeFormatManager.QR_CODE_FORMATS);
        hintTypeMap.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        hintTypeMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hintTypeMap.put(DecodeHintType.CHARACTER_SET, "utf-8");
        multiFormatReader.setHints(hintTypeMap);
    }

    /**
     * 添加Code 128格式
     */
    void addCode128BarCode() {
        decodeFormats.addAll(BarCodeFormatManager.CODE_128_FORMATS);
        hintTypeMap.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        hintTypeMap.put(DecodeHintType.CHARACTER_SET, "utf-8");
        multiFormatReader.setHints(hintTypeMap);
    }

    /**
     * 添加指定条码格式
     *
     * @param formats
     */
    void addBarCodeFormat(Collection<BarcodeFormat> formats) {
        decodeFormats.addAll(formats);
        hintTypeMap.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        if (formats.contains(BarcodeFormat.QR_CODE))
            hintTypeMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hintTypeMap.put(DecodeHintType.CHARACTER_SET, "utf-8");
        multiFormatReader.setHints(hintTypeMap);
    }

}
