// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc. All rights reserved.
// http://code.google.com/p/protobuf/
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
// * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
// * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
// * Neither the name of Google Inc. nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package com.github.protobufel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import protobuf_unittest.UnittestProto.TestAllExtensions;
import protobuf_unittest.UnittestProto.TestAllTypes;
import protobuf_unittest.UnittestProto.TestEmptyMessage;
import protobuf_unittest.UnittestProto.TestPackedTypes;

import com.github.protobufel.ProtoInterfaces.IBuilder2;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.google.protobuf.TestUtil;
import com.google.protobuf.UnknownFieldSet;

/**
 * A unit test for DynamicMessage, based on the original ProtoBuf tests.
 */
@RunWith(JUnit4.class)
public class DynamicMessageTest {
  private static final Logger log = LoggerFactory.getLogger(DynamicMessageTest.class);
  private IBuilder2 builder;
  private IDynamicMessageProvider messageProvider;

  TestUtil.ReflectionTester reflectionTester = new TestUtil.ReflectionTester(
      TestAllTypes.getDescriptor(), null);

  TestUtil.ReflectionTester extensionsReflectionTester = new TestUtil.ReflectionTester(
      TestAllExtensions.getDescriptor(), TestUtil.getExtensionRegistry());
  TestUtil.ReflectionTester packedReflectionTester = new TestUtil.ReflectionTester(
      TestPackedTypes.getDescriptor(), null);

  @Before
  public void setup() {
    messageProvider = DynamicMessage.getProvider();
    builder = messageProvider.newBuilder(TestAllTypes.getDescriptor());
  }

  @Test
  public void testDynamicMessageAccessors() throws Exception {
    reflectionTester.setAllFieldsViaReflection(builder);
    final Message message = builder.build();
    reflectionTester.assertAllFieldsSetViaReflection(message);
  }

  @Test
  public void testSettersAfterBuild() throws Exception {
    final Message firstMessage = builder.build();
    // double build()
    builder.build();
    // clear() after build()
    builder.clear();
    // setters after build()
    reflectionTester.setAllFieldsViaReflection(builder);
    Message message = builder.build();
    reflectionTester.assertAllFieldsSetViaReflection(message);
    // repeated setters after build()
    reflectionTester.modifyRepeatedFieldsViaReflection(builder);
    message = builder.build();
    reflectionTester.assertRepeatedFieldsModifiedViaReflection(message);
    // firstMessage shouldn't have been modified.
    reflectionTester.assertClearViaReflection(firstMessage);
  }

  @Test
  public void testUnknownFields() throws Exception {
    final Message.Builder builder = messageProvider.newBuilder(TestEmptyMessage.getDescriptor());
    builder.setUnknownFields(UnknownFieldSet.newBuilder()
        .addField(1, UnknownFieldSet.Field.newBuilder().addVarint(1).build())
        .addField(2, UnknownFieldSet.Field.newBuilder().addFixed32(1).build()).build());
    final Message message = builder.build();
    assertEquals(2, message.getUnknownFields().asMap().size());
    // clone() with unknown fields
    final Message.Builder newBuilder = builder.clone();
    assertEquals(2, newBuilder.getUnknownFields().asMap().size());
    // clear() with unknown fields
    newBuilder.clear();
    assertTrue(newBuilder.getUnknownFields().asMap().isEmpty());
    // serialize/parse with unknown fields
    newBuilder.mergeFrom(message.toByteString());
    assertEquals(2, newBuilder.getUnknownFields().asMap().size());
  }

  @Test
  public void testDynamicMessageSettersRejectNull() throws Exception {
    reflectionTester.assertReflectionSettersRejectNull(builder);
  }

  @Test
  public void testDynamicMessageExtensionAccessors() throws Exception {
    // We don't need to extensively test DynamicMessage2's handling of
    // extensions because, frankly, it doesn't do anything special with them.
    // It treats them just like any other fields.
    final Message.Builder builder = messageProvider.newBuilder(TestAllExtensions.getDescriptor());
    extensionsReflectionTester.setAllFieldsViaReflection(builder);
    final Message message = builder.build();
    extensionsReflectionTester.assertAllFieldsSetViaReflection(message);
  }

  @Test
  public void testDynamicMessageExtensionSettersRejectNull() throws Exception {
    final Message.Builder builder = messageProvider.newBuilder(TestAllExtensions.getDescriptor());
    extensionsReflectionTester.assertReflectionSettersRejectNull(builder);
  }

  @Test
  public void testDynamicMessageRepeatedSetters() throws Exception {
    reflectionTester.setAllFieldsViaReflection(builder);
    reflectionTester.modifyRepeatedFieldsViaReflection(builder);
    final Message message = builder.build();
    reflectionTester.assertRepeatedFieldsModifiedViaReflection(message);
  }

  @Test
  public void testDynamicMessageRepeatedSettersRejectNull() throws Exception {
    reflectionTester.assertReflectionRepeatedSettersRejectNull(builder);
  }

  @Test
  public void testDynamicMessageDefaults() throws Exception {
    reflectionTester.assertClearViaReflection(messageProvider.getDefaultInstance(TestAllTypes
        .getDescriptor()));
    reflectionTester.assertClearViaReflection(messageProvider.newBuilder(
        TestAllTypes.getDescriptor()).build());
  }

  @Test
  public void testDynamicMessageSerializedSize() throws Exception {
    final TestAllTypes message = TestUtil.getAllSet();

    final Message.Builder dynamicBuilder = messageProvider.newBuilder(TestAllTypes.getDescriptor());
    reflectionTester.setAllFieldsViaReflection(dynamicBuilder);
    final Message dynamicMessage = dynamicBuilder.build();

    assertEquals(message.getSerializedSize(), dynamicMessage.getSerializedSize());
  }

  @Test
  public void testDynamicMessageSerialization() throws Exception {
    reflectionTester.setAllFieldsViaReflection(builder);
    final Message message = builder.build();

    final ByteString rawBytes = message.toByteString();
    final TestAllTypes message2 = TestAllTypes.parseFrom(rawBytes);

    TestUtil.assertAllFieldsSet(message2);

    // In fact, the serialized forms should be exactly the same, byte-for-byte.
    assertEquals(TestUtil.getAllSet().toByteString(), rawBytes);
  }

  @Test
  public void testDynamicMessageParsing() throws Exception {
    final TestAllTypes.Builder builder = TestAllTypes.newBuilder();
    TestUtil.setAllFields(builder);
    final TestAllTypes message = builder.build();

    final ByteString rawBytes = message.toByteString();

    final Message message2 = messageProvider.parseFrom(TestAllTypes.getDescriptor(), rawBytes);
    reflectionTester.assertAllFieldsSetViaReflection(message2);

    // Test Parser interface.
    final Message message3 = message2.getParserForType().parseFrom(rawBytes);
    reflectionTester.assertAllFieldsSetViaReflection(message3);
  }

  @Test
  public void testDynamicMessageExtensionParsing() throws Exception {
    final ByteString rawBytes = TestUtil.getAllExtensionsSet().toByteString();
    final Message message =
        messageProvider.parseFrom(TestAllExtensions.getDescriptor(), rawBytes,
            TestUtil.getExtensionRegistry());
    extensionsReflectionTester.assertAllFieldsSetViaReflection(message);

    // Test Parser interface.
    final Message message2 =
        message.getParserForType().parseFrom(rawBytes, TestUtil.getExtensionRegistry());
    extensionsReflectionTester.assertAllFieldsSetViaReflection(message2);
  }

  @Test
  public void testDynamicMessagePackedSerialization() throws Exception {
    final Message.Builder builder = messageProvider.newBuilder(TestPackedTypes.getDescriptor());
    packedReflectionTester.setPackedFieldsViaReflection(builder);
    final Message message = builder.build();

    final ByteString rawBytes = message.toByteString();
    final TestPackedTypes message2 = TestPackedTypes.parseFrom(rawBytes);

    TestUtil.assertPackedFieldsSet(message2);

    // In fact, the serialized forms should be exactly the same, byte-for-byte.
    assertEquals(TestUtil.getPackedSet().toByteString(), rawBytes);
  }

  @Test
  public void testDynamicMessagePackedParsing() throws Exception {
    final TestPackedTypes.Builder builder = TestPackedTypes.newBuilder();
    TestUtil.setPackedFields(builder);
    final TestPackedTypes message = builder.build();

    final ByteString rawBytes = message.toByteString();

    final Message message2 = messageProvider.parseFrom(TestPackedTypes.getDescriptor(), rawBytes);
    packedReflectionTester.assertPackedFieldsSetViaReflection(message2);

    // Test Parser interface.
    final Message message3 = message2.getParserForType().parseFrom(rawBytes);
    packedReflectionTester.assertPackedFieldsSetViaReflection(message3);
  }

  @Test
  public void testDynamicMessageCopy() throws Exception {
    final TestAllTypes.Builder builder = TestAllTypes.newBuilder();
    TestUtil.setAllFields(builder);
    final TestAllTypes message = builder.build();

    final Message copy = messageProvider.newBuilder(message).build();
    reflectionTester.assertAllFieldsSetViaReflection(copy);
  }

  @Test
  public void testToBuilder() throws Exception {
    final IBuilder2 builder = messageProvider.newBuilder(TestAllTypes.getDescriptor());
    reflectionTester.setAllFieldsViaReflection(builder);
    final int unknownFieldNum = 9;
    final long unknownFieldVal = 90;
    builder.setUnknownFields(UnknownFieldSet
        .newBuilder()
        .addField(unknownFieldNum,
            UnknownFieldSet.Field.newBuilder().addVarint(unknownFieldVal).build()).build());
    final Message message = builder.build();

    final Message derived = message.toBuilder().build();
    reflectionTester.assertAllFieldsSetViaReflection(derived);
    assertEquals(Arrays.asList(unknownFieldVal),
        derived.getUnknownFields().getField(unknownFieldNum).getVarintList());
  }
}
