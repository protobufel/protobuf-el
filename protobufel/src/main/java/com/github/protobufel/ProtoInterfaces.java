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
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.Parser;
import com.google.protobuf.UnknownFieldSet;


/**
 * Interfaces enhancing and/or generalizing the original protobuf.
 *
 * @author protobufel@gmail.com David Tesler
 */
// TODO cleanup, stabilize, and make public its subinterfaces
public final class ProtoInterfaces {

  interface IMessageOrBuilderEx extends MessageOrBuilder {
    public IMessageEx getDefaultInstanceForType(Descriptor descriptor);
  }

  interface IMessageEx extends IMessageOrBuilderEx, Message {
    public IDynamicMessageProvider getProviderForType();

    @Override
    public IMessageEx getDefaultInstanceForType(Descriptor descriptor);

    @Override
    public IBuilder2 newBuilderForType();

    @Override
    public IBuilder2 toBuilder();

    @Override
    public Parser<? extends IMessageEx> getParserForType();

    @Override
    public IMessageEx getDefaultInstanceForType();
  }

  public interface IBuilder2 extends Message.Builder, IMessageOrBuilderEx {
    // TODO add methods with Object param new to Message.Builder, e.g. addRepeated with index,
    // or better yet, provide general (with Object value) methods, and also ones named "Child"
    // in place of "Field" with Message/Builder value params! The same with T generic type param
    // and "Attribute" name are optional. However, for convenience, refactor the attributes and
    // child methods into its own interface, and let IBuilder2 inherit from it!
    
    /**
     * Adds values to the repeated field.
     */
    public IBuilder2 addAllRepeatedField(FieldDescriptor field, Iterable<?> values);

    /**
     * Adds value to the repeated field at the specified index.
     */
    public IBuilder2 addRepeatedField(FieldDescriptor field, int index, Object value);

    /**
     * Merges the field's value.
     */
    public IBuilder2 mergeField(FieldDescriptor field, Object value);

    /**
     * Returns a live Builder list for the repeated field.
     */
    public List<? extends IBuilder2> getBuilderList(FieldDescriptor field);

    /**
     * Returns a live MessageOrBuilder list for the repeated field.
     */
    public List<? extends MessageOrBuilder> getMessageOrBuilderList(FieldDescriptor field);

    /**
     * Returns a MessageOrBuilder for the field.
     */
    public MessageOrBuilder getMessageOrBuilder(FieldDescriptor field);

    /**
     * Returns a child Builder for the repeated field at the specified index.
     */
    public IBuilder2 getFieldBuilder(FieldDescriptor field, int index);

    /**
     * Returns a MessageOrBuilder for the repeated field at the specified index.
     */
    public MessageOrBuilder getMessageOrBuilder(FieldDescriptor field, int index);

    /**
     * Sets the repeated field at the specified index.
     */
    public IBuilder2 setRepeatedField(FieldDescriptor field, int index, Message.Builder value);

    /**
     * Adds value to the repeated field.
     */
    public IBuilder2 addRepeatedField(FieldDescriptor field, Message.Builder value);

    /**
     * Adds value to the repeated field at the specified index.
     */
    public IBuilder2 addRepeatedField(FieldDescriptor field, int index, Message.Builder value);

    /**
     * Adds a new child Builder to the repeated field, and returns it.
     */
    public IBuilder2 addFieldBuilder(FieldDescriptor field);

    /**
     * Adds a new child Builder to the repeated field at the specified index, and returns it.
     */
    public IBuilder2 addFieldBuilder(FieldDescriptor field, int index);

    /**
     * Removes the value at the specified index of the repeated field.
     */
    public IBuilder2 removeRepeatedField(FieldDescriptor field, int index);

    // navigation support

    /**
     * Switches control to the parent Builder.
     */
    public IBuilder2 toParent();

    // (From Message.Builder, re-declared here only for return type covariance.
    @Override
    IBuilder2 clear();

    @Override
    IBuilder2 mergeFrom(Message other);

    @Override
    IBuilder2 clone();

    @Override
    IBuilder2 mergeFrom(CodedInputStream input) throws IOException;

    @Override
    IBuilder2 mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry)
        throws IOException;

    @Override
    IBuilder2 newBuilderForField(Descriptors.FieldDescriptor field);

    @Override
    IBuilder2 getFieldBuilder(Descriptors.FieldDescriptor field);

    @Override
    IBuilder2 setField(Descriptors.FieldDescriptor field, Object value);

    @Override
    IBuilder2 clearField(Descriptors.FieldDescriptor field);

    @Override
    IBuilder2 setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value);

    @Override
    IBuilder2 addRepeatedField(Descriptors.FieldDescriptor field, Object value);

    @Override
    IBuilder2 setUnknownFields(UnknownFieldSet unknownFields);

    @Override
    IBuilder2 mergeUnknownFields(UnknownFieldSet unknownFields);

    @Override
    IMessageEx build();

    @Override
    IMessageEx buildPartial();

    // ---------------------------------------------------------------
    // Convenience methods.

    // From MessageLite.Builder, re-declared here only for return type covariance
    @Override
    IBuilder2 mergeFrom(ByteString data) throws InvalidProtocolBufferException;

    @Override
    IBuilder2 mergeFrom(ByteString data, ExtensionRegistryLite extensionRegistry)
        throws InvalidProtocolBufferException;

    @Override
    IBuilder2 mergeFrom(byte[] data) throws InvalidProtocolBufferException;

    @Override
    IBuilder2 mergeFrom(byte[] data, int off, int len) throws InvalidProtocolBufferException;

    @Override
    IBuilder2 mergeFrom(byte[] data, ExtensionRegistryLite extensionRegistry)
        throws InvalidProtocolBufferException;

    @Override
    IBuilder2 mergeFrom(byte[] data, int off, int len, ExtensionRegistryLite extensionRegistry)
        throws InvalidProtocolBufferException;

    @Override
    IBuilder2 mergeFrom(InputStream input) throws IOException;

    @Override
    IBuilder2 mergeFrom(InputStream input, ExtensionRegistryLite extensionRegistry)
        throws IOException;
  }

  // ***************************************************************

  protected interface BuilderParent {

    /**
     * A builder becomes dirty whenever a field is modified -- including fields in nested builders
     * -- and becomes clean when build() is called. Thus, when a builder becomes dirty, all its
     * parents become dirty as well, and when it becomes clean, all its children become clean. The
     * dirtiness state is used to invalidate certain cached values. <br>
     * To this end, a builder calls markAsDirty() on its parent whenever it transitions from clean
     * to dirty. The parent must propagate this call to its own parent, unless it was already dirty,
     * in which case the grandparent must necessarily already be dirty as well. The parent can only
     * transition back to "clean" after calling build() on all children.
     */
    void markDirty();

    BuilderParent getParent();
  }

  protected interface IFieldBuilder {
    public void dispose();

    public void markDirty();

    /**
     * Clears the value of the field.
     */
    public void clear();

    public Object build();

    public boolean isInitialized();
  }

  protected interface IFieldHandler<BType extends Message.Builder, T, E> extends
      ISingleMessageFieldHandler<BType>, IRepeatedMessageFieldHandler<BType> {
    Message build(BType builder, FieldDescriptor field);

    Message buildPartial(BType builder, FieldDescriptor field);

    // BType clone();
    boolean hasField(BType builder, FieldDescriptor field);

    int getRepeatedFieldCount(BType builder, FieldDescriptor field);

    boolean isInitialized(BType builder, FieldDescriptor field);

    BType toBuilder(BType builder, FieldDescriptor field);

    BType clearField(BType builder, FieldDescriptor field);

    BType removeRepeatedField(BType builder, FieldDescriptor field, int index);

    BType newBuilderForField(BType builder, FieldDescriptor field);

    BType getFieldBuilder(BType builder, FieldDescriptor field);

    BType getFieldBuilder(BType builder, FieldDescriptor field, int index);

    BType addFieldBuilder(BType builder, FieldDescriptor field);

    BType addFieldBuilder(BType builder, FieldDescriptor field, int index);

    BType setField(BType builder, FieldDescriptor field, Object value);

    BType setField(BType builder, FieldDescriptor field, Iterable<?> value);

    BType setRepeatedField(BType builder, FieldDescriptor field, int index, Object value);

    BType addRepeatedField(BType builder, FieldDescriptor field, Object value);

    BType addAllRepeatedField(BType builder, FieldDescriptor field, Iterable<?> values);

    BType addRepeatedField(BType builder, FieldDescriptor field, int index, Object value);

    // Methods with generic returns
    E getRepeatedField(BType builder, FieldDescriptor field, int index);

    T getField(BType builder, FieldDescriptor field);
  }

  protected interface ISingleMessageFieldHandler<BType extends Message.Builder> {
    BType setField(BType builder, FieldDescriptor field, Message value);

    BType setField(BType builder, FieldDescriptor field, Message.Builder value);

    BType mergeField(BType builder, FieldDescriptor field, Message value);

    MessageOrBuilder getMessageOrBuilder(BType builder, FieldDescriptor field);

    BType mergeField(BType builder, FieldDescriptor field, Object value);
  }

  protected interface IRepeatedMessageFieldHandler<BType extends Message.Builder> {
    List<BType> getBuilderList(BType builder, FieldDescriptor field);

    List<? extends MessageOrBuilder> getMessageOrBuilderList(BType builder, FieldDescriptor field);

    BType setRepeatedField(BType builder, FieldDescriptor field, int index, Message value);

    BType addRepeatedField(BType builder, FieldDescriptor field, Message value);

    BType addRepeatedField(BType builder, FieldDescriptor field, int index, Message value);

    MessageOrBuilder getMessageOrBuilder(BType builder, FieldDescriptor field, int index);

    BType setRepeatedField(BType builder, FieldDescriptor field, int index, Message.Builder value);

    BType addRepeatedField(BType builder, FieldDescriptor field, Message.Builder value);

    BType addRepeatedField(BType builder, FieldDescriptor field, int index, Message.Builder value);
  }

  // ************************** END of New Stuff

  interface IBuilderEx extends IBuilder, IMessageOrBuilderHelper {
    IBuilderEx setChild(FieldDescriptor key);

    IBuilderEx setChild(FieldDescriptor key, int index);

    IBuilderEx addChild(FieldDescriptor key);

    IBuilderEx addChild(FieldDescriptor key, int index);

    @Override
    IBuilderEx addChildren(FieldDescriptor key, Message... values);

    @Override
    IBuilderEx addChildren(int index, FieldDescriptor key, Message... values);

    IBuilderEx addChildren(String key, Message... values);

    IBuilderEx addChildren(int index, String key, Message... values);

    @Override
    IBuilderEx getFieldBuilder(FieldDescriptor key, int index);

    IBuilderEx addFieldBuilder(FieldDescriptor key, int index);

    IBuilderEx addFieldBuilder(FieldDescriptor key);

    IBuilderEx setFieldBuilder(FieldDescriptor key, int index);

    IBuilderEx setFieldBuilder(FieldDescriptor key);

    IBuilderEx setChild(String key, Message message);

    IBuilderEx setChild(String key);

    IBuilderEx setChild(String key, int index, Message message);

    IBuilderEx setChild(String key, int index);

    IBuilderEx addChild(String key, Message message);

    IBuilderEx addChild(String key);

    IBuilderEx addChild(String key, int index, Message message);

    IBuilderEx addChild(String key, int index);

    IBuilderEx removeChild(String key);

    IBuilderEx removeChild(String key, int index);

    <T> IBuilderEx setAttribute(String key, T value);

    <T> IBuilderEx setAttribute(String key, int index, T value);

    <T> IBuilderEx addAttribute(String key, T value);

    @Override
    <T> IBuilderEx addAttributes(FieldDescriptor key, T... values);

    <T> IBuilderEx addAttributes(String key, T... values);

    @Override
    <T> IBuilderEx addAttributes(int index, FieldDescriptor key, T... values);

    <T> IBuilderEx addAttributes(int index, String key, T... values);

    <T> IBuilderEx addAttribute(String key, int index, T value);

    IBuilderEx removeAttribute(String key);

    IBuilderEx removeAttribute(String key, int index);
  }

  interface IBuilder extends IMessageOrBuilder {
    IBuilder getFieldBuilder(FieldDescriptor key, int index);

    /**
     * Returns a live unmodifiable view of {@code List<Builder>} for the field.
     */
    @Deprecated
    List<? extends IBuilder> getFieldBuilders(FieldDescriptor key);

    IBuilder setChild(FieldDescriptor key, Message message);

    IBuilder setChild(FieldDescriptor key, int index, Message message);

    IBuilder addChild(FieldDescriptor key, Message message);

    IBuilder addChild(FieldDescriptor key, int index, Message message);

    IBuilder removeChild(FieldDescriptor key);

    IBuilder removeChild(FieldDescriptor key, int index);

    IBuilder addChildren(FieldDescriptor key, Message... values);

    IBuilder addChildren(int index, FieldDescriptor key, Message... values);

    <T> IBuilder setAttribute(FieldDescriptor key, T value);

    <T> IBuilder setAttribute(FieldDescriptor key, int index, T value);

    <T> IBuilder addAttribute(FieldDescriptor key, T value);

    <T> IBuilder addAttribute(FieldDescriptor key, int index, T value);

    IBuilder removeAttribute(FieldDescriptor key);

    IBuilder removeAttribute(FieldDescriptor key, int index);

    <T> IBuilder addAttributes(FieldDescriptor key, T... values);

    <T> IBuilder addAttributes(int index, FieldDescriptor key, T... values);

    IBuilder clear(FieldDescriptor key, int index);

    IBuilder clear();

    // Message build();
    IBuilder mergeFrom(Message message); // shouldn't it be in the Builder?


    // @Override IBuilder getChild(FieldDescriptor key);
    // @Override IBuilder getChild(FieldDescriptor key, int index);
  }

  interface IMessageOrBuilderHelper {
    Message getChild(String key);

    Message getChild(String key, int index);

    boolean isAttribute(String key);

    <T> T getAttribute(String key);

    <T> T getAttribute(String key, int index);

    boolean isFieldIndexed(String key);
  }

  interface IMessageOrBuilder extends IEmpty {
    public static final int NON_INDEXED_FIELD = Integer.MIN_VALUE;

    int getChildCount();

    Set<Entry<FieldDescriptor, Object>> getChildren();

    Iterator<FieldDescriptor> getChildKeys();

    Message getChild(FieldDescriptor key);

    Message getChild(FieldDescriptor key, int index);

    boolean isAttribute(FieldDescriptor key);

    <T> T getAttribute(FieldDescriptor key);

    <T> T getAttribute(FieldDescriptor key, int index);

    int getAttributeCount();

    Set<Entry<FieldDescriptor, Object>> getAttributes();

    Iterator<FieldDescriptor> getAttributeKeys();

    /**
     * Gets an overall count of the fields, attributes and children combined.
     */
    int size();

    boolean isFieldIndexed(FieldDescriptor key);
  }

  public interface IEmpty {
    boolean isEmpty();
  }
}
