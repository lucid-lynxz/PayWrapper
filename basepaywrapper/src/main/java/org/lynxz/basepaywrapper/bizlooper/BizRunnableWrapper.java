
package org.lynxz.basepaywrapper.bizlooper;

import android.util.Log;

/**
 * 包装一层runnable, 用于子线程执行时打印日志, 便于排查问题
 */
final class BizRunnableWrapper implements Runnable {
    private static final String TAG = "BizRunnableWrapper";

    /**
     * runnable执行结束后回调
     */
    public interface OnRunFinishObserver {
        void onStartNotify();

        void onFinishNotify();
    }

    private Runnable oriRunnable;
    private OnRunFinishObserver onRunFinishObserver;

    BizRunnableWrapper(Runnable oriRunnable) {
        this(oriRunnable, null);
    }

    BizRunnableWrapper(Runnable oriRunnable, OnRunFinishObserver onRunFinishObserver) {
        this.oriRunnable = oriRunnable;
        this.onRunFinishObserver = onRunFinishObserver;
    }

    @Override
    public void run() {
        if (onRunFinishObserver != null) {
            onRunFinishObserver.onStartNotify();
        }

        if (oriRunnable != null) {
            Thread thread = Thread.currentThread();
            String threadInfo = ", name:" + thread.getName() + ",id:" + thread.getId();

            Log.w(TAG, "BizRunnableWrapper running start:" + oriRunnable + threadInfo);
            oriRunnable.run();
            Log.w(TAG, "BizRunnableWrapper running end:" + oriRunnable);
        }

        if (onRunFinishObserver != null) {
            onRunFinishObserver.onFinishNotify();
        }
    }
}
