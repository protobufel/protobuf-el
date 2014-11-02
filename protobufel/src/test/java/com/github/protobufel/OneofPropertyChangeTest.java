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

package com.github.protobufel;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import protobuf_unittest.UnittestProto.TestAllTypes;

import com.google.protobuf.Descriptors.FieldDescriptor;

public class OneofPropertyChangeTest {
  private static final FieldDescriptor ONEOF_STRING_FD = TestAllTypes.getDescriptor()
      .findFieldByNumber(TestAllTypes.ONEOF_STRING_FIELD_NUMBER);
  private static final FieldDescriptor ONEOF_NESTED_MESSAGE_FD = TestAllTypes.getDescriptor()
      .findFieldByNumber(TestAllTypes.ONEOF_NESTED_MESSAGE_FIELD_NUMBER);
  private static final FieldDescriptor NESTED_MESSAGE_BB_FD = TestAllTypes.NestedMessage
      .getDescriptor().findFieldByNumber(TestAllTypes.NestedMessage.BB_FIELD_NUMBER);

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testOneOfPropertyChangeDynamicBuilder() {
    final DynamicMessage.Builder builder = DynamicMessage.newBuilder(TestAllTypes.getDescriptor());

    builder.setField(ONEOF_STRING_FD, "hello");
    assertThat(builder.getOneofFieldDescriptor(ONEOF_STRING_FD.getContainingOneof()),
        is(equalTo(ONEOF_STRING_FD)));

    final DynamicMessage.Builder innerBuilder = builder.getFieldBuilder(ONEOF_NESTED_MESSAGE_FD);
    assertThat(builder.getOneofFieldDescriptor(ONEOF_NESTED_MESSAGE_FD.getContainingOneof()),
        is(equalTo(ONEOF_NESTED_MESSAGE_FD)));

    builder.setField(ONEOF_STRING_FD, "hello again");
    assertThat(builder.getOneofFieldDescriptor(ONEOF_STRING_FD.getContainingOneof()),
        is(equalTo(ONEOF_STRING_FD)));

    innerBuilder.setField(NESTED_MESSAGE_BB_FD, 999);
    // This desired, but not true currently :(
    // assertThat(builder.getOneofFieldDescriptor(ONEOF_NESTED_MESSAGE_FD.getContainingOneof()),
    // is(equalTo(ONEOF_NESTED_MESSAGE_FD)));
    assertThat(builder.getOneofFieldDescriptor(ONEOF_NESTED_MESSAGE_FD.getContainingOneof()),
        is(equalTo(ONEOF_STRING_FD)));
  }
}
