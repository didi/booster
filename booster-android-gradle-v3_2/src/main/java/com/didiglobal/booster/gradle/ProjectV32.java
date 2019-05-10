package com.didiglobal.booster.gradle;

import com.android.build.gradle.options.BooleanOption;
import com.android.build.gradle.options.ProjectOptions;
import org.gradle.api.Project;

class ProjectV32 {

    static boolean isAapt2Enabled(final Project project) {
        return new ProjectOptions(project).get(BooleanOption.ENABLE_AAPT2);
    }

}
