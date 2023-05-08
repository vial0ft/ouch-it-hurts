#!/bin/bash

env JDBC_URL='jdbc:postgresql://localhost:5432/postgres' DB_USER='postgres' DB_PASSWORD='12345678' PORT='8500' java -jar target/app-0.0.1-standalone.jar
