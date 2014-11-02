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

import static com.github.protobufel.grammar.ExtensionRegistries.buildFullRegistryOf;
import static com.github.protobufel.grammar.PrimitiveTypesUtil.unescapeBytes;
import static com.github.protobufel.grammar.PrimitiveTypesUtil.unescapeDoubleQuotesAndBackslashes;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.github.protobufel.grammar.PrimitiveTypesUtil.InvalidEscapeSequenceException;
import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumValueDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.DescriptorProtos.MethodDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import com.google.protobuf.DescriptorProtos.UninterpretedOption;
import com.google.protobuf.DescriptorProtos.UninterpretedOption.NamePart;
import com.google.protobuf.DescriptorProtos.UninterpretedOptionOrBuilder;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.ExtensionRegistry.ExtensionInfo;
import com.google.protobuf.GeneratedMessage.ExtendableBuilder;
import com.google.protobuf.GeneratedMessage.ExtendableMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.TextFormat;
import com.google.protobuf.TextFormat.ParseException;

/**
 * {@link UninterpretedOption}s resolution, and custom Options conversion to extensions or unknown 
 * fields.
 * 
 * @author protobufel@gmail.com David Tesler
 */
final class OptionResolver {
  private OptionResolver() {}

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private static final int UNINTERPRETED_OPTION_FIELD_NUMBER =
        FileOptions.UNINTERPRETED_OPTION_FIELD_NUMBER;
    private ExtensionRegistry registry = null;
    private boolean customOptionsAsExtensions = false;
    private FileDescriptor file;

    public boolean isCustomOptionsAsExtensions() {
      return customOptionsAsExtensions;
    }

    public Builder setCustomOptionsAsExtensions(final boolean customOptionsAsExtensions) {
      this.customOptionsAsExtensions = customOptionsAsExtensions;
      return this;
    }

    public FileDescriptorProto.Builder resolveAllOptionsFor(final FileDescriptor file) {
      Objects.requireNonNull(file);
      return resolveAllOptionsFor(file, file.toProto().toBuilder());
    }

    public FileDescriptorProto.Builder resolveAllOptionsFor(final FileDescriptor file,
        final FileDescriptorProto.Builder proto) {
      this.file = Objects.requireNonNull(file);
      buildAllOptions(Objects.requireNonNull(proto));
      return proto;
    }

    private ExtensionRegistry ensureRegistry() {
      if (registry == null) {
        registry = buildFullRegistryOf(file);
      }

      return registry;
    }

    private void buildAllOptions(final FileDescriptorProto.Builder proto) {
      if (!buildOptions(proto.getOptionsBuilder())) {
        proto.clearOptions();
      }

      for (final FieldDescriptorProto.Builder extensionProto : proto.getExtensionBuilderList()) {
        if (!buildOptions(extensionProto.getOptionsBuilder())) {
          extensionProto.clearOptions();
        }
      }

      for (final EnumDescriptorProto.Builder enumProto : proto.getEnumTypeBuilderList()) {
        buildAllOptions(enumProto);
      }

      for (final ServiceDescriptorProto.Builder serviceProto : proto.getServiceBuilderList()) {
        buildAllOptions(serviceProto);
      }

      for (final DescriptorProto.Builder messageProto : proto.getMessageTypeBuilderList()) {
        buildAllOptions(messageProto);
      }
    }

    private void buildAllOptions(final DescriptorProto.Builder proto) {
      if (!buildOptions(proto.getOptionsBuilder())) {
        proto.clearOptions();
      }

      for (final FieldDescriptorProto.Builder fieldProto : proto.getFieldBuilderList()) {
        if (!buildOptions(fieldProto.getOptionsBuilder())) {
          fieldProto.clearOptions();
        }
      }

      for (final FieldDescriptorProto.Builder extensionProto : proto.getExtensionBuilderList()) {
        if (!buildOptions(extensionProto.getOptionsBuilder())) {
          extensionProto.clearOptions();
        }
      }

      for (final EnumDescriptorProto.Builder enumProto : proto.getEnumTypeBuilderList()) {
        buildAllOptions(enumProto);
      }

      for (final DescriptorProto.Builder messageProto : proto.getNestedTypeBuilderList()) {
        buildAllOptions(messageProto);
      }
    }

    private void buildAllOptions(final ServiceDescriptorProto.Builder proto) {
      if (!buildOptions(proto.getOptionsBuilder())) {
        proto.clearOptions();
      }

      for (final MethodDescriptorProto.Builder methodProto : proto.getMethodBuilderList()) {
        if (!buildOptions(methodProto.getOptionsBuilder())) {
          methodProto.clearOptions();
        }
      }
    }

    private void buildAllOptions(final EnumDescriptorProto.Builder proto) {
      if (!buildOptions(proto.getOptionsBuilder())) {
        proto.clearOptions();
      }

      for (final EnumValueDescriptorProto.Builder enumValueProto : proto.getValueBuilderList()) {
        if (!buildOptions(enumValueProto.getOptionsBuilder())) {
          enumValueProto.clearOptions();
        }
      }
    }

    private <MType extends ExtendableMessage<MType>, BType extends ExtendableBuilder<MType, BType>> boolean buildOptions(
        final BType optionsBuilder) {
      final FieldDescriptor uninterpretedField =
          getFD(optionsBuilder, UNINTERPRETED_OPTION_FIELD_NUMBER);
      final boolean isUninterpretedEmpty =
          optionsBuilder.getRepeatedFieldCount(uninterpretedField) == 0;
      final boolean anyUninterpetedOptionResolved =
          !isUninterpretedEmpty && resolveUninterpretedOptions(optionsBuilder, uninterpretedField);
      final boolean isUnknownFieldsEmpty = optionsBuilder.getUnknownFields().asMap().isEmpty();
      Map<FieldDescriptor, Object> allFields = null;

      if (isUninterpretedEmpty && isUnknownFieldsEmpty) {
        allFields = optionsBuilder.getAllFields();

        if (allFields.isEmpty()) {
          return false;
        }
      }

      if (customOptionsAsExtensions) {
        if (!isUnknownFieldsEmpty) {
          reparseBuilder(optionsBuilder, ensureRegistry());
        }
      } else {
        boolean anyExtension = anyUninterpetedOptionResolved;

        if (!anyUninterpetedOptionResolved) {
          if (allFields == null) {
            allFields = optionsBuilder.getAllFields();
          }

          for (final FieldDescriptor fd : allFields.keySet()) {
            if (fd.isExtension()) {
              anyExtension = true;
              break;
            }
          }
        }

        if (anyExtension) {
          reparseBuilder(optionsBuilder, ExtensionRegistry.getEmptyRegistry());
        }
      }

      return true;
    }

    private void reparseBuilder(final Message.Builder builder, final ExtensionRegistry registry) {
      final ByteString byteString = builder.build().toByteString();

      try {
        builder.clear().mergeFrom(byteString, registry);
      } catch (final InvalidProtocolBufferException e) {
        throw new RuntimeException(e);
      }
    }

    private <MType extends ExtendableMessage<MType>, BType extends ExtendableBuilder<MType, BType>> boolean resolveUninterpretedOptions(
        final BType optionsBuilder, final FieldDescriptor uninterpretedField) {
      ensureRegistry();
      boolean anyUninterpetedOptionResolved = false;
      @SuppressWarnings("unchecked")
      final List<UninterpretedOption> options =
          new ArrayList<UninterpretedOption>(
              (List<UninterpretedOption>) optionsBuilder.getField(uninterpretedField));

      for (final Iterator<UninterpretedOption> iterator = options.iterator(); iterator.hasNext();) {
        final UninterpretedOption option = iterator.next();

        if (resolveUninterpretedOption(option, optionsBuilder)) {
          anyUninterpetedOptionResolved = true;
          iterator.remove();
        }
      }

      if (options.isEmpty()) {
        optionsBuilder.clearField(uninterpretedField);
      } else {
        optionsBuilder.setField(uninterpretedField, options);
      }

      return anyUninterpetedOptionResolved;
    }

    private <MType extends ExtendableMessage<MType>, BType extends ExtendableBuilder<MType, BType>> boolean resolveUninterpretedOption(
        final UninterpretedOptionOrBuilder option, final BType optionsBuilder) {
      if (option.getNameCount() == 0) {
        throw new IllegalArgumentException("custom option cannot be empty");
      }

      if (!option.getName(0).getIsExtension()) {
        throw new IllegalArgumentException("custom option name should start with '('");
      }

      final Map.Entry<FieldDescriptor, Object> fieldValue;

      if (option.getNameCount() == 1) {
        fieldValue = processUninterpretedOptionValue(option, optionsBuilder);
      } else {
        fieldValue = processUninterpretedOptionMessage(option, optionsBuilder);
      }

      if (fieldValue == null) {
        return false;
      }

      final FieldDescriptor field = fieldValue.getKey();
      final Object value = fieldValue.getValue();

      if (field == null || value == null) {
        // the UninterpretedOption is not resolved
        return false;
      }

      if (field.isRepeated()) {
        optionsBuilder.addRepeatedField(field, value);
      } else {
        optionsBuilder.setField(field, value);
      }

      return true;
    }

    private <MType extends ExtendableMessage<MType>, BType extends ExtendableBuilder<MType, BType>> Map.Entry<FieldDescriptor, Object> processUninterpretedOptionMessage(
        final UninterpretedOptionOrBuilder option, final BType optionsBuilder) {
      Message.Builder valueBuilder = optionsBuilder;
      final Deque<Map.Entry<FieldDescriptor, Message.Builder>> builders =
          new ArrayDeque<Map.Entry<FieldDescriptor, Message.Builder>>(option.getNameCount() - 2);
      boolean alreadyExists = true; // optimization: don't search for existing field if false
      int i = -1;

      for (final NamePart namePart : option.getNameList().subList(0, option.getNameCount() - 1)) {
        i++;
        FieldDescriptor field;

        if (namePart.getIsExtension()) {
          final String containingTypeName =
              i == 0 ? "" : valueBuilder.getDescriptorForType().getFullName();
          final ExtensionInfo info =
              findExtensionByName(containingTypeName, namePart.getNamePart());

          if (info == null) {
            return null;
          }

          field = info.descriptor;

          if (field.getJavaType() != JavaType.MESSAGE) {
            return null;
          }

          if (field.isRepeated()) {
            alreadyExists = false;
          } else if (alreadyExists) {
            final Message msg = (Message) valueBuilder.getField(field);

            if (msg != null) {
              alreadyExists = true;
              valueBuilder = msg.toBuilder();
            } else {
              alreadyExists = false;
            }
          }

          if (!alreadyExists) {
            valueBuilder = info.defaultInstance.newBuilderForType();
          }

          builders.addFirst(new SimpleImmutableEntry<FieldDescriptor, Message.Builder>(field,
              valueBuilder));
        } else {
          for (final String part : namePart.getNamePart().split("\\.")) {
            field = getFD(valueBuilder, part);

            if (field == null || field.getJavaType() != JavaType.MESSAGE) {
              return null;
            }

            if (field.isRepeated()) {
              alreadyExists = false;
            } else if (alreadyExists) {
              final Message msg = (Message) valueBuilder.getField(field);

              if (msg != null) {
                alreadyExists = true;
                valueBuilder = msg.toBuilder();
              } else {
                alreadyExists = false;
              }
            }

            if (!alreadyExists) {
              valueBuilder = valueBuilder.newBuilderForField(field);
            }

            builders.addFirst(new SimpleImmutableEntry<FieldDescriptor, Message.Builder>(field,
                valueBuilder));
          }
        }
      }

      final Map.Entry<FieldDescriptor, Object> fieldValue =
          processUninterpretedOptionValue(option, valueBuilder);

      if (fieldValue == null) {
        return null;
      }

      FieldDescriptor field = fieldValue.getKey();
      Object value = fieldValue.getValue();
      Entry<FieldDescriptor, Message.Builder> fieldBuilder = null;

      while ((fieldBuilder = builders.pollFirst()) != null) {
        valueBuilder = fieldBuilder.getValue();

        if (field.isRepeated()) {
          valueBuilder.addRepeatedField(field, value);
        } else {
          valueBuilder.setField(field, value);
        }

        value = valueBuilder.build();
        field = fieldBuilder.getKey();
      }

      return new SimpleImmutableEntry<FieldDescriptor, Object>(field, value);
    }

    private Map.Entry<FieldDescriptor, Object> processUninterpretedOptionValue(
        final UninterpretedOptionOrBuilder option, final Message.Builder valueBuilder) {
      final NamePart namePart = option.getName(option.getNameCount() - 1);
      FieldDescriptor field;
      Message.Builder subBuilder = null;

      if (namePart.getIsExtension()) {
        final String containingTypeName =
            option.getNameCount() == 1 ? "" : valueBuilder.getDescriptorForType().getFullName();
        final ExtensionInfo info = findExtensionByName(containingTypeName, namePart.getNamePart());

        if (info == null) {
          return null;
        }

        field = info.descriptor;

        if (field.getJavaType() == JavaType.MESSAGE) {
          subBuilder = info.defaultInstance.newBuilderForType();
        }
      } else {
        field = getFD(valueBuilder, namePart.getNamePart());

        if (field.getJavaType() == JavaType.MESSAGE) {
          subBuilder = valueBuilder.newBuilderForField(field);
        }
      }

      Object value = null;

      if (field.getJavaType() == JavaType.MESSAGE) {
        if (!option.hasAggregateValue()) {
          // TODO: return null, or register valdationError
          throw new RuntimeException(String.format(
              "custom Message option %s has a value of non-message type", valueBuilder
                  .getDescriptorForType().getFullName()));
        }

        try {
          final String aggregateValue = option.getAggregateValue();
          TextFormat.merge(aggregateValue.substring(1, aggregateValue.length() - 1), registry,
              subBuilder);

          // only done to suppress Eclipse null warning; shouldn't happen
          if (subBuilder == null) {
            throw new NullPointerException();
          }

          return new SimpleImmutableEntry<FieldDescriptor, Object>(field, subBuilder.build());
        } catch (final ParseException e) {
          throw new RuntimeException(String.format("custom Message option %s has a parsing error",
              valueBuilder.getDescriptorForType().getFullName()), e);
        }
      }

      switch (field.getJavaType()) {
        case ENUM:
          if (option.hasIdentifierValue()) {
            value = field.getEnumType().findValueByName(option.getIdentifierValue());
          } else if (option.hasPositiveIntValue()) {
            value = field.getEnumType().findValueByNumber((int) option.getPositiveIntValue());
          } else if (option.hasNegativeIntValue()) {
            value = field.getEnumType().findValueByNumber((int) option.getNegativeIntValue());
          }

          break;
        case BOOLEAN:
          if (option.hasIdentifierValue()) {
            value = Boolean.valueOf(option.getIdentifierValue());
          } else if (option.hasStringValue()) {
            value = Boolean.valueOf(option.getStringValue().toStringUtf8());
          }

          break;
        case STRING:
          if (option.hasStringValue()) {
            value = unescapeDoubleQuotesAndBackslashes(option.getStringValue().toStringUtf8());
          }

          break;
        case BYTE_STRING:
          if (option.hasStringValue()) {
            try {
              value = unescapeBytes(option.getStringValue().toStringUtf8());
            } catch (final InvalidEscapeSequenceException e) {
              value = null;
            }
          }

          break;
        case DOUBLE:
          if (option.hasDoubleValue()) {
            value = option.getDoubleValue();
          } else if (option.hasPositiveIntValue()) {
            value = Double.valueOf(option.getPositiveIntValue());
          } else if (option.hasNegativeIntValue()) {
            value = Double.valueOf(option.getNegativeIntValue());
          }

          break;
        case FLOAT:
          if (option.hasDoubleValue()) {
            value = Float.valueOf((float) option.getDoubleValue());
          } else if (option.hasPositiveIntValue()) {
            value = Float.valueOf(option.getPositiveIntValue());
          } else if (option.hasNegativeIntValue()) {
            value = Float.valueOf(option.getNegativeIntValue());
          }

          break;
        case LONG:
          if (option.hasPositiveIntValue()) {
            value = option.getPositiveIntValue();
          } else if (option.hasNegativeIntValue()) {
            value = option.getNegativeIntValue();
          }

          break;
        case INT:
          if (option.hasPositiveIntValue()) {
            value = Integer.valueOf((int) option.getPositiveIntValue());
          } else if (option.hasNegativeIntValue()) {
            value = Integer.valueOf((int) option.getNegativeIntValue());
          }

          break;
        default:
          break;
      }

      // TODO: exception vs returning null vs registering validationError?
      if (value == null) {
        throw new RuntimeException(String.format("custom option %s has a value of a wrong type",
            valueBuilder.getDescriptorForType().getFullName()));
      }

      return new SimpleImmutableEntry<FieldDescriptor, Object>(field, value);
    }

    private ExtensionInfo findExtensionByName(final String containingTypeName, final String name) {
      if (name.startsWith(".")) {
        return registry.findExtensionByName(name.substring(1));
      }

      ExtensionInfo info;

      // try first inner scoped
      if (!containingTypeName.isEmpty()) {
        info = registry.findExtensionByName(containingTypeName + '.' + name);

        if (info != null) {
          return info;
        }
      }

      // then outer scoped for empty package
      if (file.getPackage().isEmpty()) {
        return registry.findExtensionByName(name);
      }

      // then outer scoped if starts with package
      if (name.startsWith(file.getPackage() + '.')) {
        info = registry.findExtensionByName(name);

        if (info != null) {
          return info;
        }

        // then if contained in a Message with package name?
        return registry.findExtensionByName(file.getPackage() + '.' + name);
      }

      // then try the package prepending the name
      info = registry.findExtensionByName(file.getPackage() + '.' + name);

      if (info != null) {
        return info;
      }

      // then try for outer file extension
      return registry.findExtensionByName(name);
    }

    // TODO replace with MessageAdapter helper method
    private FieldDescriptor getFD(final MessageOrBuilder message, final int number) {
      return message.getDescriptorForType().findFieldByNumber(number);
    }

    // TODO replace with MessageAdapter helper method
    private FieldDescriptor getFD(final MessageOrBuilder message, final String name) {
      return message.getDescriptorForType().findFieldByName(name);
    }
  }
}
