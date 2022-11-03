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

package elf4j.util;

import elf4j.Level;
import elf4j.Logger;

import java.util.function.Supplier;

public enum NoopLogger implements Logger {
    INSTANCE;

    @Override
    public Logger atTrace() {
        return this;
    }

    @Override
    public Logger atDebug() {
        return this;
    }

    @Override
    public Logger atInfo() {
        return this;
    }

    @Override
    public Logger atWarn() {
        return this;
    }

    @Override
    public Logger atError() {
        return this;
    }

    @Override
    public String getName() {
        return NoopLogger.INSTANCE.getClass().getName();
    }

    @Override
    public Level getLevel() {
        return Level.OFF;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void log(Object message) {
        //noop
    }

    @Override
    public void log(Supplier<?> message) {
        //noop
    }

    @Override
    public void log(String message, Object... args) {
        //noop
    }

    @Override
    public void log(String message, Supplier<?>... args) {
        //noop
    }

    @Override
    public void log(Throwable t) {
        //noop
    }

    @Override
    public void log(Throwable t, Object message) {
        //noop
    }

    @Override
    public void log(Throwable t, Supplier<?> message) {
        //noop
    }

    @Override
    public void log(Throwable t, String message, Object... args) {
        //noop
    }

    @Override
    public void log(Throwable t, String message, Supplier<?>... args) {
        //noop
    }
}
