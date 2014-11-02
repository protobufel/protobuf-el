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

package com.github.protobufel.common.files;

import static com.github.protobufel.common.files.ContextPathMatchers.getHierarchicalMatcher;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeThat;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import com.github.protobufel.common.files.ContextPathMatchers.HierarchicalMatcher;

@NonNullByDefault
@RunWith(Theories.class)
public class ContextPathMatchersTest {

  @DataPoints("patterns")
  public static final String[] SYNTAX_AND_PATTERNS = new String[] { 
    "glob:part1/pa?rt2/*/**/last*part.*",
    "regex:[^/]*/text.{1,5}[.]txt",
    "bad:any:)how"
  };
  
  @DataPoints("pathTypes")
  public static final Class<?>[] PATH_TYPES = new Class<?>[] {
    String.class, 
    File.class, 
    Path.class,
    ArrayList.class // non-standard class with public default constructor, yet, should be okay! 
    };
  
  @SuppressWarnings("null")
  @Rule
  public ExpectedException thrown = ExpectedException.none();
  
  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Theory
  public void getPathMatcherWithGoodDataIsOk(
      final @FromDataPoints("patterns") String syntaxAndPattern,
      final boolean isUnix, final boolean allowDirs, boolean allowFiles, 
      final Class<?> pathType) {
    //Given
    assumeThat((!allowDirs && !allowFiles), is(false));
    assumeThat(syntaxAndPattern, anyOf(startsWith("glob:"), startsWith("regex:")));
    assumeThat(Arrays.asList(String.class, File.class, Path.class).contains(pathType), is(true));
    
    getPathMatcherWithGoodDataIsGoodHelper(syntaxAndPattern, isUnix, allowDirs, allowFiles, 
        pathType, true);
  }
  
  @Theory
  public void getPathMatcherWithUnAssumedPathTypeIsOk(
      final @FromDataPoints("patterns") String syntaxAndPattern,
      final boolean isUnix, final boolean allowDirs, boolean allowFiles, 
      final Class<?> pathType) {
    //Given
    assumeThat((!allowDirs && !allowFiles), is(false));
    assumeThat(syntaxAndPattern, anyOf(startsWith("glob:"), startsWith("regex:")));
    assumeThat(Arrays.asList(String.class, File.class, Path.class).contains(pathType), is(false));
    
    getPathMatcherWithGoodDataIsGoodHelper(syntaxAndPattern, isUnix, allowDirs, allowFiles, 
        pathType, false);
  }

  @Theory
  public void getPathMatcherWithBadSyntaxProhibited(
      final @FromDataPoints("patterns") String syntaxAndPattern,
      final boolean isUnix, final boolean allowDirs, boolean allowFiles, 
      final Class<?> pathType) {
    //Given
    assumeThat((!allowDirs && !allowFiles), is(false));
    assumeThat(syntaxAndPattern, is(not(anyOf(startsWith("glob:"), startsWith("regex:")))));
    assumeThat(Arrays.asList(String.class, File.class, Path.class).contains(pathType), is(true));
    
    //Then
    thrown.expect(isA(UnsupportedOperationException.class));
    thrown.expectMessage(containsString(syntaxAndPattern.split(":", 2)[0]));
    
    //When
    getHierarchicalMatcher(syntaxAndPattern, isUnix, allowDirs, allowFiles, pathType);
  }

  @Theory
  public void getPathMatcherWithAllowDirsAndAllowFilesBothFalseProhibited(
      final @FromDataPoints("patterns") String syntaxAndPattern,
      final boolean isUnix, final boolean allowDirs, boolean allowFiles, 
      final Class<?> pathType) {
    //Given
    assumeThat((!allowDirs && !allowFiles), is(true));
    assumeThat(syntaxAndPattern, is(anyOf(startsWith("glob:"), startsWith("regex:"))));
    assumeThat(Arrays.asList(String.class, File.class, Path.class).contains(pathType), is(true));
    
    //Then
    thrown.expect(isA(IllegalArgumentException.class));
    thrown.expectMessage(containsString("allowDirs and allowFiles cannot be both false"));
    
    //When
    getHierarchicalMatcher(syntaxAndPattern, isUnix, allowDirs, allowFiles, pathType);
  }

  private <T> void getPathMatcherWithGoodDataIsGoodHelper(
      final String syntaxAndPattern,
      final boolean isUnix, final boolean allowDirs, boolean allowFiles, 
      final Class<T> pathType, final boolean isAssumedPathType) {
    //When
    final HierarchicalMatcher<T> actualMatcher = getHierarchicalMatcher(syntaxAndPattern, isUnix, 
        allowDirs, allowFiles, pathType);
    
    //Then
    assertThat(actualMatcher, is(not(nullValue())));
    assertThat(actualMatcher.isAllowDirs(), is(allowDirs));
    assertThat(actualMatcher.isAllowFiles(), is(allowFiles));
    assertThat(actualMatcher.isEmpty(), is(false));
    
    assertThat(actualMatcher.toString(), is(not(nullValue())));
    assertThat(actualMatcher.toString(), allOf(
        containsString(actualMatcher.getPattern()), 
        containsString("allowDirs"),
        containsString("allowFiles"),
        containsString("flags")));
    
    assertThat(actualMatcher.getPattern(), is(not(nullValue())));
    final String patternNoSyntax = syntaxAndPattern.split(":", 2)[1];
    
    if (syntaxAndPattern.startsWith("regex:")) {
      if (isUnix) {
        assertThat(actualMatcher.getPattern(), is(equalTo(patternNoSyntax)));
      } else {
        assertThat(actualMatcher.getPattern(), is(not(equalTo(patternNoSyntax))));
      }
    }
    
    // all ContextHierarchicalMatcher's methods should not produce exceptions!
    actualMatcher.matches(getValidInstanceOf(pathType, isAssumedPathType), PathContexts.<T>emptyPathContext());
    actualMatcher.matchesDirectory(getValidInstanceOf(pathType, isAssumedPathType), PathContexts.<T>emptyPathContext());
  }

  @SuppressWarnings("null")
  private <T> T getValidInstanceOf(Class<T> pathType, final boolean isAssumedPathType) {
    if (pathType == String.class) {
      return pathType.cast("");
    } else if (pathType == Path.class) {
      return pathType.cast(Paths.get(""));
    } else if (pathType == File.class) {
      return pathType.cast(Paths.get("").toFile());
    } else if (!isAssumedPathType) {
      try {
        return pathType.getConstructor().newInstance();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else { // we should not be here!
      throw new RuntimeException("an unexpected pathType");
    }
  }
}
