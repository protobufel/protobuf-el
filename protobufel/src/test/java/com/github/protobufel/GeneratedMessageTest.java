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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import protobuf_unittest.UnittestProto.ForeignEnum;
import protobuf_unittest.UnittestProto.ForeignMessage;
import protobuf_unittest.UnittestProto.TestAllTypes;
import protobuf_unittest.UnittestProto.TestAllTypes.NestedMessage;

import com.github.protobufel.ProtoInterfaces.IBuilder2;
import com.github.protobufel.ProtoInterfaces.IMessageEx;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.TestUtil;
import com.google.protobuf.test.UnittestImport;

/**
 * Unit test for generated messages and generated code. See also {@link MessageTest}, which tests
 * some generated message functionality.
 *
 * based on kenton@google.com Kenton Varda work
 */
@RunWith(JUnit4.class)
public class GeneratedMessageTest {
  private final Descriptor nestedMessageDescriptor = NestedMessage.getDescriptor();
  private final Descriptor allTypesDescriptor = TestAllTypes.getDescriptor();

  private final FieldDescriptor fdOptionalInt32 = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.OPTIONAL_INT32_FIELD_NUMBER);
  private final FieldDescriptor fdOptionalNestedEnum = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.OPTIONAL_NESTED_ENUM_FIELD_NUMBER);
  private final FieldDescriptor fdOptionalNestedMessage = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.OPTIONAL_NESTED_MESSAGE_FIELD_NUMBER);
  private final FieldDescriptor fdInt32 = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.REPEATED_INT32_FIELD_NUMBER);
  private final FieldDescriptor fdNestedEnum = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.REPEATED_NESTED_ENUM_FIELD_NUMBER);
  private final FieldDescriptor fdNestedMessage = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.REPEATED_NESTED_MESSAGE_FIELD_NUMBER);
  private final FieldDescriptor fdForeignMessage = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.REPEATED_FOREIGN_MESSAGE_FIELD_NUMBER);
  private final FieldDescriptor fdBb = nestedMessageDescriptor
      .findFieldByNumber(NestedMessage.BB_FIELD_NUMBER);
  private final FieldDescriptor fdOptionalForeignMessage = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.OPTIONAL_FOREIGN_MESSAGE_FIELD_NUMBER);

  private MockBuilderParent mockParent;
  private IBuilder2 builder;

  public IDynamicMessageProvider messageProvider;

  private final FieldDescriptor fdImportEnum = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.REPEATED_IMPORT_ENUM_FIELD_NUMBER);
  private final FieldDescriptor fdFloat = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.REPEATED_FLOAT_FIELD_NUMBER);
  private final FieldDescriptor fdInt64 = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.REPEATED_INT64_FIELD_NUMBER);
  private final FieldDescriptor fdUint32 = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.REPEATED_UINT32_FIELD_NUMBER);
  private final FieldDescriptor fdUint64 = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.REPEATED_UINT64_FIELD_NUMBER);
  private final FieldDescriptor fdSint32 = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.REPEATED_SINT32_FIELD_NUMBER);
  private final FieldDescriptor fdSint64 = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.REPEATED_SINT64_FIELD_NUMBER);
  private final FieldDescriptor fdFixed32 = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.REPEATED_FIXED32_FIELD_NUMBER);
  private final FieldDescriptor fdSfixed32 = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.REPEATED_SINT32_FIELD_NUMBER);
  private final FieldDescriptor fdFixed64 = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.REPEATED_FIXED64_FIELD_NUMBER);
  private final FieldDescriptor fdSfixed64 = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.REPEATED_SFIXED64_FIELD_NUMBER);
  private final FieldDescriptor fdDouble = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.REPEATED_DOUBLE_FIELD_NUMBER);
  private final FieldDescriptor fdBool = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.REPEATED_BOOL_FIELD_NUMBER);
  private final FieldDescriptor fdString = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.REPEATED_STRING_FIELD_NUMBER);
  private final FieldDescriptor fdBytes = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.REPEATED_BYTES_FIELD_NUMBER);
  private final FieldDescriptor fdGroup = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.REPEATEDGROUP_FIELD_NUMBER);
  private final FieldDescriptor fdImportMessage = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.REPEATED_IMPORT_MESSAGE_FIELD_NUMBER);
  private final FieldDescriptor fdForeignEnum = allTypesDescriptor
      .findFieldByNumber(TestAllTypes.REPEATED_FOREIGN_ENUM_FIELD_NUMBER);

  @Before
  public void setup() {
    messageProvider = DynamicMessage.getProvider();
    mockParent = new MockBuilderParent();
    builder = messageProvider.newBuilder(allTypesDescriptor);
  }

  @After
  public void tearDown() {
    DynamicMessage.disableAlwaysUseFieldBuildersForTesting();
  }
  
  @Test
  public void testDefaultInstance() throws Exception {
    assertEquals(TestAllTypes.getDefaultInstance(),
        messageProvider.getDefaultInstance(allTypesDescriptor));
    assertEquals(messageProvider.getDefaultInstance(allTypesDescriptor), messageProvider
        .getDefaultInstance(allTypesDescriptor).getDefaultInstanceForType());
    assertEquals(messageProvider.getDefaultInstance(allTypesDescriptor), messageProvider
        .newBuilder(allTypesDescriptor).getDefaultInstanceForType());
  }

  @Test
  public void testProtosShareRepeatedArraysIfDidntChange() throws Exception {
    builder.addRepeatedField(fdInt32, 100);
    builder.addRepeatedField(fdImportEnum, UnittestImport.ImportEnum.IMPORT_BAR);
    builder.addRepeatedField(fdForeignMessage, ForeignMessage.getDefaultInstance());

    final Message value1 = builder.build();
    final Message value2 = value1.toBuilder().build();

    assertEquals(value1.getField(fdInt32), value2.getField(fdInt32));
    assertEquals(value1.getField(fdImportEnum), value2.getField(fdImportEnum));
    assertEquals(value1.getField(fdForeignMessage), value2.getField(fdForeignMessage));
  }

  @Test
  public void testRepeatedArraysAreImmutable() throws Exception {
    builder.addRepeatedField(fdInt32, 100);
    builder.addRepeatedField(fdImportEnum, UnittestImport.ImportEnum.IMPORT_BAR);
    builder.addRepeatedField(fdForeignMessage, ForeignMessage.getDefaultInstance());
    assertIsUnmodifiable(builder.getField(fdInt32));
    assertIsUnmodifiable(builder.getField(fdImportEnum));
    assertIsUnmodifiable(builder.getField(fdForeignMessage));
    assertIsUnmodifiable(builder.getField(fdFloat));


    final Message value = builder.build();
    assertIsUnmodifiable(value.getField(fdInt32));
    assertIsUnmodifiable(value.getField(fdImportEnum));
    assertIsUnmodifiable(value.getField(fdForeignMessage));
    assertIsUnmodifiable(value.getField(fdFloat));
  }

  @Test
  public void testParsedMessagesAreImmutable() throws Exception {
    final Message value =
        messageProvider.parseFrom(allTypesDescriptor, TestUtil.getAllSet().toByteString());
    assertIsUnmodifiable(value.getField(fdInt32));
    assertIsUnmodifiable(value.getField(fdInt64));
    assertIsUnmodifiable(value.getField(fdUint32));
    assertIsUnmodifiable(value.getField(fdUint64));
    assertIsUnmodifiable(value.getField(fdSint32));
    assertIsUnmodifiable(value.getField(fdSint64));
    assertIsUnmodifiable(value.getField(fdFixed32));
    assertIsUnmodifiable(value.getField(fdFixed64));
    assertIsUnmodifiable(value.getField(fdSfixed32));
    assertIsUnmodifiable(value.getField(fdSfixed64));
    assertIsUnmodifiable(value.getField(fdFloat));
    assertIsUnmodifiable(value.getField(fdDouble));
    assertIsUnmodifiable(value.getField(fdBool));
    assertIsUnmodifiable(value.getField(fdString));
    assertIsUnmodifiable(value.getField(fdBytes));
    assertIsUnmodifiable(value.getField(fdGroup));
    assertIsUnmodifiable(value.getField(fdNestedMessage));
    assertIsUnmodifiable(value.getField(fdForeignMessage));
    assertIsUnmodifiable(value.getField(fdImportMessage));
    assertIsUnmodifiable(value.getField(fdNestedEnum));
    assertIsUnmodifiable(value.getField(fdForeignEnum));
    assertIsUnmodifiable(value.getField(fdImportEnum));
  }

  public static void assertMessageUnmodifiable(final Message message) {
    for (final Entry<FieldDescriptor, Object> entry : message.getAllFields().entrySet()) {
      final FieldDescriptor field = entry.getKey();

      if (field.isRepeated()) {
        assertThat(Collections.unmodifiableList(Collections.emptyList()).getClass()
            .isAssignableFrom(entry.getValue().getClass()), is(true));
      }
    }
  }

  private void assertIsUnmodifiable(final Object object) {
    final List<?> list = List.class.cast(object);

    if (list == Collections.emptyList()) {
      // OKAY -- Need to check this b/c EmptyList allows you to call clear.
    } else {
      try {
        list.clear();
        fail("List wasn't immutable");
      } catch (final UnsupportedOperationException e) {
        // good
      }
    }
  }

  @Test
  public void testRepeatedAppend() throws Exception {
    builder.addAllRepeatedField(fdInt32, Arrays.asList(1, 2, 3, 4));
    builder.addAllRepeatedField(fdForeignEnum, Arrays.asList(ForeignEnum.FOREIGN_BAZ));

    final ForeignMessage foreignMessage = ForeignMessage.newBuilder().setC(12).build();

    builder.addAllRepeatedField(fdForeignMessage, Arrays.asList(foreignMessage));

    final IMessageEx message = builder.build();

    assertEquals(message.getField(fdInt32), Arrays.asList(1, 2, 3, 4));
    @SuppressWarnings("unchecked")
    final Iterable<EnumValueDescriptor> actualIterable =
        (Iterable<EnumValueDescriptor>) message.getField(fdForeignEnum);
    assertThat(actualIterable, hasItems(ForeignEnum.FOREIGN_BAZ.getValueDescriptor()));
    assertEquals(1, message.getRepeatedFieldCount(fdForeignMessage));
    assertEquals(12,
        ((Message) message.getRepeatedField(fdForeignMessage, 0)).getField(ForeignMessage
            .getDescriptor().findFieldByNumber(ForeignMessage.C_FIELD_NUMBER)));
  }

  @Test
  public void testRepeatedAppendRejectsNull() throws Exception {
    final ForeignMessage foreignMessage = ForeignMessage.newBuilder().setC(12).build();

    try {
      builder.addAllRepeatedField(fdForeignMessage,
          Arrays.asList(foreignMessage, (ForeignMessage) null));
      fail("Exception was not thrown");
    } catch (final NullPointerException e) {
      // We expect this exception.
    }

    try {
      builder.addAllRepeatedField(fdForeignEnum, Arrays.asList(ForeignEnum.FOREIGN_BAZ, null));
      fail("Exception was not thrown");
    } catch (final NullPointerException e) {
      // We expect this exception.
    }

    try {
      builder.addAllRepeatedField(fdString, Arrays.asList("one", null));
      fail("Exception was not thrown");
    } catch (final NullPointerException e) {
      // We expect this exception.
    }

    try {
      builder.addAllRepeatedField(fdBytes, Arrays.asList(TestUtil.toBytes("one"), null));
      fail("Exception was not thrown");
    } catch (final NullPointerException e) {
      // We expect this exception.
    }
  }

  @Test
  public void testSettingForeignMessageUsingBuilder() throws Exception {
    final TestAllTypes message = TestAllTypes.newBuilder()
    // Pass builder for foreign message instance.
        .setOptionalForeignMessage(ForeignMessage.newBuilder().setC(123)).build();
    final TestAllTypes expectedMessage = TestAllTypes.newBuilder()
    // Create expected version passing foreign message instance explicitly.
        .setOptionalForeignMessage(ForeignMessage.newBuilder().setC(123).build()).build();
    // TODO(ngd): Upgrade to using real #equals method once implemented
    assertEquals(expectedMessage.toString(), message.toString());
  }

  @Test
  public void testSettingRepeatedForeignMessageUsingBuilder() throws Exception {
    final TestAllTypes message = TestAllTypes.newBuilder()
    // Pass builder for foreign message instance.
        .addRepeatedForeignMessage(ForeignMessage.newBuilder().setC(456)).build();
    final TestAllTypes expectedMessage = TestAllTypes.newBuilder()
    // Create expected version passing foreign message instance explicitly.
        .addRepeatedForeignMessage(ForeignMessage.newBuilder().setC(456).build()).build();
    assertEquals(expectedMessage.toString(), message.toString());
  }


  @Test
  public void testInvalidations() throws Exception {
    DynamicMessage.enableAlwaysUseFieldBuildersForTesting();
    final TestAllTypes.NestedMessage nestedMessage1 =
        TestAllTypes.NestedMessage.newBuilder().build();
    final TestAllTypes.NestedMessage nestedMessage2 =
        TestAllTypes.NestedMessage.newBuilder().build();

    // Set all three flavors (enum, primitive, message and singular/repeated)
    // and verify no invalidations fired

    final IBuilder2 builder = DynamicMessage.newBuilder(allTypesDescriptor, mockParent);
    //final IBuilder2 builder = newBuilderForTesting(messageProvider, allTypesDescriptor, null);

    builder.setField(fdOptionalInt32, 1);
    builder.setField(fdOptionalNestedEnum, TestAllTypes.NestedEnum.BAR);
    builder.setField(fdOptionalNestedMessage, nestedMessage1);
    builder.addRepeatedField(fdInt32, 1);
    builder.addRepeatedField(fdNestedEnum, TestAllTypes.NestedEnum.BAR);
    builder.addRepeatedField(fdNestedMessage, nestedMessage1);
    assertEquals(0, mockParent.getInvalidationCount());

    // Now tell it we want changes and make sure it's only fired once
    // And do this for each flavor

    // primitive single
    builder.buildPartial();
    builder.setField(fdOptionalInt32, 2);
    builder.setField(fdOptionalInt32, 3);
    assertEquals(1, mockParent.getInvalidationCount());

    // enum single
    builder.buildPartial();
    builder.setField(fdOptionalNestedEnum, TestAllTypes.NestedEnum.BAZ);
    builder.setField(fdOptionalNestedEnum, TestAllTypes.NestedEnum.BAR);
    assertEquals(2, mockParent.getInvalidationCount());

    // message single
    builder.buildPartial();
    builder.setField(fdOptionalNestedMessage, nestedMessage2);
    builder.setField(fdOptionalNestedMessage, nestedMessage1);
    assertEquals(3, mockParent.getInvalidationCount());

    // primitive repeated
    builder.buildPartial();
    builder.addRepeatedField(fdInt32, 2);
    builder.addRepeatedField(fdInt32, 3);
    assertEquals(4, mockParent.getInvalidationCount());

    // enum repeated
    builder.buildPartial();
    builder.addRepeatedField(fdNestedEnum, TestAllTypes.NestedEnum.BAZ);
    builder.addRepeatedField(fdNestedEnum, TestAllTypes.NestedEnum.BAZ);
    assertEquals(5, mockParent.getInvalidationCount());

    // message repeated
    builder.buildPartial();
    builder.addRepeatedField(fdNestedMessage, nestedMessage2);
    builder.addRepeatedField(fdNestedMessage, nestedMessage1);
    assertEquals(6, mockParent.getInvalidationCount());
  }

  @Test
  public void testMessageOrBuilderGetters() {
    // single fields
    assertEquals(ForeignMessage.getDefaultInstance(),
        builder.getMessageOrBuilder(fdOptionalForeignMessage));
    final IBuilder2 subBuilder = builder.getFieldBuilder(fdOptionalForeignMessage);
    assertSame(subBuilder, builder.getMessageOrBuilder(fdOptionalForeignMessage));

    // repeated fields
    final ForeignMessage m0 = ForeignMessage.newBuilder().buildPartial();
    final ForeignMessage m1 = ForeignMessage.newBuilder().buildPartial();
    final ForeignMessage m2 = ForeignMessage.newBuilder().buildPartial();
    builder.addRepeatedField(fdForeignMessage, m0);
    builder.addRepeatedField(fdForeignMessage, m1);
    builder.addRepeatedField(fdForeignMessage, m2);
    assertSame(m0, builder.getMessageOrBuilder(fdForeignMessage, 0));
    assertSame(m1, builder.getMessageOrBuilder(fdForeignMessage, 1));
    assertSame(m2, builder.getMessageOrBuilder(fdForeignMessage, 2));
    final IBuilder2 b0 = builder.getFieldBuilder(fdForeignMessage, 0);
    final IBuilder2 b1 = builder.getFieldBuilder(fdForeignMessage, 1);
    assertSame(b0, builder.getMessageOrBuilder(fdForeignMessage, 0));
    assertSame(b1, builder.getMessageOrBuilder(fdForeignMessage, 1));
    assertSame(m2, builder.getMessageOrBuilder(fdForeignMessage, 2));

    final List<? extends MessageOrBuilder> messageOrBuilderList =
        builder.getMessageOrBuilderList(fdForeignMessage);
    assertSame(b0, messageOrBuilderList.get(0));
    assertSame(b1, messageOrBuilderList.get(1));
    assertSame(m2, messageOrBuilderList.get(2));
  }

  @Test
  public void testGetFieldBuilder() {
    final Descriptor descriptor = allTypesDescriptor;

    final FieldDescriptor fieldDescriptor = descriptor.findFieldByName("optional_nested_message");
    final FieldDescriptor foreignFieldDescriptor =
        descriptor.findFieldByName("optional_foreign_message");
    final FieldDescriptor importFieldDescriptor =
        descriptor.findFieldByName("optional_import_message");

    // Mutate the message with new field builder
    // Mutate nested message
    final IBuilder2 builder1 = messageProvider.newBuilder(allTypesDescriptor);
    final Message.Builder fieldBuilder1 =
        builder1.newBuilderForField(fieldDescriptor).mergeFrom(
            (Message) builder1.getField(fieldDescriptor));
    final FieldDescriptor subFieldDescriptor1 =
        fieldBuilder1.getDescriptorForType().findFieldByName("bb");
    fieldBuilder1.setField(subFieldDescriptor1, 1);
    builder1.setField(fieldDescriptor, fieldBuilder1.build());

    // Mutate foreign message
    final Message.Builder foreignFieldBuilder1 =
        builder1.newBuilderForField(foreignFieldDescriptor).mergeFrom(
            (Message) builder1.getField(foreignFieldDescriptor));
    final FieldDescriptor subForeignFieldDescriptor1 =
        foreignFieldBuilder1.getDescriptorForType().findFieldByName("c");
    foreignFieldBuilder1.setField(subForeignFieldDescriptor1, 2);
    builder1.setField(foreignFieldDescriptor, foreignFieldBuilder1.build());

    // Mutate import message
    final Message.Builder importFieldBuilder1 =
        builder1.newBuilderForField(importFieldDescriptor).mergeFrom(
            (Message) builder1.getField(importFieldDescriptor));
    final FieldDescriptor subImportFieldDescriptor1 =
        importFieldBuilder1.getDescriptorForType().findFieldByName("d");
    importFieldBuilder1.setField(subImportFieldDescriptor1, 3);
    builder1.setField(importFieldDescriptor, importFieldBuilder1.build());

    final Message newMessage1 = builder1.build();

    // Mutate the message with existing field builder
    // Mutate nested message
    final IBuilder2 builder2 = messageProvider.newBuilder(allTypesDescriptor);
    final Message.Builder fieldBuilder2 = builder2.getFieldBuilder(fieldDescriptor);
    final FieldDescriptor subFieldDescriptor2 =
        fieldBuilder2.getDescriptorForType().findFieldByName("bb");
    fieldBuilder2.setField(subFieldDescriptor2, 1);
    builder2.setField(fieldDescriptor, fieldBuilder2.build());

    // Mutate foreign message
    final Message.Builder foreignFieldBuilder2 =
        builder2.newBuilderForField(foreignFieldDescriptor).mergeFrom(
            (Message) builder2.getField(foreignFieldDescriptor));
    final FieldDescriptor subForeignFieldDescriptor2 =
        foreignFieldBuilder2.getDescriptorForType().findFieldByName("c");
    foreignFieldBuilder2.setField(subForeignFieldDescriptor2, 2);
    builder2.setField(foreignFieldDescriptor, foreignFieldBuilder2.build());

    // Mutate import message
    final Message.Builder importFieldBuilder2 =
        builder2.newBuilderForField(importFieldDescriptor).mergeFrom(
            (Message) builder2.getField(importFieldDescriptor));
    final FieldDescriptor subImportFieldDescriptor2 =
        importFieldBuilder2.getDescriptorForType().findFieldByName("d");
    importFieldBuilder2.setField(subImportFieldDescriptor2, 3);
    builder2.setField(importFieldDescriptor, importFieldBuilder2.build());

    final Message newMessage2 = builder2.build();

    // These two messages should be equal.
    assertEquals(newMessage1, newMessage2);
  }

  @Test
  public void testGetFieldBuilderWithInitializedValue() {
    final Descriptor descriptor = allTypesDescriptor;
    final FieldDescriptor fieldDescriptor = descriptor.findFieldByName("optional_nested_message");

    // Before setting field, builder is initialized by default value.
    IBuilder2 fieldBuilder = builder.getFieldBuilder(fieldDescriptor);
    assertEquals(0, fieldBuilder.getField(fdBb));

    // Setting field value with new field builder instance.
    builder = messageProvider.newBuilder(allTypesDescriptor);
    final IBuilder2 newFieldBuilder = builder.getFieldBuilder(fieldDescriptor);
    newFieldBuilder.setField(fdBb, 2);
    // Then get the field builder instance by getFieldBuilder().
    fieldBuilder = builder.getFieldBuilder(fieldDescriptor);
    // It should contain new value.
    assertEquals(2, fieldBuilder.getField(fdBb));
    // These two builder should be equal.
    assertSame(fieldBuilder, newFieldBuilder);
  }

  @Test
  public void testGetFieldBuilderNotSupportedException() {
    final Descriptor descriptor = allTypesDescriptor;

    try {
      builder.getFieldBuilder(descriptor.findFieldByName("optional_int32"));
      fail("Exception was not thrown");
    } catch (final UnsupportedOperationException e) {
      // We expect this exception.
    }
    try {
      builder.getFieldBuilder(descriptor.findFieldByName("optional_nested_enum"));
      fail("Exception was not thrown");
    } catch (final UnsupportedOperationException e) {
      // We expect this exception.
    }
    try {
      builder.getFieldBuilder(descriptor.findFieldByName("repeated_int32"));
      fail("Exception was not thrown");
    } catch (final UnsupportedOperationException e) {
      // We expect this exception.
    }
    try {
      builder.getFieldBuilder(descriptor.findFieldByName("repeated_nested_enum"));
      fail("Exception was not thrown");
    } catch (final UnsupportedOperationException e) {
      // We expect this exception.
    }
    try {
      builder.getFieldBuilder(descriptor.findFieldByName("repeated_nested_message"));
      fail("Exception was not thrown");
    } catch (final UnsupportedOperationException e) {
      // We expect this exception.
    }
  }
}
