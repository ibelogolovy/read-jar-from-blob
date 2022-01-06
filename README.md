## Read jar from blob console app
This small console app allows download jars from database.

## How use
#### Input:
- database url
- user login for database access
- user password for database access
- table name with database schema
- jar column
- file name column (class name)
- package name (relative path in jar) column
- BLOB name column 
- JAR name (app.jar or app for example)

#### Output:
Jar in ./output directory

## Setup
```
./gradlew build
java -jar build/libs/read-jar-from-blob-1.0-SNAPSHOT.jar
```
