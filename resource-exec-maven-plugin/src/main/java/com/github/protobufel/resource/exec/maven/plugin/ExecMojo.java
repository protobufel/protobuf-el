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

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
// import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.eclipse.sisu.Description;

@Mojo(name = "exec", defaultPhase = LifecyclePhase.GENERATE_TEST_RESOURCES, requiresProject = true)
@Description("Executes the specified command")
public class ExecMojo extends AbstractMojo {

  public interface IFileSetMatcher extends PathMatcher {
  }

  // TODO remove me!
  private Jsr330Component component;

  /**
   * A path to executable
   */
  @Parameter(name = "execLocation", alias = "exec", property = "execLocation", required = true)
  private String _execLocation = null;

  @Parameter(property = "execLocationAsIs", name = "execLocationAsIs", defaultValue = "true")
  private boolean _execLocationAsIs;

  @Parameter(property = "systemCommand", name = "systemCommand", defaultValue = "false")
  private boolean _systemCommand;

  @Parameter(property = "argQuote", name = "argQuote", defaultValue = "\"")
  private String _argQuote;

  @Parameter(property = "args", name = "args", defaultValue = "")
  private List<String> _args = Collections.emptyList();

  @Parameter(property = "workDir", name = "workDir", defaultValue = "${basedir}")
  private File _workDir;

  @Parameter(property = "environment", name = "environment", defaultValue = "")
  private Map<String, String> _environment;

  @Parameter(property = "errorProperty", name = "errorProperty", defaultValue = "")
  private String _errorProperty;

  @Parameter(property = "errorFile", name = "errorFile", defaultValue = "")
  private File _errorFile;

  @Parameter(property = "errorPipe", name = "errorPipe", defaultValue = "true")
  private boolean _errorPipe;

  @Parameter(property = "errorInherit", name = "errorInherit", defaultValue = "false")
  private boolean _errorInherit;

  @Parameter(property = "errorAppend", name = "errorAppend", defaultValue = "false")
  private boolean _errorAppend;

  @Parameter(property = "outProperty", name = "outProperty", defaultValue = "")
  private String _outProperty;

  @Parameter(property = "outFile", name = "outFile", defaultValue = "")
  private File _outFile;

  @Parameter(property = "outPipe", name = "outPipe", defaultValue = "true")
  private boolean _outPipe;

  @Parameter(property = "outInherit", name = "outInherit", defaultValue = "false")
  private boolean _outInherit;

  @Parameter(property = "outAppend", name = "outAppend", defaultValue = "false")
  private boolean _outAppend;

  @Parameter(property = "exitCode")
  private int _exitCode;

  @Parameter(property = "redirectErrorStream", name = "redirectErrorStream", defaultValue = "false")
  private boolean _redirectErrorStream;

  @Parameter(property = "fileSets", name = "fileSets", defaultValue = "")
  private List<FileSet> _fileSets = Collections.emptyList();

  @Parameter(property = "followLinks", name = "followLinks", defaultValue = "false")
  private boolean _followLinks;

  @Parameter(property = "allowDuplicates", name = "allowDuplicates", defaultValue = "true")
  private boolean _allowDuplicates;

  @Parameter(property = "allowFiles", name = "allowFiles", defaultValue = "true")
  private boolean _allowFiles;

  @Parameter(property = "allowDirs", name = "allowDirs", defaultValue = "false")
  private boolean _allowDirs;

  @Inject
  public ExecMojo(final Jsr330Component component) {
    this.component = component;
  }

  @Override
  public void execute() throws MojoExecutionException {
    //
    // Say hello to the world, my little constructor injected component!
    //
    //component.hello();

    final List<String> command = new ArrayList<>();
    command.add(getCanonicalPath(_execLocation, "exec"));

    getLog().debug(String.format("args=%s", _args));

    if (!_args.isEmpty()) {
      command.addAll(_args);
    }

    if (!_fileSets.isEmpty()) {
      getLog().debug(String.format("fileSets=%s", _fileSets));
      quoteList(getResourceFiles(_fileSets, _followLinks, _allowDuplicates, _allowFiles, 
          _allowDirs), 
          command);
    }

    if (_systemCommand) {
      convertToSystemCommandArgs(command, _argQuote);
    }

    getLog().debug(String.format("command=%s", command));
    Redirect errorRedirect;

    // if (!getCanonicalPath(_errorFile, "_errorFile").isEmpty()) {
    if (_errorFile != null) {
      _errorFile = getCanonicalFile(_errorFile, "_errorFile");
      errorRedirect = _errorAppend ? Redirect.appendTo(_errorFile) : Redirect.to(_errorFile);
    } else if (!_errorProperty.isEmpty()) {
      throw new MojoExecutionException("_errorProperty currently unsupported");
    } else if (_errorInherit) {
      errorRedirect = Redirect.INHERIT;
    } else if (_errorPipe) {
      errorRedirect = Redirect.PIPE;
    } else {
      throw new MojoExecutionException(
          "must specify one of these: _errorProperty, _errorFile, _errorPipe, _errorInherit");
    }

    Redirect outRedirect;

    // if (!getCanonicalPath(_outFile, "_outFile").isEmpty()) {
    if (_outFile != null) {
      _outFile = getCanonicalFile(_outFile, "_outFile");
      outRedirect = _outAppend ? Redirect.appendTo(_outFile) : Redirect.to(_outFile);
    } else if (_outInherit) {
      outRedirect = Redirect.INHERIT;
    } else if (_outPipe) {
      outRedirect = Redirect.PIPE;
    } else if (!_outProperty.isEmpty()) {
      throw new MojoExecutionException("_outProperty currently unsupported");
    } else {
      throw new MojoExecutionException(
          "must specify one of these: _outProperty, _outFile, _outPipe, _outInherit");
    }

    _exitCode =
        createProcess(command, errorRedirect, outRedirect, _workDir, _environment,
            _redirectErrorStream);
  }

  private void convertToSystemCommandArgs(final List<String> command, final CharSequence quote) {
    final StringBuilder sb = new StringBuilder();
    sb.append(command.get(0)).append(" ");
    
    if (System.getProperty("os.name").toLowerCase().indexOf("win") != -1) {
      // for Windows construct cmd /c "arg1 "arg2" ... "arg<N>""
      appendArgs(sb, command.subList(1, command.size()), "\"", true);
      command.clear();
      command.add("cmd");
      command.add("/c");
      command.add(sb.toString());
    } else {
      // for any other OS construct /bin/bash -c "arg1 Qarg1Q Qarg2Q ... Qarg<N>Q", where Q={'|"}
      appendArgs(sb, command.subList(1, command.size()), quote, false);
      command.clear();
      command.add("/bin/bash");
      command.add("-c");
      command.add(sb.toString());
    }
  }

  private StringBuilder appendArgs(final StringBuilder sb, final Collection<? extends CharSequence> args,
      final CharSequence quote, final boolean testDoubleQuoteOnly) {
    if (args.isEmpty()) {
      return sb;
    }
    
    for (final CharSequence arg : args) {
      appendArg(sb, arg, quote, testDoubleQuoteOnly).append(" ");
    }

    sb.setLength(sb.length() - 1);
    return sb;
  }

  private StringBuilder appendArg(final StringBuilder sb, final CharSequence arg,
      final CharSequence quote, final boolean testDoubleQuoteOnly) {
    if ((arg.charAt(0) == '"') || (!testDoubleQuoteOnly && (arg.charAt(0) == '\''))) {
      sb.append(arg);
    } else {
      sb.append(quote).append(arg).append(quote);
    }

    return sb;
  }

  private void quoteList(final Iterable<String> source, final List<String> target) {
    final String quote =
        (System.getProperty("os.name").toLowerCase().indexOf("win") == -1) ? "'" : "\"";

    for (final String s : source) {
      target.add(quote + s + quote);
    }
  }

  Collection<String> getResourceFiles(final Iterable<? extends FileSet> fileSets,
      final boolean followLinks, final boolean allowDuplicates, final boolean allowFiles,
      final boolean allowDirs) throws MojoExecutionException {
    final Set<String> result = new LinkedHashSet<>();

    for (final FileSet fileSet : Objects.requireNonNull(fileSets)) {
      final Collection<String> files =
          getResourceFiles(fileSet, followLinks, allowDuplicates, allowFiles, allowDirs);
      for (final String file : files) {
        if (!result.add(file) && !allowDuplicates) {
          getLog()
              .error(
                  String.format("a duplicate file %s under directory %s", file,
                      fileSet.getDirectory()));
          throw new MojoExecutionException(String.format("a duplicate file %s under directory %s",
              file, fileSet.getDirectory()));
        }
      }
    }

    return result;
  }

  private Collection<String> getResourceFiles(final FileSet fileSet, final boolean followLinks,
      final boolean allowDuplicates, final boolean allowFiles, final boolean allowDirs)
      throws MojoExecutionException {
    final Path dirPath = Paths.get(Objects.requireNonNull(fileSet).getDirectory());
    final FileSetPathMatcher matcher =
        new FileSetPathMatcher(fileSet.getIncludes(), fileSet.getExcludes(), dirPath);
    final EnumSet<FileVisitOption> options =
        followLinks ? EnumSet.of(FileVisitOption.FOLLOW_LINKS) : EnumSet
            .noneOf(FileVisitOption.class);
    final Set<String> result = new LinkedHashSet<>();

    try {
      final ResourceVisitor resourceVisitor =
          new ResourceVisitor(matcher, allowDuplicates, allowFiles, allowDirs, result, getLog());
      Files.walkFileTree(dirPath, options, Integer.MAX_VALUE, resourceVisitor);

      if (resourceVisitor.getErrorMessage() != null) {
        throw new MojoExecutionException(resourceVisitor.getErrorMessage());
      }
    } catch (final IOException e) {
      getLog().error("fileSet caused error", e);
      throw new MojoExecutionException("fileSet caused error", e);
    }

    return result;
  }

  private String getCanonicalPath(final String _execLocation, final String propName)
      throws MojoExecutionException {
    if (_execLocationAsIs) {
      return _execLocation;
    }

    try {
      return getWorkDir().toPath().resolve(_execLocation).toAbsolutePath().toString();
    } catch (final Throwable e) {
      // getLog().error(String.format("%s has wrong path", propName), e);
      throw new MojoExecutionException(String.format("%s has wrong path %s", propName,
          _execLocation));
    }
  }

  private File getCanonicalFile(final File file, final String propName)
      throws MojoExecutionException {
    try {
      return file.getCanonicalFile();
    } catch (final IOException e) {
      // getLog().error(String.format("%s has wrong path", propName), e);
      throw new MojoExecutionException(String.format("%s has wrong path", propName));
    }
  }

  private int createProcess(final List<String> command, final Redirect errorRedirect,
      final Redirect outRedirect, final File directory, final Map<String, String> environment,
      final boolean redirectErrorStream) {
    final ProcessBuilder processBuilder =
        new ProcessBuilder(command).redirectErrorStream(redirectErrorStream)
            .redirectError(errorRedirect).redirectOutput(outRedirect).directory(directory);

    if ((environment != null) && !environment.isEmpty()) {
      processBuilder.environment().putAll(environment);
    }

    Process process;

    try {
      process = processBuilder.start();
      return process.waitFor();
    } catch (final IOException e) {
      getLog().error(e);
    } catch (final InterruptedException e) {
      getLog().error(e);
    }

    return -1;
  }

//  public Jsr330Component getComponent() {
//    return component;
//  }
//
//  public void setComponent(final Jsr330Component component) {
//    this.component = component;
//  }

  public String getExecLocation() {
    return _execLocation;
  }

  public void setExecLocation(final String execLocation) {
    _execLocation = execLocation;
  }

  public boolean isExecLocationAsIs() {
    return _execLocationAsIs;
  }

  public void setExecLocationAsIs(final boolean execLocationAsIs) {
    _execLocationAsIs = execLocationAsIs;
  }

  public boolean isSystemCommand() {
    return _systemCommand;
  }

  public void setSystemCommand(final boolean systemCommand) {
    _systemCommand = systemCommand;
  }

  public String getArgQuote() {
    return _argQuote;
  }

  public void setArgQuote(final String argQuote) {
    _argQuote = argQuote;
  }

  public List<String> getArgs() {
    return Collections.unmodifiableList(_args);
  }

  public void setArgs(final List<String> args) {
    _args = new ArrayList<>(args);
  }

  public File getWorkDir() {
    return _workDir;
  }

  public void setWorkDir(final File workDir) {
    _workDir = workDir;
  }

  public Map<String, String> getEnvironment() {
    return _environment;
  }

  public void setEnvironment(final Map<String, String> environment) {
    _environment = environment;
  }

  public String getErrorProperty() {
    return _errorProperty;
  }

  public void setErrorProperty(final String errorProperty) {
    _errorProperty = errorProperty;
  }

  public File getErrorFile() {
    return _errorFile;
  }

  public void setErrorFile(final File errorFile) {
    _errorFile = errorFile;
  }

  public boolean isErrorPipe() {
    return _errorPipe;
  }

  public void setErrorPipe(final boolean errorPipe) {
    _errorPipe = errorPipe;
  }

  public boolean isErrorInherit() {
    return _errorInherit;
  }

  public void setErrorInherit(final boolean errorInherit) {
    _errorInherit = errorInherit;
  }

  public boolean isErrorAppend() {
    return _errorAppend;
  }

  public void setErrorAppend(final boolean errorAppend) {
    _errorAppend = errorAppend;
  }

  public String getOutProperty() {
    return _outProperty;
  }

  public void setOutProperty(final String outProperty) {
    _outProperty = outProperty;
  }

  public File getOutFile() {
    return _outFile;
  }

  public void setOutFile(final File outFile) {
    _outFile = outFile;
  }

  public boolean isOutPipe() {
    return _outPipe;
  }

  public void setOutPipe(final boolean outPipe) {
    _outPipe = outPipe;
  }

  public boolean isOutInherit() {
    return _outInherit;
  }

  public void setOutInherit(final boolean outInherit) {
    _outInherit = outInherit;
  }

  public boolean isOutAppend() {
    return _outAppend;
  }

  public void setOutAppend(final boolean outAppend) {
    _outAppend = outAppend;
  }

  public int getExitCode() {
    return _exitCode;
  }

  public void setExitCode(final int exitCode) {
    _exitCode = exitCode;
  }

  public boolean isRedirectErrorStream() {
    return _redirectErrorStream;
  }

  public void setRedirectErrorStream(final boolean redirectErrorStream) {
    _redirectErrorStream = redirectErrorStream;
  }

  public List<FileSet> getFileSets() {
    return _fileSets;
  }

  public void setFileSets(final List<? extends FileSet> fileSets) {
    _fileSets = new ArrayList<>(fileSets);
  }

  public boolean isFollowLinks() {
    return _followLinks;
  }

  public void setFollowLinks(final boolean followLinks) {
    _followLinks = followLinks;
  }

  public boolean isAllowDuplicates() {
    return _allowDuplicates;
  }

  public void setAllowDuplicates(final boolean allowDuplicates) {
    _allowDuplicates = allowDuplicates;
  }

  public boolean isAllowFiles() {
    return _allowFiles;
  }

  public void setAllowFiles(final boolean allowFiles) {
    _allowFiles = allowFiles;
  }

  public boolean isAllowDirs() {
    return _allowDirs;
  }

  public void setAllowDirs(final boolean allowDirs) {
    _allowDirs = allowDirs;
  }

  // TODO replace with common-files-v7
  static final class ResourceVisitor extends SimpleFileVisitor<Path> {
    private final IFileSetMatcher matcher;
    private final boolean allowDuplicates;
    private final boolean allowFiles;
    private final boolean allowDirs;
    private final Set<String> result;
    private String errorMessage;
    private final Log log;
    private boolean skipRoot = true;

    private ResourceVisitor(final IFileSetMatcher matcher, final boolean allowDuplicates,
        final boolean allowFiles, final boolean allowDirs, final Set<String> result, final Log log) {
      if (!allowDirs && !allowFiles) {
        throw new IllegalArgumentException("_allowDirs and _allowFiles cannot be both false");
      }

      this.matcher = Objects.requireNonNull(matcher);
      this.result = Objects.requireNonNull(result);
      this.log = Objects.requireNonNull(log);
      this.allowDuplicates = allowDuplicates;
      this.allowFiles = allowFiles;
      this.allowDirs = allowDirs;
      errorMessage = null;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
        throws IOException {
      super.preVisitDirectory(dir, attrs);

      if (skipRoot) {
        skipRoot = false;
        return FileVisitResult.CONTINUE;
      } else if (!allowDirs) {
        return FileVisitResult.CONTINUE;
      }

      if (matcher.matches(dir)) {
        if (!result.add(dir.toAbsolutePath().normalize().toString()) && !allowDuplicates) {
          errorMessage = String.format("found a duplicate folder %s", dir);
          log.error(errorMessage);
          return FileVisitResult.TERMINATE;
        }
      }

      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
        throws IOException {
      if (!allowFiles) {
        return super.visitFile(file, attrs);
      }

      super.visitFile(file, attrs);

      if (matcher.matches(file)) {
        if (!result.add(file.toAbsolutePath().normalize().toString()) && !allowDuplicates) {
          errorMessage = String.format("found a duplicate file %s", file);
          log.error(errorMessage);
          return FileVisitResult.TERMINATE;
        }
      }

      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(final Path file, final IOException exc)
        throws IOException {
      Objects.requireNonNull(file);
      log.error(String.format("file %s cannot be visited", file), exc);
      throw exc;
    }

    public String getErrorMessage() {
      return errorMessage;
    }
  }

  public static final class CompositeFileMatcher implements IFileSetMatcher {
    private static final CompositeFileMatcher EMPTY = new CompositeFileMatcher();
    private final Map<Path, Set<FileSetPathMatcher>> matchers;

    private CompositeFileMatcher() {
      matchers = Collections.emptyMap();
    }

    public CompositeFileMatcher(final Iterable<? extends FileSetPathMatcher> matchers) {
      final Map<Path, Set<FileSetPathMatcher>> matcherMap =
          updateMatcherMap(new HashMap<Path, Set<FileSetPathMatcher>>(), matchers);
      this.matchers = getVerifiedMatcherMap(matcherMap);
    }

    private CompositeFileMatcher(final Map<Path, Set<FileSetPathMatcher>> matcherMap) {
      matchers = getVerifiedMatcherMap(matcherMap);
    }

    private Map<Path, Set<FileSetPathMatcher>> getVerifiedMatcherMap(
        final Map<Path, Set<FileSetPathMatcher>> matcherMap) {
      if (matcherMap.isEmpty()) {
        throw new IllegalArgumentException("matchers cannot be empty");
      }

      for (final Entry<Path, Set<FileSetPathMatcher>> entry : matcherMap.entrySet()) {
        entry.setValue(Collections.unmodifiableSet(entry.getValue()));
      }

      return Collections.unmodifiableMap(matcherMap);
    }

    private Map<Path, Set<FileSetPathMatcher>> updateMatcherMap(
        final Map<Path, Set<FileSetPathMatcher>> matcherMap,
        final Map<Path, Set<FileSetPathMatcher>> matchers) {
      for (final Entry<Path, Set<FileSetPathMatcher>> entry : Objects.requireNonNull(matchers)
          .entrySet()) {
        Set<FileSetPathMatcher> valueSet = matcherMap.get(entry.getKey());

        if (valueSet == null) {
          valueSet = new LinkedHashSet<>();
          matcherMap.put(entry.getKey(), valueSet);
        }

        valueSet.addAll(entry.getValue());
      }

      return matcherMap;
    }

    private Map<Path, Set<FileSetPathMatcher>> updateMatcherMap(
        final Map<Path, Set<FileSetPathMatcher>> matcherMap,
        final Iterable<? extends FileSetPathMatcher> matchers) {
      for (final FileSetPathMatcher matcher : Objects.requireNonNull(matchers)) {
        updateMatcherMap(matcherMap, matcher);
      }

      return matcherMap;
    }

    private Map<Path, Set<FileSetPathMatcher>> updateMatcherMap(
        final Map<Path, Set<FileSetPathMatcher>> matcherMap, final FileSetPathMatcher matcher) {
      if (Objects.requireNonNull(matcher) == FileSetPathMatcher.EMPTY) {
        return Objects.requireNonNull(matcherMap);
      }

      Set<FileSetPathMatcher> valueSet = matcherMap.get(matcher.dir);

      if (valueSet == null) {
        valueSet = new LinkedHashSet<>();
        matcherMap.put(matcher.dir, valueSet);
      }

      valueSet.add(matcher);
      return matcherMap;
    }

    public static CompositeFileMatcher emptyInstance() {
      return EMPTY;
    }

    public CompositeFileMatcher and(final CompositeFileMatcher other) {
      final Map<Path, Set<FileSetPathMatcher>> matcherMap =
          updateMatcherMap(new HashMap<Path, Set<FileSetPathMatcher>>(matchers), other.matchers);
      return new CompositeFileMatcher(matcherMap);
    }

    public CompositeFileMatcher and(final Iterable<? extends CompositeFileMatcher> others) {
      final Map<Path, Set<FileSetPathMatcher>> matcherMap =
          new HashMap<Path, Set<FileSetPathMatcher>>(matchers);

      for (final CompositeFileMatcher matcher : others) {
        updateMatcherMap(matcherMap, matcher.matchers);
      }

      return new CompositeFileMatcher(matcherMap);
    }

    @Override
    public boolean matches(final Path path) {
      for (final Entry<Path, Set<FileSetPathMatcher>> entry : matchers.entrySet()) {
        final Path dir = entry.getKey();

        if (dir.getFileSystem().equals(path.getFileSystem())) {
          final Path relativePath;

          try {
            relativePath = dir.relativize(path.toAbsolutePath());
          } catch (final IllegalArgumentException e) {
            // this is not applicable to the current dir
            continue;
          }

          for (final FileSetPathMatcher matcher : entry.getValue()) {
            if (matcher.matches(relativePath, true)) {
              return true;
            }
          }
        }
      }

      return false;
    }
  }

  public static final class FileSetPathMatcher implements IFileSetMatcher {
    private static final FileSetPathMatcher EMPTY = new FileSetPathMatcher();
    final List<PathMatcher> includes;
    final List<PathMatcher> excludes;
    final Path dir;

    public static FileSetPathMatcher emptyInstance() {
      return EMPTY;
    }

    private FileSetPathMatcher() {
      includes = Collections.emptyList();
      excludes = Collections.emptyList();
      dir = null;
    }

    public FileSetPathMatcher(final List<String> includes, final List<String> excludes,
        final Path dir) {
      this.dir = Objects.requireNonNull(dir).toAbsolutePath().normalize();
      final FileSystem fs = this.dir.getFileSystem();

      if (Objects.requireNonNull(includes).isEmpty()) {
        this.includes = Collections.emptyList();
      } else {
        final List<PathMatcher> includesList = new ArrayList<>(includes.size());

        for (final String include : Objects.requireNonNull(includes)) {
          includesList.add(fs.getPathMatcher("glob:" + include));
        }

        this.includes = Collections.unmodifiableList(includesList);
      }

      if (Objects.requireNonNull(excludes).isEmpty()) {
        this.excludes = Collections.emptyList();
      } else {
        final List<PathMatcher> excludesList = new ArrayList<>(excludes.size());

        for (final String exclude : Objects.requireNonNull(excludes)) {
          excludesList.add(fs.getPathMatcher("glob:" + exclude));
        }

        this.excludes = Collections.unmodifiableList(excludesList);
      }
    }

    private FileSetPathMatcher(final Path dir, final List<PathMatcher> includesList,
        final List<PathMatcher> excludesList) {
      this.dir = dir;
      includes = Collections.unmodifiableList(includesList);
      excludes = Collections.unmodifiableList(excludesList);
    }

    public FileSetPathMatcher and(final FileSetPathMatcher other) {
      final List<PathMatcher> includesList = new ArrayList<>(includes);
      final List<PathMatcher> excludesList = new ArrayList<>(excludes);
      addMatcher(dir, includesList, excludesList, other);
      return new FileSetPathMatcher(dir, includesList, excludesList);
    }

    public FileSetPathMatcher and(final Iterable<? extends FileSetPathMatcher> other) {
      final List<PathMatcher> includesList = new ArrayList<>(includes);
      final List<PathMatcher> excludesList = new ArrayList<>(excludes);

      for (final FileSetPathMatcher matcher : Objects.requireNonNull(other)) {
        addMatcher(dir, includesList, excludesList, matcher);
      }

      return new FileSetPathMatcher(dir, includesList, excludesList);
    }

    private void addMatcher(final Path dir, final List<PathMatcher> includesList,
        final List<PathMatcher> excludesList, final FileSetPathMatcher other) {
      try {
        if (!Files.isSameFile(dir, Objects.requireNonNull(other).dir)) {
          throw new IllegalArgumentException("the other's directory cannont be different");
        }
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }

      includesList.addAll(other.includes);
      excludesList.addAll(other.excludes);
    }

    @Override
    public boolean matches(final Path path) {
      return matches(path, false);
    }

    public boolean matches(final Path path, final boolean isSanitized) {
      final Path sanitizedPath;

      if (!isSanitized) {
        // we should succeed relativizing, otherwise it's a user error
        // for applying a wrong matcher!
        sanitizedPath = dir.relativize(path.toAbsolutePath());
      } else {
        sanitizedPath = path;
      }

      if (this == EMPTY) {
        return false;
      }

      boolean matched = false;

      if (!includes.isEmpty()) {
        for (final PathMatcher include : includes) {
          if (include.matches(sanitizedPath)) {
            matched = true;
            break;
          }
        }
      }

      if (!matched) {
        return false;
      }

      if (!excludes.isEmpty()) {
        for (final PathMatcher exclude : excludes) {
          if (exclude.matches(sanitizedPath)) {
            return false;
          }
        }
      }

      return true;
    }
  }
}
