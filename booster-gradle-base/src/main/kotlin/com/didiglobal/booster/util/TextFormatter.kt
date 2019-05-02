package com.didiglobal.booster.util

import java.util.logging.LogRecord
import java.util.logging.SimpleFormatter

class TextFormatter : SimpleFormatter() {

    @Synchronized
    override fun format(record: LogRecord): String {
        return "${record.message}\n"
    }

}
