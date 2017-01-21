# not-so-secure-webapp

generated using Luminus version "2.9.11.22"

https://cybersecuritybase.github.io/project/ course project

## Prerequisites

1. Java 6 or newer
2. You will need [Leiningen][1] 2.0 or above installed.
   OR If you don't want to use Leiningen, .jar is provided.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

### with Leiningen
    
    export DATABASE_URL="jdbc:h2:./mydb.db"
    lein run migrate
    lein run
    
### with provided .jar
     
    export DATABASE_URL="jdbc:h2:./mydb.db"
    java -jar target/uberjar/not-so-secure-webapp.jar migrate
    java -jar target/uberjar/not-so-secure-webapp.jar

## License

Copyright © 2017 tkasu
