package com.kdp.starbarcode.core;
import android.graphics.Rect;

import com.google.zxing.BarcodeFormat;

import java.util.Collection;

/***
 * @author kdp
 * @date 2019/1/10 18:05
 */
public class BarCodeScanConfig {

    //识别区域
    private Rect borders;
    //是否自动对焦
    private boolean autofocus;
    //是否禁止连续对焦.默认禁止
    private boolean disableContinuous;
    //摄像头焦距
    private int zoom;
    //自动调节焦距(仅支持QR_CODE)
    private boolean isSupportAutoZoom;
    //识别的条码类型
    private BarCodeType barCodeType;
    //指定识别的条码格式
    private Collection<BarcodeFormat> barcodeFormats;
    private BarCodeScanConfig(Builder builder) {
        this.borders = builder.borders;
        this.autofocus = builder.autofocus;
        this.disableContinuous = builder.disableContinuous;
        this.isSupportAutoZoom = builder.isSupportAutoZoom;
        this.zoom = builder.zoom;
        this.barCodeType = builder.barCodeType;
        this.barcodeFormats = builder.barcodeFormats;
    }

    public Rect getROI() {
        return borders;
    }

    public boolean isAutofocus() {
        return autofocus;
    }

    public boolean isDisableContinuous() {
        return disableContinuous;
    }

    boolean isSupportAutoZoom() {
        return isSupportAutoZoom;
    }

    public int getZoom() {
        return zoom;
    }

    BarCodeType getBarCodeType() {
        return barCodeType != null ? barCodeType : BarCodeType.ALL;
    }

    Collection<BarcodeFormat> getBarcodeFormats() {
        return barcodeFormats;
    }


    public static class Builder{
       private Rect borders;
        private boolean autofocus = true;
        private boolean disableContinuous = true;
        private boolean isSupportAutoZoom;
        private int zoom;

        private BarCodeType barCodeType;

        private Collection<BarcodeFormat> barcodeFormats;

        public Builder setAutofocus(boolean autofocus) {
            this.autofocus = autofocus;
            return this;
        }

        public Builder setDisableContinuous(boolean disableContinuous) {
            this.disableContinuous = disableContinuous;
            return this;
        }

        public Builder setSupportAutoZoom(boolean supportAutoZoom) {
            isSupportAutoZoom = supportAutoZoom;
            return this;
        }

        public Builder setZoom(int zoom){
            this.zoom = zoom;
            return this;
        }

        public Builder setROI(Rect borders){
           this.borders = borders;
           return this;
       }

        public Builder setBarCodeType(BarCodeType barCodeType) {
            this.barCodeType = barCodeType;
            return this;
        }

        public Builder setBarcodeFormats(Collection<BarcodeFormat> barcodeFormats) {
            this.barcodeFormats = barcodeFormats;
            return this;
        }


       public BarCodeScanConfig build(){
           return new BarCodeScanConfig(this);
       }
    }
}
