package org.lynxz.basepaywrapper.observer

import org.lynxz.basepaywrapper.bean.PayType

/**
 * 之后完成后回调通知调用方
 * */
interface IOnPayResult {
    /**
     * 支付完成后回调通知调用方
     * @param payType 支付类型,目前支持 支付宝, 微信
     * @param success 是否支持成功, 根据 statusCode 判定的
     * @param statusCode 支付结果码
     *                   支付宝: 对应 payResult.getResultStatus(), "9000" 表示成功
     *                   微信: 对应 baseResp.errCode, 0-成功 1-错误(签名错误、未注册APPID等) 2-用户取消
     * @param errMsg 错误信息
     *                  支付宝: 对应 payResult.getResult()
     *                  微信: 对应 baseResp.errStr
     * @param oriPayResultObj 原始支付sdk返回的结果对象
     *                          支付宝: 对应原始 Map<String?, String?> 对象
     *                          微信: 对应 baseResp 对象
     *
     * */
    fun onPayFinish(
        payType: PayType,
        success: Boolean,
        statusCode: String?,
        errMsg: String?,
        oriPayResultObj: Any?
    )
}