package com.didiglobal.booster.transform.sharedpreferences

import com.didiglobal.booster.kotlinx.asIterable
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.transform.ArtifactManager
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.shared.preferences.Build
import com.didiglobal.booster.transform.util.ComponentHandler
import com.google.auto.service.AutoService
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Opcodes.INVOKEVIRTUAL
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import java.io.PrintWriter
import javax.xml.parsers.SAXParserFactory

/**
 * Represents a transformer for *SharedPreferences* optimization
 *
 * @author neighbWang
 */
@AutoService(ClassTransformer::class)
class SharedPreferencesTransformer : ClassTransformer {

    private lateinit var logger: PrintWriter
    private val applications = mutableSetOf<String>()

    override val name: String = Build.ARTIFACT

    override fun onPreTransform(context: TransformContext) {
        val parser = SAXParserFactory.newInstance().newSAXParser()
        context.artifacts.get(ArtifactManager.MERGED_MANIFESTS).forEach { manifest ->
            val handler = ComponentHandler()
            parser.parse(manifest, handler)
            applications.addAll(handler.applications)
        }
        logger = getReport(context, "report.txt").touch().printWriter()
    }

    override fun onPostTransform(context: TransformContext) {
        logger.close()
    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (klass.name.startsWith(BOOSTER_DIRECTORY_PREFIX)
                || klass.name.startsWith(SUPPORT_MULTIDEX_PACKAGE_PREFIX)
                || klass.name.startsWith(JETPACK_MULTIDEX_PACKAGE_PREFIX)) {
            return klass
        }

        klass.methods.forEach { method ->
            method.instructions.iterator().asIterable().filter {
                it.opcode == INVOKEVIRTUAL || it.opcode == INVOKESTATIC
            }.map {
                it as MethodInsnNode
            }.forEach {
                mapOf(
                        CONTEXT_GET_SHARED_PREFERENCES to SHADOW_CONTEXT_GET_SHARED_PREFERENCES,
                        ACTIVITY_GET_PREFERENCES to SHADOW_ACTIVITY_GET_PREFERENCES,
                        PREFERENCE_MANAGER_GET_DEFAULT_SHARED_PREFERENCES to SHADOW_PREFERENCE_MANAGER_GET_DEFAULT_SHARED_PREFERENCES
                ).forEach { (original, shadow) ->
                    if (it.opcode == original.opcode && it.name == original.name && it.desc == original.desc && context.klassPool.get(original.owner).isAssignableFrom(it.owner)) {
                        logger.println(" * ${shadow.owner}${shadow.name}${shadow.desc} => ${it.owner}.${it.name}${it.desc}: ${klass.name}.${method.name}${method.desc}")
                        it.opcode = shadow.opcode
                        it.owner = shadow.owner
                        it.name = shadow.name
                        it.desc = shadow.desc
                    }
                }
            }
        }
        return klass
    }
}

private const val ACTIVITY = "android/app/Activity"
private const val CONTEXT = "android/content/Context"
private const val PREFERENCE_MANAGER = "android.preference/PreferenceManager"
private const val SHARED_PREFERENCES = "android/content/SharedPreferences"
private const val SUPPORT_MULTIDEX_PACKAGE_PREFIX = "android/support/multidex/"
private const val JETPACK_MULTIDEX_PACKAGE_PREFIX = "androidx/multidex/"
private const val BOOSTER_DIRECTORY_PREFIX = "com/didiglobal/booster/instrument"
private const val SHADOW_SHARED_PREFERENCES = "$BOOSTER_DIRECTORY_PREFIX/ShadowSharedPreferences"

private val CONTEXT_GET_SHARED_PREFERENCES = MethodInsnNode(INVOKEVIRTUAL, CONTEXT, "getSharedPreferences", "(Ljava/lang/String;I)L$SHARED_PREFERENCES;")
private val SHADOW_CONTEXT_GET_SHARED_PREFERENCES = MethodInsnNode(INVOKESTATIC, SHADOW_SHARED_PREFERENCES, "getSharedPreferences", "(L$CONTEXT;Ljava/lang/String;I)L$SHARED_PREFERENCES;")

private val PREFERENCE_MANAGER_GET_DEFAULT_SHARED_PREFERENCES = MethodInsnNode(INVOKESTATIC, PREFERENCE_MANAGER, "getDefaultSharedPreferences", "(L$CONTEXT;)L$SHARED_PREFERENCES;")
private val SHADOW_PREFERENCE_MANAGER_GET_DEFAULT_SHARED_PREFERENCES = MethodInsnNode(INVOKESTATIC, SHADOW_SHARED_PREFERENCES, "getDefaultSharedPreferences", "(L$CONTEXT;)L$SHARED_PREFERENCES;")

private val ACTIVITY_GET_PREFERENCES = MethodInsnNode(INVOKEVIRTUAL, ACTIVITY, "getPreferences", "(I)L$SHARED_PREFERENCES;")
private val SHADOW_ACTIVITY_GET_PREFERENCES = MethodInsnNode(INVOKESTATIC, SHADOW_SHARED_PREFERENCES, "getPreferences", "(L$ACTIVITY;I)L$SHARED_PREFERENCES;")
