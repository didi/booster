package com.didiglobal.booster.aapt

/**
 * @author johnsonlee
 *
 * @see https://android.googlesource.com/platform/frameworks/base/+/master/libs/androidfw/include/androidfw/ResourceTypes.h
 */
data class Configuration(
        var size: Int = 0,
        var imsi: Imsi = Imsi(),
        var locale: Locale = Locale(),
        var screenType: ScreenType = ScreenType(),
        var input: Input = Input(),
        var screenSize: ScreenSize = ScreenSize(),
        var version: Version = Version(),
        var screenConfig: ScreenConfig = ScreenConfig(),
        var screenSizeDp: ScreenSize = ScreenSize(),

        /**
         * The ISO-15924 short name for the script corresponding to this
         * configuration. (eg. Hant, Latn, etc.). Interpreted in conjunction
         * with the locale field.
         */
        var localeScript: ByteArray = ByteArray(4),
        /**
         * A single BCP-47 variant subtag. Will vary in length between 4 and 8
         * chars. Interpreted in conjunction with the locale field.
         */
        var localeVariant: ByteArray = ByteArray(8),
        var screenConfig2: ScreenConfig2 = ScreenConfig2()) {

    /**
     * Deep copy constructor
     *
     * @param configuration The original instance
     */
    constructor(configuration: Configuration) : this(
            configuration.size,
            configuration.imsi.copy(),
            configuration.locale.copy(language = configuration.locale.language, country = configuration.locale.country),
            configuration.screenType.copy(),
            configuration.input.copy(),
            configuration.screenSize.copy(),
            configuration.version.copy(),
            configuration.screenConfig.copy(),
            configuration.screenSizeDp.copy(),
            configuration.localeScript.copyOf(),
            configuration.localeVariant.copyOf(),
            configuration.screenConfig2.copy())

    private fun unpackLanguageOrRegion(data: ByteArray, base: Byte): ByteArray {
        if (0 != data[0].toInt() and 0x80) {
            val first = (data[1].toInt() and 0x1f).toByte()
            val second = ((data[1].toInt() and 0xe0 shr 5) + (data[0].toInt() and 0x03 shl 3)).toByte()
            val third = (data[0].toInt() and 0x7c shr 2).toByte()
            return byteArrayOf((first + base).toByte(), (second + base).toByte(), (third + base).toByte())
        }

        return if (0 != data[0].toInt()) {
            byteArrayOf(data[0], data[1])
        } else ByteArray(0)

    }

    private fun unpackLanguage(): ByteArray {
        return unpackLanguageOrRegion(this.locale.language, 0x61.toByte())
    }

    private fun unpackRegion(): ByteArray {
        return unpackLanguageOrRegion(this.locale.country, 0x30.toByte())
    }

    private fun appendLocaleDir(builder: StringBuilder) {
        if (0 != this.locale.language[0].toInt()) {
            return
        }

        if (0 == this.localeScript[0].toInt() && 0 == this.localeVariant[0].toInt()) {
            if (builder.isNotEmpty()) {
                builder.append("-")
            }

            builder.append(String(unpackLanguage()))

            if (0 != this.locale.country[0].toInt()) {
                builder.append("-r")
                builder.append(String(unpackRegion()))
            }

            return
        }

        if (builder.isNotEmpty()) {
            builder.append("-")
        }

        builder.append("b+")
        builder.append(String(unpackLanguage()))

        if (0 != this.localeScript[0].toInt()) {
            builder.append("+")
            builder.append(String(this.localeScript))
        }

        if (0 != this.locale.country[0].toInt()) {
            builder.append("+")
            builder.append(String(unpackRegion()))
        }

        if (0 != this.localeVariant[0].toInt()) {
            builder.append("+")
            builder.append(String(this.localeVariant))
        }
    }

    override fun toString() = toString(StringBuilder())

    fun toString(sequence: CharSequence) = toString(when (sequence) {
        is StringBuilder -> sequence
        else -> StringBuilder(sequence)
    })

    fun toString(builder: StringBuilder): String {
        if (this.imsi.mcc.toInt() != 0) {
            if (builder.isNotEmpty()) {
                builder.append("-")
            }
            builder.append("mcc${this.imsi.mcc}")
        }

        if (this.imsi.mnc.toInt() != 0) {
            if (builder.isNotEmpty()) {
                builder.append("-")
            }
            builder.append("mnc${this.imsi.mnc}")
        }

        appendLocaleDir(builder)

        if (this.screenConfig.smallestWidthDp.toInt() != 0) {
            if (builder.isNotEmpty()) {
                builder.append("-")
            }
            builder.append("sw${this.screenConfig.smallestWidthDp}dp")
        }

        if (this.screenSizeDp.width.toInt() != 0) {
            if (builder.isNotEmpty()) {
                builder.append("-")
            }
            builder.append("w${this.screenSizeDp.width}dp")
        }

        if (this.screenSizeDp.height.toInt() != 0) {
            if (builder.isNotEmpty()) {
                builder.append("-")
            }
            builder.append("h${this.screenSizeDp.height}dp")
        }

        if (this.screenType.orientation.toInt() != ScreenType.ORIENTATION_ANY) {
            if (builder.isNotEmpty()) {
                builder.append("-")
            }

            when (this.screenType.orientation.toInt()) {
                ScreenType.ORIENTATION_PORT -> builder.append("port")
                ScreenType.ORIENTATION_LAND -> builder.append("land")
                ScreenType.ORIENTATION_SQUARE -> builder.append("square")
                else -> builder.append("orientation=${this.screenType.orientation}")
            }
        }

        if (this.screenType.density != ScreenType.DENSITY_DEFAULT) {
            if (builder.isNotEmpty()) {
                builder.append("-")
            }

            when (this.screenType.density) {
                ScreenType.DENSITY_LOW -> builder.append("ldpi")
                ScreenType.DENSITY_MEDIUM -> builder.append("mdpi")
                ScreenType.DENSITY_TV -> builder.append("tvdpi")
                ScreenType.DENSITY_HIGH -> builder.append("hdpi")
                ScreenType.DENSITY_XHIGH -> builder.append("xhdpi")
                ScreenType.DENSITY_XXHIGH -> builder.append("xxhdpi")
                ScreenType.DENSITY_XXXHIGH -> builder.append("xxxhdpi")
                ScreenType.DENSITY_NONE -> builder.append("nodpi")
                ScreenType.DENSITY_ANY -> builder.append("anydpi")
                else -> builder.append("${this.screenType.density}dpi")
            }
        }

        if (this.screenSize.width.toInt() != 0 || this.screenSize.height.toInt() != 0) {
            if (builder.isNotEmpty()) {
                builder.append("-")
            }
            builder.append("${this.screenSize.width}x${this.screenSize.height}")
        }

        if (this.version.sdk.toInt() != 0 || this.version.minor.toInt() != 0) {
            if (builder.isNotEmpty()) {
                builder.append("-")
            }
            builder.append("v${this.version.sdk}")
            if (this.version.minor.toInt() != 0) {
                builder.append(".${this.version.minor}")
            }
        }

        return builder.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Configuration

        if (size != other.size) return false
        if (imsi != other.imsi) return false
        if (locale != other.locale) return false
        if (screenType != other.screenType) return false
        if (input != other.input) return false
        if (screenSize != other.screenSize) return false
        if (version != other.version) return false
        if (screenConfig != other.screenConfig) return false
        if (screenSizeDp != other.screenSizeDp) return false
        if (!localeScript.contentEquals(other.localeScript)) return false
        if (!localeVariant.contentEquals(other.localeVariant)) return false
        if (screenConfig2 != other.screenConfig2) return false

        return true
    }

    override fun hashCode(): Int {
        var result = size
        result = 31 * result + imsi.hashCode()
        result = 31 * result + locale.hashCode()
        result = 31 * result + screenType.hashCode()
        result = 31 * result + input.hashCode()
        result = 31 * result + screenSize.hashCode()
        result = 31 * result + version.hashCode()
        result = 31 * result + screenConfig.hashCode()
        result = 31 * result + screenSizeDp.hashCode()
        result = 31 * result + localeScript.contentHashCode()
        result = 31 * result + localeVariant.contentHashCode()
        result = 31 * result + screenConfig2.hashCode()
        return result
    }

    data class Imsi(

            /**
             * Mobile country code (from SIM). 0 means "any"
             */
            var mcc: Short = 0,

            /**
             * Mobile network code (from SIM). 0 means "any".
             */
            var mnc: Short = 0
    )

    data class Locale(
            /**
             * This field can take three different forms:
             *
             *
             * \0\0 means "any".
             * Two 7 bit ascii values interpreted as ISO-639-1 language
             * codes ('fr', 'en' etc. etc.). The high bit for both bytes is
             * zero.
             * A single 16 bit little endian packed value representing an
             * ISO-639-2 3 letter language code. This will be of the form:
             *
             * <pre>
             * { 1, t, t, t, t, t, s, s, s, s, s, f, f, f, f, f }
             * </pre>
             *
             * bit[0, 4] = first letter of the language code<br></br>
             * bit[5, 9] = second letter of the language code<br></br>
             * bit[10, 14] = third letter of the language code.<br></br>
             * bit[15] = 1 always<br></br>
             *
             * For backwards compatibility, languages that have unambiguous two
             * letter codes are represented in that format.
             *
             * The layout is always bigendian irrespective of the runtime
             * architecture.
             */
            var language: ByteArray = ByteArray(2),

            /**
             * This field can take three different forms:
             *
             * \0\0 means "any".
             * Two 7 bit ascii values interpreted as 2 letter region codes
             * ('US', 'GB' etc.). The high bit for both bytes is zero.
             * An UN M.49 3 digit region code. For simplicity, these are
             * packed in the same manner as the language codes, though we should
             * need only 10 bits to represent them, instead of the 15.
             *
             * The layout is always bigendian irrespective of the runtime
             * architecture.
             */
            var country: ByteArray = ByteArray(2)
    ) {

        val isDefined: Boolean
            get() = this.language[0].toInt() == 0 && this.language[1].toInt() == 0 && this.country[0].toInt() == 0 && this.country[1].toInt() == 0

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Locale

            if (!language.contentEquals(other.language)) return false
            if (!country.contentEquals(other.country)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = language.contentHashCode()
            result = 31 * result + country.contentHashCode()
            return result
        }
    }

    data class ScreenType(
            var orientation: Byte = 0,
            var touchscreen: Byte = 0,
            var density: Short = 0
    ) {
        companion object {
            /**
             * Orientation: not specified
             */
            const val ORIENTATION_ANY = 0x0000
            const val ORIENTATION_PORT = 0x0001
            const val ORIENTATION_LAND = 0x0002
            const val ORIENTATION_SQUARE = 0x0003

            const val TOUCHSCREEN_ANY = 0x0000
            const val TOUCHSCREEN_NOTOUCH = 0x0001
            const val TOUCHSCREEN_STYLUS = 0x0002
            const val TOUCHSCREEN_FINGER = 0x0003

            const val DENSITY_DEFAULT: Short = 0
            const val DENSITY_LOW: Short = 120
            const val DENSITY_MEDIUM: Short = 160
            const val DENSITY_TV: Short = 213
            const val DENSITY_HIGH: Short = 240
            const val DENSITY_XHIGH: Short = 320
            const val DENSITY_XXHIGH: Short = 480
            const val DENSITY_XXXHIGH: Short = 640
            const val DENSITY_ANY = 0xfffe.toShort()
            const val DENSITY_NONE = 0xffff.toShort()

            const val KEYBOARD_ANY = 0x0000
            const val KEYBOARD_NOKEYS = 0x0001
            const val KEYBOARD_QWERTY = 0x0002
            const val KEYBOARD_12KEY = 0x0003

            const val NAVIGATION_ANY = 0x0000
            const val NAVIGATION_NONAV = 0x0001
            const val NAVIGATION_DPAD = 0x0002
            const val NAVIGATION_TRACKBALL = 0x0003
            const val NAVIGATION_WHEEL = 0x0004

            const val KEYSHIDDEN_ANY = 0x0000
            const val KEYSHIDDEN_NO = 0x0001
            const val KEYSHIDDEN_YES = 0x0002
            const val KEYSHIDDEN_SOFT = 0x0003

            const val NAVHIDDEN_ANY = 0x0000
            const val NAVHIDDEN_NO = 0x0001
            const val NAVHIDDEN_YES = 0x0002

            const val SCREENWIDTH_ANY = 0x0000

            const val SCREENHEIGHT_ANY = 0x0000

            const val SDKVERSION_ANY = 0x0000

            const val MINORVERSION_ANY = 0x0000

            const val SCREENSIZE_ANY = 0x00
            const val SCREENSIZE_SMALL = 0x01
            const val SCREENSIZE_NORMAL = 0x02
            const val SCREENSIZE_LARGE = 0x03
            const val SCREENSIZE_XLARGE = 0x04
        }
    }

    data class Input(
            var keyboard: Byte = 0,
            var navigation: Byte = 0,
            var flags: Byte = 0,
            var pad0: Byte = 0
    )

    data class ScreenSize(
            var width: Short = 0,
            var height: Short = 0
    )

    data class ScreenConfig(
            /**
             * @see SCREEN_LAYOUT_DIR_UNDEFINED
             * @see SCREEN_LAYOUT_DIR_LTR
             * @see SCREEN_LAYOUT_DIR_RTL
             */
            var layout: Byte = 0,
            var uiMode: Byte = 0,
            var smallestWidthDp: Short = 0
    ) {
        companion object {
            const val SCREEN_LAYOUT_DIR_UNDEFINED: Byte = 0
            const val SCREEN_LAYOUT_DIR_LTR: Byte = 1
            const val SCREEN_LAYOUT_DIR_RTL: Byte = 2
        }
    }

    data class ScreenConfig2(
            /**
             * Contains round/notround qualifier
             */
            var layout: Byte = 0,
            /**
             * Wide-gamut, HDR, etc.
             */
            var colorMode: Byte = 0,
            /**
             * Reserved padding
             */
            var pad2: Short = 0
    )

    data class Version(
            var sdk: Short = 0,
            var minor: Short = 0 // always 0
    )

}
