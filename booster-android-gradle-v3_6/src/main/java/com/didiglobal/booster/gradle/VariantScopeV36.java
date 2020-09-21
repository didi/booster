package com.didiglobal.booster.gradle;

import com.android.build.api.artifact.ArtifactType;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.internal.res.LinkAndroidResForBundleTask;
import com.android.build.gradle.internal.scope.AnchorOutputType;
import com.android.build.gradle.internal.scope.InternalArtifactType;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.tasks.PackageBundleTask;
import com.android.build.gradle.tasks.ProcessAndroidResources;
import com.android.sdklib.BuildToolInfo;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.tasks.TaskContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.didiglobal.booster.gradle.ArtifactTypesV36Kt.ARTIFACT_TYPES;

class VariantScopeV36 {

    @NotNull
    static BaseExtension getExtension(@NotNull final VariantScope scope) {
        return scope.getGlobalScope().getExtension();
    }

    /**
     * The merged AndroidManifest.xml
     */
    @NotNull
    static Collection<File> getMergedManifests(@NotNull final VariantScope scope) {
        return getFinalArtifactFiles(scope, InternalArtifactType.BUNDLE_MANIFEST.INSTANCE);
    }

    /**
     * The merged resources
     */
    @NotNull
    static Collection<File> getMergedRes(@NotNull final VariantScope scope) {
        return getFinalArtifactFiles(scope, InternalArtifactType.MERGED_RES.INSTANCE);
    }

    /**
     * The merged assets
     */
    @NotNull
    static Collection<File> getMergedAssets(@NotNull final VariantScope scope) {
        return getFinalArtifactFiles(scope, InternalArtifactType.MERGED_ASSETS.INSTANCE);
    }

    /**
     * The processed resources
     */
    @NotNull
    static Collection<File> getProcessedRes(@NotNull final VariantScope scope) {
        return getFinalArtifactFiles(scope, InternalArtifactType.PROCESSED_RES.INSTANCE);
    }

    /**
     * All of classes
     */
    @NotNull
    static Collection<File> getAllClasses(@NotNull final VariantScope scope) {
        return getFinalArtifactFiles(scope, AnchorOutputType.ALL_CLASSES.INSTANCE);
    }

    @NotNull
    static Collection<File> getSymbolList(@NotNull final VariantScope scope) {
        return getFinalArtifactFiles(scope, InternalArtifactType.RUNTIME_SYMBOL_LIST.INSTANCE);
    }

    @NotNull
    static Collection<File> getSymbolListWithPackageName(@NotNull final VariantScope scope) {
        return getFinalArtifactFiles(scope, InternalArtifactType.SYMBOL_LIST_WITH_PACKAGE_NAME.INSTANCE);
    }

    @NotNull
    static Collection<File> getAar(@NotNull final VariantScope scope) {
        return getFinalArtifactFiles(scope, InternalArtifactType.AAR.INSTANCE);
    }

    @NotNull
    static Collection<File> getApk(@NotNull final VariantScope scope) {
        return getFinalArtifactFiles(scope, InternalArtifactType.APK.INSTANCE);
    }

    @NotNull
    static Collection<File> getJavac(@NotNull final VariantScope scope) {
        return getFinalArtifactFiles(scope, InternalArtifactType.JAVAC.INSTANCE);
    }

    @NotNull
    static Map<String, Collection<File>> getAllArtifacts(@NotNull final VariantScope scope) {
        return ARTIFACT_TYPES.entrySet().stream()
                .map(it -> new AbstractMap.SimpleEntry<>(it.getKey(), getFinalArtifactFiles(scope, it.getValue())))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    @NotNull
    static Collection<File> getFinalArtifactFiles(@NotNull final VariantScope scope, @NotNull final ArtifactType<? extends FileSystemLocation> type) {
        return Stream.of(scope.getArtifacts().getFinalProduct(type).map(FileSystemLocation::getAsFile).getOrNull())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @NotNull
    static BuildToolInfo getBuildTools(@NotNull final VariantScope scope) {
        return scope.getGlobalScope().getSdkComponents().getBuildToolInfoProvider().get();
    }

    @NotNull
    static Collection<File> getRawAndroidResources(@NotNull final VariantScope scope) {
        return scope.getVariantData().getAllRawAndroidResources().getFiles();
    }

    @NotNull
    static ProcessAndroidResources getProcessResourcesTask(@NotNull final VariantScope scope) {
        final TaskContainer tasks = scope.getGlobalScope().getProject().getTasks();
        return (ProcessAndroidResources) tasks.getByName(scope.getTaskName("process", "Resources"));
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
    static PackageBundleTask getPackageBundleTask(@NotNull VariantScope scope) {
        final TaskContainer tasks = scope.getGlobalScope().getProject().getTasks();
        try {
            return (PackageBundleTask) tasks.getByName(scope.getTaskName("package", "Bundle"));
        } catch (final UnknownTaskException e) {
            return null;
        }
    }
}
