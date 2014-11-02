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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;

/**
 * Utilities for dealing with comments, needed for the SourceInfo generation.
 * @author protobufel@gmail.com David Tesler
 */
final class ParserUtils {
  private static final Pattern LINE_FINDER = Pattern.compile("\\n");
  private static final Pattern BLOCK_COMMENT_PROTO_STRIPPER = Pattern.compile(
      "^[\\s&&[^\\n]]*(?:[*]|[\\s&&[^\\n\\r]]|\\r(?=.))", Pattern.MULTILINE | Pattern.UNIX_LINES);

  private ParserUtils() {}

  /**
   * Returns the block comment body stripped of leading whitespace and * in all but the first line,
   * and the block comments delimiters removed.
   * 
   * @param blockComment a C-style block comment
   * @return the comment's body with all but the first line specially trimmed
   */
  public static String getTrimmedBlockCommentContent(final String blockComment) {
    final String content = blockComment.substring(0, blockComment.length() - 2);
    return BLOCK_COMMENT_PROTO_STRIPPER.matcher(content).replaceAll("").substring(2);
  }

  /**
   * Gets a zero-based line count of text.
   * 
   * @param text a string in which to count lines
   * @return a number of {@code \n} new lines in the text
   */
  public static int getLineCount(final String text) {
    final Matcher matcher = LINE_FINDER.matcher(text);
    int i = 0;

    while (matcher.find()) {
      i++;
    }

    return i;
  }

  public static int getEndPositionInLine(final Token token) {
    return token.getCharPositionInLine() + token.getStopIndex() - token.getStartIndex() + 1;
  }

  public static int getTotalFieldCount(final Message.Builder builder) {
    int count = 0;

    for (final FieldDescriptor field : builder.getDescriptorForType().getFields()) {
      if (field.isRepeated()) {
        count += builder.getRepeatedFieldCount(field);
      } else if (builder.hasField(field)) {
        count++;
      }
    }

    return count;
  }

  public static final class CommonTokenStreamEx extends CommonTokenStream {

    public CommonTokenStreamEx(final TokenSource tokenSource) {
      super(tokenSource);
    }

    public Token nextToken(final int i, final int channel) {
      final int tokenIndex = super.nextTokenOnChannel(i, channel);
      return tokenIndex == -1 ? null : tokens.get(tokenIndex);
    }

    public Token previousToken(final int i, final int channel) {
      final int tokenIndex = super.previousTokenOnChannel(i, channel);
      return tokenIndex == -1 ? null : tokens.get(tokenIndex);
    }
  }
}
