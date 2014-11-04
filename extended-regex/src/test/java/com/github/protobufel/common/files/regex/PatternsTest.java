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

package com.github.protobufel.common.files.regex;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import org.eclipse.jdt.annotation.NonNullByDefault;

@RunWith(JUnit4.class)
@NonNullByDefault(false)
//TODO provide additional thorough tests for all Patterns' classes! 
public class PatternsTest {
  private Pattern expected;
  private int expectedFlags;

  @Before
  public void setUp() throws Exception {
    // Given
    expected = Pattern.compile("hello", 
        Pattern.CASE_INSENSITIVE 
        | Pattern.UNIX_LINES 
        | Pattern.MULTILINE 
        | Pattern.DOTALL 
        | Pattern.UNICODE_CASE
        | Pattern.COMMENTS
        // | Pattern.UNICODE_CHARACTER_CLASS
        );
    expectedFlags = expected.flags();
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void patternFlagsInlinedEqualToSet() {
    // When
    //Pattern actual = Pattern.compile("(?idmsuxU)hello"); // for Java >= 7
    Pattern actual = Pattern.compile("(?idmsux)hello");
    int actualFlags = actual.flags();

    //Then
    assertThat(actualFlags, equalTo(expectedFlags));
  }

  @Test
  public void patternFlagsInlinedEqualToSet2() {
    // When
    Pattern actual = Pattern.compile("he(?idmsux)llo");
    int actualFlags = actual.flags();

    //Then
    assertThat(actualFlags, equalTo(expectedFlags));
  }

  @Test
  public void patternFlagsInlinedEqualToSet3() {
    // When
    Pattern actual = Pattern.compile("he(?idmsu)l(?x)lo");
    int actualFlags = actual.flags();

    //Then
    assertThat(actualFlags, equalTo(expectedFlags));
  }

  @Test
  public void patternFlagsInlinedEqualToSet4() {
    // When
    Pattern actual = Pattern.compile("he(?idmsu)l(?-x)l(?x)o");
    int actualFlags = actual.flags();

    //Then
    assertThat(actualFlags, equalTo(expectedFlags));
  }
  
  @Test
  public void patternFlagsInlinedEqualToSet5() {
    // When
    Pattern actual = Pattern.compile("he(?idmsu)l(?x)l(?-x)o");
    int actualFlags = actual.flags();

    //Then
    assertThat(actualFlags, not(equalTo(expectedFlags)));
  }
  
  @Test
  public void patternFlagsInlinedEqualToSet6() {
    // When
    Pattern actual = Pattern.compile("he(?idmsu)l(?x:o)");
    int actualFlags = actual.flags();

    //Then
    assertThat(actualFlags, not(equalTo(expectedFlags)));
  }
}
