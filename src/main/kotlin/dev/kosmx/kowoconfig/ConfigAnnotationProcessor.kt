package dev.kosmx.kowoconfig

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.annotation.Config
import io.wispforest.owo.config.annotation.Hook
import io.wispforest.owo.config.annotation.Nest

class ConfigAnnotationProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {

    companion object {
        private const val OWO_CONFIG = "io.wispforest.owo.config"
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val (toProcess, delayed) = resolver.getSymbolsWithAnnotation(Config::class.qualifiedName!!)
            .mapNotNull { it as? KSClassDeclaration }
            .partition { it.validate() }

        environment.logger.info("Processing ${toProcess.size} annotations.")
        environment.logger.info("Skipping ${delayed.size} annotations.")

        for (kClass in toProcess) {
            KConfigGenerator(kClass)()
        }

        return delayed
    }

    private inner class KConfigGenerator(private val kClass: KSClassDeclaration) {

        private val logger get() = environment.logger
        private val currentPackage = kClass.qualifiedName!!.getQualifier()

        operator fun invoke() {
            val className = kClass.qualifiedName!!
            val config = kClass.getAnnotationsByType<Config>().first()

            val (fields, nested) = collectFields(kClass, config.defaultHook)

            val sourceShortName = className.getShortName()
            val wrapperName = config.wrapperName

            val optionClass = ClassName(OWO_CONFIG, "Option")
            val optionKeyClass = optionClass.nestedClass("Key")
            val configWrapper = ClassName(OWO_CONFIG, "ConfigWrapper")
                .parameterizedBy(ClassName(currentPackage, sourceShortName))
            val builderConsumer = ClassName(OWO_CONFIG, "ConfigWrapper").nestedClass("BuilderConsumer")

            val wrapper = TypeSpec.classBuilder(wrapperName)
                .addModifiers(KModifier.PUBLIC) // keep 'public'
                .addAnnotation(
                    AnnotationSpec.builder(Suppress::class)
                        .addMember("%S, %S", "RedundantVisibilityModifier", "RemoveRedundantQualifierName")
                        .build()
                )
                .addOriginatingKSFile(kClass.containingFile!!)
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addModifiers(KModifier.PRIVATE)
                        .addParameter(
                            ParameterSpec.builder("builder", builderConsumer)
                                .defaultValue("%T {}", builderConsumer)
                                .build()
                        )
                        .build()
                )
                .superclass(configWrapper)
                .addSuperclassConstructorParameter(
                    "%T::class.java, builder",
                    ClassName(currentPackage, sourceShortName)
                )
                .addProperty(
                    PropertySpec.builder("parentKey", optionKeyClass, KModifier.PRIVATE)
                        .initializer("%T.ROOT", optionKeyClass)
                        .build()
                )
                .addType(
                    TypeSpec.companionObjectBuilder()
                        .addFunction(
                            FunSpec.builder("createAndLoad")
                                .returns(ClassName(currentPackage, wrapperName))
                                .addParameter(
                                    ParameterSpec.builder("builder", builderConsumer)
                                        .defaultValue("%T {}", builderConsumer)
                                        .build()
                                )
                                .addStatement("return %L(builder).apply { load() }", wrapperName)
                                .build()
                        )
                        .build()
                )
                .apply {
                    val ctx = EmitCtx(
                        packageName = currentPackage,
                        wrapperName = wrapperName,
                        optionClass = optionClass,
                        optionKeyClass = optionKeyClass,
                        parentKeyExpr = CodeBlock.of("parentKey")
                    )
                    fields.forEach { it.emit(this, ctx) }
                    nested.forEach { addType(it.toTypeSpec(ctx)) }
                }
                .build()

            // private operator fun <T> Option<T>.getValue(...) / setValue(...)
            val typeVarT = TypeVariableName("T")
            val kPropertyStar = ClassName("kotlin.reflect", "KProperty").parameterizedBy(STAR)
            val suppressUnused = AnnotationSpec.builder(Suppress::class)
                .addMember("%S", "unused")
                .build()

            val getValue = FunSpec.builder("getValue")
                .addAnnotation(suppressUnused)
                .addModifiers(KModifier.PRIVATE, KModifier.OPERATOR)
                .receiver(optionClass.parameterizedBy(typeVarT))
                .addTypeVariable(typeVarT)
                .addParameter("hisRef", ANY.copy(nullable = true))
                .addParameter("property", kPropertyStar)
                .returns(typeVarT)
                .addStatement("return this.value()")
                .build()

            val setValue = FunSpec.builder("setValue")
                .addAnnotation(suppressUnused)
                .addModifiers(KModifier.PRIVATE, KModifier.OPERATOR)
                .receiver(optionClass.parameterizedBy(typeVarT))
                .addTypeVariable(typeVarT)
                .addParameter("hisRef", ANY.copy(nullable = true))
                .addParameter("property", kPropertyStar)
                .addParameter("value", typeVarT)
                .returns(UNIT)
                .addStatement("this.set(value)")
                .build()

            FileSpec.builder(currentPackage, wrapperName)
                .addType(wrapper)
                .addFunction(getValue)
                .addFunction(setValue)
                .build()
                .writeTo(environment.codeGenerator, Dependencies(true, kClass.containingFile!!))
        }

        private fun collectFields(
            clazz: KSClassDeclaration,
            defaultHook: Boolean
        ): Pair<List<ConfigField>, Set<NestedClass>> {
            val fields = mutableListOf<ConfigField>()
            val nested = mutableSetOf<NestedClass>()

            for (field in clazz.getAllProperties()) {
                if (!field.hasBackingField) {
                    logger.warn("Property does not have backing field, not generating config", field)
                    continue
                }

                val fieldType = field.type.resolve()
                val fieldName = field.simpleName.asString()

                if (fieldType.declaration is KSTypeParameter) {
                    logger.error("Generic field types are not allowed in config classes", field)
                }

                val typeElement: KSClassDeclaration? =
                    (fieldType.declaration as? KSClassDeclaration)?.let {
                        it.takeIf { it !== clazz } ?: run {
                            logger.error("Illegal self-reference in nested config object", field)
                            null
                        }
                    }

                if (typeElement != null && field.hasAnnotation<Nest>()) {
                    val (subProps, subClasses) =
                        collectFields(typeElement, defaultHook || field.hasAnnotation<Hook>())
                    nested += subClasses
                    nested += NestedClass(typeElement.simpleName, subProps)
                    fields += NestField(fieldName, Option.Key(fieldName), typeElement.simpleName.asString())
                } else {
                    fields += ValueField(
                        fieldName,
                        Option.Key(fieldName),
                        field,
                        defaultHook || field.hasAnnotation<Hook>()
                    )
                }
            }
            return fields to nested
        }
    }
}

/* ---------- KotlinPoet-backed model ---------- */

private data class EmitCtx(
    val packageName: String,
    val wrapperName: String,
    val optionClass: ClassName,
    val optionKeyClass: ClassName,
    val parentKeyExpr: CodeBlock
)

private sealed class ConfigField {
    abstract fun emit(into: TypeSpec.Builder, ctx: EmitCtx)
}

private class NestField(
    private val name: String,
    private val key: Option.Key,
    private val className: String
) : ConfigField() {
    override fun emit(into: TypeSpec.Builder, ctx: EmitCtx) {
        val nestedType = ClassName(ctx.packageName, ctx.wrapperName, className)
        into.addProperty(
            PropertySpec.builder(name, nestedType)
                .initializer("%T(%L.child(%S))", nestedType, ctx.parentKeyExpr, key.asString())
                .build()
        )
    }
}

private class ValueField(
    private val fieldName: String,
    private val key: Option.Key,
    private val field: KSPropertyDeclaration,
    private val makeSubscribe: Boolean
) : ConfigField() {

    override fun emit(into: TypeSpec.Builder, ctx: EmitCtx) {
        val typeName = field.type.resolve().toTypeName()

        into.addProperty(
            PropertySpec.builder(fieldName, typeName)
                .mutable(field.isMutable)
                .delegate(CodeBlock.of("optionForKey(%L.child(%S))!!", ctx.parentKeyExpr, key.asString()))
                .build()
        )

        if (makeSubscribe) {
            val lambda = LambdaTypeName.get(parameters = arrayOf(typeName), returnType = UNIT)
            into.addFunction(
                FunSpec.builder("subscribeTo${fieldName.replaceFirstChar { it.uppercase() }}")
                    .addParameter("subscriber", lambda)
                    .addStatement(
                        "optionForKey<%T>(%T.Key(%S))!!.observe(subscriber)",
                        typeName,
                        ctx.optionClass,
                        key.asString()
                    )
                    .build()
            )
        }
    }
}

private class NestedClass(
    private val name: KSName,
    private val props: List<ConfigField>
) {
    fun toTypeSpec(rootCtx: EmitCtx): TypeSpec {
        val builder = TypeSpec.classBuilder(name.asString())
            .addModifiers(KModifier.INNER)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("parentKey", rootCtx.optionKeyClass)
                    .build()
            )

        val nestedCtx = rootCtx.copy(parentKeyExpr = CodeBlock.of("parentKey"))
        props.forEach { it.emit(builder, nestedCtx) }
        return builder.build()
    }

    override fun equals(other: Any?) = other is NestedClass && name == other.name
    override fun hashCode(): Int = name.hashCode()
}

@OptIn(KspExperimental::class)
private inline fun <reified T : Annotation> KSAnnotated.getAnnotationsByType() =
    getAnnotationsByType(T::class)

private inline fun <reified T : Annotation> KSAnnotated.hasAnnotation() =
    getAnnotationsByType<T>().any()
