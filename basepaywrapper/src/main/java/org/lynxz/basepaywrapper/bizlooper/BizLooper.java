package org.lynxz.basepaywrapper.bizlooper;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 创建一个 主线程/子线程 looper, 并持续运行
 * 使用方法:
 * 1. 初始化: {@link #BizLooper(IBizHandler)} 创建子线程looper
 * 3. 启用: {@link #start(int)}
 * 3. 发送消息跟本bizLooper执行: {@link #sendBizMessage(Message)} {@link #sendBizMessage(int, Object)} {@link #sendBizMessage(Runnable)}
 * 4. 停止子线程looper并释放资源: mBizLooper.stop();
 * <p>
 * 使用demo:
 * <pre>
 *      BizLooper bizLooper = new BizLooper(null);
 *      bizLooper.setThreadNamePrefix("customTreadName_"); // 可选, 子线程时有效, 线程名前缀, 拼接id后生成最终线程名
 *      bizLooper.start(1000); // 启动 bizlooper,并设置id, id主要用于调用方区分
 *      bizLooper.clearAllMessages(); // 主动清空已有消息
 *      bizLooper.stop(); // 停止,若为子线程, 则退出子线程,释放资源
 *
 *      String threadName = bizLooper.getBizThreadName(); // 获取线程名
 *      boolean active = bizLooper.isActive(); // 在 start()~stop() 之间为true
 *
 *      // 发送消息给本bizLooper执行, isActive() 为true时才会成功
 *      // 若为runnable类型,则不会触发通知 IBizHandler, 系统会自动执行
 *      bizLooper.sendBizMessage(Message.obtain());
 *      bizLooper.sendBizMessage(0, null);
 *      bizLooper.sendBizMessage(new Runnable() {
 *          @Override
 *          public void run() {
 *          }
 *      });
 * </pre>
 */
public class BizLooper implements BizRunnableWrapper.OnRunFinishObserver {
    private static final String TAG = "BizLooperTag";
    private Handler handler = null;
    private Looper looper = null;

    private boolean mUseMainLooper; // 是否使用主线程looper
    private boolean isBizLooperActive = false;// bizLooper是否可用(stop()后不可用)

    @Nullable
    private Thread bizThread = null; // 非null表示使用子线程
    private String bizThreadName = ""; // 非空表示子线程名, 否则为主线程
    private String threadNamePrefix = "Biz_"; // 子线程名前缀

    @Nullable
    private IBizHandler bizHandler = null; // 调用方实际处理类

    /**
     * 创建子线程 bizLooper
     * 参考 {@link #BizLooper(boolean, IBizHandler)}
     */
    public BizLooper(@Nullable IBizHandler bizHandler) {
        this(false, bizHandler);
    }

    /**
     * @param useMainLooper 是否使用ui线程的looper, false-创建子线程并初始化looper
     * @param bizHandler    将收到的message转发给调用方的 bizHandler 处理
     */
    public BizLooper(boolean useMainLooper, @Nullable IBizHandler bizHandler) {
        this.bizHandler = bizHandler;
        this.mUseMainLooper = useMainLooper;

        if (useMainLooper) {
            looper = Looper.getMainLooper();
            updateLooper(looper);
        } else {
            bizThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Thread thread = Thread.currentThread();
                    String threadInfo = "name:" + thread.getName() + ",id:" + thread.getId();

                    Log.w(TAG, "bizThread running, " + threadInfo);
                    Looper.prepare();
                    looper = Looper.myLooper();
                    updateLooper(looper);
                    Looper.loop();
                    Log.w(TAG, "bizThread exit, " + threadInfo);
                }
            });
        }
    }

    /**
     * 设置子线程名前缀, 最终拼接上id后生成实际的线程名
     * 要求在 {@link #start(int)} 之前设定才有效
     */
    public BizLooper setThreadNamePrefix(String prefix) {
        if (!TextUtils.isEmpty(prefix)) {
            threadNamePrefix = prefix;
        }
        return this;
    }

    /**
     * 启动动bizLooper功能
     *
     * @param id 用户可识别的id, 最终会拼接在线程名上(子线程looper时)
     * @return 是否启动成功
     */
    public boolean start(int id) {
        if (bizThread != null) {
            try {
                bizThreadName = threadNamePrefix + id;
                bizThread.setName(bizThreadName);
                bizThread.start();
                while (looper == null) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IllegalThreadStateException e) {
                e.printStackTrace();
            }
        }
        isBizLooperActive = true;

        Log.w(TAG, "start(" + id + ") mUseMainLooper=" + mUseMainLooper);
        // 非主线程looper才setBizLooper()
        if (mUseMainLooper) {
            return true;
        }

        boolean result = looper != null;
        Log.w(TAG, "setBizLooper(" + id + ") result= " + result + ",looper=" + looper);
        return result;
    }

    /**
     * 清空looper队列中现有的所有message
     */
    public void clearAllMessages() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            pendingRunnableCount.getAndSet(0);
        }
    }

    /**
     * 释放子线程looper
     */
    public void stop() {
        if (!isBizLooperActive) {
            return;
        }

        synchronized (this) {
            isBizLooperActive = false;
        }

        clearAllMessages();
        if (!mUseMainLooper && bizThread != null && bizThread.isAlive() && looper != null) {
            looper.quit();
        }
        looper = null;
        bizThread = null;
        bizHandler = null;
    }

    /**
     * @return 是否可用
     */
    public boolean isActive() {
        return isBizLooperActive;
    }

    private void updateLooper(Looper looper) {
        handler = new Handler(looper) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                // 注意: 带 callback 的 message 并不会执行到这里,因此若有限制,请提前到 sendBizMessage() 处判断
                if (!isBizLooperActive || msg == null) {
                    return;
                }

                // msg.what指定的message才交给调用方自定义处理方案
                // 此处不做同步, 发给上层调用方处理
                if (bizHandler != null) {
                    bizHandler.onHandleBizMessage(msg);
                }
            }
        };
    }

    public Looper getLooper() {
        return looper;
    }

    /**
     * 获取线程名, 若为空,则表示主线程
     */
    public String getBizThreadName() {
        return bizThreadName;
    }

    /**
     * 通过本方法发送的message,会仅looper队列中进行排队处理
     */
    public boolean sendBizMessage(Message msg) {
        synchronized (this) {
            if (handler == null || !isBizLooperActive) {
                return false;
            }
            return handler.sendMessage(msg);
        }
    }

    /**
     * 通过本方法发送的message,会仅looper队列中进行排队处理
     */
    public boolean sendBizMessage(int what, @Nullable Object obj) {
        Message msg = Message.obtain();
        msg.obj = obj;
        msg.what = what;
        return sendBizMessage(msg);
    }

    /**
     * 通过本方法发送的runnable:
     * 1. 若 mUseMainLooper = true, 且当前线程是主线程,则直接执行
     * 2. 否则发送到 looper 队列,排队处理;
     */
    public boolean sendBizMessage(@Nullable Runnable runnable) {
        if (runnable == null || handler == null || !isBizLooperActive) {
            return false;
        }

        // 当前是主线程,且不使用子线程bizLooper时,直接执行,避免主线程调用代码执行顺序不一致
        boolean isMainThread = Looper.getMainLooper() == looper;
        if (mUseMainLooper && isMainThread) {
            runnable.run();
            return true;
        }

        synchronized (this) {
            pendingRunnableCount.getAndIncrement();
            return handler.post(new BizRunnableWrapper(runnable, this));
        }
    }

    // 子线程中未执行完的runnable数量(已post到队列的数量)
    private AtomicInteger pendingRunnableCount = new AtomicInteger(0);
    // 当前是否有runnable正在执行
    private AtomicBoolean isRunnableActive = new AtomicBoolean(false);

    @Override
    public void onStartNotify() {
        synchronized (this) {
            isRunnableActive.getAndSet(true);
        }
    }

    @Override
    public void onFinishNotify() {
        synchronized (this) {
            pendingRunnableCount.getAndDecrement();
            isRunnableActive.getAndSet(false);
        }
    }

    public boolean isAllRunnableFinished() {
        synchronized (this) {
            return pendingRunnableCount.get() <= 0 && !isRunnableActive.get();
        }
    }
}
