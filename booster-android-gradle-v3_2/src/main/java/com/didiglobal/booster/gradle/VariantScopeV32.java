package com.didiglobal.booster.gradle;

import com.android.build.api.artifact.ArtifactType;
import com.android.build.gradle.internal.publishing.AndroidArtifacts;
import com.android.build.gradle.internal.scope.AnchorOutputType;
import com.android.build.gradle.internal.scope.InternalArtifactType;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.sdklib.BuildToolInfo;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.android.build.gradle.internal.publishing.AndroidArtifacts.ArtifactScope.ALL;
import static com.android.build.gradle.internal.publishing.AndroidArtifacts.ConsumedConfigType.RUNTIME_CLASSPATH;

class VariantScopeV32 {

    /**
     * The merged AndroidManifest.xml
     */
    @NotNull
    static Collection<File> getMergedManifests(@NotNull final VariantScope scope) {
        return getFinalArtifactFiles(scope, InternalArtifactType.MERGED_MANIFESTS);
    }

    /**
     * The merged resources
     */
    @NotNull
    static Collection<File> getMergedRes(@NotNull final VariantScope scope) {
        return getFinalArtifactFiles(scope, InternalArtifactType.MERGED_RES);
    }

    /**
     * The merged assets
     */
    @NotNull
    static Collection<File> getMergedAssets(@NotNull final VariantScope scope) {
        return getFinalArtifactFiles(scope, InternalArtifactType.MERGED_ASSETS);
    }

    /**
     * The processed resources
     */
    @NotNull
    static Collection<File> getProcessedRes(@NotNull final VariantScope scope) {
        return getFinalArtifactFiles(scope, InternalArtifactType.PROCESSED_RES);
    }

    /**
     * All of classes
     */
    @NotNull
    static Collection<File> getAllClasses(@NotNull final VariantScope scope) {
        return getFinalArtifactFiles(scope, AnchorOutputType.ALL_CLASSES);
    }

    @NotNull
    static Collection<File> getSymbolList(@NotNull final VariantScope scope) {
        return getFinalArtifactFiles(scope, InternalArtifactType.SYMBOL_LIST);
    }

    @NotNull
    static Collection<File> getSymbolListWithPackageName(@NotNull final VariantScope scope) {
        return getFinalArtifactFiles(scope, InternalArtifactType.SYMBOL_LIST_WITH_PACKAGE_NAME);
    }

    @NotNull
    static Collection<File> getApk(@NotNull final VariantScope scope) {
        return getFinalArtifactFiles(scope, InternalArtifactType.APK);
    }

    @NotNull
    static Collection<File> getJavac(@NotNull final VariantScope scope) {
        return getFinalArtifactFiles(scope, InternalArtifactType.JAVAC);
    }

    @NotNull
    static Map<String, Collection<File>> getAllArtifacts(@NotNull final VariantScope scope) {
        return Stream.concat(Arrays.stream(InternalArtifactType.values()), Arrays.stream(AnchorOutputType.values()))
                .collect(Collectors.toMap(Enum::name, v -> getFinalArtifactFiles(scope, v)));
    }

    @NotNull
    static Collection<File> getFinalArtifactFiles(@NotNull final VariantScope scope, @NotNull final ArtifactType type) {
        return scope.getArtifacts().getFinalArtifactFiles(type).getFiles();
    }

    @NotNull
    static BuildToolInfo getBuildTools(@NotNull final VariantScope scope) {
        return scope.getGlobalScope().getAndroidBuilder().getBuildToolInfo();
    }

    @NotNull
    static File getDataBindingDependencyArtifacts(@NotNull final VariantScope scope) {
        return scope.getArtifacts().getFinalArtifactFiles(InternalArtifactType.DATA_BINDING_DEPENDENCY_ARTIFACTS)
                .get()
                .getSingleFile();
    }
}
