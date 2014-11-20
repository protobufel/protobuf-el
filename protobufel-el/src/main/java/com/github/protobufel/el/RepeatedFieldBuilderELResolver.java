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

import static com.github.protobufel.el.ELSupport.*;
import static com.github.protobufel.el.ProtoLists.getFieldClass;
import static com.github.protobufel.el.ProtoLists.verifyAndConvertFieldSingleValue;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;

import com.github.protobufel.el.ProtoLists.IRepeatedFieldMessageBuilder;
import com.github.protobufel.el.ProtoLists.IRepeatedFieldValueBuilder;
import com.google.protobuf.MessageOrBuilder;

/**
 * A ProtoBuf RepeatedFieldBuilder ELResolver, similar to ListELResolver and ArrayELResolver.
 * Supports only {@code index} as its property.
 *
 * @see ListELResolver
 * @see ELResolver 
 * @author protobufel@gmail.com David Tesler
 */
public class RepeatedFieldBuilderELResolver extends ELResolver {
  private final boolean isReadOnly;

  public RepeatedFieldBuilderELResolver() {
    this(false);
  }

  public RepeatedFieldBuilderELResolver(final boolean isReadOnly) {
    this.isReadOnly = isReadOnly;
  }

  @Override
  public Class<?> getType(final ELContext context, final Object base, final Object property) {
    if (context == null) {
      throw new NullPointerException();
    }

    if ((base != null) && (base instanceof IRepeatedFieldValueBuilder)) {
      context.setPropertyResolved(true);
      final IRepeatedFieldValueBuilder<?> repeatedBuilder = (IRepeatedFieldValueBuilder<?>) base;
      final int index = toInteger(property);

      if ((index < 0) || (index >= repeatedBuilder.size())) {
        throw new PropertyNotFoundException();
      }

      return getFieldClass(repeatedBuilder.getFieldDescriptor(), false);
    }

    return null;
  }

  @Override
  public Object getValue(final ELContext context, final Object base, final Object property) {
    if (context == null) {
      throw new NullPointerException();
    }

    if ((property != null) && (base instanceof IRepeatedFieldValueBuilder)) {
      context.setPropertyResolved(base, property);
      final IRepeatedFieldValueBuilder<?> repeatedBuilder = (IRepeatedFieldValueBuilder<?>) base;
      final int index = toInteger(property);

      if ((index < 0) || (index >= repeatedBuilder.size())) {
        return null;
      }

      return repeatedBuilder.get(index);
    }

    return null;
  }

  @Override
  public void setValue(final ELContext context, final Object base, final Object property,
      final Object val) {
    if (context == null) {
      throw new NullPointerException();
    }

    if ((property != null) && (base instanceof IRepeatedFieldValueBuilder)) {
      context.setPropertyResolved(base, property);

      if (isReadOnly) {
        throw new PropertyNotWritableException();
      }

      final int index = toInteger(property);

      try {
        if (base instanceof IRepeatedFieldMessageBuilder) {
          final IRepeatedFieldMessageBuilder repeatedBuilder = (IRepeatedFieldMessageBuilder) base;
          repeatedBuilder.set(
              index,
              (MessageOrBuilder) verifyAndConvertFieldSingleValue(val,
                  repeatedBuilder.getFieldDescriptor()));
        } else {
          @SuppressWarnings("unchecked")
          final IRepeatedFieldValueBuilder<Object> repeatedBuilder =
              (IRepeatedFieldValueBuilder<Object>) base;
          repeatedBuilder.set(index,
              verifyAndConvertFieldSingleValue(val, repeatedBuilder.getFieldDescriptor()));
        }
      } catch (final UnsupportedOperationException ex) {
        throw new PropertyNotWritableException();
      } catch (final IndexOutOfBoundsException ex) {
        throw new PropertyNotFoundException();
      } catch (final ClassCastException ex) {
        throw ex;
      } catch (final NullPointerException ex) {
        throw ex;
      } catch (final IllegalArgumentException ex) {
        throw ex;
      }
    }
  }

  @Override
  public boolean isReadOnly(final ELContext context, final Object base, final Object property) {
    if (context == null) {
      throw new NullPointerException();
    }

    if ((property != null) && (base instanceof IRepeatedFieldValueBuilder)) {
      context.setPropertyResolved(true);
      final IRepeatedFieldValueBuilder<?> repeatedBuilder = (IRepeatedFieldValueBuilder<?>) base;
      final int index = toInteger(property);

      if ((index < 0) || (index >= repeatedBuilder.size())) {
        throw new PropertyNotFoundException();
      }

      return isReadOnly;
    }

    return false;
  }

  @Override
  public Iterator<FeatureDescriptor> getFeatureDescriptors(final ELContext context,
      final Object base) {
    return null;
  }

  @Override
  public Class<?> getCommonPropertyType(final ELContext context, final Object base) {
    if (base instanceof IRepeatedFieldValueBuilder) {
      return Integer.class;
    }

    return null;
  }
}
