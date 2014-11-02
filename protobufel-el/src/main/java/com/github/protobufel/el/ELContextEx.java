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

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ResourceBundleELResolver;
import javax.el.StandardELContext;
import javax.el.StaticFieldELResolver;
import javax.el.VariableMapper;

/**
 * A customized ELContext to be set on {@link StandardELContext}. Replaces the standard
 * {@link BeanELResolver} with the more advanced {@link BeanELResolverEx}.
 * <p>
 * <strong>Usage</strong>: replace the standard {@link StandardELContext} with
 * {@code new StandardELContext(new ELContextEx())}, or via
 * {@code ELManager.setELContext(new ELContextEx())}.
 *
 * @author protobufel@gmail.com David Tesler
 */
public class ELContextEx extends ELContext {
  private final ExpressionFactory factory;
  private ELResolver elResolver;
  private FunctionMapper functionMapper;
  private VariableMapper variableMapper;
  private CompositeELResolver customResolver;

  public ELContextEx(final ExpressionFactory factory) {
    this.factory = factory;
  }

  @Override
  public ELResolver getELResolver() {
    if (elResolver == null) {
      final CompositeELResolver resolver = new CompositeELResolver();
      customResolver = new CompositeELResolver();
      resolver.add(customResolver);
      resolver.add(factory.getStreamELResolver());
      resolver.add(new StaticFieldELResolver());
      resolver.add(new MapELResolver());
      resolver.add(new ResourceBundleELResolver());
      resolver.add(new ListELResolver());
      resolver.add(new ArrayELResolver());
      resolver.add(new BeanELResolverEx());
      elResolver = resolver;
    }

    return elResolver;
  }

  public void addELResolver(final ELResolver resolver) {
    getELResolver();
    customResolver.add(resolver);
  }

  @Override
  public FunctionMapper getFunctionMapper() {
    if (functionMapper == null) {
      init();
    }

    return functionMapper;
  }

  @Override
  public VariableMapper getVariableMapper() {
    if (variableMapper == null) {
      init();
    }

    return variableMapper;
  }

  private void init() {
    final StandardELContext context = new StandardELContext(factory);
    functionMapper = context.getFunctionMapper();
    variableMapper = context.getVariableMapper();
  }
}
