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

    println(PersonBuilder()
            .withFirstName("foo")
            .withLastName("bar")
            .withAge(42)
            .withAFloat(42F)
            .build())

}
