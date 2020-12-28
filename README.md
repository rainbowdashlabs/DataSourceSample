## DataSourceSample
 
This repository provides example implementations to work with data 
pools in combination with [HikariCP](https://github.com/brettwooldridge/HikariCP). \
The examples are not considered as a best practice and are not designed to be copy pasted to other code,
but can be used as a reference for own implementations of HikariCP.\
They are also a better implementation than the present implementation in the most non professional projects.

This repository also provides some basic introduction into inserting and retrieving data.

The implementation can be found in the main directory.\
A example implementation of the DataSourceProvider and some benchmarks can be found in the test section. 

Implementation of DataSources for:
- MariaDB
- PostgreSQL

## Why you should use a Data Source and not a single Connection
A Data Source allows you to use pooling for connection.\
Using a connection pool allows you to use parallel requests. This can speed up your database operations by a lot, 
since your current Thread does not need to wait until the current single connection is free again.\
The Data Source also takes care of your connections 
and secures that your connection doesnt time out or that you get a closed connection.

There is a lot more good stuff in using them, but I dont want to write a book here.

## Why use try with Resources
A `connection`, `Statement` (`PreparedStatement`) and `ResultSet` are `AutoCloseable`.\
This means that they are closeable, but can be also closed automatically (obviously...).

When you open a connection this connection will stay open until it is closed.\
A statement need cache till it is closed.\
A Result set is also cached until it is closed.\

If you miss to close it you will have a memory leak and you will block connections and/or cache.\
You could also run out of free connections.

To avoid this you want to use a `try with resources`.\
This ensures that all closeables are closed when you leave the code block.

Here is some "pseudo code" which shows you the advantage of a auto closeable.

Currently you probably do something like this without a data source or a try with resources.
You get your connection from somewhere. This connection is probably static and some other bad stuff.

```java
try {
    Connection conn = getConnection();
    PreparedStatement stmt = conn.prepareStatement(Do some sql stuff);
    stmt.setSomething(1, something);
    ResultSet rs = stmt.exeuteQuery();
    // do something with the ResultSet

    // The following part is missed most of the time. Many ppl forget to close their shit.
    conn.close();
    stmt.close(); // Closing the Statement closes the ResultSet of the statement as well. 
} catch (SQLException e){
    e.printStackstrace(); // This should be replaced with a propper logging solution. Dont do this.
}
```

With `AutoCloseable` you dont have to bother anymore about closing your stuff.\
We will also use a DataSource named source which we cached somewhere inside our class 
(No we dont get it via a static variable from somewhere. This is bad design...)

```java
try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Do some sql stuff)) {
    ResultSet rs = stmt.exeuteQuery();
    stmt.setSomething(1, something);
    ResultSet rs = stmt.exeuteQuery();
    // do something with the ResultSet
} catch (SQLException e) {
    e.printStackstrace(); // This should be replaced with a propper logging solution. Dont do this.
}
```

Like you can see, we dont close our Stuff here, because we dont need it.;
Any object you assign inside the braces of the try braces will be closed when you exit the code block.\
Obviously object assigned inside these braces need to be of type `AutoCloseable`.

And thats it. Thats try with resources. Your connection, statement 
and result set are freed when you exit the code block and you dont have to care about it anymore.\
Now get out there and fix your broken stuff. (Any no a MySQL.java is not good...)