package com.loshodges.ktbuilder

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import org.jetbrains.annotations.Nullable
import javax.lang.model.element.Element
import kotlin.reflect.jvm.internal.impl.name.FqName
import kotlin.reflect.jvm.internal.impl.platform.JavaToKotlinClassMap


fun Element.javaToKotlinType(): TypeName {
    val annotation = this.getAnnotation(Nullable::class.java)
    val typeName = asType().asTypeName().javaToKotlinType()
    return if (annotation != null) typeName.asNullable() else typeName
}

fun TypeName.javaToKotlinType(): TypeName {
    return if (this is ParameterizedTypeName) {
        ParameterizedTypeName.get(
                rawType.javaToKotlinType() as ClassName,
                *typeArguments.map { it.javaToKotlinType() }.toTypedArray()
        )
    } else {
        val className =
                JavaToKotlinClassMap.INSTANCE.mapJavaToKotlin(FqName(toString()))
                        ?.asSingleFqName()?.asString()

        return if (className == null) {
            this
        } else {
            ClassName.bestGuess(className)
        }
    }
}