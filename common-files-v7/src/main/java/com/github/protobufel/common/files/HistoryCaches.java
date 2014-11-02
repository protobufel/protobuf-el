//
// Copyright © 2014, David Tesler (https://github.com/protobufel)
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//     * Redistributions of source code must retain the above copyright
//       notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above copyright
//       notice, this list of conditions and the following disclaimer in the
//       documentation and/or other materials provided with the distribution.
//     * Neither the name of the <organization> nor the
//       names of its contributors may be used to endorse or promote products
//       derived from this software without specific prior written permission.
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

package com.github.protobufel.common.files;

import static com.github.protobufel.common.verifications.Verifications.*;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
class HistoryCaches {
  private HistoryCaches() {
  }

  private static final class FakeHistoryCache<K, V> implements IHistoryCache<K, V> {
    private volatile int size;
    
    FakeHistoryCache() {
    }

    FakeHistoryCache(final int initialSize) {
      this.size = initialSize;
    }

    @Override
    public void clear() {
      size = 0;
    }

    @Override
    public void pop() {
      if (size > 0) {
        size--;
      }
    }

    @Override
    public void pop(int historySize) {
      size = (size > historySize) ? (size - historySize) : 0;
    }

    @Override
    public void push() {
      size++;
    }

    @Override
    public IHistoryCacheView<K, V> getCacheView() {
      return new IHistoryCacheView<K, V>() {
        @Override
        public boolean setCachedValue(K key, V value) {
          return false;
        }

        @Override
        public @Nullable V getCachedValue(K key) {
          return null;
        }

        @Override
        public int currentDepth() {
          return size;
        }
      };
    }

    @Override
    public int size() {
      return size;
    }
  }
  
  public static <K, V> IHistoryCache<K, V> fakeInstance() {
    return new FakeHistoryCache<K, V>();
  }
  
  public static <K, V> IHistoryCache<K, V> fakeInstance(final int initialSize) {
    return new FakeHistoryCache<K, V>(initialSize);
  } 
  
  static class HistoryCache<K, V> implements IHistoryCache<K, V> {
    private static final HistoryCache<?, ?> EMPTY = new HistoryCache<Object, Object>();
    private final Deque<Map<K, V>> history;
    private int maxSize;
    private Map<K, V> prevCache;
    private final IHistoryCacheView<K, V> cacheView;

    private HistoryCache() {
      // virtually unmodifiable; ideally would be Collections.umodifiableDeque()!
      this.history = new LinkedList<>();
      this.maxSize = 0;
      @SuppressWarnings("null")
      @NonNull Map<K, V> emptyMap = Collections.emptyMap();
      this.prevCache = emptyMap;
      this.cacheView = new IHistoryCacheView<K, V>() {
        @Override
        public boolean setCachedValue(K key, V value) {
          return false;
        }

        @Override
        public @Nullable V getCachedValue(K key) {
          return null;
        }

        @Override
        public int currentDepth() {
          return -1;
        }
      };
    }

    public HistoryCache(int maxSize) {
      @SuppressWarnings("null")
      final @NonNull Integer maxSizeInteger = maxSize;
      this.maxSize = verifyArgument(maxSize > 0, maxSizeInteger);
      this.history = new LinkedList<>();
      this.history.add(new IdentityHashMap<K, V>());
      @SuppressWarnings("null")
      @NonNull Map<K, V> emptyMap = Collections.emptyMap();
      this.prevCache = emptyMap;
      this.cacheView = new IHistoryCacheView<K, V>() {
        @Override
        public boolean setCachedValue(K key, V value) {
          return HistoryCache.this.setValue(key, value);
        }

        @Override
        public @Nullable V getCachedValue(K key) {
          return HistoryCache.this.getValue(key);
        }

        @Override
        public int currentDepth() {
          return HistoryCache.this.size();
        }
      };
    }

    @SuppressWarnings("unchecked")
    public static final <K, V> IHistoryCache<K, V> emptyInstance() {
      return (IHistoryCache<K, V>) EMPTY;
    }

    @Override
    public IHistoryCacheView<K, V> getCacheView() {
      return cacheView;
    }

    @Override
    public int size() {
      if (this == EMPTY) {
        return 0;
      }

      return history.size();
    }
    
    private boolean setValue(final K key, final V value) {
      Objects.requireNonNull(key);
      Objects.requireNonNull(value);

      if (this == EMPTY) {
        return false;
      }

      history.getFirst().put(key, value);
      return true;
    }

    private @Nullable V getValue(final K key) {
      Objects.requireNonNull(key);

      if (this == EMPTY) {
        return null;
      }

      return prevCache.get(key);
    }

    @Override
    public void clear() {
      if (this == EMPTY) {
        return;
      }

      if (history.size() == 1) {
        history.getFirst().clear();
      } else {
        history.clear();
        history.addFirst(new IdentityHashMap<K, V>());
      }

      @SuppressWarnings("null")
      @NonNull Map<K, V> emptyMap = Collections.emptyMap();
      prevCache = emptyMap;
    }

    @Override
    public void pop() {
      if (this == EMPTY) {
        return;
      }

      prevCache = assertNonNull(history.removeFirst());

      if (history.size() == 0) {
        history.addFirst(new IdentityHashMap<K, V>());
      }
    }

    @Override
    public void pop(final int historySize) {
      verifyCondition(historySize > 0, "historySize must be positive");

      if (this == EMPTY) {
        return;
      }

      final int size = Math.min(history.size(), historySize) - 1;

      for (int i = 0; i < size; i++) {
        history.removeFirst();
      }
      
      @SuppressWarnings("null")
      final @NonNull Map<K, V> removeFirst = history.removeFirst();
      prevCache = removeFirst;

      if (history.size() == 0) {
        history.addFirst(new IdentityHashMap<K, V>());
      }
    }

    @Override
    public void push() {
      if (this == EMPTY) {
        return;
      }

      prevCache = assertNonNull(history.getFirst());

      if ((maxSize > 1) && (history.size() == maxSize)) {
        history.removeLast();
      }

      history.addFirst(new IdentityHashMap<K, V>());
    }
  }
  
  static class SimpleEntryHistoryCache<K, V> {
    protected static final SimpleEntryHistoryCache<?, ?> EMPTY = new SimpleEntryHistoryCache<Object, Object>();
    private final Deque<Entry<K, V>> history;
    private int maxSize;

    private SimpleEntryHistoryCache() {
      // virtually unmodifiable; ideally would be Collections.umodifiableDeque()!
      this.history = new LinkedList<>();
      this.maxSize = 0;
    }

    public SimpleEntryHistoryCache(int maxSize) {
      this.maxSize = verifyArgument(maxSize > 0, maxSize);
      this.history = new LinkedList<>();
    }
    
    @SuppressWarnings("unchecked")
    public static <K, V> SimpleEntryHistoryCache<K, V> emptyCache() {
      return (SimpleEntryHistoryCache<K, V>) EMPTY;
    }

    public void push(final K key, final V value) {
      Objects.requireNonNull(key);
      Objects.requireNonNull(value);

      if (this == EMPTY) {
        return;
      }

      if ((maxSize > 1) && (history.size() == maxSize)) {
        history.removeLast();
      }

      history.addFirst(new SimpleImmutableEntry<>(key, value));
    }

    public void clear() {
      if (this == EMPTY) {
        return;
      }

      history.clear();
    }

    
    public @Nullable Entry<K, V> peek() {
      if (this == EMPTY) {
        return null;
      }

      return history.peekFirst();
    }

    
    public void pop() {
      if (this == EMPTY) {
        return;
      }

      history.pollFirst();
    }

    public void pop(final int historySize) {
      verifyCondition(historySize > 0, "historySize must be positive");

      if (this == EMPTY) {
        return;
      }

      final int size = Math.min(history.size(), historySize) - 1;

      for (int i = 0; i < size; i++) {
        history.removeFirst();
      }

      history.removeFirst();
    }
    
    public void adjustCache(final int historySize) {
      verifyCondition((historySize - history.size() <= 1),
          "historySize cannot exceed cache size by 1");

      if (historySize < history.size()) {
        pop(historySize);
      }
    }

    public boolean isEmpty() {
      return history.isEmpty();
    }
  }
  
  static class SimpleHistoryCache<T> {
    protected static final SimpleHistoryCache<?> EMPTY = new SimpleHistoryCache<Object>();
    private final Deque<PositionValue<T>> history;
    private int maxSize;
    private int currentPos;

    private SimpleHistoryCache() {
      // virtually unmodifiable; ideally would be Collections.umodifiableDeque()!
      this.history = new LinkedList<>();
      this.maxSize = 0;
      this.currentPos = -1;
    }

    public SimpleHistoryCache(int maxSize) {
      this.maxSize = verifyArgument(maxSize > 0, maxSize);
      this.history = new LinkedList<>();
      this.currentPos = -1;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> SimpleHistoryCache<T> emptyCache() {
      return (SimpleHistoryCache<T>) EMPTY;
    }

    public void push(final T value) {
      if (this == EMPTY) {
        return;
      }
      
      verifyCondition(currentPos != -1, "call adjustCache before calling push");

      if ((maxSize > 1) && (history.size() == maxSize)) {
        history.removeLast();
      }

      history.addFirst(new PositionValue<T>(currentPos, value));
    }

    public void clear() {
      if (this == EMPTY) {
        return;
      }

      history.clear();
      currentPos = -1;
    }

    
    public @Nullable T peek() {
      if ((this == EMPTY) || history.isEmpty()) {
        return null;
      }

      return history.peekFirst().getValue();
    }

    
    public @Nullable T pop() {
      if ((this == EMPTY) || history.isEmpty()) {
        return null;
      }

      return history.pollFirst().getValue();
    }

    public @Nullable T pop(final int historySize) {
      verifyCondition(historySize > 0, "historySize must be positive");
      currentPos = -1;
      final int historyDepth = getHistoryDepth(); 
      
      if ((this == EMPTY) || history.isEmpty() || (historyDepth <= historySize)) {
        return null;
      }

      PositionValue<T> element = history.removeFirst();
      
      while (!history.isEmpty() && (element.getPosition() > historySize)) {
        element = history.removeFirst();
      }
      
      return element.getValue();
    }
    
    public void adjustCache(final int historySize) {
      if (historySize < history.size()) {
        pop(historySize);
      }
      
      currentPos = historySize;
    }
    
    public int getHistoryDepth() {
      return history.isEmpty() ? 0 : history.peekFirst().getPosition();
    }

    public boolean isEmpty() {
      return history.isEmpty();
    }
  }
  
  private static final class PositionValue<T> {
    private final int position;
    private final T value;
    
    public PositionValue(int position, T value) {
      this.position = position;
      this.value = value;
    }

    public int getPosition() {
      return position;
    }

    public T getValue() {
      return value;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + position;
      result = prime * result + ((value == null) ? 0 : value.hashCode());
      return result;
    }

    @NonNullByDefault(false)
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof PositionValue)) {
        return false;
      }
      PositionValue<?> other = (PositionValue<?>) obj;
      if (position != other.position) {
        return false;
      }
      if (!value.equals(other.value)) {
        return false;
      }
      return true;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("PositionValue [position=").append(position).append(", value=").append(value)
          .append("]");
      @SuppressWarnings("null")
      @NonNull String string = builder.toString();
      return string;
    }
  }
}