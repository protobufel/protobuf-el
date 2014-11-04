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

package com.github.protobufel.resource.exec.maven.plugin;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.MojoExecutionException;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.protobufel.resource.exec.maven.plugin.ExecMojo;
import com.github.protobufel.resource.exec.maven.plugin.Jsr330Component;

@RunWith(MockitoJUnitRunner.class)
public class ExecMojoTest {
  private static final Logger log = LoggerFactory.getLogger(ExecMojoTest.class);

  @Mock 
  private Jsr330Component component;
  
  @Rule 
  public TemporaryFolder temp = new TemporaryFolder();
  
  private File resultFile;
  private File errorFile;
  private List<String> args;
  private File execLocation;
  private ExecMojo mojo;
  private Map<String, String> environment = Collections.emptyMap();
  private File workDir;
  private Path rootDir;

  private String glob;
  
  @Before
  public void setUp() throws Exception {
    // given
    final Properties properties = new Properties();
    
    try (InputStream in = getClass().getResourceAsStream("test.properties")) {
      properties.load(in);
    }
    
    execLocation = new File(properties.getProperty("com.google.protobuf.protoc.path"));
    
    errorFile = temp.newFile("errrors.txt");
    resultFile = temp.newFile("result");
    workDir = temp.newFolder();
    
    rootDir = Paths.get(getClass().getResource("protoc-errors").toURI());
    glob = "*.proto";
    args = new ArrayList<>(Arrays.asList(
        "--proto_path=" + rootDir.toRealPath().toString(),
        "--descriptor_set_out=" + resultFile.getCanonicalPath(),
        "--include_imports"
        ));

    mojo = new ExecMojo(component);
    mojo.setExecLocation(execLocation);
    mojo.setEnvironment(environment);
    mojo.setErrorFile(errorFile);
    mojo.setRedirectErrorStream(false);
    mojo.setErrorInherit(false);
    mojo.setErrorPipe(true);
    mojo.setErrorProperty("");
    mojo.setErrorAppend(false);
    mojo.setOutAppend(false);
    mojo.setOutFile(null);
    mojo.setOutInherit(false);
    mojo.setOutPipe(true);
    mojo.setOutProperty("");
    mojo.setWorkDir(workDir);
    mojo.setAllowDuplicates(true);
    mojo.setAllowFiles(true);
    mojo.setAllowDirs(false);
    mojo.setFileSets(Collections.<FileSet>emptyList());
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testExcecMojo() {
  }

  @Test
  public void executeWithFileArgs() throws IOException {
    try {
      addFileArgs(rootDir, glob, args);
      mojo.setArgs(args);
      mojo.execute();
    } catch (MojoExecutionException e) {
      fail(e.getLongMessage());
    }
    
    assertThat("the errorFile is not readable", errorFile.canRead());
    assertThat("the errorFile is a directory", errorFile.isFile());
    
    List<String> actualErrorContents = null;
    
    try {
      actualErrorContents = Files.readAllLines(errorFile.toPath(), Charset.defaultCharset());
    } catch (IOException e) {
      fail(e.getMessage());
    }
    
    assertThat(actualErrorContents, hasItem(not(isEmptyOrNullString())));
  }

  @Test
  public void executeWithFileSets() throws IOException {
    try {
      mojo.setArgs(args);
      mojo.setFileSets(getFileSets(rootDir));
      mojo.execute();
    } catch (MojoExecutionException e) {
      fail(e.getLongMessage());
    }
    
    assertThat("the errorFile is not readable", errorFile.canRead());
    assertThat("the errorFile is a directory", errorFile.isFile());
    
    List<String> actualErrorContents = null;
    
    try {
      actualErrorContents = Files.readAllLines(errorFile.toPath(), Charset.defaultCharset());
    } catch (IOException e) {
      fail(e.getMessage());
    }
    
    assertThat(actualErrorContents, hasItem(not(isEmptyOrNullString())));
  }

  @Test
  public void testGetResourceFiles() throws Exception {
    Collection<String> actualFiles = mojo.getResourceFiles(getFileSets(rootDir), 
        mojo.isFollowLinks(), true, true, false);
    assertThat(actualFiles, is(not(nullValue())));
    assertThat(actualFiles, everyItem(not(matchesRegex(".*excl.de.*[.]proto"))));
  }
  
  private List<FileSet> getFileSets(final Path rootDir) 
      throws IOException {
    List<FileSet> fileSets = new ArrayList<>();
    
    FileSet fileSet = new FileSet();
    fileSet.setDirectory(rootDir.toString());
    fileSet.addInclude("*.proto");
    fileSet.addExclude("excl?de?.proto");
    fileSets.add(fileSet);
    
    fileSet = new FileSet();
    fileSet.setDirectory(rootDir.toString());
    fileSet.addInclude("*.proto");
    fileSet.addExclude("exclude*.proto");
    fileSets.add(fileSet);
    
    return fileSets;
  }

  private void addFileArgs(final Path rootDir, final String glob, final List<String> args) 
      throws IOException {
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootDir, glob)) {
      for (Path path : stream) {
        args.add(path.toRealPath().toString());
      }
    } catch (DirectoryIteratorException ex) {
      throw ex.getCause();
    }
  }
  
  public static final Matcher<String> matchesRegex(final String regex) {
    return new RegexMatcher(regex);
  }
  
  public static final class RegexMatcher extends TypeSafeMatcher<String> {
    private final Pattern pattern;

    private RegexMatcher(final String regex) {
      this.pattern = Pattern.compile(Objects.requireNonNull(regex));
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("regex of '" + pattern.pattern() + "'");
    }

    @Override
    protected boolean matchesSafely(final String item) {
      return pattern.matcher(item).matches();
    }
  }
}
