package com.didiglobal.booster.android.gradle.v7_2

import com.android.repository.Revision
import com.didiglobal.booster.gradle.AGPInterface
import com.didiglobal.booster.gradle.AGPInterfaceFactory
import com.google.auto.service.AutoService

@AutoService(AGPInterfaceFactory::class)
class V72Factory : AGPInterfaceFactory {

    override val revision: Revision = Revision(7, 2, 0)

    override fun newAGPInterface(): AGPInterface = V72

}
