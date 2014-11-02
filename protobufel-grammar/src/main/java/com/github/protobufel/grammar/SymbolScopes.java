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

package com.github.protobufel.grammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.github.protobufel.grammar.ProtoFileParser.ContextLookup;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type;
import com.google.protobuf.Message;

/**
 * Classes for {@link ProtoFileParser}'s symbol scope processing, including some validation.
 *   
 * @author protobufel@gmail.com David Tesler
 */
final class SymbolScopes {
  // FIXME tighten visibility up
  private static final Pattern NAME_SPLITTER = Pattern.compile("\\.");
  private final List<Symbol<? extends Message.Builder>> unresolved;
  private RootScope globalScope;
  private SymbolScope currentScope;
  private boolean validateFullNames = false;

  public SymbolScopes() {
    unresolved = new ArrayList<Symbol<? extends Message.Builder>>();
  }

  public void initGlobalScope(final String packageName) {
    if (globalScope != null) {
      throw new IllegalStateException("already initialized");
    }

    globalScope = new RootScope(packageName);
    currentScope = globalScope;
  }

  public boolean isValidateFullNames() {
    return validateFullNames;
  }

  public void setValidateFullNames(final boolean validateFullNames) {
    this.validateFullNames = validateFullNames;
  }

  public SymbolScope popScope() {
    currentScope = currentScope.getParent();
    return currentScope;
  }

  public SymbolScope getCurrentScope() {
    return currentScope;
  }

  public SymbolScope pushScope(final String scopeName) {
    currentScope = currentScope.addChild(scopeName);
    return currentScope;
  }

  /**
   * Adds a leaf scope, which doesn't change the current scope. No popScope() needed after leaving
   * this element.
   *
   * @param scopeName
   * @return the unchanged current scope
   */
  public SymbolScope addLeaf(final String scopeName) {
    currentScope.addLeaf(scopeName);
    return currentScope;
  }

  public boolean addIfUnresolved(final FieldDescriptorProto.Builder protoBuilder) {
    if (!isUnresolved(protoBuilder)) {
      return false;
    }

    return unresolved.add(new FieldDescriptorProtoSymbol(protoBuilder, currentScope));
  }

  private boolean isUnresolved(final FieldDescriptorProto.Builder protoBuilder) {
    return protoBuilder.hasExtendee() && !protoBuilder.getExtendee().startsWith(".")
        || protoBuilder.hasTypeName() && !protoBuilder.getTypeName().startsWith(".");
  }

  public List<? extends Message.Builder> resolveAllSymbols(final ContextLookup contextLookup) {
    return resolveAllSymbols(unresolved, contextLookup);
  }

  public List<? extends Message.Builder> resolveAllSymbols(
      final List<Symbol<? extends Message.Builder>> unresolved, final ContextLookup contextLookup) {
    if (unresolved.isEmpty()) {
      return Collections.emptyList();
    }

    // unresolved is scope-consecutive, so cache resolved names for the same SymbolScope
    // and release them after leaving the scope! It's easy, fast, and memory-lite!
    // In other words, cache/uncache the resolved names as you loop.
    final Map<String, NameContext> resolvedSymbols = new HashMap<String, NameContext>();
    SymbolScope activeScope = null;

    for (final Iterator<Symbol<? extends Message.Builder>> iterator = unresolved.iterator(); iterator
        .hasNext();) {
      final Symbol<? extends Message.Builder> symbol = iterator.next();

      if (symbol.getScope() != activeScope) {
        activeScope = symbol.getScope();
        resolvedSymbols.clear();
      }

      if (symbol.resolveSymbol(resolvedSymbols)) {
        iterator.remove();
      }
    }

    if (unresolved.isEmpty()) {
      return Collections.emptyList();
    }

    final List<Message.Builder> result = new ArrayList<Message.Builder>();

    for (final Symbol<? extends Message.Builder> symbol : unresolved) {
      result.add(symbol.getProtoBuilder());
    }

    return result;
  }

  public class SymbolScope {
    private final String name;
    final private SymbolScope parent;
    /* lazy field; is null for leaf nodes */
    private Map<String, SymbolScope> children = null;

    public SymbolScope(final String name, final SymbolScope parent) {
      if (name == null || parent == null) {
        throw new NullPointerException();
      }

      this.name = name;
      this.parent = parent;
    }

    protected SymbolScope(final String name) {
      this.name = name;
      parent = null;
    }

    public boolean isLeaf() {
      return false;
    }

    public boolean isEmpty() {
      return children == null || children.isEmpty();
    }

    protected Map<String, SymbolScope> getChildren() {
      if (children == null) {
        children = new HashMap<String, SymbolScope>();
      }

      return children;
    }

    protected SymbolScope getRoot() {
      if (globalScope != null) {
        return globalScope;
      }

      SymbolScope root = this;

      for (SymbolScope parent = this; parent != null; parent = root.parent) {
        root = parent;
      }

      return root;
    }

    public SymbolScope addChild(final String scopeName) {
      final SymbolScope child = new SymbolScope(scopeName, this);
      getChildren().put(scopeName, child);
      return child;
    }

    protected SymbolScope addChild(final SymbolScope symbolScope) {
      getChildren().put(symbolScope.name, symbolScope);
      return symbolScope;
    }

    public SymbolScope addLeaf(final String scopeName) {
      final SymbolScope child = new LeafSymbolScope(scopeName, this);
      getChildren().put(scopeName, child);
      return child;
    }

    protected String getName() {
      return name;
    }

    protected SymbolScope getParent() {
      return parent;
    }

    public SymbolScope findChild(final String scopeName) {
      return children == null ? null : children.get(scopeName);
    }

    public NameContext getFullName(final String name) {
      if (name.startsWith(".")) {
        return validateFullNames ? validateFullName(name) : NameContext.newUnresolvedInstance(name);
      }

      final String[] nameParts = NAME_SPLITTER.split(name);

      for (SymbolScope parent = this; parent != null; parent = parent.getParent()) {
        SymbolScope child = parent;

        for (final String part : nameParts) {
          child = child.findChild(part);

          if (child == null) {
            break;
          }
        }

        if (child != null) {
          // the path is found
          final StringBuilder sb = new StringBuilder();

          for (SymbolScope scope = child; scope != null; scope = scope.getParent()) {
            sb.insert(0, "." + scope.getName());
          }

          return NameContext.newResolvedInstance(sb.toString(), child.isLeaf());
        }
      }

      return NameContext.emptyInstance();
    }

    public NameContext getFullName(final String name, final Map<String, NameContext> cache) {
      if (cache == null) {
        return getFullName(name);
      }

      NameContext fullName = cache.get(name);

      if (fullName != null) {
        return fullName;
      }

      fullName = getFullName(name);
      cache.put(name, fullName);
      return fullName;
    }


    public NameContext validateFullName(final String fullName) {
      SymbolScope child = parent;
      final String noDotName = fullName.startsWith(".") ? fullName.substring(1) : fullName;
      final String[] nameParts = NAME_SPLITTER.split(noDotName);

      for (final String part : nameParts) {
        child = child.findChild(part);

        if (child == null) {
          return NameContext.emptyInstance();
        }
      }

      return NameContext.newResolvedInstance("." + noDotName, child.isLeaf());
    }
  }

  public class RootScope extends SymbolScope {

    public RootScope(final String packageName) {
      super(packageName);
    }
  }

  public class LeafSymbolScope extends SymbolScope {

    private static final String NOT_SUPPORTED_ON_LEAF_SCOPE = "not supported on leaf scope";

    public LeafSymbolScope(final String name, final SymbolScope parent) {
      super(name, parent);
    }

    @Override
    protected Map<String, SymbolScope> getChildren() {
      return Collections.emptyMap();
    }

    @Override
    public SymbolScope addChild(final String scopeName) {
      throw new UnsupportedOperationException(NOT_SUPPORTED_ON_LEAF_SCOPE);
    }

    @Override
    protected SymbolScope addChild(final SymbolScope symbolScope) {
      throw new UnsupportedOperationException(NOT_SUPPORTED_ON_LEAF_SCOPE);
    }

    @Override
    public SymbolScope addLeaf(final String scopeName) {
      throw new UnsupportedOperationException(NOT_SUPPORTED_ON_LEAF_SCOPE);
    }

    @Override
    public SymbolScope findChild(final String scopeName) {
      return null;
    }

    @Override
    public boolean isLeaf() {
      return true;
    }

    @Override
    public boolean isEmpty() {
      return true;
    }
  }

  public static abstract class Symbol<T extends Message.Builder> {
    protected final T payload;
    protected final SymbolScope scope;

    public abstract boolean resolveSymbol(Map<String, NameContext> cache);

    public abstract List<String> getUnresolvedInfo();

    public abstract void reportUnresolvedTypeNameError(ContextLookup contextLookup, boolean removeMe);

    public Symbol(final T payload, final SymbolScope scope) {
      this.payload = payload;
      this.scope = scope;
    }

    public T getProtoBuilder() {
      return payload;
    }

    public SymbolScope getScope() {
      return scope;
    }
  }

  public static class FieldDescriptorProtoSymbol extends Symbol<FieldDescriptorProto.Builder> {

    public FieldDescriptorProtoSymbol(final FieldDescriptorProto.Builder protoBuilder,
        final SymbolScope scope) {
      super(protoBuilder, scope);
    }

    @Override
    public boolean resolveSymbol(final Map<String, NameContext> cache) {
      if (payload.hasExtendee()) {
        final NameContext fullName = scope.getFullName(payload.getExtendee(), cache);

        if (fullName.isEmpty()) {
          return false;
        }

        payload.setExtendee(fullName.getName());
      }

      if (payload.hasTypeName()) {
        final NameContext fullName = scope.getFullName(payload.getTypeName(), cache);

        if (fullName.isEmpty()) {
          return false;
        }


        if (fullName.isResolved()) {
          payload.setTypeName(fullName.getName());

          if (fullName.isLeaf()) {
            payload.setType(Type.TYPE_ENUM);
          } else {
            payload.setType(Type.TYPE_MESSAGE);
          }
        }
      }

      return true;
    }

    @Override
    public List<String> getUnresolvedInfo() {
      final List<String> unresolvedProps = new ArrayList<String>();

      if (payload.hasExtendee() && !payload.getExtendee().startsWith(".")) {
        unresolvedProps.add("field " + payload.getName() + "'s extendee property: '"
            + payload.getExtendee() + "'");
      }

      if (payload.hasTypeName() && !payload.getTypeName().startsWith(".")) {
        unresolvedProps.add("field " + payload.getName() + "'s typeName property: '"
            + payload.getTypeName() + "'");
      }

      return unresolvedProps;
    }

    @Override
    public void reportUnresolvedTypeNameError(final ContextLookup contextLookup,
        final boolean removeMe) {
      contextLookup.reportUnresolvedTypeNameError(getProtoBuilder(), getUnresolvedInfo(), removeMe);
    }
  }

  public static final class NameContext {
    private static final NameContext EMPTY = new NameContext("", false, true);
    private final String name;
    private final Boolean isLeaf;

    private NameContext(final String name, final boolean isResolved, final boolean isLeaf) {
      if (name == null || isResolved && !name.startsWith(".")) {
        throw new IllegalArgumentException();
      }

      this.name = name;
      this.isLeaf = isResolved ? isLeaf : null;
    }

    public static NameContext emptyInstance() {
      return EMPTY;
    }

    public static NameContext newUnresolvedInstance(final String name) {
      return new NameContext(name, false, true);
    }

    public static NameContext newResolvedInstance(final String name, final boolean isLeaf) {
      return new NameContext(name, true, isLeaf);
    }

    public boolean isEmpty() {
      return name.isEmpty();
    }

    public String getName() {
      return name;
    }

    public boolean isResolved() {
      return isLeaf != null;
    }

    public boolean isLeaf() {
      return isLeaf == null || isLeaf;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (isLeaf == null ? 0 : isLeaf.hashCode());
      result = prime * result + (name == null ? 0 : name.hashCode());
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
      if (!(obj instanceof NameContext)) {
        return false;
      }
      final NameContext other = (NameContext) obj;
      if (isLeaf == null) {
        if (other.isLeaf != null) {
          return false;
        }
      } else if (!isLeaf.equals(other.isLeaf)) {
        return false;
      }
      if (name == null) {
        if (other.name != null) {
          return false;
        }
      } else if (!name.equals(other.name)) {
        return false;
      }
      return true;
    }
  }
}
