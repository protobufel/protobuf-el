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

package com.github.protobufel.el;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.protobufel.MessageAdapter;
import com.github.protobufel.ProtoInterfaces.IBuilder2;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;

/**
 * Support for EL-friendly ProtoBuf Attribute/Builder/Message Lists. Provides a special wrapper for
 * the repeated fields, {@link IRepeatedFieldMessageBuilder} for Message fields,
 * {@link IRepeatedFieldValueBuilder} otherwise.
 * <p>
 * In concert with {@link RepeatedFieldBuilderELResolver} exposes all the wrapper's methods and the
 * array-like indexed access to its elements.
 *
 * @author protobufel@gmail.com David Tesler
 */
public class ProtoLists {
  private ProtoLists() {}

  public interface IRepeatedFieldValueBuilder<T> {
    public T add();

    public IRepeatedFieldValueBuilder<T> add(T value);

    public IRepeatedFieldValueBuilder<T> add(int index, T value);

    public IRepeatedFieldValueBuilder<T> addAll(Collection<? extends T> values);

    public IRepeatedFieldValueBuilder<T> set(int index, T value);

    public IRepeatedFieldValueBuilder<T> remove(int index);

    public IRepeatedFieldValueBuilder<T> clear();

    public Message.Builder getParent();

    public T newInstance();

    public int size();

    public T get(int index);

    public int getChangedIndex();

    public T getChanged(int index);

    public T getLast();

    public List<? extends T> getList();

    public FieldDescriptor getFieldDescriptor();

    public JavaType getType();
  }

  public interface IRepeatedFieldMessageBuilder extends
      IRepeatedFieldValueBuilder<MessageOrBuilder> {
    @Override
    public Message.Builder add();

    @Override
    public IRepeatedFieldMessageBuilder add(MessageOrBuilder value);

    @Override
    public IRepeatedFieldMessageBuilder add(int index, MessageOrBuilder value);

    @Override
    public IRepeatedFieldMessageBuilder addAll(Collection<? extends MessageOrBuilder> values);

    @Override
    public IRepeatedFieldMessageBuilder set(int index, MessageOrBuilder value);

    @Override
    public IRepeatedFieldMessageBuilder remove(int index);

    @Override
    public IRepeatedFieldMessageBuilder clear();

    @Override
    public Message.Builder get(int index);

    @Override
    public int getChangedIndex();

    @Override
    public Message.Builder getChanged(int index);

    @Override
    public Message.Builder getLast();

    @Override
    public List<? extends Message> getList();

    @Override
    public Message.Builder newInstance();

    public List<? extends Message.Builder> getBuilders();

    public Message getMessage(int index);
  }

  public interface IRepeatedList<T> extends List<T> {
    public T add();

    public Message.Builder getParent();

    public T getChanged();

    public int getChangedIndex();
  }

  public interface IRepeatedMessageOrBuilderList<T extends MessageOrBuilder> extends
      IRepeatedList<MessageOrBuilder> {
    @Override
    public Message.Builder add();

    @Override
    public Message set(int index, MessageOrBuilder value);

    public List<Message.Builder> getBuilders();

    public List<Message> getMessages();
  }

  // **** Factory methods
  public static final Class<?> getFieldClass(final FieldDescriptor field,
      final boolean isRepeatedAsList) {
    if (isRepeatedAsList && field.isRepeated()) {
      // return IRepeatedFieldValueBuilder.class;
      return List.class;
    }

    switch (field.getJavaType()) {
      case BOOLEAN:
        return Boolean.class;
      case DOUBLE:
        return Double.class;
      case FLOAT:
        return Float.class;
      case INT:
        return Integer.class;
      case LONG:
        return Long.class;
      case STRING:
        return String.class;
      case BYTE_STRING:
        return String.class;
      case ENUM:
        // return String.getClass();
        // return field.getEnumType().getClass();
        return Object.class;
      case MESSAGE:
        // return field.getMessageType().getClass();
        return MessageOrBuilder.class;
      default:
        return null;
    }
  }

  public static final Object verifyAndConvertField(final Object val, final FieldDescriptor field) {
    switch (field.getJavaType()) {
      case MESSAGE:
        if (field.isRepeated()) {
          final List<?> list = List.class.cast(val);
          final List<Message> messages = new ArrayList<Message>(list.size());

          for (final Object obj : list) {
            messages.add(verifyAndConvertToMessageIfBuilder(field, obj));
          }

          return messages;
        } else {
          return verifyAndConvertToMessageIfBuilder(field, val);
        }
      case ENUM:
        if (field.isRepeated()) {
          final List<?> list = List.class.cast(val);
          final List<EnumValueDescriptor> enums = new ArrayList<EnumValueDescriptor>(list.size());

          for (final Object obj : list) {
            enums.add(MessageAdapter.getEnumValue(field, obj));
          }

          return enums;
        } else {
          return MessageAdapter.getEnumValue(field, val);
        }
      default:
        return val;
    }
  }

  public static final Object verifyAndConvertFieldSingleValue(final Object val,
      final FieldDescriptor field) {
    if (val == null) {
      throw new NullPointerException();
    } else if (val instanceof Iterable) {
      throw new IllegalArgumentException("must be a single value, not Iterable");
    }

    switch (field.getJavaType()) {
      case MESSAGE:
        return verifyAndConvertToMessageIfBuilder(field, val);
      case ENUM:
        return MessageAdapter.getEnumValue(field, val);
      default:
        return val;
    }
  }

  public static final Message verifyAndConvertToMessageIfBuilder(final FieldDescriptor field,
      final Object val) {
    if (!(val instanceof MessageOrBuilder)) {
      throw new IllegalArgumentException("the value should be of MessageOrBuilder type");
    } else if (field.getMessageType() != ((MessageOrBuilder) val).getDescriptorForType()) {
      throw new IllegalArgumentException("the value of a wrong builder type");
    }

    return val instanceof Message.Builder ? ((Message.Builder) val).buildPartial() : (Message) val;
  }

  public static final IRepeatedFieldValueBuilder<?> getBuilderForRepeatedField(
      final Message.Builder builder, final FieldDescriptor field) {
    if (field.getJavaType() == JavaType.MESSAGE) {
      return getBuilderForRepeatedChild(builder, field);
    } else {
      return getBuilderForRepeatedAttribute(builder, field);
    }
  }

  public static final IRepeatedFieldValueBuilder<Object> getBuilderForRepeatedAttribute(
      final Message.Builder builder, final FieldDescriptor field) {
    if (!field.isRepeated()) {
      throw new IllegalArgumentException("the field must be repeated");
    } else if (builder.getDescriptorForType() != field.getContainingType()) {
      throw new IllegalArgumentException("the field doen't belong to this type");
    }
    if (field.getJavaType() == JavaType.MESSAGE) {
      throw new IllegalArgumentException("the field must be of a non-Message type");
    }

    return new RepeatedFieldValueBuilder<Message.Builder, Object>(builder, field);
  }

  @SuppressWarnings("unchecked")
  public static final IRepeatedFieldMessageBuilder getBuilderForRepeatedChild(
      final Message.Builder builder, final FieldDescriptor field) {
    if (!field.isRepeated()) {
      throw new IllegalArgumentException("the field must be repeated");
    } else if (field.getJavaType() != JavaType.MESSAGE) {
      throw new IllegalArgumentException("the field must be of Message type");
    } else if (builder.getDescriptorForType() != field.getContainingType()) {
      throw new IllegalArgumentException("the field doen't belong to this type");
    }

    if (builder instanceof GeneratedMessage.Builder) {
      final GeneratedMessage.Builder<?> generatedBuilder = (GeneratedMessage.Builder<?>) builder;
      return getGeneratedRepeatedFieldBuilder(generatedBuilder, field, generatedBuilder.getClass());
    } else if (builder instanceof IBuilder2) {
      return new DynamicRepeatedFieldMessageBuilder((IBuilder2) builder, field);
    } else {
      throw new IllegalArgumentException("builder is not of a supported Builder type");
    }
  }

  private static <P extends GeneratedMessage.Builder<P>> IRepeatedFieldMessageBuilder getGeneratedRepeatedFieldBuilder(
      final GeneratedMessage.Builder<?> builder, final FieldDescriptor field, final Class<P> type) {
    return new GeneratedRepeatedFieldMessageBuilder<P>(type.cast(builder), field);
  }

  public static class RepeatedFieldValueBuilder<P extends Message.Builder, E> implements
      IRepeatedFieldValueBuilder<E> {
    protected final P parent;
    protected final FieldDescriptor field;
    protected int changedIndex = -1;

    public RepeatedFieldValueBuilder(final P parent, final FieldDescriptor field) {
      this.parent = parent;
      this.field = field;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E get(final int index) {
      return (E) parent.getRepeatedField(field, index);
    }

    @Override
    public int size() {
      return parent.getRepeatedFieldCount(field);
    }

    @Override
    public IRepeatedFieldValueBuilder<E> set(final int index, final E element) {
      parent.setRepeatedField(field, index, element);
      changedIndex = index;
      return this;
    }

    @Override
    public E add() {
      add(size(), newInstance());
      return getLast();
    }

    @SuppressWarnings("unchecked")
    @Override
    public E newInstance() {
      return (E) field.getDefaultValue();
    }

    @Override
    public IRepeatedFieldValueBuilder<E> add(final E element) {
      return add(size(), element);
    }

    @Override
    public IRepeatedFieldValueBuilder<E> add(final int index, final E element) {
      @SuppressWarnings("unchecked")
      final List<E> list = (List<E>) parent.getField(field);

      if (((list == null) && (index != 0)) || (index < 0)
          || ((list != null) && (index > list.size()))) {
        throw new IndexOutOfBoundsException();
      }

      final List<E> newList = list == null ? new ArrayList<E>() : new ArrayList<E>(list);
      newList.add(index, element);
      parent.setField(field, newList);
      changedIndex = index;
      return this;
    }

    @Override
    public IRepeatedFieldValueBuilder<E> remove(final int index) {
      @SuppressWarnings("unchecked")
      final List<E> list = (List<E>) parent.getField(field);

      if ((list == null) || (index < 0) || (index >= list.size())) {
        throw new IndexOutOfBoundsException();
      }

      final List<E> newList = new ArrayList<E>(list);
      newList.remove(index);
      parent.setField(field, newList);
      changedIndex = index;
      return this;
    }

    @Override
    public IRepeatedFieldValueBuilder<E> clear() {
      parent.clearField(field);
      changedIndex = -1;
      return this;
    }

    @Override
    public Message.Builder getParent() {
      return parent;
    }

    @Override
    public int getChangedIndex() {
      return changedIndex;
    }

    @Override
    public E getChanged(final int index) {
      return (changedIndex == -1) ? null : get(changedIndex);
    }

    @Override
    public E getLast() {
      final int size = size();
      return (size == 0) ? null : get(size - 1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<? extends E> getList() {
      return (List<? extends E>) parent.getField(field);
    }

    @Override
    public IRepeatedFieldValueBuilder<E> addAll(final Collection<? extends E> values) {
      for (final E value : values) {
        add(value);
      }

      return this;
    }

    @Override
    public FieldDescriptor getFieldDescriptor() {
      return field;
    }

    @Override
    public JavaType getType() {
      return field.getJavaType();
    }
  }

  public static abstract class AbstractRepeatedFieldMessageBuilder<P extends Message.Builder>
      extends RepeatedFieldValueBuilder<P, MessageOrBuilder> implements
      IRepeatedFieldMessageBuilder {
    protected Class<? extends Message.Builder> elementBuilderType;

    public AbstractRepeatedFieldMessageBuilder(final P parent, final FieldDescriptor field) {
      super(parent, field);
      elementBuilderType = getElementBuilderType();
    }

    // public abstract boolean isElementMessage();

    protected abstract Message.Builder getBuilder(int index);

    protected abstract void addBuilder(int index, Message.Builder builder);

    protected abstract void removeElement(int index);

    protected abstract void setBuilder(int index, Message.Builder builder);

    protected abstract void addMessage(int index, Message message);

    public Class<? extends Message.Builder> getElementBuilderType() {
      return parent.newBuilderForField(field).getClass();
    }

    public Class<? extends Message> getElementMessageType() {
      return parent.newBuilderForField(field).buildPartial().getClass();
    }

    protected Message.Builder getSafeBuilder(final Message.Builder element) {
      return element.clone();
    }

    @Override
    public Message.Builder get(final int index) {
      return getBuilder(index);
    }

    @Override
    public Message getMessage(final int index) {
      return (Message) parent.getRepeatedField(field, index);
    }

    @Override
    public Message.Builder getChanged(final int index) {
      return (Message.Builder) super.getChanged(index);
    }

    @Override
    public Message.Builder getLast() {
      return (Message.Builder) super.getLast();
    }

    @Override
    public IRepeatedFieldMessageBuilder set(final int index, final MessageOrBuilder element) {
      if (element instanceof Message) {
        parent.setRepeatedField(field, index, element);
      } else {
        setBuilder(index, getSafeBuilder((Message.Builder) element));
      }

      return this;
    }

    @Override
    public Message.Builder add() {
      add(size(), newInstance().buildPartial());
      return getLast();
    }

    @Override
    public Message.Builder newInstance() {
      return parent.newBuilderForField(field);
    }

    @Override
    public IRepeatedFieldMessageBuilder add(final MessageOrBuilder element) {
      return add(size(), element);
    }

    @Override
    public IRepeatedFieldMessageBuilder add(final int index, final MessageOrBuilder element) {
      if (element instanceof Message) {
        addMessage(index, (Message) element);
      } else {
        addBuilder(index, getSafeBuilder((Message.Builder) element));
      }

      return this;
    }

    @Override
    public IRepeatedFieldMessageBuilder remove(final int index) {
      removeElement(index);
      return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<? extends Message> getList() {
      return (List<? extends Message>) super.getList();
    }

    @Override
    public IRepeatedFieldMessageBuilder clear() {
      return (IRepeatedFieldMessageBuilder) super.clear();
    }

    @Override
    public IRepeatedFieldMessageBuilder addAll(final Collection<? extends MessageOrBuilder> values) {
      return (IRepeatedFieldMessageBuilder) super.addAll(values);
    }
  }

  public static class DynamicRepeatedFieldMessageBuilder extends
      AbstractRepeatedFieldMessageBuilder<IBuilder2> {

    public DynamicRepeatedFieldMessageBuilder(final IBuilder2 parent, final FieldDescriptor field) {
      super(parent, field);
    }

    @Override
    protected Message.Builder getBuilder(final int index) {
      return parent.getFieldBuilder(field, index);
    }

    @Override
    protected void addBuilder(final int index, final Message.Builder builder) {
      parent.addRepeatedField(field, index, builder);
    }

    @Override
    protected void removeElement(final int index) {
      parent.removeRepeatedField(field, index);
    }

    @Override
    protected void setBuilder(final int index, final Message.Builder builder) {
      parent.setRepeatedField(field, index, builder);
    }

    @Override
    protected void addMessage(final int index, final Message message) {
      parent.addRepeatedField(field, index, message);
    }

    @Override
    public List<? extends Message.Builder> getBuilders() {
      return parent.getBuilderList(field);
    }
  }

  public static class GeneratedRepeatedFieldMessageBuilder<P extends GeneratedMessage.Builder<P>>
      extends AbstractRepeatedFieldMessageBuilder<P> {
    private final IGeneratedBuilderReflectionSupport<P> reflection;

    @SuppressWarnings("unchecked")
    public GeneratedRepeatedFieldMessageBuilder(final P parent, final FieldDescriptor field) {
      super(parent, field);
      this.reflection =
          new GeneratedBuilderReflectionSupport<P>((Class<P>) parent.getClass(), field);
    }

    @Override
    protected Message.Builder getBuilder(final int index) {
      return reflection.getBuilder(parent, index);
    }

    @Override
    protected void addBuilder(final int index, final Message.Builder builder) {
      reflection.addBuilder(parent, index, builder);
    }

    @Override
    protected void removeElement(final int index) {
      reflection.removeElement(parent, index);
    }

    @Override
    protected void setBuilder(final int index, final Message.Builder builder) {
      reflection.setBuilder(parent, index, builder);
    }

    @Override
    protected void addMessage(final int index, final Message message) {
      reflection.addMessage(parent, index, message);
    }

    @Override
    public List<? extends Message.Builder> getBuilders() {
      return reflection.getBuilderList(parent);
    }
  }

  // ***************************** GeneratedMessage.Builder Reflection START ***************
  public interface IGeneratedBuilderReflectionSupport<P extends GeneratedMessage.Builder<P>> {

    public GeneratedMessage.Builder<?> getBuilder(P parent, int index);

    public void addBuilder(P parent, int index, Message.Builder builder);

    public void removeElement(P parent, int index);

    public void setBuilder(P parent, int index, Message.Builder builder);

    public void addMessage(P parent, int index, Message message);

    public Class<?> getElementBuilderType();

    public Class<?> getElementMessageType();

    public List<? extends GeneratedMessage.Builder<?>> getBuilderList(P parent);
  }

  public static class GeneratedBuilderReflectionSupport<P extends GeneratedMessage.Builder<P>>
      implements IGeneratedBuilderReflectionSupport<P> {
    private Method methodAddBuilder;
    private Method methodRemove;
    private Method methodSetBuilder;
    private Method methodGetBuilder;
    private Method methodAddMessage;
    private Method methodBuilderList;
    protected final Class<P> parentClass;
    protected final FieldDescriptor field;

    public GeneratedBuilderReflectionSupport(final Class<P> parentClass, final FieldDescriptor field) {
      this.parentClass = parentClass;
      this.field = field;
    }

    protected Method getMethodGetBuilder() {
      if (methodGetBuilder == null) {
        methodGetBuilder = getGeneratedMethod(parentClass, field, "get", "Builder", int.class);
      }

      return methodGetBuilder;
    }

    protected Method getMethodAddBuilder() {
      if (methodAddBuilder == null) {
        methodAddBuilder =
            getGeneratedMethod(parentClass, field, "add", "Builder", int.class,
                getElementBuilderType());
      }

      return methodAddBuilder;
    }

    protected Method getMethodRemove() {
      if (methodRemove == null) {
        methodRemove = getGeneratedMethod(parentClass, field, "remove", "", int.class);
      }

      return methodRemove;
    }

    protected Method getMethodSetBuilder() {
      if (methodSetBuilder == null) {
        methodSetBuilder =
            getGeneratedMethod(parentClass, field, "set", "Builder", int.class,
                getElementBuilderType());
      }

      return methodSetBuilder;
    }

    protected Method getMethodAddMessage() {
      if (methodAddMessage == null) {
        methodAddMessage =
            getGeneratedMethod(parentClass, field, "add", "", int.class, GeneratedMessage.class);
      }

      return methodAddMessage;
    }

    protected Method getMethodBuilderList() {
      if (methodBuilderList == null) {
        methodBuilderList = getGeneratedMethod(parentClass, field, "get", "BuilderList");
      }

      return methodBuilderList;
    }

    // ************************
    @Override
    public GeneratedMessage.Builder<?> getBuilder(final P parent, final int index) {
      return invokeMethod(getMethodGetBuilder(), parent, index);
    }

    @Override
    public void addBuilder(final P parent, final int index, final Message.Builder builder) {
      invokeMethod(getMethodAddBuilder(), parent, index, builder);
    }

    @Override
    public void removeElement(final P parent, final int index) {
      invokeMethod(getMethodRemove(), parent, index);
    }

    @Override
    public void setBuilder(final P parent, final int index, final Message.Builder builder) {
      invokeMethod(getMethodSetBuilder(), parent, index, builder);
    }

    @Override
    public void addMessage(final P parent, final int index, final Message message) {
      invokeMethod(getMethodAddMessage(), parent, index, message);
    }

    @Override
    public List<? extends GeneratedMessage.Builder<?>> getBuilderList(final P parent) {
      return invokeMethod(getMethodBuilderList(), parent);
    }

    @Override
    public Class<?> getElementBuilderType() {
      return getMethodGetBuilder().getReturnType();
    }

    @Override
    public Class<?> getElementMessageType() {
      return getMethodAddMessage().getReturnType();
    }

    @SuppressWarnings("unchecked")
    protected <U> U invokeMethod(final Method method, final Object base, final Object... params) {
      try {
        return (U) method.invoke(params);
      } catch (final Throwable e) {
        throw new RuntimeException(e);
      }
    }

    protected Method getGeneratedMethod(final Class<P> parentClass, final FieldDescriptor field,
        final String methodPrefix, final String methodSuffix, final Class<?>... parameterTypes) {
      String name = field.getName();
      name =
          new StringBuilder(methodPrefix).append(name.substring(0, 1).toUpperCase())
              .append(name.substring(1)).append(methodSuffix).toString();
      Method method;

      try {
        method = parentClass.getMethod(name, parameterTypes);
      } catch (final Throwable e) {
        throw new RuntimeException(e);
      }

      return method;
    }
  }

  // ***************************** GeneratedMessage.Builder Reflection END
}
