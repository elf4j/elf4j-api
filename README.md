[![Maven Central](https://img.shields.io/maven-central/v/io.github.elf4j/elf4j-api.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.elf4j%22%20AND%20a:%22elf4j-api%22)

# elf4j-api

API and SPI of Easy Logging Facade for Java (ELF4J)

## User stories

1. As an application developer, I want to program logs of my application against an API, so that I can choose or
   change the actual logging implementation at the deployment time without code change.
2. As a logging framework provider, I want to have
   a [Service Provider Interfaces (SPI)](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) definition I can
   implement, so that my independent logging framework can be discovered and used by any ELF4J API application at the
   deployment time.

## Prerequisite

Java 8 or better

## Get it...

[![Maven Central](https://img.shields.io/maven-central/v/io.github.elf4j/elf4j-api.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.elf4j%22%20AND%20a:%22elf4j-api%22)

## Conventions, defaults, and implementation notes

### Placeholder token

The empty curly braces token `{}` is chosen to be the placeholder for message arguments.
This is by convention, and does not syntactically appear in the API or SPI. Both the API user and the SPI provider
must honor such convention.

### Immutability

An ELF4J `Logger` instance should be assumed immutable, thus thread-safe, by both the API client and the SPI provider.
This applies, even and especially, to those instances returned by the fluent-style `Logger#at[Level]()` methods.

### Logger name

To get an ELF4J `Logger` instance, the API user may supply a name or class to suggest the name of the logger when
calling the `Logger#instance(...)` method. However, it is up to the SPI provider how, if at all, to use the
user-supplied value to determine the logger name. E.g. if the API user ends up passing in `null` or using the
no-arg `Logger#instance()` method, then the name of the logger instance is undefined; the provider may opt to supply a
default, e.g. the name of the caller class.

### Log level

If the API user requests a `Logger` instance and does not set the log level by using a `Logger#at[Level]()` call, then
the actual logging behavior is undefined when `Logger#log(...)` is called. The SPI provider may opt to supply a default
level.

### Handling of supplied log message and arguments

Any `Object`-type argument to the `Logger#log(...)` methods, regardless a log message or a placeholder replacement
argument, must be treated specially if its actual type at runtime is `java.util.function.Supplier`. That is, the result
of `Supplier#get()`, instead of the `Supplier` function itself, should be used to compute the final log message. This
affords the API client the convenience of mixing `Supplier` functions and other `Object` types to form a log message.
When a lambda expression is used for the `Supplier` function, the lambda needs to be explicitly cast. e.g.

```jshelllanguage
logger.atInfo()
        .log("A log message with arguments of mixed types: {} and {}",
                "an Object type that is not a Supplier function",
                (Supplier) () -> "a Supplier function type");
```

## Use it...

### Client API

#### The Logger

Notice the fluent style of the API, where the `Logger#log(...)` methods are terminal operations, and
the `Logger#at[Level]()` methods returning a `Logger` instance are intermediate/configuration operations. Any `Logger`
instance returned by the API should be immutable, thus thread-safe.

```java
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

    boolean isEnabled();

    Logger atTrace();

    Logger atDebug();

    Logger atInfo();

    Logger atWarn();

    Logger atError();

    void log(Object message);

    void log(String message, Object... args);

    void log(Throwable t);

    void log(Throwable t, Object message);

    void log(Throwable t, String message, Object... args);
}
```

#### Sample usage

Note that ELF4J is a facade, rather than implementation. As such,

1. Nothing will be logging out (no-op) until you include an ELF4J logging provider JAR in the classpath. An API
   client as in this sample can select or change to use any provider ([tinylog](https://github.com/elf4j/elf4j-tinylog)
   , [LOG4J](https://github.com/elf4j/elf4j-log4j), ...) of the ELF4J SPI, at application deployment time, without code
   change.
2. At most one in-effect logging provider is expected.
    - The default and expected configuration setup is to ensure only one provider JAR present in the classpath, or no
      provider JAR when no-op is desired.
    - Otherwise, if multiple provider JARs are present, the system property `elf4j.logger.factory.fqcn` can be used to
      select the intended one. An intended provider absent from the classpath results in no-op.
    - It is considered a configuration error to have multiple provider JARs in the classpath without a selection. ELF4J
      falls back to no-op in all error scenarios.

```java

@Nested
class ReadmeSample {
    private final Logger logger = Logger.instance(ReadmeSample.class);

    @Test
    void messagesArgsAndGuards() {
        logger.log("logger name {} is usually the same as the param class name {}",
                logger.getName(),
                ReadmeSample.class.getName());
        assertEquals(ReadmeSample.class.getName(), logger.getName());
        logger.log("default log level is {}, which depends on the individual logging provider", logger.getLevel());
        Logger info = logger.atInfo();
        info.log("level set omitted here but we know the level is {}", INFO);
        assertEquals(INFO, info.getLevel());
        info.log("Supplier and other Object args can be mixed: Object arg1 {}, Supplier arg2 {}, Object arg3 {}",
                "a11111",
                (Supplier) () -> "a22222",
                "a33333");
        info.atWarn()
                .log("switched to WARN level on the fly. that is, {} is a different Logger instance from {}",
                        "`info.atWarn()`",
                        "`info`");
        assertNotSame(info, info.atWarn());
        assertEquals(INFO, info.getLevel(), "immutable info's level never changes");

        Logger debug = logger.atDebug();
        assertNotSame(logger, debug, "different instances of different levels");
        assertEquals(logger.getName(), debug.getName(), "same name, only level is different");
        assertEquals(Level.DEBUG, debug.getLevel());
        if (debug.isEnabled()) {
            debug.log(
                    "a {} message guarded by a {}, so that no {} is created unless this logger instance - name and level combined - is {}",
                    "long and expensive-to-construct",
                    "level check",
                    "message object",
                    "enabled by system configuration of the logging provider");
        }
        debug.log((Supplier) () -> "alternative to the level guard, using a Supplier<?> function like this should achieve the same goal of avoiding unnecessary message creation, pending quality of the logging provider");
    }
}

@Nested
class ReadmeSample2 {
    private final Logger error = Logger.instance(ReadmeSample2.class).atError();

    @Test
    void throwableAndMessageAndArgs() {
        Throwable ex = new Exception("ex message");
        error.log(ex);
        error.atInfo()
                .log("{} is an immutable Logger instance whose name is {}, and level is {}",
                        error,
                        error.getName(),
                        error.getLevel());
        assertEquals(Level.ERROR, error.getLevel());
        error.atError()
                .log(ex,
                        "here the {} call is unnecessary because a Logger instance is immutable, and the {} instance's log level is already and will always be {}",
                        "atError()",
                        error,
                        ERROR);
        error.log(ex,
                "now at Level.ERROR, together with the exception stack trace, logging some items expensive to compute: item1 {}, item2 {}, item3 {}, item4 {}, ...",
                "i11111",
                (Supplier) () -> "i22222",
                "i33333",
                (Supplier) () -> Arrays.stream(new Object[] { "i44444" }).collect(Collectors.toList()));
    }
}
```

### Provider SPI

#### The Service/Provider Interface

In terms of the [Java SPI](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) setup, the Service and Service
Provider Interface in this simple case is one and the same. The service provider should implement this interface such
that the ELF4J client application can discover and load the implementation using
the [ServiceLoader](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html).

```java
public interface LoggerFactory {
    Logger logger();

    Logger logger(String name);

    Logger logger(Class<?> clazz);
}
```

#### SPI implementations

Available logging service providers:

- [tinylog provider](https://github.com/elf4j/elf4j-tinylog)
- [LOG4J provider](https://github.com/elf4j/elf4j-log4j)
- [LOGBACK provider](https://github.com/elf4j/elf4j-logback)
- [java.util.logging (JUL) provider](https://github.com/elf4j/elf4j-jul)
- [SLF4J provider](https://github.com/elf4j/elf4j-slf4j)
- ...

More providers to come:

- ...
