package com.didiglobal.booster.gradle

import com.android.build.api.artifact.ArtifactType
import com.android.build.api.artifact.BuildArtifactType
import com.android.build.gradle.internal.api.artifact.SourceArtifactType
import com.android.build.gradle.internal.scope.AnchorOutputType
import com.android.build.gradle.internal.scope.InternalArtifactType
import com.android.build.gradle.internal.scope.SingleArtifactType
import org.gradle.api.file.FileSystemLocation

@JvmField
val ARTIFACT_TYPES = arrayOf(
        AnchorOutputType::class,
        InternalArtifactType::class).map {
    it.sealedSubclasses
}.flatten().map {
    it.objectInstance as SingleArtifactType<out FileSystemLocation>
}.map {
    it.javaClass.simpleName to it
}.toMap()