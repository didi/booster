package com.didiglobal.booster.android.gradle.v7_1

import com.android.repository.Revision
import com.didiglobal.booster.gradle.AGPInterface
import com.didiglobal.booster.gradle.AGPInterfaceFactory
import com.google.auto.service.AutoService

@AutoService(AGPInterfaceFactory::class)
class V71Factory : AGPInterfaceFactory {

    override val revision: Revision = Revision(7, 1, 0)

    override fun newAGPInterface(): AGPInterface = V71

}
