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
        val fq = name ?: return emptySet()
        val stack = Stack<String>().apply {
            push(fq)
        }

        val children = mutableSetOf<ClassFile>()
        while (stack.isNotEmpty()) {
            children += graph[ClassNode(stack.pop())].takeIf(Set<ClassNode>::isNotEmpty)?.mapNotNull {
                get(it.name)
            }?.filter {
                filter(this, it)
            }?.onEach {
                stack.push(getClassName(it))
            } ?: emptyList()
        }
        return children
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

    fun getSuperTypes(clazz: ClassFile): Set<ClassFile> {
        val superName = getSuperName(clazz) ?: return emptySet()
        var parent = this[superName]

        if (superName == JAVA_LANG_OBJECT) {
            return parent?.let(::setOf) ?: throw ClassNotFoundException(superName)
        }

        val classes = mutableSetOf<ClassFile>()

        while (null != parent) {
            classes += parent
            parent = getSuperName(parent)?.let(this::get)
        }

        return classes
    }

}

typealias OnClassResolveFailed = (String) -> Unit

const val JAVA_LANG_OBJECT = "java/lang/Object"
