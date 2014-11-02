//
// Copyright © 2014, David Tesler (https://github.com/protobufel)
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
// * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
// * Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
// * Neither the name of the <organization> nor the
// names of its contributors may be used to endorse or promote products
// derived from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

package com.github.protobufel.grammar;

import java.util.BitSet;
import java.util.Iterator;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Error handlers extending {@link ANTLRErrorListener} with {@code validationError}s.
 * 
 * @author protobufel@gmail.com David Tesler
 */
public final class ErrorListeners {
  private ErrorListeners() {}

  /**
   * A base interface for all ErrorListeners reporting syntax and validation errors.
   * 
   * @author protobufel@gmail.com David Tesler
   */
  public interface IProtoErrorListener extends ANTLRErrorListener {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
        int charPositionInLine, String msg, RecognitionException e);

    /**
     * Reports a validation error. All parameters are nullable.
     */
    public void validationError(Integer line, Integer charPositionInLine, String msg,
        RuntimeException exception);

    /**
     * Returns the proto name this listener is reporting about.
     */
    public String getProtoName();
  }

  /**
   * Extends {@link IProtoErrorListener} with the ability to set {@code protoName}.
   *
   * @author protobufel@gmail.com David Tesler
   */
  public interface IBaseProtoErrorListener extends IProtoErrorListener {
    public void setProtoName(String protoName);
  }

  /**
   * A {@link BaseErrorListener} with the settable {@code protoName}.
   *
   * @author protobufel@gmail.com David Tesler
   */
  public static class BaseProtoErrorListener extends BaseErrorListener implements
      IBaseProtoErrorListener {
    private String protoName;

    public BaseProtoErrorListener() {
      protoName = null;
    }

    public BaseProtoErrorListener(final String protoName) {
      this.protoName = protoName;
    }

    @Override
    public String getProtoName() {
      return protoName;
    }

    @Override
    public void setProtoName(final String protoName) {
      if (protoName == null) {
        throw new NullPointerException();
      }

      this.protoName = protoName;
    }

    @Override
    public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol,
        final int line, final int charPositionInLine, final String msg, final RecognitionException e) {}

    @Override
    public void validationError(final Integer line, final Integer charPositionInLine,
        final String msg, final RuntimeException exception) {}
  }

  /**
   * A composite {@link BaseErrorListener} delegating to its list of delegate error listeners.
   * 
   * @author protobufel@gmail.com David Tesler
   */
  public static final class ProxyProtoErrorListener extends BaseProtoErrorListener {
    private final Iterable<? extends IBaseProtoErrorListener> delegates;

    public ProxyProtoErrorListener(final Iterable<? extends IBaseProtoErrorListener> delegates) {
      if (delegates == null) {
        throw new NullPointerException("delegates");
      }

      this.delegates = delegates;
    }

    @Override
    public String getProtoName() {
      final Iterator<? extends IBaseProtoErrorListener> iterator = delegates.iterator();
      return iterator.hasNext() ? iterator.next().getProtoName() : null;
    }

    @Override
    public void setProtoName(final String protoName) {
      for (final IBaseProtoErrorListener listener : delegates) {
        listener.setProtoName(protoName);
      }
    }

    @Override
    public void reportAmbiguity(final Parser recognizer, final DFA dfa, final int startIndex,
        final int stopIndex, final boolean exact, final BitSet ambigAlts, final ATNConfigSet configs) {
      for (final IBaseProtoErrorListener listener : delegates) {
        listener.reportAmbiguity(recognizer, dfa, startIndex, stopIndex, exact, ambigAlts, configs);
      }
    }

    @Override
    public void reportAttemptingFullContext(final Parser recognizer, final DFA dfa,
        final int startIndex, final int stopIndex, final BitSet conflictingAlts,
        final ATNConfigSet configs) {
      for (final IBaseProtoErrorListener listener : delegates) {
        listener.reportAttemptingFullContext(recognizer, dfa, startIndex, stopIndex,
            conflictingAlts, configs);;
      }
    }

    @Override
    public void reportContextSensitivity(final Parser recognizer, final DFA dfa,
        final int startIndex, final int stopIndex, final int prediction, final ATNConfigSet configs) {
      for (final IBaseProtoErrorListener listener : delegates) {
        listener.reportContextSensitivity(recognizer, dfa, startIndex, stopIndex, prediction,
            configs);;
      }
    }

    @Override
    public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol,
        final int line, final int charPositionInLine, final String msg, final RecognitionException e) {
      for (final IBaseProtoErrorListener listener : delegates) {
        listener.syntaxError(recognizer, offendingSymbol, line, charPositionInLine, msg, e);;
      }
    }


    @Override
    public void validationError(final Integer line, final Integer charPositionInLine,
        final String msg, final RuntimeException exception) {
      for (final IBaseProtoErrorListener listener : delegates) {
        listener.validationError(line, charPositionInLine, msg, exception);
      }
    }
  }

  /**
   * An error listener printing to {@link System.err}.
   * 
   * @author protobufel@gmail.com David Tesler
   */
  public static class ConsoleProtoErrorListener extends BaseProtoErrorListener {

    public ConsoleProtoErrorListener() {
      super();
    }

    public ConsoleProtoErrorListener(final String protoName) {
      super(protoName);
    }

    @Override
    public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol,
        final int line, final int charPositionInLine, final String msg, final RecognitionException e) {
      printError(line, charPositionInLine, msg);
    }

    @Override
    public void validationError(final Integer line, final Integer charPositionInLine,
        final String msg, final RuntimeException exception) {
      printError(line, charPositionInLine, msg);
    }

    protected void printError(final int line, final int charPositionInLine, final String msg) {
      System.err.println("proto '" + getProtoName() + "' @ line " + line + ":" + charPositionInLine
          + " " + msg);
    }
  }

  /**
   * A logging error listener using org.slf4j.
   *
   * @author protobufel@gmail.com David Tesler
   */
  public static class LogProtoErrorListener extends BaseProtoErrorListener {
    private static final String VALIDATION_ERROR_LOG_MSG_FORMAT =
        "validationError proto '{}' @{}:{} msg:'{}' exception:'{}'";
    private static final String SYNTAX_ERROR_LOG_MSG_FORMAT =
        "syntaxError proto '{}' @{}:{} msg:'{}' recognizer:'{}' offendingSymbol:'{}' exception:'{}'";
    private Logger logger;
    private final IBaseProtoErrorListener delegate;

    public LogProtoErrorListener() {
      this(null, new BaseProtoErrorListener());
    }

    public LogProtoErrorListener(final String protoName) {
      this(protoName, new BaseProtoErrorListener(protoName));
    }

    public LogProtoErrorListener(final IBaseProtoErrorListener delegate) {
      this(null, delegate);
    }

    public LogProtoErrorListener(final String protoName, final IBaseProtoErrorListener delegate) {
      super(protoName);
      logger = LoggerFactory.getLogger(getClass());
      this.delegate = delegate;
    }

    @Override
    public void setProtoName(final String protoName) {
      super.setProtoName(protoName);
      delegate.setProtoName(protoName);
    }

    public LogProtoErrorListener setLogger(final Logger logger) {
      this.logger = logger;
      return this;
    }

    public LogProtoErrorListener setLogger(final Class<?> classToLog) {
      logger = LoggerFactory.getLogger(classToLog);
      return this;
    }

    @Override
    public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol,
        final int line, final int charPositionInLine, final String msg, final RecognitionException e) {
      logger.error(SYNTAX_ERROR_LOG_MSG_FORMAT, getProtoName(), line, charPositionInLine, msg,
          recognizer, offendingSymbol, e);
      delegate.syntaxError(recognizer, offendingSymbol, line, charPositionInLine, msg, e);
    }

    @Override
    public void validationError(final Integer line, final Integer charPositionInLine,
        final String msg, final RuntimeException exception) {
      logValidationError(line, charPositionInLine, msg, exception);
      delegate.validationError(line, charPositionInLine, msg, exception);
    }

    protected void logValidationError(final Integer line, final Integer charPositionInLine,
        final String msg, final RuntimeException exception) {
      logger.error(VALIDATION_ERROR_LOG_MSG_FORMAT, getProtoName(), line, charPositionInLine, msg,
          exception);
    }
  }
}
