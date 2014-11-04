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

import static com.github.protobufel.common.verifications.Verifications.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.github.protobufel.common.files.PathContexts.PathContext;

public final class ContextPathMatchers {
  private ContextPathMatchers() {}

  interface HierarchicalMatcherCommon {
    boolean isEmpty();
    boolean isAllowDirs();
    boolean isAllowFiles();
    String getPattern();
  }
  
  public interface ContextHierarchicalMatcher<T> extends HierarchicalMatcherCommon {
    boolean matches(T path, PathContext<T> pathContext);
    DirectoryMatchResult matchesDirectory(T path, PathContext<T> pathContext);
  }

  public interface BasicHierarchicalMatcher<T> extends HierarchicalMatcherCommon {
    boolean matchesResolved(@Nullable String path, PathContext<T> pathContext);
    DirectoryMatchResult matchesResolvedDirectory(@Nullable String path, @Nullable String separator, 
        PathContext<T> pathContext);
  }

  public interface HierarchicalMatcher<T> extends ContextHierarchicalMatcher<T>, BasicHierarchicalMatcher<T> {
  }
  
  public static <T> HierarchicalMatcher<T> getHierarchicalMatcher(final String syntaxAndPattern, 
      final boolean isUnix, final boolean allowDirs, final boolean allowFiles, 
      final Class<? extends T> pathType) {
    final String[] parts = verifyNonNull(syntaxAndPattern).split(":", 2);
    final @NonNull String regex = verifyArgument(parts.length == 2, parts[1]);
    
    if (parts[0].equals("regex")) {
      return new SimpleHierarchicalMatcher<T>(isUnix, regex, allowDirs, allowFiles);
    } else if (parts[0].equals("glob")) {
      return new SimpleHierarchicalMatcher<T>(regex , isUnix, allowDirs, allowFiles);
    } else {
      throw new UnsupportedOperationException(String.format("%s syntax", parts[0]));
    }
  }
  
  public static final class SimpleHierarchicalMatcher<T> implements HierarchicalMatcher<T> {
    private final Pattern pattern;
    private final boolean allowDirs;
    private final boolean allowFiles;
    private final boolean isUnix;

    public SimpleHierarchicalMatcher(final SimpleHierarchicalMatcher<T> other) {
      this.pattern = other.pattern;
      this.allowDirs = other.allowDirs;
      this.allowFiles = other.allowFiles;
      this.isUnix = other.isUnix;
    }

    public SimpleHierarchicalMatcher(final String glob, final boolean isUnix, final boolean allowDirs, 
        final boolean allowFiles) {
      this(isUnix, convertGlobToRegex(glob, isUnix), true, allowDirs, allowFiles);
    }

    public SimpleHierarchicalMatcher(final boolean isUnix, final String regex, 
        final boolean allowDirs, final boolean allowFiles) {
      this(isUnix, regex, false, allowDirs, allowFiles);
    }

    public SimpleHierarchicalMatcher(final boolean isUnix, final String regex, 
        final boolean isRegexSystemSpecific, final boolean allowDirs, final boolean allowFiles) {
      verifyCondition((allowDirs || allowFiles), "allowDirs and allowFiles cannot be both false");
      this.allowDirs = allowDirs;
      this.allowFiles = allowFiles;
      final String regexFlags = isUnix ? "" : "(?iu)";
      final String fsRegex = isRegexSystemSpecific ? regex 
          : convertRegexToSystemSpecific(regex, isUnix);
      @SuppressWarnings("null")
      @NonNull Pattern compiled = Pattern.compile(regexFlags + verifyNonNull(fsRegex));
      this.pattern = compiled;
      this.isUnix = isUnix;
    }

    @SuppressWarnings("null")
    @Override
    public String getPattern() {
      return pattern.pattern();
    }
    
    @Override
    public String toString() {
      return "SimpleHierarchicalMatcher [pattern=" + pattern + ", flags=" + pattern.flags() 
          + ", allowDirs=" + allowDirs + ", allowFiles=" + allowFiles + ", isUnix=" + isUnix + "]";
    }
    
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (allowDirs ? 1231 : 1237);
      result = prime * result + (allowFiles ? 1231 : 1237);
      result = prime * result + (isUnix ? 1231 : 1237);
      result = prime * result + pattern.pattern().hashCode();
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
      if (!(obj instanceof SimpleHierarchicalMatcher)) {
        return false;
      }
      final SimpleHierarchicalMatcher<?> other = (SimpleHierarchicalMatcher<?>) obj;
      if (allowDirs != other.allowDirs) {
        return false;
      }
      if (allowFiles != other.allowFiles) {
        return false;
      }
      if (isUnix != other.isUnix) {
        return false;
      }
      if (!pattern.pattern().equals(other.pattern.pattern())) {
        return false;
      }
      return true;
    }

    @Override
    public boolean isEmpty() {
      return "".equals(pattern.pattern());
    }

    public boolean isAllowDirs() {
      return allowDirs;
    }

    public boolean isAllowFiles() {
      return allowFiles;
    }

    @Override
    public boolean matches(final T path, final PathContext<T> pathContext) {
      return matchesResolved(pathContext.resolvePath(path), pathContext);
    }

    @Override
    public DirectoryMatchResult matchesDirectory(final T path, final PathContext<T> pathContext) {
      return matchesResolvedDirectory(pathContext.resolvePath(path), 
          pathContext.getSeparator(path), pathContext);
    }

    @Override
    public boolean matchesResolved(final @Nullable String path, final PathContext<T> pathContext) {
      return (allowFiles && (path != null)) ? pattern.matcher(path).matches() : false;
    }

    @Override
    public DirectoryMatchResult matchesResolvedDirectory(final @Nullable String path, 
        final @Nullable String separator, final PathContext<T> pathContext) {
      if ((path == null) || (separator == null)) {
        return DirectoryMatchResult.NO_MATCH;
      }
      
      final String pathWithSlash = path + separator;
      final Matcher matcher;
      final boolean isMatched;
      
      if (allowDirs) {
        matcher = pattern.matcher(path);
        isMatched = matcher.matches();
        matcher.reset(pathWithSlash);
      } else {
        matcher = pattern.matcher(pathWithSlash);
        isMatched = false;
      }
      
      final boolean skip = matcher.matches() ? matcher.requireEnd() : (!matcher.hitEnd());
      return DirectoryMatchResult.valueOf(isMatched, skip);
    }
    
    protected final static String convertGlobToRegex(final String glob, final boolean isUnix) {
      if (isUnix) {
        return Globs.toUnixRegexPattern(glob);
      } else {
        return Globs.toWindowsRegexPattern(glob);
      }
    }
    
    protected final String convertRegexToSystemSpecific(final String normalizedRelativeRegex, 
        final boolean isUnix) {
      if (isUnix) {
        return normalizedRelativeRegex;
      } else {
        return convertNormalizedRelativeRegexToWindows(normalizedRelativeRegex);
      }
    }
    
    @SuppressWarnings("null")
    private static final Pattern UNIX_TO_WINDOWS_SLASH_FIND_REGEX = Pattern
        .compile("/|(?:(\\\\*+)(?:(\\[)|(\\])|(Q)|(E)))");
    
    protected final String convertNormalizedRelativeRegexToWindows(
        final String normalizedRelativeRegex) {
      @SuppressWarnings("null")
      final @NonNull Matcher matcher = UNIX_TO_WINDOWS_SLASH_FIND_REGEX.matcher(
          normalizedRelativeRegex);
      final StringBuffer sb = new StringBuffer();
      boolean foundCharGroup = false;
      boolean foundQuote = false;

      while (matcher.find()) {
        if (foundQuote) {
          if (matcher.group(5) != null) {
            if ((matcher.group(1).length() % 2) == 1) {
              foundQuote = false;
            }
          }
        } else if (matcher.group(4) != null) {
          if ((matcher.group(1).length() % 2) == 1) {
            foundQuote = true;
          }
        } else if (matcher.group(2) != null) {
          if ((matcher.group(1).length() % 2) == 0) {
            foundCharGroup = true;
          }
        } else if (matcher.group(3) != null) {
          if ((matcher.group(1).length() % 2) == 0) {
            foundCharGroup = false;
          }
        } else if (!foundCharGroup) {
          matcher.appendReplacement(sb, "\\\\\\\\");
        }
      }

      @SuppressWarnings("null")
      final @NonNull String result = matcher.appendTail(sb).toString();
      return result;
    }
  }
}
