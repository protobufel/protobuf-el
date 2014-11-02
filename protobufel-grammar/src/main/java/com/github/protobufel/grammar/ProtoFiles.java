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

package com.github.protobufel.grammar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ANTLRErrorStrategy;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import com.github.protobufel.grammar.ErrorListeners.ConsoleProtoErrorListener;
import com.github.protobufel.grammar.ErrorListeners.IBaseProtoErrorListener;
import com.github.protobufel.grammar.Exceptions.DescriptorValidationRuntimeException;
import com.github.protobufel.grammar.ParserUtils.CommonTokenStreamEx;
import com.github.protobufel.grammar.ProtoFileParser.ParsedContext;
import com.github.protobufel.grammar.ProtoParser.ProtoContext;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProtoOrBuilder;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.ExtensionRegistry;

/**
 * The main entry point for .proto parsing, and FileDescriptor and FileDescriptorProto generation.
 *
 * @author protobufel@gmail.com David Tesler
 */
public final class ProtoFiles {
  private ProtoFiles() {}

  /**
   * Returns a new Builder with its defaults set to {@link DefaultErrorStrategy} and console error
   * printing.
   */
  public static Builder newBuilder() {
    return new Builder();
  }

  /**
   * Returns a new Builder with its defaults set to {@link DefaultErrorStrategy} and the supplied
   * errorListener.
   */
  public static Builder newBuilder(final IBaseProtoErrorListener errorListener) {
    return new Builder(new DefaultErrorStrategy(), errorListener);
  }

  /**
   * Returns a new Builder with its defaults set to {@link BailErrorStrategy} and the supplied
   * errorListener.
   */
  public static Builder newFailFastBuilder(final IBaseProtoErrorListener errorListener) {
    return new Builder(new BailErrorStrategy(), errorListener);
  }

  /**
   * Builds FileDecsriptors/FileDecsriptorProtos based on the specified .proto files, .proto texts,
   * and FileDecsriptorProtos. The errors are reported based on the supplied
   * {@link ANTLRErrorStrategy} and {@link IBaseProtoErrorListener}. The built FileDescriptors and
   * FileDescriptorProtos are canonical, with all types, options, and the rest resolved.
   * <p>
   * If customOptionsAsExtensions set to {@code true}, the resulted FileDecsriptors and
   * FileDecsriptorProtos will have their custom options resolved as extensions, otherwise as the
   * unknown fields. Furthermore, when ExtensionRegistry is supplied, the custom options' extensions
   * will be build with it, otherwise these extensions will be built as {@link DynamicMessage}s for
   * the Message types.
   * 
   * @author protobufel@gmail.com David Tesler
   */
  public static final class Builder {
    private static final String GOOGLE_PROTOBUF_DESCRIPTOR_PROTO =
        "google/protobuf/descriptor.proto";
    private final Map<URI, Set<URI>> dirs;
    private final Map<URI, String> sources;
    private final List<FileDescriptorProto> sourceProtos;
    private final ANTLRErrorStrategy errorStrategy;
    private final IBaseProtoErrorListener errorListener;
    private boolean showCircularTwice;
    private boolean showExistingNonCircular;
    private final FileDescriptors.Builder fileBuilder;

    Builder() {
      this(new DefaultErrorStrategy(), new ConsoleProtoErrorListener(""), FileDescriptors
          .newBuilder());
    }

    Builder(final ANTLRErrorStrategy errorStrategy, final IBaseProtoErrorListener errorListener) {
      this(errorStrategy, errorListener, FileDescriptors.newBuilder());
    }

    Builder(final ANTLRErrorStrategy errorStrategy, final IBaseProtoErrorListener errorListener,
        final FileDescriptors.Builder fileBuilder) {
      dirs = new LinkedHashMap<URI, Set<URI>>();
      sources = new LinkedHashMap<URI, String>();
      sourceProtos = new ArrayList<FileDescriptorProto>();
      this.errorStrategy = Objects.requireNonNull(errorStrategy);
      this.errorListener = Objects.requireNonNull(errorListener);
      this.fileBuilder = Objects.requireNonNull(fileBuilder);
    }

    /**
     * Gets whether to convert a FieldDescriptor's default_field into its regular presentation, or
     * leave as in the original .proto source. The default is false, have it as-is.
     */
    public boolean isProtocCompatible() {
      return fileBuilder.isProtocCompatible();
    }

    /**
     * Sets whether to convert a FieldDescriptor's default_field into its regular presentation, or
     * leave as in the original .proto source. The default is false, have it as-is.
     */
    // FIXME make public when fixed; for now the default is false!
    Builder setProtocCompatible(final boolean protocCompatible) {
      fileBuilder.setProtocCompatible(protocCompatible);
      return this;
    }

    /**
     * Gets whether the custom options will be resolved to extensions, or presented as unknown
     * fields.
     */
    public boolean isCustomOptionsAsExtensions() {
      return fileBuilder.isCustomOptionsAsExtensions();
    }

    /**
     * sets whether the custom options will be resolved to extensions, or presented as unknown
     * fields.
     */
    public Builder setCustomOptionsAsExtensions(final boolean customOptionsAsExtensions) {
      fileBuilder.setCustomOptionsAsExtensions(customOptionsAsExtensions);
      return this;
    }

    /**
     * Gets the user supplied ExtensionRegistry used to resolve the custom options into extensions.
     */
    public ExtensionRegistry getRegistry() {
      return fileBuilder.getRegistry();
    }

    /**
     * Clears the user supplied ExtensionRegistry used to resolve the custom options into
     * extensions. Will use internal ExtensionRegistry, if customOptionsAsExtensions set to true.
     * The resulted extensions for Message types will be built as {@link DynamicMessage}s.
     */
    public Builder clearRegistry() {
      fileBuilder.clearRegistry();
      return this;
    }

    /**
     * Supplies the ExtensionRegistry used to resolve the custom options into extensions.
     */
    public Builder setRegistry(final ExtensionRegistry registry) {
      fileBuilder.setRegistry(registry);
      return this;
    }

    /**
     * Gets whether to report circular dependency error twice for related dependencies; false by
     * default.
     */
    public boolean isShowCircularTwice() {
      return showCircularTwice;
    }

    /**
     * Sets whether to report circular dependency error twice for related dependencies; false by
     * default.
     */
    public Builder setShowCircularTwice(final boolean showCircularTwice) {
      this.showCircularTwice = showCircularTwice;
      return this;
    }

    /**
     * Gets whether to report a dependency on existing but not resolvable proto; false by default.
     */
    public boolean isShowExistingNonCircular() {
      return showExistingNonCircular;
    }

    /**
     * Sets whether to report a dependency on existing but not resolvable proto; false by default.
     */
    public Builder setShowExistingNonCircular(final boolean showExistingNonCircular) {
      this.showExistingNonCircular = showExistingNonCircular;
      return this;
    }

    /**
     * Adds source protos to be processed.
     */
    public Builder addProtos(final Iterable<FileDescriptorProto> protos) {
      if (protos == null) {
        throw new NullPointerException();
      }

      for (final FileDescriptorProto proto : protos) {
        addProto(proto);
      }

      return this;
    }

    /**
     * Adds a source proto to be processed.
     */
    public Builder addProto(final FileDescriptorProto proto) {
      if (proto == null) {
        throw new NullPointerException();
      }

      sourceProtos.add(proto);
      return this;
    }

    /**
     * Adds a source proto to be processed; protoName must be unique.
     */
    public Builder addSource(final String protoName, final String source) {
      if (protoName == null || source == null || source.isEmpty()) {
        throw new NullPointerException();
      }

      sources.put(URI.create(protoName), source);
      return this;
    }

    /**
     * Adds source .proto files, by glob pattern, for processing.
     * 
     * @param baseDir a base directory to get .proto files from
     * @param globPattern a canonical glob pattern, including * and **, separated by {@code /}
     */
    public Builder addFilesByGlob(final File baseDir, final String globPattern) {
      // TODO replace file discovery by common-files' discovery!
      return addFilesByRegex(baseDir, convertGlobToRegex(globPattern));
    }

    private Pattern convertGlobToRegex(final String globPattern) {
      return Pattern.compile(globPattern.replaceAll("\\\\", "/").replaceAll("[.]", "[.]")
          .replaceAll("[?]", ".").replaceAll("[*]{2}+", ".*?")
          .replaceAll("(?<![.])[*]{1}+(?![?])", "[^/]*"));
    }

    /**
     * Adds source .proto files, by regex pattern, for processing.
     * 
     * @param baseDir a base directory to get .proto files from
     * @param pattern a file regex pattern, with canonical {@code /} separator
     */
    public Builder addFilesByRegex(final File rootDir, final Pattern pattern) {
      final List<String> targetFiles = new ArrayList<String>();
      // final File dir = new File(rootDir.toURI().normalize());
      final File dir = getCanonicalFile(rootDir);
      processChildren(dir, dir.toURI(), targetFiles, pattern);
      return addFiles(dir, targetFiles.toArray(new String[0]));
    }

    private void processChildren(final File dir, final URI rootURI, final List<String> targetFiles,
        final Pattern pattern) {
      for (final File child : dir.listFiles()) {
        final String file = rootURI.relativize(child.toURI()).getPath();
        final Matcher matcher = pattern.matcher(file);

        if (child.isDirectory()) {
          if (!matcher.matches() && matcher.hitEnd()) {
            processChildren(child, rootURI, targetFiles, pattern);
          }
        } else {
          if (matcher.matches()) {
            targetFiles.add(file);
          }
        }
      }
    }

    /**
     * Adds source .proto files, based on the rootDir.
     * 
     * @param rootDir a base directory to get .proto files from
     * @param protoFiles file paths relative to the rootDir
     */
    public Builder addFiles(final File rootDir, final String... protoFiles) {
      if (rootDir == null || protoFiles == null || protoFiles.length == 0) {
        throw new NullPointerException();
      }

      // final URI dir = baseDir.toURI().normalize();
      final URI dirURI = getCanonicalFile(rootDir).toURI();
      Set<URI> fileSet = dirs.get(dirURI);

      if (fileSet == null) {
        fileSet = new LinkedHashSet<URI>();
        dirs.put(dirURI, fileSet);
      }

      for (final String file : protoFiles) {
        fileSet.add(dirURI.relativize(URI.create(file)).normalize());
      }

      return this;
    }

    private File getCanonicalFile(final File file) {
      try {
        return file.getCanonicalFile();
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
    }

    /**
     * Adds .proto resources, based on the rootURI. Imports will be treated as URIs.
     * 
     * @param rootURI a base URI to get .proto resources from
     * @param uris proto URIs relative to the rootURI
     */
    public Builder addResources(final URI rootURI, final String... uris) {
      if (rootURI == null || !rootURI.isAbsolute() || uris == null || uris.length == 0) {
        throw new NullPointerException();
      }

      final URI dir = rootURI.normalize();
      Set<URI> fileSet = dirs.get(dir);

      if (fileSet == null) {
        fileSet = new LinkedHashSet<URI>();
        dirs.put(dir, fileSet);
      }

      for (final String uri : uris) {
        fileSet.add(URI.create(uri).normalize());
      }

      return this;
    }

    private ParsedContext parseString(final String source, final String protoName) {
      final ANTLRInputStream fis = new ANTLRInputStream(source);
      return parseProto(fis, protoName);
    }

    private ParsedContext parseFile(final URI rootDir, final URI proto) {
      Throwable exception = null;
      InputStream is = null;

      try {
        is = rootDir.resolve(proto).toURL().openStream();
        final ANTLRInputStream ais = new ANTLRInputStream(is);
        return parseProto(ais, proto.toString());
      } catch (final IOException e) {
        exception = e;
      } finally {
        if (is != null) {
          try {
            is.close();
          } catch (final IOException e) {
            if (exception == null) {
              throw new RuntimeException(e);
            }
          }
        }

        if (exception != null) {
          // FIXME report error instead
          throw new RuntimeException(exception);
        }
      }

      return null;
    }

    @SuppressWarnings("unused")
    private ParsedContext parseFile(final File rootDir, final String protoName) {
      Throwable exception = null;

      try {
        final File file = new File(rootDir, protoName);
        final ANTLRFileStream ais = new ANTLRFileStream(file.getCanonicalPath());
        return parseProto(ais, protoName);
      } catch (final IOException e) {
        exception = e;
      } finally {
        if (exception != null) {
          throw new RuntimeException(exception);
        }
      }

      return null;
    }

    private ParsedContext parseProto(final ANTLRInputStream is, final String protoName) {
      final ProtoLexer lexer = new ProtoLexer(is);
      final CommonTokenStreamEx tokens = new CommonTokenStreamEx(lexer);
      final ProtoParser parser = new ProtoParser(tokens);
      parser.setErrorHandler(errorStrategy);
      parser.removeErrorListeners();
      errorListener.setProtoName(protoName);
      parser.addErrorListener(errorListener);
      // parser.notifyErrorListeners("!!!BEGIN!!!!");
      parser.setBuildParseTree(true); // tell ANTLR to build a parse tree
      final ProtoContext tree = parser.proto();
      final ProtoFileParser protoParser = new ProtoFileParser(protoName, errorListener);
      final ParseTreeWalker walker = new ParseTreeWalker();
      walker.walk(protoParser, tree);
      return protoParser.getParsed();
    }

    /**
     * Returns a mutable list of canonical {@link FileDescriptorProto}s built based on all supplied
     * sources, in the best-effort original input order, as follows:
     * <ol>
     * <li>based on input FileDescriptorProtos
     * <li>based on input .proto files/resources
     * <li>based on input source texts
     * </ol>
     * <p>
     * Any source proto in the {@code google.protobuf} reserved space will cause a validation error.
     * The source {@code descriptor.proto} with the {@code google.protobuf} package, if present,
     * will be replaced by the runtime singleton {@code DescriptorProtos.getDescriptor()}, ensuring
     * the runtime uniqueness of the {@code descriptor.proto}.
     */
    public List<FileDescriptorProto> buildProtos() {
      return buildProtos(true);
    }

    /**
     * Builds a mutable list of canonical {@link FileDescriptorProto}s like {@link #buildProtos()},
     * but also allows to suppress the reserved space validation; useful for testing.
     */
    List<FileDescriptorProto> buildProtos(final boolean validateNotInReservedSpace) {
      final Collection<FileDescriptor> files = build(validateNotInReservedSpace).values();
      final List<FileDescriptorProto> protoList = new ArrayList<FileDescriptorProto>(files.size());

      for (final FileDescriptor file : files) {
        protoList.add(file.toProto());
      }

      return protoList;
    }

    /**
     * Builds a FileDescriptor map, sorted in the original order, with regard to the reserved space.
     * Any source proto in the {@code google.protobuf} reserved space will cause a validation error.
     * The source {@code descriptor.proto} with the {@code google.protobuf} package, if present,
     * will be replaced by the runtime singleton {@code DescriptorProtos.getDescriptor()}, ensuring
     * the runtime uniqueness of the {@code descriptor.proto}.
     *
     * @return a sorted FileDescriptor map based on the source protos
     * @see #buildProtos()
     */
    public Map<String, FileDescriptor> build() {
      return build(true);
    }

    /**
     * Builds a sorted FileDescriptor map, in the original order. Useful for testing within the
     * reserved space; the {@link #build()} delegates to this method with
     * {@code validateNotInReservedSpace = true}.
     *
     * @param validateNotInReservedSpace if true, disallows protos in reseved space, which is the
     *        {@code "google.protobuf"}
     * @return a sorted FileDescriptor map based on the source protos
     */
    Map<String, FileDescriptor> build(final boolean validateNotInReservedSpace) {
      final List<ParsedContext> protos = new ArrayList<ParsedContext>();

      if (!sourceProtos.isEmpty()) {
        for (final FileDescriptorProto proto : sourceProtos) {
          protos.add(new ParsedContext(proto));
        }
      }

      if (!dirs.isEmpty()) {
        for (final Entry<URI, Set<URI>> entry : dirs.entrySet()) {
          final URI rootDir = entry.getKey();

          for (final URI proto : entry.getValue()) {
            protos.add(parseFile(rootDir, proto));
          }
        }
      }

      if (!sources.isEmpty()) {
        for (final Entry<URI, String> entry : sources.entrySet()) {
          final String protoSource = entry.getValue();
          protos.add(parseString(protoSource, entry.getKey().toString()));
        }
      }

      return buildFiles(protos, validateNotInReservedSpace);
    }

    private Map<String, FileDescriptor> buildFiles(final List<ParsedContext> protos,
        final boolean validateNotInReservedSpace) {
      String protoName = null;

      try {
        final Map<String, FileDescriptor> cache =
            buildFilesFrom(protos, errorListener, showCircularTwice, showExistingNonCircular,
                validateNotInReservedSpace);
        // FIXME get protoSet from BuildFrom!
        final Map<String, FileDescriptor> resultCache = new LinkedHashMap<String, FileDescriptor>();

        for (final ParsedContext parsedContext : protos) {
          // there can be some bad protos, so check for null first!
          protoName = parsedContext.getProto().getName();
          final FileDescriptor fileDescriptor = cache.get(protoName);

          if (fileDescriptor == null) {
            errorListener.validationError(null, null,
                String.format("the proto '%s' is not in the file cache!", protoName), null);
          } else {
            // add the cached fileDescriptor in the original order!
            resultCache.put(fileDescriptor.getName(), fileDescriptor);
          }
        }

        return resultCache;
      } catch (final RuntimeException e) {
        errorListener.validationError(null, null, "protoName=" + protoName, e);
      }

      return Collections.emptyMap();
    }

    private Map<String, FileDescriptor> buildFilesFrom(
    /* FIXME change to Map? */final List<ParsedContext> protos,
        final IBaseProtoErrorListener errorListener, final boolean showCircularTwice,
        final boolean showExistingNonCircular, final boolean validateNotInReservedSpace) {
      final Map<String, FileDescriptor> fileMap = new LinkedHashMap<String, FileDescriptor>();
      final LinkedList<ParsedContext> fileProtos = new LinkedList<ParsedContext>();
      final FileDescriptor[] emptyDependencies = new FileDescriptor[0];
      final Set<String> protoNames = new HashSet<String>();
      boolean addedDescriptorProto = true;
      boolean validateNotInReservedSpaceError = false;
      boolean foundDuplicateProtos = false;

      // it is crucial to replace or add the default runtime DescriptorProtos.getDescriptor(),
      // otherwise all message options will fail!!!
      for (final ListIterator<ParsedContext> iterator = protos.listIterator(); iterator.hasNext();) {
        final ParsedContext parsedContext = iterator.next();
        final String protoName = parsedContext.getProto().getName();

        if (!protoNames.add(protoName)) {
          foundDuplicateProtos = true;
          errorListener.validationError(null, null,
              String.format("a duplicate proto %s", protoName), null);
        }

        if (foundDuplicateProtos) {
          continue;
        }

        if (GOOGLE_PROTOBUF_DESCRIPTOR_PROTO.equals(protoName)) {
          addedDescriptorProto = false;
          fileMap.put(GOOGLE_PROTOBUF_DESCRIPTOR_PROTO, DescriptorProtos.getDescriptor());
          iterator.set(ParsedContext.getDescriptorProtoParsedContext());
        } else {
          if (validateNotInReservedSpace
              && !validateNotInReservedSpace(parsedContext.getProto(), errorListener)) {
            validateNotInReservedSpaceError = true;
          }
        }
      }

      if (validateNotInReservedSpaceError || foundDuplicateProtos) {
        return Collections.emptyMap();
      }

      if (addedDescriptorProto) {
        fileMap.put(GOOGLE_PROTOBUF_DESCRIPTOR_PROTO, DescriptorProtos.getDescriptor());
        protoNames.add(GOOGLE_PROTOBUF_DESCRIPTOR_PROTO);
      }

      try {
        // find all leaves
        for (final ParsedContext fileProto : protos) {
          // FIXME if public dependency don't build this proto at all and replace by the referred?
          if (GOOGLE_PROTOBUF_DESCRIPTOR_PROTO.equals(fileProto.getProto().getName())) {
            continue;
          } else if (fileProto.getProto().getDependencyCount() == 0) {
            buildAndCacheProto(fileMap, fileProto, emptyDependencies, errorListener);
          } else if (!protoNames.containsAll(fileProto.getProto().getDependencyList())) {
            for (final String dependency : fileProto.getProto().getDependencyList()) {
              if (!protoNames.contains(dependency)) {
                errorListener.validationError(null, null, "has a non-existent dependency on '"
                    + dependency + "'", null);
              }
            }
          } else {
            // FIXME do processing after this validation loop!
            fileProtos.addFirst(fileProto);
          }
        }

        while (!fileProtos.isEmpty()) {
          // find all fileProto(-s) with only dependencies from the fileMap and
          // make them into files
          boolean isLeafFound = false;

          for (final Iterator<ParsedContext> iterator = fileProtos.iterator(); iterator.hasNext();) {
            final ParsedContext fileProto = iterator.next();
            final List<String> dependencyList = fileProto.getProto().getDependencyList();
            FileDescriptor[] dependencies;

            if (dependencyList.isEmpty()) {
              dependencies = emptyDependencies;
            } else if (!fileMap.keySet().containsAll(fileProto.getProto().getDependencyList())) {
              continue;
            } else {
              dependencies = new FileDescriptor[dependencyList.size()];
              int i = 0;

              for (final String fileName : dependencyList) {
                dependencies[i++] = fileMap.get(fileName);
              }
            }

            buildAndCacheProto(fileMap, fileProto, dependencies, errorListener);
            iterator.remove();
            isLeafFound = true;
          }

          if (!isLeafFound) {
            final Map<String, Set<String>> multimap = new LinkedHashMap<String, Set<String>>();

            for (final ParsedContext fileProto : fileProtos) {
              final LinkedHashSet<String> dependencySet =
                  new LinkedHashSet<String>(fileProto.getProto().getDependencyList());
              dependencySet.removeAll(fileMap.keySet());
              multimap.put(fileProto.getProto().getName(), dependencySet);
            }

            for (final Entry<String, Set<String>> entry : multimap.entrySet()) {
              final String protoName = entry.getKey();
              errorListener.setProtoName(protoName);

              for (final Iterator<String> iterator = entry.getValue().iterator(); iterator
                  .hasNext();) {
                final String dependency = iterator.next();

                if (!multimap.keySet().contains(dependency)) {
                  // a non-existent dependency
                  errorListener.validationError(null, null, "has a non-existent dependency on '"
                      + dependency + "'", null);
                } else if (showCircularTwice ? multimap.get(dependency).contains(protoName)
                    : multimap.get(dependency).remove(protoName)) {
                  // a circular dependency
                  errorListener.validationError(null, null, "has a circular dependency on '"
                      + dependency + "'", null);
                } else if (showExistingNonCircular) {
                  // a dependency on existing but not resolvable
                  errorListener.validationError(null, null,
                      "has a dependency on existing but not resolvable '" + dependency + "'", null);
                }
              }
            }

            // FIXME or should we instead return Collections.emptyMap();
            // for now, returns all valid FileDescriptors, in order of leafs to roots
            return fileMap;
          }
        }
      } finally {
        if (addedDescriptorProto) {
          // get rid of our descriptor.proto before we return!
          fileMap.remove(GOOGLE_PROTOBUF_DESCRIPTOR_PROTO);
        }
      }

      return fileMap;
    }

    private boolean buildAndCacheProto(final Map<String, FileDescriptor> cache,
        final ParsedContext fileProto, final FileDescriptor[] dependencies,
        final IBaseProtoErrorListener errorListener) {
      try {
        // if (fileProto.resolveAllRefs(Arrays.asList(dependencies), errorListener)) {

        // TODO: implement ProtoFileParser.hasCustomOption(); then skip build's proto
        // reserialization and OptionResolver if hasCustomOption() is true!

        final FileDescriptor fileDescriptor =
            fileBuilder.setProto(fileProto.getProto()).addDependencies(dependencies).build();
        cache.put(fileProto.getProto().getName(), fileDescriptor);
        return true;
        // }
      } catch (final DescriptorValidationRuntimeException e) {
        errorListener.setProtoName(fileProto.getProto().getName());
        errorListener.validationError(null, null, e.getDescription(), e);
      }

      return false;
    }

    private boolean validateNotInReservedSpace(final FileDescriptorProtoOrBuilder proto,
        final IBaseProtoErrorListener errorListener) {
      if (proto.hasPackage()
          && (proto.getPackage().equals("google.protobuf") || proto.getPackage().startsWith(
              "google.protobuf."))) {
        errorListener.validationError(null, null,
            String.format("proto %s has package in reserved space", proto.getName()), null);
        return false;
      } else {
        return true;
      }
    }
  }
}
