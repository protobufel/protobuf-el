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

package com.github.protobufel.resource.exec.maven.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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
import org.assertj.core.api.JUnitSoftAssertions;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(JUnit4.class)
public class ExecMojoTest {
  private static final Logger log = LoggerFactory.getLogger(ExecMojoTest.class);
  private static final List<String> SUBDIRS = Arrays.asList("dir1", "dir2/dir21/dir211",
      "dir2/dir22/dir221", "dir2/dir22/dir222/dir2221");
  private static final List<String> SUBFILES = Arrays.asList("file1", "file2.txt", "file3.ok",
  // "file4." will become file4 on Windows!
      "file4");
  private static final String GLOB_ALL = "**/*";
  private static final List<String> BAD_ARGS = Arrays.asList(
  // "dir1/file1", // good WEIRD: Windows del won't report error in presence of any good file!
      "dir1/non.existent.file" // bad
      );

  @Mock
  private Jsr330Component component;

  public final TemporaryFolder temp = new TemporaryFolder();
  public final ExpectedException expected = ExpectedException.none();
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  @Rule
  public final TestRule chain = RuleChain.outerRule(expected).around(new MockitoJUnitRule(this))
      .around(temp).around(softly);

  private File outFile;
  private File errorFile;
  private List<String> args;
  private String execLocation;
  private List<String> execArgs;
  private ExecMojo mojo;
  private final Map<String, String> environment = Collections.emptyMap();
  private File workDir;
  private Path rootDir;
  private List<String> expectedFiles;

  private String glob;

  @Before
  public void setUp() throws Exception {
    // given
    final Properties properties = new Properties();

    try (InputStream in = getClass().getResourceAsStream("test.properties")) {
      properties.load(in);
    }

    execLocation = properties.getProperty("plugin.test.exec");
    assertThat(execLocation).isIn("rm", "del");

    // errorFile = temp.newFile("errors.txt");
    errorFile = new File(temp.getRoot(), "errors.txt");
    // outFile = temp.newFile("result");
    outFile = new File(temp.getRoot(), "result");
    workDir = temp.newFolder();
    expectedFiles = makeTestTree(workDir.toPath(), SUBDIRS, SUBFILES);

    rootDir = workDir.toPath();
    glob = GLOB_ALL;
    args = new ArrayList<>();

    mojo = new ExecMojo(component);
    mojo.setExecLocation(execLocation);
    mojo.setExecLocationAsIs(true);
    mojo.setSystemCommand(true);
    mojo.setArgQuote("\"");
    mojo.setEnvironment(environment);
    mojo.setErrorFile(errorFile);
    mojo.setRedirectErrorStream(false);
    mojo.setErrorInherit(false);
    mojo.setErrorPipe(true);
    mojo.setErrorProperty("");
    mojo.setErrorAppend(false);
    mojo.setOutAppend(false);
    mojo.setOutFile(outFile);
    mojo.setOutInherit(false);
    mojo.setOutPipe(true);
    mojo.setOutProperty("");
    mojo.setWorkDir(workDir);
    mojo.setAllowDuplicates(true);
    mojo.setAllowFiles(true);
    mojo.setAllowDirs(false);
    mojo.setFileSets(Collections.<FileSet>emptyList());
  }

  private List<String> makeTestTree(final Path rootDir, final Iterable<String> subDirs,
      final Iterable<String> files) throws IOException {
    final Path realDir = Files.createDirectories(Objects.requireNonNull(rootDir));

    for (final String subDir : Objects.requireNonNull(subDirs)) {
      Files.createDirectories(realDir.resolve(Objects.requireNonNull(subDir)));
    }

    final List<String> allFiles = new ArrayList<>();
    Files.walkFileTree(rootDir, new SimpleFileVisitor<Path>() {
      @NonNullByDefault(false)
      @Override
      public FileVisitResult postVisitDirectory(final Path dir, final IOException exc)
          throws IOException {
        super.postVisitDirectory(dir, exc);

        if (!dir.equals(realDir)) {
          for (final String file : files) {
            final Path filePath = dir.resolve(file);

            if (Files.notExists(filePath)) {
              allFiles.add(Files.createFile(filePath).toAbsolutePath().toString());
            }
          }
        }

        return FileVisitResult.CONTINUE;
      }
    });

    Collections.sort(allFiles);
    return Collections.unmodifiableList(allFiles);
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testGetResourceFiles() throws Exception {
    final Collection<String> actualFiles =
        mojo.getResourceFiles(getFileSets(rootDir), mojo.isFollowLinks(), true, true, false);

    assertThat(actualFiles).isNotNull().doesNotContainNull().hasSameSizeAs(expectedFiles)
        .containsOnlyElementsOf(expectedFiles);

    // the following is the same as above, but easy to compare
    // final ArrayList<String> actualSortedFiles = new ArrayList<String>(actualFiles);
    // Collections.sort(actualSortedFiles);
    // assertThat(actualSortedFiles).isEqualTo(expectedFiles);
  }

  @Test
  public void executeWithFileArgsGood() throws IOException {
    try {
      addFileArgs(rootDir, glob, args);
      mojo.setArgs(args);
      mojo.execute();
    } catch (final MojoExecutionException e) {
      fail(e.getLongMessage());
    }

    assertThat(contentOf(errorFile)).as("the errorFile is empty").hasSize(0);
    assertDeepEmpty(rootDir);
  }

  @Test
  public void executeWithFileArgsBad() throws IOException {
    try {
      addFileArgs(rootDir, BAD_ARGS, args);
      mojo.setArgs(args);
      mojo.execute();
    } catch (final MojoExecutionException e) {
      fail(e.getLongMessage());
    }

    final String outFileText = contentOf(outFile);
    final String errorFileText = contentOf(errorFile);
    log.debug("errorFileText='{}'", errorFileText);
    assertThat(errorFileText).as("the errorFile's contents").contains("non.existent.file");
  }

  private void assertDeepEmpty(final Path dir) {
    try {
      Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
            throws IOException {
          fail(String.format("directory '%s' is not deeply empty - found file '%s'", dir, file));
          return super.visitFile(file, attrs);
        }
      });
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void executeWithFileSetsGood() throws IOException {
    try {
      mojo.setArgs(args);
      mojo.setFileSets(getFileSets(rootDir));
      mojo.execute();
    } catch (final MojoExecutionException e) {
      fail(e.getLongMessage());
    }

    assertThat(contentOf(errorFile)).as("the errorFile is empty").hasSize(0);
    assertDeepEmpty(rootDir);
  }

  @Test
  public void executeWithFileSetsWithNonExistentIncludes() throws IOException {
    try {
      mojo.setArgs(args);
      mojo.setFileSets(getFileSetsWithNonExistentIncludes(rootDir));
      mojo.execute();
    } catch (final MojoExecutionException e) {
      fail(e.getLongMessage());
    }

    assertThat(contentOf(errorFile)).as("the errorFile is empty").hasSize(0);
    assertDeepEmpty(rootDir);
  }

  @Test
  public void executeWithFileSetsWithDuplicatesAndAllowDuplicates() throws IOException {
    try {
      mojo.setAllowDuplicates(true);
      mojo.setArgs(args);
      mojo.setFileSets(getFileSetsWithDuplicates(rootDir));
      mojo.execute();
    } catch (final MojoExecutionException e) {
      fail(e.getLongMessage());
    }

    // weirdly, Windows del command okay with duplicate files as args
    // assertThat(contentOf(errorFile)).as("the errorFile's contents").matches(".*file4.*");
    assertThat(contentOf(errorFile)).as("the errorFile is empty").hasSize(0);
    assertDeepEmpty(rootDir);
  }

  @Test
  public void executeWithFileSetsWithDuplicatesAndDisallowDuplicates() throws IOException,
      MojoExecutionException {
    mojo.setAllowDuplicates(false);
    mojo.setArgs(args);
    mojo.setFileSets(getFileSetsWithDuplicates(rootDir));

    expected.expect(MojoExecutionException.class);
    expected.expectMessage("a duplicate file");
    expected.expectMessage("file4");
    mojo.execute();
  }

  private List<FileSet> getFileSets(final Path rootDir) throws IOException {
    final List<FileSet> fileSets = new ArrayList<>();

    FileSet fileSet = new FileSet();
    fileSet.setDirectory(rootDir.toString());
    fileSet.addInclude("**/file?");
    fileSet.addExclude("**/file4");
    fileSets.add(fileSet);

    fileSet = new FileSet();
    fileSet.setDirectory(rootDir.toString());
    fileSet.addInclude("**/*");
    fileSet.addExclude("**/file?");
    fileSets.add(fileSet);

    fileSet = new FileSet();
    fileSet.setDirectory(rootDir.toString());
    fileSet.addInclude("**/file4");
    fileSets.add(fileSet);

    return fileSets;
  }

  private List<FileSet> getFileSetsWithNonExistentIncludes(final Path rootDir) throws IOException {
    final List<FileSet> fileSets = getFileSets(rootDir);

    final FileSet fileSet = new FileSet();
    fileSet.setDirectory(rootDir.toString());

    for (final String arg : BAD_ARGS) {
      fileSet.addInclude(arg);
    }

    fileSets.add(fileSet);
    return fileSets;
  }

  private List<FileSet> getFileSetsWithDuplicates(final Path rootDir) throws IOException {
    final List<FileSet> fileSets = getFileSets(rootDir);

    final FileSet fileSet = new FileSet();
    fileSet.setDirectory(rootDir.toString());
    fileSet.addInclude("**/file4");

    fileSets.add(fileSet);
    return fileSets;
  }

  private void addFileArgs(final Path rootDir, final String glob, final List<String> args)
      throws IOException {
    final PathMatcher pathMatcher = rootDir.getFileSystem().getPathMatcher("glob:" + glob);
    Files.walkFileTree(rootDir, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
          throws IOException {
        if (pathMatcher.matches(file)) {
          args.add(file.toRealPath().toString());
        }

        return super.visitFile(file, attrs);
      }
    });
  }

  private void addFileArgs(final Path rootDir, final List<String> files, final List<String> args) {
    for (final String file : files) {
      args.add(rootDir.resolve(file).toAbsolutePath().toString());
    }
  }

  public static final Matcher<String> matchesRegex(final String regex) {
    return new RegexMatcher(regex);
  }

  public static final class RegexMatcher extends TypeSafeMatcher<String> {
    private final Pattern pattern;

    private RegexMatcher(final String regex) {
      pattern = Pattern.compile(Objects.requireNonNull(regex));
    }

    @Override
    public void describeTo(final Description description) {
      description.appendText("regex of '" + pattern.pattern() + "'");
    }

    @Override
    protected boolean matchesSafely(final String item) {
      return pattern.matcher(item).matches();
    }
  }
}
