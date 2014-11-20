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

package com.github.protobufel.crud.el;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.el.ELContext;
import javax.el.ELManager;
import javax.el.ELProcessor;
import javax.el.ValueExpression;

import com.github.protobufel.DynamicMessage;
import com.github.protobufel.IDynamicMessageProvider;
import com.github.protobufel.ProtoInterfaces.IBuilder2;
import com.github.protobufel.crud.el.QueryProcessors.IQueryProcessor;
import com.github.protobufel.crud.el.QueryProcessors.QueryResultListener;
import com.github.protobufel.crud.el.QueryProcessors.ValidationListener;
import com.github.protobufel.el.ProtoELProcessorEx;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;

/**
 * Processes queries, lists of objects, using the EL 3.0 language, including validation and error
 * reporting.
 * <p>
 * Also, it allows you to specify your own JavaBeans to be used within the expression, in addition
 * to the predefined ones:
 * <ol>
 * <li>{@code records} - the immutable List<Message> of the original records
 * <li>{@code results} - the immutable List<Message> of the results being produced
 * <li>{@code record} - the current row's Message.Builder, will be in the {@code results}
 * <li>{@code index} - the current row's index in the results
 * </ol>
 * <p>
 * You can also specify the so called empty expression to the {@link Builder} to produce the
 * original records.
 * <p>
 * In addition, you can add your own {@link ValidationListener} and {@link QueryResultListener}.
 * <p>
 * If your expression returns {@code null}, the current result will be skipped, so this is the way
 * to remove the original record from the results. The {@link QueryResultListener#resultAdded(int)}
 * allows you to keep track of the being produced results. Initially, the {@code results} list is
 * empty; and the result producing loop follows the original records. So, the originals vs the
 * results diffs can be easily and efficiently calculated.
 * 
 * @author protobufel@gmail.com David Tesler
 */
public final class ProtoMessageQueryProcessor implements IQueryProcessor<Message> {
  private static final DefaultValidationListener DEFAULT_VALIDATION_LISTENER =
      new DefaultValidationListener();
  private static final ProtoMessageQueryProcessor EMPTY_INSTANCE = new ProtoMessageQueryProcessor();
  private static final ProtoMessageQueryProcessor IDENTITY_INSTANCE =
      new ProtoMessageQueryProcessor("record", null, null, null, null, null);
  private final String expression;
  private final String emptyExpression;
  private final Descriptor type;
  private final QueryResultListener resultListener;
  private final ValidationListener validationListener;
  private final IDynamicMessageProvider messageProvider;

  private ProtoMessageQueryProcessor() {
    this(null, null, null, null, null, null);
  }

  protected ProtoMessageQueryProcessor(final String expression, final String emptyExpression,
      final Descriptor type, final QueryResultListener resultListener,
      final ValidationListener validationListener, final IDynamicMessageProvider messageProvider) {
    this.type = type;
    this.resultListener = resultListener;
    this.expression = expression;
    this.emptyExpression = emptyExpression;
    this.validationListener =
        (validationListener == null) ? DEFAULT_VALIDATION_LISTENER : validationListener;
    this.messageProvider = messageProvider;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + (expression == null ? 0 : expression.hashCode());
    result = (prime * result) + (emptyExpression == null ? 0 : emptyExpression.hashCode());
    result = (prime * result) + (type == null ? 0 : type.hashCode());
    result = (prime * result) + (resultListener == null ? 0 : resultListener.hashCode());
    result = (prime * result) + (validationListener == null ? 0 : validationListener.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ProtoMessageQueryProcessor)) {
      return false;
    }
    final ProtoMessageQueryProcessor other = (ProtoMessageQueryProcessor) obj;
    if (expression == null) {
      if (other.expression != null) {
        return false;
      }
    } else if (!expression.equals(other.expression)) {
      return false;
    }
    if (emptyExpression == null) {
      if (other.emptyExpression != null) {
        return false;
      }
    } else if (!emptyExpression.equals(other.emptyExpression)) {
      return false;
    }
    if (type == null) {
      if (other.type != null) {
        return false;
      }
    } else if (!type.equals(other.type)) {
      return false;
    }
    if (resultListener == null) {
      if (other.resultListener != null) {
        return false;
      }
    } else if (!resultListener.equals(other.resultListener)) {
      return false;
    }
    if (validationListener == null) {
      if (other.validationListener != null) {
        return false;
      }
    } else if (!validationListener.equals(other.validationListener)) {
      return false;
    }
    return true;
  }

  public static ProtoMessageQueryProcessor getIdentityProcessor() {
    return IDENTITY_INSTANCE;
  }

  public static ProtoMessageQueryProcessor getEmptyProcessor() {
    return EMPTY_INSTANCE;
  }

  public static QueryBuilder builder() {
    return new QueryBuilder();
  }

  @Override
  public List<Message> process(final List<? extends Message> originalList) {
    return process(originalList, type, expression, emptyExpression, null, messageProvider);
  }

  @Override
  public List<Message> process(final List<? extends Message> originalList,
      final Map<String, Object> beans) {
    return process(originalList, type, expression, emptyExpression, beans, messageProvider);
  }

  public List<Message> process(final List<? extends Message> originalList, final Descriptor type,
      final String expression, final String emptyExpression, final Map<String, Object> beans,
      final IDynamicMessageProvider messageProvider) {
    if (originalList == null) {
      throw new NullPointerException();
    } else if (originalList.isEmpty() ? (emptyExpression == null) || emptyExpression.isEmpty()
        : (expression == null) || expression.isEmpty()) {
      return Collections.emptyList();
    }

    final ELProcessor elp = newELProcessor();

    if ((beans != null) && !beans.isEmpty()) {
      for (final Entry<String, Object> entry : beans.entrySet()) {
        elp.defineBean(entry.getKey(), entry.getValue());
      }
    }

    final List<Message> results = new ArrayList<Message>();
    final List<Message> readOnlyResults = Collections.unmodifiableList(results);
    int index = -1;

    if (originalList.isEmpty()) {
      final Object result = elp.eval(emptyExpression);

      if (!collectResult(results, type, index, result, originalList)) {
        return Collections.emptyList();
      }

      return readOnlyResults;
    }

    final List<Message> records = Collections.unmodifiableList(originalList);
    elp.defineBean("records", records);
    elp.defineBean("results", readOnlyResults);
    final ELContext context = elp.getELManager().getELContext();
    final ValueExpression processExpr = getValueExpression(context, expression, Object.class);

    for (final Message message : records) {
      elp.defineBean("record", getBuilder(message, messageProvider));
      elp.defineBean("index", ++index);
      final Object result = processExpr.getValue(context);

      if (!collectResult(results, type, index, result, originalList)) {
        return Collections.emptyList();
      }
    }

    return readOnlyResults;
  }

  private boolean collectResult(final List<Message> results, final Descriptor type,
      final int index, final Object result, final List<? extends Message> originalList) {
    if (result == null) {
      // noop - record is not added to the results, i.e. skipped!
    } else if (result instanceof Iterable) {
      // add all non-null items, properly converted, to the results
      for (final Object element : (Iterable<?>) result) {
        if (!collectSingleResult(results, type, index, element, originalList)) {
          return false;
        }
      }
    } else {
      if (!collectSingleResult(results, type, index, result, originalList)) {
        return false;
      }
    }

    return true;
  }

  private boolean collectSingleResult(final List<Message> results, final Descriptor type,
      final int index, final Object result, final List<? extends Message> originalList) {
    if (result == null) {
      return true;
    } else if (!(result instanceof MessageOrBuilder)) {
      throw new IllegalArgumentException(String.format(
          "result of original record #%s is not a MessageOrBuilder", index));
    } else if (!validationListener.validate(type, originalList.get(index),
        (MessageOrBuilder) result)) {
      throw new IllegalArgumentException(String.format(
          "result of original record #%s is of the wrong message type", index));
    }

    if (result instanceof Message.Builder) {
      results.add(((Message.Builder) result).build());
    } else if (result instanceof Message) {
      results.add((Message) result);
    }

    return notifyResultAdded(index);
  }

  /**
   * Notifies resultListener when the record added.
   *
   * @param index originalList record index
   * @return true - to continue (by default), false - to abort
   */
  protected boolean notifyResultAdded(final int index) {
    return (resultListener == null) ? true : resultListener.resultAdded(index);
  }

  protected ELProcessor newELProcessor() {
    return new ProtoELProcessorEx();
  }

  protected Message.Builder getBuilder(final Message message,
      final IDynamicMessageProvider messageProvider) {
    if (message == null) {
      return null;
    } else if ((message instanceof GeneratedMessage) || (message instanceof IBuilder2)) {
      return message.toBuilder();
    } else {
      return messageProvider.newBuilder(message);
    }
  }

  private ValueExpression getValueExpression(final ELContext context, final String expression,
      final Class<?> expectedType) {
    return ELManager.getExpressionFactory().createValueExpression(context, bracket(expression),
        expectedType);
  }

  private String bracket(final String expression) {
    return "${" + expression + '}';
  }

  /**
   * A query builder and EL 3.0 Language processor with ProtoBuf.
   *
   * @author protobufel@gmail.com David Tesler
   */
  public static final class QueryBuilder implements IQueryProcessor<Message> {
    private Descriptor type = null;
    private String expression = null;
    private String emptyExpression = null;
    private QueryResultListener resultListener = null;
    private ValidationListener validationListener = null;
    private Map<String, Object> beans = null;
    private IDynamicMessageProvider messageProvider = null;

    /**
     * Constructs a default QueryBuilder
     */
    public QueryBuilder() {
      messageProvider = DynamicMessage.getProvider();
    }

    /**
     * Constructs a QueryBuilder based on another QueryBuilder.
     * <p>
     * All the fields of the parameter builder are copied as-is but {@code  beans}, which are not
     * set. This is done to prevent the inadvertent original builder's {@code beans} mutation. If
     * needed, this can be accomplished explicitly via {@link #setBeans(Map)} or
     * {@link #addBean(String, Object)} methods.
     *
     * @param builder a template QueryBuilder to copy from
     */
    public QueryBuilder(final QueryBuilder builder) {
      type = builder.type;
      expression = builder.expression;
      emptyExpression = builder.emptyExpression;
      resultListener = builder.resultListener;
      validationListener = builder.validationListener;
      messageProvider = builder.messageProvider;
      // beans = (builder.beans == null) ? null : new HashMap<String, Object>(builder.beans);
    }

    public QueryBuilder(final ProtoMessageQueryProcessor processor) {
      type = processor.type;
      expression = processor.expression;
      emptyExpression = processor.emptyExpression;
      resultListener = processor.resultListener;
      validationListener = processor.validationListener;
      messageProvider = processor.messageProvider;
    }

    public IDynamicMessageProvider getMessageProvider() {
      return messageProvider;
    }

    public QueryBuilder setMessageProvider(final IDynamicMessageProvider messageProvider) {
      this.messageProvider = messageProvider;
      return this;
    }

    public Map<String, Object> getBeans() {
      return beans;
    }

    /**
     * Sets this QueryBuilder's beans to the shallow copy of the parameter.
     *
     * @param beans a map of a bean name and its value to be copied over
     */
    public QueryBuilder setBeans(final Map<String, Object> beans) {
      this.beans = beans == null ? null : new HashMap<String, Object>(beans);
      return this;
    }

    public QueryBuilder addBean(final String name, final Object bean) {
      if (beans == null) {
        beans = new HashMap<String, Object>(beans);
      }

      beans.put(name, bean);
      return this;
    }

    public Descriptor getType() {
      return type;
    }

    public QueryBuilder setType(final Descriptor type) {
      this.type = type;
      return this;
    }

    public QueryResultListener getResultListener() {
      return resultListener;
    }

    public QueryBuilder setResultListener(final QueryResultListener resultListener) {
      this.resultListener = resultListener;
      return this;
    }

    public ValidationListener getValidationListener() {
      return validationListener;
    }

    public QueryBuilder setValidationListener(final ValidationListener validationListener) {
      this.validationListener = validationListener;
      return this;
    }

    public String getExpression() {
      return expression;
    }

    public QueryBuilder setExpression(final String expression) {
      this.expression = expression;
      return this;
    }

    public String getEmptyExpression() {
      return emptyExpression;
    }

    public QueryBuilder setEmptyExpression(final String emptyExpression) {
      this.emptyExpression = emptyExpression;
      return this;
    }

    public ProtoMessageQueryProcessor build() {
      if (messageProvider == null) {
        throw new NullPointerException();
      }

      final ProtoMessageQueryProcessor processor =
          new ProtoMessageQueryProcessor(expression, emptyExpression, type, resultListener,
              validationListener, messageProvider);
      return processor;
    }

    @Override
    public List<Message> process(final List<? extends Message> originalList) {
      return process(originalList, beans);
    }

    @Override
    public List<Message> process(final List<? extends Message> originalList,
        final Map<String, Object> beans) {
      return getEmptyProcessor().process(originalList, type, expression, emptyExpression, beans,
          messageProvider);
    }
  }

  public static class DefaultValidationListener implements ValidationListener {

    @Override
    public boolean validate(final Descriptor type, final Message original,
        final MessageOrBuilder result) {
      if (type == null) {
        return original == null ? true : original.getDescriptorForType().equals(
            result.getDescriptorForType());
      } else {
        return type.equals(result.getDescriptorForType());
      }
    }
  }
}
