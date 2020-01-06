package com.didiglobal.booster.task.compression.pngquant

import com.didiglobal.booster.compression.CompressionOptions
import com.didiglobal.booster.compression.CompressionTool

class PngquantOptions(quality: Int, val speed: Int) : CompressionOptions(quality = quality)
