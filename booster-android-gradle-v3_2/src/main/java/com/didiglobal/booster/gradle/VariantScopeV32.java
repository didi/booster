package com.didiglobal.booster.gradle;

import com.android.build.api.artifact.ArtifactType;
import com.android.build.api.artifact.BuildArtifactType;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.internal.api.artifact.SourceArtifactType;
import com.android.build.gradle.internal.res.LinkAndroidResForBundleTask;
import com.android.build.gradle.internal.scope.AnchorOutputType;
import com.android.build.gradle.internal.scope.InternalArtifactType;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.tasks.ProcessAndroidResources;
import com.android.sdklib.BuildToolInfo;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.tasks.TaskContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class VariantScopeV32 {

    @NotNull
    static BaseExtension getExtension(@NotNull final VariantScope scope) {
        return (BaseExtension) scope.getGlobalScope().getExtension();
    }

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
    static Collection<File> getAar(@NotNull final VariantScope scope) {
        return getFinalArtifactFiles(scope, InternalArtifactType.AAR);
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
        return Stream.of(
                AnchorOutputType.values(),
                BuildArtifactType.values(),
                SourceArtifactType.values(),
                InternalArtifactType.values()
        ).flatMap(Arrays::stream).collect(Collectors.toMap(Enum::name, v -> getFinalArtifactFiles(scope, v)));
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

    @NotNull
    static ProcessAndroidResources getProcessResourcesTask(@NotNull final VariantScope scope) {
        return scope.getProcessResourcesTask();
    }

    @Nullable
    static LinkAndroidResForBundleTask getBundleResourceTask(@NotNull final VariantScope scope) {
        final TaskContainer tasks = scope.getGlobalScope().getProject().getTasks();
        try {
            return (LinkAndroidResForBundleTask) tasks.getByName(scope.getTaskName("bundle", "Resources"));
        } catch (final UnknownTaskException e) {
            return null;
        }
    }

    @Nullable
    static Task getPackageBundleTask(@NotNull final VariantScope scope) {
        return null;
    }
}
