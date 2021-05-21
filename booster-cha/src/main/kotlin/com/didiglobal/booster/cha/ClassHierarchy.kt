package com.didiglobal.booster.cha

import java.util.Collections
import java.util.Objects
import java.util.concurrent.ConcurrentHashMap

/**
 * @author johnsonlee
 */
@Suppress("MemberVisibilityCanBePrivate")
class ClassHierarchy<ClassFile, ClassParser : ClassFileParser<ClassFile>>(
        private val classSet: ClassSet<ClassFile, ClassParser>
) : ClassFileParser<ClassFile> by classSet.parser {

    private val unresolved = ConcurrentHashMap.newKeySet<String>()

    val unresolvedClasses: Set<String>
        get() = Collections.unmodifiableSet(unresolved)

    operator fun get(name: String?): ClassFile? {
        val clazz = name?.let { classSet[it] }
        if (null == clazz) {
            unresolved += name
        }
        return clazz
    }

    val classes: Iterable<ClassFile> = classSet

    fun isInheritFrom(child: ClassFile, parent: ClassFile) = when {
        getClassName(child) == getClassName(parent) -> true
        isInterface(parent) -> isInheritFromInterface(child, parent)
        isInterface(child) -> getClassName(parent) == JAVA_LANG_OBJECT
        isFinal(parent) -> false
        else -> isInheritFromClass(child, parent)
    }

    fun isInheritFrom(child: String, parent: String): Boolean {
        val childClass = this[child] ?: return false
        val parentClass = this[parent] ?: return false
        return isInheritFrom(childClass, parentClass)
    }

    fun isInheritFrom(child: String, parent: ClassFile) = (!isFinal(parent)) && this[child]?.let { childClass ->
        isInheritFrom(childClass, parent)
    } ?: false

    fun isInheritFrom(child: ClassFile, parent: String) = this[parent]?.let { parentClass ->
        isInheritFrom(child, parentClass)
    } ?: false

    fun isInheritFromInterface(child: ClassFile, parent: ClassFile): Boolean {
        val interfaces = getInterfaces(child)
        if (getClassName(parent) in interfaces) {
            return true
        }

        return interfaces.any { itf ->
            this[itf]?.let {
                isInheritFromInterface(it, parent)
            } ?: false
        }
    }

    fun isInheritFromClass(child: ClassFile, parent: ClassFile): Boolean {
        if (Objects.equals(getSuperName(child), getClassName(parent))) {
            return true
        }

        if (null == getSuperName(child)
                || Objects.equals(getSuperName(child), getSuperName(parent))
                || Objects.equals(getSuperName(parent), getClassName(child))) {
            return false
        }

        return this[getSuperName(child)]?.let {
            isInheritFromClass(it, parent)
        } ?: false
    }

    fun getSuperClasses(clazz: ClassFile): Set<ClassFile> {
        if (getSuperName(clazz) == null) {
            return emptySet()
        }

        if (getSuperName(clazz) == JAVA_LANG_OBJECT) {
            return setOf(this[getSuperName(clazz)]!!)
        }

        val classes = mutableSetOf<ClassFile>()
        var parent = this[getSuperName(clazz)]

        while (null != parent) {
            classes += parent
            parent = getSuperName(parent)?.let {
                this[it]
            }
        }

        return classes
    }

}

const val JAVA_LANG_OBJECT = "java/lang/Object"
