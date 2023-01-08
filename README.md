[![Maven Central](https://img.shields.io/maven-central/v/io.github.elf4j/elf4j-api.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.elf4j%22%20AND%20a:%22elf4j-api%22)

# elf4j-api

API and SPI of Easy Logging Facade for Java (ELF4J)

## User Stories

1. As an application developer, I want to program logs of my application against an API, so that I can choose or
   change the actual logging implementation at the deployment time without code change.
2. As a logging framework provider, I want to have
   a [Service Provider Interfaces (SPI)](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) definition I can
   implement, so that my independent logging framework can be discovered and used by any ELF4J API application at the
   deployment time.

## Prerequisite

Java 8 or better

## Get It...

From [![Maven Central](https://img.shields.io/maven-central/v/io.github.elf4j/elf4j-api.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.elf4j%22%20AND%20a:%22elf4j-api%22)
as a compile-scope dependency

```xml

<dependency>
    <groupId>io.github.elf4j</groupId>
    <artifactId>elf4j-api</artifactId>
</dependency>
```

## The Logger Service Interface And Access API

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

## Conventions, Defaults, And Implementation Notes

- Placeholder Token

The empty curly braces token `{}` is chosen to be the placeholder for message arguments. e.g.

```jshelllanguage
logger.log("A log {} can have {}", "message", "arguments");
```

This is by convention, and does not syntactically appear in the API or SPI. Both the API user and the SPI provider
must honor such convention.

- Immutability

The API client should assume any ELF4J `Logger` instance is immutable, thus thread-safe. The SPI provider implementation
must support such assumption.

- Logger Name

To get an ELF4J `Logger` instance, the API user may supply a name or class to suggest the name of the logger when
calling the `Logger#instance(...)` methods. However, it is up to the SPI provider how, if at all, to use the
user-supplied value to determine the logger name. e.g. if the API user ends up passing in `null` or using the
no-arg `Logger#instance()` method, then the name of the logger instance is undefined; the provider may opt to supply a
default, e.g. the name of the caller class.

- Log Level

If the API user gets a `Logger` instance via a `Logger#instance(...)` method, the default log level of such instance is
decided by the SPI provider implementation. If the API user gets a `Logger` instance via a `Logger#at[Level]()` method,
then the SPI provider should supply such instance with the requested level.

- `Supplier` Functional Arguments

An `Object`-type argument passed to any of the `Logger#log(...)` methods must be treated specially if the actual type at
runtime is `java.util.function.Supplier`. That is, the result of `Supplier#get()`, instead of the `Supplier` function
itself, should be used to compute the final log message.

This special handling of `Supplier` arguments is by convention, and not syntactically enforced by the API or SPI. This
allows for the API user to mix up `Supplier` and other `Object` types of arguments within the same call
of `Logger#log(...)`, and get sensible outcome for the final log message:

```jshelllanguage
logger.log(
        "A Logger#log(...) method's arguments of Object type can be a mixture of {} type and other {} types, with sensible logging message result.",
        (Supplier) () -> "Supplier function",
        "Object");
```

Per the lambda expression syntax requirement, the downcast of `Supplier/Supplier<?>/Supplier<String>` here is necessary
because this lambda is used as a parameter declared as an `Object` rather than a functional interface.

## For API Users...

Note that ELF4J is a facade, rather than implementation. As such,

No-op by default:

- Nothing will be logging out (no-op) unless a properly configured external ELF4J logging provider is discovered at the
  application start time. The only/default implementation shipped with the ELF4J facade itself is the no-op provider.

Only one in-effect logging provider:

- An API user can select or change to
  use [any ELF4J service provider](https://github.com/elf4j/elf4j-api#available-logging-service-providers-of-the-elf4j-spi)
  at deploy time, without code change.
- The default and recommended setup is to ensure that only the one desired logging provider JAR is present in the
  classpath at deploy time, or, no external provider JAR if no-op is desired. In this case, nothing further is needed
  for the ELF4J API to work.
- If multiple external provider JARs are present, however, then the system property `elf4j.logger.factory.fqcn` has to
  be used to select the desired provider. e.g.
  ```
  java -jar MyApplication.jar -Delf4j.logger.factory.fqcn="elf4j.log4j.Log4jLoggerFactory"
  ```
  No-op if the selected provider JAR is absent from the classpath.
- It is considered a setup error to have multiple provider JARs in the classpath without a selection. ELF4J falls back
  to no-op in all error scenarios.

```java

class ReadmeSample {
    private final Logger defaultLogger = Logger.instance();

    @Test
    void messagesArgsAndGuards() {
        defaultLogger.log("default logger name is usually the same as the API caller class name");
        assertEquals(ReadmeSample.class.getName(), defaultLogger.getName());
        defaultLogger.log("default log level is {}, which depends on the individual logging provider",
                defaultLogger.getLevel());
        Logger info = defaultLogger.atInfo();
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
        assertEquals(info.getName(), info.atWarn().getName(), "same name, only level is different");
        assertEquals(WARN, info.atWarn().getLevel());
        assertEquals(INFO, info.getLevel(), "immutable info's level never changes");

        if (defaultLogger.atDebug().isEnabled()) {
            defaultLogger.atDebug()
                    .log("a {} message guarded by a {}, so that no {} is created unless this logger instance - name and level combined - is {}",
                            "long and expensive-to-construct",
                            "level check",
                            "message object",
                            "enabled by system configuration of the logging provider");
        }
        defaultLogger.atDebug()
                .log((Supplier) () -> "alternative to the level guard, using a Supplier<?> function like this should achieve the same goal of avoiding unnecessary message creation, pending quality of the logging provider");
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

## For Service Providers...

As with the [Java Service Provider Framework](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html),
the logging Service Provider should supply a concrete and complete implementation, including both the provider class for
the `LoggerFactory` [SPI](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html) and the service class for
the `Logger` API, such that the ELF4J API client application can discover and load the provider implementation using the
`java.util.ServiceLoader`.

```java
public interface LoggerFactory {
    Logger logger();

    Logger logger(String name);

    Logger logger(Class<?> clazz);
}
```

## Available Logging Service Providers Of The ELF4J SPI

- [tinylog provider](https://github.com/elf4j/elf4j-tinylog)
- [LOG4J provider](https://github.com/elf4j/elf4j-log4j)
- [LOGBACK provider](https://github.com/elf4j/elf4j-logback)
- [java.util.logging (JUL) provider](https://github.com/elf4j/elf4j-jul)
- [SLF4J provider](https://github.com/elf4j/elf4j-slf4j)
- ...

More providers to come:

- ...
