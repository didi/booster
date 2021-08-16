package com.didiglobal.booster.android.gradle.v3_4

import com.android.repository.Revision
import com.didiglobal.booster.gradle.AGPInterface
import com.didiglobal.booster.gradle.AGPInterfaceFactory
import com.google.auto.service.AutoService

@AutoService(AGPInterfaceFactory::class)
class V34Factory : AGPInterfaceFactory {

    override val revision: Revision = Revision(3, 4, 0)

    override fun newAGPInterface(): AGPInterface = V34

}
