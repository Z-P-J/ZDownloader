package com.zpj.qianxundialoglib.manager;

import com.zpj.qianxundialoglib.QianxunDialog;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 支持多个Dialog依次弹出
 * Created by mq on 2018/9/1 下午4:35
 * mqcoder90@gmail.com
 */

public class QianxunDialogsManager {

    private volatile boolean showing = false;//是否有dialog在展示
    private ConcurrentLinkedQueue<QianxunDialogWrapper> dialogQueue = new ConcurrentLinkedQueue<>();

    private QianxunDialogsManager() {
    }

    public static QianxunDialogsManager getInstance() {
        return DialogHolder.instance;
    }

    private static class DialogHolder {
        private static QianxunDialogsManager instance = new QianxunDialogsManager();
    }

    /**
     * 请求加入队列并展示
     *
     * @param qianxunDialogWrapper QianxunDialogWrapper
     * @return 加入队列是否成功
     */
    public synchronized boolean requestShow(QianxunDialogWrapper qianxunDialogWrapper) {
        boolean b = dialogQueue.offer(qianxunDialogWrapper);
        checkAndDispatch();
        return b;
    }

    /**
     * 结束一次展示 并且检查下一个弹窗
     */
    public synchronized void over() {
        showing = false;
        next();
    }

    private synchronized void checkAndDispatch() {
        if (!showing) {
            next();
        }
    }

    /**
     * 弹出下一个弹窗
     */
    private synchronized void next() {
        QianxunDialogWrapper poll = dialogQueue.poll();
        if (poll == null) return;
        QianxunDialog dialog = poll.getDialog();
        if (dialog != null) {
            showing = true;
            dialog.show();
        }
    }


}
