package com.didiglobal.booster.android.gradle.v3_5

import com.android.repository.Revision
import com.didiglobal.booster.gradle.AGPInterface
import com.didiglobal.booster.gradle.AGPInterfaceFactory
import com.google.auto.service.AutoService

@AutoService(AGPInterfaceFactory::class)
class V35Factory : AGPInterfaceFactory {

    override val revision: Revision = Revision(3, 5, 0)

    override fun newAGPInterface(): AGPInterface = V35

}
