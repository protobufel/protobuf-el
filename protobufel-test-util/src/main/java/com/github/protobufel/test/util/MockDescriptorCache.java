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

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.google.common.collect.Maps;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;

@PrepareForTest({FieldDescriptor.class, Descriptor.class})
public class MockDescriptorCache {
  private static final class MockDescriptorEntry {
    private final Descriptor descriptor;
    private final SortedMap<String, FieldDescriptor> fields;

    public MockDescriptorEntry(Descriptor descriptor) {
      this(descriptor, Maps.<String, FieldDescriptor>newTreeMap());
    }
    
    private MockDescriptorEntry(Descriptor descriptor, SortedMap<String, FieldDescriptor> fields) {
      if ((descriptor == null) || (fields == null)) {
        throw new NullPointerException();
      }
      
      this.descriptor = descriptor;
      this.fields = fields;
    }
    
    public Descriptor getDescriptor() {
      return descriptor;
    }

    public SortedMap<String, FieldDescriptor> getFields() {
      return fields;
    }

    public FieldDescriptor getField(String fieldName) {
      return fields.get(fieldName);
    }
    
    public FieldDescriptor getField(int fieldIndex) {
      for (FieldDescriptor field : fields.values()) {
        if (field.getIndex() == fieldIndex) {
          return field;
        }
      }
      
      return null;
    }
    
    public FieldDescriptor putField(String fieldName, FieldDescriptor field) {
      return fields.put(fieldName, field);
    }

    @Override
    public int hashCode() {
      return Objects.hash(descriptor.getFullName(), fields);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      
      if (obj == null) {
        return false;
      }
      
      if (!(obj instanceof MockDescriptorEntry)) {
        return false;
      }
      
      MockDescriptorEntry other = (MockDescriptorEntry) obj;
      
      if (descriptor == null) {
        if (other.descriptor != null) {
          return false;
        }
      } else if (!descriptor.getFullName().equals(other.descriptor.getFullName())) {
        return false;
      }
      
      if (fields == null) {
        if (other.fields != null) {
          return false;
        }
      } else if (!fields.equals(other.fields)) {
        return false;
      }
      
      return true;
    }
  }
  
  private final Map<String, MockDescriptorEntry> descriptorMap;
  private final String basePath;

  public MockDescriptorCache(Class<?> classForBasePath) {
    this(classForBasePath.getName()); 
  }

  public MockDescriptorCache(String basePath) {
    this(new HashMap<String, MockDescriptorEntry>(), basePath); 
  }

  private MockDescriptorCache(Map<String, MockDescriptorEntry> descriptorMap, 
      String basePath) {
    this.descriptorMap = descriptorMap;
    this.basePath = basePath + ".";
  }
  
  public FieldDescriptor getField(String descriptorShortName, String fieldShortName) {
    String descriptorFullName = getFullName(descriptorShortName);
    MockDescriptorEntry fieldMap = descriptorMap.get(descriptorFullName);
    
    if (fieldMap == null) {
      return null;
    } 
    
    return fieldMap.getField(fieldShortName);
  }

  public FieldDescriptor getField(String descriptorShortName, int index) {
    String descriptorFullName = getFullName(descriptorShortName);
    MockDescriptorEntry fieldMap = descriptorMap.get(descriptorFullName);
    
    if (fieldMap == null) {
      return null;
    } 
    
    return fieldMap.getField(index);
  }

  public FieldDescriptor putField(String descriptorShortName, String fieldShortName,
      int index, boolean isRepeated, JavaType type, String fieldMessageTypeName) {
    String descriptorFullName = getFullName(descriptorShortName);
    MockDescriptorEntry fieldMap = descriptorMap.get(descriptorFullName);
    FieldDescriptor field = null;
    
    if (fieldMap == null) {
      fieldMap = new MockDescriptorEntry(createMockDescriptor(descriptorShortName));
      descriptorMap.put(descriptorFullName, fieldMap);
    } else {
      field = fieldMap.getField(fieldShortName);
    }
    
    if (field == null) {
      field = createMockField(fieldShortName, index, isRepeated, type, 
          fieldMap.getDescriptor(), fieldMessageTypeName);
      fieldMap.putField(fieldShortName, field);
    }
    
    return field;
  }

  private String getFullName(String shortName) {
    return basePath + shortName;
  }
  
  private FieldDescriptor createMockField(String name, int index, boolean isRepeated, 
      JavaType type, Descriptor parent, String fieldMessageTypeName) {
    return createMockField(fieldMessageTypeName, index, isRepeated, type, parent, 
        putDescriptor(fieldMessageTypeName));
  }

  public Descriptor putDescriptor(String descriptorName) {
    Descriptor descriptor = getDescriptor(descriptorName);
    
    if (descriptor == null) {
      descriptor = createMockDescriptor(descriptorName);
      descriptorMap.put(descriptorName, new MockDescriptorEntry(descriptor));
    }
    
    return descriptor;
  }

  public Descriptor getDescriptor(String decsriptorFullName) {
    MockDescriptorEntry entry = descriptorMap.get(decsriptorFullName);
    return (entry == null) ? null : entry.getDescriptor();
  }

  public FieldDescriptor createMockField(String name, int index, boolean isRepeated, 
      JavaType type, Descriptor parent, Descriptor fieldMessageType) {
    FieldDescriptor field = mock(FieldDescriptor.class);
    
    //given
    given(field.isRepeated()).willReturn(isRepeated);
    given(field.getJavaType()).willReturn(type);
    given(field.getContainingType()).willReturn(parent);
    given(field.getName()).willReturn(parent.getFullName() + "." + name);
    given(field.getIndex()).willReturn(index);
    given(field.getMessageType()).willReturn((type == JavaType.MESSAGE) ? fieldMessageType : null);
    
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
  
  @SuppressWarnings("unchecked")
  public Descriptor createMockDescriptor(final String name) {
    Descriptor descriptor = mock(Descriptor.class);
    final String fullName = getFullName(name);
    
    //given
    given(descriptor.getFullName()).willReturn(fullName);
    given(descriptor.findFieldByName(anyString())).willAnswer(new Answer<FieldDescriptor>() {
         @Override 
         public FieldDescriptor answer(InvocationOnMock invocation) throws Throwable {
           // Descriptor mock = (Descriptor) invocation.getMock();
           return getField(fullName, (String) invocation.getArguments()[0]);
         }
      });
    given(descriptor.findFieldByNumber(anyInt())).willAnswer(new Answer<FieldDescriptor>(){
          @Override
          public FieldDescriptor answer(InvocationOnMock invocation) throws Throwable {
            // Descriptor mock = (Descriptor) invocation.getMock();
            return getField(fullName, (Integer) invocation.getArguments()[0]);
          }});
    given(descriptor.findEnumTypeByName(fullName)).willThrow(UnsupportedOperationException.class);
    
    return descriptor;
  }
}