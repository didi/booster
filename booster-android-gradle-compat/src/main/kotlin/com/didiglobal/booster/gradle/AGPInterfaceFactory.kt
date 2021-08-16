package com.didiglobal.booster.gradle

import com.android.repository.Revision

interface AGPInterfaceFactory {
    val revision: Revision
    fun newAGPInterface(): AGPInterface
}