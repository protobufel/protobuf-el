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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import protobuf_unittest.Engine;
import protobuf_unittest.Vehicle;
import protobuf_unittest.Wheel;

import com.github.protobufel.ProtoInterfaces.IBuilder2;
import com.github.protobufel.ProtoInterfaces.IMessageEx;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;

/**
 * Test cases that exercise end-to-end use cases involving {@link SingleFieldBuilder} and
 * {@link RepeatedFieldBuilder}.
 *
 * based on jonp@google.com (Jon Perlow) work
 */
@RunWith(JUnit4.class)
public class NestedBuildersTest {
  private static final Logger log = LoggerFactory.getLogger(NestedBuildersTest.class);
  private static final FieldDescriptor WHEEL;
  private static final FieldDescriptor ENGINE;
  private static final FieldDescriptor RADIUS;
  private static final FieldDescriptor WIDTH;
  private static final FieldDescriptor LITERS;
  private static final FieldDescriptor CYLINDER;
  private IBuilder2 vehicleBuilder;

  static {
    Descriptor descriptor = Vehicle.getDescriptor();
    WHEEL = descriptor.findFieldByNumber(Vehicle.WHEEL_FIELD_NUMBER);
    ENGINE = descriptor.findFieldByNumber(Vehicle.ENGINE_FIELD_NUMBER);

    descriptor = Wheel.getDescriptor();
    RADIUS = descriptor.findFieldByNumber(Wheel.RADIUS_FIELD_NUMBER);
    WIDTH = descriptor.findFieldByNumber(Wheel.WIDTH_FIELD_NUMBER);

    descriptor = Engine.getDescriptor();
    LITERS = descriptor.findFieldByNumber(Engine.LITERS_FIELD_NUMBER);
    CYLINDER = descriptor.findFieldByNumber(Engine.CYLINDER_FIELD_NUMBER);
  }

  @Before
  public void init() {
    vehicleBuilder = DynamicMessage.newBuilder(Vehicle.getDescriptor());
  }

  @Test
  public void testMessagesAndBuilders() {
    vehicleBuilder.addFieldBuilder(WHEEL).setField(RADIUS, 4).setField(WIDTH, 1);
    vehicleBuilder.addFieldBuilder(WHEEL).setField(RADIUS, 4).setField(WIDTH, 2);
    vehicleBuilder.addFieldBuilder(WHEEL).setField(RADIUS, 4).setField(WIDTH, 3);
    vehicleBuilder.addFieldBuilder(WHEEL).setField(RADIUS, 4).setField(WIDTH, 4);
    vehicleBuilder.getFieldBuilder(ENGINE).setField(LITERS, 10);

    IMessageEx vehicle = vehicleBuilder.build();
    assertEquals(4, vehicle.getRepeatedFieldCount(WHEEL));
    for (int i = 0; i < 4; i++) {
      final Message wheel = (Message) vehicle.getRepeatedField(WHEEL, i);
      assertEquals(4, wheel.getField(RADIUS));
      assertEquals(i + 1, wheel.getField(WIDTH));
    }
    assertEquals(10, ((Message) vehicle.getField(ENGINE)).getField(LITERS));

    for (int i = 0; i < 4; i++) {
      vehicleBuilder.getFieldBuilder(WHEEL, i).setField(RADIUS, 5).setField(WIDTH, i + 10);
    }
    vehicleBuilder.getFieldBuilder(ENGINE).setField(LITERS, 20);

    vehicle = vehicleBuilder.build();
    for (int i = 0; i < 4; i++) {
      final Message wheel = (Message) vehicle.getRepeatedField(WHEEL, i);
      assertEquals(5, wheel.getField(RADIUS));
      assertEquals(i + 10, wheel.getField(WIDTH));
    }
    assertEquals(20, ((Message) vehicle.getField(ENGINE)).getField(LITERS));
    assertTrue(vehicle.hasField(ENGINE));
  }

  @Test
  public void testMessagesAreCached() {
    vehicleBuilder.addFieldBuilder(WHEEL).setField(RADIUS, 1).setField(WIDTH, 2);
    vehicleBuilder.addFieldBuilder(WHEEL).setField(RADIUS, 3).setField(WIDTH, 4);
    vehicleBuilder.addFieldBuilder(WHEEL).setField(RADIUS, 5).setField(WIDTH, 6);
    vehicleBuilder.addFieldBuilder(WHEEL).setField(RADIUS, 7).setField(WIDTH, 8);

    // Make sure messages are cached.
    @SuppressWarnings("unchecked")
    final List<Message> wheels =
        new ArrayList<Message>((List<Message>) vehicleBuilder.getField(WHEEL));
    for (int i = 0; i < wheels.size(); i++) {
      assertSame(wheels.get(i), vehicleBuilder.getRepeatedField(WHEEL, i));
    }

    // Now get builders and check they didn't change.
    for (int i = 0; i < wheels.size(); i++) {
      vehicleBuilder.getRepeatedField(WHEEL, i);
    }
    for (int i = 0; i < wheels.size(); i++) {
      assertSame(wheels.get(i), vehicleBuilder.getRepeatedField(WHEEL, i));
    }

    // Change just one
    vehicleBuilder.getFieldBuilder(WHEEL, 3).setField(RADIUS, 20).setField(WIDTH, 20);

    // Now get wheels and check that only that one changed
    for (int i = 0; i < wheels.size(); i++) {
      if (i < 3) {
        assertSame(wheels.get(i), vehicleBuilder.getRepeatedField(WHEEL, i));
      } else {
        assertNotSame(wheels.get(i), vehicleBuilder.getRepeatedField(WHEEL, i));
      }
    }
  }

  @Test
  public void testRemove_WithNestedBuilders() {
    vehicleBuilder.addFieldBuilder(WHEEL).setField(RADIUS, 1).setField(WIDTH, 1);
    vehicleBuilder.addFieldBuilder(WHEEL).setField(RADIUS, 2).setField(WIDTH, 2);
    vehicleBuilder.removeRepeatedField(WHEEL, 0);

    assertEquals(1, vehicleBuilder.getRepeatedFieldCount(WHEEL));
    assertEquals(2, ((Message) vehicleBuilder.getRepeatedField(WHEEL, 0)).getField(RADIUS));
  }

  public void testRemove_WithNestedMessages() {
    vehicleBuilder.addRepeatedField(WHEEL, Wheel.newBuilder().setField(RADIUS, 1)
        .setField(WIDTH, 1));
    vehicleBuilder.addRepeatedField(WHEEL, Wheel.newBuilder().setField(RADIUS, 2)
        .setField(WIDTH, 2));
    vehicleBuilder.removeRepeatedField(WHEEL, 0);

    assertEquals(1, vehicleBuilder.getRepeatedFieldCount(WHEEL));
    assertEquals(2, ((Message) vehicleBuilder.getRepeatedField(WHEEL, 0)).getField(RADIUS));
  }

  public void testMerge() {
    final Vehicle vehicle1 =
        Vehicle.newBuilder().addWheel(Wheel.newBuilder().setField(RADIUS, 1).build())
            .addWheel(Wheel.newBuilder().setField(RADIUS, 2).build()).build();

    final Vehicle vehicle2 = Vehicle.newBuilder().mergeFrom(vehicle1).build();
    // List should be the same -- no allocation
    assertSame(vehicle1.getField(WHEEL), vehicle2.getField(WHEEL));

    final Vehicle vehicle3 = vehicle1.toBuilder().build();
    assertSame(vehicle1.getField(WHEEL), vehicle3.getField(WHEEL));
  }

  public void testGettingBuilderMarksFieldAsHaving() {
    vehicleBuilder.getFieldBuilder(ENGINE);
    final Message vehicle = vehicleBuilder.buildPartial();
    assertTrue(vehicle.hasField(ENGINE));
  }
}
