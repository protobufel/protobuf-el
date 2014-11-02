package com.github.protobufel.test.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.assertj.core.util.Objects;
import org.assertj.core.util.Preconditions;
import org.assertj.core.util.introspection.Introspection;

//TODO remove when AssertJ usingElementComparatorOnFields fixed
public final class BeanPropertiesComparator<T> implements Comparator<T> {
  private final List<String> propNames;
  private List<Method> methods;

  private BeanPropertiesComparator(final String... propNames) {
    Preconditions.checkNotNullOrEmpty(propNames);
    final ArrayList<String> args = new ArrayList<String>();
    Collections.addAll(args, propNames);
    this.propNames = Collections.unmodifiableList(args);
    this.methods = null;
  }

  public static <T> BeanPropertiesComparator<T> of(final String... propNames) {
    return new BeanPropertiesComparator<T>(propNames);
  }
  
  @Override
  public int compare(T o1, T o2) {
    if (o1 == o2) {
      return 0;
    } else if (o1 == null) {
      return (o2 == null) ? 0 : -1;
    } else if (o2 == null) {
      return (o1 == null) ? 0 : 1;
    } 
    
    for (Method method : getMethods(o1)) {
      try {
        if (!Objects.areEqual(method.invoke(o1), method.invoke(o1))) {
          // this comparator is for custom equality, so either -1, or 1 would do!   
          return -1;
        }
      } catch (Exception e) {
        new RuntimeException(e);
      }
    }
    
    return 0;
  }

  private List<Method> getMethods(final T object) {
    if (this.methods == null) {
      final List<Method> methods = new ArrayList<Method>(propNames.size());

      for (String propName : propNames) {
        final Method method =
            Preconditions.checkNotNull(Introspection.getProperty(propName, object)
                .getReadMethod());
        methods.add(method);
      }
      
      this.methods = Collections.unmodifiableList(methods);
    }

    return this.methods;
  }
}