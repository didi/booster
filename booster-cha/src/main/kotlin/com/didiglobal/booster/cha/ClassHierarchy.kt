package com.didiglobal.booster.cha

import com.didiglobal.booster.cha.graph.ClassNode
import com.didiglobal.booster.graph.Graph
import java.util.Objects
import java.util.Stack

/**
 * @author johnsonlee
 */
@Suppress("MemberVisibilityCanBePrivate")
class ClassHierarchy<ClassFile : Any, ClassParser>(
        private val classSet: ClassSet<ClassFile, ClassParser>,
        private val onClassResolveFailed: OnClassResolveFailed? = null
) : ClassFileParser<ClassFile> by classSet
        where ClassParser : ClassFileParser<ClassFile> {

    /**
     * A graph that each edge is from parent type to children types
     */
    private val graph: Graph<ClassNode> by lazy {
        classSet.load().fold(Graph.Builder<ClassNode>()) { builder, clazz ->
            val className = getClassName(clazz)
            getSuperName(clazz)?.let { superName ->
                builder.addEdge(ClassNode(superName), ClassNode(className))
            }
            getInterfaces(clazz).forEach { interfaceName ->
                builder.addEdge(ClassNode(interfaceName), ClassNode(className))
            }
            builder
        }.build()
    }

    val classes: Iterable<ClassFile> = classSet

    operator fun get(name: String?): ClassFile? {
        val qn = name ?: return null
        val clazz = classSet[qn]
        if (null == clazz) {
            onClassResolveFailed?.invoke(qn)
        }
        return clazz
    }

    fun getDerivedTypes(
            name: String?,
            filter: ClassFileParser<ClassFile>.(clazz: ClassFile) -> Boolean = { true }
    ): Set<ClassFile> {
        return get(name)?.let {
            getDerivedTypes(it, filter)
        } ?: emptySet()
    }

    fun getDerivedTypes(
            clazz: ClassFile,
            filter: ClassFileParser<ClassFile>.(clazz: ClassFile) -> Boolean = { true }
    ): Set<ClassFile> {
        val node = ClassNode(getClassName(clazz))
        return graph.getSuccessors(node).filter {
            get(it.name)?.let { v -> filter(this, v) } == true
        }.mapNotNull {
            get(it.name)
        }.toSet()
    }

    fun getSuperTypes(
            name: String?,
            filter: ClassFileParser<ClassFile>.(clazz: ClassFile) -> Boolean = { true }
    ): Set<ClassFile> {
        return get(name)?.let {
           getSuperTypes(it, filter)
        } ?: emptySet()
    }

    fun getSuperTypes(
            clazz: ClassFile,
            filter: ClassFileParser<ClassFile>.(clazz: ClassFile) -> Boolean = { true }
    ): Set<ClassFile> {
        val node = ClassNode(getClassName(clazz))
        return graph.getPredecessors(node).filter {
            get(it.name)?.let { v -> filter(this, v) } == true
        }.mapNotNull {
            get(it.name)
        }.toSet()
    }

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
        val childSuperName = getSuperName(child)
        val parentName = getClassName(parent)
        if (Objects.equals(childSuperName, parentName)) {
            return true
        }

        if (null == childSuperName
                || Objects.equals(childSuperName, getSuperName(parent))
                || Objects.equals(getSuperName(parent), getClassName(child))) {
            return false
        }

        return this[childSuperName]?.let {
            isInheritFromClass(it, parent)
        } ?: false
    }

}

typealias OnClassResolveFailed = (String) -> Unit

const val JAVA_LANG_OBJECT = "java/lang/Object"
