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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.TextFormat;
import com.google.protobuf.TextFormat.ParseException;

public class AggregateValueTextFormatTest {

  private static final String NL = System.lineSeparator();

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testMergeCharSequenceExtensionRegistryBuilder() throws ParseException,
      URISyntaxException {
    final Map<String, FileDescriptor> info1 =
        ProtoFiles
            .newBuilder()
            .addFiles(new File(getClass().getResource("protoc-misc/").toURI()),
                "DeepAggregateOptionValue1.proto").build();

    final String protoSource =
        new StringBuilder().append("message Message1 {").append(NL)
            .append("  optional string f1 = 1;").append(NL).append("  optional Message2 f2 = 2;")
            .append(NL).append("}").append(NL).append("").append(NL).append("message Message2 {")
            .append(NL).append(" optional string f21 = 1;").append(NL)
            .append(" optional string f22 = 2;").append(NL).append("}").append(NL).toString();

    final Map<String, FileDescriptor> info =
        ProtoFiles.newBuilder().addSource("DeepAggregateOptionValue1.proto", protoSource).build();
    final Descriptor type =
        info.get("DeepAggregateOptionValue1.proto").findMessageTypeByName("Message1");

    final FieldDescriptor f1FD = type.findFieldByName("f1");
    final FieldDescriptor f2FD = type.findFieldByName("f2");
    final Descriptor type2 = f2FD.getMessageType();

    final DynamicMessage expected =
        DynamicMessage
            .newBuilder(type)
            .setField(f1FD, "hello")
            .setField(
                f2FD,
                DynamicMessage.newBuilder(type2).setField(type2.findFieldByName("f21"), "deep1")
                    .setField(type2.findFieldByName("f22"), "deep2").build()).build();

    final DynamicMessage.Builder builder = DynamicMessage.newBuilder(type);
    final String input = "f1:'hello' f2 {f21:'deep1' f22:'deep2'}";
    final ExtensionRegistry extensionRegistry = ExtensionRegistry.getEmptyRegistry();

    TextFormat.merge(input, extensionRegistry, builder);
    final DynamicMessage actual = builder.build();

    assertThat(actual, equalTo(expected));
  }
}
