/*
 * MIT License
 *
 * Copyright (c) 2022. Qingtian Wang
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

package org.elf4j;

import org.elf4j.spi.LoggerFactory;
import org.elf4j.util.NoopLoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;

enum LoggerFactoryProvider {
    INSTANCE;
    private final LoggerFactory loggerFactory;
    private final java.util.logging.Logger utilLogger =
            java.util.logging.Logger.getLogger(LoggerFactoryProvider.class.getName());

    LoggerFactoryProvider() {
        this.loggerFactory = load();
    }

    LoggerFactory loggerFactory() {
        return loggerFactory;
    }

    private LoggerFactory load() {
        ServiceLoader<LoggerFactory> serviceLoader = ServiceLoader.load(LoggerFactory.class);
        List<LoggerFactory> loggerFactories = new ArrayList<>();
        for (LoggerFactory loaded : serviceLoader) {
            loggerFactories.add(loaded);
        }
        if (loggerFactories.isEmpty()) {
            utilLogger.warning("no provisioned logger factory loaded!!! falling back to NO-OP logging...");
            return NoopLoggerFactory.INSTANCE;
        }
        if (loggerFactories.size() == 1) {
            LoggerFactory provisionedLoggerFactory = loggerFactories.get(0);
            utilLogger.log(Level.INFO, "provisioned ELF4J logger factory [{0}]", provisionedLoggerFactory);
            return provisionedLoggerFactory;
        }
        throw new IllegalStateException(
                "expected one single provided logger factory but loaded " + loggerFactories.size() + ": "
                        + loggerFactories + ", by current class loader: " + Thread.currentThread()
                        .getContextClassLoader());
    }
}
