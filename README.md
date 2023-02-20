# cancun-booking
Project created used spring boot to simulate a hotel booking API.

## Stack used
- **Java 17**;
- **Maven** as the build tool;
- **Lombok** to avoid the needed java boilerplate;
- **Spring MVC** to handle http requests;
- **Spring Data JPA** to handle database interactions;
- **Bean Validation** to validate incoming requests payload;
- **JUnit, Mockito and AssertJ** for unit testing;
- **MySQL** as the database engine.

## How to run the application
Have a running instance of a MySQL 8 database.

Set the following environment varibles:
- DATABASE_HOST - the database host (e.g. localhost);
- DATABASE_PORT - the database port (e.g. 3306);
- DATABASE_SCHEMA - the database schema (e.g. cancundb);
- DATABASE_USER - the database connection user;
- DATABASE_PASSWORD - the database connection password.

Either start the application with a IDE of choice or package it with **mvn clean package** and start the generated .jar app with **java -jar <app_name>.jar**
