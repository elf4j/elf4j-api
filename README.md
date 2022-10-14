# Easy Logging Facade for Java (ELF4J)

A no-fluff Java logging facade API, using the Java Service Provider Interfaces ([SPI](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html)) mechanism to discover and load the run-time logging provider, e.g. JDK1.4 logging, logback, tinylog, log4j... No client code change needed for switching to a different logging implementations at deployment time. 

## User story

As a JLF client, I want to keep my application logging code untouched even when the underlying logging service implemention needs to be changed at the deployment time.

Notes:

For such a simple development need, there have been other catering efforts, e.g. Appache Commons Logging, SLF4J... If you have used some of those over the years, you may understand the reason why other attempts like this still exist.

The user would want to code against the [elf4j-api]() interface - hopefully, it's boringly simple yet convenient to use. 

Nothing will be logging out until a bound Service Provider is included in the application classpath - you can use the sample [tinylog binding](), and configure the run-time behavior of [tinylog](https://tinylog.org/v2/) as usual. It is a working SPI reference implementation, and serves as a bridge between the JLF API and the independent logging framework as the runtime logging service.

Hoping more bindings (JDK1.4 logging, logback, log4j, ...) will become available.
