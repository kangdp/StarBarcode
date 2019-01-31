package com.kdp.starbarcode.core;

/***
 * @author kdp
 * @date 2019/1/25 13:08
 * @description 条码类型
 */
public enum  BarCodeType {
    ALL,//所有条形码
    ONE_D_CODE,//一维条形码
    TWO_D_CODE,//二维条形码
    QR_CODE,//仅识别QR_CODE码
    CODE_128,//仅识别Code_128码
    CUSTOME//识别指定条码格式(可以是多个)

}
