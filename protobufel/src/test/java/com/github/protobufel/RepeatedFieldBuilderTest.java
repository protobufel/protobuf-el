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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import protobuf_unittest.UnittestProto.TestAllTypes;

import com.github.protobufel.DynamicMessage.Builder;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;

/**
 * Tests for {@link RepeatedFieldBuilder}. This tests basic functionality. More extensive testing is
 * provided via other tests that exercise the builder.
 *
 * based @author jonp@google.com (Jon Perlow) work
 */
@RunWith(JUnit4.class)
public class RepeatedFieldBuilderTest {
  private static final Logger log = LoggerFactory.getLogger(RepeatedFieldBuilderTest.class);
  private static final FieldDescriptor OPTIONAL_INT32;
  private static final FieldDescriptor OPTIONAL_DOUBLE;
  private static final FieldDescriptor OPTIONAL_STRING;
  private RepeatedFieldBuilder<Message, Builder, MessageOrBuilder> builder;
  private MockBuilderParent mockParent;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  static {
    final Descriptor descriptor = TestAllTypes.getDescriptor();
    OPTIONAL_INT32 = MessageAdapter.getFieldDescriptor(descriptor, "optional_int32");
    OPTIONAL_DOUBLE = MessageAdapter.getFieldDescriptor(descriptor, "optional_double");
    OPTIONAL_STRING = MessageAdapter.getFieldDescriptor(descriptor, "optional_string");
  }

  @Before
  public void init() {
    mockParent = new MockBuilderParent();
    builder =
        DynamicMessage.Builder.newRepeatedFieldBuilderForTesting(Collections.<Message>emptyList(),
            false, mockParent, false);
  }

  @Test
  public void testBasicUse() {
    builder.addMessage(TestAllTypes.newBuilder().setOptionalInt32(0).build());
    builder.addMessage(TestAllTypes.newBuilder().setOptionalInt32(1).build());
    assertEquals(0, ((TestAllTypes) builder.getMessage(0)).getOptionalInt32());
    assertEquals(1, ((TestAllTypes) builder.getMessage(1)).getOptionalInt32());

    @SuppressWarnings("unchecked")
    final List<TestAllTypes> list = (List<TestAllTypes>) (List<?>) builder.build();
    assertEquals(2, list.size());
    assertEquals(0, list.get(0).getOptionalInt32());
    assertEquals(1, list.get(1).getOptionalInt32());
    assertIsUnmodifiable(list);

    // Make sure it doesn't change.
    @SuppressWarnings("unchecked")
    final List<TestAllTypes> list2 = (List<TestAllTypes>) (List<?>) builder.build();
    assertSame(list, list2);
    assertEquals(0, mockParent.getInvalidationCount());
  }

  @Test
  public void testGoingBackAndForth() {
    builder.addMessage(TestAllTypes.newBuilder().setOptionalInt32(0).build());
    builder.addMessage(TestAllTypes.newBuilder().setOptionalInt32(1).build());
    assertEquals(0, ((TestAllTypes) builder.getMessage(0)).getOptionalInt32());
    assertEquals(1, ((TestAllTypes) builder.getMessage(1)).getOptionalInt32());

    // Convert to list
    @SuppressWarnings("unchecked")
    final List<TestAllTypes> list = (List<TestAllTypes>) (List<?>) builder.build();
    assertEquals(2, list.size());
    assertEquals(0, list.get(0).getOptionalInt32());
    assertEquals(1, list.get(1).getOptionalInt32());
    assertIsUnmodifiable(list);

    // Update 0th item
    assertEquals(0, mockParent.getInvalidationCount());
    builder.getBuilder(0).setField(OPTIONAL_STRING, "foo");
    assertEquals(1, mockParent.getInvalidationCount());
    final List<Message> list2 = builder.build();
    assertEquals(2, list2.size());
    assertEquals(0, list2.get(0).getField(OPTIONAL_INT32));
    assertEquals("foo", list2.get(0).getField(OPTIONAL_STRING));
    assertEquals(1, list2.get(1).getField(OPTIONAL_INT32));
    assertIsUnmodifiable(list2);
    assertEquals(1, mockParent.getInvalidationCount());
  }

  @Test
  public void testVariousMethods() {
    builder.addMessage(TestAllTypes.newBuilder().setOptionalInt32(1).build());
    builder.addMessage(TestAllTypes.newBuilder().setOptionalInt32(2).build());
    builder.addBuilder(0, TestAllTypes.getDefaultInstance()).setField(OPTIONAL_INT32, 0);
    builder.addBuilder(TestAllTypes.getDefaultInstance()).setField(OPTIONAL_INT32, 3);

    assertEquals(0, builder.getMessage(0).getField(OPTIONAL_INT32));
    assertEquals(1, builder.getMessage(1).getField(OPTIONAL_INT32));
    assertEquals(2, builder.getMessage(2).getField(OPTIONAL_INT32));
    assertEquals(3, builder.getMessage(3).getField(OPTIONAL_INT32));

    assertEquals(0, mockParent.getInvalidationCount());
    final List<Message> messages = builder.build();
    assertEquals(4, messages.size());
    assertSame(messages, builder.build()); // expect same list

    // Remove a message.
    builder.remove(2);
    assertEquals(1, mockParent.getInvalidationCount());
    assertEquals(3, builder.getCount());
    assertEquals(0, builder.getMessage(0).getField(OPTIONAL_INT32));
    assertEquals(1, builder.getMessage(1).getField(OPTIONAL_INT32));
    assertEquals(3, builder.getMessage(2).getField(OPTIONAL_INT32));

    // Remove a builder.
    builder.remove(0);
    assertEquals(1, mockParent.getInvalidationCount());
    assertEquals(2, builder.getCount());
    assertEquals(1, builder.getMessage(0).getField(OPTIONAL_INT32));
    assertEquals(3, builder.getMessage(1).getField(OPTIONAL_INT32));

    // Test clear.
    builder.clear();
    assertEquals(1, mockParent.getInvalidationCount());
    assertEquals(0, builder.getCount());
    assertTrue(builder.isEmpty());
  }

  @Test
  public void testLists() {
    builder.addMessage(TestAllTypes.newBuilder().setOptionalInt32(1).build());
    builder.addMessage(0, TestAllTypes.newBuilder().setOptionalInt32(0).build());
    assertEquals(0, builder.getMessage(0).getField(OPTIONAL_INT32));
    assertEquals(1, builder.getMessage(1).getField(OPTIONAL_INT32));

    // Use list of builders.
    final List<Builder> builders = builder.getBuilderList();
    assertEquals(0, builders.get(0).getField(OPTIONAL_INT32));
    assertEquals(1, builders.get(1).getField(OPTIONAL_INT32));
    builders.get(0).setField(OPTIONAL_INT32, 10);
    builders.get(1).setField(OPTIONAL_INT32, 11);

    // Use list of protos
    final List<Message> protos = builder.getMessageList();
    assertEquals(10, protos.get(0).getField(OPTIONAL_INT32));
    assertEquals(11, protos.get(1).getField(OPTIONAL_INT32));

    // Add an item to the builders and verify it's updated in both
    builder.addMessage(TestAllTypes.newBuilder().setOptionalInt32(12).build());
    assertEquals(3, builders.size());
    assertEquals(3, protos.size());
  }

  private void assertIsUnmodifiable(final List<?> list) {
    if (list != Collections.emptyList()) {
      expectedException.expect(UnsupportedOperationException.class);
      list.clear();
    }
  }
}
