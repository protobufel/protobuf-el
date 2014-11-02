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

import static com.github.protobufel.common.verifications.Verifications.verifyArgument;
import static com.github.protobufel.common.verifications.Verifications.verifyNonNull;
import static com.github.protobufel.common.verifications.Verifications.verifyNonNullIterable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A container of the extended parentPattern support.
 *
 * @author protobufel@gmail.com David Tesler
 */
public final class Patterns {
  private Patterns() {
  }

  public static final class ExtendedPattern implements IPatternSupport {
    private final List<Pattern> includes;
    private final List<Pattern> excludes;

    @SuppressWarnings("null")
    public ExtendedPattern(final Collection<Pattern> includes, final Collection<Pattern> excludes, 
        final int flags) {
      @NonNull Collection<Pattern> nonNullIncludes = verifyNonNullIterable(includes);
      this.includes = Collections.unmodifiableList(new ArrayList<Pattern>(
          verifyArgument(!nonNullIncludes.isEmpty(), nonNullIncludes, "includes must be non-empty")));
      this.excludes = Collections.unmodifiableList(new ArrayList<Pattern>(verifyNonNullIterable(excludes)));
    }

    private ExtendedPattern(final Iterable<String> includes, final Iterable<String> excludes, 
        final int flags) {
      this.includes = getCompiledPatterns(includes, flags);
      this.excludes = getCompiledPatterns(excludes, flags);
    }
    
    private List<Pattern> getCompiledPatterns(final Iterable<String> regexes, final int flags) {
      final @NonNull ArrayList<Pattern> patterns = new ArrayList<Pattern>();
      
      for (String regex : regexes) {
        patterns.add(Pattern.compile(verifyNonNull(regex), flags));
      }
      
      @SuppressWarnings("null")
      @NonNull List<Pattern> unmodifiableList = Collections.unmodifiableList(patterns);
      return unmodifiableList;
    }

    public static ExtendedPattern compile(final Iterable<String> includes, 
        final Iterable<String> excludes) {
      return new ExtendedPattern(includes, excludes, 0);
    }

    public static ExtendedPattern compile(final Iterable<String> includes, 
        final Iterable<String> excludes, final int flags) {
      return new ExtendedPattern(includes, excludes, flags);
    }

    public static boolean matches(final String regex, final CharSequence input) {
      return Pattern.matches(regex, input);
    }

    public static boolean matches(final Iterable<String> includes, 
        final Iterable<String> excludes, final CharSequence input) {
      return ExtendedPattern.compile(includes, excludes).matcher(input).matches();
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((excludes == null) ? 0 : excludes.hashCode());
      result = prime * result + ((includes == null) ? 0 : includes.hashCode());
      return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof ExtendedPattern)) {
        return false;
      }
      ExtendedPattern other = (ExtendedPattern) obj;
      if (!excludes.equals(other.excludes)) {
        return false;
      }
      if (!includes.equals(other.includes)) {
        return false;
      }
      return true;
    }

    @Override
    public String toString() {
      return "ExtendedPattern [includes=" + includes + ", excludes=" + excludes + "]";
    }

    public String pattern() {
      return "[includes=" + includes + ", excludes=" + excludes + "]";
    }

    public int excludeFlags(final int index) {
      return excludes.get(index).flags();
    }

    public int includeFlags(final int index) {
      return includes.get(index).flags();
    }

    public int includeCount() {
      return includes.size();
    }

    public int excludeCount() {
      return excludes.size();
    }

    @Override
    public ExtendedMatcher matcher(CharSequence input) {
      return new ExtendedMatcher(this, input);
    }

    public String[] split(CharSequence input, int limit) {
      return RegexSupport.split(this, input, limit);
    }

    public String[] split(CharSequence input) {
      return RegexSupport.split(this, input, 0);
    }
  }

  public static final class ExtendedMatcher implements MatchResult, IMatcherSupport {
    private static final MatchResult EMPTY_MATCH_RESULT = new MatchResult() {
      @Override
      public int start(int group) {
        throw new IllegalStateException("No match available");
      }

      @Override
      public int start() {
        throw new IllegalStateException("No match available");
      }

      @Override
      public int groupCount() {
        return -1;
      }

      @Override
      public String group(int group) {
        throw new IllegalStateException("No match found");
      }

      @Override
      public String group() {
        throw new IllegalStateException("No match found");
      }

      @Override
      public int end(int group) {
        throw new IllegalStateException("No match available");
      }

      @Override
      public int end() {
        throw new IllegalStateException("No match available");
      }
    };
    
    private final Matcher delegate;
    private ExtendedPattern parentPattern;
    private CharSequence input;
    private int lastAppendPosition = 0;
    private int includeIndex;
    private MatcherOp lastOp;
    private int lastStart;
    
    private ExtendedMatcher(final ExtendedPattern pattern, final CharSequence input) {
      this.parentPattern = verifyNonNull(pattern);
      this.input = verifyNonNull(input);
      @SuppressWarnings("null")
      @NonNull Matcher matcher = pattern.includes.get(0).matcher(input);
      this.delegate = matcher;
      this.includeIndex = -1;
      this.lastOp = MatcherOp.NOOP;
      lastStart = -1;
    }

    public int getLastAppendPosition() {
      return lastAppendPosition;
    }

    protected List<Pattern> getIncludes() {
      return parentPattern.includes;
    }

    protected List<Pattern> getExcludes() {
      return parentPattern.excludes;
    }
    
    @Override
    public ExtendedMatcher setLastAppendPosition(int lastAppendPosition) {
      this.lastAppendPosition = lastAppendPosition;
      return this;
    }

    @Override
    public Matcher getDelegate() {
      return delegate;
    }

    @Override
    public CharSequence getInput() {
      return input;
    }

    @Override
    public ExtendedPattern pattern() {
      return parentPattern;
    }

    public ExtendedMatcher region(int start, int end) {
      delegate.region(start, end);
      return this;
    }

    public int regionStart() {
      return delegate.regionStart();
    }

    public int regionEnd() {
      return delegate.regionEnd();
    }

    public ExtendedMatcher usePattern(final ExtendedPattern pattern) {
      @NonNull ExtendedPattern nonNullPattern = verifyArgument(pattern != null, pattern, 
          "Pattern cannot be null");
      this.parentPattern = nonNullPattern;
      delegate.usePattern(nonNullPattern.includes.get(0));
      return this;
    }

    @Override
    public ExtendedMatcher reset() {
      delegate.reset();
      includeIndex = -1;
      return this;
    }

    public ExtendedMatcher reset(CharSequence input) {
      this.input = input;
      includeIndex = -1;
      return reset();
    }

    @SuppressWarnings("null")
    public MatchResult toMatchResult() {
      return (includeIndex == -1) ? EMPTY_MATCH_RESULT : delegate.toMatchResult();
    }

    @Override
    public int start() {
      return delegate.start();
    }

    @Override
    public int start(int group) {
      return delegate.start(group);
    }

    @Override
    public int end() {
      return delegate.end();
    }

    @Override
    public int end(int group) {
      return delegate.end(group);
    }

    @Override
    @SuppressWarnings("null")
    public String group() {
      return delegate.group();
    }

    @Override
    public @Nullable String group(int group) {
      return delegate.group(group);
    }

    public @Nullable String group(String name) {
      return delegate.group(name);
    }

    @Override
    public int groupCount() {
      return delegate.groupCount();
    }

    public boolean matches() {
      lastOp = MatcherOp.MATCHES;
      lastStart = -1;
      int i = -1;
      
      for (Pattern pattern : getExcludes()) {
        i++;
        
        if (delegate.usePattern(pattern).matches()) {
          includeIndex = -1;
          return false;
        }
      }
      
      i= -1;
      
      for (Pattern pattern : getIncludes()) {
        i++;
        
        if (delegate.usePattern(pattern).matches()) {
          includeIndex = i;
          return true;
        }
      }
      
      includeIndex = -1;
      return false;
    }
    
    @Override
    public boolean find() {
      lastOp = MatcherOp.FIND;
      int start;
      
      if (includeIndex == -1) {
        start = 0;
      } else {
        start = (delegate.end() == delegate.start()) ? (delegate.end() + 1) : delegate.end();
      }

      final int regionStart = delegate.regionStart();
      final int regionEnd = delegate.regionEnd();
      
      if (start < regionStart) {
        start = regionStart;
      }
      
      if (start > regionEnd) {
        includeIndex = -1;
        lastStart = -1;
        return false;
      }

      lastStart = start;
      final Set<Interval> rejected = new HashSet<Interval>();
      int i = -1;
      
      for (Pattern pattern : getIncludes()) {
        i++;
        
        if (delegate.usePattern(pattern).find(start)) {
          final Interval matchedInterval = new Interval(delegate.start(), delegate.end());
          
          if (rejected.contains(matchedInterval)) {
            continue;
          }
          
          if (!matchesExcludes(delegate.start(), delegate.end())) {
            includeIndex = i;
            return true;
          }
          
          rejected.add(matchedInterval);
        }
      }
      
      includeIndex = -1;
      return false;
    }
    
    private boolean matchesExcludes(final int start, final int end) {
      if (getExcludes().isEmpty()) {
        return false;
      }
      
      Matcher matcher = null;
      
      for (Pattern pattern : getExcludes()) {
        @SuppressWarnings("null")
        @NonNull Pattern nonNullPattern = pattern;
        
        if (matcher == null) {
          matcher = getNewMatcher(nonNullPattern, start, end);
        } else {
          matcher.usePattern(nonNullPattern);
        }
        
        if (matcher.matches()) {
          return true;
        }
      }
      
      return false;
    }

    @SuppressWarnings("null")
    private Matcher getNewMatcher(final Pattern pattern, final int regionStart, final int regionEnd) {
      return pattern.matcher(input)
          .region(regionStart, regionEnd)
          .useTransparentBounds(delegate.hasTransparentBounds())
          .useAnchoringBounds(delegate.hasAnchoringBounds());
    }
    
    public boolean find(final int start) {
      lastOp = MatcherOp.FIND_AT;
      
      if ((start < 0) || (start > input.length())) {
        lastStart = -1;
        includeIndex = -1;
        throw new IndexOutOfBoundsException("Illegal start index");
      }
      
      lastStart = start;
      final Set<Interval> rejected = new HashSet<Interval>();
      int i = -1;
      
      for (Pattern pattern : getIncludes()) {
        i++;
        
        if (delegate.usePattern(pattern).find(start)) {
          final Interval matchedInterval = new Interval(delegate.start(), delegate.end());
          
          if (rejected.contains(matchedInterval)) {
            continue;
          }
          
          if (!matchesExcludes(delegate.start(), delegate.end())) {
            includeIndex = i;
            return true;
          }
          
          rejected.add(matchedInterval);
        }
      }
      
      includeIndex = -1;
      return false;
    }

    public boolean lookingAt() {
      lastOp = MatcherOp.LOOKING_AT;
      lastStart = -1;
      final Set<Integer> rejected = new HashSet<Integer>();
      int i = -1;
      
      for (Pattern pattern : getIncludes()) {
        i++;
        
        if (delegate.usePattern(pattern).lookingAt()) {
          if (rejected.contains(delegate.end())) {
            continue;
          }
          
          if (!matchesExcludes(delegate.start(), delegate.end())) {
            includeIndex = i;
            return true;
          }
          
          rejected.add(delegate.end());
        }
      }
      
      includeIndex = -1;
      return false;
    }

    public StringBuffer appendTail(StringBuffer sb) {
      return RegexSupport.appendTail(input, lastAppendPosition, sb);
    }

    public ExtendedMatcher appendReplacement(StringBuffer sb, String replacement) {
      return RegexSupport.appendReplacement(this, sb, replacement);
    }
    
    public String replaceAll(String replacement) {
      return RegexSupport.replaceAll(this, replacement);
    }

    public String replaceFirst(String replacement) {
      return RegexSupport.replaceFirst(this, replacement);
    }

    public boolean hasTransparentBounds() {
      return delegate.hasTransparentBounds();
    }

    public ExtendedMatcher useTransparentBounds(boolean b) {
      delegate.useTransparentBounds(b);
      return this;
    }

    public boolean hasAnchoringBounds() {
      return delegate.hasAnchoringBounds();
    }

    public ExtendedMatcher useAnchoringBounds(boolean b) {
      delegate.useAnchoringBounds(b);
      return this;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("ExtendedMatcher [parentPattern=").append(pattern())
          .append(", region=").append(regionStart()).append(", ").append(regionEnd())
          .append(", lastmatch=").append(includeIndex == -1 ? null : group())
          .append("]");
      @SuppressWarnings("null")
      @NonNull String string = builder.toString();
      return string;
    }
    
    public boolean hitEnd() {
      return delegate.hitEnd();
    }

    public boolean requireEnd() {
      if (includeIndex == -1) {
        // requireEnd has no meaning, return false
        return false; 
      }
      
      return delegate.requireEnd();
    }
    
    public boolean tryHitEnd() {
      if (delegate.hitEnd()) {
        return true;
      }
      
      Matcher matcher = null;
      int skipIndex = (includeIndex == -1) ? (parentPattern.includeCount() - 1) : includeIndex;
      int i = -1;
      
      for (Pattern pattern : getIncludes()) {
        i++;
        
        if (i == skipIndex) {
          continue;
        }
        
        @SuppressWarnings("null")
        @NonNull Pattern nonNullPattern = pattern;
        
        if (matcher == null) {
          matcher = getNewMatcher(nonNullPattern, delegate.regionStart(), delegate.regionEnd());
        } else {
          matcher.usePattern(nonNullPattern).reset();
        }
        
        if (lastOp.apply(matcher, lastStart)) {
          if (matcher.requireEnd()) {
            return true;
          }
        }
      }
      
      return false;
    }

    public boolean tryRequireEnd() {
      if (includeIndex == -1) {
        // requireEnd has no meaning, return false
        return false; 
      }
      
      if (!delegate.requireEnd()) {
        return false;
      }
      
      Matcher matcher = null;
      
      for (Pattern pattern : getIncludes().subList(includeIndex + 1, getIncludes().size())) {
        @SuppressWarnings("null")
        @NonNull Pattern nonNullPattern = pattern;
        
        if (matcher == null) {
          matcher = getNewMatcher(nonNullPattern, delegate.regionStart(), delegate.regionEnd());
        } else {
          matcher.usePattern(nonNullPattern).reset();
        }
        
        if (lastOp.apply(matcher, lastStart)) {
          if (!matchesExcludes(matcher.start(), matcher.end()) && !matcher.requireEnd()) {
            return false;
          }
        }
      }
      
      return true;
    }
    
    private enum MatcherOp {
      NOOP {
        @Override
        public boolean apply(Matcher matcher, int start) {
          throw new UnsupportedOperationException();
        }
      },
      FIND_AT() {
        @Override
        public boolean apply(final Matcher matcher, final int start) {
          return matcher.find(start);
        }
      },
      LOOKING_AT {
        @Override
        public boolean apply(final Matcher matcher, final int start) {
          return matcher.lookingAt();
        }
      },
      FIND { // internally, same as FIND_AT
        @Override
        public boolean apply(final Matcher matcher, final int start) {
          return matcher.find(start);
        }
      },
      MATCHES {
        @Override
        public boolean apply(final Matcher matcher, final int start) {
          return matcher.matches();
        }
      };

      public abstract boolean apply(final Matcher matcher, final int start);
    }
  }

  private static final class Interval implements Comparable<Interval>{
    final int start;
    final int end;
    
    public Interval(int start, int end) {
      this.start = start;
      this.end = end;
    }

    @SuppressWarnings("unused")
    public int getStart() {
      return start;
    }

    @SuppressWarnings("unused")
    public int getEnd() {
      return end;
    }
    
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + end;
      result = prime * result + start;
      return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof Interval)) {
        return false;
      }
      Interval other = (Interval) obj;
      if (end != other.end) {
        return false;
      }
      if (start != other.start) {
        return false;
      }
      return true;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("Interval [start=").append(start).append(", end=").append(end).append("]");
      @SuppressWarnings("null")
      @NonNull String str = builder.toString();
      return str;
    }

    @Override
    public int compareTo(final @Nullable Interval other) {
      if (other == null) {
        return 1;
      } else if (start < other.start) {
        return -1;
      } else if (start > other.start) {
        return 1;
      } else if (end < other.end) {
        return -1;
      } else if (end > other.end) {
        return 1;
      } else {
        return 0;
      }
    }
  }
}
