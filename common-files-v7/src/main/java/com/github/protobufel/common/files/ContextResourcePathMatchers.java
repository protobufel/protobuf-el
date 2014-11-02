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

import static com.github.protobufel.common.files.Utils.isUnix;
import static com.github.protobufel.common.verifications.Verifications.*;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.github.protobufel.common.files.ContextPathMatchers.ContextHierarchicalMatcher;
import com.github.protobufel.common.files.ContextPathMatchers.HierarchicalMatcher;
import com.github.protobufel.common.files.PathContexts.PathContext;
import com.github.protobufel.common.files.HistoryCaches.SimpleHistoryCache;

@NonNullByDefault
public final class ContextResourcePathMatchers {
  private ContextResourcePathMatchers() {
  }
  
  public static class CompositePathMatcher<T, E extends ContextHierarchicalMatcher<T>> 
      implements ContextHierarchicalMatcher<T> {
    private static final CompositePathMatcher<?, ?> EMPTY = 
        new CompositePathMatcher<Object, ContextHierarchicalMatcher<Object>>();
    private final List<E> matchers;
    private final boolean allowDirs;
    private final boolean allowFiles;
    //private final transient SimpleHistoryCache<MatcherCacheData> cache;
    private final transient SimpleHistoryCache<IterableFilter> cache;

    private CompositePathMatcher() {
      matchers = assertNonNull(Collections.<E>emptyList());
      this.cache = SimpleHistoryCache.emptyCache();
      this.allowDirs = false;
      this.allowFiles = false;
    }

    public CompositePathMatcher(final Iterable<? extends E> matchers) {
      this(matchers, Integer.MAX_VALUE);
    }

    public CompositePathMatcher(final Iterable<? extends E> matchers, final int cacheSize) {
      final @NonNull Set<E> matcherSet = new HashSet<E>();
      final @NonNull List<E> matcherList = new ArrayList<E>();
      boolean allowDirs = false;
      boolean allowFiles = false;

      for (E matcher : verifyNonNull(matchers)) {
        if (!verifyNonNull(matcher).isEmpty()) {
          matcherSet.add(matcher); // there should be no equal matchers!
          matcherList.add(matcher);

          if (!allowDirs && matcher.isAllowDirs()) {
            allowDirs = true;
          }

          if (!allowFiles && matcher.isAllowFiles()) {
            allowFiles = true;
          }
        }
      }

      this.allowDirs = allowDirs;
      this.allowFiles = allowFiles;
      @SuppressWarnings("null")
      final @NonNull List<E> unmodifiableList = Collections.unmodifiableList(matcherList);
      this.matchers = unmodifiableList;
      this.cache = new SimpleHistoryCache<IterableFilter>(verifyArgument(cacheSize > 0, cacheSize));
    }

    @SuppressWarnings("unchecked")
    public static <T, E extends FileSetPathMatcher<T>> CompositePathMatcher<T, E> emptyInstance() {
      return (CompositePathMatcher<T, E>) EMPTY;
    }

    @Override
    public boolean isAllowDirs() {
      return allowDirs;
    }

    @Override
    public boolean isAllowFiles() {
      return allowFiles;
    }

    @Override
    public String getPattern() {
      if (matchers.isEmpty()) {
        return "[]";
      }
      
      final StringBuilder sb = new StringBuilder("[");
      
      for (E matcher : matchers) {
        sb.append(matcher.getPattern()).append(",");
      }
      
      sb.setCharAt(sb.length() - 1, ']');
      @SuppressWarnings("null")
      final @NonNull String result = sb.toString();
      return result;
    }

    @Override
    public boolean matches(final T path, final PathContext<T> context) {
      if (!allowFiles) {
        return false;
      }

      cache.adjustCache(context.currentDepth());
      final IterableFilter cacheData = getCacheData();
      
      for (int index : cacheData) {
        if (matchers.get(index).matches(path, context)) {
          return true;
        }
      }

      return false;
    }

    @Override
    public DirectoryMatchResult matchesDirectory(final T path, final PathContext<T> context) {
      if ((this == EMPTY) || matchers.isEmpty()) {
        return DirectoryMatchResult.NO_MATCH;
      }

      cache.adjustCache(context.currentDepth());
      final IterableFilter cacheData = getCacheData();
      
      if (cacheData.isEmpty()) {
        return DirectoryMatchResult.NO_MATCH;
      }
      
      final IterableFilter.Builder cacheBuilder = cacheData.toBuilder();
      boolean skip = true;
      boolean matched = false;
      
      for (int index : cacheData) {
        final DirectoryMatchResult matchResult = matchers.get(index)
            .matchesDirectory(path, context);
        
        if (matchResult.isMatched()) {
          matched = true;
          
          if (!matchResult.isSkip()) {
            // MATCH_CONTINUE
            skip = false;
            break;
          }
          
          cacheBuilder.clear(index);
          
          if (!skip) {
            // MATCH_CONTINUE
            break;
          }
        } else if (!matchResult.isSkip()) {
          skip = false;
          
          if (matched) {
            // MATCH_CONTINUE
            break;
          } else if (!allowDirs) {
            // exact match is not required, so we lazily stop searching further 
            // NO_MATCH_CONTINUE
            break;
          }
        } else {
          // this matcher didn't match anyhow, so remove it, and continue searching for match
          cacheBuilder.clear(index);
        }
      }
      
      final DirectoryMatchResult result = DirectoryMatchResult.valueOf(matched, skip);
      
      if (!skip) {
        cache.push(cacheBuilder.build());
      }
      
      return result;
    }

    private IterableFilter getCacheData() {
      if (cache.isEmpty()) {
        return IterableFilter.builder().resetAll(matchers).build();
      } else {
        return assertNonNull(cache.peek());
      }
    }

    @Override
    public boolean isEmpty() {
      return (this == EMPTY) || matchers.isEmpty();
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (allowDirs ? 1231 : 1237);
      result = prime * result + (allowFiles ? 1231 : 1237);
      result = prime * result + ((matchers == null) ? 0 : matchers.hashCode());
      return result;
    }

    @NonNullByDefault(false)
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof CompositePathMatcher)) {
        return false;
      }
      final CompositePathMatcher<?, ?> other = (CompositePathMatcher<?, ?>) obj;
      if (allowDirs != other.allowDirs) {
        return false;
      }
      if (allowFiles != other.allowFiles) {
        return false;
      }
      if (!matchers.equals(other.matchers)) {
        return false;
      }
      return true;
    }

    @Override
    public String toString() {
      @SuppressWarnings("null")
      @NonNull String result = new StringBuilder()
          .append("CompositePathMatcher [matchers=").append(matchers)
          .append(", allowDirs=").append(allowDirs)
          .append(", allowFiles=").append(allowFiles)
          .append("]").toString();
      return result;
    }
  }

  public static class FileSetPathMatcher<T> implements HierarchicalMatcher<T> {
    private static final FileSetPathMatcher<?> EMPTY = new FileSetPathMatcher<Object>();
    private final List<HierarchicalMatcher<T>> includes;
    private final List<HierarchicalMatcher<T>> excludes;
    private final T dir;
    private final boolean allowDirs;
    private final boolean allowFiles;
    private final transient SimpleHistoryCache<FileSetCacheData> cache;

    @SuppressWarnings("unchecked")
    public static <T> FileSetPathMatcher<T> emptyInstance() {
      return (FileSetPathMatcher<T>) EMPTY;
    }

    private FileSetPathMatcher() {
      @SuppressWarnings("null")
      final @NonNull List<HierarchicalMatcher<T>> emptyList = Collections.emptyList();
      this.includes = emptyList;
      this.excludes = emptyList;
      @SuppressWarnings("unchecked")
      final T t = (T) new Object(); // okay because only used for the EMPTY singleton
      this.dir = t;
      this.allowDirs = false;
      this.allowFiles = false;
      this.cache = SimpleHistoryCache.emptyCache();
    }

    public FileSetPathMatcher(final Collection<String> includes, final Collection<String> excludes, 
        final T dir, final boolean allowDirs, final boolean allowFiles) {
      this(includes, excludes, dir, allowDirs, allowFiles, Integer.MAX_VALUE);
    }

    public FileSetPathMatcher(final Collection<String> includes, final Collection<String> excludes, 
        final T dir, final boolean allowDirs, final boolean allowFiles, final int cacheSize) {
      verifyCondition((allowDirs || allowFiles), "allowDirs and allowFiles cannot be both false");
      this.cache = new SimpleHistoryCache<FileSetCacheData>(verifyArgument(cacheSize > 0, cacheSize));
      this.allowDirs = allowDirs;
      this.allowFiles = allowFiles;
      this.dir = verifyNonNull(dir);
      @SuppressWarnings({ "unchecked", "null" })
      final @NonNull Class<T> pathType = (Class<T>) dir.getClass();
      @SuppressWarnings("null")
      final @NonNull List<HierarchicalMatcher<T>> emptyList = Collections.emptyList();
      
      if (verifyNonNull(includes).isEmpty()) {
        this.includes = emptyList;
      } else {
        final List<HierarchicalMatcher<T>> includesList = 
            new ArrayList<HierarchicalMatcher<T>>(includes.size());

        for (String include : includes) {
          includesList.add(ContextPathMatchers.getHierarchicalMatcher(verifyNonNull(include), 
              isUnix(), allowDirs, allowFiles, pathType));
        }

        this.includes = assertNonNull(Collections.unmodifiableList(includesList));
      }

      if (verifyNonNull(excludes).isEmpty()) {
        this.excludes = emptyList;
      } else {
        final List<HierarchicalMatcher<T>> excludesList = 
            new ArrayList<HierarchicalMatcher<T>>(excludes.size());

        for (String exclude : excludes) {
          excludesList.add(ContextPathMatchers.getHierarchicalMatcher(verifyNonNull(exclude), 
              isUnix(), allowDirs, allowFiles, pathType));
        }

        this.excludes = assertNonNull(Collections.unmodifiableList(excludesList));
      }
    }
    
    private List<HierarchicalMatcher<T>> getIncludes() {
      return includes;
    }

    private List<HierarchicalMatcher<T>> getExcludes() {
      return excludes;
    }
    
    public static <T, BType extends Builder<T, BType>> BType builder(final T dir) {
      return new Builder<T, BType>(dir).self();
    }

    public <BType extends Builder<T, BType>> BType newBuilder() {
      return new Builder<T, BType>(dir).self();
    }

    public <BType extends Builder<T, BType>> BType toBuilder() {
      return new Builder<T, BType>(this).self();
    }
    
    @Override
    public String getPattern() {
      final StringBuilder sb = new StringBuilder("[filter:[");
      
      if (includes.isEmpty()) {
        sb.append("]");
      } else {
        for (HierarchicalMatcher<T> include : includes) {
          sb.append(include.getPattern()).append(",");
        }
        
        sb.setCharAt(sb.length() - 1, ']');
      }
      
      sb.append(", excludes:[");
      
      if (excludes.isEmpty()) {
        sb.append("]");
      } else {
        for (HierarchicalMatcher<T> exclude : excludes) {
          sb.append(exclude.getPattern()).append(",");
        }
        
        sb.setCharAt(sb.length() - 1, ']');
      }
      
      @SuppressWarnings("null")
      final @NonNull String result = sb.append("]").toString();
      return result;
    }
    
    @Override
    public String toString() {
      return "FileSetPathMatcher [filter=" + includes + ", excludes=" + excludes + ", dir=" + dir
          + ", allowDirs=" + allowDirs + ", allowFiles=" + allowFiles + "]";
    }
    
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (allowDirs ? 1231 : 1237);
      result = prime * result + (allowFiles ? 1231 : 1237);
      result = prime * result + ((dir == null) ? 0 : dir.hashCode());
      result = prime * result + ((excludes == null) ? 0 : excludes.hashCode());
      result = prime * result + ((includes == null) ? 0 : includes.hashCode());
      return result;
    }

    @Override
    @NonNullByDefault(false)
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof FileSetPathMatcher)) {
        return false;
      }

      try {
        final FileSetPathMatcher<?> other = (FileSetPathMatcher<?>) obj;
        
        if (allowDirs != other.allowDirs) {
          return false;
        }
        if (allowFiles != other.allowFiles) {
          return false;
        }
        if (!dir.equals(other.dir)) {
          return false;
        }
        if (!excludes.equals(other.excludes)) {
          return false;
        }
        if (!includes.equals(other.includes)) {
          return false;
        }
        return true;
      } catch (Exception e) {
        return false;
      }
    }
    
    @Override
    public boolean isEmpty() {
      return (this == EMPTY) || includes.isEmpty();
    }

    public T getDir() {
      return dir;
    }

    public boolean isAllowDirs() {
      return allowDirs;
    }

    public boolean isAllowFiles() {
      return allowFiles;
    }

    protected SimpleHistoryCache<FileSetCacheData> getCache() {
      return cache;
    }
    
    @Override
    public boolean matches(final T path, final PathContext<T> context) {
      if (!allowFiles) {
        return false;
      }

      return matchesResolved(context.resolvePath(dir, path), context);
    }
    
    @Override
    public boolean matchesResolved(final @Nullable String sanitizedPath, final PathContext<T> context) {
      if (!allowFiles) {
        return false;
      }

      cache.adjustCache(context.currentDepth());
      
      boolean matched = false;
      final FileSetCacheData cacheData = getCacheData();

      if (cacheData.getIncludes().isEmpty()) {
        return false;
      }

      for (int index : cacheData.getIncludes()) {
        final HierarchicalMatcher<T> include = includes.get(index);
        
        if (include.matchesResolved(sanitizedPath, context)) {
          matched = true;
          break;
        }
      }  

      if (!matched) {
        return false;
      }

      if (cacheData.getExcludes().isEmpty()) {
        return true;
      }
      
      for (int index : cacheData.getExcludes()) {
        final HierarchicalMatcher<T> exclude = includes.get(index);
        
        if (exclude.matchesResolved(sanitizedPath, context)) {
          return false;
        }
      }  

      return true;
    }

    @Override
    public DirectoryMatchResult matchesDirectory(final T path, final PathContext<T> context) {
      if (this == EMPTY) {
        return DirectoryMatchResult.NO_MATCH;
      }
      
      if (context.resolvePath(path, dir) != null) {
        //this a parent of dir
        return DirectoryMatchResult.NO_MATCH_CONTINUE;
      }
      
      return matchesResolvedDirectory(context.resolvePath(dir, path), context.getSeparator(path), 
          context);
    }

    @Override
    public DirectoryMatchResult matchesResolvedDirectory(final @Nullable String sanitizedPath, 
        final @Nullable String separator, final PathContext<T> context) {
      if ((this == EMPTY) || (sanitizedPath == null) || (separator == null)) {
        return DirectoryMatchResult.NO_MATCH;
      }
      
      cache.adjustCache(context.currentDepth());
        
      /*      if (sanitizedPath.isEmpty()) {
        return DirectoryMatchResult.NO_MATCH_CONTINUE;
      }
       */
      
      final FileSetCacheData cacheData = getCacheData();
      final FileSetCacheData.Builder cacheBuilder = cacheData.newBuilder();

      if (!cacheData.getExcludes().isEmpty()) {
        for (int index : cacheData.getExcludes()) {
          final HierarchicalMatcher<T> exclude = excludes.get(index);
          final DirectoryMatchResult matchResult = exclude.matchesResolvedDirectory(sanitizedPath, 
              separator, context);
          
          if (matchResult.isMatched()) {
            return DirectoryMatchResult.NO_MATCH;
          } else if (!matchResult.isSkip()) {
            cacheBuilder.setExclude(index);
          }
        }
      }
      
      boolean skip = true;
      boolean matched = false;
      cacheBuilder.includesFrom(cacheData);
      
      if (!cacheData.getIncludes().isEmpty()) {
        for (int index : cacheData.getIncludes()) {
          final HierarchicalMatcher<T> include = includes.get(index);
          final DirectoryMatchResult matchResult = include.matchesResolvedDirectory(sanitizedPath, 
              separator, context);
          
          if (matchResult.isMatched()) {
            matched = true;
            
            if (!matchResult.isSkip()) {
              // MATCH_CONTINUE
              skip = false;
              break;
            }
            
            cacheBuilder.clearInclude(index);
            
            if (!skip) {
              // MATCH_CONTINUE
              break;
            }
          } else if (!matchResult.isSkip()) {
            skip = false;
            
            if (matched) {
              // MATCH_CONTINUE
              break;
            } else if (!allowDirs) {
              // exact match is not required, so we lazily stop searching further 
              // NO_MATCH_CONTINUE
              break;
            }
          } else {
            // this include didn't match anyhow, so remove it, and continue searching for match
            cacheBuilder.clearInclude(index);
          }
        }
      }
      
      final DirectoryMatchResult result = DirectoryMatchResult.valueOf(matched, skip);
      
      // if (result != DirectoryMatchResult.NO_MATCH) {
      if (!skip) {
        cache.push(cacheBuilder.build());
      }
      
      return result;
    }

    private FileSetCacheData getCacheData() {
      if (cache.isEmpty()) {
        return FileSetCacheData.builder().setIncludes(includes).setExcludes(excludes).build();
      } else {
        return assertNonNull(cache.peek());
      }
    }
    
    public static class Builder<T, BType extends Builder<T, BType>> {
      private final LinkedHashSet<String> includes;
      private final LinkedHashSet<String> excludes;
      private T dir;
      private boolean allowDirs;
      private boolean allowFiles;

      public Builder(final T dir) {
        this.includes = new LinkedHashSet<String>();
        this.excludes = new LinkedHashSet<String>();
        this.dir = dir;
        this.allowDirs = false;
        this.allowFiles = true;
      }
      
      public Builder(final FileSetPathMatcher<T> original) {
        this(original.getDir());
        from(original);
      }
      
      public Builder(final BType original) {
        this(original.dir());
        from(original);
      }

      @SuppressWarnings("unchecked")
      protected BType self() {
        return (BType) this;
      }

      public BType from(final FileSetPathMatcher<T> original) {
        verifyNonNull(original);
        this.includes.clear();
        this.excludes.clear();
        addAllMatchers(this.includes, original.getIncludes());
        addAllMatchers(this.excludes, original.getExcludes());
        this.dir = original.getDir();
        this.allowDirs = original.isAllowDirs();
        this.allowFiles = original.isAllowFiles();
        return self();
      }
      
      public BType from(final BType original) {
        verifyNonNull(original);
        this.includes.clear();
        this.excludes.clear();
        this.includes.addAll(original.includes());
        this.excludes.addAll(original.excludes());
        this.dir = original.dir();
        this.allowDirs = original.isAllowDirs();
        this.allowFiles = original.isAllowFiles();
        return self();
      }
      
      private LinkedHashSet<String> addAllMatchers(final LinkedHashSet<String> target, 
          final Iterable<? extends HierarchicalMatcher<? extends T>> source) {
        for (HierarchicalMatcher<? extends T> include : verifyNonNull(source)) {
          target.add(verifyNonNull(include).getPattern());
        }
        
        return target;
      }
      
      //TODO should this be replaced by addAllIncludesAndExcludes, as the rest is murky!
      @SuppressWarnings("unused")
      @Deprecated
      private BType combine(final FileSetPathMatcher<T> other) {
        addAllMatchers(this.includes, other.getIncludes());
        addAllMatchers(this.excludes, other.getExcludes());
        verifyCondition(Objects.equals(this.dir, other.getDir()), "directories must be equal");
        //this.dir = other.dir;
        this.allowDirs = this.allowDirs || other.isAllowDirs();
        this.allowFiles = this.allowFiles || other.isAllowFiles();
        return self();
      }
      
      public BType addIncludesExcludesFrom(final FileSetPathMatcher<T> other) {
        addAllMatchers(this.includes, other.getIncludes());
        addAllMatchers(this.excludes, other.getExcludes());
        return self();
      }
      
      @SuppressWarnings("null")
      public Set<String> includes() {
        return Collections.unmodifiableSet(includes);
      }

      @SuppressWarnings("null")
      public Set<String> excludes() {
        return  Collections.unmodifiableSet(excludes);
      }

      public BType clearExcludes() {
        excludes.clear();
        return self();
      }
      
      public BType removeExclude(final String exclude) {
        excludes.remove(exclude);
        return self();
      }
      
      public BType removeExcludes(final Collection<String> excludes) {
        this.excludes.removeAll(excludes);
        return self();
      }

      public BType clearIncludes() {
        includes.clear();
        return self();
      }
      
      public BType removeInclude(final String include) {
        includes.remove(include);
        return self();
      }
      
      public BType removeIncludes(final Collection<String> includes) {
        this.includes.removeAll(includes);
        return self();
      }
      
      public BType addExcludes(final Iterable<String> excludes) {
        return addAll(this.excludes, excludes);
      }
      
      public BType addExclude(final String exclude) {
        this.excludes.add(verifyNonNull(exclude));
        return self();
      }

      public BType addIncludes(final Iterable<String> includes) {
        return addAll(this.includes, includes);
      }
      
      public BType addInclude(final String include) {
        this.includes.add(verifyNonNull(include));
        return self();
      }
      
      private BType addAll(final Collection<String> target, final Iterable<String> source) {
        for (String include : verifyNonNull(source)) {
          target.add(verifyNonNull(include));
        }
        
        return self();
      }
      
      public T dir() {
        return dir;
      }

      @SuppressWarnings({ "unchecked", "null" })
      protected T convertPath(String dir) {
        if (this.dir instanceof Path) {
          return (T) ((Path) this.dir).getFileSystem().getPath(dir);
        } else if (this.dir instanceof File) {
          return (T) new File(dir);
        } else if (this.dir instanceof String) {
          return (T) dir;
        } else {
          throw new UnsupportedOperationException();
        }
      }
      
      public BType dir(final String dir) {
        this.dir = convertPath(verifyNonNull(dir));
        return self();
      }

      public BType dir(final T dir) {
        this.dir = verifyNonNull(dir);
        return self();
      }
      
      public boolean isAllowDirs() {
        return allowDirs;
      }

      public BType allowDirs(boolean allowDirs) {
        this.allowDirs = allowDirs;
        return self();
      }

      public boolean isAllowFiles() {
        return allowFiles;
      }

      public BType allowFiles(boolean allowFiles) {
        this.allowFiles = allowFiles;
        return self();
      }
      
      public FileSetPathMatcher<T> build() {
        if (includes.isEmpty()) {
          @SuppressWarnings("unchecked")
          final FileSetPathMatcher<T> empty = (FileSetPathMatcher<T>) EMPTY;
          return empty;
        }
        
        final FileSetPathMatcher<T> result = new FileSetPathMatcher<T>(includes, excludes, 
            verifyNonNull(dir), allowDirs, allowFiles);
        return result;
      }
    }
  }
  
  protected static final class FileSetCacheData {
    private final IterableFilter includes;
    private final IterableFilter excludes;
    
    private FileSetCacheData(IterableFilter includes, IterableFilter excludes) {
      this.includes = new IterableFilter(includes);
      this.excludes = new IterableFilter(excludes);
    }
    
    private FileSetCacheData(BitSet includes, BitSet excludes) {
      this.includes = new IterableFilter(includes);
      this.excludes = new IterableFilter(excludes);
    }

    public IterableFilter getIncludes() {
      return includes;
    }

    public IterableFilter getExcludes() {
      return excludes;
    }
    
    public static Builder builder() {
      return new Builder();
    }

    public Builder newBuilder() {
      return new Builder();
    }

    public Builder toBuilder() {
      return new Builder().from(this);
    }

    @Override
    public String toString() {
      @SuppressWarnings("null")
      @NonNull String result = new StringBuilder()
          .append("FileSetCacheData [includes=").append(includes)
          .append(", excludes=").append(excludes)
          .append("]").toString();
      return result;
    }
    
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((excludes == null) ? 0 : excludes.hashCode());
      result = prime * result + ((includes == null) ? 0 : includes.hashCode());
      return result;
    }

    @NonNullByDefault(false)
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof FileSetCacheData)) {
        return false;
      }
      FileSetCacheData other = (FileSetCacheData) obj;
      if (!excludes.equals(other.excludes)) {
        return false;
      }
      if (!includes.equals(other.includes)) {
        return false;
      }
      return true;
    }

    public static class Builder {
      private IterableFilter.Builder includes;
      private IterableFilter.Builder excludes;
      
      private Builder() {
        this.includes = IterableFilter.builder();
        this.excludes = IterableFilter.builder();
      }
      
      public Builder from(final Builder other) {
        this.includes = IterableFilter.builder().from(other.includes);
        this.excludes = IterableFilter.builder().from(other.excludes);
        return this;
      }
      
      public Builder from(final FileSetCacheData other) {
        this.includes = IterableFilter.builder().from(other.includes);
        this.excludes = IterableFilter.builder().from(other.excludes);
        return this;
      }
      
      public Builder includesFrom(final FileSetCacheData other) {
        this.includes = IterableFilter.builder().from(other.includes);
        return this;
      }
      
      public Builder excludesFrom(final FileSetCacheData other) {
        this.excludes = IterableFilter.builder().from(other.excludes);
        return this;
      }

      public Builder setExclude(final int index) {
        excludes.set(index);
        return this;
      }
      
      public Builder setInclude(final int index) {
        includes.set(index);
        return this;
      }

      public Builder setAllIncludes(final Iterable<?> values) {
        includes.resetAll(values);
        return this;
      }
      
      public Builder setAllExcludes(final Iterable<?> values) {
        excludes.resetAll(values);
        return this;
      }
      
      public Builder setIncludes(final Iterable<?> values) {
        includes.reset(values);
        return this;
      }
      
      public Builder setExcludes(final Iterable<?> values) {
        excludes.reset(values);
        return this;
      }
      
      public Builder clearIncludes() {
        includes.clear();
        return this;
      }
      
      public Builder clearExcludes() {
        excludes.clear();
        return this;
      }
      
      public Builder clearExclude(final int index) {
        excludes.clear(index);
        return this;
      }
      
      public Builder clearInclude(final int index) {
        includes.clear(index);
        return this;
      }

      public FileSetCacheData build() {
        return new FileSetCacheData(includes.build(), excludes.build());
      }
    }
  }
  
  protected static final class IterableFilter implements Iterable<Integer> {
    private final BitSet filter;

    private IterableFilter() {
      this.filter = new BitSet();
    }

    private IterableFilter(final BitSet filter) {
      this.filter = compactClone(verifyNonNull(filter));
    }
    
    private IterableFilter(final IterableFilter other) {
      this.filter = compactClone(verifyNonNull(other.filter));
    }
    
    @SuppressWarnings("null")
    protected static final BitSet compactClone(final BitSet original) {
      return BitSet.valueOf(original.toLongArray());
    }
    
    public static Builder builder() {
      return new Builder();
    }

    public Builder newBuilder() {
      return new Builder();
    }

    public Builder toBuilder() {
      return new Builder().from(this);
    }

    public int size() {
      return filter.length();
    }
    
    public boolean isEmpty() {
      return filter.isEmpty();
    }
    
    @Override
    public Iterator<Integer> iterator() {
      return new FilteredIterator(filter);
    }

    @Override
    public String toString() {
      @SuppressWarnings("null")
      final @NonNull String result = new StringBuilder()
          .append("IterableFilter [filter=").append(filter)
          .append("]")
          .toString();
      return result;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((filter == null) ? 0 : filter.hashCode());
      return result;
    }

    @NonNullByDefault(false)
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof IterableFilter)) {
        return false;
      }
      IterableFilter other = (IterableFilter) obj;
      if (!filter.equals(other.filter)) {
        return false;
      }
      return true;
    }

    public static class Builder {
      private BitSet filter;
      
      private Builder() {
        this.filter = new BitSet();
      }

      public Builder from(final Builder other) {
        this.filter = compactClone(other.filter);
        return this;
      }
      
      public Builder from(final IterableFilter other) {
        this.filter = compactClone(other.filter);
        return this;
      }
      
      public Builder set(final int index) {
        filter.set(index);
        return this;
      }

      public Builder reset(final int size, boolean value) {
        filter.set(0, size, value);
        return this;
      }
      
      public Builder resetAll(final Iterable<?> values) {
        int size = 0;

        if (values instanceof Collection) {
          size = ((Collection<?>) values).size();
        } else {
          for (@SuppressWarnings("unused") Object value : values) {
            size++;
          }
        }
        
        return reset(size, true);
      }

      public Builder reset(final Iterable<?> values) {
        if (values instanceof Collection) {
          final Collection<?> col = (Collection<?>) values;
          filter = new BitSet(col.size());
          
          if (col.isEmpty()) {
            return this;
          }
        }
        
        int i = 0;
        
        for (Object value : values) {
          filter.set(i++, (value != null));
        }
        
        return this;
      }
      
      public Builder clear() {
        filter.clear();
        return this;
      }
      
      public Builder clear(final int index) {
        filter.clear(index);
        return this;
      }

      public boolean isEmpty() {
        return filter.isEmpty();
      }
      
      public IterableFilter build() {
        return new IterableFilter(filter);
      }
    }
    
    private static final class FilteredIterator implements Iterator<Integer> {
      private final BitSet filter;
      private int current;

      public FilteredIterator(final BitSet filter) {
        this.filter = verifyNonNull(filter);
        this.current = filter.nextSetBit(0);
      }

      @Override
      public boolean hasNext() {
        return (current >= 0);
      }

      @Override
      public Integer next() {
        if (current < 0) {
          throw new NoSuchElementException();
        }
        
        @SuppressWarnings("null")
        final @NonNull Integer result = Integer.valueOf(current);
        current = filter.nextSetBit(current + 1);
        return result;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    }
  }
}
