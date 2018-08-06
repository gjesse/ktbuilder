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

        println("x process")

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


        fields.forEach {
            el -> println("el: ${el.kind} ${el.modifiers} ${el.asType().asTypeName()} ${el.simpleName}")
        }


        val builderClass = ClassName(pack, fileName)


        val constring = fields.map { "${it.simpleName} = this.${it.simpleName}" }
                .joinToString(", ")

        // todo kotlin strings vs java strings
        val generatedClass = TypeSpec.classBuilder(fileName)
                .addFunction(FunSpec.builder("build")
                        .returns(element.asType().asTypeName())
                        .addStatement("return $className($constring)")
                        .build())

        fields
                .map {
                    field ->
                    FunSpec.builder("with${field.simpleName}")
                            .returns(builderClass)
                            .addParameter(ParameterSpec.builder(field.simpleName.toString(), field.asType().asTypeName()).build())
                            .addStatement("this.${field.simpleName} = ${field.simpleName}")
                            .addStatement("return this")
                            .build()

                }.forEach {
                    generatedClass.addFunction(it)
                }


        fields.forEach {

            val typeName = if (it.asType().asTypeName().toString() == "java.lang.String") {
                    String::class.asTypeName().asNullable()
            } else {
                it.asType().asTypeName()
            }
            val propertySpec =
                PropertySpec
                        .builder(it.simpleName.toString(), typeName)
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


    override fun getSupportedAnnotationTypes() = mutableSetOf(KtBuilder::class.java.name)
    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}