package org.lynxz.basepaywrapper

/**
 * 当前支付包装库的错误码
 * */
object PayWrapperErrorCode {
    const val OK = 0

    /**
     * 未初始化
     * */
    const val NOT_INIT = 0xFFFF00

    /**
     * 不支持当前支付方式(未注册)
     * */
    const val NOT_SUPPORT_PAY_TYPE = 0xFFFF01
}