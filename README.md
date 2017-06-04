# jdbc-batch-perftester
Reproducing JDBC batch performance degradation problem

Requirements: java 8 and a PostgreSQL database which may be empty

Usage: `./run_test.bash <test database JDBC URL>`

The time taken on my machine is consistently slower with the newer version 
(42.1.1) of PostgreSQL JDBC driver than with an older version (9.4.1212), e.g.

```
$ ./run_test.bash 
JDBC URL not provideded, using the default of "jdbc:postgresql://localhost/test?user=test&password=password"
******************* JDBC Batch insert performance tester *******************

Compiling with javac ./src/Main.java -d ./classes
--------
Running test with ./lib/postgresql-42.1.1.jar :
Inserting 1000000 rows to test table and then dropping it...
Inserted 1000000 rows, took 28563 ms

real	0m28.829s
user	0m12.364s
sys	0m1.444s
--------
Running test with ./lib/postgresql-9.4.1212.jar :
Inserting 1000000 rows to test table and then dropping it...
Inserted 1000000 rows, took 10998 ms

real	0m11.230s
user	0m10.772s
sys	0m1.116s

```

This is consistent with our experience from using the two drivers in production.

Update
======

Looks like this is a regression bug of [pgjdbc/pgjdbc#690](https://github.com/pgjdbc/pgjdbc/pull/690) .
The previously
prepared statement is only used when `prepareThreshold=1`, but not for any
other value – including the `PGProperty.PREPARE_THRESHOLD` default of 5.

Latest master (as of Jun 3 2017) exhibits the problem, and the first bad
commit is [pgjdbc/pgjdbc@aca26a07026e9b289a209799ab28131a33b296dd](https://github.com/pgjdbc/pgjdbc/commit/aca26a07026e9b289a209799ab28131a33b296dd) .
A snapshot jar built from that commit is now included in the test run.

(The test run of `prepareThreshold=0` exhibits the original issue fixed by
pgjdbc/pgjdbc#690 ; the old 9.4.1212 driver does not honor this value.)

````
$ for N in `seq 0 5`; do echo "===============================================================================================" ; echo "with prepareThreshold=$N :" ; echo ; ./run_test.bash "jdbc:postgresql://localhost/test?user=test&password=password&prepareThreshold=$N"; done
===============================================================================================
with prepareThreshold=0 :

******************* JDBC Batch insert performance tester *******************

Compiling with javac ./src/Main.java -d ./classes
--------
Running test with ./lib/postgresql-42.0.0-SNAPSHOT-aca26a07026e9b289a209799ab28131a33b296dd.jar :
Inserting 1000000 rows to test table and then dropping it...
Inserted 1000000 rows, took 27839 ms

real	0m28.051s
user	0m12.392s
sys	0m0.756s
--------
Running test with ./lib/postgresql-42.1.1.jar :
Inserting 1000000 rows to test table and then dropping it...
Inserted 1000000 rows, took 28731 ms

real	0m28.983s
user	0m12.936s
sys	0m0.568s
--------
Running test with ./lib/postgresql-9.4.1212.jar :
Inserting 1000000 rows to test table and then dropping it...
Inserted 1000000 rows, took 10812 ms

real	0m11.024s
user	0m10.816s
sys	0m0.692s
===============================================================================================
with prepareThreshold=1 :

******************* JDBC Batch insert performance tester *******************

Compiling with javac ./src/Main.java -d ./classes
--------
Running test with ./lib/postgresql-42.0.0-SNAPSHOT-aca26a07026e9b289a209799ab28131a33b296dd.jar :
Inserting 1000000 rows to test table and then dropping it...
Inserted 1000000 rows, took 11242 ms

real	0m11.485s
user	0m11.524s
sys	0m0.656s
--------
Running test with ./lib/postgresql-42.1.1.jar :
Inserting 1000000 rows to test table and then dropping it...
Inserted 1000000 rows, took 10925 ms

real	0m11.144s
user	0m10.908s
sys	0m0.736s
--------
Running test with ./lib/postgresql-9.4.1212.jar :
Inserting 1000000 rows to test table and then dropping it...
Inserted 1000000 rows, took 10934 ms

real	0m11.138s
user	0m11.128s
sys	0m0.644s
===============================================================================================
with prepareThreshold=2 :

******************* JDBC Batch insert performance tester *******************

Compiling with javac ./src/Main.java -d ./classes
--------
Running test with ./lib/postgresql-42.0.0-SNAPSHOT-aca26a07026e9b289a209799ab28131a33b296dd.jar :
Inserting 1000000 rows to test table and then dropping it...
Inserted 1000000 rows, took 27558 ms

real	0m27.782s
user	0m12.080s
sys	0m0.864s
--------
Running test with ./lib/postgresql-42.1.1.jar :
Inserting 1000000 rows to test table and then dropping it...
Inserted 1000000 rows, took 27276 ms

real	0m27.494s
user	0m12.336s
sys	0m0.692s
--------
Running test with ./lib/postgresql-9.4.1212.jar :
Inserting 1000000 rows to test table and then dropping it...
Inserted 1000000 rows, took 10602 ms

real	0m10.804s
user	0m10.864s
sys	0m0.644s
===============================================================================================
with prepareThreshold=3 :

******************* JDBC Batch insert performance tester *******************

Compiling with javac ./src/Main.java -d ./classes
--------
Running test with ./lib/postgresql-42.0.0-SNAPSHOT-aca26a07026e9b289a209799ab28131a33b296dd.jar :
Inserting 1000000 rows to test table and then dropping it...
Inserted 1000000 rows, took 27044 ms

real	0m27.261s
user	0m12.244s
sys	0m0.584s
--------
Running test with ./lib/postgresql-42.1.1.jar :
Inserting 1000000 rows to test table and then dropping it...
Inserted 1000000 rows, took 27152 ms

real	0m27.368s
user	0m12.376s
sys	0m0.652s
--------
Running test with ./lib/postgresql-9.4.1212.jar :
Inserting 1000000 rows to test table and then dropping it...
Inserted 1000000 rows, took 10581 ms

real	0m10.781s
user	0m11.092s
sys	0m0.664s
===============================================================================================
with prepareThreshold=4 :

******************* JDBC Batch insert performance tester *******************

Compiling with javac ./src/Main.java -d ./classes
--------
Running test with ./lib/postgresql-42.0.0-SNAPSHOT-aca26a07026e9b289a209799ab28131a33b296dd.jar :
Inserting 1000000 rows to test table and then dropping it...
Inserted 1000000 rows, took 27231 ms

real	0m27.442s
user	0m12.332s
sys	0m0.612s
--------
Running test with ./lib/postgresql-42.1.1.jar :
Inserting 1000000 rows to test table and then dropping it...
Inserted 1000000 rows, took 27311 ms

real	0m27.530s
user	0m12.420s
sys	0m0.788s
--------
Running test with ./lib/postgresql-9.4.1212.jar :
Inserting 1000000 rows to test table and then dropping it...
Inserted 1000000 rows, took 10719 ms

real	0m10.934s
user	0m12.076s
sys	0m0.528s
===============================================================================================
with prepareThreshold=5 :

******************* JDBC Batch insert performance tester *******************

Compiling with javac ./src/Main.java -d ./classes
--------
Running test with ./lib/postgresql-42.0.0-SNAPSHOT-aca26a07026e9b289a209799ab28131a33b296dd.jar :
Inserting 1000000 rows to test table and then dropping it...
Inserted 1000000 rows, took 27189 ms

real	0m27.408s
user	0m12.364s
sys	0m0.772s
--------
Running test with ./lib/postgresql-42.1.1.jar :
Inserting 1000000 rows to test table and then dropping it...
Inserted 1000000 rows, took 26993 ms

real	0m27.211s
user	0m12.124s
sys	0m0.824s
--------
Running test with ./lib/postgresql-9.4.1212.jar :
Inserting 1000000 rows to test table and then dropping it...
Inserted 1000000 rows, took 10870 ms

real	0m11.070s
user	0m11.888s
sys	0m0.556s


````
