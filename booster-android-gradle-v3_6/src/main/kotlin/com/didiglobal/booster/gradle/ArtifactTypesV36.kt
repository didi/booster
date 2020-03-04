package com.didiglobal.booster.gradle

import com.android.build.api.artifact.ArtifactType
import com.android.build.api.artifact.BuildArtifactType
import com.android.build.gradle.internal.api.artifact.SourceArtifactType
import com.android.build.gradle.internal.scope.AnchorOutputType
import com.android.build.gradle.internal.scope.InternalArtifactType
import org.gradle.api.file.FileSystemLocation

@JvmField
val ARTIFACT_TYPES = arrayOf(
        AnchorOutputType::class,
        BuildArtifactType::class,
        SourceArtifactType::class,
        InternalArtifactType::class).map {
    it.sealedSubclasses
}.flatten().map {
    it.objectInstance as ArtifactType<out FileSystemLocation>
}.map {
    it.javaClass.simpleName to it
}.toMap()