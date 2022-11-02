# elf4j-api

The API and SPI of Easy Logging Facade for Java (ELF4J) - a no-fluff Java logging facade.

## User stories

1. As an application developer, I want to program logs of my application against an API, so that I can choose or
   change the actual logging implementation at the application deployment time without code change.
2. As a logging framework provider, I want to have
   a [Service Provider Interfaces (SPI)](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) definition
   I can implement, so that my independent logging framework can be discovered and used by any ELF4J API
   application at the deployment time.

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
method. However, it is up to the SPI provider how to use the passed-in name - if at all. E.g. if the API user ends up
passing in null or using the no-arg Logger#instance method, then the name of the logger instance is undefined; the
provider may opt to supply a default, e.g. the name of the caller class.

### Log level

If the API user does not set the log level by using the fluent-style Logger#at[Level] methods, then the actual logging
behavior is undefined; the SPI provider may opt to supply a default logging level.

### Message arguments

For any `Object` type message argument, if its run-time type is
Supplier<?>, then by convention, the Supplier#get method will be applied first before further logging process. This makes it possible to mix and match Supplier<?>
type of arguments with those of other types. E.g. using the convenient Logger#arg method to provide a Supplier<?>
lambda, we can mix message arguments as in:

```
logger.log("mixing arguments {} and {}", "a regular Object arg1", arg(() -> "a Supplier<?> arg2"));
```

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
    static Supplier<?> arg(Supplier<?> arg) {
        return arg;
    }
}
```

#### Sample usage

Note that ELF4J is a facade, rather than implementation. As such,

1. Nothing will be logging out (no-op) until you include an ELF4J logging provider JAR in the classpath. An API
   client as in this sample can select or change to use any provider ([tinylog](https://github.com/elf4j/elf4j-tinylog)
   , [LOG4J](https://github.com/elf4j/elf4j-log4j), ...) of the ELF4J SPI, at application deployment time, without code
   change.
2. At most one in-effect logging provider is expected.
    - The default configuration setup is to ensure only one provider JAR present in the classpath, or no provider JAR
      when no-op is desired.
    - Otherwise, the system property `elf4j.logger.factory.fqcn` can be used to select the intended one among multiple
      providers. An intended provider absent from the classpath results in no-op.
    - It is considered a configuration error having multiple provider JARs in the classpath without a selection. ELF4J
      falls back to no-op in all error scenarios.

```
    class readmeSamples {
        private final Logger logger = Logger.instance(readmeSamples.class);

        @Test
        void messagesArgsAndGuards() {
            logger.atInfo().log("info message");
            logger.atWarn()
                    .log("message arguments of Supplier<?> and other Object types can be mixed and matched, e.g. arg1 {}, arg2 {}, arg3 {}",
                            "a11111",
                            "a22222",
                            arg(() -> Arrays.stream(new Object[] { "a33333 supplier" }).collect(Collectors.toList())));
            Logger debug = logger.atDebug();
            if (debug.isEnabled()) {
                debug.log("a {} guarded by a {}, so {} is created {} DEBUG {} is {}",
                        "long message",
                        "level check",
                        "no message object",
                        "unless",
                        "level",
                        "enabled");
            }
            debug.log(() -> "alternative to the level guard, using a supplier function should achieve the same goal, pending quality of the logging provider");
        }

        @Test
        void throwableAndMessageAndArgs() {
            logger.atInfo().log("let's see immutability in action...");
            Logger error = logger.atError();
            error.log("this is an immutable logger instance whose level is Level.ERROR");
            Throwable ex = new Exception("ex message");
            error.log(ex, "level set omitted but we know the level is Level.ERROR");
            error.atWarn()
                    .log(ex,
                            "the log level switched to WARN on the fly. that is, {} returns a {} and {} Logger {}",
                            "atWarn()",
                            "different",
                            "immutable",
                            "instance");
            error.atError()
                    .log(ex,
                            "here the {} call is {} because the {} instance is {}, and the instance's log level has and will always be Level.ERROR",
                            "atError()",
                            "unnecessary",
                            "error logger",
                            "immutable");
            error.log(ex,
                    "now at Level.ERROR, together with the exception stack trace, logging some items expensive to compute: item1 {}, item2 {}, item3 {}, item4 {}, ...",
                    "i11111",
                    arg(() -> "i22222"),
                    "i33333",
                    arg(() -> Arrays.stream(new Object[] { "i44444" }).collect(Collectors.toList())));
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
- ...

More providers to come:

- ...
