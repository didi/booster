package com.didiglobal.booster.android.gradle.v8_0

import com.android.repository.Revision
import com.didiglobal.booster.android.gradle.v8_0.V80
import com.didiglobal.booster.gradle.AGPInterface
import com.didiglobal.booster.gradle.AGPInterfaceFactory
import com.google.auto.service.AutoService

@AutoService(AGPInterfaceFactory::class)
class V80Factory : AGPInterfaceFactory {

    override val revision: Revision = Revision(8, 0, 0)

    override fun newAGPInterface(): AGPInterface = V80

}
