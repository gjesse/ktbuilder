package com.loshodges.ktbuilder

import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement

class Processor : AbstractProcessor() {
    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(KtBuilder::class.java)
                .forEach {
                    val className = it.simpleName.toString()
                    println("Processing: $className")
                    val pack = processingEnv.elementUtils.getPackageOf(it).toString()
                    generateClass(it, pack)
                }
        return true
    }

    private fun generateClass(element: Element, pack: String) {

        val className = element.simpleName.toString()
        println("class $className pack $pack")
        val fileName = "${className}Builder"


        val fields = element.enclosedElements
                .filter { it.kind == ElementKind.FIELD }

        val builderClass = ClassName(pack, fileName)


        val constructorString = fields.map {
            if (!it.javaToKotlinType().nullable) {
                "${it.simpleName} = this.${it.simpleName}.nonNull(\"${it.simpleName}\")"
            } else {
                "${it.simpleName} = this.${it.simpleName}"
            }
        }.joinToString(", \n\t")

        val generatedClass = TypeSpec.classBuilder(fileName)
                .addFunction(FunSpec.builder("build")
                        .returns(element.asType().asTypeName())
                        .addStatement("return $className($constructorString)")
                        .build()
                )
                .addFunction(nonNullFnSpec())

        fields
                .map { field ->

                    val typeName = field.javaToKotlinType()
                    FunSpec.builder("with${field.simpleName.toString().capitalize()}")
                            .returns(builderClass)
                            .addParameter(ParameterSpec.builder(field.simpleName.toString(), typeName).build())
                            .addStatement("this.${field.simpleName} = ${field.simpleName}")
                            .addStatement("return this")
                            .build()

                }.forEach {
                    generatedClass.addFunction(it)
                }


        fields.forEach {

            // properties will all be null at first
            val typeName = it.javaToKotlinType().asNullable()
            val propertySpec =
                    PropertySpec
                            .builder(it.simpleName.toString(), typeName)
                            .addModifiers(KModifier.PRIVATE)
                            .initializer("null")
                            .mutable(true)


            generatedClass
                    .addProperty(propertySpec.build())
        }

        val file = FileSpec.builder(pack, fileName)
                .addType(generatedClass.build()).build()

        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        file.writeTo(File(kaptKotlinGeneratedDir, "$fileName.kt"))
        println("wrote some files: $kaptKotlinGeneratedDir / $fileName.kt")
    }

    private fun nonNullFnSpec(): FunSpec {
        val genericTypeName = TypeVariableName("T")
        return FunSpec.builder("nonNull")
                .receiver(genericTypeName.asNullable())
                .addParameter("fieldName", String::class.asTypeName())
                .returns(genericTypeName.asNonNullable())
                .addTypeVariable(genericTypeName)
                .addModifiers(KModifier.PRIVATE)
                .addStatement("return this ?: throw NullPointerException(\"\${fieldName} can not be null\")")
                .build()
    }


    override fun getSupportedAnnotationTypes() = mutableSetOf(KtBuilder::class.java.name)
    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}