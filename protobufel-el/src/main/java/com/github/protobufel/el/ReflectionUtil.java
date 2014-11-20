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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * EL Reflection Utilities.
 *
 * @author protobufel@gmail.com David Tesler
 */
public final class ReflectionUtil {
  private ReflectionUtil() {}

  public interface Invocable {
    Object invoke(String methodName, Object... args);
  }

  public interface IDecorator<T, I> {
    IDecorator<T, I> newInstance();

    void setDelegate(T delegate);

    I get();
  }

  public static <T, I, E extends IDecorator<T, I>> Object getWrapper(final T original,
      final Class<I> interfaceClass, final E interfaceImpl, final boolean implementInvokable) {
    final Class<?>[] interfaces = getAllInterfaces(original, implementInvokable, interfaceClass);
    final Class<?> proxyClass = Proxy.getProxyClass(interfaceClass.getClassLoader(), interfaces);

    try {
      final Constructor<?> proxyConstructor =
          proxyClass.getConstructor(new Class[] {InvocationHandler.class});
      return proxyConstructor.newInstance(newInvocationHandler(original, interfaceClass,
          interfaceImpl.newInstance()));
    } catch (final Throwable e) {
      throw new RuntimeException(e);
    }
  }

  private static <T, I, E extends IDecorator<T, I>> InvocationHandler newInvocationHandler(
      final T original, final Class<I> interfaceClass, final E interfaceImpl) {
    interfaceImpl.setDelegate(original);
    return new InvocationHandler() {
      @Override
      public Object invoke(final Object proxy, final Method method, final Object[] args)
          throws Throwable {
        if (method.getDeclaringClass() == Invocable.class) {
          final Object[] invokeArgs = (Object[]) args[1];
          final Class<?>[] paramTypes = new Class<?>[invokeArgs.length - 1];

          for (int i = 1; i < invokeArgs.length; i++) {
            paramTypes[i] = invokeArgs[i].getClass();
          }

          return original.getClass().getMethod((String) args[0], paramTypes)
              .invoke(original, invokeArgs);
        } else if (method.getDeclaringClass() == interfaceClass) {
          return method.invoke(interfaceImpl.get(), args);
        } else {
          return method.invoke(proxy, args);
        }
      }
    };
  }

  private static <T, I, E extends IDecorator<T, I>> InvocationHandler newInvocationHandler2(
      final T original, final Class<I> interfaceClass, final E interfaceImpl) {
    interfaceImpl.setDelegate(original);
    return new DecoratorInvocationHandler<T, I, E>(interfaceClass, interfaceImpl);
  }

  private static class DecoratorInvocationHandler<T, I, E extends IDecorator<T, I>> implements
      InvocationHandler {
    private final Class<I> interfaceClass;
    private final E interfaceImpl;

    private DecoratorInvocationHandler(final Class<I> interfaceClass, final E interfaceImpl) {
      this.interfaceClass = interfaceClass;
      this.interfaceImpl = interfaceImpl;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args)
        throws Throwable {
      if (method.getDeclaringClass() == Invocable.class) {
        final Object[] invokeArgs = (Object[]) args[1];
        final Class<?>[] paramTypes = new Class<?>[invokeArgs.length - 1];

        for (int i = 1; i < invokeArgs.length; i++) {
          paramTypes[i] = invokeArgs[i].getClass();
        }

        return proxy.getClass().getMethod((String) args[0], paramTypes).invoke(proxy, invokeArgs);
      } else if (method.getDeclaringClass() == interfaceClass) {
        return method.invoke(interfaceImpl.get(), args);
      } else {
        return method.invoke(proxy, args);
      }
    }
  }

  public interface ConditionalInvocationHandler extends InvocationHandler {
    boolean isApplicable(Object proxy, Method method, Object[] args);
  }

  private static class InvocationHandlerDecorator<T> implements InvocationHandler {
    private final T original;
    private final InvocationHandler handler;

    public InvocationHandlerDecorator(final T original, final InvocationHandler handler) {
      this.original = original;
      this.handler = handler;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args)
        throws Throwable {
      if (!method.getDeclaringClass().isAssignableFrom(original.getClass())
          || ((handler instanceof ConditionalInvocationHandler) && ((ConditionalInvocationHandler) handler)
              .isApplicable(original, method, args))) {
        return handler.invoke(original, method, args);
      } else {
        return method.invoke(original, args);
      }
    }
  }

  private static Class<?>[] getAllInterfaces(final Object object, final boolean implementInvokable,
      final Class<?>... extraInterfaces) {
    final Class<?> clazz = object == null ? Object.class : object.getClass();
    Class<?>[] result;
    final int addInvokable = implementInvokable ? 1 : 0;

    if (clazz.isInterface()) {
      result = new Class<?>[extraInterfaces.length + 1 + addInvokable];
      System.arraycopy(extraInterfaces, 0, result, 1, extraInterfaces.length);
      result[0] = clazz;
    } else {
      final Class<?>[] interfaces = clazz.getInterfaces();
      result = new Class<?>[extraInterfaces.length + interfaces.length + addInvokable];
      System.arraycopy(interfaces, 0, result, 0, interfaces.length);
      System.arraycopy(extraInterfaces, 0, result, interfaces.length, extraInterfaces.length);
    }

    if (implementInvokable) {
      result[result.length - 1] = Invocable.class;
    }

    return result;
  }
}
