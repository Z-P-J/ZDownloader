package com.zpj.zdialog.base;

import android.support.annotation.IdRes;
import android.view.View;

public interface ZAbstractDialog {

    void dismiss();

    void dismissWithoutAnim();

    void hide();

    ZAbstractDialog show();

    <T extends View> T getView(@IdRes int id);

}
