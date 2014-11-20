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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.el.BeanELResolver;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.MethodNotFoundException;

/**
 * A BeanELResolver with advanced handling of the bean's overloaded methods. Consults with the
 * {@code base}'s BeanInfo, if present, to better handle overloading methods' ambiguity - the
 * BeanInfo declared methods are in preference to the rest.
 *
 * @author protobufel@gmail.com David Tesler
 */
public class BeanELResolverEx extends BeanELResolver {
  protected static final Comparator<Method> DEFAULT_SUITABLE_METHOD_COMPARATOR =
      new SuitableMethodComparator();
  private final Comparator<Method> suitableMethodComparator;

  public BeanELResolverEx() {
    suitableMethodComparator = DEFAULT_SUITABLE_METHOD_COMPARATOR;
  }

  public BeanELResolverEx(final Comparator<Method> suitableMethodComparator) {
    this.suitableMethodComparator = suitableMethodComparator;
  }

  /**
   * Invokes the method considering the base's BeanInfo class' MethodDescriptors. Resolves method
   * overloads by looking at the MethodDescriptors first and only then the base's class itself,
   * choosing the most specific candidate.
   *
   */
  @Override
  public Object invoke(final ELContext context, final Object base, final Object method,
      Class<?>[] paramTypes, final Object[] params) {
    if ((base == null) || (method == null)) {
      return null;
    }

    final MethodDescriptor[] methodDescriptors = getMethodDescriptors(context, base);

    if (methodDescriptors != null) {
      final Method m =
          findMethodOrThrow(method.toString(), paramTypes, params, false, methodDescriptors);
      paramTypes = m.getParameterTypes();
    }

    return super.invoke(context, base, method, paramTypes, params);
  }

  public Object invokeOnInterface(final ELContext context, final Class<?> baseInterface,
      final Object base, final Object method, final Class<?>[] paramTypes, final Object[] params) {
    if ((base == null) || (method == null) || (baseInterface == null)) {
      return null;
    }

    final Method m = findMethodOrThrow(baseInterface, method.toString(), paramTypes, params, false);
    return invoke(context, base, m, params);
  }

  public Object invokeIfBeanInfoOnly(final ELContext context, final Object base,
      final Object method, final Class<?>[] paramTypes, final Object[] params) {
    if ((base == null) || (method == null)) {
      return null;
    }

    final Method m =
        findMethodOrThrow(method.toString(), paramTypes, params, false,
            getMethodDescriptors(context, base));
    return super.invoke(context, base, method, m.getParameterTypes(), params);
  }

  public Object invokeIfBeanInfoOnly(final ELContext context, final Object base,
      final Object method, final Class<?>[] paramTypes, final Object[] params,
      final Map<String, List<MethodDescriptor>> methodDescriptors) {
    if ((base == null) || (method == null)) {
      return null;
    }

    final Method m =
        findMethodOrThrow(method.toString(), paramTypes, params, false, methodDescriptors);
    return super.invoke(context, base, method, m.getParameterTypes(), params);
  }

  protected Method findMethodOrThrow(final String methodName, final Class<?>[] paramTypes,
      final Object[] params, final boolean staticOnly, final MethodDescriptor[] methodDescriptors) {
    final Method method = findMethod(methodName, paramTypes, params, staticOnly, methodDescriptors);

    if (method == null) {
      throw new MethodNotFoundException("Method " + methodName + "for bean class "
          + "not found or accessible");
    }

    return method;
  }

  protected Method findMethodOrThrow(final String methodName, final Class<?>[] paramTypes,
      final Object[] params, final boolean staticOnly,
      final Map<String, List<MethodDescriptor>> methodDescriptors) {
    final Method method = findMethod(methodName, paramTypes, params, staticOnly, methodDescriptors);

    if (method == null) {
      throw new MethodNotFoundException("Method " + methodName + " for bean class "
          + "not found or accessible");
    }

    return method;
  }

  protected Method findMethodOrThrow(final Class<?> klass, final String methodName,
      final Class<?>[] paramTypes, final Object[] params, final boolean staticOnly) {
    final Method method = findMethod(klass, methodName, paramTypes, params, staticOnly);

    if (method == null) {
      throw new MethodNotFoundException("Method " + methodName + "for class " + klass
          + " not found or accessible");
    }

    return method;
  }

  protected Method findMethod(final Class<?> klass, String methodName, final Class<?>[] paramTypes,
      final Object[] params, final boolean staticOnly) {
    Method candidate = null;
    methodName = methodName.intern();

    if (paramTypes != null) {
      try {
        final Method method = klass.getMethod(methodName, paramTypes);

        if (isMethodValid(method, staticOnly)) {
          return method;
        }
      } catch (final NoSuchMethodException ex) {
      }
    } else {
      for (final Method method : klass.getMethods()) {
        if (isMethodCandidate(method, methodName, params, staticOnly, false)) {
          if ((candidate == null) || (suitableMethodComparator.compare(method, candidate) < 0)) {
            candidate = method;
          }
        }
      }
    }

    return candidate;
  }

  protected Method findMethod(final String methodName, final Class<?>[] paramTypes,
      final Object[] params, final boolean staticOnly, final MethodDescriptor[] methodDescriptors) {
    final int paramCount =
        paramTypes == null ? params == null ? 0 : params.length : paramTypes.length;
    return findBeanInfoCandidate(paramCount, methodName.intern(), methodDescriptors, staticOnly);
  }

  protected Method findMethod(final String methodName, final Class<?>[] paramTypes,
      final Object[] params, final boolean staticOnly,
      final Map<String, List<MethodDescriptor>> methodDescriptors) {
    final int paramCount =
        paramTypes == null ? params == null ? 0 : params.length : paramTypes.length;
    return findBeanInfoCandidate(paramCount, methodName.intern(), methodDescriptors, staticOnly);
  }

  protected <T> Method findBeanInfoCandidate(final int paramCount, final String methodName,
      final MethodDescriptor[] methodDescriptors, final boolean staticOnly) {
    for (final MethodDescriptor mDescriptor : methodDescriptors) {
      if (mDescriptor.getName().equals(methodName)) {
        final Method method = mDescriptor.getMethod();

        if (isMethodValid(method, staticOnly)
            && ((method.isVarArgs() && ((method.getParameterTypes().length - paramCount) <= 1)) || (method
                .getParameterTypes().length == paramCount))) {
          return method;
        }
      }
    }

    return null;
  }

  protected <T> Method findBeanInfoCandidate(final int paramCount, final String methodName,
      final Map<String, List<MethodDescriptor>> methodDescriptors, final boolean staticOnly) {
    for (final MethodDescriptor mDescriptor : methodDescriptors.get(methodName)) {
      if (mDescriptor.getName().equals(methodName)) {
        final Method method = mDescriptor.getMethod();

        if (isMethodValid(method, staticOnly)
            && ((method.isVarArgs() && ((method.getParameterTypes().length - paramCount) <= 1)) || (method
                .getParameterTypes().length == paramCount))) {
          return method;
        }
      }
    }

    return null;
  }

  public static enum StaticMethodCompareStrategy {
    STATIC_IS_MORE(true, true, false), STATIC_IS_LESS(false, true, false), STATIC_IS_MORE_IF_EQUAL(
        true, false, true), STATIC_IS_LESS_IF_EQUAL(false, false, true), STATIC_IGNORED(false,
        false, false);

    private boolean checkBefore;
    private boolean checkAfter;
    private int signMore;

    private StaticMethodCompareStrategy(final boolean isMore, final boolean checkBefore,
        final boolean checkAfter) {
      signMore = isMore ? 1 : -1;
      this.checkBefore = checkBefore;
      this.checkAfter = checkAfter;
    }

    public int compareBeforeParameters(final Method method1, final Method method2) {
      return checkBefore ? signMore
          * Boolean.compare(Modifier.isStatic(method1.getModifiers()),
              Modifier.isStatic(method2.getModifiers())) : 0;
    }

    public int compareAfterParameters(final Method method1, final Method method2) {
      return checkAfter ? signMore
          * Boolean.compare(Modifier.isStatic(method1.getModifiers()),
              Modifier.isStatic(method2.getModifiers())) : 0;
    }
  }

  public static class SuitableMethodComparator implements Comparator<Method> {
    private final StaticMethodCompareStrategy staticStrategy;

    public SuitableMethodComparator() {
      staticStrategy = StaticMethodCompareStrategy.STATIC_IS_MORE;
    }

    public SuitableMethodComparator(final StaticMethodCompareStrategy staticStrategy) {
      this.staticStrategy = staticStrategy;
    }

    @Override
    public int compare(final Method method1, final Method method2) {
      switch (staticStrategy.compareBeforeParameters(method1, method2)) {
        case -1:
          return -1;
        case 1:
          return 1;
      }

      final Class<?> class1 = method1.getDeclaringClass();
      final Class<?> class2 = method2.getDeclaringClass();

      if (class1.equals(class2)) {
        final Class<?>[] params1 = method1.getParameterTypes();
        final Class<?>[] params2 = method2.getParameterTypes();

        int i = 0;

        for (final Class<?> pClass1 : params1) {
          final Class<?> pClass2 = params2[i++];

          if (!pClass1.equals(pClass2)) {
            return pClass1.isAssignableFrom(pClass2) ? 1 : -1;
          }
        }

        return staticStrategy.compareAfterParameters(method1, method2);
      }

      return class1.isAssignableFrom(class2) ? 1 : -1;
    }
  }

  protected <T> boolean isMethodCandidate(final Method method, final String name, final T[] params,
      final boolean staticOnly, final boolean isParamTypes) {
    if (!(method.getName().equals(name) && isMethodValid(method, staticOnly))) {
      return false;
    }

    final Class<?>[] mParamTypes = method.getParameterTypes();

    if ((params == null) || (params.length == 0)) {
      return method.isVarArgs() ? mParamTypes.length <= 1 : mParamTypes.length == 0;
    } else if (method.isVarArgs() ? (mParamTypes.length - params.length) > 1
        : mParamTypes.length != params.length) {
      return false;
    }

    int i = 0;

    for (final Class<?> pClass : mParamTypes) {
      final Object param = params[i++];

      if (isParamTypes) {
        if (!pClass.isAssignableFrom((Class<?>) param)) {
          return false;
        }
      } else {
        if (!(param instanceof javax.el.LambdaExpression) && !pClass.isInstance(param)) {
          return false;
        }
      }
    }

    return true;
  }

  private boolean isMethodValid(final Method m, final boolean staticOnly) {
    final int mod = m.getModifiers();
    return Modifier.isPublic(mod) && (!staticOnly || Modifier.isStatic(mod));
  }

  public String[] addBeanInfoSearchPath(final String[] additionalBeanInfoPath) {
    final String[] beanInfoSearchPath = Introspector.getBeanInfoSearchPath();

    final List<String> list =
        new ArrayList<String>(beanInfoSearchPath.length + additionalBeanInfoPath.length);
    Collections.addAll(list, beanInfoSearchPath);
    Collections.addAll(list, additionalBeanInfoPath);

    Introspector.setBeanInfoSearchPath(list.toArray(new String[0]));
    return beanInfoSearchPath;
  }

  public BeanInfo getBeanInfo(final Object base, final String... additionalBeanInfoPath) {
    if (base == null) {
      return null;
    }

    BeanInfo info = null;
    String[] beanInfoSearchPath = null;

    try {
      if (additionalBeanInfoPath != null) {
        beanInfoSearchPath = addBeanInfoSearchPath(additionalBeanInfoPath);
      }

      info = Introspector.getBeanInfo(base instanceof Class ? (Class<?>) base : base.getClass());
    } catch (final Exception ex) {
    } finally {
      if (beanInfoSearchPath != null) {
        Introspector.setBeanInfoSearchPath(beanInfoSearchPath);
      }
    }

    return info;
  }

  public MethodDescriptor[] getMethodDescriptors(final ELContext context, final Object base,
      final String... additionalBeanInfoPath) {
    final BeanInfo info = getBeanInfo(base, additionalBeanInfoPath);

    if (info == null) {
      return null;
    }

    return info.getMethodDescriptors();
  }

  private Object invoke(final ELContext context, final Object base, final Method method,
      final Object[] params) {
    if ((base == null) || (method == null) || (method == null)) {
      return null;
    }

    Method baseMethod = method;

    if (!method.getDeclaringClass().isAssignableFrom(base.getClass())) {
      try {
        baseMethod = base.getClass().getMethod(method.getName(), method.getParameterTypes());
      } catch (final Throwable e) {
        throw new ELException(e);
      }
    }

    for (final Object p : params) {
      // If the parameters is a LambdaExpression, set the ELContext
      // for its evaluation
      if (p instanceof javax.el.LambdaExpression) {
        ((javax.el.LambdaExpression) p).setELContext(context);
      }
    }

    final Object ret = ELUtil.invokeMethod(context, baseMethod, base, params);
    context.setPropertyResolved(base, method.getName());
    return ret;
  }
}
