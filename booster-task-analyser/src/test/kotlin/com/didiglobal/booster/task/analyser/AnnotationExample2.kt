package com.didiglobal.booster.task.analyser

import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class AnnotationExample2 {

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun fun1() {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun fun2() {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun fun3() {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun fun4() {
    }

}