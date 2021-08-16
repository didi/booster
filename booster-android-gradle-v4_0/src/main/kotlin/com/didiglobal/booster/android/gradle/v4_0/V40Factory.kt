package com.didiglobal.booster.android.gradle.v4_0

import com.android.repository.Revision
import com.didiglobal.booster.gradle.AGPInterface
import com.didiglobal.booster.gradle.AGPInterfaceFactory
import com.google.auto.service.AutoService

@AutoService(AGPInterfaceFactory::class)
class V40Factory : AGPInterfaceFactory {

    override val revision: Revision = Revision(4, 0, 0)

    override fun newAGPInterface(): AGPInterface = V40

}
