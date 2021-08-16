package com.didiglobal.booster.android.gradle.v3_3

import com.android.repository.Revision
import com.didiglobal.booster.gradle.AGPInterface
import com.didiglobal.booster.gradle.AGPInterfaceFactory
import com.google.auto.service.AutoService

@AutoService(AGPInterfaceFactory::class)
class V33Factory : AGPInterfaceFactory {

    override val revision: Revision = Revision(3, 3, 0)

    override fun newAGPInterface(): AGPInterface = V33

}
