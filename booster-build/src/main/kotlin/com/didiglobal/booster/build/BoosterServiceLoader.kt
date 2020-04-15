package com.didiglobal.booster.build

import java.util.ServiceLoader

/**
 * @author johnsonlee
 */
class BoosterServiceLoader {

    companion object {

        fun <S : Any> load(service: Class<S>, classLoader: ClassLoader): List<S> = listOf(
                ServiceLoader.load(service),
                ServiceLoader.load(service, classLoader)
        ).flatten().distinctBy {
            it.javaClass
        }

    }

}