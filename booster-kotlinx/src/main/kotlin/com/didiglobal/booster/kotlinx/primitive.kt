package com.didiglobal.booster.kotlinx

data class byte(val value: Byte = 0) {
    constructor(value: Byte? = null) : this(value ?: 0)
}

data class char(val value: Char = '\u0000') {
    constructor(value: Char? = null) : this(value ?: '\u0000')
}

data class short(val value: Short = 0) {
    constructor(value: Short? = null) : this(value ?: 0)
}

data class int(val value: Int = 0) {
    constructor(value: Int? = null) : this(value ?: 0)
}

data class float(val value: Float = 0f) {
    constructor(value: Float? = null) : this(value ?: 0f)
}

data class double(val value: Double = 0.0) {
    constructor(value: Double? = null) : this(value ?: 0.0)
}

data class boolean(val value: Boolean = false) {
    constructor(value: Boolean? = null) : this(value ?: false)
}
