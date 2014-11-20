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

import static com.github.protobufel.el.ProtoLists.getBuilderForRepeatedField;
import static com.github.protobufel.el.ProtoLists.getFieldClass;
import static com.github.protobufel.el.ProtoLists.verifyAndConvertField;

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.MapELResolver;
import javax.el.MethodNotFoundException;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;

/**
 * ProtoBuf Builder ELResolver, like MapELResolver with field name or FieldDescriptor as a key.
 * <ul>
 * <li>the field can be either a String, or a FieldDescriptor
 * <li>{@link #getValue} returns a special wrapper for the repeated field, either 
 * {@link IRepeatedFieldValueBuilder} for a primitive field, or {@link IRepeatedFieldMessageBuilder} 
 * for a Message type field; and delegates to the underlying Message.Builder in case of a singular 
 * field 
 * <li>{@link #setValue} delegates to to the underlying Message.Builder   
 *
 * @see ProtoLists
 * @see MapELResolver
 * @see ELResolver
 * @author protobufel@gmail.com David Tesler
 */
public class BuilderELResolver extends ELResolver {
  private final boolean isReadOnly;
  private final boolean isStrictMode;

  public BuilderELResolver() {
    this(false, false);
  }

  public BuilderELResolver(final boolean isReadOnly) {
    this(isReadOnly, false);
  }

  public BuilderELResolver(final boolean isReadOnly, final boolean isStrictMode) {
    this.isReadOnly = isReadOnly;
    this.isStrictMode = isStrictMode;
  }

  protected boolean resolveType(final Object base) {
    return base instanceof Message.Builder;
  }

  @Override
  public Iterator<FeatureDescriptor> getFeatureDescriptors(final ELContext context,
      final Object base) {
    if (resolveType(base)) {
      final List<FieldDescriptor> fields =
          ((MessageOrBuilder) base).getDescriptorForType().getFields();
      final List<FeatureDescriptor> list = new ArrayList<FeatureDescriptor>(fields.size());

      for (final FieldDescriptor field : fields) {
        final FeatureDescriptor descriptor = new FeatureDescriptor();
        descriptor.setName(field.getName());
        descriptor.setDisplayName(field.getName());
        descriptor.setShortDescription(field.getFullName());
        descriptor.setExpert(false);
        descriptor.setHidden(false);
        descriptor.setPreferred(true);
        descriptor.setValue(TYPE, getFieldClass(field, true));
        descriptor.setValue(RESOLVABLE_AT_DESIGN_TIME, Boolean.TRUE);
        list.add(descriptor);
      }

      return list.iterator();
    }

    return null;
  }

  @Override
  public Class<?> getType(final ELContext context, final Object base, final Object property) {
    if (context == null) {
      throw new NullPointerException();
    }

    if ((property != null) && resolveType(base)) {
      context.setPropertyResolved(true);
      return getFieldClass(getPropertyFieldDescriptor((MessageOrBuilder) base, property), true);
    }

    return null;
  }

  protected FieldDescriptor getPropertyFieldDescriptor(final MessageOrBuilder mob,
      final Object property) {
    final Descriptor descriptor = mob.getDescriptorForType();
    FieldDescriptor field = null;

    if (property instanceof String) {
      field = descriptor.findFieldByName(property.toString());
    } else if (property instanceof FieldDescriptor) {
      field = FieldDescriptor.class.cast(property);

      if (field.getContainingType() != descriptor) {
        field = null;
      }
    }

    if (field == null) {
      throw new PropertyNotFoundException();
    }

    return field;
  }

  @Override
  public Class<?> getCommonPropertyType(final ELContext context, final Object base) {
    if (context == null) {
      throw new NullPointerException();
    }

    if (resolveType(base)) {
      context.setPropertyResolved(true);
      return Object.class;
    }

    return null;
  }

  @Override
  public Object getValue(final ELContext context, final Object base, final Object property) {
    if (context == null) {
      throw new NullPointerException();
    }

    if ((property != null) && resolveType(base)) {
      context.setPropertyResolved(base, property);
      final Message.Builder builder = (Message.Builder) base;
      final FieldDescriptor field = getPropertyFieldDescriptor(builder, property);

      if (field.isRepeated()) {
        return getBuilderForRepeatedField(builder, field);
      } else {
        if (field.getJavaType() == JavaType.MESSAGE) {
          return builder.getFieldBuilder(field);
        } else {
          return builder.getField(field);
        }
      }
    }

    return null;
  }

  @Override
  public void setValue(final ELContext context, final Object base, final Object property,
      final Object val) {
    if (context == null) {
      throw new NullPointerException();
    }

    if ((property != null) && resolveType(base)) {
      context.setPropertyResolved(base, property);

      if (isReadOnly(context, base, property)) {
        throw new PropertyNotWritableException();
      }

      if (val == null) {
        throw new IllegalArgumentException(String.format("property %s cannot be set to null",
            property));
      }

      try {
        final Message.Builder builder = (Message.Builder) base;
        final FieldDescriptor field = getPropertyFieldDescriptor(builder, property);
        builder.setField(field, verifyAndConvertField(val, field));
      } catch (final UnsupportedOperationException ex) {
        throw new PropertyNotWritableException();
      }
    }
  }

  @Override
  public boolean isReadOnly(final ELContext context, final Object base, final Object property) {
    if (context == null) {
      throw new NullPointerException();
    }

    if ((property != null) && resolveType(base)) {
      context.setPropertyResolved(true);
      return isReadOnly;
    }

    return false;
  }

  @Override
  public Object invoke(final ELContext context, final Object base, final Object method,
      final Class<?>[] paramTypes, final Object[] params) {
    if (resolveType(base)) {
      if (isStrictMode) {
        throw new MethodNotFoundException();
      }
    }

    return null;
  }
}
