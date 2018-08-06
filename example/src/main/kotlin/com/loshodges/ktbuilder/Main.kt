package com.loshodges.ktbuilder



@KtBuilder
data class Person(val firstName: String,
                  val middleName: String? = null,
                  val lastName: String,
                  val age: Int,
                  val netWorth: Long? = null,
                  val aDouble: Double? = null,
                  val aFloat: Float)

fun main(args: Array<String>) {
  //  println("Hello ${HelloBuilder().getName()}")
}
