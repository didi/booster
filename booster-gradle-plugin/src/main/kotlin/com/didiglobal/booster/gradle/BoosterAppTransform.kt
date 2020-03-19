package com.didiglobal.booster.gradle

import org.gradle.api.Project

/**
 * Represents android transform for application project
 *
 * @author johnsonlee
 */
@Deprecated(
        message = "Use class BoosterTransform instead",
        replaceWith = ReplaceWith(expression = "BoosterTransform(project)")
)
class BoosterAppTransform(project: Project) : BoosterTransform(project)
