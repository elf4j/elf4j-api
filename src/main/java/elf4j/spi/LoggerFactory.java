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

package elf4j.spi;

import elf4j.Logger;

/**
 * Service Provider Interface for the ELF4J logging provider. A provider implementation class of this SPI needs to be
 * loadable by {@link java.util.ServiceLoader}.
 *
 * @see <a href="https://docs.oracle.com/javase/7/docs/api/java/util/ServiceLoader.html">Javadoc of
 *         ServiceLoader</a>
 */
public interface LoggerFactory {
    /**
     * @return a Logger instance with a default name at the provider's discretion, usually the caller class name.
     */
    Logger logger();

    /**
     * @param name Client proposed name of the Logger instance returned. It's up to the provider whether to honor the
     *             logger name as requested although, usually, the name request is honored verbatim.
     * @return Logger instance with the requested name
     */
    Logger logger(String name);

    /**
     * @param clazz used to suggest the name of the Logger instance returned. It's up to the provider how, if at all, to
     *              use the class to name the logger. Usually the fully qualified class name is used to name the
     *              logger.
     * @return Logger instance of the requesting class
     */
    Logger logger(Class<?> clazz);
}
