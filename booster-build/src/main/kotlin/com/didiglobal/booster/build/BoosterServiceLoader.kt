package com.didiglobal.booster.build

import com.didiglobal.booster.annotations.Priority
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
        }.sortedBy {
            it.javaClass.getAnnotation(Priority::class.java)?.value ?: 0
        }

    }

}