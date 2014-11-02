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

package com.github.protobufel.protoc.maven.plugin;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assume.*;

import java.nio.file.SimpleFileVisitor;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.security.SecureRandom;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;

import org.hamcrest.Matcher;
import org.hamcrest.collection.IsIn;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.ParameterSignature;
import org.junit.experimental.theories.ParameterSupplier;
import org.junit.experimental.theories.ParametersSuppliedBy;
import org.junit.experimental.theories.PotentialAssignment;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

@RunWith(Theories.class)
public class GlobFilterTheories {
  private static final Logger log = LoggerFactory.getLogger(GlobFilterTheories.class);
  
  private static final FileSystem DEFAULT_FS = FileSystems.getDefault();
  @DataPoints
  public static final Path[] dirs = { Paths.get("test1"), Paths.get("test2/test2/") };
  public static final List<String> subDirs = Arrays.asList(
      "dir1",
      "dir2/dir21/dir211",
      "dir2/dir22/dir221",
      "dir2/dir22/dir222/dir2221"
      );
  public static final List<String> subFiles = Arrays.asList(
      "file1",
      "file2.txt",
      "file3.ok",
      "file4."
      );
  
  @DataPoints
  public static SimpleImmutableEntry[] globs() {
    final ImmutableList.Builder<Map.Entry<String, ? extends Collection<Matcher<? super String>>>> builder = 
        ImmutableList.builder();
    builder
        .add(new SimpleImmutableEntry<>("dir2", 
            Collections.<Matcher<? super String>>singletonList(new IsIn<>(subFiles))))
        .add(new SimpleImmutableEntry<>("dir2/**/dir22?/dir*/*.*", 
            Collections.<Matcher<? super String>>singletonList(new IsIn<>(subFiles))))
        .add(new SimpleImmutableEntry<>("*",
            ImmutableList.<Matcher<? super String>>builder()
                .add(new IsIn<>(subFiles))
                .add(new IsIn<>(Arrays.asList("dir1", "dir2")))
                .build()))
        .add(new SimpleImmutableEntry<>("*.*", 
            Collections.<Matcher<? super String>>singletonList(new IsIn<>(subFiles))))
        ;
    return builder.build().toArray(new SimpleImmutableEntry<?, ?>[0]);
  }

  
  
//  @DataPoints
//  public static List<? extends Map.Entry<?, ?>> globs() {
//    final ImmutableList.Builder<Map.Entry<String, List<Matcher<? super String>>>> builder = 
//        ImmutableList.builder();
//    builder
//        .add(new SimpleImmutableEntry<>("*", 
//            Collections.<Matcher<? super String>>singletonList(new IsIn<>(subFiles))))
//        .add(new SimpleImmutableEntry<>("*.*", 
//            Collections.<Matcher<? super String>>singletonList(new IsIn<>(subFiles))))
//        .add(new SimpleImmutableEntry<>("dir2/**/dir22?/dir*/*.*", 
//            Collections.<Matcher<? super String>>singletonList(new IsIn<>(subFiles))))
//        ;
//    
//    ImmutableList<Entry<String, List<Matcher<? super String>>>> build = builder.build();
//    return build;
//  }

  
  //TODO make my own rule based on nio Files!
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();
  private Path tempPath;

  @Before
  public void setUp() throws Exception {
    tempPath = temp.getRoot().toPath();
    assumeThat(tempPath.getFileSystem(), equalTo(DEFAULT_FS));
  }

  @After
  public void tearDown() throws Exception {
  }

  @Theory(nullsAccepted = false)
  public void globShouldMatchPartially(
      // final Map.Entry<String, List<Matcher<? super String>>> glob, final Path relativeDir) 
      final Entry globParam, final Path relativeDir) 
          throws IOException {
    assumeThat(relativeDir.getFileSystem(), equalTo(tempPath.getFileSystem()));
    assumeThat(relativeDir, is(notNullValue()));
    assumeThat(relativeDir.isAbsolute(), is(false));
    
    final Path dir = tempPath.resolve(relativeDir).normalize();
    assumeThat(dir.isAbsolute(), is(true));

    makeTestTree(dir);
    
    @SuppressWarnings("unchecked")
    
    final Entry<String, Collection<Matcher<? super String>>> glob = globParam;
    final PathMatcher matcher = dir.getFileSystem().getPathMatcher("glob:" + glob.getKey());
    final List<String> actualMatches = new ArrayList<>(); 
    
    Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
      private boolean skipRoot = true;
      
      @Override
      public FileVisitResult preVisitDirectory(Path subDir, BasicFileAttributes attrs)
          throws IOException {
        super.preVisitDirectory(subDir, attrs);

        if (skipRoot) {
          skipRoot = false;
          return FileVisitResult.CONTINUE;
        }
        
        final Path path = dir.relativize(subDir);

        if (!matcher.matches(path)) {
          log.debug("for glob {} fails dir {}", glob.getKey(), subDir);
          return FileVisitResult.CONTINUE;
        }

        actualMatches.add(path.toString());
        log.debug("for glob {} found dir {}", glob.getKey(), subDir);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        super.visitFile(file, attrs);
        
        final Path path = dir.relativize(file);

        if (!matcher.matches(path)) {
          log.debug("for glob {} fails dir {}", glob.getKey(), file);
          return FileVisitResult.CONTINUE;
        }

        actualMatches.add(path.toString());
        log.debug("for glob {} found dir {}", glob.getKey(), file);
        return FileVisitResult.CONTINUE;
      }});
    
    Collection<Matcher<? super String>> expectedMatchers = glob.getValue();
    //assertThat(actualMatches, containsInAnyOrder(expectedMatchers));
  }
  
  private void makeTestTree(final Path dir) throws IOException {
    makeTestTree(dir, subDirs, subFiles);
  }

  private void makeTestTree(final Path dir, final Iterable<String> subDirs, 
      final Iterable<String> files) throws IOException {
    final Path realDir = Files.createDirectories(Objects.requireNonNull(dir));
    
    for (String subDir : Objects.requireNonNull(subDirs)) {
      final Path realSubDir = Files.createDirectories(realDir.resolve(Objects.requireNonNull(subDir)));
    }
    
    Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        super.postVisitDirectory(dir, exc);
        
        for (String file : files) {
          final Path filePath = dir.resolve(file);
          
          if (Files.notExists(filePath)) {
            Files.createFile(filePath);
          }
        }
        
        return FileVisitResult.CONTINUE;
      }});
  }
}
