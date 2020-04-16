package org.lynxz.alipaywrapper

import android.app.Activity
import android.app.Application
import android.os.Message
import com.alipay.sdk.app.PayTask
import org.lynxz.alipaywrapper.bean.PayResult
import org.lynxz.basepaywrapper.IPayManager
import org.lynxz.basepaywrapper.bean.PayType
import org.lynxz.basepaywrapper.bizlooper.BizLooper
import org.lynxz.basepaywrapper.bizlooper.IBizHandler
import org.lynxz.basepaywrapper.observer.IOnPayResult
import org.lynxz.basepaywrapper.util.LoggerUtil

object AliPayManager : IPayManager {
    private const val TAG = "AliPayManager"

    private var appId: String? = null
    private var appSecret: String? = null
    private var isInitComplete = false
    private var application: Application? = null

    private const val SDK_PAY_FLAG = 0xFF00 // 支付结果
    private var pendingOnPayResult: IOnPayResult? = null // 支付结果回调监听

    @Suppress("UNCHECKED_CAST")
    private val bizHandler by lazy {
        IBizHandler {
            if (it.what == SDK_PAY_FLAG) {
                AliPayTemplateActivity.closeActivity(application)

                /*
                 * 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                 */
                val payResult = PayResult(it.obj as Map<String?, String?>)
                val sresultInfo = payResult.result // 同步返回需要验证的信息
                val resultStatus = payResult.resultStatus

                // 判断resultStatus 为9000则代表支付成功
                pendingOnPayResult?.onPayFinish(
                    PayType.AliPay,
                    resultStatus == "9000",
                    resultStatus,
                    payResult.result,
                    it.obj
                )
            }
            true
        }
    }
    private var bizLooper: BizLooper? = null

    override fun init(application: Application, appId: String?, appSecret: String?) {
        if (isInitialized()) {
            LoggerUtil.w(TAG, "AliPayManager 已初始化过,不必重新初始化")
            return
        }

        AliPayManager.application = application
        AliPayManager.appId = appId
        AliPayManager.appSecret = appSecret

        bizLooper = BizLooper(bizHandler).apply {
            setThreadNamePrefix("alipayBizLooper_")
        }
        bizLooper?.start(888888)
        isInitComplete = true
    }

    override fun uninit() {
        if (isInitialized()) {
            bizLooper?.stop()
        }
        pendingOnPayResult = null
        application = null
        isInitComplete = false
    }

    override fun isInitialized() = isInitComplete

    /**
     * 发起支付
     * */
    override fun pay(orderJsonByServer: String, onPayResult: IOnPayResult?): Boolean {
        pay(orderJsonByServer, null, onPayResult)
        return true
    }

    /**
     * 发起支付, 可使用外部的activity
     * */
    fun pay(orderInfo: String, activity: Activity? = null, onPayResult: IOnPayResult? = null) {
        pendingOnPayResult = onPayResult

        if (activity != null) {
            payInner(orderInfo, activity)
        } else {
            AliPayTemplateActivity.startThenPay(application, orderInfo)
        }
    }

    internal fun payInner(orderInfo: String, activity: Activity) {
        // 必须在线程中运行,否则ANR
        bizLooper?.sendBizMessage(Runnable {
            val result = PayTask(activity).payV2(orderInfo, true);
            LoggerUtil.w(TAG, "aliPay.payV2 result=$result");

            bizLooper?.sendBizMessage(Message.obtain().apply {
                what = SDK_PAY_FLAG
                obj = result
            })
        })
    }
}