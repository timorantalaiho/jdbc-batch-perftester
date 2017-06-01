# jdbc-batch-perftester
Reproducing JDBC batch performance degradation problem

Requirements: java 8 and a PostgreSQL database which may be empty

Usage: `./run_test.bash <test database JDBC URL>`

The time taken on my machine is consistently slower with the newer version 
(42.1.1) of PostgreSQL JDBC driver than with an older version (9.4.1212), e.g.

```
$ ./run_test.bash 
JDBC URL not provideded, using the default of "jdbc:postgresql://localhost/tester?user=tester&password=tester"
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
