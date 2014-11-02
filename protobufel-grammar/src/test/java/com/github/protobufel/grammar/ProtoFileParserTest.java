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

import static com.github.protobufel.grammar.Misc.getProtocFileDescriptorProto;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

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

import com.github.protobufel.grammar.Misc.FieldTypeRefsMode;
import com.github.protobufel.grammar.ParserUtils.CommonTokenStreamEx;
import com.github.protobufel.grammar.ProtoParser.ProtoContext;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.FileDescriptor;

// TODO move the relevant tests from ProtoFilesTest and enable
@Ignore
@RunWith(Parameterized.class)
public class ProtoFileParserTest {
  private static final String PROTOC_SUBDIR = "protoc/";
  private static final Logger log = LoggerFactory.getLogger(ProtoFileParserTest.class);
  private static final List<String> TEST_PROTOS = ImmutableList.of("MessageExtendee.proto"
  // ,"simple1.proto"
  // ,"test1.proto"
  // , "unittest_custom_options.proto"
      );
  private ParseTreeWalker walker;
  private ProtoFileParser protoParser;
  private ProtoContext tree;
  private ProtoParser parser;
  private FileDescriptorProto originalProto;

  @Parameters(name = "{index}:{1}{0}")
  public static Collection<Object[]> data() {
    final ImmutableList.Builder<Object[]> builder = ImmutableList.builder();

    for (final String fileName : TEST_PROTOS) {
      builder.add(new Object[] {fileName, PROTOC_SUBDIR}).add(new Object[] {fileName, ""});
    }

    return builder.build();
  }

  @Parameter
  public String protoName;

  @Parameter(1)
  public String subDir;

  @Before
  public void setUp() throws Exception {
    originalProto = getProtocFileDescriptorProto(protoName, false, FieldTypeRefsMode.AS_IS);
    final InputStream is = getClass().getResourceAsStream(subDir + protoName);
    final ProtoLexer lexer = new ProtoLexer(new ANTLRInputStream(is));
    final CommonTokenStreamEx tokens = new CommonTokenStreamEx(lexer);
    parser = new ProtoParser(tokens);
    parser.setBuildParseTree(true); // tell ANTLR to build a parse tree
    tree = parser.proto();
    protoParser = new ProtoFileParser(protoName);
    walker = new ParseTreeWalker();
  }

  @After
  public void tearDown() throws Exception {}

  @Ignore
  @Test
  public final void testParsed() throws Exception {
    walker.walk(protoParser, tree);
    final FileDescriptorProto proto =
        ((FileDescriptorProto.Builder) protoParser.getParsed().getProto()).build();
    // log.debug(proto.toString());
    assertThat(proto, equalTo(originalProto));

    // log.debug(tree.toStringTree(parser));
    // assert that protoc compiled GeneratedMessage.getDescriptor().getFile().toProto() equals to
    // our proto!
  }

  @Test
  public final void testParsedPhase2() throws Exception {
    walker.walk(protoParser, tree);
    final FileDescriptorProto.Builder protoBuilder =
        (FileDescriptorProto.Builder) protoParser.getParsed().getProto();
    final FileDescriptorProto proto = protoBuilder.build();
    log.debug("1st phase proto: \n{}\n", proto);
    final FileDescriptor fileDescriptor = FileDescriptor.buildFrom(proto, new FileDescriptor[0]);
    final FileDescriptorProto proto2 = fileDescriptor.toProto();
    assertThat(proto2, equalTo(originalProto));

    // log.debug(tree.toStringTree(parser));
    // assert that protoc compiled GeneratedMessage.getDescriptor().getFile().toProto() equals to
    // our proto!
  }
}
