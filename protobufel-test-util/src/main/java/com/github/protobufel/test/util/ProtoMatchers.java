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

import static com.github.protobufel.test.util.CommonMatchers.IsKnownImmutableComposite
    .knownImmutableComposite;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Message;

public class ProtoMatchers {
  
  private ProtoMatchers() {}
  
  public static class IsImmutableMessage<T extends Message> extends TypeSafeDiagnosingMatcher<T> {
    private final Matcher<List<?>> subMatcher;

    public IsImmutableMessage() {
      subMatcher = knownImmutableComposite();
    }

    @Override
    protected boolean matchesSafely(T item, Description mismatch) {
      final Map<FieldDescriptor, Object> allFields = item.getAllFields();
      
      if (!subMatcher.matches(allFields)) {
        mismatch.appendText(String.format("getAllFields() is not immutable %s", 
            allFields.getClass()))
            .appendDescriptionOf(subMatcher).appendText(" ");
        subMatcher.describeMismatch(allFields, mismatch);
        return false;
      }
      
      for (Entry<FieldDescriptor, Object> entry : allFields.entrySet()) {
        final FieldDescriptor field = entry.getKey();
        
        if (field.isRepeated()) {
          //final List<?> value = List.class.cast(entry.getValue());
          final Object value = entry.getValue();
          
          if (!subMatcher.matches(value)) {
            mismatch.appendText(String.format("repeated field %s ", field))
                .appendDescriptionOf(subMatcher).appendText(" ");
            subMatcher.describeMismatch(value, mismatch);
            return false;
          }
        }
      }
      
      return true;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("an immutable message");
    }
    
    @Factory
    public static <T extends Message> Matcher<T> immutableMessage() {
      return new IsImmutableMessage<T>();
    }
  }
  
  public static class IsDeeplyImmutableMessage<T extends Message> extends TypeSafeDiagnosingMatcher<T> {
    private final IsImmutableMessage<Message> subMatcher;

    public IsDeeplyImmutableMessage() {
      subMatcher = (IsImmutableMessage<Message>) IsImmutableMessage.immutableMessage();
    }

    @Override
    protected boolean matchesSafely(T item, Description mismatch) {
      //shallow verify the Message itself first
      if (!subMatcher.matches(item)) {
        mismatch.appendDescriptionOf(subMatcher).appendText(" ");
        subMatcher.describeMismatch(item, mismatch);
        return false;
      }
      
      // now, verify all subMessages
      for (Entry<FieldDescriptor, Object> entry : item.getAllFields().entrySet()) {
        final FieldDescriptor field = entry.getKey();
        
        if (field.getJavaType() == JavaType.MESSAGE) {
          if (field.isRepeated()) {
            Iterable<?> messages = Iterable.class.cast(entry.getValue());
            int index = -1; 
            
            for (Object message : messages) {
              index++;
              
              if (!matches(message)) {
                mismatch.appendText(String.format("repeated field %s at index %s ", field, index))
                    .appendDescriptionOf(this).appendText(" ");
                describeMismatch(message, mismatch);
                return false;
              }
            }
          } else {
            final Object message = entry.getValue();
            
            if (!matches(message)) {
              mismatch.appendText(String.format("field %s ", field))
                  .appendDescriptionOf(this).appendText(" ");
              describeMismatch(message, mismatch);
              return false;
            }
          }
        }
      }
      
      return true;
    }
    
    @Override
    public void describeTo(Description description) {
      description.appendText("a deeply immutable message");
    }
    
    @Factory
    public static <T extends Message> Matcher<T> deeplyImmutableMessage() {
      return new IsDeeplyImmutableMessage<T>();
    }
  }
}
