package org.lynxz.alipaywrapper

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.lynxz.alipaywrapper.AliPayTemplateActivity.Companion.closeActivity
import org.lynxz.alipaywrapper.AliPayTemplateActivity.Companion.startThenPay

/**
 * 由于支付宝发起支付时需要传入 activity 对象,因此单独创建一个
 * 目前设置其 launchMode 为 singleTask, 并且背景透明
 * 使用:
 * 1. 通过 [startThenPay] 来启动activity并发起支付
 * 2. 支付结束后, 通过 [closeActivity] 来关闭当前activity
 * */
class AliPayTemplateActivity : Activity() {
    companion object {
        private const val KEY_CLOSE_ACTIVITY = "key_close_activity"
        private const val KEY_ORDER_INFO = "key_order_info"
        private var pendingClose = false // 本页面是否有创建, 若有,则支付结束后需要关闭

        /**
         * 启动activity并发起支付
         * @param orderJsonByServer 商家后台返回的订单信息
         * */
        fun startThenPay(context: Context, orderJsonByServer: String) {
            startInner(context, orderJsonByServer, false)
        }

        /**
         * 支付结束,关闭当前activity
         * */
        fun closeActivity(context: Context) {
            startInner(context, null, true)
        }

        /**
         * 发送intent给本activity
         * @param closeActivity true-若当前activity已创建,则关闭,用于支付完成后关闭该页面
         * @param orderJsonByServer 商家后台返回的订单信息, 非空时会发起支付宝付款
         * */
        private fun startInner(
            context: Context,
            orderJsonByServer: String? = null,
            closeActivity: Boolean = false
        ) {
            // 若页面未创建,调用方又发起关闭页面请求,则直接发返回
            if (!pendingClose && closeActivity) {
                return
            }

            // 调用方发起创建页面功能,则支付结束后需要进行关闭
            if (!closeActivity) {
                pendingClose = true;
            }

            val intent = Intent(context, AliPayTemplateActivity::class.java)
            intent.putExtra(KEY_CLOSE_ACTIVITY, closeActivity)
            intent.putExtra(KEY_ORDER_INFO, orderJsonByServer)

            if (context is Application) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        processIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        processIntent(intent)
    }

    private fun processIntent(intent: Intent?) {
        intent?.getBooleanExtra(KEY_CLOSE_ACTIVITY, false)?.let {
            if (it) {
                finish()
                return
            }
        }

        val orderInfo = intent?.getStringExtra(KEY_ORDER_INFO)
        if (!orderInfo.isNullOrBlank()) {
            AliPayManager.payInner(orderInfo, this)
        }
    }
}