package com.didiglobal.booster.android.gradle.v7_4

import com.android.repository.Revision
import com.didiglobal.booster.gradle.AGPInterface
import com.didiglobal.booster.gradle.AGPInterfaceFactory
import com.google.auto.service.AutoService

@AutoService(AGPInterfaceFactory::class)
class V74Factory : AGPInterfaceFactory {

    override val revision: Revision = Revision(7, 4, 2)

    override fun newAGPInterface(): AGPInterface = V74

}
