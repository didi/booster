package com.didiglobal.booster.aapt

/**
 * @author johnsonlee
 * @see https://android.googlesource.com/platform/frameworks/base/+/master/libs/androidfw/include/androidfw/ResourceTypes.h
 */
class Configuration {

    var size: Int = 0

    val imsi = Imsi()

    val locale = Locale()

    val screenType = ScreenType()

    val input = Input()

    val screenSize = ScreenSize()

    val version = Version()

    val screenConfig = ScreenConfig()

    val screenSizeDp = ScreenSize()

    /**
     * The ISO-15924 short name for the script corresponding to this
     * configuration. (eg. Hant, Latn, etc.). Interpreted in conjunction
     * with the locale field.
     */
    val localeScript = ByteArray(4)

    /**
     * A single BCP-47 variant subtag. Will vary in length between 4 and 8
     * chars. Interpreted in conjunction with the locale field.
     */
    val localeVariant = ByteArray(8)

    val screenConfig2 = ScreenConfig2()

    class Imsi {

        /**
         * Mobile country code (from SIM). 0 means "any"
         */
        var mcc: Short = 0

        /**
         * Mobile network code (from SIM). 0 means "any".
         */
        var mnc: Short = 0
    }

    class Locale {

        /**
         * This field can take three different forms:
         *
         *
         *  * \0\0 means "any".
         *  * Two 7 bit ascii values interpreted as ISO-639-1 language
         * codes ('fr', 'en' etc. etc.). The high bit for both bytes is
         * zero.
         *  * A single 16 bit little endian packed value representing an
         * ISO-639-2 3 letter language code. This will be of the form:
         *
         * <pre>
         * { 1, t, t, t, t, t, s, s, s, s, s, f, f, f, f, f }
        </pre> *
         *
         * bit[0, 4] = first letter of the language code<br></br>
         * bit[5, 9] = second letter of the language code<br></br>
         * bit[10, 14] = third letter of the language code.<br></br>
         * bit[15] = 1 always<br></br>
         *
         *
         *
         * For backwards compatibility, languages that have unambiguous two
         * letter codes are represented in that format.
         *
         * The layout is always bigendian irrespective of the runtime
         * architecture.
         */
        val language = ByteArray(2)

        /**
         * This field can take three different forms:
         *
         *
         *  * \0\0 means "any".
         *  * Two 7 bit ascii values interpreted as 2 letter region codes
         * ('US', 'GB' etc.). The high bit for both bytes is zero.
         *  * An UN M.49 3 digit region code. For simplicity, these are
         * packed in the same manner as the language codes, though we should
         * need only 10 bits to represent them, instead of the 15.
         *
         *
         * he layout is always bigendian irrespective of the runtime
         * architecture.
         */
        val country = ByteArray(2)

        val isDefined: Boolean
            get() = this.language[0].toInt() == 0 && this.language[1].toInt() == 0 && this.country[0].toInt() == 0 && this.country[1].toInt() == 0
    }

    class ScreenType {

        var orientation: Byte = 0
        var touchscreen: Byte = 0
        var density: Short = 0

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

    class Input {
        var keyboard: Byte = 0
        var navigation: Byte = 0
        var flags: Byte = 0
        var pad0: Byte = 0
    }

    class ScreenSize {
        var width: Short = 0
        var height: Short = 0
    }

    class ScreenConfig {
        var layout: Byte = 0
        var uiMode: Byte = 0
        var smallestWidthDp: Short = 0
    }

    class ScreenConfig2 {
        /**
         * Contains round/notround qualifier
         */
        var layout: Byte = 0
        /**
         * Wide-gamut, HDR, etc.
         */
        var colorMode: Byte = 0
        /**
         * Reserved padding
         */
        var pad2: Short = 0
    }

    class Version {
        var sdk: Short = 0
        var minor: Short = 0 // always 0
    }

    fun unpackLanguageOrRegion(data: ByteArray, base: Byte): ByteArray {
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

    fun unpackLanguage(): ByteArray {
        return unpackLanguageOrRegion(this.locale.language, 0x61.toByte())
    }

    fun unpackRegion(): ByteArray {
        return unpackLanguageOrRegion(this.locale.country, 0x30.toByte())
    }

    fun appendLocaleDir(out: StringBuilder) {
        if (0 != this.locale.language[0].toInt()) {
            return
        }

        if (0 == this.localeScript[0].toInt() && 0 == this.localeVariant[0].toInt()) {
            if (out.isNotEmpty()) {
                out.append("-")
            }

            out.append(String(unpackLanguage()))

            if (0 != this.locale.country[0].toInt()) {
                out.append("-r")
                out.append(String(unpackRegion()))
            }

            return
        }

        if (out.isNotEmpty()) {
            out.append("-")
        }

        out.append("b+")
        out.append(String(unpackLanguage()))

        if (0 != this.localeScript[0].toInt()) {
            out.append("+")
            out.append(String(this.localeScript))
        }

        if (0 != this.locale.country[0].toInt()) {
            out.append("+")
            out.append(String(unpackRegion()))
        }

        if (0 != this.localeVariant[0].toInt()) {
            out.append("+")
            out.append(String(this.localeVariant))
        }
    }

    override fun toString(): String {
        val res = StringBuilder()

        if (this.imsi.mcc.toInt() != 0) {
            if (res.isNotEmpty()) {
                res.append("-")
            }
            res.append(String.format("mcc%d", this.imsi.mcc))
        }

        if (this.imsi.mnc.toInt() != 0) {
            if (res.isNotEmpty()) {
                res.append("-")
            }
            res.append(String.format("mnc%d", this.imsi.mnc))
        }

        appendLocaleDir(res)

        if (this.screenConfig.smallestWidthDp.toInt() != 0) {
            if (res.isNotEmpty()) {
                res.append("-")
            }
            res.append(String.format("sw%ddp", this.screenConfig.smallestWidthDp))
        }

        if (this.screenSizeDp.width.toInt() != 0) {
            if (res.isNotEmpty()) {
                res.append("-")
            }
            res.append(String.format("w%ddp", this.screenSizeDp.width))
        }

        if (this.screenSizeDp.height.toInt() != 0) {
            if (res.isNotEmpty()) {
                res.append("-")
            }
            res.append(String.format("h%ddp", this.screenSizeDp.width))
        }

        if (this.screenType.orientation.toInt() != ScreenType.ORIENTATION_ANY) {
            if (res.isNotEmpty()) {
                res.append("-")
            }

            when (this.screenType.orientation.toInt()) {
                ScreenType.ORIENTATION_PORT -> res.append("port")
                ScreenType.ORIENTATION_LAND -> res.append("land")
                ScreenType.ORIENTATION_SQUARE -> res.append("square")
                else -> res.append(String.format("orientation=%d", this.screenType.orientation))
            }
        }

        if (this.screenType.density != ScreenType.DENSITY_DEFAULT) {
            if (res.isNotEmpty()) {
                res.append("-")
            }

            val density = this.screenType.density

            when (density) {
                ScreenType.DENSITY_LOW -> res.append("ldpi")
                ScreenType.DENSITY_MEDIUM -> res.append("mdpi")
                ScreenType.DENSITY_TV -> res.append("tvdpi")
                ScreenType.DENSITY_HIGH -> res.append("hdpi")
                ScreenType.DENSITY_XHIGH -> res.append("xhdpi")
                ScreenType.DENSITY_XXHIGH -> res.append("xxhdpi")
                ScreenType.DENSITY_XXXHIGH -> res.append("xxxhdpi")
                ScreenType.DENSITY_NONE -> res.append("nodpi")
                ScreenType.DENSITY_ANY -> res.append("anydpi")
                else -> res.append(String.format("%ddpi", density))
            }
        }

        if (this.screenSize.width.toInt() != 0 || this.screenSize.height.toInt() != 0) {
            if (res.isNotEmpty()) {
                res.append("-")
            }
            res.append(String.format("%dx%d", this.screenSize.width, this.screenSize.height))
        }

        if (this.version.sdk.toInt() != 0 || this.version.minor.toInt() != 0) {
            if (res.isNotEmpty()) {
                res.append("-")
            }
            res.append(String.format("v%d", this.version.sdk))
            if (this.version.minor.toInt() != 0) {
                res.append(String.format(".%d", this.version.minor))
            }
        }

        return if (res.isEmpty()) "" else res.toString()
    }
}
