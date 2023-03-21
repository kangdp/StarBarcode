package com.kdp.starbarcode.core;
import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.kdp.starbarcode.State;
import com.kdp.starbarcode.view.BarCodePreview;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.DetectorResult;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.detector.Detector;
import java.lang.ref.WeakReference;
import java.util.Collection;

/***
 * @author kdp
 * @date 2019/1/15 10:54
 * @description
 */
public class BarCodeProcessor {
    private static final String NAME = "DecodeBarCodeThread";
    private boolean running;
    private Handler decodeHandler;
    private HandlerThread decodeThread;
    private BarCodeReaderManager bfrm;
    private Handler barCodeSRH;
    private BarCodePreview barCodePreview;
    private BarCodeScanConfig barCodeScanConfig;

    public BarCodeProcessor(Handler barCodeSRH, BarCodePreview barCodePreview) {
        this.barCodeSRH = barCodeSRH;
        this.barCodePreview = barCodePreview;
    }

    public void startDecode() {
        barCodeScanConfig = barCodePreview.getBarCodeScanConfig();
        if (barCodeScanConfig == null)
            throw new IllegalArgumentException("You must initialize the BarCodeScanConfig");
        initBarCodeFormatReader(barCodeScanConfig);
        if (decodeThread == null) {
            decodeThread = new HandlerThread(NAME);
            decodeThread.start();
            running = true;
            DecodeCallback decodeCallback = new DecodeCallback(this);
            decodeHandler = new Handler(decodeThread.getLooper(), decodeCallback);
        }
    }

    private void initBarCodeFormatReader(BarCodeScanConfig barCodeScanConfig) {
        if (bfrm == null) {
            bfrm = new BarCodeReaderManager();
            BarCodeType barCodeType = barCodeScanConfig.getBarCodeType();
            switch (barCodeType){
                case ALL:
                    bfrm.addAllBarCodeFormat();
                    break;
                case ONE_D_CODE:
                    bfrm.addOneDBarCodeFormat();
                    break;
                case TWO_D_CODE:
                    bfrm.addTwoDBarCodeForamt();
                    break;
                case QR_CODE:
                    bfrm.addQRBarCode();
                    break;
                case CODE_128:
                    bfrm.addCode128BarCode();
                    break;
                case CUSTOME:
                    Collection<BarcodeFormat> barcodeFormats = barCodeScanConfig.getBarcodeFormats();
                    if (barcodeFormats == null || barcodeFormats.size() == 0)
                        throw new IllegalArgumentException("The custom BarcodeFormats cannot be null,need at least one barcode format");
                    bfrm.addBarCodeFormat(barCodeScanConfig.getBarcodeFormats());
                    break;
            }

        }
    }

    public void stopDecode() {
        if (decodeHandler == null) return;
        Message quit = Message.obtain(decodeHandler, State.QUIT.ordinal());
        quit.sendToTarget();
        try {
            decodeThread.join(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        decodeHandler.removeMessages(State.SUCCESS.ordinal());
        decodeHandler.removeMessages(State.FAILED.ordinal());
        decodeThread = null;
        decodeHandler = null;
    }

    public void pushFrame(byte[] data, int width, int height) {
        if (decodeHandler != null)
            decodeHandler.obtainMessage(State.DECODE.ordinal(), width, height, data).sendToTarget();
    }


    static class DecodeCallback implements Handler.Callback {

        private WeakReference<BarCodeProcessor> weakReference;

        DecodeCallback(BarCodeProcessor barCodeProcessor) {
            this.weakReference = new WeakReference<>(barCodeProcessor);
        }

        @Override
        public boolean handleMessage(Message msg) {

            BarCodeProcessor barCodeProcessor = weakReference.get();
            if (barCodeProcessor == null) return true;
            if (msg == null || !barCodeProcessor.running) return true;

            switch (State.values()[msg.what]){
                case DECODE:
                    barCodeProcessor.decodeFrame((byte[]) msg.obj, msg.arg1, msg.arg2);
                    break;
                case QUIT:
                    barCodeProcessor.running = false;
                    Looper looper = Looper.myLooper();
                    if (looper != null) {
                        looper.quit();
                    }
                    break;
            }
            return true;
        }
    }

    /**
     * 解析条码
     * @param data
     * @param width
     * @param height
     */
    private void decodeFrame(byte[] data, int width, int height) {
        PlanarYUVLuminanceSource source = buildLuminanceSource(data, width, height);
        Result result = null;
        State state = State.FAILED;
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
            result = bfrm.decodeWithState(bitmap);
            if (result == null){
                bitmap = new BinaryBitmap(new HybridBinarizer(source));
                result = bfrm.decodeWithState(bitmap);
            }
            if (result != null) {
                state = State.SUCCESS;
            } else {

                if (barCodeScanConfig.getBarCodeType() == BarCodeType.QR_CODE && barCodeScanConfig.isSupportAutoZoom()) {
                    DetectorResult detectorResult;
                    try {
                        detectorResult = new Detector(bitmap.getBlackMatrix()).detect(bfrm.getHintTypeMap());
                        ResultPoint[] resultPoint = detectorResult.getPoints();
                            int distance = calculateDistance(resultPoint);
                            if (distance < (barCodeScanConfig.getROI().width()) / 5) {
                                //若二维码图像宽度小于扫描库宽度的1/5，则需要放大镜头
                                state = State.NONE;
                            }
                    } catch (NotFoundException e) {
                        e.printStackTrace();
                    } catch (FormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        dispatchResult(result, state);
    }

    /**
     * 两个定位点间距
     * @param resultPoint
     * @return
     */
    private int calculateDistance(ResultPoint[] resultPoint) {
        int point1X = (int) resultPoint[0].getX();
        int point1Y = (int) resultPoint[0].getY();
        int point2X = (int) resultPoint[1].getX();
        int point2Y = (int) resultPoint[1].getY();
        return (int) Math.sqrt(Math.pow(point1X - point2X, 2) + Math.pow(point1Y - point2Y, 2));
    }

    private void dispatchResult(Result result,State state) {
        if (result != null) {
            Message message = Message.obtain(barCodeSRH, state.ordinal(), result);
            message.sendToTarget();
        } else {
            Message message = Message.obtain(barCodeSRH, state.ordinal());
            message.sendToTarget();
        }
    }

    private PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        if (barCodeScanConfig == null) return null;
        Rect borders = barCodeScanConfig.getROI();
        if (borders == null) return null;
        return new PlanarYUVLuminanceSource(data, width, height, borders.left, borders.top,
                borders.width(), borders.height(), false);
    }

}
