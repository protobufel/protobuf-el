//
// Copyright © 2014, David Tesler (https://github.com/protobufel)
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//     * Redistributions of source code must retain the above copyright
//       notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above copyright
//       notice, this list of conditions and the following disclaimer in the
//       documentation and/or other materials provided with the distribution.
//     * Neither the name of the <organization> nor the
//       names of its contributors may be used to endorse or promote products
//       derived from this software without specific prior written permission.
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

package com.github.protobufel.test.util;

import static com.github.protobufel.test.util.ProtoMatchers.IsDeeplyImmutableMessage.deeplyImmutableMessage;
import static com.github.protobufel.test.util.ProtoMatchers.IsImmutableMessage.immutableMessage;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.isA;
//import static org.mockito.BDDMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Message;

@Ignore // PowerMock fails creating a mock of FieldDescriptor.class, since Protobuf 2.6.1RC1 
@PrepareForTest({FieldDescriptor.class})
@RunWith(Parameterized.class)
public class ProtoMatchersTest {
  private static final Logger log = LoggerFactory.getLogger(ProtoMatchersTest.class);
  private Message testMessage;

  @Parameters(name = "{index}:immutable={1}")
  public static Collection<Object[]> data() {
    return ImmutableList.of(
        //Mutable collections
        new Object[]{Lists.newArrayList(1, 2, 3), false},
        //Immutable collections
        new Object[]{ImmutableList.of(1, 2, 3), true},
        new Object[]{Collection.class.cast(ImmutableList.of(1, 2)), true},
        new Object[]{Collections.unmodifiableList(Lists.newArrayList(1, 2, 3)), true},
        new Object[]{Collections.unmodifiableCollection(Lists.newArrayList(1, 2)), true},
        new Object[]{Collections.unmodifiableSet(new HashSet<>(Arrays.asList(1, 2, 3))), true}
        );
  }
  
  @Parameter(value = 0)
  public Collection<?> testCollection;

  @Parameter(value = 1)
  public boolean isImmutable;
  
  @Rule
  public PowerMockRule powerMockRule = new PowerMockRule();

  @Before
  public void setUp() throws Exception {
  }
  
  private Message createMockMessage(final boolean isMapImmutable, final int depth,
      final Collection<?> testCollection, final boolean... deepRepeatedPattern) {
    final Message mocked = mock(Message.class);
    final SortedMap<FieldDescriptor, Object> map = new TreeMap<FieldDescriptor, Object>(new Comparator<FieldDescriptor>() {
      @Override
      public int compare(FieldDescriptor o1, FieldDescriptor o2) {
        return Integer.compare(o1.getNumber(), o2.getNumber());
      }
    });
    map.put(getMockFieldDescriptor(1, false, JavaType.STRING), "immutable string");
    map.put(getMockFieldDescriptor(2, true, JavaType.STRING),
        Collections.unmodifiableList(Arrays.asList("one", "two")));

    if (depth == 0) {
      map.put(getMockFieldDescriptor(3, true, JavaType.INT), testCollection);
    } else {
      final Message child = createMockMessage(true, depth - 1, testCollection, 
          deepRepeatedPattern);
      boolean isRepeated = deepRepeatedPattern.length < depth 
          ? false 
              : deepRepeatedPattern[deepRepeatedPattern.length - depth];
      map.put(getMockFieldDescriptor(3, isRepeated, JavaType.MESSAGE),
          (isRepeated ? Collections.singletonList(child) : child));
    }

    given(mocked.getAllFields())
        .willReturn(isMapImmutable ? Collections.unmodifiableSortedMap(map) : map);
    return mocked;
  }
  
  private FieldDescriptor getMockFieldDescriptor(int fieldNumber, boolean isRepeated, JavaType fieldType) {
    final FieldDescriptor field = mock(FieldDescriptor.class);
    given(field.getNumber()).willReturn(fieldNumber);
    given(field.isRepeated()).willReturn(isRepeated);
    given(field.getJavaType()).willReturn(fieldType);
    
    given(field.compareTo(isA(FieldDescriptor.class))).willAnswer(new Answer<Integer>() {
      @Override
      public Integer answer(InvocationOnMock invocation) throws Throwable {
        FieldDescriptor other = (FieldDescriptor) invocation.getArguments()[0];
        FieldDescriptor mock = (FieldDescriptor) invocation.getMock();
        return Integer.compare(mock.getNumber(), other.getNumber());
      }
    });
    return field;
  }
  
  @Test
  public void testIsImmutableMessageMatcherWithMutableMapClass() throws Exception {
    final Message mocked = createMockMessage(false, 0, testCollection);
    assertThat(immutableMessage().matches(mocked), equalTo(false));
  }
  
  @Test
  public void testIsImmutableMessageMatcherWithShallowRepeatedFieldMessage() throws Exception {
    final Message mocked = createMockMessage(true, 0, testCollection);
    assertThat(immutableMessage().matches(mocked), equalTo(isImmutable));
  }
  
  @Test
  public void testIsDeeplyImmutableMessageMatcher1() throws Exception {
    final Message mocked = createMockMessage(true, 2, testCollection, true, false, false);
    assertThat(deeplyImmutableMessage().matches(mocked), equalTo(isImmutable));
  }
  
  @Test
  public void testIsDeeplyImmutableMessageMatcher2() throws Exception {
    final Message mocked = createMockMessage(true, 3, testCollection, true, true, true);
    assertThat(deeplyImmutableMessage().matches(mocked), equalTo(isImmutable));
  }
  
  @Test
  public void testIsDeeplyImmutableMessageMatcher3() throws Exception {
    final Message mocked = createMockMessage(true, 4, testCollection, false, true, false);
    assertThat(deeplyImmutableMessage().matches(mocked), equalTo(isImmutable));
  }
  
  @Test
  public void testIsImmutableMessageMatcherDescribe() throws Exception {
    assertThat(immutableMessage().toString(), is("an immutable message"));
  }
  
  @Test
  public void testIsDeeplyImmutableMessageMatcherDescribe() throws Exception {
    assertThat(deeplyImmutableMessage().toString(), is("a deeply immutable message"));
  }
}
