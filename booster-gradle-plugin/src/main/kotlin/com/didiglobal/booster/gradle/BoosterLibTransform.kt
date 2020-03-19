package com.didiglobal.booster.gradle

import org.gradle.api.Project

/**
 * Represents android transform for library project
 *
 * @author johnsonlee
 */
@Deprecated(
        message = "Use class BoosterTransform instead",
        replaceWith = ReplaceWith(expression = "BoosterTransform(project)")
)
class BoosterLibTransform(project: Project) : BoosterTransform(project)
