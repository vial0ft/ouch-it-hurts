#!/bin/bash

env DB_HOST='localhost' DB_PORT='5432' DB_USER='postgres' DB_PASSWORD='12345678' PORT='8500' java -jar target/app-0.0.1-standalone.jar
