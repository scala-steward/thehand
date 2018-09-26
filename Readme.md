# The Hand

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/533390e37ca04f7fac354b2acc71b681)](https://app.codacy.com/app/0um/thehand?utm_source=github.com&utm_medium=referral&utm_content=0um/thehand&utm_campaign=Badge_Grade_Dashboard)


## Requirements
- Postgresql [download](https://www.postgresql.org/download/)
- Java SDK 8 ou Superior [download](https://www.oracle.com/technetwork/java/javase/downloads/index.html)
- Sbt [download](https://www.scala-sbt.org/download.html)

### Setup DB
Create a user and database
[link for example](https://www.postgresql.org/docs/8.0/static/sql-createuser.html)

### Config
You can find an example in /src/main/resources/application.conf

#### Test config for running in memory
```
testConfig = {
  connectionPool      = disabled
  url                 = "jdbc:h2:mem:testdb"
  driver              = "org.h2.Driver"
  keepAliveConnection = true
}
```

#### Setup DB
```
dbconfig = {
  connectionPool = disabled
  url = "jdbc:postgresql:DATABASENAME?user=USERDBNAME&password=PASSDB"
  profile = ""
  driver = "org.postgresql.Driver"
  keepAliveConnection = true
  users = "USERDBNAME"
  password = "PASSDB"
  maxActive = 2
  maxConnections = 20
  numThreads = 10
}
```

#### Project Setup
Each project has a distinct configuration with the repository connection data and connection to the database.

It is important to switch to each project ```database_suffix =" demo_ "```, to keep each project separate using suffixes.

```
projectDemo = {
  user = "YOUR USER"
  pass = "YOUR PASS"
  url = "YOUR SVN URL"
  database_suffix = "demo_"
  mode = "auto"
  start_revision = 1  //use only if mode is no auto
  end_revision = 1000 //use only if mode is no auto
  task_model = {
    patternParser = "(#\\d)\\d+" //task or fix #NUMBER
    patternSplit = "#" //task or fix #NUMBER
    separator = ""
  }
}
```


Defines how the information about the tasks is extracted
```
patternParser = "(#\\d)\\d+" //task or fix #NUMBER
patternSplit = "#" //task or fix #NUMBER
separator = ""
```

##### Scan scm mode
There are two operating modes for scm scanning

###### Auto Mode
Search in the records what the last version already saved, and in scm which the last version sent. Using this data to load the records.
```
mode = "auto"
```

###### Manual Mode
Use the user-defined revision number to set the log load.
Currently does not update existing records. Not having an "insertOrUpdate" behavior.
```
mode = "off"
start_revision = 1
end_revision = 1000
```

##### Target Configuration
For agile flow data aggregation using [TargetProcess] (targetprocess.com)
Currently thehand does not have support for other means of authentication.
```
target = {
  user = "YOUR USER"
  pass = "YOUR PASS"
  url = "YOUR URL"
}
```

### Run
```
> sbt run
```

#### Test
```
> sbt test
```
