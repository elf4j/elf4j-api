/*
 * MIT License
 *
 * Copyright (c) 2022 ELF4J
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
 *
 */

package elf4j;

import elf4j.spi.LoggerFactory;
import elf4j.util.NoopLoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.logging.Level;

enum LoggerFactoryProvider {
    INSTANCE;
    private static final String ELF4J_LOGGER_FACTORY_FQCN = "elf4j.logger.factory.fqcn";
    private final java.util.logging.Logger julLogger =
            java.util.logging.Logger.getLogger(LoggerFactoryProvider.class.getName());
    private final LoggerFactory loggerFactory;

    LoggerFactoryProvider() {
        this.loggerFactory = getLoggerFactory();
    }

    private static Optional<String> getLoggerFactorySelection() {
        String desiredLoggerFactoryFqcn = System.getProperty(ELF4J_LOGGER_FACTORY_FQCN);
        if (desiredLoggerFactoryFqcn == null || desiredLoggerFactoryFqcn.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(desiredLoggerFactoryFqcn.trim());
    }

    private static List<LoggerFactory> loadLoggerFactories() {
        ServiceLoader<LoggerFactory> serviceLoader = ServiceLoader.load(LoggerFactory.class);
        List<LoggerFactory> loadedFactories = new ArrayList<>();
        for (LoggerFactory loaded : serviceLoader) {
            loadedFactories.add(loaded);
        }
        return loadedFactories;
    }

    LoggerFactory loggerFactory() {
        return loggerFactory;
    }

    private LoggerFactory getLoggerFactory() {
        List<LoggerFactory> loadedFactories = loadLoggerFactories();
        Optional<String> desiredLoggerFactoryFqcn = getLoggerFactorySelection();
        if (desiredLoggerFactoryFqcn.isPresent()) {
            for (LoggerFactory loaded : loadedFactories) {
                if (loaded.getClass().getName().equals(desiredLoggerFactoryFqcn.get())) {
                    julLogger.log(Level.INFO, "desired ELF4J logger factory discovered: {0}", loaded);
                    return loaded;
                }
            }
            julLogger.log(Level.SEVERE,
                    "configuration error! desired ELF4J logger factory [{0}] not found in discovered factories: {1}. falling back to NO-OP logging...",
                    new Object[] { desiredLoggerFactoryFqcn.get(), loadedFactories });
            return new NoopLoggerFactory();
        }
        if (loadedFactories.isEmpty()) {
            julLogger.log(Level.WARNING,
                    "no ELF4J logger factory discovered; this is OK if NO-OP logging is desired. falling back to NO-OP logging...");
            return new NoopLoggerFactory();
        }
        if (loadedFactories.size() == 1) {
            LoggerFactory provisionedLoggerFactory = loadedFactories.get(0);
            julLogger.log(Level.INFO, "successfully discovered ELF4J logger factory: {0}", provisionedLoggerFactory);
            return provisionedLoggerFactory;
        }
        julLogger.log(Level.SEVERE,
                "configuration error! expected zero or one ELF4J logger factory but discovered {0}: {1}. please either re-configure, or select the desired factory using the `{2}` system property. falling back to NO-OP logging...",
                new Object[] { loadedFactories.size(), loadedFactories, ELF4J_LOGGER_FACTORY_FQCN });
        return new NoopLoggerFactory();
    }
}
