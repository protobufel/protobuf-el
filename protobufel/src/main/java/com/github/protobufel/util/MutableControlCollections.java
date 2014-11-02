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

package com.github.protobufel.util;

import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import java.util.concurrent.atomic.AtomicBoolean;

final class MutableControlCollections {
  private MutableControlCollections() {}

  public static final <E> List<E> mutableControlList(final List<E> delegate,
      final Immutable immutable) {
    if (delegate instanceof RandomAccess) {
      return new MutableControlRandomAccessList<E>(delegate, immutable);
    } else {
      return new MutableControlList<E>(delegate, immutable);
    }
  }

  public interface Immutable {
    boolean isImmutable();
  }

  public interface IImmutableState extends Immutable {
    boolean makeImmutable();
  }

  public static class ImmutableStates {
    public static final class BooleanImmutableState implements IImmutableState, Serializable {
      private static final long serialVersionUID = 553811341266855092L;
      private final AtomicBoolean isImmutable;

      public BooleanImmutableState(final boolean isImmutable) {
        this.isImmutable = new AtomicBoolean(isImmutable);
      }

      @Override
      public boolean isImmutable() {
        return isImmutable.get();
      }

      @Override
      public boolean makeImmutable() {
        return isImmutable.compareAndSet(false, true);
      }
    }

    public static final class ReferenceImmutableState implements IImmutableState, Serializable {
      private static final long serialVersionUID = 553811341266855092L;
      /**
       * @serialField isImmutable Boolean the immutable status of this object
       */
      private static final ObjectStreamField[] serialPersistentFields = {new ObjectStreamField(
          "isImmutable", Boolean.class)};
      // private final AtomicBoolean isImmutable;
      private WeakReference<IImmutableState> reference;

      public ReferenceImmutableState(final IImmutableState reference) {
        this.reference = new WeakReference<IImmutableState>(reference);
        // this.isImmutable = new AtomicBoolean(reference.isImmutable());
      }

      @Override
      public boolean isImmutable() {
        if (reference == null) {
          return true;
        }

        IImmutableState immutableState = reference.get();

        if (immutableState == null) {
          immutableState = null;
          return true;
        }

        return immutableState.isImmutable();
      }

      @Override
      public boolean makeImmutable() {
        throw new UnsupportedOperationException();
      }

      private void writeObject(final ObjectOutputStream out) throws java.io.IOException {
        final PutField fields = out.putFields();
        fields.put("isImmutable", isImmutable());
        out.writeFields();
      }

      /**
       * Restores the object to the always immutable state!
       */
      private void readObject(final java.io.ObjectInputStream in) throws java.io.IOException,
          ClassNotFoundException {
        final GetField fields = in.readFields();
        // ignore it, as we always deserialize this object to immutable state
        fields.get("isImmutable", true);
        // make sure the object is immutable!
        reference = null;
      }
    }
  }

  private static class MutableControlList<E> implements List<E>, Immutable, java.io.Serializable {
    private static final long serialVersionUID = -2643387676083235458L;
    private static final ObjectStreamField[] serialPersistentFields = {
      new ObjectStreamField("delegate", List.class),
        new ObjectStreamField("isImmutable", Boolean.class)};
    final List<E> delegate;
    private final Immutable immutable;

    public MutableControlList(final List<E> delegate, final Immutable immutable) {
      this.delegate = delegate;
      this.immutable = immutable;
    }

    /**
     * Return the mutability status of this list. Sublasses should override this method, as it
     * defaults to mutable.
     */
    @Override
    public boolean isImmutable() {
      return immutable == null || immutable.isImmutable();
    }

    protected Immutable getImmutable() {
      return immutable;
    }

    private void verifyIsMutable() {
      if (isImmutable()) {
        throw new UnsupportedOperationException();
      }
    }

    @Override
    public int size() {
      return delegate.size();
    }

    @Override
    public boolean isEmpty() {
      return delegate.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
      return delegate.contains(o);
    }

    @Override
    public Object[] toArray() {
      return delegate.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
      return delegate.toArray(a);
    }

    @Override
    public boolean add(final E e) {
      verifyIsMutable();
      return delegate.add(e);
    }

    @Override
    public boolean remove(final Object o) {
      verifyIsMutable();
      return delegate.remove(o);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
      return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
      verifyIsMutable();
      return delegate.addAll(c);
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends E> c) {
      verifyIsMutable();
      return delegate.addAll(index, c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
      verifyIsMutable();
      return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
      verifyIsMutable();
      return delegate.retainAll(c);
    }

    @Override
    public void clear() {
      verifyIsMutable();
      delegate.clear();
    }

    @Override
    public boolean equals(final Object o) {
      // TODO(protobufel): add canEqual()!
      // TODO(protobufel): should I add boolean isImmutable()?
      return this == o || delegate.equals(o);
    }

    @Override
    public int hashCode() {
      // TODO(protobufel): should this be a special case when isImmutable field/value irrelevant?
      // TODO(protobufel): should I add boolean isImmutable()?
      return delegate.hashCode();
    }

    @Override
    public E get(final int index) {
      return delegate.get(index);
    }

    @Override
    public E set(final int index, final E element) {
      verifyIsMutable();
      return delegate.set(index, element);
    }

    @Override
    public void add(final int index, final E element) {
      verifyIsMutable();
      delegate.add(index, element);
    }

    @Override
    public E remove(final int index) {
      verifyIsMutable();
      return delegate.remove(index);
    }

    @Override
    public int indexOf(final Object o) {
      return delegate.indexOf(o);
    }

    @Override
    public int lastIndexOf(final Object o) {
      return delegate.lastIndexOf(o);
    }

    @Override
    public Iterator<E> iterator() {
      return new Itr(delegate.iterator());
    }

    @Override
    public ListIterator<E> listIterator() {
      return new ListItr(delegate.listIterator());
    }

    @Override
    public ListIterator<E> listIterator(final int index) {
      return new ListItr(delegate.listIterator(index));
    }

    @Override
    public List<E> subList(final int fromIndex, final int toIndex) {
      return new MutableControlList<E>(delegate.subList(fromIndex, toIndex), immutable);
    }

    private class Itr implements Iterator<E> {
      private final Iterator<E> it;

      public Itr(final Iterator<E> it) {
        this.it = it;
      }

      @Override
      public boolean hasNext() {
        return it.hasNext();
      }

      @Override
      public E next() {
        return it.next();
      }

      @Override
      public void remove() {
        verifyIsMutable();
        it.remove();
      }
    }

    private class ListItr implements ListIterator<E> {
      private final ListIterator<E> it;

      public ListItr(final ListIterator<E> it) {
        this.it = it;
      }

      @Override
      public boolean hasNext() {
        return it.hasNext();
      }

      @Override
      public E next() {
        return it.next();
      }

      @Override
      public boolean hasPrevious() {
        return it.hasPrevious();
      }

      @Override
      public E previous() {
        return it.previous();
      }

      @Override
      public int nextIndex() {
        return it.nextIndex();
      }

      @Override
      public int previousIndex() {
        return it.previousIndex();
      }

      @Override
      public void remove() {
        verifyIsMutable();
        it.remove();
      }

      @Override
      public void set(final E e) {
        verifyIsMutable();
        it.set(e);
      }

      @Override
      public void add(final E e) {
        verifyIsMutable();
        it.add(e);
      }
    }

    private void writeObject(final ObjectOutputStream out) throws java.io.IOException {
      final PutField fields = out.putFields();
      fields.put("delegate", delegate);
      // isImmutable in this class is purely informational and ignored by readObject method!
      fields.put("isImmutable", isImmutable());
      out.writeFields();
    }

    /**
     * Reconstitute an always immutable list!
     */
    private void readObject(final java.io.ObjectInputStream in) throws java.io.IOException,
        ClassNotFoundException {
      // isImmutable simply ignored, so the object is immutable!
      in.defaultReadObject();
    }
  }

  static class MutableControlRandomAccessList<E> extends MutableControlList<E> implements
      Serializable, RandomAccess {
    private static final long serialVersionUID = -37339958348657863L;

    public MutableControlRandomAccessList(final List<E> delegate, final Immutable immutable) {
      super(delegate, immutable);
    }

    @Override
    public List<E> subList(final int fromIndex, final int toIndex) {
      return new MutableControlRandomAccessList<E>(delegate.subList(fromIndex, toIndex),
          getImmutable());
    }
  }

  // What to do with this?
  // FIXME
  static class ArrayListEx<E> extends ArrayList<E> {
    private static final long serialVersionUID = 6103893868125258638L;

    public ArrayListEx() {
      super();
    }

    public ArrayListEx(final Collection<? extends E> c) {
      super(c);
    }

    public ArrayListEx(final int initialCapacity) {
      super(initialCapacity);
    }

    /**
     * Switches mutability of this list. Sublasses should override this method, as it defaults to
     * noop.
     */
    protected boolean setImmutable(final boolean isImmutable) {
      return false;
    }

    /**
     * Return the mutability status of this list. Sublasses should override this method, as it
     * defaults to mutable.
     */
    public boolean isImmutable() {
      return false;
    }

    public boolean makeImmutable() {
      if (isImmutable()) {
        return false;
      }

      return setImmutable(true);
    }

    private void verifyIsMutable() {
      if (isImmutable()) {
        throw new UnsupportedOperationException();
      }
    }

    @Override
    public E set(final int index, final E element) {
      verifyIsMutable();
      return super.set(index, element);
    }

    @Override
    public boolean add(final E e) {
      verifyIsMutable();
      return super.add(e);
    }

    @Override
    public void add(final int index, final E element) {
      verifyIsMutable();
      super.add(index, element);
    }

    @Override
    public E remove(final int index) {
      verifyIsMutable();
      return super.remove(index);
    }

    @Override
    public boolean remove(final Object o) {
      verifyIsMutable();
      return super.remove(o);
    }

    @Override
    public void clear() {
      verifyIsMutable();
      super.clear();
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
      verifyIsMutable();
      return super.addAll(c);
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends E> c) {
      verifyIsMutable();
      return super.addAll(index, c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
      verifyIsMutable();
      return super.removeAll(c);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
      verifyIsMutable();
      return super.retainAll(c);
    }

    @Override
    public List<E> subList(final int fromIndex, final int toIndex) {
      // verifyIsMutable();
      return super.subList(fromIndex, toIndex);
    }

    @Override
    protected void removeRange(final int fromIndex, final int toIndex) {
      verifyIsMutable();
      super.removeRange(fromIndex, toIndex);
    }
  }
}
