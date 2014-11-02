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

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import com.github.protobufel.ProtoInterfaces.IEmpty;
import com.github.protobufel.ProtoInterfaces.IMessageOrBuilder;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.ProtocolMessageEnum;

/**
 * A MessageOrBuilder adapter with useful extensions and utilities.
 * 
 * @author protobufel@gmail.com David Tesler
 */
public class MessageAdapter<T extends MessageOrBuilder> implements IMessageOrBuilder {
  private final T message;
  private Map<FieldDescriptor, Object> fields;
  private Set<Entry<FieldDescriptor, Object>> attributes;
  private Set<Entry<FieldDescriptor, Object>> children;

  public MessageAdapter(final T message) {
    this.message = message;
  }

  private Map<FieldDescriptor, Object> getFields() {
    if (fields != null) {
      return fields;
    }

    synchronized (this) {
      if (fields != null) {
        return fields;
      }

      fields = message.getAllFields();
    }

    return fields;
  }

  @Override
  public boolean isEmpty() {
    return isEmpty(message);
  }

  @Override
  public boolean isAttribute(final FieldDescriptor key) {
    return key.getJavaType() != JavaType.MESSAGE;
  }

  @Override
  public <V> V getAttribute(final FieldDescriptor key) {
    return getAttribute(key, message);
  }

  @Override
  public <V> V getAttribute(final FieldDescriptor key, final int index) {
    return getAttribute(key, index, message);
  }

  @Override
  public int getAttributeCount() {
    return size() - getChildCount();
  }

  @Override
  public Set<Entry<FieldDescriptor, Object>> getAttributes() {
    if (attributes != null) {
      return attributes;
    }

    synchronized (this) {
      if (attributes != null) {
        return attributes;
      }

      attributes = new AttributeEntrySet();
    }

    return attributes;
  }

  @Override
  public Iterator<FieldDescriptor> getAttributeKeys() {
    return new AttributeKeyIterator(getFields().keySet().iterator());
  }

  @Override
  public int getChildCount() {
    return getChildCount(getFields().keySet());
  }

  @Override
  public Set<Entry<FieldDescriptor, Object>> getChildren() {
    if (children != null) {
      return children;
    }

    synchronized (this) {
      if (children != null) {
        return children;
      }

      children = new ChildEntrySet();
    }

    return children;
  }

  @Override
  public Iterator<FieldDescriptor> getChildKeys() {
    return new ChildKeyIterator(getFields().keySet().iterator());
  }

  @Override
  public boolean isFieldIndexed(final FieldDescriptor key) {
    return key.isRepeated();
  }

  @Override
  public int size() {
    return getFields().size();
  }

  @Override
  public Message getChild(final FieldDescriptor key) {
    return getChild(key, message);
  }

  @Override
  public Message getChild(final FieldDescriptor key, final int index) {
    return getChild(key, index, message);
  }

  // ********************* STATIC ADAPTER SECTION ************************

  public static boolean isFieldAttribute(final FieldDescriptor key) {
    return key.getJavaType() != JavaType.MESSAGE;
  }

  @SuppressWarnings("unchecked")
  public static <T> T getAttribute(final FieldDescriptor key, final MessageOrBuilder message) {
    return (T) message.getField(verifyFieldIsAttribute(key));
  }

  @SuppressWarnings("unchecked")
  public static <T> T getAttribute(final FieldDescriptor key, final int index,
      final MessageOrBuilder message) {
    verifyIndex(key, index);
    return (T) (key.isRepeated() ? message.getRepeatedField(verifyFieldIsAttribute(key), index)
        : getAttribute(key, message));
  }

  public static int getAttributeCount(final MessageOrBuilder message) {
    return size(message) - getChildCount(message);
  }

  public static int size(final MessageOrBuilder message) {
    return message.getAllFields().size();
  }

  public static <T extends MessageOrBuilder> Set<Entry<FieldDescriptor, Object>> getAttributes(
      final T message) {
    return new MessageAdapter<T>(message).getAttributes();
  }

  public static Iterator<FieldDescriptor> getAttributeKeys(final MessageOrBuilder message) {
    return new AttributeKeyIterator(getKeySet(message).iterator());
  }

  public static Set<FieldDescriptor> getKeySet(final MessageOrBuilder message) {
    return message.getAllFields().keySet();
  }

  public static int getChildCount(final MessageOrBuilder message) {
    return getChildCount(getKeySet(message));
  }

  protected static int getChildCount(final Set<FieldDescriptor> keySet) {
    int size = 0;

    for (final FieldDescriptor key : keySet) {
      if (!isFieldAttribute(key)) {
        size++;
      }
    }

    return size;
  }

  public static <T extends MessageOrBuilder> Set<Entry<FieldDescriptor, Object>> getChildren(
      final T message) {
    return new MessageAdapter<T>(message).getChildren();
  }

  public static Iterator<FieldDescriptor> getChildKeys(final MessageOrBuilder message) {
    return new ChildKeyIterator(getKeySet(message).iterator());
  }

  public static Message getChild(final FieldDescriptor key, final MessageOrBuilder message) {
    return (Message) message.getField(verifyFieldIsChild(key));
  }

  public static Message getChild(final FieldDescriptor key, final int index,
      final MessageOrBuilder message) {
    verifyIndex(key, index);
    return MessageAdapter.<Message>getField(key, index, message);
  }

  @SuppressWarnings("unchecked")
  public static <T> T getField(final FieldDescriptor key, final int index,
      final MessageOrBuilder message) {
    verifyIndex(key, index);
    return (T) (key.isRepeated() ? message.getRepeatedField(key, index) : message.getField(key));
  }

  public static boolean isEmpty(final MessageOrBuilder message) {
    if (message instanceof IEmpty) {
      return ((IEmpty) message).isEmpty();
    }

    return message.equals(message.getDefaultInstanceForType());
  }

  private final class AttributeEntrySet extends
      AbstractFieldSet<Map.Entry<FieldDescriptor, Object>> {

    @Override
    public Iterator<Map.Entry<FieldDescriptor, Object>> iterator() {
      return new AttributeEntryIterator(getFields().entrySet().iterator());
    }
  }

  private static class AttributeEntryIterator extends
      AbstractFilteredIterator<Entry<FieldDescriptor, Object>> {

    public AttributeEntryIterator(final Iterator<Entry<FieldDescriptor, Object>> iterator) {
      super(iterator);
    }

    @Override
    protected boolean apply(final Map.Entry<FieldDescriptor, Object> entry) {
      return entry.getKey().getJavaType() != JavaType.MESSAGE;
    }
  }

  private static class AttributeKeyIterator extends AbstractFilteredIterator<FieldDescriptor> {

    public AttributeKeyIterator(final Iterator<FieldDescriptor> iterator) {
      super(iterator);
    }

    @Override
    protected boolean apply(final FieldDescriptor descriptor) {
      return descriptor.getJavaType() != JavaType.MESSAGE;
    }
  }

  private final class ChildEntrySet extends AbstractFieldSet<Map.Entry<FieldDescriptor, Object>> {

    @Override
    public Iterator<Map.Entry<FieldDescriptor, Object>> iterator() {
      return new ChildEntryIterator(getFields().entrySet().iterator());
    }
  }

  private static class ChildEntryIterator extends
      AbstractFilteredIterator<Entry<FieldDescriptor, Object>> {

    public ChildEntryIterator(final Iterator<Entry<FieldDescriptor, Object>> iterator) {
      super(iterator);
    }

    @Override
    protected boolean apply(final Map.Entry<FieldDescriptor, Object> entry) {
      return entry.getKey().getJavaType() == JavaType.MESSAGE;
    }
  }

  private static class ChildKeyIterator extends AbstractFilteredIterator<FieldDescriptor> {

    public ChildKeyIterator(final Iterator<FieldDescriptor> iterator) {
      super(iterator);
    }

    @Override
    protected boolean apply(final FieldDescriptor descriptor) {
      return descriptor.getJavaType() == JavaType.MESSAGE;
    }
  }

  private static abstract class AbstractFieldSet<K> extends AbstractSet<K> {
    private int size = -1;

    /**
     * Gets an evaluated and cached size. <strong>Becomes wrong if the underlying message
     * changes!</strong>
     * <p>
     * {@inheritDoc}
     */
    @Override
    public int size() {
      if (size != -1) {
        return size;
      }

      synchronized (this) {
        if (size != -1) {
          return size;
        }

        size = 0;

        for (final Iterator<K> iterator = iterator(); iterator.hasNext(); iterator.next()) {
          size++;
        }
      }

      return size;
    }
  }

  private static abstract class AbstractFilteredIterator<K> implements Iterator<K> {
    final Iterator<K> iterator;
    private K next = null;

    public AbstractFilteredIterator(final Iterator<K> iterator) {
      this.iterator = iterator;
    }

    protected abstract boolean apply(K item);

    protected K getLazyItem(final K item) {
      return item;
    }

    protected boolean isLazyItem(final K item) {
      return false;
    }

    @Override
    public boolean hasNext() {
      if (next != null) {
        return true;
      }

      while (iterator.hasNext()) {
        next = iterator.next();

        if (apply(next)) {
          return true;
        }
      }

      next = null;
      return false;
    }

    @Override
    public K next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      final K item = next;
      next = null;

      if (isLazyItem(item)) {
        return getLazyItem(item);
      } else {
        return item;
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  // *********** Helpful Utilities *****************************************************

  public static int verifyAndNormalizeIndex(final int index) {
    return verifyIndex(index) == NON_INDEXED_FIELD ? 0 : index;
  }

  public static int verifyAndNormalizeIndex(final FieldDescriptor key, final int index) {
    if (key.isRepeated()) {
      return verifyArgument(index, index >= 0, "illegal index!");
    } else if (index == NON_INDEXED_FIELD) {
      return 0;
    } else {
      return verifyArgument(index, index == 0, "illegal index!");
    }
  }

  public static boolean verifyArgument(final boolean condition, final String message) {
    return verifyArgument(condition, condition, message);
  }

  public static <T> T verifyArgument(final T value, final boolean condition, final String message) {
    if (!condition) {
      throw new IllegalArgumentException(message);
    }

    return value;
  }

  public static FieldDescriptor verifyFieldIsAttribute(final FieldDescriptor key) {
    return verifyArgument(key, key.getJavaType() != JavaType.MESSAGE, "not an attribute!");
  }

  public static FieldDescriptor verifyFieldIsChild(final FieldDescriptor key) {
    return verifyArgument(key, key.getJavaType() == JavaType.MESSAGE, "not a child!");
  }

  public static FieldDescriptor verifyFieldIsIndexed(final FieldDescriptor key) {
    return verifyArgument(key, key.isRepeated(), "key is not indexed!");
  }

  public static FieldDescriptor verifyFieldIsNotIndexed(final FieldDescriptor key) {
    return verifyArgument(key, !key.isRepeated(), "key is indexed!");
  }

  public static int verifyIndex(final FieldDescriptor key, final int index) {
    return key.isRepeated() ? verifyArgument(index, index >= 0, "illegal index!") : verifyArgument(
        index, index == NON_INDEXED_FIELD || index == 0, "illegal index!");
  }

  public static int verifyIndex(final int index) {
    if (index < 0 && index != NON_INDEXED_FIELD) {
      throw new IllegalArgumentException("Illegal index!");
    }

    return index;
  }

  public static <T> EnumValueDescriptor getEnumValue(final FieldDescriptor key, final T value) {
    MessageAdapter.verifyArgument(key.getJavaType() == JavaType.ENUM, "Not enum type!");

    if (value instanceof EnumValueDescriptor) {
      return getEnumValue(key, (EnumValueDescriptor) value);
    } else if (value instanceof ProtocolMessageEnum) {
      return getEnumValue(key, (ProtocolMessageEnum) value);
    } else if (value instanceof String) {
      return getEnumValue(key, (String) value);
    } else if (value instanceof Integer) {
      return getEnumValue(key, (Integer) value);
    } else {
      throw new IllegalArgumentException("value is not of the allowed type");
    }
  }

  public static EnumValueDescriptor getEnumValue(final FieldDescriptor key,
      final ProtocolMessageEnum value) {
    MessageAdapter.verifyArgument(key.getEnumType() == value.getDescriptorForType(),
        "Wrong enum type!");
    return value.getValueDescriptor();
  }

  public static EnumValueDescriptor getEnumValue(final FieldDescriptor key, final String value) {
    return getEnumValue(key, key.getEnumType().findValueByName(value));
  }

  public static EnumValueDescriptor getEnumValue(final FieldDescriptor key, final Integer value) {
    return getEnumValue(key, key.getEnumType().findValueByNumber(value));
  }

  public static EnumValueDescriptor getEnumValue(final FieldDescriptor key,
      final EnumValueDescriptor value) {
    return verifyArgument(value, value != null, "Wrong enum value!");
  }

  public static FieldDescriptor getFieldDescriptor(final Descriptor descriptor,
      final String fieldName) {
    return descriptor.findFieldByName(fieldName);
  }

  public static FieldDescriptor getFieldDescriptor(final MessageOrBuilder builder,
      final String fieldName) {
    return builder.getDescriptorForType().findFieldByName(fieldName);
  }

  @SuppressWarnings("unchecked")
  public static <T> T getField(final MessageOrBuilder builder, final String fieldName) {
    return (T) builder.getField(getFieldDescriptor(builder, fieldName));
  }

  public static Message.Builder setField(final Message.Builder builder, final String fieldName,
      final Object value) {
    final FieldDescriptor field = getFieldDescriptor(builder.getDescriptorForType(), fieldName);

    if (field.isRepeated()) {
      return builder.addRepeatedField(field, value);
    } else {
      return builder.setField(field, value);
    }
  }

  /**
   * Gets all child MessageOrBuilder descriptors for the type.
   *
   * @author protobufel@gmail.com David Tesler
   */
  protected static List<FieldDescriptor> getChildFieldDescriptors(final Descriptor type) {
    final List<FieldDescriptor> fields = new ArrayList<FieldDescriptor>();

    for (final FieldDescriptor field : type.getFields()) {
      if (field.getJavaType() == JavaType.MESSAGE) {
        fields.add(field);
      }
    }

    for (final FieldDescriptor field : type.getExtensions()) {
      if (field.getJavaType() == JavaType.MESSAGE) {
        fields.add(field);
      }
    }

    return Collections.unmodifiableList(fields);
  }

  public static Map<String, FieldDescriptor> getFieldDescriptors(final Descriptor type,
      final String... fieldNames) {
    final Map<String, FieldDescriptor> result =
        new HashMap<String, FieldDescriptor>(fieldNames.length);

    for (final String name : fieldNames) {
      final FieldDescriptor field = type.findFieldByName(name);

      if (field == null) {
        throw new NullPointerException(String.format("field '%s' doesn't exist", name));
      }

      result.put(name, field);
    }

    return Collections.unmodifiableMap(result);
  }
}
