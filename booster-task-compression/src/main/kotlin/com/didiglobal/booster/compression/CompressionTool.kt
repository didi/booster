package com.didiglobal.booster.compression

import com.didiglobal.booster.command.Command
import java.io.File

/**
 * Represents a compression tool
 *
 * @author johnsonlee
 *
 * @param command The command for image compression
 */
abstract class CompressionTool(val command: Command) : CompressionTaskCreatorFactory {

}
