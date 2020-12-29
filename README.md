## Introduction
I worked on java applications for some time now. Since I am writing more and more Plugins with the SpigotAPI, I am more and more involved into the large community of plugin developers.\
While helping ppl with the same problems I had at the beginning I always stumble about bad design pattern and other bad stuff especially when it comes to database connections.\
Many ppl use single connections which they are trying to maintain in the best case. Some ppl just create the connection and just hope that it will never break (Hint: It will break.)

While starting to work with Java and Databases I also used some of the mentioned bad pattern. But that was just because everyone does it like that and I thought it would be okay. But everything changed when a friend mentioned DataSources. I was curious and discovered a bunch of new possibilities in case of performance and reliability.

Since not everyone has friends like me, which sometimes say "What are you doing? This is shit!", I want to be this friend for you.

So let me say something, if you can answer one of these questions with "yes":
- I never head or used a DataSource.
- I never heard of Try-with-Resources in connection with database connections and queries
- I have a file in my plugin named MySQL.java
- I never used a method calles close() in connection with database connections and queries.

If you anwered one or more with yes, this is for you:
**What are you doing? This is shit!**

Okay lets be friends. I want to help you. Lets start with changing how you interact with your database.

## DataSource
A DataSource is an interface of the javax.sql package. Dont worry I will keep this as simple as possible.\

A Data Source is implemented by a database driver. This driver is different for each database software. This means that you will need different driver depending on what database you want to connect.\
Since the most of you use MariaDB I will stick to this in my examples if I require some database specific stuff.\
However the DataSource is designed that you dont need to bother that much which database software you are using.

## But what does the DataSource do?
A Data Source provides a connection to your database. If you read this you are probably using a single connection which sometimes probably fails or gets a timeout. The DataSource will handle this for you.

Beside the DataSource we want to use connection pooling. To do this we will use [HikariCP](https://github.com/brettwooldridge/HikariCP). HikariCP is a Connection Pooling Framework which allows us to get a pool of connection which are managed by Hikari.\
This means that we always have a available not broken database connection and can do parallel read and writes on our database without waiting that the current transaction finishes. Amazing right? No fear of blocking other request because your connection is busy currently. Just use another one from the pool.

## How to create a DataSource with HikariCP
Okay lets get to the real thing.

We will now create a database connection to a mariadb using the Hikari ConnectionPool.\
To do this you will need the data you always need. So you probably have a sql setting already. You can continue using this.

I will start with showing you the whole code and explain everything afterwards.\
There are different ways of creating a connection pool with Hikari. I will show you the way I use.\
Hikari has reasonable default settings. We wont change much here.

```java
    Properties props = new Properties();
    props.setProperty("dataSourceClassName", "org.mariadb.jdbc.MariaDbDataSource");
    props.setProperty("dataSource.serverName", settings.getAddress());
    props.setProperty("dataSource.portNumber", settings.getPort());
    props.setProperty("dataSource.user", settings.getUser());
    props.setProperty("dataSource.password", settings.getPassword());
    props.setProperty("dataSource.databaseName", settings.getDatabase());

    HikariConfig config = new HikariConfig(props);

    config.setMaximumPoolSize(settings.getMaxConnections());

    DataSource source = new HikariDataSource(config);
```

And thats it. We have a connection pool with a defined amount of connections. This will be 10 by default, which will be enough in the most cases, especially when programming spigot plugins.

But lets go through it step by step.

We create a new Properties object. This is a java build in data type. Its basically a key value map. Nothing magic here.\
Now we configure our database.
The first line it the only thing for us which is depending on the database software.\
You have to enter the driver class for the database. This driver has to be included in your plugin or needs to be provided in any other way. A list of what driver class needs to be used for which database can be found [here](https://github.com/brettwooldridge/HikariCP#popular-datasource-class-names).

The rest should be pretty common for you. We set Address, Port, Username, Password and the database name.\
Important here:
You can enter just user and password and dont set the rest. In this case the default values will be used. The values will be defined by the default values of the database driver you are using and will differ with each driver (Especially the port since every database uses a different port).

After configuring our property object we just create a Hikari Config.\
The Hikari config allows us to configure the connection pool. You can define a lot here. But since the most default values are pretty reasonable, I wouldnt change much here.

In our case we just define the max active connections to our database.

And with these settings we finally create our HikariDataSource with a connection pool. We are done \o/

## How to use a data source
After Creating our data source we dont care about the underlying implementation anymore. Thats why I will always refer to DataSource instead of a HikariDataSource from now on.

In order to keep your code flow clean you will hopefully use this opportunity to get rid of your MySQL.java file.\
From now on you will pass the data source to the class where it is needed.

But lets get real and show you how you get a connection.
All code snippets are defined in a class which probably looks like this:
```java
public class SomeClass {
    DataSource source;

    public SomeClass(DataSource source) {
        this.source = source;
    }
}
```

A connection can be retrieved like this:
```java
    Connection conn = source.getConnection();
```
Thats all. But STOP! If you stop reading now you will just make everything worse.\
To work with database connections properly you have to use Try-with-Resources.

And thats what we are looking at next.

## Why use try with Resources
A `Connection`, `Statement` (`PreparedStatement`) and `ResultSet` are `AutoCloseable`.\
This means that they are closeable, but can be also closed automatically (obviously...).

When you open a connection this connection will stay open until it is closed.\
A statement need cache till it is closed.\
A Result set is also cached until it is closed.

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
    PreparedStatement stmt = conn.prepareStatement("SELECT some from stuff");
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
try (Connection conn = source.getConnection(); PreparedStatement stmt = conn.prepareStatement("SELECT some from stuff")) {
    ResultSet rs = stmt.exeuteQuery();
    stmt.setSomething(1, something);
    ResultSet rs = stmt.exeuteQuery();
    // do something with the ResultSet
} catch (SQLException e) {
    e.printStackstrace(); // This should be replaced with a propper logging solution. Dont do this.
}
```

You can see, that we dont close our stuff here, because we dont need it.
Any object you assign inside the braces of the try braces will be closed when you exit the code block.\
This will return the connection to our connection pool. Free the blocked memory for the result cache and statement and we are happy and ready for the next request.\
Obviously object assigned inside the braces need to be of type `AutoCloseable`.\
(Hint: Many more classes are auto closeable. Like input and output streams for example. Keep a look at the stuff you are using and use try with resources wherever you can.)

One more addition here. The result set is also a auto closeable, but we dont create it inside the try braces. It will still be closed. Lets take a look at the ResultSet documentation.
> A ResultSet object is automatically closed when the Statement object that generated it is closed, re-executed, or used to retrieve the next result from a sequence of multiple results.

[Source](https://docs.oracle.com/javase/7/docs/api/java/sql/ResultSet.html)

And thats it. Thats try with resources. Your connection, statement 
and result set are freed when you exit the code block and you dont have to care about it anymore.\
Now get out there and fix your broken stuff. (No a MySQL.java is not good...)

## Do you have some examples?
Of course! You may want to dive into this code. Its not perfect but better than 99% of the stuff I see on a daily base.

[Example implementation of a DataSource for MariaDB and PostgreSQL](https://github.com/RainbowDashLabs/DataSourceSample/tree/master/src/main/java/de/eldoria/databasesamples/datasources)

[Example Implementation and usage of a data Source with examples for try with resources and basic sql query stuff](https://github.com/RainbowDashLabs/DataSourceSample/blob/master/src/test/java/de/eldoria/databasesamples/datarequests/DataRequestSample.java)

[If you want to see how much a DataSource and ConnectionPool can speed up your application you may want to run this small benchmark](https://github.com/RainbowDashLabs/DataSourceSample/blob/master/src/test/java/de/eldoria/databasesamples/datarequests/RequestBenchmark.java)

[Oracle Documentation and examples for try-with-resources](https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html)

[Oracle Documentation for DataSources](https://docs.oracle.com/javase/tutorial/jdbc/basics/sqldatasources.html)