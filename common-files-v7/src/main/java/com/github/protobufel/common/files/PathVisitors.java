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

import static com.github.protobufel.common.verifications.Verifications.assertNonNull;
import static com.github.protobufel.common.verifications.Verifications.verifyArgument;
import static com.github.protobufel.common.verifications.Verifications.verifyNonNull;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;

import com.github.protobufel.common.files.ContextPathMatchers.ContextHierarchicalMatcher;
import com.github.protobufel.common.files.ContextResourcePathMatchers.CompositePathMatcher;
import com.github.protobufel.common.files.ContextResourcePathMatchers.FileSetPathMatcher;
import com.github.protobufel.common.files.HistoryCaches.HistoryCache;
import com.github.protobufel.common.files.PathContexts.BasePathContext;
import com.github.protobufel.common.files.PathContexts.PathContext;
import com.github.protobufel.common.files.PathContexts.SimplePathContext;
import com.github.protobufel.common.files.resources.Resources.IFileSet;

@NonNullByDefault
public class PathVisitors {

  private PathVisitors() {}
  
  public static class ResourceFileException extends RuntimeException {
    private static final long serialVersionUID = 4984405890116628621L;

    private ResourceFileException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
    }

    private ResourceFileException(String message, Throwable cause) {
      super(message, cause);
    }

    private ResourceFileException(String message) {
      super(message);
    }

    private ResourceFileException(Throwable cause) {
      super(cause);
    }
  }
  
  public static Iterable<Path> getResourceFiles(final Path rootDir, 
      final IFileSet fileSet, final boolean followLinks, final boolean allowDuplicates, 
      final Logger log) {
    return getResourceFiles(rootDir, assertNonNull(Collections.singleton(fileSet)), followLinks, 
        allowDuplicates, log);
  }

  public static Iterable<Path> getResourceFiles(final Path rootDir, 
      final Iterable<? extends IFileSet> fileSets, final boolean followLinks, 
      final boolean allowDuplicates, final Logger log) {
    final List<FileSetPathMatcher<Path>> matchers = new ArrayList<>(); 
    
    for (IFileSet fileSet : verifyNonNull(fileSets)) {
      @SuppressWarnings("null")
      final @NonNull Path dir = Paths.get(verifyNonNull(fileSet).getDirectory());
      final FileSetPathMatcher<Path> matcher = new FileSetPathMatcher<Path>(fileSet.getIncludes(),
          fileSet.getExcludes(), dir, fileSet.isAllowDirs(), fileSet.isAllowFiles());
      matchers.add(matcher);
    }

    final CompositePathMatcher<Path, FileSetPathMatcher<Path>> matcher = new CompositePathMatcher<>(matchers);
    final Set<Path> result = new LinkedHashSet<>();

    try {
      final EnumSet<FileVisitOption> options = followLinks 
          ? EnumSet.of(FileVisitOption.FOLLOW_LINKS) 
              : EnumSet.noneOf(FileVisitOption.class);
      final ResourceVisitor<Path> resourceVisitor = new ResourceVisitor<>(matcher, allowDuplicates, 
          result, log, rootDir);
      Files.walkFileTree(rootDir, options, Integer.MAX_VALUE, resourceVisitor);
      
      if (!resourceVisitor.getErrorMessage().isEmpty()) {
        throw new ResourceFileException(resourceVisitor.getErrorMessage());
      }
      
      @SuppressWarnings("null")
      final @NonNull Set<Path> unmodifiableSet = Collections.unmodifiableSet(result);
      return unmodifiableSet;
    } catch (Exception e) {
      log.error("fileSet caused error", e);
      throw new ResourceFileException("fileSet caused error", e);
    }
  }

  public static final class ResourceVisitor<T> extends SimpleFileVisitor<T> {
    private static final int MATCHER_CACHE_SIZE = 1024;
    private static boolean useEmptyCache = true; // for testing only
    private final ContextHierarchicalMatcher<T> matcher;
    private final boolean allowDuplicates;
    private final Set<T> result;
    private String errorMessage;
    private final Logger log;
    private boolean skipRoot = true;
    private final IHistoryCache<Object, Object> matcherCache;
    private final PathContext<T> pathContext;

    public ResourceVisitor(final ContextHierarchicalMatcher<T> matcher, final boolean allowDuplicates, 
        final Set<T> result, final Logger log, T rootDir) {
      this(matcher, allowDuplicates, result, log, MATCHER_CACHE_SIZE, rootDir);
    }

    public ResourceVisitor(final ContextHierarchicalMatcher<T> matcher, final boolean allowDuplicates, 
        final Set<T> result, final Logger log, final int maxCacheSize, final T rootDir) {
      this.matcher = verifyNonNull(matcher);
      this.matcherCache = getHistoryCache(matcher, maxCacheSize);
      this.result = verifyNonNull(result);
      this.log = verifyNonNull(log);
      this.allowDuplicates = allowDuplicates;
      this.errorMessage = "";
      this.pathContext = getPathContext(verifyNonNull(rootDir));
    }
    
    protected PathContext<T> getPathContext(T root) {
      if (root instanceof Path) {
        @SuppressWarnings("unchecked")
        final PathContext<T> pathContext = (PathContext<T>) new SimplePathContext(
            matcherCache.getCacheView(), (Path) root);
        return pathContext;
      }
      
      return new BasePathContext<T>(matcherCache.getCacheView(), root);
    }
    
    static boolean isUseEmptyCache() {
      // for testing only
      return useEmptyCache;
    }

    static void setUseEmptyCache(final boolean useEmptyCache) {
      // for testing only
      ResourceVisitor.useEmptyCache = useEmptyCache;
    }

    private IHistoryCache<Object, Object> getHistoryCache(
        final ContextHierarchicalMatcher<T> matcher, final int maxCacheSize) {
      final IHistoryCache<Object, Object> matcherCache;
      
      if (isUseEmptyCache()) {
        matcherCache = HistoryCaches.fakeInstance();
      } else {
        matcherCache = new HistoryCache<>(verifyArgument(maxCacheSize > 0, maxCacheSize));
      }
      
      return matcherCache;
    }

    @Override
    @NonNullByDefault(false)
    public FileVisitResult preVisitDirectory(T dir, BasicFileAttributes attrs)
        throws IOException {
      super.preVisitDirectory(dir, attrs); // dir, attrs are checked for nonNull!
      @SuppressWarnings("null")
      final @NonNull T nonNullDir = dir;
      //TODO either use or remove!
      @SuppressWarnings({ "unused", "null" })
      final @NonNull BasicFileAttributes nonNullAttrs = attrs;
      
      if (skipRoot) {
        skipRoot = false;
        return FileVisitResult.CONTINUE;
      } 
      
      matcherCache.push();
      final DirectoryMatchResult matcherResult = matcher.matchesDirectory(nonNullDir, pathContext);
      
      if (matcherResult.isMatched()) {
        if (!result.add(nonNullDir) && !allowDuplicates) {
          @SuppressWarnings("null")
          @NonNull String nonNullErrorMsg = String.format("found a duplicate folder %s", nonNullDir);
          errorMessage = nonNullErrorMsg;
          log.error(errorMessage);
          matcherCache.clear();
          return FileVisitResult.TERMINATE;
        }
      }
      
      if (matcherResult.isSkip()) {
        matcherCache.pop();
        return FileVisitResult.SKIP_SUBTREE;
      } else {
        return FileVisitResult.CONTINUE;
      }  
    }

    @Override
    @NonNullByDefault(false)
    public FileVisitResult postVisitDirectory(T dir, IOException exc) throws IOException {
      super.postVisitDirectory(dir, exc);
      matcherCache.pop();
      return FileVisitResult.CONTINUE;
    }
    
    @Override
    @NonNullByDefault(false)
    public FileVisitResult visitFile(T file, BasicFileAttributes attrs) throws IOException {
      super.visitFile(file, attrs);
      @SuppressWarnings("null")
      final @NonNull T nonNullFile = file;

      if (matcher.matches(nonNullFile, pathContext)) {
        if (!result.add(nonNullFile) && !allowDuplicates) {
          @SuppressWarnings("null")
          final @NonNull String nonNullErrorMsg = String.format("found a duplicate file %s", 
              nonNullFile);
          errorMessage = nonNullErrorMsg;
          log.error(errorMessage);
          matcherCache.clear();
          return FileVisitResult.TERMINATE;
        }
      }

      return FileVisitResult.CONTINUE;
    }

    @Override
    @NonNullByDefault(false)
    public FileVisitResult visitFileFailed(T file, IOException exc) throws IOException {
      log.error(String.format("file %s cannot be visited", verifyNonNull(file)), exc);
      throw exc;
    }
    
    public String getErrorMessage() {
      return errorMessage;
    }
  }
}
