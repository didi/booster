package com.didiglobal.booster.android.gradle.v8_3

import com.android.repository.Revision
import com.didiglobal.booster.gradle.AGPInterface
import com.didiglobal.booster.gradle.AGPInterfaceFactory
import com.google.auto.service.AutoService

@AutoService(AGPInterfaceFactory::class)
class V83Factory : AGPInterfaceFactory {

    override val revision: Revision = Revision(8, 3, 0)

    override fun newAGPInterface(): AGPInterface = V83

}
