package com.didiglobal.booster.android.gradle.v4_2

import com.android.repository.Revision
import com.didiglobal.booster.gradle.AGPInterface
import com.didiglobal.booster.gradle.AGPInterfaceFactory
import com.google.auto.service.AutoService

@AutoService(AGPInterfaceFactory::class)
class V42Factory : AGPInterfaceFactory {

    override val revision: Revision = Revision(4, 2, 0)

    override fun newAGPInterface(): AGPInterface = V42

}
