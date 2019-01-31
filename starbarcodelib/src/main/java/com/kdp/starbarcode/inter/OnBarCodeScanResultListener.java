package com.kdp.starbarcode.inter;


/***
 * @author kdp
 * @date 2019/1/17 14:32
 * @description
 */
public interface OnBarCodeScanResultListener {
    void onSuccess(String result);
    void onFailure();
}
