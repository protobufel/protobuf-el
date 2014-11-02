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

import java.util.List;

import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Message;

/**
 * Various validation exceptions used by {@link ProtoFileParser}.
 *
 * @author protobufel@gmail.com David Tesler
 */
public final class Exceptions {
  private Exceptions() {}

  public static interface IProtoParserException {
    public String getMessageFormat();
  }

  public static final class NonUniqueException extends RuntimeException implements
      IProtoParserException {
    private static final long serialVersionUID = 1L;
    private static final String MESSAGE_FORMAT = "the %s:%s already set";
    private final String propertyName;

    public NonUniqueException(final Object value, final String propertyName) {
      super(String.format(MESSAGE_FORMAT, propertyName, value));
      this.propertyName = propertyName + ":" + value;
    }

    public String getPropertyName() {
      return propertyName;
    }

    @Override
    public String getMessageFormat() {
      return MESSAGE_FORMAT;
    }
  }

  public static final class NonUniqueExtensionNumber extends RuntimeException implements
      IProtoParserException {
    private static final long serialVersionUID = 1L;
    private static final String MESSAGE_FORMAT = "extension '%s.%s' number %d is already taken";

    public NonUniqueExtensionNumber(final String message) {
      super(message);
    }

    public NonUniqueExtensionNumber(final String extendee, final String name, final int number) {
      super(String.format(MESSAGE_FORMAT, extendee, name, number));
    }

    @Override
    public String getMessageFormat() {
      return MESSAGE_FORMAT;
    }
  }

  public static final class FieldInExtensionRangeException extends RuntimeException implements
      IProtoParserException {
    private static final long serialVersionUID = 1L;
    private static final String MESSAGE_FORMAT =
        "the %s field number should not be in the message/group extension ranges";

    public FieldInExtensionRangeException(final String fieldName) {
      super(String.format(MESSAGE_FORMAT, fieldName));
    }

    @Override
    public String getMessageFormat() {
      return MESSAGE_FORMAT;
    }
  }

  public static final class InvalidExtensionRange extends RuntimeException implements
      IProtoParserException {
    private static final long serialVersionUID = 1L;
    private static final String MESSAGE_FORMAT = "extension range[%s, %s] overlaps with others";

    public InvalidExtensionRange(final int start, final int end) {
      super(String.format(MESSAGE_FORMAT, start, end));
    }

    public InvalidExtensionRange(final String message) {
      super(message);
    }

    @Override
    public String getMessageFormat() {
      return MESSAGE_FORMAT;
    }
  }

  public static final class UnresolvedTypeNameException extends RuntimeException implements
      IProtoParserException {
    private static final long serialVersionUID = 1L;
    private static final String MESSAGE_FORMAT = "The field %s has unresolvable type name: %s";

    public UnresolvedTypeNameException(final String fieldName, final List<?> unresolvedInfo) {
      super(String.format(MESSAGE_FORMAT, fieldName, unresolvedInfo));
    }

    @Override
    public String getMessageFormat() {
      return MESSAGE_FORMAT;
    }
  }

  public static final class CircularProtoDependenciesException extends RuntimeException implements
      IProtoParserException {
    private static final long serialVersionUID = 1L;
    private static final String MESSAGE_FORMAT =
        "these FileDescriprorProtos are circular or refer to nonexisting files: %s";
    private final List<String> protoNames;

    public CircularProtoDependenciesException(final List<String> protoNames) {
      super(String.format(MESSAGE_FORMAT, protoNames));
      this.protoNames = protoNames;
    }

    public List<String> getProtoNames() {
      return protoNames;
    }

    @Override
    public String getMessageFormat() {
      return MESSAGE_FORMAT;
    }
  }

  public static final class DescriptorValidationRuntimeException extends RuntimeException {
    public static final String REPEATING_CUSTOM_OPTION_SINGULAR_FIELD_MSG =
        "repeating custom option's singular field";
    private static final long serialVersionUID = 1L;
    private final String problemSymbolName;
    private final Message proto;
    private final String description;

    public DescriptorValidationRuntimeException(final String problemSymbolName,
        final Message proto, final String description) {
      super(problemSymbolName + ": " + description);
      this.problemSymbolName = problemSymbolName;
      this.proto = proto;
      this.description = description;
    }

    public DescriptorValidationRuntimeException(final DescriptorValidationException delegate) {
      super(delegate.getMessage(), delegate);
      problemSymbolName = delegate.getProblemSymbolName();
      proto = delegate.getProblemProto();
      description = delegate.getDescription();
    }

    public String getProblemSymbolName() {
      return problemSymbolName;
    }

    public Message getProblemProto() {
      return proto;
    }

    public String getDescription() {
      return description;
    }
  }
}
