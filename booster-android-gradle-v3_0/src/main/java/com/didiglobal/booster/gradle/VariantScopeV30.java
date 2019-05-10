package com.didiglobal.booster.gradle;

import com.android.build.gradle.internal.scope.TaskOutputHolder.AnchorOutputType;
import com.android.build.gradle.internal.scope.TaskOutputHolder.OutputType;
import com.android.build.gradle.internal.scope.TaskOutputHolder.TaskOutputType;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.sdklib.BuildToolInfo;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class VariantScopeV30 {

    /**
     * The merged AndroidManifest.xml
     */
    @NotNull
    static Collection<File> getMergedManifests(@NotNull final VariantScope scope) {
        return scope.getOutput(TaskOutputType.MERGED_MANIFESTS).getFiles();
    }

    /**
     * The merged resources
     */
    @NotNull
    static Collection<File> getMergedRes(@NotNull final VariantScope scope) {
        return scope.getOutput(TaskOutputType.MERGED_RES).getFiles();
    }

    /**
     * The merged assets
     */
    @NotNull
    static Collection<File> getMergedAssets(@NotNull final VariantScope scope) {
        return scope.getOutput(TaskOutputType.MERGED_ASSETS).getFiles();
    }

    /**
     * The processed resources
     */
    @NotNull
    static Collection<File> getProcessedRes(@NotNull final VariantScope scope) {
        return scope.getOutput(TaskOutputType.PROCESSED_RES).getFiles();
    }

    /**
     * All of classes
     */
    @NotNull
    static Collection<File> getAllClasses(@NotNull final VariantScope scope) {
        return scope.getOutput(AnchorOutputType.ALL_CLASSES).getFiles();
    }

    @NotNull
    static Collection<File> getSymbolList(@NotNull final VariantScope scope) {
        return getOutput(scope, TaskOutputType.SYMBOL_LIST);
    }

    @NotNull
    static Collection<File> getSymbolListWithPackageName(@NotNull final VariantScope scope) {
        return getOutput(scope, TaskOutputType.SYMBOL_LIST_WITH_PACKAGE_NAME);
    }

    @NotNull
    static Collection<File> getApk(@NotNull final VariantScope scope) {
        return getOutput(scope, TaskOutputType.APK);
    }

    @NotNull
    static Collection<File> getJavac(@NotNull final VariantScope scope) {
        return getOutput(scope, TaskOutputType.JAVAC);
    }

    @NotNull
    static Map<String, Collection<File>> getAllArtifacts(@NotNull final VariantScope scope) {
        return Stream.concat(Arrays.stream(TaskOutputType.values()), Arrays.stream(AnchorOutputType.values()))
                .collect(Collectors.toMap(OutputType::name, v -> getOutput(scope, v)));
    }

    @NotNull
    static Collection<File> getOutput(@NotNull final VariantScope scope, @NotNull final OutputType type) {
        return scope.getOutput(type).getFiles();
    }

    @NotNull
    static BuildToolInfo getBuildTools(@NotNull final VariantScope scope) {
        return scope.getGlobalScope().getAndroidBuilder().getBuildToolInfo();
    }

}
