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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;
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
 * Tests for {@link SingleFieldBuilder}. This tests basic functionality. More extensive testing is
 * provided via other tests that exercise the builder.
 *
 * based on @author jonp@google.com (Jon Perlow) work
 */
@RunWith(JUnit4.class)
public class SingleFieldBuilderTest {
  private static final Logger log = LoggerFactory.getLogger(SingleFieldBuilderTest.class);
  private static final FieldDescriptor OPTIONAL_INT32;
  private static final FieldDescriptor OPTIONAL_DOUBLE;
  private MockBuilderParent mockParent;
  private SingleFieldBuilder<Message, Builder, MessageOrBuilder> builder;

  static {
    final Descriptor descriptor = TestAllTypes.getDescriptor();
    OPTIONAL_INT32 = MessageAdapter.getFieldDescriptor(descriptor, "optional_int32");
    OPTIONAL_DOUBLE = MessageAdapter.getFieldDescriptor(descriptor, "optional_double");
  }

  @Before
  public void init() {
    mockParent = new MockBuilderParent();
    builder =
        DynamicMessage.Builder.newSingleFieldBuilderForTesting(TestAllTypes.getDefaultInstance(),
            mockParent, false);
  }

  @Test
  public void testBasicUseAndInvalidations() {
    assertSame(TestAllTypes.getDefaultInstance(), builder.getMessage());
    assertEquals(TestAllTypes.getDefaultInstance(), builder.getBuilder().buildPartial());
    assertEquals(0, mockParent.getInvalidationCount());

    builder.getBuilder().setField(OPTIONAL_INT32, 10);
    assertEquals(0, mockParent.getInvalidationCount());
    final Message message = builder.build();
    assertEquals(10, message.getField(OPTIONAL_INT32));

    // Test that we receive invalidations now that build has been called.
    assertEquals(0, mockParent.getInvalidationCount());
    builder.getBuilder().setField(OPTIONAL_INT32, 20);
    assertEquals(1, mockParent.getInvalidationCount());

    // Test that we don't keep getting invalidations on every change
    builder.getBuilder().setField(OPTIONAL_INT32, 30);
    assertEquals(1, mockParent.getInvalidationCount());
  }

  @Test
  public void testSetMessage() {
    builder.setMessage(TestAllTypes.newBuilder().setOptionalInt32(0).build());
    assertEquals(0, builder.getMessage().getField(OPTIONAL_INT32));

    // Update message using the builder
    builder.getBuilder().setField(OPTIONAL_INT32, 1);
    assertEquals(0, mockParent.getInvalidationCount());
    assertEquals(1, builder.getBuilder().getField(OPTIONAL_INT32));
    assertEquals(1, builder.getMessage().getField(OPTIONAL_INT32));
    builder.build();
    builder.getBuilder().setField(OPTIONAL_INT32, 2);
    assertEquals(2, builder.getBuilder().getField(OPTIONAL_INT32));
    assertEquals(2, builder.getMessage().getField(OPTIONAL_INT32));

    // Make sure message stays cached
    assertSame(builder.getMessage(), builder.getMessage());
  }

  @Test
  public void testClear() {
    builder.setMessage(TestAllTypes.newBuilder().setOptionalInt32(0).build());
    assertNotEquals(TestAllTypes.getDefaultInstance(), builder.getMessage());

    builder.clear();
    assertSame(TestAllTypes.getDefaultInstance(), builder.getMessage());

    builder.getBuilder().setField(OPTIONAL_INT32, 1);
    assertNotEquals(TestAllTypes.getDefaultInstance(), builder.getMessage());

    builder.clear();
    assertEquals(TestAllTypes.getDefaultInstance(), builder.getMessage());
  }

  @Test
  public void testMerge() {
    // Merge into default field.
    builder.mergeFrom(TestAllTypes.getDefaultInstance());
    assertSame(TestAllTypes.getDefaultInstance(), builder.getMessage());

    // Merge into non-default field on existing builder.
    builder.getBuilder().setField(OPTIONAL_INT32, 2);
    builder.mergeFrom(TestAllTypes.newBuilder().setOptionalDouble(4.0).buildPartial());
    assertEquals(2, builder.getMessage().getField(OPTIONAL_INT32));
    assertEquals(4.0, builder.getMessage().getField(OPTIONAL_DOUBLE));

    // Merge into non-default field on existing message
    builder.setMessage(TestAllTypes.newBuilder().setOptionalInt32(10).buildPartial());
    builder.mergeFrom(TestAllTypes.newBuilder().setOptionalDouble(5.0).buildPartial());
    assertEquals(10, builder.getMessage().getField(OPTIONAL_INT32));
    assertEquals(5.0, builder.getMessage().getField(OPTIONAL_DOUBLE));
  }
}
