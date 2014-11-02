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

import static org.assertj.core.api.Assertions.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;

import java.io.InputStream;
import java.util.Collection;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.protobufel.grammar.Exceptions.FieldInExtensionRangeException;
import com.github.protobufel.grammar.Exceptions.InvalidExtensionRange;
import com.github.protobufel.grammar.Exceptions.NonUniqueException;
import com.github.protobufel.grammar.Exceptions.NonUniqueExtensionNumber;
import com.github.protobufel.grammar.Exceptions.UnresolvedTypeNameException;
import com.github.protobufel.grammar.ParserUtils.CommonTokenStreamEx;
import com.github.protobufel.grammar.ProtoParser.ProtoContext;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;

// TODO redo all tests and enable
@Ignore
@RunWith(Parameterized.class)
public class ProtoFileParserValidationsTest {
  private static final Logger log = LoggerFactory.getLogger(ProtoFileParserValidationsTest.class);
  private ParseTreeWalker walker;
  private ProtoFileParser protoParser;
  private ProtoContext tree;
  private ProtoParser parser;

  @Parameters(name = "{index}:{0}")
  public static Collection<Object[]> data() {
    return ImmutableList.<Object[]>of(new Object[] {"NonUniqueFieldName1.proto",
        NonUniqueException.class, "field name"}, new Object[] {"NonUniqueFieldNumber1.proto",
        NonUniqueException.class, "field number"}, new Object[] {"FieldsInExtensionRange1.proto",
        FieldInExtensionRangeException.class, ""}, new Object[] {"InvalidExtensionRange1.proto",
        InvalidExtensionRange.class, ""}, new Object[] {"InvalidExtensionRange2.proto",
        InvalidExtensionRange.class, ""}, new Object[] {"InvalidExtensionRange3.proto",
        InvalidExtensionRange.class, ""}, new Object[] {"UnresolvedTypeName1.proto",
        UnresolvedTypeNameException.class, ""}, new Object[] {"UnresolvedTypeName2.proto",
        UnresolvedTypeNameException.class, ""}, new Object[] {"UnresolvedTypeName3.proto",
        UnresolvedTypeNameException.class, ""}

    , new Object[] {"UnresolvedTypeName4.proto", DescriptorValidationException.class, ""},
    new Object[] {"UnresolvedTypeName5.proto", DescriptorValidationException.class, ""}

    , new Object[] {"NonUniqueExtensionName1.proto", DescriptorValidationException.class, ""},
    new Object[] {"NonUniqueExtensionNumber1.proto", NonUniqueExtensionNumber.class, ""});
  }

  @Parameter
  public String protoName;

  @Parameter(1)
  public Class<? extends RuntimeException> exception;

  @Parameter(2)
  public String exceptionExtra;

  @Before
  public void setUp() throws Exception {
    final InputStream is = getClass().getResourceAsStream(protoName);
    final ProtoLexer lexer = new ProtoLexer(new ANTLRInputStream(is));
    final CommonTokenStreamEx tokens = new CommonTokenStreamEx(lexer);
    parser = new ProtoParser(tokens);
    // parser.notifyErrorListeners("!!!BEGIN!!!!");
    parser.setBuildParseTree(true); // tell ANTLR to build a parse tree
    tree = parser.proto();
    protoParser = new ProtoFileParser(protoName);
    walker = new ParseTreeWalker();
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  public final void testParsed() throws Exception {
    try {
      walker.walk(protoParser, tree);
      final FileDescriptorProto.Builder protoBuilder =
          (FileDescriptorProto.Builder) protoParser.getParsed().getProto();
      final FileDescriptor fileDescriptor =
          FileDescriptor.buildFrom(protoBuilder.build(), new FileDescriptor[0]);
      fail(String.format("expected exception type %s with message containg '%s', but got none!",
          exception, exceptionExtra));
    } catch (final Exception e) {
      log.debug("expected exception with message '{}'", e.getMessage());
      assertThat(e, instanceOf(exception));
      assertThat(e.getMessage(), containsString(exceptionExtra));
    }
  }
}
