/*
 * MIT License
 *
 * Copyright (c) 2023 ELF4J
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package elf4j;

import java.util.function.Supplier;

/**
 * All {@link Logger} instances from this API should be immutable.
 */
public interface Logger {
    /**
     * @return Logger instance with default name and Level
     */
    static Logger instance() {
        return LoggerFactoryProvider.INSTANCE.loggerFactory().logger();
    }

    /**
     * @param name suggested name of the Logger instance
     * @return Logger instance with the suggested name
     */
    static Logger instance(String name) {
        return LoggerFactoryProvider.INSTANCE.loggerFactory().logger(name);
    }

    /**
     * @param clazz Class used to suggest the Logger name
     * @return Logger instance using the given Class as the suggested name
     */
    static Logger instance(Class<?> clazz) {
        return LoggerFactoryProvider.INSTANCE.loggerFactory().logger(clazz);
    }

    /**
     * @return Logger instance with the same name, and DEBUG log level
     */
    Logger atDebug();

    /**
     * @return Logger instance with the same name, and ERROR log level
     */
    Logger atError();

    /**
     * @return Logger instance with the same name, and INFO log level
     */
    Logger atInfo();

    /**
     * @return Logger instance with the same name, and TRACE log level
     */
    Logger atTrace();

    /**
     * @return Logger instance with the same name, and WARN log level
     */
    Logger atWarn();

    /**
     * @return log Level of the logger instance
     */
    Level getLevel();

    /**
     * @return name of the logger instance
     */
    String getName();

    /**
     * @return true if the Logger instance is configured to be active per its name and Level, false otherwise
     */
    boolean isEnabled();

    /**
     * @param message to be logged. If the actual type is {@link java.util.function.Supplier}, the result of
     *                {@link Supplier#get()}, instead of the {@code message} itself, should be used to construct the
     *                final log message.
     */
    void log(Object message);

    /**
     * @param message to be logged
     * @param args    the arguments to replace the placeholders in the message. If any of the argument's actual type is
     *                {@link java.util.function.Supplier}, the result of {@link Supplier#get()}, instead of the argument
     *                itself, should be used to construct the final log message.
     */
    void log(String message, Object... args);

    /**
     * @param t the Throwable to be logged
     */
    void log(Throwable t);

    /**
     * @param t       the Throwable to be logged
     * @param message the message to be logged. If the actual type is {@link java.util.function.Supplier}, the result of
     *                {@link Supplier#get()}, instead of the {@code message} itself, should be used to construct the
     *                final log message.
     */
    void log(Throwable t, Object message);

    /**
     * @param t       the Throwable to be logged
     * @param message the message to be logged
     * @param args    the arguments to replace the placeholders in the message. If any of argument's actual type is
     *                {@link java.util.function.Supplier}, the result of {@link Supplier#get()}, instead of the argument
     *                itself, should be used to construct the final log message.
     */
    void log(Throwable t, String message, Object... args);
}
