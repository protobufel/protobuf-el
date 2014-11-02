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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeThat;
import static com.github.protobufel.common.verifications.Verifications.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.protobufel.common.files.ContextPathMatchers.SimpleHierarchicalMatcher;

@NonNullByDefault
@RunWith(Theories.class)
public class GlobRegexTest {
  @SuppressWarnings({ "unused", "null" })
  private static final Logger log = LoggerFactory.getLogger(GlobRegexTest.class);

  @DataPoints("globs") 
  public static final Object[] globs = new String[][] {
      // "part1*/?part2?/*/part3{_1,_2,_3}/**/part4.extra/*.ext"
      new String[] {"part1*/?part2?/*/part3{_1,_2,_3}/*/part4.extra/*.ext", "/part4.extra/1.ext1"}
      }; 

  @DataPoints("testPaths")
  public static final String[] testPaths = new String[] {
    //"part1Hello/Apart2Z/anything/part3_1/any1/any2/part4.extra/"
    "part1Hello/Apart2Z/anything/part3_1/any1/part4.extra/"
    };

  @Rule public TemporaryFolder temp = new TemporaryFolder();

  @After
  public void tearDown() throws Exception {
  }
  
  @Test
  public void testConvertNormalizedRelativeRegexToWindows() throws Exception {
    final String normalizedRelativeRegex = 
        "[\\[/]/part1/part2[abc//de/]part3\\\\[123/45\\]67/]part4[\\Q]/[\\E]part5\\Q/[]/\\E";
    final String expectedRegex = 
        "[\\[/]\\\\part1\\\\part2[abc//de/]part3\\\\[123/45\\]67/]part4[\\Q]/[\\E]part5\\Q/[]/\\E";
    
    // make sure patterns are valid!
    @SuppressWarnings("unused")
    final Pattern normalizedRelativePattern = Pattern.compile(normalizedRelativeRegex);
    @SuppressWarnings("unused")
    final Pattern expectedPattern = Pattern.compile(expectedRegex);
    
    final String actualRegex = new SimpleHierarchicalMatcher<Path>(true, "", true, true)
        .convertNormalizedRelativeRegexToWindows(normalizedRelativeRegex);
    assertThat(actualRegex, equalTo(expectedRegex));
  }

  @Ignore
  @Theory(nullsAccepted = false)
  public void parentDirsShouldMatchPartially(final @FromDataPoints("globs") String[] globData, 
      final @FromDataPoints("testPaths") String testString) throws IOException {
    final Path testPath = Paths.get(testString);
    final Path testDir = testPath.toString().endsWith("/") 
        ? testPath.subpath(0, testPath.getNameCount() - 2) 
            : testPath;
    Files.createDirectories(temp.getRoot().toPath().resolve(testDir));
    verifyCondition(globData.length == 2);
    final String glob = assertNonNull(globData[0]);
    final String noMatchEnd = convertToSystemPath(assertNonNull(globData[1]));
    final boolean isUnix = "/".equals(testPath.getFileSystem().getSeparator());
    final String regex = isUnix ? Globs.toUnixRegexPattern(glob) : Globs.toWindowsRegexPattern(glob);
    final Pattern pattern = Pattern.compile(regex.substring(1, regex.length() - 1));
    
    final Matcher matcher = pattern.matcher(testPath.toString());
    assertThat(matcher.matches(), is(false));
    
    for (Path path = testDir; path != null; path = path.getParent()) {
      assumeThat(testPath.toString(), startsWith(path.toString()));
      matcher.reset(path.toString());
      assertThat(matcher.matches(), is(false));
      assertThat(matcher.hitEnd(), is(true));
    }

    final String separator = testDir.getFileSystem().getSeparator();

    for (Path path = testDir; path != null; path = path.getParent()) {
      assumeThat(testPath.toString(), startsWith(path.toString()));
      final String unmatchable = path.toString() + noMatchEnd + separator;
      final Matcher submatcher = pattern.matcher(unmatchable);
      assertThat(submatcher.matches(), is(false));
      assertThat(submatcher.hitEnd(), is(false));
    }
  }
  
  @SuppressWarnings("null")
  private String convertToSystemPath(final String path) {
    return Utils.isUnix() ? path : path.replaceAll("/", "\\\\");
  }
}
