# not-so-secure-webapp

generated using [Luminus](http://www.luminusweb.net/) version "2.9.11.22"

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
    lein cljsbuild once
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

### Missing Function Level Access Control

#### Flaw

The app has no proper authorization whatsoever. At least following flaws exists:

Issue: Missing Function Level Access Control in /#/admin
Steps to reproduce:

1. Instead going to /#/signin, enter url /#/admin
2. You know have full access to admin functionalities


Issue: Missing Function Level Access Control for DELETE and PUT /price requests 
Steps to reproduce:

1. Open the Network tab from your browser
2. Open Home from the navbar
3. Click "check your code!"
4. Open GET /code request Headers
5. Copy JSESSIONID=yoursession from the request cookie (and x-csrf-token if included)
6. Use e.g. [postman](https://www.getpostman.com/) or your browser to send following request with your session (and csrf)

Request:

```
HTTP Request type: PUT
Request URL: http://localhost:3000/price
```

Headers:

```
Host: localhost:3000
User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:50.0) Gecko/20100101 Firefox/50.0
Accept: application/json
Accept-Language: en-US,en;q=0.5
Accept-Encoding: gzip, deflate
Referer: http://localhost:3000/
x-csrf-token: (FIXME maybe)
Content-Type: application/transit+json
Content-Length: 48
Cookie: JSESSIONID=FIXME
Connection: keep-alive
```

Params:

```
["^ ","~:code","mycode","~:price","White House"]
```

8. Check that the response status code for your request was 200
9. Open Home tab again
10. Insert code "mycode" to input box
11. Click "check your code!"
12. Redeem White House

### Cross-Site Request Forgery (CSRF)

#### Flaw

Issue: Lack of CSRF protection
Step to reproduce:

1. Go to section "Home" from navbar
2. Use a code from [test-h2-data](resources/migrations/20170106140736-add-data.up.sql) or use the SQL injection flaw above
3. Click "check your code!"
4. Choose one price from the table by clicking the clickbox in the first column
5. Enter some Email address
6. Enter some Street address
7. Open Network tab from your browser to check requests
8. Click "Redeem your price!"
9. Inspect sended request and response "POST redeem" headers. Verify that there is no csrf-token and still request returns HTTP status 200

This possibles session hijackers to do succesful requests to site from e.g. other sites' XSS flaws.

#### Fixing proposal

##### 1. Fix server side

Enable server side CSRF-protection by adding following code to route-wrapper in function app-routes in file [handler.clj](src/clj/not_so_secure_webapp/handler.clj):

```clojure
(wrap-routes middleware/wrap-csrf)
```

After that addition, the function should look like e.g. this:

```clojure
(def app-routes
  (routes
    (-> #'home-routes
        (wrap-routes middleware/wrap-csrf)
        (wrap-routes middleware/wrap-formats))
    (route/not-found
      (:body
        (error-page {:status 404
:title "page not found"})))))
```

##### 2. Add CSRF token to html-template 

Add CSRF-token by adding following line to function render in file [layout.clj](src/clj/not_so_secure_webapp/layout.clj):

```clojure
:csrf-token *anti-forgery-token*
```

After that addition, the function should look like e.g. this:

```clojure
(defn render
  "renders the HTML template located relative to resources/templates"
  [template & [params]]
  (content-type
    (ok
      (parser/render-file
        template
        (assoc params
          :page template
          :csrf-token *anti-forgery-token*
          :servlet-context *app-context*)))
    "text/html; charset=utf-8"))
```

## LICENSE

Copyright © 2017 tkasu
