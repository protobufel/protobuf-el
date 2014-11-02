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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import protobuf_unittest.UnittestProto.ForeignMessage;
import protobuf_unittest.UnittestProto.TestAllTypes;
import protobuf_unittest.UnittestProto.TestRequired;
import protobuf_unittest.UnittestProto.TestRequiredForeign;

import com.github.protobufel.ProtoInterfaces.IBuilder2;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.UninitializedMessageException;

/**
 * Misc. unit tests for message operations that apply to both generated and dynamic messages.
 *
 * based on kenton@google.com Kenton Varda work
 */
@RunWith(JUnit4.class)
public class MessageTest {
  private static final Logger log = LoggerFactory.getLogger(MessageTest.class);
  // =================================================================
  // Message-merging tests.

  static final TestAllTypes MERGE_SOURCE = TestAllTypes.newBuilder().setOptionalInt32(1)
      .setOptionalString("foo").setOptionalForeignMessage(ForeignMessage.getDefaultInstance())
      .addRepeatedString("bar").build();

  static final TestAllTypes MERGE_DEST = TestAllTypes.newBuilder().setOptionalInt64(2)
      .setOptionalString("baz")
      .setOptionalForeignMessage(ForeignMessage.newBuilder().setC(3).build())
      .addRepeatedString("qux").build();

  static final String MERGE_RESULT_TEXT = "optional_int32: 1\n" + "optional_int64: 2\n"
      + "optional_string: \"foo\"\n" + "optional_foreign_message {\n" + "  c: 3\n" + "}\n"
      + "repeated_string: \"qux\"\n" + "repeated_string: \"bar\"\n";

  public IDynamicMessageProvider messageProvider;

  private static final Descriptor TEST_REQUIRED_FOREIGN_DESC = TestRequiredForeign.getDescriptor();
  private final FieldDescriptor fdOptionalMessage = TEST_REQUIRED_FOREIGN_DESC
      .findFieldByNumber(TestRequiredForeign.OPTIONAL_MESSAGE_FIELD_NUMBER);
  private final FieldDescriptor fdRepeatedMessage = TEST_REQUIRED_FOREIGN_DESC
      .findFieldByNumber(TestRequiredForeign.REPEATED_MESSAGE_FIELD_NUMBER);

  @Before
  public void init() {
    messageProvider = DynamicMessage.getProvider();
  }

  @Test
  public void testMergeFrom() throws Exception {
    final Message result = messageProvider.newBuilder(MERGE_DEST).mergeFrom(MERGE_SOURCE).build();
    assertEquals(MERGE_RESULT_TEXT, result.toString());
  }

  /**
   * Test merging a DynamicMessage2 into a GeneratedMessage. As long as they have the same
   * descriptor, this should work, but it is an entirely different code path.
   */
  @Test
  public void testMergeFromDynamic() throws Exception {
    final TestAllTypes result =
        TestAllTypes.newBuilder(MERGE_DEST)
            .mergeFrom(messageProvider.newBuilder(MERGE_SOURCE).build()).build();

    assertEquals(MERGE_RESULT_TEXT, result.toString());
  }

  /** Test merging two TestSupport. */
  @Test
  public void testDynamicMergeFrom() throws Exception {
    final Message result =
        messageProvider.newBuilder(MERGE_DEST)
            .mergeFrom(messageProvider.newBuilder(MERGE_SOURCE).build()).build();

    assertEquals(MERGE_RESULT_TEXT, result.toString());
  }

  // =================================================================
  // Required-field-related tests.

  private static final Descriptor TEST_REQUIRED_UNINITIALIZED_DESC = TestRequired.getDescriptor();
  private static final TestRequired TEST_REQUIRED_UNINITIALIZED = TestRequired.getDefaultInstance();
  private static final TestRequired TEST_REQUIRED_INITIALIZED = TestRequired.newBuilder().setA(1)
      .setB(2).setC(3).build();

  @Test
  public void testRequiredDynamic() throws Exception {
    final Descriptors.Descriptor descriptor = TestRequired.getDescriptor();
    final Message.Builder builder = messageProvider.newBuilder(descriptor);

    assertFalse(builder.isInitialized());
    builder.setField(descriptor.findFieldByName("a"), 1);
    assertFalse(builder.isInitialized());
    builder.setField(descriptor.findFieldByName("b"), 1);
    assertFalse(builder.isInitialized());
    builder.setField(descriptor.findFieldByName("c"), 1);
    assertTrue(builder.isInitialized());
  }

  @Test
  public void testRequiredDynamicForeign() throws Exception {
    final Descriptors.Descriptor descriptor = TEST_REQUIRED_FOREIGN_DESC;
    final IBuilder2 builder = messageProvider.newBuilder(descriptor);

    assertTrue(builder.isInitialized());

    builder.setField(descriptor.findFieldByName("optional_message"), TEST_REQUIRED_UNINITIALIZED);
    assertFalse(builder.isInitialized());

    builder.setField(descriptor.findFieldByName("optional_message"), TEST_REQUIRED_INITIALIZED);
    assertTrue(builder.isInitialized());

    builder.addRepeatedField(descriptor.findFieldByName("repeated_message"),
        TEST_REQUIRED_UNINITIALIZED);
    assertFalse(builder.isInitialized());

    builder.setRepeatedField(descriptor.findFieldByName("repeated_message"), 0,
        TEST_REQUIRED_INITIALIZED);
    assertTrue(builder.isInitialized());
  }

  @Test
  public void testUninitializedException() throws Exception {
    try {
      messageProvider.newBuilder(TEST_REQUIRED_UNINITIALIZED_DESC).build();
      fail("Should have thrown an exception.");
    } catch (final UninitializedMessageException e) {
      assertEquals("Message missing required fields: a, b, c", e.getMessage());
    }
  }

  @Test
  public void testBuildPartial() throws Exception {
    // We're mostly testing that no exception is thrown.
    final Message message =
        messageProvider.newBuilder(TEST_REQUIRED_UNINITIALIZED_DESC).buildPartial();
    assertFalse(message.isInitialized());
  }

  @Test
  public void testNestedUninitializedException() throws Exception {
    try {
      messageProvider.newBuilder(TEST_REQUIRED_FOREIGN_DESC)
      .setField(fdOptionalMessage, TEST_REQUIRED_UNINITIALIZED)
      .addRepeatedField(fdRepeatedMessage, TEST_REQUIRED_UNINITIALIZED)
      .addRepeatedField(fdRepeatedMessage, TEST_REQUIRED_UNINITIALIZED).build();
      fail("Should have thrown an exception.");
    } catch (final UninitializedMessageException e) {
      assertEquals("Message missing required fields: " + "optional_message.a, "
          + "optional_message.b, " + "optional_message.c, " + "repeated_message[0].a, "
          + "repeated_message[0].b, " + "repeated_message[0].c, " + "repeated_message[1].a, "
          + "repeated_message[1].b, " + "repeated_message[1].c", e.getMessage());
    }
  }

  @Test
  public void testBuildNestedPartial() throws Exception {
    // We're mostly testing that no exception is thrown.
    final Message message =
        messageProvider.newBuilder(TEST_REQUIRED_FOREIGN_DESC)
            .setField(fdOptionalMessage, TEST_REQUIRED_UNINITIALIZED)
            .addRepeatedField(fdRepeatedMessage, TEST_REQUIRED_UNINITIALIZED)
            .addRepeatedField(fdRepeatedMessage, TEST_REQUIRED_UNINITIALIZED).buildPartial();

    assertFalse(message.isInitialized());
  }

  @Test
  public void testParseUnititialized() throws Exception {
    try {
      messageProvider.parseFrom(TEST_REQUIRED_UNINITIALIZED_DESC, ByteString.EMPTY);
      fail("Should have thrown an exception.");
    } catch (final InvalidProtocolBufferException e) {
      assertEquals("Message missing required fields: a, b, c", e.getMessage());
    }
  }

  @Test
  public void testParseNestedUnititialized() throws Exception {
    final ByteString data =
        messageProvider.newBuilder(TEST_REQUIRED_FOREIGN_DESC)
            .setField(fdOptionalMessage, TEST_REQUIRED_UNINITIALIZED)
            .addRepeatedField(fdRepeatedMessage, TEST_REQUIRED_UNINITIALIZED)
            .addRepeatedField(fdRepeatedMessage, TEST_REQUIRED_UNINITIALIZED).buildPartial()
            .toByteString();

    try {
      messageProvider.parseFrom(TEST_REQUIRED_FOREIGN_DESC, data);
      fail("Should have thrown an exception.");
    } catch (final InvalidProtocolBufferException e) {
      assertEquals("Message missing required fields: " + "optional_message.a, "
          + "optional_message.b, " + "optional_message.c, " + "repeated_message[0].a, "
          + "repeated_message[0].b, " + "repeated_message[0].c, " + "repeated_message[1].a, "
          + "repeated_message[1].b, " + "repeated_message[1].c", e.getMessage());
    }
  }

  @Test
  public void testDynamicUninitializedException() throws Exception {
    try {
      messageProvider.newBuilder(TestRequired.getDescriptor()).build();
      fail("Should have thrown an exception.");
    } catch (final UninitializedMessageException e) {
      assertEquals("Message missing required fields: a, b, c", e.getMessage());
    }
  }

  @Test
  public void testDynamicBuildPartial() throws Exception {
    // We're mostly testing that no exception is thrown.
    final Message message = messageProvider.newBuilder(TestRequired.getDescriptor()).buildPartial();
    assertFalse(message.isInitialized());
  }

  @Test
  public void testDynamicParseUnititialized() throws Exception {
    try {
      final Descriptors.Descriptor descriptor = TestRequired.getDescriptor();
      messageProvider.parseFrom(descriptor, ByteString.EMPTY);
      fail("Should have thrown an exception.");
    } catch (final InvalidProtocolBufferException e) {
      assertEquals("Message missing required fields: a, b, c", e.getMessage());
    }
  }

  /** Test reading unset repeated message from DynamicMessage2. */
  @Test
  public void testDynamicRepeatedMessageNull() throws Exception {
    final Message result =
        messageProvider.newBuilder(TestAllTypes.getDescriptor())
            .mergeFrom(messageProvider.newBuilder(MERGE_SOURCE).build()).build();

    assertTrue(result.getField(result.getDescriptorForType().findFieldByName(
        "repeated_foreign_message")) instanceof List<?>);
    assertEquals(
        result.getRepeatedFieldCount(result.getDescriptorForType().findFieldByName(
            "repeated_foreign_message")), 0);
  }

  /** Test reading repeated message from DynamicMessage2. */
  @Test
  public void testDynamicRepeatedMessageNotNull() throws Exception {
    final TestAllTypes REPEATED_NESTED =
        TestAllTypes.newBuilder().setOptionalInt32(1).setOptionalString("foo")
            .setOptionalForeignMessage(ForeignMessage.getDefaultInstance())
            .addRepeatedString("bar")
            .addRepeatedForeignMessage(ForeignMessage.getDefaultInstance())
            .addRepeatedForeignMessage(ForeignMessage.getDefaultInstance()).build();
    final Message result =
        messageProvider.newBuilder(TestAllTypes.getDescriptor())
            .mergeFrom(messageProvider.newBuilder(REPEATED_NESTED).build()).build();

    assertTrue(result.getField(result.getDescriptorForType().findFieldByName(
        "repeated_foreign_message")) instanceof List<?>);
    assertEquals(
        result.getRepeatedFieldCount(result.getDescriptorForType().findFieldByName(
            "repeated_foreign_message")), 2);
  }
}
