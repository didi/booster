package com.didiglobal.booster.android.gradle.v8_1

import com.android.repository.Revision
import com.didiglobal.booster.gradle.AGPInterface
import com.didiglobal.booster.gradle.AGPInterfaceFactory
import com.google.auto.service.AutoService

@AutoService(AGPInterfaceFactory::class)
class V81Factory : AGPInterfaceFactory {

    override val revision: Revision = Revision(8, 1, 0)

    override fun newAGPInterface(): AGPInterface = V81

}
