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

import static com.github.protobufel.grammar.AbstractMessageUtils.compareProto;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.TextFormat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.protobufel.grammar.Misc.ReplacerTextComparator.BaseReplacer;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.DescriptorProto.ExtensionRange;
import com.google.protobuf.DescriptorProtos.DescriptorProtoOrBuilder;
import com.google.protobuf.DescriptorProtos.EnumOptions;
import com.google.protobuf.DescriptorProtos.EnumValueOptions;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.DescriptorProtos.MessageOptions;
import com.google.protobuf.DescriptorProtos.MethodOptions;
import com.google.protobuf.DescriptorProtos.ServiceOptions;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;

@NonNullByDefault
public final class Misc {
  @SuppressWarnings("null")
  private static final String RANGE_END_FIELD = ExtensionRange.getDescriptor()
      .findFieldByNumber(ExtensionRange.END_FIELD_NUMBER).getFullName();
  @SuppressWarnings("null")
  private static final String DESCRIPTOR_DEFAULT_FIELD = FieldDescriptorProto.getDescriptor()
      .findFieldByNumber(FieldDescriptorProto.DEFAULT_VALUE_FIELD_NUMBER).getFullName();
  @SuppressWarnings("unused")
  private static final Set<Descriptor> ALL_OPTION_DESCRIPTORS = new HashSet<>(Arrays.asList(
      FileOptions.getDescriptor(), MessageOptions.getDescriptor(), EnumOptions.getDescriptor(),
      FieldOptions.getDescriptor(), EnumValueOptions.getDescriptor(),
      ServiceOptions.getDescriptor(), MethodOptions.getDescriptor()));
  private static final String PROTOC_FDS = "protoc/FileDescriptorSet";
  private static final String PROTOC_FDS_WITH_SOURCE_INFO =
      "protoc/FileDescriptorSetWithSourceInfo";
  @SuppressWarnings("null")
  private static final Logger log = LoggerFactory.getLogger(Misc.class);

  private Misc() {}

  public enum FieldTypeRefsMode {
    AS_IS, RELATIVE_TO_PACKAGE, RELATIVE_SIMPLE_NAME, RELATIVE_TO_PARENT
  }

  public static boolean isValidProtoPath(final String path) {
    final Pattern pattern = Pattern.compile("(?:)");
    return path != null && !path.isEmpty() && pattern.matcher(path).matches();
  }

  @SuppressWarnings("null")
  public static @Nullable URL getFileUrl(final String path) {
    return getUrl(new File(path).toURI());
  }

  public static @Nullable URL getUrl(final URI absoluteUri) {
    try {
      return absoluteUri.toURL();
    } catch (final MalformedURLException e) {
    }

    return null;
  }

  public static @Nullable URL getUrl(final String absolutePath) {
    try {
      return new URI(absolutePath).toURL();
    } catch (final MalformedURLException e) {
    } catch (final URISyntaxException e) {
    }

    return null;
  }

  @SuppressWarnings("null")
  public static FileDescriptorProto getProtocFileDescriptorProto(final String protoName,
      final boolean includeSourceInfo, final FieldTypeRefsMode fieldTypeRefsMode) {
    return getProtocFileDescriptorProtos(Pattern.compile(protoName, Pattern.LITERAL),
        includeSourceInfo, fieldTypeRefsMode).get(0);
  }

  public static List<FileDescriptorProto> getProtocFileDescriptorProtos(
      final Pattern protoNamePattern, final boolean includeSourceInfo,
      final FieldTypeRefsMode fieldTypeRefsMode) {
    final String fdsPath = includeSourceInfo ? PROTOC_FDS_WITH_SOURCE_INFO : PROTOC_FDS;
    return getFileDescriptorProtos(protoNamePattern, includeSourceInfo, fieldTypeRefsMode, fdsPath,
        Misc.class);
  }

  @SuppressWarnings("null")
  public static FileDescriptorProto getFileDescriptorProto(final String protoName,
      final boolean includeSourceInfo, final FieldTypeRefsMode fieldTypeRefsMode,
      final String fileDescriptorSetPath, final Class<?> baseResourceClass) {
    return getFileDescriptorProtos(Pattern.compile(protoName, Pattern.LITERAL), includeSourceInfo,
        fieldTypeRefsMode, fileDescriptorSetPath, baseResourceClass).get(0);
  }

  @SuppressWarnings("null")
  public static List<FileDescriptorProto> getFileDescriptorProtos(final Pattern protoNamePattern,
      final boolean includeSourceInfo, final FieldTypeRefsMode fieldTypeRefsMode,
      final String fileDescriptorSetPath, final Class<?> baseResourceClass) {
    final List<FileDescriptorProto> fdProtos =
        getFileDescriptorProtos(protoNamePattern, fileDescriptorSetPath, baseResourceClass);

    if (!includeSourceInfo && fieldTypeRefsMode != FieldTypeRefsMode.AS_IS) {
      for (final ListIterator<FileDescriptorProto> iterator = fdProtos.listIterator(); iterator
          .hasNext();) {
        final FileDescriptorProto fd = iterator.next();
        iterator.set(makeProtoRefsRelative(fd, fieldTypeRefsMode).build());
      }
    }

    return fdProtos;
  }

  private static List<FileDescriptorProto> getFileDescriptorProtos(final Pattern protoNamePattern,
      final String fileDescriptorSetPath, final @Nullable Class<?> baseResourceClass) {
    InputStream is = null;

    try {
      if (baseResourceClass == null) {
        is = new File(fileDescriptorSetPath).toURI().toURL().openStream();
      } else {
        is = baseResourceClass.getResourceAsStream(fileDescriptorSetPath);
      }

      final FileDescriptorSet fdSet = FileDescriptorSet.parseFrom(is);
      final List<String> fdProtoNames = new ArrayList<String>();

      for (final FileDescriptorProto fdProto : fdSet.getFileList()) {
        fdProtoNames.add(fdProto.getName());
      }

      log.debug("all fdProtoNames: {}", fdProtoNames);

      final List<FileDescriptorProto> result = new ArrayList<FileDescriptorProto>();
      fdProtoNames.clear();

      for (final FileDescriptorProto fdProto : fdSet.getFileList()) {
        if (protoNamePattern.matcher(fdProto.getName()).matches()) {
          result.add(fdProto);
          fdProtoNames.add(fdProto.getName());
        }
      }

      log.debug("result fdProtoNames {}", fdProtoNames);
      return result;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (final IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  @SuppressWarnings("null")
  public static FileDescriptorProto.Builder makeProtoRefsRelative(final FileDescriptorProto proto,
      final FieldTypeRefsMode fieldTypeRefsMode) {
    if (fieldTypeRefsMode == FieldTypeRefsMode.AS_IS) {
      return FileDescriptorProto.newBuilder(proto);
    }

    final FileDescriptorProto.Builder protoBuilder = FileDescriptorProto.newBuilder(proto);
    final String packagePath = "." + proto.getPackage();

    for (final FieldDescriptorProto.Builder field : protoBuilder.getExtensionBuilderList()) {
      makeFieldRefsRelative(packagePath, field, fieldTypeRefsMode, packagePath);
    }

    for (final DescriptorProto.Builder message : protoBuilder.getMessageTypeBuilderList()) {
      makeMessageRefsRelative(packagePath, message, fieldTypeRefsMode, packagePath);
    }

    return protoBuilder;
  }

  @SuppressWarnings("null")
  private static void makeMessageRefsRelative(final String packagePath,
      final DescriptorProto.Builder message, final FieldTypeRefsMode fieldTypeRefsMode,
      final String parentFullName) {
    final String myFullName = parentFullName + "." + message.getName();

    for (final FieldDescriptorProto.Builder field : message.getExtensionBuilderList()) {
      makeFieldRefsRelative(packagePath, field, fieldTypeRefsMode, myFullName);
    }

    for (final FieldDescriptorProto.Builder field : message.getFieldBuilderList()) {
      makeFieldRefsRelative(packagePath, field, fieldTypeRefsMode, myFullName);
    }

    for (final DescriptorProto.Builder child : message.getNestedTypeBuilderList()) {
      makeMessageRefsRelative(packagePath, child, fieldTypeRefsMode, myFullName);
    }
  }

  @SuppressWarnings("null")
  private static void makeFieldRefsRelative(final String packagePath,
      final FieldDescriptorProto.Builder field, final FieldTypeRefsMode fieldTypeRefsMode,
      final String parentFullName) {
    if (field.hasExtendee() && field.getExtendee().startsWith(".")) {
      field.setExtendee(getRelativeName(packagePath, field.getExtendee(), fieldTypeRefsMode,
          parentFullName));
    }

    if (field.hasTypeName() && field.getTypeName().startsWith(".")) {
      field.setTypeName(getRelativeName(packagePath, field.getTypeName(), fieldTypeRefsMode,
          parentFullName));

      if (field.hasType()) {
        field.clearType();
      }
    }
  }

  @SuppressWarnings("null")
  private static String getRelativeName(final String packagePath, final String fullName,
      final FieldTypeRefsMode fieldTypeRefsMode, final String parentFullName) {
    switch (fieldTypeRefsMode) {
      case AS_IS:
        return fullName;
      case RELATIVE_SIMPLE_NAME:
        return fullName.substring(fullName.lastIndexOf(".") + 1);
      case RELATIVE_TO_PARENT:
        return getRelativePath(fullName, parentFullName + ".", ".");
      case RELATIVE_TO_PACKAGE:
        if (fullName.startsWith(packagePath)) {
          return fullName.substring(packagePath.length() + 1);
        } else {
          return fullName;
        }
      default:
        throw new UnsupportedOperationException();
    }
  }

  /**
   * Returns the relative path based on the common path of two strings. If {@code basePath} is
   * supposed to be a real base of the {@code path}, then it should end with {@code separator}.
   */
  @SuppressWarnings("null")
  public static String getRelativePath(final String path, final String basePath,
      final String separator) {
    return path.substring(lengthOfCommonPath(path, basePath, separator));
  }

  /**
   * Returns the common path's length of two strings, including the separator.
   */
  public static int lengthOfCommonPath(final String one, final String two, final String separator) {
    final int sepLen = separator.length();
    int pos = -sepLen;
    int oldPos;

    do {
      oldPos = pos + sepLen;
      pos = one.indexOf(separator, oldPos);
    } while (pos != -1 && one.regionMatches(oldPos, two, oldPos, pos - oldPos + 1));

    return oldPos;
  }

  @NonNullByDefault(false)
  public static final class IsComparableToDescriptor extends TypeSafeDiagnosingMatcher<Descriptor> {
    private final Descriptor expected;

    public IsComparableToDescriptor(final Descriptor expected) {
      super();
      this.expected = expected;
    }

    @Override
    public void describeTo(final Description description) {
      appendDescriptorDescription(expected, description);
    }

    @SuppressWarnings("null")
    @Override
    protected boolean matchesSafely(final Descriptor actual, final Description mismatchDescription) {
      boolean matched = true;

      if (!actual.getClass().equals(expected.getClass())) {
        mismatchDescription.appendText("\nmismatched class: ").appendValue(
            actual.getClass().getName());
        matched = false;
      }

      if (!actual.getFullName().equals(expected.getFullName())) {
        mismatchDescription.appendText("\nmismatched full name:").appendValue(actual.getFullName());
        matched = false;
      }

      if (!actual.getFile().getName().equals(expected.getFile().getName())) {
        mismatchDescription.appendText("\nmismatched file name:").appendValue(
            actual.getFile().getName());
        matched = false;
      }

      if (actual.getIndex() != expected.getIndex()) {
        mismatchDescription.appendText("\nmismatched index:").appendValue(actual.getIndex());
        matched = false;
      }

      if (!compareProto(actual.toProto(), expected.toProto())) {
        mismatchDescription.appendText("\nmismatched proto:\n").appendText(
            TextFormat.printToUnicodeString(actual.toProto()));
        matched = false;
      }

      return matched;
    }

    private void appendDescriptorDescription(final Descriptor desc, final Description description) {
      description.appendText("\nclass: ").appendValue(desc.getClass().getName())
          .appendText("\nfull name:").appendValue(desc.getFullName()).appendText("\nfile name:")
          .appendValue(desc.getFile().getName()).appendText("\nindex:")
          .appendValue(desc.getIndex()).appendText("\nproto:\n")
          .appendText(TextFormat.printToUnicodeString(desc.toProto()));
    }

    @Factory
    public static Matcher<Descriptor> comparesToDescriptor(final Descriptor descriptor) {
      return new IsComparableToDescriptor(descriptor);
    }
  }


  @NonNullByDefault(false)
  public static final class IsComparableToFileDescriptorProto extends
      TypeSafeDiagnosingMatcher<FileDescriptorProto> {
    private final FileDescriptorProto expected;

    public IsComparableToFileDescriptorProto(final FileDescriptorProto expected) {
      super();
      this.expected = expected;
    }

    @Override
    public void describeTo(final Description description) {
      appendDescriptorDescription(expected, description);
    }

    @Override
    protected boolean matchesSafely(final FileDescriptorProto actual,
        final Description mismatchDescription) {
      boolean matched = true;

      if (!actual.getClass().equals(expected.getClass())) {
        mismatchDescription.appendText("\nmismatched class: ").appendValue(
            actual.getClass().getName());
        matched = false;
      }

      if (!actual.getPackage().equals(expected.getPackage())) {
        mismatchDescription.appendText("\nmismatched package:").appendValue(actual.getPackage());
        matched = false;
      }

      if (!actual.getName().equals(expected.getName())) {
        mismatchDescription.appendText("\nmismatched name:").appendValue(actual.getName());
        matched = false;
      }

      if (!compareProto(actual, expected)) {
        mismatchDescription.appendText("\nmismatched proto:\n").appendText(
            TextFormat.printToUnicodeString(actual));
        matched = false;
      }

      return matched;
    }

    private void appendDescriptorDescription(final FileDescriptorProto proto,
        final Description description) {
      description.appendText("\nclass: ").appendValue(proto.getClass().getName())
          .appendText("\npackage:").appendValue(proto.getPackage()).appendText("\nname:")
          .appendValue(proto.getName()).appendText("\nproto:\n")
          .appendText(TextFormat.printToUnicodeString(proto));
    }

    @Factory
    public static Matcher<FileDescriptorProto> comparesToFileProto(final FileDescriptorProto proto) {
      return new IsComparableToFileDescriptorProto(proto);
    }
  }

  @NonNullByDefault(false)
  public static final class FileDescriptorByNameComparator extends Ordering<FileDescriptorProto> {
    private static final FileDescriptorByNameComparator INSTANCE =
        new FileDescriptorByNameComparator();

    private FileDescriptorByNameComparator() {}

    public static FileDescriptorByNameComparator of() {
      return INSTANCE;
    }

    @Override
    public int compare(final FileDescriptorProto o1, final FileDescriptorProto o2) {
      return ComparisonChain.start().compare(o1.getPackage(), o2.getPackage())
          .compare(o1.getName(), o2.getName()).result();
    }
  }

  @NonNullByDefault(false)
  public static final class FileDescriptorProtoComparator extends Ordering<FileDescriptorProto> {
    private static final FileDescriptorProtoComparator INSTANCE =
        new FileDescriptorProtoComparator();

    private FileDescriptorProtoComparator() {}

    public static FileDescriptorProtoComparator of() {
      return INSTANCE;
    }

    @Override
    public int compare(final FileDescriptorProto o1, final FileDescriptorProto o2) {
      return ComparisonChain.start().compare(o1.getPackage(), o2.getPackage())
          .compare(o1.getName(), o2.getName()).compare(o1.toString(), o2.toString()).result();
    }
  }

  // **************** ReplacerComparator START

  private static final Replacer DISREGARD_UNSUPPORTED_PROTO_REPLACER = new BaseReplacer() {
    @Override
    @Nullable
    public Object replaceValue(final FieldDescriptor field, final Object value,
        final List<? extends Entry<FieldDescriptor, ? extends MessageOrBuilder>> path) {
      final String fieldName = field.getFullName();

      if (DESCRIPTOR_DEFAULT_FIELD.equals(fieldName)) {
        return "";
      } else if (RANGE_END_FIELD.equals(fieldName)) {
        final DescriptorProtoOrBuilder messageProto =
            (DescriptorProtoOrBuilder) path.get(1).getValue();

        if (messageProto.getOptionsOrBuilder().getMessageSetWireFormat()) {
          return Math.min((Integer) value, ProtoFileParser.MAX_FIELD_NUMBER + 1);
        }
      }

      return null;
    }
  };

  public static FileDescriptorProto getUnsupportedReplacedWithDefaultsProto(
      final FileDescriptorProto proto) {
    return ReplacerTextComparator
        .getReplacementMessage(proto, DISREGARD_UNSUPPORTED_PROTO_REPLACER);
  }

  public static ReplacerTextComparator<FileDescriptorProto> getDisregardUnsupportedProtoComparator() {
    return ReplacerTextComparator.of(DISREGARD_UNSUPPORTED_PROTO_REPLACER,
        DISREGARD_UNSUPPORTED_PROTO_REPLACER);

  }

  public interface Replacer {
    @Nullable
    <T extends Message> T replaceMessage(@Nullable final FieldDescriptor field, final T message,
        final List<? extends Entry<FieldDescriptor, ? extends MessageOrBuilder>> path);

    @Nullable
    Object replaceValue(final FieldDescriptor field, final Object value,
        final List<? extends Entry<FieldDescriptor, ? extends MessageOrBuilder>> path);

    boolean isEmpty();
  }

  // possibly, asymmetric Comparator!
  public static final class ReplacerTextComparator<T extends Message> implements Comparator<T> {

    public static class BaseReplacer implements Replacer {

      @Override
      @Nullable
      public <T extends Message> T replaceMessage(@Nullable final FieldDescriptor field,
          final T message,
          final List<? extends Entry<FieldDescriptor, ? extends MessageOrBuilder>> path) {
        return null;
      }

      @Override
      @Nullable
      public Object replaceValue(final FieldDescriptor field, final Object value,
          final List<? extends Entry<FieldDescriptor, ? extends MessageOrBuilder>> path) {
        return null;
      }

      @Override
      public boolean isEmpty() {
        return false;
      }
    }

    public static final class CompositeReplacer implements Replacer {
      private static final CompositeReplacer EMPTY = new CompositeReplacer();
      private final List<? extends Replacer> replacers;

      @SuppressWarnings("null")
      private CompositeReplacer() {
        this.replacers = Collections.<Replacer>emptyList();
      }

      @SuppressWarnings("null")
      CompositeReplacer(final Iterable<? extends Replacer> replacers) {
        final List<Replacer> list = new ArrayList<>();

        for (final Replacer replacer : replacers) {
          list.add(Objects.requireNonNull(replacer));
        }

        this.replacers =
            list.isEmpty() ? Collections.<Replacer>emptyList() : Collections.unmodifiableList(list);
      }

      public static CompositeReplacer empty() {
        return EMPTY;
      }

      public static CompositeReplacer of(final Iterable<? extends Replacer> replacers) {
        return new CompositeReplacer(replacers);
      }

      @SuppressWarnings("null")
      public static CompositeReplacer of(final Replacer... replacers) {
        return new CompositeReplacer(Arrays.asList(Objects.requireNonNull(replacers)));
      }

      @Override
      public boolean isEmpty() {
        return replacers.isEmpty();
      }

      public List<? extends Replacer> getReplacers() {
        return replacers;
      }

      @Override
      @Nullable
      public <T extends Message> T replaceMessage(@Nullable final FieldDescriptor field,
          final T message,
          final List<? extends Entry<FieldDescriptor, ? extends MessageOrBuilder>> path) {
        if (replacers.isEmpty()) {
          return null;
        }

        for (final Replacer replacer : replacers) {
          final T result = replacer.replaceMessage(field, message, path);

          if (result != null) {
            return result;
          }
        }

        return null;
      }

      @Override
      @Nullable
      public Object replaceValue(final FieldDescriptor field, final Object value,
          final List<? extends Entry<FieldDescriptor, ? extends MessageOrBuilder>> path) {
        if (replacers.isEmpty()) {
          return null;
        }

        for (final Replacer replacer : replacers) {
          final Object result = replacer.replaceValue(field, value, path);

          if (result != null) {
            return result;
          }
        }

        return null;
      }
    }

    private static final ReplacerTextComparator<Message> IDENTITY_COMPARATOR =
        new ReplacerTextComparator<Message>();
    private final Replacer replacer1;
    private final Replacer replacer2;

    private ReplacerTextComparator() {
      this.replacer1 = CompositeReplacer.empty();
      this.replacer2 = CompositeReplacer.empty();
    }

    @SuppressWarnings("null")
    ReplacerTextComparator(final Replacer replacer1, final Replacer replacer2) {
      this.replacer1 = Objects.requireNonNull(replacer1);
      this.replacer2 = Objects.requireNonNull(replacer2);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Message> ReplacerTextComparator<T> identity() {
      return (ReplacerTextComparator<T>) IDENTITY_COMPARATOR;
    }

    public static <T extends Message> ReplacerTextComparator<T> of(final Replacer replacer1,
        final Replacer replacer2) {
      return new ReplacerTextComparator<T>(replacer1, replacer2);
    }

    public static <T extends Message> T getReplacementMessage(final T message,
        final Replacer replacer) {
      return ReplacerTextComparator.<T>identity().buildReplacementMessage(message, replacer);
    }

    @NonNullByDefault(false)
    @Override
    public int compare(final T o1, final T o2) {
      if (o1 == null) {
        if (o2 == null) {
          return 0;
        } else {
          return -1;
        }
      } else if (o2 == null) {
        return 1;
      } else {
        final String filteredProto1 = buildReplacementMessage(o1, replacer1).toString();
        final String filteredProto2 = buildReplacementMessage(o2, replacer2).toString();
        return filteredProto1.compareTo(filteredProto2);
      }
    }

    @SuppressWarnings({"unchecked", "hiding"})
    private <T extends Message> T buildReplacementMessage(final T original, final Replacer replacer) {
      Objects.requireNonNull(original);

      if (Objects.requireNonNull(replacer).isEmpty()) {
        return original;
      }

      final LinkedList<Entry<FieldDescriptor, MessageOrBuilder>> path = new LinkedList<>();
      // path.addFirst(new SimpleImmutableEntry<FieldDescriptor, Message>(null, original));
      return (T) getMessage(null, original, replacer, path);
    }

    @SuppressWarnings("null")
    private Message getMessage(@Nullable final FieldDescriptor field, final Message message,
        final Replacer replacer, final LinkedList<Entry<FieldDescriptor, MessageOrBuilder>> path) {
      final Message result = replacer.replaceMessage(field, message, path);

      if (result != null) {
        return result;
      }

      path.addFirst(new SimpleImmutableEntry<FieldDescriptor, MessageOrBuilder>(field, message));
      final Message.Builder builder = message.newBuilderForType();

      for (final Entry<FieldDescriptor, Object> entry : message.getAllFields().entrySet()) {
        addField(builder, entry.getKey(), entry.getValue(), replacer, path);
      }

      path.removeFirst();
      return builder.buildPartial();
    }

    @SuppressWarnings({"null", "unchecked"})
    private void addField(final Message.Builder builder, final FieldDescriptor field,
        final Object value, final Replacer replacer,
        final LinkedList<Entry<FieldDescriptor, MessageOrBuilder>> path) {

      if (field.getJavaType() == JavaType.MESSAGE) {
        if (field.isRepeated()) {
          for (final Message message : (List<Message>) value) {
            builder.addRepeatedField(field, getMessage(field, message, replacer, path));
          }
        } else {
          builder.setField(field, getMessage(field, (Message) value, replacer, path));
        }
      } else {
        builder.setField(field, getValue(field, value, replacer, path));
      }
    }

    private Object getValue(final FieldDescriptor field, final Object value,
        final Replacer replacer, final LinkedList<Entry<FieldDescriptor, MessageOrBuilder>> path) {
      final Object result = replacer.replaceValue(field, value, path);
      return result == null ? value : result;
    }
  }

  // **************** ReplacerComparator END
}
