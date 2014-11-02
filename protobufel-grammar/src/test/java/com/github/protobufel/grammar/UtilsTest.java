//
// Copyright © 2014, David Tesler (https://github.com/protobufel)
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
// * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
// * Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
// * Neither the name of the <organization> nor the
// names of its contributors may be used to endorse or promote products
// derived from this software without specific prior written permission.
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

package com.github.protobufel.grammar;

import static com.github.protobufel.grammar.ParserUtils.getLineCount;
import static com.github.protobufel.grammar.ParserUtils.getTrimmedBlockCommentContent;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(JUnit4.class)
public class UtilsTest {
  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(UtilsTest.class);

  @Rule
  public ErrorCollector errors = new ErrorCollector();

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testGetLineCount() throws Exception {
    String text;
    int count;

    text = "";
    count = getLineCount(text);
    errors.checkThat(count, is(0));

    text = "1";
    count = getLineCount(text);
    errors.checkThat(count, is(0));

    text = "\n";
    count = getLineCount(text);
    errors.checkThat(count, is(1));

    text = "\r";
    count = getLineCount(text);
    errors.checkThat(count, is(0));

    text = "\nline2\r\n\rno line\n";
    count = getLineCount(text);
    errors.checkThat(count, is(3));
  }

  @Test
  public void testAppendComment() throws Exception {
    String expected;
    String actual;

    expected = "    First Line!" + "\n*" + "\n123" + "\n" + "\n";
    actual = "/*    First Line!" + "\n  **" + "\n*123" + "\n*" + "\n*/";
    actual = getTrimmedBlockCommentContent(actual);
    errors.checkThat("'space*' case", actual, equalTo(expected));

    expected = "    First Line!" + "\n\r" + "\n" + "\n" + "\n";
    actual = "/*    First Line!" + "\n \t\r" + "\n \r\t" + "\n\r\r\t " + "\n*/";
    actual = getTrimmedBlockCommentContent(actual);
    errors.checkThat("'space*blank' case", actual, equalTo(expected));

    expected = "    First Line!" + "\n1" + "\n(" + "\n\r" + "\n";
    actual = "/*    First Line!" + "\n \t\r1" + "\n \r\t\r(" + "\n\r\r\t\r\r" + "\n*/";
    actual = getTrimmedBlockCommentContent(actual);
    errors.checkThat("'space*\\r{not last!}' case", actual, equalTo(expected));
  }
}
