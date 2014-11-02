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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;

import static org.powermock.api.mockito.PowerMockito.mock;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;

//FIXME either complete implementation or remove with its tested source!!!
@Ignore
@PrepareForTest({FieldDescriptor.class, Descriptor.class})
@RunWith(MockitoJUnitRunner.class)
public class MockDescriptorCacheTest {
  private String basePath;
  private MockDescriptorCache mockDescriptorCache;
  
  @Rule
  public PowerMockRule powerMockRule = new PowerMockRule();

  @Before
  public void setUp() throws Exception {
    basePath = getClass().getName();
    mockDescriptorCache = new MockDescriptorCache(getClass());
  }

  @Test
  public void testMockDescriptorCacheString() throws Exception {
    // TODO
    throw new RuntimeException("not yet implemented");
  }

  @Test
  public void testGetFieldStringString() throws Exception {
    // TODO
    throw new RuntimeException("not yet implemented");
  }

  @Test
  public void testGetFieldStringInt() throws Exception {
    // TODO
    throw new RuntimeException("not yet implemented");
  }

  @Test
  public void testPutField() throws Exception {
    // TODO
    throw new RuntimeException("not yet implemented");
  }

  @Test
  public void testPutDescriptor() throws Exception {
    // TODO
    throw new RuntimeException("not yet implemented");
  }

  @Test
  public void testGetDescriptor() throws Exception {
    //when
    final String descriptorShortName = "Galaxy";
    Descriptor descriptor = mockDescriptorCache.createMockDescriptor(descriptorShortName);
    //then
    assertThat(descriptor.getFullName(), equalTo(basePath + descriptorShortName));
  }

  @Test
  public void testCreateMockFieldStringIntBooleanJavaTypeDescriptorDescriptor() throws Exception {
    // TODO
    throw new RuntimeException("not yet implemented");
  }

  @Test
  public void testCreateMockDescriptor() throws Exception {
    
    throw new RuntimeException("not yet implemented");
  }

  @Test
  public void testHashCode() throws Exception {
    // TODO
    throw new RuntimeException("not yet implemented");
  }

  @Test
  public void testEquals() throws Exception {
    // TODO
    throw new RuntimeException("not yet implemented");
  }

}
