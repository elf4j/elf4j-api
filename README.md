# elf4j-api

The API and SPI of Easy Logging Facade for Java (ELF4J)

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

An ELF4J `Logger` instance must be assumed immutable, thus thread-safe, by both the API client and the SPI provider.
This applies, even and especially, to those instances returned by the fluent-style Logger#at[Level] methods.

### Logger name

To get an ELF4J `Logger` instance, the API user may supply an associated name or class when calling the Logger#instance
method. However, it is up to the SPI provider how, if at all, to use the user-supplied value to determine the logger
name. E.g. if the API user ends up passing in `null` or using the no-arg Logger#instance method, then the name of the
logger instance is undefined; the provider may opt to supply a default, e.g. the name of the caller class.

### Log level

If the API user requests a `Logger` instance and does not set the log level by using a Logger#at[Level] call, then the
actual logging behavior is undefined when Logger#log is called. The SPI provider may opt to supply a default level.

## Use it...

### The client API

#### The Logger

Notice the fluent style of the API, where the Logger#log methods are terminal operations, and the methods returning
a `Logger` instance are intermediate/configuration operations. Any `Logger` instance returned by the API should be
immutable, thus thread-safe.

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
    Logger atTrace();
    Logger atDebug();
    Logger atInfo();
    Logger atWarn();
    Logger atError();
    String getName();
    Level getLevel();
    boolean isEnabled();
    void log(Object message);
    void log(Supplier<?> message);
    void log(String message, Object... args);
    void log(String message, Supplier<?>... args);
    void log(Throwable t);
    void log(Throwable t, Object message);
    void log(Throwable t, Supplier<?> message);
    void log(Throwable t, String message, Object... args);
    void log(Throwable t, String message, Supplier<?>... args);
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

```
    class readmeSamples {
        private final Logger logger = Logger.instance(readmeSamples.class).atInfo();

        @Test
        void messagesArgsAndGuards() {
            assertEquals(INFO, logger.getLevel());
            logger.log("info level message with arguments - arg1 {}, arg2 {}, arg3 {}", "a11111", "a22222", "a33333");
            logger.atWarn().log("switched to warn level on the fly");
            assertEquals(INFO, logger.getLevel(), "immutable logger's level/state never changes");

            Logger debug = logger.atDebug();
            assertNotSame(logger, debug, "different instances of different levels");
            assertEquals(logger.getName(), debug.getName(), "same name, only level is different");
            assertEquals(Level.DEBUG, debug.getLevel());
            if (debug.isEnabled()) {
                debug.log(
                        "a {} message guarded by a {}, so that no {} is created unless this logger - name and level combined - is {}",
                        "long and expensive-to-construct",
                        "level check",
                        "message object",
                        "enabled by system configuration of the logging provider");
            }
            debug.log(() -> "alternative to the level guard, using a Supplier<?> function like this should achieve the same goal of avoiding unnecessary message creation, pending quality of the logging provider");
        }

        @Test
        void throwableAndMessageAndArgs() {
            Throwable ex = new Exception("ex message");
            Logger error = logger.atError();
            error.log(ex, "this is an immutable Logger instance whose level is Level.ERROR");
            assertEquals(Level.ERROR, error.getLevel());
            error.log(ex, "level set omitted but we know the level is Level.ERROR");
            error.atWarn()
                    .log(ex,
                            "switched to warn level on the fly. that is, {} returns a {} and {} Logger {}",
                            "atWarn()",
                            "different",
                            "immutable",
                            "instance");
            error.atError()
                    .log(ex,
                            "here the {} call is {} because a Logger instance is {}, and the instance's log level has and will always be Level.ERROR",
                            "atError()",
                            "unnecessary",
                            "immutable");
            error.log(ex,
                    "now at Level.ERROR, together with the exception stack trace, logging some items expensive to compute: item1 {}, item2 {}, item3 {}, item4 {}, ...",
                    () -> "i11111",
                    () -> "i22222",
                    () -> "i33333",
                    () -> Arrays.stream(new Object[] { "i44444" }).collect(Collectors.toList()));
        }
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
