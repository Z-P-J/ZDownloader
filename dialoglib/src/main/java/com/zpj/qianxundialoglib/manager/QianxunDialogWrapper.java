package com.zpj.qianxundialoglib.manager;

import com.zpj.qianxundialoglib.QianxunDialog;

/**
 * 管理多个dialog 按照dialog的优先级依次弹出
 * Created by mq on 2018/9/16 下午9:44
 * mqcoder90@gmail.com
 */

public class QianxunDialogWrapper {

    private QianxunDialog dialog;//统一管理dialog的弹出顺序

    public QianxunDialogWrapper(QianxunDialog dialog) {
        this.dialog = dialog;
    }

    public QianxunDialog getDialog() {
        return dialog;
    }

    public void setDialog(QianxunDialog dialog) {
        this.dialog = dialog;
    }

}
