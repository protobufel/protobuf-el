// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc. All rights reserved.
// http://code.google.com/p/protobuf/
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
// * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
// * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
// * Neither the name of Google Inc. nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package com.github.protobufel.grammar;

import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.github.protobufel.grammar.AbstractMessageUtils.FieldFilter.Result;
import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.DescriptorProto.ExtensionRange;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumOptions;
import com.google.protobuf.DescriptorProtos.EnumValueDescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumValueOptions;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.DescriptorProtos.MessageOptions;
import com.google.protobuf.DescriptorProtos.MethodDescriptorProto;
import com.google.protobuf.DescriptorProtos.MethodOptions;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceOptions;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Message;
import com.google.protobuf.UnknownFieldSet;
import com.google.protobuf.UnknownFieldSet.Field;

/**
 * Utilities for filtered Message equality. Large portions taken with minimal modifications from
 * {@link com.google.protobuf.AbstractMessage}.
 *
 * @author protobufel@gmail.com David Tesler
 */
@NonNullByDefault
final class AbstractMessageUtils {
  private AbstractMessageUtils() {}

  /**
   * Compares the filtered field's values conforming the equals contract, or returns NOT_FILTERED.
   * The regular comparison is assumed in case of NOT_FILTERED, to be done by {@link compareField}.
   */
  public interface FieldFilter {
    public static enum Result {
      EQUAL, NOT_EQUAL, NOT_FILTERED
    }

    /**
     * Compares the filtered field's values conforming the equals contract, or returns NOT_FILTERED.
     * The regular comparison is assumed in case of NOT_FILTERED, to be done by {@link compareField}
     * .
     */
    Result isEqual(FieldDescriptor field, Object value1, Object value2);

    Result isEqual(Descriptor type, UnknownFieldSet value1, UnknownFieldSet value2);

  }

  private static final FieldFilter IDENTITY_FIELD_FILTER = new FieldFilter() {
    @Override
    public Result isEqual(final FieldDescriptor field, final Object value1, final Object value2) {
      return Result.NOT_FILTERED;
    }

    @Override
    public Result isEqual(final Descriptor type, final UnknownFieldSet value1,
        final UnknownFieldSet value2) {
      return Result.NOT_FILTERED;
    }
  };

  /**
   * Returns the identity filter, which always returns NOT_FILTERED.
   */
  public static FieldFilter identityFilter() {
    return IDENTITY_FIELD_FILTER;
  }

  public static final class ProtoFilter implements FieldFilter {
    @SuppressWarnings("null")
    private static final FieldDescriptor RANGE_END_FIELD = ExtensionRange.getDescriptor()
    .findFieldByNumber(ExtensionRange.END_FIELD_NUMBER);
    @SuppressWarnings("null")
    private static final String DESCRIPTOR_DEFAULT_FIELD = FieldDescriptorProto.getDescriptor()
    .findFieldByNumber(FieldDescriptorProto.DEFAULT_VALUE_FIELD_NUMBER).getFullName();
    private static final Set<Descriptor> ALL_OPTION_DESCRIPTORS = new HashSet<>(Arrays.asList(
        FileOptions.getDescriptor(), MessageOptions.getDescriptor(), EnumOptions.getDescriptor(),
        FieldOptions.getDescriptor(), EnumValueOptions.getDescriptor(),
        ServiceOptions.getDescriptor(), MethodOptions.getDescriptor()));
    private final Set<String> skipFields;

    public ProtoFilter() {
      skipFields = new HashSet<String>(Arrays.asList(DESCRIPTOR_DEFAULT_FIELD));
    }

    public ProtoFilter(final ProtoFilter other) {
      skipFields = new HashSet<String>(other.skipFields);
    }

    protected ProtoFilter addSkipField(final String skipField) {
      skipFields.add(skipField);
      return this;
    }

    protected ProtoFilter addSkipFields(final String... skipFields) {
      if (skipFields.length > 0) {
        for (final String skipField : skipFields) {
          this.skipFields.add(skipField);
        }
      }

      return this;
    }

    @SuppressWarnings("null")
    @Override
    public Result isEqual(final FieldDescriptor field, final Object value1, final Object value2) {
      if (skipFields.contains(field.getFullName())) {
        return Result.EQUAL;
      }

      if (field == RANGE_END_FIELD) {
        if (Objects.equals(min(ProtoFileParser.MAX_FIELD_NUMBER + 1, (Integer) value1),
            min(ProtoFileParser.MAX_FIELD_NUMBER + 1, (Integer) value2))) {
          return Result.EQUAL;
        } else {
          return Result.NOT_EQUAL;
        }
      }

      if (field.getJavaType() != JavaType.MESSAGE) {
        return Result.NOT_FILTERED;
      }

      if (field.getMessageType() == FieldDescriptorProto.getDescriptor()) {
        if (field.isRepeated()) {
          @SuppressWarnings("unchecked")
          final List<Message> list1 = (List<Message>) value1;
          final List<?> list2 = (List<?>) value1;

          if (list1.size() != list2.size()) {
            return Result.NOT_EQUAL;
          }

          for (int i = 0; i < list1.size(); i++) {
            if (!isSingleFieldProtoEqual(list1.get(i), list2.get(i))) {
              return Result.NOT_EQUAL;
            }
          }
        } else if (!isSingleFieldProtoEqual(value1, value2)) {
          return Result.NOT_EQUAL;
        }

        return Result.EQUAL;
      }

      return Result.NOT_FILTERED;
    }

    private boolean isSingleFieldProtoEqual(final Object value1, @Nullable final Object value2) {
      final FieldDescriptorProto fieldProto1 = (FieldDescriptorProto) value1;
      final FieldDescriptorProto fieldProto2 = (FieldDescriptorProto) value2;

      final ProtoFilter filter = new ProtoFilter();

      switch (fieldProto1.getType()) {
        default:
          break;
      }

      return compareMessage(fieldProto1, fieldProto2, filter);
    }

    @SuppressWarnings("null")
    @Override
    public Result isEqual(final Descriptor type, final UnknownFieldSet value1,
        final UnknownFieldSet value2) {
      if (value1 == value2) {
        return Result.EQUAL;
      }

      if (!ALL_OPTION_DESCRIPTORS.contains(type)) {
        return Result.NOT_FILTERED;
      }

      final Map<Integer, Field> map1 = value1.asMap();
      final Map<Integer, Field> map2 = value2.asMap();

      if (map1.size() != map2.size()) {
        return Result.NOT_EQUAL;
      }

      for (final Entry<Integer, Field> entry : map1.entrySet()) {
        final Field field1 = entry.getValue();
        final Field field2 = map2.get(entry.getKey());

        if (field2 == null
            || !Objects.equals(getUnknownFieldValue(field1), getUnknownFieldValue(field2))) {
          return Result.NOT_EQUAL;
        }
      }

      return Result.EQUAL;
    }

    private @Nullable Object getUnknownFieldValue(final Field field) {
      if (!field.getFixed32List().isEmpty()) {
        return field.getFixed32List().get(field.getFixed32List().size() - 1);
      }

      if (!field.getFixed64List().isEmpty()) {
        return field.getFixed64List().get(field.getFixed64List().size() - 1);
      }

      if (!field.getGroupList().isEmpty()) {
        return field.getGroupList().get(field.getGroupList().size() - 1);
      }

      if (!field.getLengthDelimitedList().isEmpty()) {
        return field.getLengthDelimitedList().get(field.getLengthDelimitedList().size() - 1);
      }

      if (!field.getVarintList().isEmpty()) {
        return field.getVarintList().get(field.getVarintList().size() - 1);
      }

      return null;
    }
  }

  public static FilteredProtoTextComparator filteredUnsupportedProtoComparator() {
    final Iterable<String> skipRegexes =
        Arrays.asList(
            // skip default values
            Pattern.quote(FieldDescriptorProto.getDescriptor()
                .findFieldByNumber(FieldDescriptorProto.DEFAULT_VALUE_FIELD_NUMBER).getFullName()),
                // skip extension range max
                Pattern.quote(DescriptorProto.ExtensionRange.getDescriptor()
                    .findFieldByNumber(DescriptorProto.ExtensionRange.END_FIELD_NUMBER).getFullName()));
    return FilteredProtoTextComparator.filteredByRegexes(skipRegexes);
  }

  public static final class FilteredProtoTextComparator implements Comparator<FileDescriptorProto> {
    private static final FilteredProtoTextComparator IDENTITY_COMPARATOR =
        new FilteredProtoTextComparator();
    private final List<Pattern> skipPatterns;

    @SuppressWarnings("null")
    private FilteredProtoTextComparator() {
      skipPatterns = Collections.emptyList();
    }

    @SuppressWarnings("null")
    FilteredProtoTextComparator(final Iterable<Pattern> skipPatterns) {
      final List<Pattern> list = new ArrayList<>();

      for (final Pattern pattern : skipPatterns) {
        list.add(Objects.requireNonNull(pattern));
      }

      this.skipPatterns = Collections.unmodifiableList(list);
    }

    public static FilteredProtoTextComparator indentity() {
      return IDENTITY_COMPARATOR;
    }

    public static FilteredProtoTextComparator filteredByRegexes(final Iterable<String> skipRegexes) {
      return new FilteredProtoTextComparator(getSkipPatterns(skipRegexes));
    }

    public static FilteredProtoTextComparator filteredByPatterns(
        final Iterable<Pattern> skipPatterns) {
      return new FilteredProtoTextComparator(skipPatterns);
    }

    @NonNullByDefault(false)
    @Override
    public int compare(final FileDescriptorProto o1, final FileDescriptorProto o2) {
      if (o1 == o2) {
        return 0;
      } else if (o1 == null) {
        return -1;
      } else if (o2 == null) {
        return 1;
      } else {
        final String filteredProto1 = buildFilteredProtoByPatterns(o1, skipPatterns).toString();
        final String filteredProto2 = buildFilteredProtoByPatterns(o2, skipPatterns).toString();
        return filteredProto1.compareTo(filteredProto2);
      }
    }

    private FileDescriptorProto buildFilteredProtoByPatterns(final FileDescriptorProto original,
        final Collection<Pattern> skipPatterns) {
      Objects.requireNonNull(original);

      if (Objects.requireNonNull(skipPatterns).isEmpty()) {
        return original;
      }

      return getMessage(original, skipPatterns);
    }

    private static List<Pattern> getSkipPatterns(final Iterable<String> skipRegexes) {
      final List<Pattern> skipPatterns = new ArrayList<Pattern>();

      for (final String regex : Objects.requireNonNull(skipRegexes)) {
        skipPatterns.add(Pattern.compile(Objects.requireNonNull(regex)));
      }
      return skipPatterns;
    }

    @SuppressWarnings({"null", "unchecked"})
    private boolean addField(final Message.Builder protoBuilder, final FieldDescriptor field,
        final Object value, final Iterable<Pattern> skipPatterns) {
      if (isSkip(field.getFullName(), skipPatterns)) {
        return false;
      }

      if (field.getJavaType() == JavaType.MESSAGE) {
        if (field.isRepeated()) {
          for (final Message message : (List<Message>) value) {
            protoBuilder.addRepeatedField(field, getMessage(message, skipPatterns));
          }
        } else {
          protoBuilder.setField(field, getMessage((Message) value, skipPatterns));
        }
      } else {
        protoBuilder.setField(field, value);
      }

      return true;
    }

    @SuppressWarnings({"null", "unchecked"})
    private <MType extends Message> MType getMessage(final MType message,
        final Iterable<Pattern> skipPatterns) {
      final Message.Builder builder = message.toBuilder();

      for (final Entry<FieldDescriptor, Object> entry : message.getAllFields().entrySet()) {
        addField(builder, entry.getKey(), entry.getValue(), skipPatterns);
      }

      return (MType) builder.buildPartial();
    }

    private boolean isSkip(final String name, final Iterable<Pattern> skipPatterns) {
      for (final Pattern pattern : skipPatterns) {
        if (pattern.matcher(name).matches()) {
          return true;
        }
      }

      return false;
    }
  }

  // FIXME remove when no longer needed!
  public static final class FileProtoEqualComparator implements Comparator<FileDescriptorProto> {
    private static final FileProtoEqualComparator INSTANCE = new FileProtoEqualComparator();

    private FileProtoEqualComparator() {}

    public static FileProtoEqualComparator of() {
      return INSTANCE;
    }

    @NonNullByDefault(false)
    @Override
    public int compare(final FileDescriptorProto o1, final FileDescriptorProto o2) {
      return o1 == o2 ? 0 : o1 == null ? -1 : compareProto(o1, o2) ? 0 : -1;
    }
  }

  public static boolean compareProto(final FileDescriptorProto me, final @Nullable Object other) {
    return compareMessage(me, other, new ProtoFilter());
  }

  public static boolean compareProto(final DescriptorProto me, final @Nullable Object other) {
    return compareMessage(me, other, new ProtoFilter());
  }

  public static boolean compareProto(final EnumDescriptorProto me, final @Nullable Object other) {
    return compareMessage(me, other, new ProtoFilter());
  }

  public static boolean compareProto(final EnumValueDescriptorProto me, final @Nullable Object other) {
    return compareMessage(me, other, new ProtoFilter());
  }

  public static boolean compareProto(final FieldDescriptorProto me, final @Nullable Object other) {
    return compareMessage(me, other, new ProtoFilter());
  }

  public static boolean compareProto(final ServiceDescriptorProto me, final @Nullable Object other) {
    return compareMessage(me, other, new ProtoFilter());
  }

  public static boolean compareProto(final MethodDescriptorProto me, final @Nullable Object other) {
    return compareMessage(me, other, new ProtoFilter());
  }

  /**
   * A helper method for Message equality. Compares two messages using the filter. Filtered fields
   * will be compared directly by the filter, otherwise regularly. If field is of Message type, then
   * it will be compared by this method recursively.
   *
   * @param me "this" Message
   * @param other a Message to compare with
   * @param filter a {@link FieldFilter} to compare fields with
   * @return true iif messages are equal, according to equals contract
   */
  @SuppressWarnings("null")
  public static boolean compareMessage(final Message me, final @Nullable Object other,
      final FieldFilter filter) {
    if (me == other) {
      return true;
    }

    if (!(other instanceof Message)) {
      return false;
    }

    final Message notMe = (Message) other;

    if (me.getDescriptorForType() != notMe.getDescriptorForType()) {
      return false;
    }

    final Result filterResult =
        filter.isEqual(me.getDescriptorForType(), me.getUnknownFields(), notMe.getUnknownFields());

    switch (filterResult) {
      case EQUAL:
        break;
      case NOT_EQUAL:
        return false;
      default:
        if (!me.getUnknownFields().equals(notMe.getUnknownFields())) {
          return false;
        }
    }

    final Map<FieldDescriptor, Object> myFields = me.getAllFields();
    final Map<FieldDescriptor, Object> otherFields = notMe.getAllFields();

    if (myFields.size() != otherFields.size()) {
      return false;
    }

    for (final Entry<FieldDescriptor, Object> myEntry : myFields.entrySet()) {
      final FieldDescriptor field = myEntry.getKey();
      final Object otherValue = otherFields.get(field);

      if (otherValue == null) {
        return false;
      }

      if (!compareField(field, myEntry.getValue(), otherValue, filter)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Compares two field values using the filter. Takes special care of bytes fields because
   * immutable messages and mutable messages use different Java type to reprensent a bytes field and
   * this method should be able to compare immutable messages, mutable messages and also an
   * immutable message to a mutable message.
   *
   * @param compareUnknown
   */
  @SuppressWarnings("null")
  public static boolean compareField(final FieldDescriptor descriptor, final Object value1,
      final Object value2, final FieldFilter filter) {
    final Result filterResult = filter.isEqual(descriptor, value1, value2);

    switch (filterResult) {
      case EQUAL:
        return true;
      case NOT_EQUAL:
        return false;
      default:
        break;
    }

    if (descriptor.getType() == FieldDescriptor.Type.BYTES) {
      if (descriptor.isRepeated()) {
        final List<?> list1 = (List<?>) value1;
        final List<?> list2 = (List<?>) value2;
        if (list1.size() != list2.size()) {
          return false;
        }
        for (int i = 0; i < list1.size(); i++) {
          if (!compareBytes(list1.get(i), list2.get(i))) {
            return false;
          }
        }
      } else {
        // Compares a singular bytes field.
        if (!compareBytes(value1, value2)) {
          return false;
        }
      }
    } else {
      // Compare non-bytes fields.
      if (descriptor.getJavaType() == JavaType.MESSAGE) {
        if (descriptor.isRepeated()) {
          @SuppressWarnings("unchecked")
          final List<Message> list1 = (List<Message>) value1;
          final List<?> list2 = (List<?>) value2;

          if (list1.size() != list2.size()) {
            return false;
          }

          int i = -1;

          for (final Message message1 : list1) {
            if (!compareMessage(message1, list2.get(++i), filter)) {
              return false;
            }
          }
        } else if (!compareMessage((Message) value1, value2, filter)) {
          return false;
        }
      } else if (!value1.equals(value2)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Compares two bytes fields. The parameters must be either a byte array or a ByteString object.
   * They can be of different type though.
   */
  private static boolean compareBytes(final Object a, final Object b) {
    if (a instanceof byte[] && b instanceof byte[]) {
      return Arrays.equals((byte[]) a, (byte[]) b);
    }
    return toByteString(a).equals(toByteString(b));
  }

  @SuppressWarnings("null")
  private static ByteString toByteString(final Object value) {
    if (value instanceof byte[]) {
      return ByteString.copyFrom((byte[]) value);
    } else {
      return (ByteString) value;
    }
  }
}
