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

import static com.github.protobufel.test.util.CommonMatchers.IsClearableComposite.clearableComposite;
import static com.github.protobufel.test.util.CommonMatchers.IsKnownImmutableComposite.knownImmutableComposite;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.protobufel.test.util.CommonMatchers.KnownImmutableComposites;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 *
 * @author protobufel@gmail.com David Tesler
 */
@RunWith(Parameterized.class)
public class CommonMatchersTest {
  private static final Logger log = LoggerFactory.getLogger(CommonMatchersTest.class);
  
  @Parameters(name = "{index}:immutable={1}")
  public static Collection<Object[]> data() {
    return ImmutableList.of(
        //Mutable collections
        new Object[]{Lists.newArrayList(1, 2, 3), false},
        new Object[]{Sets.newHashSet("1", "2"), false},
        new Object[]{Collection.class.cast(Lists.newArrayList(true, false)), false},
        //Immutable collections
        new Object[]{ImmutableList.of(1, 2, 3), true},
        new Object[]{ImmutableSet.of("1", "2"), true},
        new Object[]{ImmutableSortedSet.of(1, 2, 3), true},
        new Object[]{ImmutableMultiset.of(1, 2, 3), true},
        new Object[]{Collection.class.cast(ImmutableList.of(true, false)), true},
        new Object[]{Collections.unmodifiableList(Lists.newArrayList(1, 2, 3)), true},
        new Object[]{Collections.unmodifiableSet(Sets.newHashSet("1", "2")), true},
        new Object[]{Collections.unmodifiableCollection(Lists.newArrayList(true, false)), true}
        );
  }
  
  @Parameter(value = 0)
  public Collection<?> testCollection;

  @Parameter(value = 1)
  public boolean isImmutable;

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testKnownImmutableCollectionEnum() throws Exception {
    assertThat(KnownImmutableComposites.isImmutableComposite(testCollection), 
        equalTo(isImmutable));
    assertThat(KnownImmutableComposites.isClearableComposite(testCollection), 
        not(equalTo(isImmutable)));
  }
  
  @Test
  public void testIsClearableCollectionMatcher() throws Exception {
    assertThat(clearableComposite().matches(testCollection), not(equalTo(isImmutable)));
  }
  
  @Test
  public void testIsClearableCollectionMatcherDescribe() throws Exception {
    assertThat(clearableComposite().toString(), is("a clearable collection"));
  }
  
  @Test
  public void testIsKnownImmutableCollectionMatcher() throws Exception {
    assertThat(knownImmutableComposite().matches(testCollection), equalTo(isImmutable));
  }
  
  @Test
  public void testIsKnownImmutableCollectionMatcherDescribe() throws Exception {
    assertThat(knownImmutableComposite().toString(), is("an immutable collection"));
  }
}
