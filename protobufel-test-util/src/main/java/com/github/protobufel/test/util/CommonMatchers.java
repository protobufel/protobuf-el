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

import java.util.Collections;
import java.util.TreeMap;
import java.util.TreeSet;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class CommonMatchers {
  
  private CommonMatchers() {}
  
  protected enum KnownImmutableComposites {
    GUAVA_COLLECTION("com.google.common.collect.ImmutableCollection", true),
    GUAVA_MAP("com.google.common.collect.ImmutableMap", true),
    GUAVA_MULTISET("com.google.common.collect.ImmutableMultiset", true),
    GUAVA_MULTIMAP("com.google.common.collect.ImmutableMultimap", true),
    JDK_COLLECTION(Collections.unmodifiableCollection(
        Collections.emptyList()).getClass().getName(), true),
    JDK_MAP(Collections.unmodifiableMap(Collections.emptyMap()).getClass().getName(), true),
    JDK_SORTEDMAP(Collections.unmodifiableSortedMap(new TreeMap<>()).getClass().getName(), true),
    JDK_SORTEDSET(Collections.unmodifiableSortedSet(new TreeSet<>()).getClass().getName(), true),
    JDK_SINGLETONLIST(Collections.singletonList(Boolean.FALSE).getClass().getName(), false),
    JDK_SINGLETONMAP(Collections.singletonMap(Boolean.FALSE, Boolean.FALSE).getClass().getName(), 
        false),
    JDK_SINGLETON(Collections.singleton(Boolean.FALSE).getClass().getName(), false),
    ;
    
    private String className;
    private boolean isAncestor;
    
    private KnownImmutableComposites(String className, boolean isAncestor) {
      this.className = className;
      this.isAncestor = isAncestor;
    }
    
    private boolean isImmutable(final Object composite) {
      if (isAncestor) {
        for (Class<?> clazz = composite.getClass(); clazz != null; 
            clazz = clazz.getSuperclass()) {
          if (className.equals(clazz.getName())) {
            return true;
          }
        }
      } else if (className.equals(composite.getClass().getName())) {
        return true;
      }
      
      return false;
    }
    
    public static <T> boolean isImmutableComposite(T composite) {
      for (KnownImmutableComposites value : values()) {
        if (value.isImmutable(composite)) {
          return true;
        }
      }
      
      return false;
    }
    
    public static <T> boolean isClearableComposite(T composite) {
      if (isImmutableComposite(composite)) {
        return false;
      }
      
      try {
        composite.getClass().getMethod("clear").invoke(composite);
      } catch (UnsupportedOperationException e) {
        return false;
      } catch (Throwable e) {
        //throw new RuntimeException(e);
        return false;
      }
      
      return true;
    }
  }
  
  public static class IsClearableComposite<T> extends TypeSafeMatcher<T> {

    @Override
    public void describeTo(Description description) {
      description.appendText("a clearable collection");
    }

    @Override
    protected boolean matchesSafely(T item) {
      return KnownImmutableComposites.isClearableComposite(item);
    }
    
    @Factory
    public static <T> Matcher<T> clearableComposite() {
      return new IsClearableComposite<T>();
    }
  }
  
  public static class IsKnownImmutableComposite<T> extends TypeSafeMatcher<T> {

    @Override
    public void describeTo(Description description) {
      description.appendText("an immutable collection");
    }

    @Override
    protected boolean matchesSafely(T item) {
      return KnownImmutableComposites.isImmutableComposite(item);
    }
    
    @Factory
    public static <T> Matcher<T> knownImmutableComposite() {
      return new IsKnownImmutableComposite<T>();
    }
  }
}
