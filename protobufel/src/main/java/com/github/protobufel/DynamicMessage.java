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

package com.github.protobufel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.github.protobufel.ProtoInterfaces.BuilderParent;
import com.github.protobufel.ProtoInterfaces.IBuilder2;
import com.github.protobufel.ProtoInterfaces.IFieldBuilder;
import com.github.protobufel.ProtoInterfaces.IFieldHandler;
import com.github.protobufel.ProtoInterfaces.IMessageEx;
import com.google.protobuf.AbstractMessage;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Descriptors.OneofDescriptor;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.Internal;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.LazyField;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.Parser;
import com.google.protobuf.UnknownFieldSet;

/**
 * A DynamicMessage wrapping the original protobuf DynamicMessage and mirroring all relevant
 * GeneratedMessage functionality.
 *
 * @see com.google.protobuf.DynamicMessage
 * @see com.google.protobuf.GeneratedMessage
 *
 * @author protobufel@gmail.com David Tesler
 */
public final class DynamicMessage implements Message, IMessageEx {
  private static final DynamicMesageProvider DYNAMIC_MESSAGE_PROVIDER = new DynamicMesageProvider();

  /**
   * For testing. Allows a test to disable the optimization that avoids using field builders for
   * nested messages until they are requested. By disabling this optimization, existing tests can be
   * reused to test the field builders.
   */
  protected static boolean alwaysUseFieldBuilders = false;
  private final com.google.protobuf.DynamicMessage message;
  private int memoizedHashCode = 0;
  private byte memoizedIsInitialized = -1;

  private DynamicMessage(final com.google.protobuf.DynamicMessage message) {
    this.message = message;
  }

  private static DynamicMessage wrap(final com.google.protobuf.DynamicMessage message) {
    return new DynamicMessage(message);
  }

  public static DynamicMesageProvider getProvider() {
    return DYNAMIC_MESSAGE_PROVIDER;
  }

  @Override
  public DynamicMesageProvider getProviderForType() {
    return DYNAMIC_MESSAGE_PROVIDER;
  }

  /**
   * For testing. Allows a test to disable the optimization that avoids using field builders for
   * nested messages until they are requested. By disabling this optimization, existing tests can be
   * reused to test the field builders. See {@link RepeatedFieldBuilder} and
   * {@link SingleFieldBuilder}.
   */
  static void enableAlwaysUseFieldBuildersForTesting() {
    alwaysUseFieldBuilders = true;
  }
  
  static void disableAlwaysUseFieldBuildersForTesting() {
    alwaysUseFieldBuilders = false;
  }

  /**
   * Get a {@code DynamicMessage} representing the default instance of the given type.
   */
  public static DynamicMessage getDefaultInstance(final Descriptor type) {
    return wrap(com.google.protobuf.DynamicMessage.getDefaultInstance(type));
  }

  /** Parse a message of the given type from the given input stream. */
  public static DynamicMessage parseFrom(final Descriptor type, final CodedInputStream input)
      throws IOException {
    return wrap(com.google.protobuf.DynamicMessage.parseFrom(type, input));
  }

  /** Parse a message of the given type from the given input stream. */
  public static DynamicMessage parseFrom(final Descriptor type, final CodedInputStream input,
      final ExtensionRegistry extensionRegistry) throws IOException {
    return wrap(com.google.protobuf.DynamicMessage.parseFrom(type, input, extensionRegistry));
  }

  /** Parse {@code data} as a message of the given type and return it. */
  public static DynamicMessage parseFrom(final Descriptor type, final ByteString data)
      throws InvalidProtocolBufferException {
    return wrap(com.google.protobuf.DynamicMessage.parseFrom(type, data));
  }

  /** Parse {@code data} as a message of the given type and return it. */
  public static DynamicMessage parseFrom(final Descriptor type, final ByteString data,
      final ExtensionRegistry extensionRegistry) throws InvalidProtocolBufferException {
    return wrap(com.google.protobuf.DynamicMessage.parseFrom(type, data, extensionRegistry));
  }

  /** Parse {@code data} as a message of the given type and return it. */
  public static DynamicMessage parseFrom(final Descriptor type, final byte[] data)
      throws InvalidProtocolBufferException {
    return wrap(com.google.protobuf.DynamicMessage.parseFrom(type, data));
  }

  /** Parse {@code data} as a message of the given type and return it. */
  public static DynamicMessage parseFrom(final Descriptor type, final byte[] data,
      final ExtensionRegistry extensionRegistry) throws InvalidProtocolBufferException {
    return wrap(com.google.protobuf.DynamicMessage.parseFrom(type, data, extensionRegistry));
  }

  /** Parse a message of the given type from {@code input} and return it. */
  public static DynamicMessage parseFrom(final Descriptor type, final InputStream input)
      throws IOException {
    return wrap(com.google.protobuf.DynamicMessage.parseFrom(type, input));
  }

  /** Parse a message of the given type from {@code input} and return it. */
  public static DynamicMessage parseFrom(final Descriptor type, final InputStream input,
      final ExtensionRegistry extensionRegistry) throws IOException {
    return wrap(com.google.protobuf.DynamicMessage.parseFrom(type, input, extensionRegistry));
  }

  /** Construct a {@link Message.Builder} for the given type. */
  public static Builder newBuilder(final Descriptor type) {
    return new Builder(type);
  }

  /**
   * Construct a {@link Message.Builder} for a message of the same type as {@code prototype}, and
   * initialize it with {@code prototype}'s contents.
   */
  public static Builder newBuilder(final Message prototype) {
    return new Builder(prototype.getDescriptorForType()).mergeFrom(prototype);
  }

  Builder newBuilderForType(final Descriptor type, final BuilderParent parent) {
    final Builder builder = new Builder(type, parent);
    return builder;
  }

  static Builder newBuilder(final Descriptor type, final BuilderParent parent) {
    final Builder builder = new Builder(type, parent);
    return builder;
  }

  /**
   * Gets whether there are any fields set, including unknown fields
   */
  public boolean isEmpty() {
    return message.getAllFields().isEmpty() && message.getUnknownFields().asMap().isEmpty();
  }

  // -----------------------------------------------------------------
  // Implementation of Message interface.

  @Override
  public ByteString toByteString() {
    return message.toByteString();
  }

  @Override
  public byte[] toByteArray() {
    return message.toByteArray();
  }

  @Override
  public void writeTo(final OutputStream output) throws IOException {
    message.writeTo(output);
  }

  @Override
  public void writeDelimitedTo(final OutputStream output) throws IOException {
    message.writeDelimitedTo(output);
  }

  @Override
  public List<String> findInitializationErrors() {
    return message.findInitializationErrors();
  }

  @Override
  public String getInitializationErrorString() {
    return message.getInitializationErrorString();
  }

  @Override
  public final String toString() {
    return message.toString();
  }

  @Override
  public boolean equals(final Object other) {
    return message.equals(other);
  }

  @Override
  public Descriptor getDescriptorForType() {
    return message.getDescriptorForType();
  }

  @Override
  public DynamicMessage getDefaultInstanceForType() {
    return wrap(message.getDefaultInstanceForType());
  }

  @Override
  public DynamicMessage getDefaultInstanceForType(final Descriptor descriptor) {
    return getDefaultInstance(descriptor);
  }

  @Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }

    memoizedHashCode = message.hashCode();
    return memoizedHashCode;
  }

  @Override
  public Map<FieldDescriptor, Object> getAllFields() {
    return message.getAllFields();
  }

  @Override
  public boolean hasField(final FieldDescriptor field) {
    return message.hasField(field);
  }

  @Override
  public Object getField(final FieldDescriptor field) {
    return message.getField(field);
  }

  @Override
  public int getRepeatedFieldCount(final FieldDescriptor field) {
    return message.getRepeatedFieldCount(field);
  }

  @Override
  public Object getRepeatedField(final FieldDescriptor field, final int index) {
    return message.getRepeatedField(field, index);
  }

  @Override
  public UnknownFieldSet getUnknownFields() {
    return message.getUnknownFields();
  }

  @Override
  public boolean isInitialized() {
    final byte isInitialized = memoizedIsInitialized;

    if (isInitialized != -1) {
      return isInitialized == 1;
    }

    if (message.isInitialized()) {
      memoizedIsInitialized = 1;
      return true;
    } else {
      memoizedIsInitialized = 0;
      return false;
    }
  }

  @Override
  public void writeTo(final CodedOutputStream output) throws IOException {
    message.writeTo(output);
  }

  @Override
  public int getSerializedSize() {
    return message.getSerializedSize();
  }

  @Override
  public Builder newBuilderForType() {
    return new Builder(message.getDescriptorForType());
  }

  @Override
  public Builder toBuilder() {
    return newBuilderForType().mergeFrom(this);
  }

  @Override
  public FieldDescriptor getOneofFieldDescriptor(final OneofDescriptor oneof) {
    return message.getOneofFieldDescriptor(oneof);
  }

  @Override
  public boolean hasOneof(final OneofDescriptor oneof) {
    return message.hasOneof(oneof);
  }

  @Override
  public Parser<DynamicMessage> getParserForType() {
    return new AbstractParser<DynamicMessage>() {
      @Override
      public DynamicMessage parsePartialFrom(final CodedInputStream input,
          final ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
        final Builder builder = newBuilder(message.getDescriptorForType());
        try {
          builder.mergeFrom(input, extensionRegistry);
        } catch (final InvalidProtocolBufferException e) {
          throw e.setUnfinishedMessage(builder.buildPartial());
        } catch (final IOException e) {
          throw new InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(builder
              .buildPartial());
        }
        return builder.buildPartial();
      }
    };
  }

  // =================================================================

  /**
   * A DynamicMessage.Builder wrapping the original protobuf DynamicMessage.Builder and mirroring
   * all relevant GeneratedMessage.Builder functionality, including hierarchical builders, insertion
   * of repeated fields, etc.
   *
   * @see com.google.protobuf.DynamicMessage.Builder
   * @see com.google.protobuf.GeneratedMessage.Builder
   *
   * @author protobufel@gmail.com David Tesler
   */
  public static final class Builder extends AbstractMessage.Builder<Builder> implements IBuilder2 {
    private final com.google.protobuf.DynamicMessage.Builder builder;
    private BuilderParent builderParent;
    private BuilderParentImpl meAsParent;

    // Indicates that we've built a message and so we are now obligated
    // to dispatch dirty invalidations. See GeneratedMessage.BuilderListener.
    private boolean isClean;

    // SubBuilders
    private final SubBuilders builders;

    /** Construct a {@code Builder} for the given type. */
    private Builder(final Descriptor type) {
      this(type, null);
    }

    /** Construct a {@code Builder} for the given type. */
    private Builder(final Descriptor type, final BuilderParent builderParent) {
      builder = com.google.protobuf.DynamicMessage.newBuilder(type);
      this.builderParent = builderParent;
      builders = new SubBuilders();
      maybeForceBuilderInitialization();
    }

    /** Constructor for cloning. */
    private Builder(final com.google.protobuf.DynamicMessage.Builder builder,
        final BuilderParent builderParent) {
      this.builder = builder;
      this.builderParent = builderParent;
      builders = new SubBuilders();
      maybeForceBuilderInitialization();
    }

    private void maybeForceBuilderInitialization() {
      if (alwaysUseFieldBuilders) {
        for (final FieldDescriptor field : builder.getDescriptorForType().getFields()) {
          if (field.getJavaType() == JavaType.MESSAGE) {
            if (field.isRepeated()) {
              getRepeatedFieldBuilder(field, true);
            } else {
              getSingleFieldBuilder(field, true);
            }
          }
        }
      }
    }

    @Override
    public Builder toParent() {
      while (builderParent != null) {
        if (builderParent instanceof BuilderParentImpl) {
          // we've found the real Builder parent
          return ((BuilderParentImpl) builderParent).getBuilder();
        }

        builderParent = builderParent.getParent();
      }

      return this;
    }

    public boolean isEmpty() {
      return builders.isEmpty() && builder.getAllFields().isEmpty()
          && builder.getUnknownFields().asMap().isEmpty();
    }

    private static Builder create(final Descriptor type) {
      return new Builder(type);
    }

    void dispose() {
      builderParent = null;
    }

    /**
     * Called by the subclass when a message is built.
     */
    protected void onBuilt() {
      if (builderParent != null) {
        markClean();
      }
    }

    /**
     * Called by the subclass or a builder to notify us that a message was built and may be cached
     * and therefore invalidations are needed.
     */
    protected void markClean() {
      isClean = true;
    }

    /**
     * Gets whether invalidations are needed
     *
     * @return whether invalidations are needed
     */
    protected boolean isClean() {
      return isClean;
    }

    @Override
    public Builder clone() {
      final com.google.protobuf.DynamicMessage.Builder cloned = builder.clone();

      for (final Entry<FieldDescriptor, IFieldBuilder> entry : builders.entrySet()) {
        cloned.setField(entry.getKey(), entry.getValue().build());
      }

      return new Builder(cloned, null);
    }

    /**
     * Called by the initialization and clear code paths to allow subclasses to reset any of their
     * built-in fields back to the initial values.
     */
    @Override
    public Builder clear() {
      onChanged();
      builder.clear();
      builders.clear();
      return this;
    }

    @Override
    public Descriptor getDescriptorForType() {
      return builder.getDescriptorForType();
    }

    @Override
    public Map<FieldDescriptor, Object> getAllFields() {
      return Collections.unmodifiableMap(getAllFieldsMutable());
    }

    /** Internal helper which returns a mutable map. */
    private Map<FieldDescriptor, Object> getAllFieldsMutable() {
      final TreeMap<FieldDescriptor, Object> result = new TreeMap<FieldDescriptor, Object>();
      for (final FieldDescriptor field : builder.getDescriptorForType().getFields()) {
        if (field.isRepeated()) {
          final List<?> value = (List<?>) getField(field);

          if (!value.isEmpty()) {
            result.put(field, value);
          }
        } else {
          if (hasField(field)) {
            result.put(field, getField(field));
          }
        }
      }
      return result;
    }

    @Override
    public Builder newBuilderForField(final FieldDescriptor field) {
      verifyContainingType(field);

      if (field.getJavaType() != FieldDescriptor.JavaType.MESSAGE) {
        throw new IllegalArgumentException(
            "newBuilderForField is only valid for fields with message type.");
      }

      return create(field.getMessageType());
    }

    @Override
    public final Builder setUnknownFields(final UnknownFieldSet unknownFields) {
      builder.setUnknownFields(unknownFields);
      onChanged();
      return this;
    }

    @Override
    public final Builder mergeUnknownFields(final UnknownFieldSet unknownFields) {
      builder.mergeUnknownFields(unknownFields);
      onChanged();
      return this;
    }

    @Override
    public boolean isInitialized() {
      if (!builder.isInitialized()) {
        return false;
      }

      if (!builders.isEmpty()) {
        for (final IFieldBuilder builder : builders.values()) {
          if (!builder.isInitialized()) {
            return false;
          }
        }
      }

      return true;
    }

    @Override
    public final UnknownFieldSet getUnknownFields() {
      return builder.getUnknownFields();
    }

    /**
     * Called by subclasses to parse an unknown field.
     *
     * @return {@code true} unless the tag is an end-group tag.
     */
    protected boolean parseUnknownField(final CodedInputStream input,
        final UnknownFieldSet.Builder unknownFields, final ExtensionRegistryLite extensionRegistry,
        final int tag) throws IOException {
      return unknownFields.mergeFieldFrom(tag, input);
    }

    /**
     * Implementation of {@link BuilderParent} for giving to our children. This small inner class
     * makes it so we don't publicly expose the BuilderParent methods.
     */
    private class BuilderParentImpl implements BuilderParent {

      // @Override (Java 1.6 override semantics, but we must support 1.5)
      @Override
      public void markDirty() {
        onChanged();
      }

      @Override
      public BuilderParent getParent() {
        return builderParent;
      }

      private Builder getBuilder() {
        return Builder.this;
      }
    }

    /**
     * Gets the {@link BuilderParent} for giving to our children.
     *
     * @return The builder parent for our children.
     */
    protected BuilderParent getParentForChildren() {
      if (meAsParent == null) {
        meAsParent = new BuilderParentImpl();
      }
      return meAsParent;
    }

    /**
     * Called when a the builder or one of its nested children has changed and any parent should be
     * notified of its invalidation.
     */
    protected final void onChanged() {
      if (isClean && builderParent != null) {
        builderParent.markDirty();

        // Don't keep dispatching invalidations until build is called again.
        isClean = false;
      }
    }

    @Override
    public DynamicMessage build() {
      final DynamicMessage result = buildPartial();

      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }

      return result;
    }

    @Override
    public DynamicMessage buildPartial() {
      for (final Entry<FieldDescriptor, IFieldBuilder> entry : builders.entrySet()) {
        builder.setField(entry.getKey(), entry.getValue().build());
      }

      builders.dispose();
      final DynamicMessage result = wrap(builder.buildPartial());
      onBuilt();
      return result;
    }

    @Override
    public Builder mergeFrom(final Message other) {
      if (other instanceof DynamicMessage) {
        return mergeFrom((DynamicMessage) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    /**
     * Merges other DynamicMessage into this.
     *
     * @see #mergeFrom(Message)
     */
    public Builder mergeFrom(final DynamicMessage other) {
      verifyNotNull(other);

      if (other.getDescriptorForType() != getDescriptorForType()) {
        throw new IllegalArgumentException(
            "mergeFrom(Message) can only merge messages of the same type.");
      }

      if (other.isEmpty()) {
        return this;
      }

      for (final Entry<FieldDescriptor, Object> entry : other.getAllFields().entrySet()) {
        final FieldDescriptor field = entry.getKey();

        if (field.isRepeated()) {
          final List<?> otherValue = (List<?>) entry.getValue();

          if (!otherValue.isEmpty()) {
            if (field.getJavaType() == JavaType.MESSAGE) {
              final RepeatedFieldBuilder<Message, Builder, MessageOrBuilder> fieldBuilder =
                  getRepeatedFieldBuilder(field, false);

              if (fieldBuilder != null) {
                @SuppressWarnings("unchecked")
                final Iterable<? extends Message> otherMessages =
                (Iterable<? extends Message>) otherValue;
                fieldBuilder.addAllMessages(otherMessages);
                continue;
              }
            }

            FieldHandlerType.REPEATED_PRIMITIVE.getFieldHandler().addAllRepeatedField(this, field,
                otherValue);
          }
        } else if (field.getJavaType() == JavaType.MESSAGE) {
          FieldHandlerType.SINGLE_MESSAGE.getFieldHandler().mergeField(this, field,
              (Message) entry.getValue());
        } else {
          builder.setField(field, entry.getValue());
          onChanged();
        }
      }

      mergeUnknownFields(other.getUnknownFields());
      return this;
    }

    @Override
    public DynamicMessage getDefaultInstanceForType() {
      return getDefaultInstance(builder.getDescriptorForType());
    }

    @Override
    public DynamicMessage getDefaultInstanceForType(final Descriptor descriptor) {
      return getDefaultInstance(descriptor);
    }

    /** Verifies that the field is a field of this message. */
    private void verifyContainingType(final FieldDescriptor field) {
      if (field.getContainingType() != builder.getDescriptorForType()) {
        throw new IllegalArgumentException("FieldDescriptor does not match message type.");
      }
    }

    // *********************** Visited START ***********************************

    /**
     * Builds the Message for the given field.
     */
    Message build(final FieldDescriptor field) {
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).build(this, field);
    }

    /**
     * Builds the Message for the given field without the initialization check.
     */
    Message buildPartial(final FieldDescriptor field) {
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).buildPartial(this, field);
    }

    @Override
    public boolean hasField(final FieldDescriptor field) {
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).hasField(this, field);
    }

    @Override
    public Object getRepeatedField(final FieldDescriptor field, final int index) {
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).getRepeatedField(this, field, index);
    }

    @Override
    public Object getField(final FieldDescriptor field) {
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).getField(this, field);
    }

    @Override
    public int getRepeatedFieldCount(final FieldDescriptor field) {
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).getRepeatedFieldCount(this, field);
    }

    /**
     * Gets whether the field Message is initialized.
     */
    boolean isInitialized(final FieldDescriptor field) {
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).isInitialized(this, field);
    }

    public Builder toBuilder(final FieldDescriptor field) {
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).toBuilder(this, field);
    }

    @Override
    public Builder clearField(final FieldDescriptor field) {
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).clearField(this, field);
    }

    @Override
    public Builder getFieldBuilder(final FieldDescriptor field) {
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).getFieldBuilder(this, field);
    }

    @Override
    public Builder setField(final FieldDescriptor field, final Object value) {
      verifyNotNull(value);
      verifyContainingType(field);

      final OneofDescriptor oneofDescriptor = field.getContainingOneof();

      if (oneofDescriptor != null) {
        final FieldDescriptor oldField = builder.getOneofFieldDescriptor(oneofDescriptor);

        if (oldField != null && oldField != field) {
          clearField(oldField);
        }
      }

      return FieldHandlerType.getFieldHandler(field).setField(this, field, value);
    }

    @Override
    public Builder setRepeatedField(final FieldDescriptor field, final int index, final Object value) {
      verifyNotNull(value);
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).setRepeatedField(this, field, index, value);
    }

    @Override
    public Builder addRepeatedField(final FieldDescriptor field, final Object value) {
      verifyNotNull(value);
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).addRepeatedField(this, field, value);
    }

    @Override
    public Builder addAllRepeatedField(final FieldDescriptor field, final Iterable<?> values) {
      verifyNotNull(values);
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).addAllRepeatedField(this, field, values);
    }

    @Override
    public Builder addRepeatedField(final FieldDescriptor field, final int index, final Object value) {
      verifyNotNull(value);
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).addRepeatedField(this, field, index, value);
    }

    @Override
    public Builder mergeField(final FieldDescriptor field, final Object value) {
      verifyNotNull(value);
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).mergeField(this, field, value);
    }

    @Override
    public List<Builder> getBuilderList(final FieldDescriptor field) {
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).getBuilderList(this, field);
    }

    @Override
    public List<? extends MessageOrBuilder> getMessageOrBuilderList(final FieldDescriptor field) {
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).getMessageOrBuilderList(this, field);
    }

    @Override
    public MessageOrBuilder getMessageOrBuilder(final FieldDescriptor field) {
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).getMessageOrBuilder(this, field);
    }

    @Override
    public Builder getFieldBuilder(final FieldDescriptor field, final int index) {
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).getFieldBuilder(this, field, index);
    }

    @Override
    public MessageOrBuilder getMessageOrBuilder(final FieldDescriptor field, final int index) {
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).getMessageOrBuilder(this, field, index);
    }

    @Override
    public Builder setRepeatedField(final FieldDescriptor field, final int index,
        final Message.Builder value) {
      verifyNotNull(value);
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).setRepeatedField(this, field, index, value);
    }

    @Override
    public Builder addRepeatedField(final FieldDescriptor field, final Message.Builder value) {
      verifyNotNull(value);
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).addRepeatedField(this, field, value);
    }

    @Override
    public Builder addRepeatedField(final FieldDescriptor field, final int index,
        final Message.Builder value) {
      verifyNotNull(value);
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).addRepeatedField(this, field, index, value);
    }

    @Override
    public Builder addFieldBuilder(final FieldDescriptor field) {
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).addFieldBuilder(this, field);
    }

    @Override
    public Builder addFieldBuilder(final FieldDescriptor field, final int index) {
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).addFieldBuilder(this, field, index);
    }

    @Override
    public Builder removeRepeatedField(final FieldDescriptor field, final int index) {
      verifyContainingType(field);
      return FieldHandlerType.getFieldHandler(field).removeRepeatedField(this, field, index);
    }

    @Override
    public Builder clearOneof(final OneofDescriptor oneof) {
      final FieldDescriptor field = builder.getOneofFieldDescriptor(oneof);

      if (field != null) {
        clearField(field);
      }

      return this;
    }

    @Override
    public FieldDescriptor getOneofFieldDescriptor(final OneofDescriptor oneof) {
      return builder.getOneofFieldDescriptor(oneof);
    }

    @Override
    public boolean hasOneof(final OneofDescriptor oneof) {
      return builder.hasOneof(oneof);
    }

    /** Verifies that the oneof is an oneof of this message. */
    private void verifyOneofContainingType(final OneofDescriptor oneof) {
      if (oneof.getContainingType() != builder.getDescriptorForType()) {
        throw new IllegalArgumentException("OneofDescriptor does not match message type.");
      }
    }

    private static abstract class AbstractFieldHandler<T, E> implements
    IFieldHandler<Builder, T, E> {

      protected static <E> boolean addAll(final Collection<E> col,
          final Iterable<? extends E> values) {
        if (values == null) {
          throw new NullPointerException();
        }

        if (values instanceof Collection) {
          return !((Collection<?>) values).isEmpty()
              && col.addAll((Collection<? extends E>) values);
        } else {
          boolean isChanged = false;

          for (final E e : values) {
            col.add(e);
            isChanged = true;
          }

          return isChanged;
        }
      }

      @Override
      public Message build(final Builder builder, final FieldDescriptor field) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Message buildPartial(final Builder builder, final FieldDescriptor field) {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean hasField(final Builder builder, final FieldDescriptor field) {
        throw new UnsupportedOperationException();
      }

      @Override
      public int getRepeatedFieldCount(final Builder builder, final FieldDescriptor field) {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean isInitialized(final Builder builder, final FieldDescriptor field) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Builder toBuilder(final Builder builder, final FieldDescriptor field) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Builder clearField(final Builder builder, final FieldDescriptor field) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Builder removeRepeatedField(final Builder builder, final FieldDescriptor field,
          final int index) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Builder newBuilderForField(final Builder builder, final FieldDescriptor field) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Builder getFieldBuilder(final Builder builder, final FieldDescriptor field) {
        throw new UnsupportedOperationException();
      }

      @Override
      public MessageOrBuilder getMessageOrBuilder(final Builder builder, final FieldDescriptor field) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Builder getFieldBuilder(final Builder builder, final FieldDescriptor field,
          final int index) {
        throw new UnsupportedOperationException();
      }

      @Override
      public MessageOrBuilder getMessageOrBuilder(final Builder builder,
          final FieldDescriptor field, final int index) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Builder setRepeatedField(final Builder builder, final FieldDescriptor field,
          final int index, final Message.Builder value) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Builder addRepeatedField(final Builder builder, final FieldDescriptor field,
          final Message.Builder value) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Builder addRepeatedField(final Builder builder, final FieldDescriptor field,
          final int index, final Message.Builder value) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Builder addFieldBuilder(final Builder builder, final FieldDescriptor field) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Builder addFieldBuilder(final Builder builder, final FieldDescriptor field,
          final int index) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Builder setField(final Builder builder, final FieldDescriptor field, final Object value) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Builder setRepeatedField(final Builder builder, final FieldDescriptor field,
          final int index, final Object value) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Builder addRepeatedField(final Builder builder, final FieldDescriptor field,
          final Object value) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Builder addAllRepeatedField(final Builder builder, final FieldDescriptor field,
          final Iterable<?> values) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Builder addRepeatedField(final Builder builder, final FieldDescriptor field,
          final int index, final Object value) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Builder mergeField(final Builder builder, final FieldDescriptor field,
          final Object value) {
        throw new UnsupportedOperationException();
      }

      @Override
      public List<? extends MessageOrBuilder> getMessageOrBuilderList(final Builder builder,
          final FieldDescriptor field) {
        throw new UnsupportedOperationException();
      }

      @Override
      public E getRepeatedField(final Builder builder, final FieldDescriptor field, final int index) {
        throw new UnsupportedOperationException();
      }

      @Override
      public T getField(final Builder builder, final FieldDescriptor field) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Builder setField(final Builder builder, final FieldDescriptor field,
          final Message value) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Builder mergeField(final Builder builder, final FieldDescriptor field,
          final Message value) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Builder setRepeatedField(final Builder builder, final FieldDescriptor field,
          final int index, final Message value) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Builder addRepeatedField(final Builder builder, final FieldDescriptor field,
          final int index, final Message value) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Builder setField(final Builder builder, final FieldDescriptor field,
          final Iterable<?> value) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Builder addRepeatedField(final Builder builder, final FieldDescriptor field,
          final Message value) {
        throw new UnsupportedOperationException();
      }

      @Override
      public List<Builder> getBuilderList(final Builder builder, final FieldDescriptor field) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Builder setField(final Builder builder, final FieldDescriptor field,
          final com.google.protobuf.Message.Builder value) {
        throw new UnsupportedOperationException();
      }
    }

    private static class RepeatedPrimitiveHandler<T> extends AbstractFieldHandler<List<T>, T> {

      @Override
      public int getRepeatedFieldCount(final Builder builder, final FieldDescriptor field) {
        return builder.builder.getRepeatedFieldCount(field);
      }

      @Override
      public Builder clearField(final Builder builder, final FieldDescriptor field) {
        builder.builder.clearField(field);
        builder.onChanged();
        return builder;
      }

      @Override
      public Builder removeRepeatedField(final Builder builder, final FieldDescriptor field,
          final int index) {
        builder.ensureFieldListIsMutable(field).remove(index);
        builder.onChanged();
        return builder;
      }

      @Override
      public Builder setField(final Builder builder, final FieldDescriptor field, final Object value) {
        return setField(builder, field, (Iterable<?>) value);
      }

      @Override
      public Builder setField(final Builder builder, final FieldDescriptor field,
          final Iterable<?> value) {
        clearField(builder, field);

        for (final Object element : value) {
          addRepeatedField(builder, field, element);
        }

        return builder;
      }

      @Override
      public Builder setRepeatedField(final Builder builder, final FieldDescriptor field,
          final int index, final Object value) {
        builder.ensureFieldListIsMutable(field).set(index,
            getVerifiedSingleValue(field, value, null));
        builder.onChanged();
        return builder;
      }

      @Override
      public Builder addRepeatedField(final Builder builder, final FieldDescriptor field,
          final int index, final Object value) {
        builder.ensureFieldListIsMutable(field).add(index,
            getVerifiedSingleValue(field, value, null));
        builder.onChanged();
        return builder;
      }

      @Override
      public Builder addRepeatedField(final Builder builder, final FieldDescriptor field,
          final Object value) {
        builder.ensureFieldListIsMutable(field).add(getVerifiedSingleValue(field, value, null));
        builder.onChanged();
        return builder;
      }

      @Override
      public Builder addAllRepeatedField(final Builder builder, final FieldDescriptor field,
          final Iterable<?> values) {
        addAll(builder.ensureFieldListIsMutable(field),
            (Iterable<?>) getVerifiedValue(field, values, null));
        builder.onChanged();
        return builder;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T getRepeatedField(final Builder builder, final FieldDescriptor field, final int index) {
        return (T) builder.getRepeatedFieldValues(field, false).get(index);
      }

      @Override
      public List<T> getField(final Builder builder, final FieldDescriptor field) {
        return builder.getRepeatedFieldValues(field, true);
      }
    }

    private static class SinglePrimitiveHandler<T> extends AbstractFieldHandler<T, T> {

      @Override
      public boolean hasField(final Builder builder, final FieldDescriptor field) {
        return builder.builder.hasField(field);
      }

      @Override
      public Builder clearField(final Builder builder, final FieldDescriptor field) {
        builder.builder.clearField(field);
        builder.onChanged();
        return builder;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T getField(final Builder builder, final FieldDescriptor field) {
        return (T) builder.getFieldOrDefault(field);
      }

      @Override
      public Builder setField(final Builder builder, final FieldDescriptor field, final Object value) {
        builder.builder.setField(field, getVerifiedSingleValue(field, value, null));
        builder.onChanged();
        return builder;
      }
    }

    private static class RepeatedEnumHandler extends RepeatedPrimitiveHandler<EnumValueDescriptor> {
    }

    private static class SingleEnumHandler extends SinglePrimitiveHandler<EnumValueDescriptor> {
    }

    private static class RepeatedMessageHandler extends RepeatedPrimitiveHandler<Message> {

      @Override
      public Builder addRepeatedField(final Builder builder, final FieldDescriptor field,
          final int index, final Message value) {
        final RepeatedFieldBuilder<Message, Builder, MessageOrBuilder> fieldBuilder =
            builder.getRepeatedFieldBuilder(field, false);

        if (fieldBuilder == null) {
          super.addRepeatedField(builder, field, index, (Object) value);
        } else {
          fieldBuilder.addMessage(index, (Message) getVerifiedSingleValue(field, value, null));
        }

        return builder;
      }

      @Override
      public Builder addRepeatedField(final Builder builder, final FieldDescriptor field,
          final Message value) {
        final RepeatedFieldBuilder<Message, Builder, MessageOrBuilder> fieldBuilder =
            builder.getRepeatedFieldBuilder(field, false);

        if (fieldBuilder == null) {
          super.addRepeatedField(builder, field, (Object) value);
        } else {
          fieldBuilder.addMessage((Message) getVerifiedSingleValue(field, value, null));
        }

        return builder;
      }

      @Override
      public Builder addRepeatedField(final Builder builder, final FieldDescriptor field,
          final Message.Builder value) {
        return addRepeatedField(builder, field, value.buildPartial());
      }

      @Override
      public Builder addRepeatedField(final Builder builder, final FieldDescriptor field,
          final int index, final Message.Builder value) {
        return addRepeatedField(builder, field, index, value.buildPartial());
      }

      @Override
      public Builder addFieldBuilder(final Builder builder, final FieldDescriptor field) {
        return builder.getRepeatedFieldBuilder(field, true).addBuilder(
            builder.newBuilderForField(field).getDefaultInstanceForType());
      }

      @Override
      public Builder addFieldBuilder(final Builder builder, final FieldDescriptor field,
          final int index) {
        return builder.getRepeatedFieldBuilder(field, true).addBuilder(index,
            builder.newBuilderForField(field).getDefaultInstanceForType());
      }

      @Override
      public Builder addRepeatedField(final Builder builder, final FieldDescriptor field,
          final Object value) {
        if (value instanceof Message) {
          return addRepeatedField(builder, field, (Message) value);
        } else if (value instanceof Message.Builder) {
          return addRepeatedField(builder, field, (Message.Builder) value);
        } else {
          throw new IllegalArgumentException("value is not Message or Message.Builder");
        }
      }

      @Override
      public Builder addRepeatedField(final Builder builder, final FieldDescriptor field,
          final int index, final Object value) {
        if (value instanceof Message) {
          return addRepeatedField(builder, field, index, (Message) value);
        } else if (value instanceof Message.Builder) {
          return addRepeatedField(builder, field, index, (Message.Builder) value);
        } else {
          throw new IllegalArgumentException("value is not Message or Message.Builder");
        }
      }

      @Override
      public Builder addAllRepeatedField(final Builder builder, final FieldDescriptor field,
          final Iterable<?> values) {
        final RepeatedFieldBuilder<Message, Builder, MessageOrBuilder> fieldBuilder =
            builder.getRepeatedFieldBuilder(field, false);

        if (fieldBuilder == null) {
          super.addAllRepeatedField(builder, field, values);
        } else {
          @SuppressWarnings("unchecked")
          final Iterable<? extends Message> verifiedValues =
          (Iterable<? extends Message>) getVerifiedValue(field, values, null);
          fieldBuilder.addAllMessages(verifiedValues);
        }

        return builder;
      }

      @Override
      public Builder setRepeatedField(final Builder builder, final FieldDescriptor field,
          final int index, final Message value) {
        final RepeatedFieldBuilder<Message, Builder, MessageOrBuilder> fieldBuilder =
            builder.getRepeatedFieldBuilder(field, false);

        if (fieldBuilder == null) {
          super.setRepeatedField(builder, field, index, (Object) value);
        } else {
          fieldBuilder.setMessage(index, (Message) getVerifiedSingleValue(field, value, null));
        }

        return builder;
      }

      @Override
      public Builder setRepeatedField(final Builder builder, final FieldDescriptor field,
          final int index, final Message.Builder value) {
        return setRepeatedField(builder, field, index, value.buildPartial());
      }

      @Override
      public Builder setRepeatedField(final Builder builder, final FieldDescriptor field,
          final int index, final Object value) {
        if (value instanceof Message) {
          return setRepeatedField(builder, field, index, (Message) value);
        } else if (value instanceof Message.Builder) {
          return setRepeatedField(builder, field, index, (Message.Builder) value);
        } else {
          throw new IllegalArgumentException("value is not Message or Message.Builder");
        }
      }

      @Override
      public Builder clearField(final Builder builder, final FieldDescriptor field) {
        final RepeatedFieldBuilder<Message, Builder, MessageOrBuilder> fieldBuilder =
            builder.getRepeatedFieldBuilder(field, false);

        if (fieldBuilder == null) {
          super.clearField(builder, field);
        } else {
          fieldBuilder.clear();
        }

        return builder;
      }

      @Override
      public List<Message> getField(final Builder builder, final FieldDescriptor field) {
        final RepeatedFieldBuilder<Message, Builder, MessageOrBuilder> fieldBuilder =
            builder.getRepeatedFieldBuilder(field, false);

        if (fieldBuilder == null) {
          return super.getField(builder, field);
        } else {
          return fieldBuilder.getMessageList();
        }
      }

      @Override
      public int getRepeatedFieldCount(final Builder builder, final FieldDescriptor field) {
        final RepeatedFieldBuilder<Message, Builder, MessageOrBuilder> fieldBuilder =
            builder.getRepeatedFieldBuilder(field, false);

        if (fieldBuilder == null) {
          return super.getRepeatedFieldCount(builder, field);
        } else {
          return fieldBuilder.getCount();
        }
      }

      @Override
      public Message getRepeatedField(final Builder builder, final FieldDescriptor field,
          final int index) {
        final RepeatedFieldBuilder<Message, Builder, MessageOrBuilder> fieldBuilder =
            builder.getRepeatedFieldBuilder(field, false);

        if (fieldBuilder == null) {
          return super.getRepeatedField(builder, field, index);
        } else {
          return fieldBuilder.getMessage(index);
        }
      }

      @Override
      public MessageOrBuilder getMessageOrBuilder(final Builder builder,
          final FieldDescriptor field, final int index) {
        final RepeatedFieldBuilder<Message, Builder, MessageOrBuilder> fieldBuilder =
            builder.getRepeatedFieldBuilder(field, false);

        if (fieldBuilder == null) {
          return super.getRepeatedField(builder, field, index);
        } else {
          return fieldBuilder.getMessageOrBuilder(index);
        }
      }

      @Override
      public List<? extends MessageOrBuilder> getMessageOrBuilderList(final Builder builder,
          final FieldDescriptor field) {
        final RepeatedFieldBuilder<Message, Builder, MessageOrBuilder> fieldBuilder =
            builder.getRepeatedFieldBuilder(field, false);

        if (fieldBuilder == null) {
          return super.getField(builder, field);
        } else {
          return fieldBuilder.getMessageOrBuilderList();
        }
      }

      @Override
      public List<Builder> getBuilderList(final Builder builder, final FieldDescriptor field) {
        return builder.getRepeatedFieldBuilder(field, true).getBuilderList();
      }

      @Override
      public Builder getFieldBuilder(final Builder builder, final FieldDescriptor field,
          final int index) {
        return builder.getRepeatedFieldBuilder(field, true).getBuilder(index);
      }

      @Override
      public Builder removeRepeatedField(final Builder builder, final FieldDescriptor field,
          final int index) {
        final RepeatedFieldBuilder<Message, Builder, MessageOrBuilder> fieldBuilder =
            builder.getRepeatedFieldBuilder(field, false);

        if (fieldBuilder == null) {
          super.removeRepeatedField(builder, field, index);
        } else {
          fieldBuilder.remove(index);
        }

        return builder;
      }
    }

    private static class SingleMessageHandler extends SinglePrimitiveHandler<Message> {

      @Override
      public boolean hasField(final Builder builder, final FieldDescriptor field) {
        return builder.getSingleFieldBuilder(field, false) != null
            || super.hasField(builder, field);
      }

      @Override
      public Builder clearField(final Builder builder, final FieldDescriptor field) {
        final SingleFieldBuilder<Message, Builder, MessageOrBuilder> fieldBuilder =
            builder.getSingleFieldBuilder(field, false);

        if (fieldBuilder == null) {
          super.clearField(builder, field);
        } else {
          final OneofDescriptor oneofDescriptor = field.getContainingOneof();

          if (oneofDescriptor != null) {
            // TODO update the oneof if the original ProtoBuf does so!
            builder.builders.dispose(field);
            builder.builder.clearField(field); // or call super.clearField(builder, field);
          } else {
            fieldBuilder.clear();
          }
        }

        return builder;
      }

      @Override
      public Builder getFieldBuilder(final Builder builder, final FieldDescriptor field) {
        builder.onChanged();
        return builder.getSingleFieldBuilder(field, true).getBuilder();
      }

      @Override
      public MessageOrBuilder getMessageOrBuilder(final Builder builder, final FieldDescriptor field) {
        final SingleFieldBuilder<Message, Builder, MessageOrBuilder> fieldBuilder =
            builder.getSingleFieldBuilder(field, false);

        if (fieldBuilder == null) {
          return super.getField(builder, field);
        } else {
          return fieldBuilder.getMessageOrBuilder();
        }
      }

      @Override
      public Message getField(final Builder builder, final FieldDescriptor field) {
        final SingleFieldBuilder<Message, Builder, MessageOrBuilder> fieldBuilder =
            builder.getSingleFieldBuilder(field, false);

        if (fieldBuilder == null) {
          return super.getField(builder, field);
        } else {
          return fieldBuilder.getMessage();
        }
      }

      @Override
      public Builder setField(final Builder builder, final FieldDescriptor field,
          final Message value) {
        final SingleFieldBuilder<Message, Builder, MessageOrBuilder> fieldBuilder =
            builder.getSingleFieldBuilder(field, false);

        if (fieldBuilder == null) {
          super.setField(builder, field, (Object) value);
        } else {
          fieldBuilder.setMessage((Message) getVerifiedSingleValue(field, value, null));

          if (field.getContainingOneof() != null) {
            // we set field to default message in order to trigger oneof setting :(
            builder.builder.setField(field,
                DynamicMessage.getDefaultInstance(field.getMessageType()));
          }
        }

        return builder;
      }

      @Override
      public Builder setField(final Builder builder, final FieldDescriptor field,
          final Message.Builder value) {
        return setField(builder, field, value.build());
      }

      @Override
      public Builder setField(final Builder builder, final FieldDescriptor field, final Object value) {
        if (value instanceof Message) {
          return setField(builder, field, (Message) value);
        } else if (value instanceof Message.Builder) {
          return setField(builder, field, (Message.Builder) value);
        } else {
          throw new IllegalArgumentException("value is not a Message or a Message.Builder");
        }
      }

      @Override
      public Builder mergeField(final Builder builder, final FieldDescriptor field,
          final Object value) {
        if (value instanceof Message) {
          return mergeField(builder, field, (Message) value);
        } else {
          throw new IllegalArgumentException("value is not a Message");
        }
      }

      @Override
      public Builder mergeField(final Builder builder, final FieldDescriptor field,
          final Message value) {
        verifySingleValue(field, value, null);
        final SingleFieldBuilder<Message, Builder, MessageOrBuilder> fieldBuilder =
            builder.getSingleFieldBuilder(field, false);

        if (fieldBuilder == null) {
          Message result;

          if (builder.hasField(field)) {
            result =
                newBuilder((Message) builder.builder.getField(field)).mergeFrom(value)
                .buildPartial();
          } else {
            result = value;
          }

          builder.builder.setField(field, getVerifiedSingleValue(field, result, null));
          builder.onChanged();
        } else {
          fieldBuilder.mergeFrom(value);

          if (field.getContainingOneof() != null) {
            // we set field to default message in order to trigger oneof setting :(
            builder.builder.setField(field,
                DynamicMessage.getDefaultInstance(field.getMessageType()));
          }
        }

        return builder;
      }
    }

    private enum FieldHandlerType {
      REPEATED_MESSAGE(new RepeatedMessageHandler()), SINGLE_MESSAGE(new SingleMessageHandler()), REPEATED_PRIMITIVE(
          new RepeatedPrimitiveHandler<Object>()), SINGLE_PRIMITIVE(
              new SinglePrimitiveHandler<Object>()), REPEATED_ENUM(new RepeatedEnumHandler()), SINGLE_ENUM(
                  new SingleEnumHandler());

      private IFieldHandler<Builder, ?, ?> fieldHandler;

      private FieldHandlerType(final IFieldHandler<Builder, ?, ?> fieldHandler) {
        this.fieldHandler = fieldHandler;
      }

      public IFieldHandler<Builder, ?, ?> getFieldHandler() {
        return fieldHandler;
      }

      public static FieldHandlerType getFieldHandlerType(final FieldDescriptor field) {
        if (field.isRepeated()) {
          switch (field.getJavaType()) {
            case MESSAGE:
              return REPEATED_MESSAGE;
            case ENUM:
              return REPEATED_ENUM;
            default:
              return REPEATED_PRIMITIVE;
          }
        } else {
          switch (field.getJavaType()) {
            case MESSAGE:
              return SINGLE_MESSAGE;
            case ENUM:
              return SINGLE_ENUM;
            default:
              return SINGLE_PRIMITIVE;
          }
        }
      }

      public static IFieldHandler<Builder, ?, ?> getFieldHandler(final FieldDescriptor field) {
        return getFieldHandlerType(field).getFieldHandler();
      }
    }

    // ******************************** Visitors END

    private static void verifyRepeatedField(final FieldDescriptor field) {
      if (!field.isRepeated()) {
        throw new IllegalArgumentException("the field is not repeated");
      }
    }

    private static void verifyValue(final FieldDescriptor field, final Object value,
        final Class<? extends MessageOrBuilder> type) {
      if (field.isRepeated()) {
        if (value == null) {
          throw new NullPointerException();
        }

        final Iterable<?> values = Iterable.class.cast(value);
        final Class<? extends MessageOrBuilder> elementType = type == null ? Message.class : type;

        for (final Object element : values) {
          verifySingleValue(field, element, elementType);
        }
      } else {
        verifySingleValue(field, value, type);
      }
    }

    private static void verifySingleValue(final FieldDescriptor field, final Object value,
        final Class<? extends MessageOrBuilder> type) {
      if (value == null) {
        throw new NullPointerException();
      }

      boolean isValid = false;

      switch (field.getJavaType()) {
        case INT:
          isValid = value instanceof Integer;
          break;
        case LONG:
          isValid = value instanceof Long;
          break;
        case FLOAT:
          isValid = value instanceof Float;
          break;
        case DOUBLE:
          isValid = value instanceof Double;
          break;
        case BOOLEAN:
          isValid = value instanceof Boolean;
          break;
        case STRING:
          isValid = value instanceof String;
          break;
        case BYTE_STRING:
          isValid = value instanceof ByteString;
          break;
        case ENUM:
          if (value instanceof EnumValueDescriptor) {
            isValid = ((EnumValueDescriptor) value).getType() == field.getEnumType();
          } else if (value instanceof Internal.EnumLite) {
            isValid =
                field.getEnumType().findValueByNumber(((Internal.EnumLite) value).getNumber()) != null;
          } else if (value instanceof Number) {
            isValid = field.getEnumType().findValueByNumber(((Number) value).intValue()) != null;
          } else if (value instanceof String) {
            isValid = field.getEnumType().findValueByName((String) value) != null;
          }

          break;
        case MESSAGE:
          final Class<? extends MessageOrBuilder> expectedType =
          type == null ? Message.class : (Class<? extends MessageOrBuilder>) type;
          final Object resolvedValue =
              value instanceof LazyField ? ((LazyField) value).getValue() : value;
              isValid =
                  expectedType.cast(resolvedValue).getDescriptorForType() == field.getMessageType();
              break;
      }

      if (!isValid) {
        // TODO(kenton): When chaining calls to setField(), it can be hard to
        // tell from the stack trace which exact call failed, since the whole
        // chain is considered one line of code. It would be nice to print
        // more information here, e.g. naming the field. We used to do that.
        // But we can't now that FieldSet doesn't use descriptors. Maybe this
        // isn't a big deal, though, since it would only really apply when using
        // reflection and generally people don't chain reflection setters.
        throw new IllegalArgumentException(
            "Wrong object type used with protocol message reflection.");
      }
    }

    private static Object getVerifiedValue(final FieldDescriptor field, final Object value,
        final Class<? extends MessageOrBuilder> type) {
      if (field.isRepeated()) {
        if (value == null) {
          throw new NullPointerException();
        }

        final Iterable<?> values = Iterable.class.cast(value);
        final Class<? extends MessageOrBuilder> elementType = type == null ? Message.class : type;
        final List<Object> result = new ArrayList<Object>();

        for (final Object element : values) {
          result.add(getVerifiedSingleValue(field, element, elementType));
        }

        return result;
      } else {
        return getVerifiedSingleValue(field, value, type);
      }
    }

    private static Object getVerifiedSingleValue(final FieldDescriptor field, final Object value,
        final Class<? extends MessageOrBuilder> type) {
      if (value == null) {
        throw new NullPointerException();
      }

      switch (field.getJavaType()) {
        case INT:
          return Integer.class.cast(value);
        case LONG:
          return Long.class.cast(value);
        case FLOAT:
          return Float.class.cast(value);
        case DOUBLE:
          return Double.class.cast(value);
        case BOOLEAN:
          return Boolean.class.cast(value);
        case STRING:
          return String.class.cast(value);
        case BYTE_STRING:
          return ByteString.class.cast(value);
        case ENUM:
          if (value instanceof EnumValueDescriptor) {
            if (((EnumValueDescriptor) value).getType() == field.getEnumType()) {
              return value;
            }
          } else {
            EnumValueDescriptor enumValue = null;

            if (value instanceof Internal.EnumLite) {
              enumValue =
                  field.getEnumType().findValueByNumber(((Internal.EnumLite) value).getNumber());
            } else if (value instanceof Number) {
              enumValue = field.getEnumType().findValueByNumber(((Number) value).intValue());
            } else if (value instanceof String) {
              enumValue = field.getEnumType().findValueByName((String) value);
            }

            if (enumValue != null) {
              return enumValue;
            }
          }

          break;
        case MESSAGE:
          final Class<? extends MessageOrBuilder> expectedType =
          type == null ? Message.class : (Class<? extends MessageOrBuilder>) type;
          final MessageOrBuilder resolvedValue =
              expectedType
              .cast(value instanceof LazyField ? ((LazyField) value).getValue() : value);

          if (resolvedValue.getDescriptorForType() == field.getMessageType()) {
            return resolvedValue;
          }

          break;
      }

      throw new IllegalArgumentException("Wrong object type used with protocol message reflection.");
    }

    private static <T> T verifyNotNull(final T value) {
      if (value == null) {
        throw new NullPointerException();
      }

      return value;
    }

    @SuppressWarnings("unchecked")
    private List<Object> ensureFieldListIsMutable(final FieldDescriptor field) {
      if (builder.getRepeatedFieldCount(field) == 0) {
        builder.setField(field, Collections.emptyList());
      } else {
        final List<Object> list = (List<Object>) builder.getField(field);

        if (isListMutable(list)) {
          return list;
        } else {
          builder.setField(field, list);
        }
      }

      return (List<Object>) builder.getField(field);
    }

    private boolean isListMutable(final List<?> list) {
      return list.getClass() == ArrayList.class;
    }

    private RepeatedFieldBuilder<Message, Builder, MessageOrBuilder> getRepeatedFieldBuilder(
        final FieldDescriptor field, final boolean newIfNull) {
      @SuppressWarnings("unchecked")
      RepeatedFieldBuilder<Message, Builder, MessageOrBuilder> fieldBuilder =
      (RepeatedFieldBuilder<Message, Builder, MessageOrBuilder>) builders.get(field);

      if (newIfNull && fieldBuilder == null) {
        final List<Message> messages = this.<Message>getRepeatedFieldValues(field, false);
        fieldBuilder =
            new DynamicRepeatedFieldBuilder(messages, isListMutable(messages),
                getParentForChildren(), isClean());
        builders.put(field, fieldBuilder);
        builder.clearField(field);
      }

      return fieldBuilder;
    }

    private <T> List<T> getRepeatedFieldValues(final FieldDescriptor field,
        final boolean returnImmutable) {
      if (builder.getRepeatedFieldCount(field) == 0) {
        return Collections.<T>emptyList();
      } else {
        @SuppressWarnings("unchecked")
        final List<T> values = (List<T>) builder.getField(field);
        return returnImmutable ? Collections.unmodifiableList(values) : values;
      }
    }

    private SingleFieldBuilder<Message, Builder, MessageOrBuilder> getSingleFieldBuilder(
        final FieldDescriptor field, final boolean newIfNull) {
      @SuppressWarnings("unchecked")
      SingleFieldBuilder<Message, Builder, MessageOrBuilder> fieldBuilder =
      (SingleFieldBuilder<Message, Builder, MessageOrBuilder>) builders.get(field);

      if (newIfNull && fieldBuilder == null) {
        fieldBuilder =
            new DynamicSingleFieldBuilder((Message) getSingleFieldValue(field),
                getParentForChildren(), isClean());

        builders.put(field, fieldBuilder);

        if (field.getContainingOneof() == null) {
          builder.clearField(field);
        } else {
          builder.setField(field, DynamicMessage.getDefaultInstance(field.getMessageType()));
        }
      }

      return fieldBuilder;
    }


    @SuppressWarnings("unchecked")
    private <T> T getSingleFieldValue(final FieldDescriptor field) {
      if (builder.hasField(field)) {
        return (T) builder.getField(field);
      }

      if (field.getJavaType() == JavaType.MESSAGE) {
        return (T) DynamicMessage.getDefaultInstance(field.getMessageType());
      } else {
        return (T) field.getDefaultValue();
      }
    }

    private Object getFieldOrDefault(final FieldDescriptor field) {
      if (field.isRepeated()) {
        if (builder.getRepeatedFieldCount(field) == 0) {
          return Collections.emptyList();
        }
      } else if (field.getJavaType() == FieldDescriptor.JavaType.MESSAGE
          && !builder.hasField(field)) {
        return getDefaultInstance(field.getMessageType());
      }

      return builder.getField(field);
    }

    // ********************* FieldBuilder STUFF END ****************************

    // ******************************** SubBuilders START
    private class SubBuilders implements Map<FieldDescriptor, IFieldBuilder> {
      private final Map<FieldDescriptor, IFieldBuilder> fields;

      public SubBuilders() {
        fields = new HashMap<FieldDescriptor, IFieldBuilder>();
      }

      @Override
      public int size() {
        return fields.size();
      }

      @Override
      public boolean isEmpty() {
        return fields.isEmpty();
      }

      @Override
      public boolean containsKey(final Object key) {
        return fields.containsKey(key);
      }

      @Override
      public boolean containsValue(final Object value) {
        return fields.containsValue(value);
      }

      @Override
      public IFieldBuilder get(final Object key) {
        return fields.get(key);
      }

      @Override
      public IFieldBuilder put(final FieldDescriptor key, final IFieldBuilder value) {
        return fields.put(key, value);
      }

      @Override
      public IFieldBuilder remove(final Object key) {
        return fields.remove(key);
      }

      public void dispose(final FieldDescriptor key) {
        final IFieldBuilder builder = fields.remove(key);

        if (builder != null) {
          builder.dispose();
        }
      }


      @Override
      public void putAll(final Map<? extends FieldDescriptor, ? extends IFieldBuilder> m) {
        fields.putAll(m);
      }

      @Override
      public void clear() {
        if (!fields.isEmpty()) {
          for (final IFieldBuilder builder : fields.values()) {
            builder.clear();
          }

          fields.clear();
        }
      }

      public void dispose() {
        if (!fields.isEmpty()) {
          for (final IFieldBuilder builder : fields.values()) {
            builder.dispose();
          }

          fields.clear();
        }
      }

      @Override
      public Set<FieldDescriptor> keySet() {
        return fields.keySet();
      }

      @Override
      public Collection<IFieldBuilder> values() {
        return fields.values();
      }

      @Override
      public Set<Entry<FieldDescriptor, IFieldBuilder>> entrySet() {
        return fields.entrySet();
      }

      @Override
      public boolean equals(final Object o) {
        return fields.equals(o);
      }

      @Override
      public int hashCode() {
        return fields.hashCode();
      }
    }

    // ******************************** SubBuilders END

    private static final class DynamicRepeatedFieldBuilder extends
        RepeatedFieldBuilder<Message, Builder, MessageOrBuilder> {
      private DynamicRepeatedFieldBuilder(final List<Message> messages,
          final boolean isMessagesListMutable, final BuilderParent parent, final boolean isClean) {
        super(messages, isMessagesListMutable, parent, isClean);
      }

      @Override
      protected SingleFieldBuilder<Message, Builder, MessageOrBuilder> newSingleFieldBuilder(
          final Message message, final BuilderParent parent, final boolean isClean) {
        return new DynamicSingleFieldBuilder(message, parent, isClean);
      }
    }

    private static final class DynamicSingleFieldBuilder extends
        SingleFieldBuilder<Message, Builder, MessageOrBuilder> {
      private DynamicSingleFieldBuilder(final Message message, final BuilderParent parent,
          final boolean isClean) {
        super(message, parent, isClean);
      }

      @Override
      protected Builder newBuilderForType(final Descriptor type, final BuilderParent parent) {
        return DynamicMessage.newBuilder(type, parent);
      }

      @Override
      protected void markClean(final Builder builder) {
        builder.markClean();
      }

      @Override
      protected void dispose(final Builder builder) {
        builder.dispose();
      }
    }

    static SingleFieldBuilder<Message, Builder, MessageOrBuilder> newSingleFieldBuilderForTesting(
        final Message message, final BuilderParent parent, final boolean isClean) {
      return new DynamicSingleFieldBuilder(message, parent, isClean);
    }

    static RepeatedFieldBuilder<Message, Builder, MessageOrBuilder> newRepeatedFieldBuilderForTesting(
        final List<Message> messages, final boolean isMessagesListMutable,
        final BuilderParent parent, final boolean isClean) {
      return new DynamicRepeatedFieldBuilder(messages, isMessagesListMutable, parent, isClean);
    }
  }

  private static final class DynamicMesageProvider implements IDynamicMessageProvider {

    @Override
    public Builder newBuilder(final Descriptor type) {
      return DynamicMessage.newBuilder(type);
    }

    @Override
    public Builder newBuilder(final Message message) {
      return DynamicMessage.newBuilder(message);
    }

    @Override
    public DynamicMessage getDefaultInstance(final Descriptor type) {
      return DynamicMessage.getDefaultInstance(type);
    }

    @Override
    public DynamicMessage parseFrom(final Descriptor descriptor, final ByteString rawBytes)
        throws InvalidProtocolBufferException {
      return DynamicMessage.parseFrom(descriptor, rawBytes);
    }

    @Override
    public DynamicMessage parseFrom(final Descriptor descriptor, final ByteString rawBytes,
        final ExtensionRegistry extensionRegistry) throws InvalidProtocolBufferException {
      return DynamicMessage.parseFrom(descriptor, rawBytes, extensionRegistry);
    }

    @Override
    public DynamicMessage parseFrom(final Descriptor descriptor, final byte[] bytes)
        throws InvalidProtocolBufferException {
      return DynamicMessage.parseFrom(descriptor, bytes);
    }

    @Override
    public DynamicMessage parseFrom(final Descriptor type, final CodedInputStream input)
        throws IOException {
      return DynamicMessage.parseFrom(type, input);
    }

    @Override
    public DynamicMessage parseFrom(final Descriptor type, final CodedInputStream input,
        final ExtensionRegistry extensionRegistry) throws IOException {
      return DynamicMessage.parseFrom(type, input, extensionRegistry);
    }

    @Override
    public DynamicMessage parseFrom(final Descriptor type, final byte[] data,
        final ExtensionRegistry extensionRegistry) throws InvalidProtocolBufferException {
      return DynamicMessage.parseFrom(type, data, extensionRegistry);
    }

    @Override
    public DynamicMessage parseFrom(final Descriptor type, final InputStream input)
        throws IOException {
      return DynamicMessage.parseFrom(type, input);
    }

    @Override
    public DynamicMessage parseFrom(final Descriptor type, final InputStream input,
        final ExtensionRegistry extensionRegistry) throws IOException {
      return DynamicMessage.parseFrom(type, input, extensionRegistry);
    }
  }
}
