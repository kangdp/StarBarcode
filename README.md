## StarBarcode
一个基于Zxing封装的条形码扫描库，支持多种条形码，可生成、解析带logo的二维码，自动放大镜头，设备移动后进行对焦、连续对焦，扫描UI自定义。

## Demo下载
[app-release.apk](https://github.com/kangdongpu/StarBarcode/raw/master/app-release.apk)
## 截图
<img src="https://github.com/kangdongpu/StarBarcode/blob/master/screenshot/MainAct.png" width="300"/>   <img src="https://github.com/kangdongpu/StarBarcode/blob/master/screenshot/QRCodeCodecAct.png" width="300"/>
<img src="https://github.com/kangdongpu/StarBarcode/blob/master/screenshot/BarcodeScanAct.png" width="300"/>   <img src="https://github.com/kangdongpu/StarBarcode/blob/master/screenshot/ScanResult.png" width="300"/>

## 功能
- 支持多种条形码格式
- 支持移动对焦、连续对焦
- 支持当二维码较小时自动放大镜头(仅限二维码)
- 支持闪光灯
- 支持创建二维码，识别Bitmap二维码图片
- 支持定制自己的条形码编解码器
- 支持扫描UI自定义

## 添加依赖

## 简单使用

- 在布局xml中加入`BarCodePreview`
 
      <com.kdp.starbarcode.view.BarCodePreview
         android:id="@+id/barcodepreview"
         android:layout_width="match_parent"
         android:layout_height="match_parent"/>
- 在Java中这样使用

    配置扫描参数
       
       Rect rect = new Rect(left,top,right,bottom);
       BarCodeScanConfig barCodeScanConfig = new BarCodeScanConfig.Builder()
                .setROI(rect)//识别区域
                .setBarCodeType(BarCodeType.ALL)//可识别所有的条形码
                .build();
       barCodePreview.setBarCodeScanConfig(barCodeScanConfig);
       
       //结果回调
       barCodePreview.setOnBarCodeScanResultListener(new OnBarCodeScanResultListener() {
            @Override
            public void onSuccess(String result) {
                //识别成功后再次识别
                barCodePreview.startRecognize();
               ...
            }
            @Override
            public void onFailure() {
               //失败
               ...
            }
        });
        
     打开摄像头，开始识别
        
        @Override
        protected void onStart() {
            super.onStart();
            barCodePreview.openCamera();
            barCodePreview.startRecognize();
          }
     关闭摄像头，停止识别
         
         
         @Override
         protected void onStop() {
             super.onStop();
             barCodePreview.stopRecognize();
             barCodePreview.closeCamera();
          }
## API

- BarCodeScanConfig 
         
         BarCodeScanConfig barCodeScanConfig = new BarCodeScanConfig.Builder()
                .setROI(rect) //识别区域
                .setAutofocus(true) //自动对焦，默认为true
                .setDisableContinuous(false) //是否禁用连续对焦，必须在Autofocus为true的前提下，该参数才有效;默认为true
                .setBarCodeType(BarCodeType.ALL) //识别所有的条形码
                .setBarCodeType(BarCodeType.ONE_D_CODE) //仅识别所有的一维条形码
                .setBarCodeType(BarCodeType.TWO_D_CODE) //仅识别所有的二维条形码
                .setBarCodeType(BarCodeType.QR_CODE) //仅识别二维码，可提升识别速度
                .setBarCodeType(BarCodeType.CODE_128) //仅识别CODE 128码，可提升识别速度
                .setBarCodeType(BarCodeType.CUSTOME) //自定义条码类型，必须指定识别的条形码格式
                .setBarcodeFormats(EnumSet.of(BarcodeFormat.QR_CODE,BarcodeFormat.CODE_128)) //自定义识别的条形码格式
                .setSupportAutoZoom(true) //当二维码图片较小时自动放大镜头(仅支持QR_CODE)
                .build();
    > **注意**
    
    > - 该库内部并没有提供扫描框的实现，需要开发者自己去定制，而识别区域需要通过`setROI(...)`参数去设置，并且该参数不能为空。**
    > - 当多次设置`BarCodeType`时，只有最后一次才会生效;而当`BarCodeType`设置为`CUSTOME`时，必须要使用`setBarcodeFormats(...)`来指定要识别的条形码格式;若没有配置此参数，默认会识别所有的条形码。
- BarCodePreview

         barCodePreview.openCamera() //打开摄像头开始预览
               ...      closeCamera() 关闭摄像头结束预览
               ...      startRecognize() //开始识别
               ...      stopRecognize() //停止识别
               ...      turnOnFlashLight() //打开闪光灯,注意：在此之前必须先打开摄像头
               ...      turnOffFlashLight() //关闭闪光灯
         
- 创建/解析 二维码图片

             //实例化QRCode编解码器
              QRCodeCodec  qrCodeCodec = new QRCodeCodec();
              //解析二维码图片,返回String
              String content = qrCodeCodec.decodeQRCode(Bitmap bitmap); //参数：要解析二维码图片
              //创建二维码图片,返回Bitmap
              Bitmap bitmap = qrCodeCodec.encodeQRCode(String content,int pixelSize); //参数：要创建的二维码内容和尺寸
              //创建带logo的二维码图片,返回Bitmap
              Bitmap bitmap = qrCodeCodec.encodeQRCode(String content,int pixelSize,Bitmap logo); //参数：要创建的二维码的内容、尺寸和logo
- 自定义条形码编解码器，定制自己的条形码(可参考库中已实现的[QRCodeCodec](https://github.com/kangdongpu/StarBarcode-master/blob/master/starbarcodelib/src/main/java/com/kdp/starbarcode/codec/QRCodeCodec.java)类)

## 感谢
- [Zxing](https://github.com/zxing/zxing)
- [https://github.com/WellerV/SweetCamera](https://github.com/WellerV/SweetCamera)

LICENSE
=======
    
    Copyright 2019 kangdongpu

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.





 
