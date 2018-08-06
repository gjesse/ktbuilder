package com.loshodges.ktbuilder;

public class Main {

    public static void main(String[] args) {

        Person person = new PersonBuilder()
                .withFirstName("first")
                .withLastName("last")
                .withAge(42)
                .withAFloat(42F)
                .build();

        System.out.println(person);
    }
}
