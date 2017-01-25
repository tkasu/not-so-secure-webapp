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
    
open localhost:3000 with your browser
    
### with provided .jar
     
    export DATABASE_URL="jdbc:h2:./mydb.db"
    java -jar target/uberjar/not-so-secure-webapp.jar migrate
    java -jar target/uberjar/not-so-secure-webapp.jar

open localhost:3000 with your browser

## Vulnerabilities

Vulnerabilities below are done intentionally and are chosen from https://www.owasp.org/index.php/Top_10_2013-Top_10 

### SQL Injection

#### Flaw

Issue: SQL Injection
Steps to reproduce:

1. Go to section "Home" from navbar
2. Write " ' OR '1' = '1 " to the input box
3. Click "check your code!"
4. You can now see all the codes and prices

#### Fixing proposal

Function get-prices in [db/core.clj](src/clj/not_so_secure_webapp/db/core.clj) concatinates user input directly to SQL-query. 

```clojure
(defn get-prices [code] 
  (jdbc/query 
   *db* 
   [(str 
     "select * from price where code = '" 
     code 
     "' and code not in (select code from winner)")]))
```

To fix this, one should use parametrizes query. In the case of this application, query should be moved to [queries.sql](resources/sql/queries.sql) so that user input code can be parametrized. See https://www.hugsql.org/ for more details.

### XSS

#### Flaw

Issue: XSS vulnerability
Steps to reproduce:

1. Go to section "Home" from navbar
2. Use a code from [test-h2-data](resources/migrations/20170106140736-add-data.up.sql) or use the SQL injection flaw above
3. Click "check your code!"
4. Choose one price from the table by clicking the clickbox in the first column
5. Enter some Email address
6. Enter following Street address: " \<img src="https://media.giphy.com/media/fRB9j0KCRe0KY/giphy.gif"/> "
7. Click "Redeem your price!"
8. Click "Check the past winners!"
9. You can now see how you rickrolled the app

#### Fixing proposal

Problem is that function winners-page in [core.cljs](src/cljs/not_so_secure_webapp/core.cljs) uses React.js tag "dangerouslySetInnerHTML". That causes React not to quote html-tags. However, browsers don't evaluate \<script> tags set to InnerHTML, but it might still be possible to use e.g. <img> onload and onerror attributes to evaluate scripts in victims browser.

To fix this just replace below code:

```clojure
[:td {:dangerouslySetInnerHTML {:__html (:email winner)}}]
```

With this simpler code:

```clojure
[:td (:email winner)]
```

## License

Copyright © 2017 tkasu
