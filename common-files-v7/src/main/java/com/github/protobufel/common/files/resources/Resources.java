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

package com.github.protobufel.common.files.resources;

import static com.github.protobufel.common.verifications.Verifications.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class Resources {
  private Resources() {}
  
  private interface ISharedFileSet {

    public String getDirectory();

    public List<String> getIncludes();

    public List<String> getExcludes();

    public boolean isAllowDirs();

    public boolean isAllowFiles();
  }
  
  public interface IFileSet extends ISharedFileSet {
  }
  
  public static final class FileSet implements IFileSet {
    //TODO make it Serializable
    private final String directory;
    private final List<String> includes;
    private final List<String> excludes;
    private final boolean allowDirs;
    private final boolean allowFiles;

    public FileSet(final IFileSet other) {
      this(other.getDirectory(), other.getIncludes(), other.getExcludes(), false, true);
    }
    
    public FileSet(final String directory, final List<String> includes, 
        final List<String> excludes, final boolean allowDirs, final boolean allowFiles) {
      this.directory = directory;
      this.includes = assertNonNull(includes.isEmpty() ? Collections.<String>emptyList() : 
        Collections.unmodifiableList(new ArrayList<String>(includes)));
      this.excludes = assertNonNull(excludes.isEmpty() ? Collections.<String>emptyList() : 
        Collections.unmodifiableList(new ArrayList<String>(excludes)));
      this.allowDirs = allowDirs;
      this.allowFiles = allowFiles;
    }

    @Override
    public String getDirectory() {
      return directory;
    }

    @Override
    public List<String> getIncludes() {
      return includes;
    }

    @Override
    public List<String> getExcludes() {
      return excludes;
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
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (allowDirs ? 1231 : 1237);
      result = prime * result + (allowFiles ? 1231 : 1237);
      result = prime * result + ((directory == null) ? 0 : directory.hashCode());
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
      if (!(obj instanceof FileSet)) {
        return false;
      }
      FileSet other = (FileSet) obj;
      if (allowDirs != other.allowDirs) {
        return false;
      }
      if (allowFiles != other.allowFiles) {
        return false;
      }
      if (!directory.equals(other.directory)) {
        return false;
      }
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
      return "FileSet [directory=" + directory + ", includes=" + includes + ", excludes="
          + excludes + ", allowDirs=" + allowDirs + ", allowFiles=" + allowFiles + "]";
    }

    public static Builder builder() {
      return new Builder();
    }

    public static Builder builder(final IFileSet other) {
      return new Builder(other);
    }
    
    public static final class Builder implements ISharedFileSet {
      private String directory;
      private List<String> includes;
      private List<String> excludes;
      private boolean allowDirs;
      private boolean allowFiles;

      public Builder() {
        directory = "";
        includes = new ArrayList<String>(); 
        excludes = new ArrayList<String>();
        allowDirs = false;
        allowFiles = true;
      }

      public Builder(final ISharedFileSet other) {
        this(other.getDirectory(), other.getIncludes(), other.getExcludes(), other.isAllowDirs(), 
            other.isAllowFiles());
      }

      private Builder(final String directory, final List<String> includes, 
          final List<String> excludes, final boolean allowDirs, final boolean allowFiles) {
        this.directory = directory;
        this.includes = new ArrayList<String>(includes);
        this.excludes = new ArrayList<String>(excludes);
        this.allowDirs = allowDirs;
        this.allowFiles = allowFiles;
      }

      public String getDirectory() {
        return directory;
      }
      
      public boolean isAllowDirs() {
        return allowDirs;
      }

      public Builder allowDirs(boolean allowDirs) {
        this.allowDirs = allowDirs;
        return this;
      }

      public boolean isAllowFiles() {
        return allowFiles;
      }

      public Builder allowFiles(boolean allowFiles) {
        this.allowFiles = allowFiles;
        return this;
      }

      public Builder directory(final String directory) {
        this.directory = directory;
        return this;
      }
      
      @SuppressWarnings("null")
      public List<String> getIncludes() {
        return Collections.unmodifiableList(includes);
      }
      
      @SuppressWarnings("null")
      public List<String> getExcludes() {
        return Collections.unmodifiableList(excludes);
      }
      
      public Builder addIncludes(String... includes) {
        Collections.addAll(this.includes, includes);
        return this;
      }
      
      public Builder addExcludes(String... excludes) {
        Collections.addAll(this.excludes, excludes);
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
      
      public Builder clearDir() {
        directory = "";
        return this;
      }
      
      public Builder reset() {
        clearDir();
        clearIncludes();
        clearExcludes();
        return this;
      }
      
      public IFileSet build() {
        return new FileSet(directory, includes, excludes, allowDirs, allowFiles);
      }
    }
  }
}
