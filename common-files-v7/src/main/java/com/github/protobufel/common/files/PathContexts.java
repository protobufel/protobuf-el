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

import static com.github.protobufel.common.verifications.Verifications.verifyNonNull;

import java.io.File;
import java.nio.file.Path;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class PathContexts {
  private static final PathContext<Object> EMPTY_PATH_CONTEXT = new PathContext<Object>() {
    @Override
    public int currentDepth() {
      return -1;
    }

    @Override
    public boolean setCachedValue(Object key, Object value) {
      return false;
    }

    @Override
    public @Nullable Object getCachedValue(Object key) {
      return null;
    }

    @Override
    public @Nullable String resolvePath(Object basePath, Object path) {
      return null;
    }

    @Override
    public @Nullable String resolvePath(Object path) {
      return null;
    }

    @Override
    public @Nullable String getSeparator(Object path) {
      return null;
    }
  };
  
  private PathContexts() {}
  
  public interface PathContext<T> extends IHistoryCacheView<Object, Object> {
    @Nullable String resolvePath(T basePath, T path);
    @Nullable String resolvePath(T path);
    @Nullable String getSeparator(T path);
    //void reportError(String message, String path);
  }

  @SuppressWarnings("unchecked")
  public static <T> PathContext<T> emptyPathContext() {
    return (PathContext<T>) EMPTY_PATH_CONTEXT;
  }
  
  public static class BasePathContext<T> implements PathContext<T> {
    private final IHistoryCacheView<Object, Object> cache;
    private final @Nullable T root;
    
    public BasePathContext(IHistoryCacheView<Object, Object> cache, @Nullable T root) {
      this.cache = verifyNonNull(cache);
      this.root = root;
    }

    protected @Nullable T getRoot() {
      return root;
    }

    @Override
    public @Nullable String resolvePath(T path) {
      final T root = this.root;
      
      if (root != null) {
        return resolvePath(root, path);
      } else {
        return path.toString();
      }
    }

    @Override
    public @Nullable String resolvePath(T basePath, T path) {
      final @NonNull String strPath = verifyNonNull(path.toString());
      final @NonNull String strBasePath = verifyNonNull(basePath.toString()) + File.separator;
      return strPath.startsWith(strBasePath) ? strPath.substring(strBasePath.length()) : null;
    }

    @Override
    public @Nullable String getSeparator(T path) {
      return File.separator;
    }

    @Override
    public @Nullable Object getCachedValue(Object key) {
      return cache.getCachedValue(key);
    }

    @Override
    public boolean setCachedValue(Object key, Object value) {
      return cache.setCachedValue(key, value);
    }

    @Override
    public int currentDepth() {
      return cache.currentDepth();
    }
  }

  public static final class SimplePathContext extends BasePathContext<Path> {

    public SimplePathContext(IHistoryCacheView<Object, Object> cache, @Nullable Path root) {
      super(cache, root);
    }

    @Override
    public @Nullable String resolvePath(Path basePath, Path path) {
      return path.startsWith(basePath) ? basePath.relativize(path).toString() : null;
    }

    @Override
    public @Nullable String resolvePath(Path path) {
      final Path root = getRoot();
      
      if (root != null) {
        return resolvePath(root, path);
      } else {
        return path.toString();
      }
    }

    @Override
    public @Nullable String getSeparator(Path path) {
      return path.getFileSystem().getSeparator();
    }
  }
}
