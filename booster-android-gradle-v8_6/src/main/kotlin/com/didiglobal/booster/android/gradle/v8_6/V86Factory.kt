package com.didiglobal.booster.android.gradle.v8_6

import com.android.repository.Revision
import com.didiglobal.booster.gradle.AGPInterface
import com.didiglobal.booster.gradle.AGPInterfaceFactory
import com.google.auto.service.AutoService

@AutoService(AGPInterfaceFactory::class)
class V86Factory : AGPInterfaceFactory {

    override val revision: Revision = Revision(8, 6, 0)

    override fun newAGPInterface(): AGPInterface = V86

}
