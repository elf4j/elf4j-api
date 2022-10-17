# elf4j-api

Easy Logging Facade for Java (ELF4J) API and SPI

## User stories

1. As an application developer, I want to program my application logs against an API, so that the run-time logging
   implementation can be discovered and loaded when my application deploys without code change.
2. As a logging framework provider, I want to have
   a [Service Provider Interfaces (SPI)](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) definition, so
   that my independent logging framework can be discovered and bound to an application at the deployment time through
   such SPI mechanism.

## Prerequisite

Java 8 or better

## Get it...

[![Maven Central](https://img.shields.io/maven-central/v/io.github.elf4j/elf4j-api.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.elf4j%22%20AND%20a:%22elf4j-api%22)

## Use it...

Conventions, defaults, and implementation notes:

1. Placeholder token: The empty curly braces token `{}` is chosen to be the placeholder for the log message arguments.
   This is by convention, and does not syntactically appear in the API or SPI. Both the API user and the SPI provider
   must honor such convention. If the native logging framework uses different placeholder token(s), the SPI provider
   must take care of the token conversion.
2. Thread safety: Any logger instance should be considered thread-safe by both the API user and the SPI provider. This
   applies, even and especially, to those logger instances returned by the fluent-style `Logger.atZzz(...)` methods. The
   SPI provider e.g. can opt to achieve this by making the `Logger` implementation immutable.
3. Logger name: To get a `Logger` instance, ELF4J simply passes through the user-supplied logger name to the SPI
   provider. If the API user ends up passing in `null` or uses the no-arg `instance()` method to get a logger, then the
   name of the logger instance is undefined; the provider may opt to supply a default, e.g. the name of the caller
   class. It is also up to the SPI provider whether to conduct any sanitization on the logger name for security
   concerns.
4. Log level: Before every eventual log action, the API user is expected to set the log level by using the
   fluent-style `atLevel(Level level)` method or one of the no-arg shorthand equivalents. If the user omits such
   setting, the actual logging behavior is undefined; the SPI provider may opt to supply a default logging level.

### The client API

#### The Logger

Notice the fluent style of the API. The `.log(...)` methods are Terminal operations; the methods with return
type `Logger` are chain-able Intermediate/configuration operations.

```
public interface Logger {
   static Logger instance() {
      return LoggerFactoryProvider.INSTANCE.loggerFactory().logger();
   }
   
   static Logger instance(String name) {
      return LoggerFactoryProvider.INSTANCE.loggerFactory().logger(name);
   }
   
   static Logger instance(Class<?> clazz) {
      return LoggerFactoryProvider.INSTANCE.loggerFactory().logger(clazz);
   }
   
   String getName();
   Level getLevel();
   Logger atLevel(Level level);
   Logger atTrace();
   Logger atDebug();
   Logger atInfo();
   Logger atWarn();
   Logger atError();
   void log(Object message);
   void log(Supplier<?> message);
   void log(String message, Object... args);
   void log(String message, Supplier<?>... args);
   void log(Throwable t);
   void log(Throwable t, String message);
   void log(Throwable t, Supplier<String> message);
   void log(Throwable t, String message, Object... args);
   void log(Throwable t, String message, Supplier<?>... args);
}
```

#### Sample usage

Note: Once coding is done as in the sample, nothing will be logging out until you include a binding logging framework in
the classpath. The [tinylog ELF4J Service Provider](https://github.com/elf4j/elf4j-tinylog) binding JAR can be used as a
working example of the logging service provider, together with the [tinylog](https://tinylog.org/v2/) JAR itself. The
application using this ELF4J API can opt to use
any logging provider (e.g. the [LOG4J provider](https://github.com/elf4j/elf4j-log4j)) of the ELF4J SPI at deployment
time,
without any code change.

```
class LoggerSample {

   private static final Logger LOGGER = Logger.instance(LoggerSample.class);

   @Test
   void messageAndArgs() {
      LOGGER.atInfo().log("info message");
      LOGGER.atLevel(Level.INFO).log("{} is a shorthand of {}", "atInfo()", "atLevel(Level.INFO)");
      LOGGER.atWarn().log("warn message with supplier arg1 {}, arg2 {}, arg3 {}",
            () -> "a11111",
            () -> "a22222",
            () -> Arrays.stream(new Object[] { "a33333" }).collect(Collectors.toList()));
   }
   
   @Test
   void throwableAndMessageAndArgs() {
      Logger logger = LOGGER.atError();
      logger.log("level set omitted, this log's level is Level.ERROR");
      Throwable ex = new Exception("ex message");
      logger.log(ex);
      logger.atWarn().log(ex, "this log's level switched to WARN on the fly");
      logger.log(ex,
            "this log's level is now {} if the SPI provider opts to make the logger instance {}",
            "Level.ERROR",
            "immutable");
      logger.atInfo().log("set the {} before the {} inside the same {} logging statement to be certain",
            "Level",
            "final .log(...) call",
            "fluent-style");
   }
   ...
}
```

### The provider SPI

#### The Service/Provider Interface

In terms of the [Java SPI](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) setup, the Service and Service
Provider Interface in this simple case is one and the same. The service provider should implement this interface such
that the ELF4J client application can discover and load the implementation using
the [ServiceLoader](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html).

```
public interface LoggerFactory {
    Logger logger();
    Logger logger(String name);
    Logger logger(Class clazz);    
}
```

#### SPI implementations

Available logging service providers:

- [tinylog provider](https://github.com/elf4j/elf4j-tinylog) as a reference implementation
- [LOG4J provider](https://github.com/elf4j/elf4j-log4j)
- ...

More providers to come:

- JDK1.4 util logging provider
- logback provider
- ...
