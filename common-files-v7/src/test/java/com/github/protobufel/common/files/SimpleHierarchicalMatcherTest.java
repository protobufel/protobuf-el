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

import static org.junit.Assert.fail;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.runner.RunWith;

import com.github.protobufel.common.files.ContextPathMatchers.SimpleHierarchicalMatcher;

@RunWith(Theories.class)
public class SimpleHierarchicalMatcherTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testEqualsContract() throws Exception {
    EqualsVerifier.forClass(SimpleHierarchicalMatcher.class)
        .suppress(Warning.NULL_FIELDS) // non-nullness is by contract!
        .verify();
  }

  @Ignore
  @Test
  public void testResolvePath() {
    fail("Not yet implemented");
  }

  @Ignore
  @Test
  public void testMatches() {
    fail("Not yet implemented");
  }

  @Ignore
  @Test
  public void testMatchesDirectory() {
    fail("Not yet implemented");
  }

  @Ignore
  @Test
  public void testConvertGlobToRegex() {
    fail("Not yet implemented");
  }

  @Ignore
  @Test
  public void testConvertRegexToSystemSpecific() {
    fail("Not yet implemented");
  }

  @Ignore
  @Test
  public void testConvertNormalizedRelativeRegexToWindows() {
    fail("Not yet implemented");
  }
  
  @Ignore
  @Test
  public void testGetPattern() {
    fail("Not yet implemented");
  }

  @Ignore
  @Test
  public void testToString() {
    fail("Not yet implemented");
  }

  @Ignore
  @Test
  public void testToStringT() {
    fail("Not yet implemented");
  }

  @Ignore
  @Test
  public void testGetSeparator() {
    fail("Not yet implemented");
  }

  @Ignore
  @Test
  public void testGetSeparatorForPath() {
    fail("Not yet implemented");
  }

  @Ignore
  @Test
  public void testIsUnix() {
    fail("Not yet implemented");
  }

  @Ignore
  @Test
  public void testGetPatternFor() {
    fail("Not yet implemented");
  }

  @Ignore
  @Test
  public void testIsMultiTypeFileSystemSupported() {
    fail("Not yet implemented");
  }

  @Ignore
  @Test
  public void testGetAlternativePattern() {
    fail("Not yet implemented");
  }

  @Ignore
  @Test
  public void testIsEmpty() {
    fail("Not yet implemented");
  }
}
