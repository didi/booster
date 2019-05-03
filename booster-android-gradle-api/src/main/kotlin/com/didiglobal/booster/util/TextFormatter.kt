package com.didiglobal.booster.util

import java.util.logging.LogRecord
import java.util.logging.SimpleFormatter

/**
 * Represents a log message formatter
 *
 * @author johnsonlee
 */
class TextFormatter : SimpleFormatter() {

    @Synchronized
    override fun format(record: LogRecord) = "${record.level.localizedName}: ${record.message}\n"

}
