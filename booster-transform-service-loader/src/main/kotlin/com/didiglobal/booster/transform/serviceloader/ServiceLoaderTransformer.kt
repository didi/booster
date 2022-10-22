package com.didiglobal.booster.transform.serviceloader

import com.didiglobal.booster.kotlinx.search
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.asm.asIterable
import com.didiglobal.booster.transform.service.loader.Build
import com.google.auto.service.AutoService
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.tree.VarInsnNode
import java.io.File
import java.io.PrintWriter
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

@AutoService(ClassTransformer::class)
class ServiceLoaderTransformer : ClassTransformer {

    private lateinit var logger: PrintWriter
    private lateinit var services: Map<String, Set<String>>

    override val name: String = Build.ARTIFACT

    override fun onPreTransform(context: TransformContext) {
        this.services = context.compileClasspath.map(File::services).flatten().toMap()
        this.logger = getReport(context, "report.txt").touch().printWriter()
    }

    override fun onPostTransform(context: TransformContext) {
        this.logger.close()
    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (services.isNotEmpty()) {
            klass.methods.forEach { method ->
                method.instructions.asIterable()
                        .filterIsInstance<MethodInsnNode>()
                        .filter(::isServiceLoaderLoad)
                        .forEach {
                            when (it.desc) {
                                // java/util/ServiceLoader.load:(Ljava/lang/Class;)Ljava/util/ServiceLoader;
                                "(L${JAVA_LANG_CLASS};)L${JAVA_UTIL_SERVICE_LOADER};" -> transformServiceLoader(method, it, false)
                                // java/util/ServiceLoader.load:(Ljava/lang/Class;Ljava/lang/ClassLoader;)Ljava/util/ServiceLoader;
                                "(L${JAVA_LANG_CLASS};L${JAVA_LANG_CLASS_LOADER};)L${JAVA_UTIL_SERVICE_LOADER};" -> transformServiceLoader(method, it, true)
                            }
                        }
            }
        }
        return klass
    }

    /**
     * ServiceLoader.load(Service.class, ...).iterator() => Arrays.asList(new Service[] { new A(), new B(), new C(), ...}).iterator()
     */
    private fun transformServiceLoader(method: MethodNode, invoke: MethodInsnNode, hasClassLoader: Boolean) {
        val service = ((invoke.previous as? LdcInsnNode)?.cst as? Type)?.className ?: return
        val implementations = services[service]?.takeIf(Collection<String>::isNotEmpty)?.map {
            it.replace('.', '/')
        } ?: return

        method.instructions.apply {
            // consume the arguments of ServiceLoader.load(...)
            insertBefore(invoke, InsnNode(if (hasClassLoader) Opcodes.POP2 else Opcodes.POP))
            insertBefore(invoke, InsnList().apply {
                // array length
                add(implementations.size.insn)
                // new Service[] { ... }
                add(TypeInsnNode(Opcodes.ANEWARRAY, service.replace('.', '/')))
                implementations.withIndex().forEach { (index, implementation) ->
                    /* dup             */ add(InsnNode(Opcodes.DUP))
                    /* iconst_x/bipush */ add(index.insn)
                    /* new             */ add(TypeInsnNode(Opcodes.NEW, implementation))
                    /* dup             */ add(InsnNode(Opcodes.DUP))
                    /* invokespecial   */ add(MethodInsnNode(Opcodes.INVOKESPECIAL, implementation, "<init>", "()V"))
                    /* aastore         */ add(InsnNode(Opcodes.AASTORE))
                }
                add(MethodInsnNode(Opcodes.INVOKESTATIC, JAVA_UTIL_ARRAYS, "asList", "([L${JAVA_LANG_OBJECT};)L${JAVA_UTIL_LIST};"))
            })
            (invoke.next as MethodInsnNode).apply {
                opcode = Opcodes.INVOKEINTERFACE
                owner = JAVA_UTIL_LIST
                itf = true
            }
            remove(invoke)
        }
        logger.println("* ${JAVA_UTIL_SERVICE_LOADER}.load(L${JAVA_LANG_CLASS};${
        if (hasClassLoader) "L${JAVA_LANG_CLASS_LOADER};" else ""
        }) => ${JAVA_UTIL_ARRAYS}.asList(new ${service}[] {${implementations.joinToString(", ") {
            "new ${it}()"
        }})L${JAVA_UTIL_LIST};")
    }


    /**
     * ldc           #? // Class  ...
     * invokestatic  #? // Method java/util/ServiceLoader.load:(Ljava/lang/Class;)Ljava/util/ServiceLoader;
     * invokevirtual #? // Method java/util/ServiceLoader.iterator:()Ljava/util/Iterator;
     */
    private fun isServiceLoaderLoad(invoke: MethodInsnNode): Boolean {
        val prev = (invoke.previous as? LdcInsnNode) ?: return false
        val next = invoke.next as? MethodInsnNode ?: return false
        return invoke.opcode == Opcodes.INVOKESTATIC
                && invoke.owner == JAVA_UTIL_SERVICE_LOADER
                && invoke.name == "load"
                && invoke.desc == "(L${JAVA_LANG_CLASS};)L${JAVA_UTIL_SERVICE_LOADER};"
                && (prev.cst as? Type)?.className in services.keys
                && next.owner == JAVA_UTIL_SERVICE_LOADER
                && next.name == "iterator"
                && next.desc == "()L${JAVA_UTIL_ITERATOR};"
    }

}

private val Int.insn: AbstractInsnNode
    get() = when (this) {
        0 -> InsnNode(Opcodes.ICONST_0)
        1 -> InsnNode(Opcodes.ICONST_1)
        2 -> InsnNode(Opcodes.ICONST_2)
        3 -> InsnNode(Opcodes.ICONST_3)
        4 -> InsnNode(Opcodes.ICONST_4)
        5 -> InsnNode(Opcodes.ICONST_5)
        else -> VarInsnNode(Opcodes.BIPUSH, this)
    }

private val List<String>.services: Set<String>
    get() = filter { line ->
        line.startsWith("#") || line.isNotBlank()
    }.map(String::trim).toSet()

private val File.services: List<Pair<String, Set<String>>>
    get() = when {
        isDirectory -> search {
            it.isFile && it.parentFile?.name == "services" && it.parentFile?.parentFile?.name == "META-INF"
        }.map { spi ->
            spi.name to spi.readLines().services
        }
        isFile -> when (extension.lowercase()) {
            "jar" -> ZipFile(this).use { zip ->
                zip.entries()
                        .asSequence()
                        .toList()
                        .filterNot(ZipEntry::isDirectory)
                        .filter {
                            it.name.startsWith(SPI_PREFIX) && it.name.length > SPI_PREFIX.length
                        }.map {
                            it.name.substringAfter(SPI_PREFIX) to zip.getInputStream(it)
                                    .bufferedReader().readLines().services
                        }
            }.toList()
            else -> emptyList()
        }
        else -> emptyList()
    }

private const val DIR_META_INF = "META-INF"
private const val DIR_SERVICES = "services"
private const val SPI_PREFIX = "${DIR_META_INF}/${DIR_SERVICES}/"

private const val JAVA_LANG_CLASS = "java/lang/Class"
private const val JAVA_LANG_OBJECT = "java/lang/Object"
private const val JAVA_LANG_CLASS_LOADER = "java/lang/ClassLoader"
private const val JAVA_UTIL_ARRAYS = "java/util/Arrays"
private const val JAVA_UTIL_ITERATOR = "java/util/Iterator"
private const val JAVA_UTIL_LIST = "java/util/List"
private const val JAVA_UTIL_SERVICE_LOADER = "java/util/ServiceLoader"
