package org.lynxz.basepaywrapper

import org.lynxz.basepaywrapper.bean.PayType
import org.lynxz.basepaywrapper.observer.IOnPayResult
import org.lynxz.basepaywrapper.util.LoggerUtil

/**
 * 支付方式管理工具
 * 使用:
 * 1. 通过 [registerPayType] 来注册支付方式, 请确保其已初始化过
 * 2. 通过 [pay] 来发起支付
 * */
object PayManager {

    private const val TAG = "PayManager"
    private val payMap = mutableMapOf<PayType, IPayManager>()

    /**
     * 注册某种支付方式
     * 若多次注册,则最后一次生效
     * */
    fun registerPayType(payType: PayType, subPayManager: IPayManager): PayManager {
        if (!subPayManager.isInitialized()) {
            LoggerUtil.w(TAG, "registerPayType ${payType.name} 未初始化:$subPayManager")
        }
        payMap[payType] = subPayManager
        return this
    }

    /**
     * 取消注册某支付方式
     * @param payType 要移除的支付方式,若为null,则表示移除所有支付方式
     * */
    fun unRegisterPayType(payType: PayType?) {
        val iterator = payMap.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            val shouldRemove = payType == null || next.key == payType
            if (shouldRemove) {
                next.value.uninit()
                iterator.remove()
            }
        }
//        payMap.remove(payType)
    }

    /**
     * 通过指定的支付方式,发起支付
     * @param orderInfo 商家后台返回的符合特定支付方式所需的订单数据
     * @param onPayResult 支付完成后回调通知用户(注意: 最终由调用方自行向商家后台发起查询确认结果)
     * @return true-发起请求支付成功
     * */
    fun pay(payType: PayType, orderInfo: String, onPayResult: IOnPayResult? = null): Boolean {
        val iPayManager = payMap[payType]
        if (iPayManager == null) {
            LoggerUtil.w(TAG, "pay fail: ${payType.name}  payManager not exist")
            onPayResult?.onPayFinish(
                payType,
                false,
                "${PayWrapperErrorCode.NOT_SUPPORT_PAY_TYPE}",
                "未注册本支付方式,请先调用 PayManager.registerPayType(payType,payManager)",
                null
            )
            return false
        }

        return iPayManager.pay(orderInfo, onPayResult)
    }

//    /**
//     * 发起支付
//     * @param payType 支付方式
//     * @param payReq 发起支付时的请求参数对象
//     *                  微信: 传入 payReq 对象类型才生效
//     *                  支付宝: 传入 Map<String, String> 参数
//     * @param onPayResult 支付完成后回调通知用户
//     * */
//    fun pay(payType: PayType, payReq: Any?, onPayResult: IOnPayResult? = null): Boolean {
//        val iPayManager = payMap[payType]
//        if (iPayManager == null) {
//            LoggerUtil.w(TAG, "pay fail: ${payType.name}  payManager not exist")
//            return false
//        }
//
//        return iPayManager.pay(payReq, onPayResult)
//    }
}