## Read jar from blob console app
Sample app for reading BLOB in Oracle DB.
This small console app allows download jars from DB.


## How use
The data is assumed to be stored as follows:

| JAR Name  | File name (class) | Package name (path) | BLOB |
| --------- | ------------- |  ------------- | ------------- |
| App.jar  | Main.class | org/company/engine | (BLOB)
| ...  | ...  |  ...  |  ...  |

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
