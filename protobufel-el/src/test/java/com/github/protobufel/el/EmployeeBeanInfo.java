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

import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class EmployeeBeanInfo extends SimpleBeanInfo {

  // Bean descriptor//GEN-FIRST:BeanDescriptor
  /* lazy BeanDescriptor */
  private static BeanDescriptor getBdescriptor() {
    final BeanDescriptor beanDescriptor =
        new BeanDescriptor(com.github.protobufel.el.Employee.class, null); // NOI18N//GEN-HEADEREND:BeanDescriptor
    // Here you can add code for customizing the BeanDescriptor.

    return beanDescriptor;
  }// GEN-LAST:BeanDescriptor


  // Property identifiers//GEN-FIRST:Properties
  private static final int PROPERTY_attributes = 0;
  private static final int PROPERTY_keywords = 1;
  private static final int PROPERTY_name = 2;
  private static final int PROPERTY_salary = 3;

  // Property array
  /* lazy PropertyDescriptor */
  private static PropertyDescriptor[] getPdescriptor() {
    final PropertyDescriptor[] properties = new PropertyDescriptor[4];

    try {
      properties[PROPERTY_attributes] =
          new PropertyDescriptor("attributes", com.github.protobufel.el.Employee.class,
              "getAttributes", "setAttributes"); // NOI18N
      properties[PROPERTY_keywords] =
          new PropertyDescriptor("keywords", com.github.protobufel.el.Employee.class,
              "getKeywords", "setKeywords"); // NOI18N
      properties[PROPERTY_name] =
          new PropertyDescriptor("name", com.github.protobufel.el.Employee.class, "getName",
              "setName"); // NOI18N
      properties[PROPERTY_salary] =
          new PropertyDescriptor("salary", com.github.protobufel.el.Employee.class, "getSalary",
              "setSalary"); // NOI18N
    } catch (final IntrospectionException e) {
      e.printStackTrace();
    }// GEN-HEADEREND:Properties
     // Here you can add code for customizing the properties array.

    return properties;
  }// GEN-LAST:Properties

  // EventSet identifiers//GEN-FIRST:Events

  // EventSet array
  /* lazy EventSetDescriptor */
  private static EventSetDescriptor[] getEdescriptor() {
    final EventSetDescriptor[] eventSets = new EventSetDescriptor[0];// GEN-HEADEREND:Events
    // Here you can add code for customizing the event sets array.

    return eventSets;
  }// GEN-LAST:Events

  // Method identifiers//GEN-FIRST:Methods
  private static final int METHOD_equals0 = 0;
  private static final int METHOD_hashCode1 = 1;
  private static final int METHOD_overloaded12 = 2;
  private static final int METHOD_toString3 = 3;

  // Method array
  /* lazy MethodDescriptor */
  private static MethodDescriptor[] getMdescriptor() {
    final MethodDescriptor[] methods = new MethodDescriptor[4];

    try {
      methods[METHOD_equals0] =
          new MethodDescriptor(com.github.protobufel.el.Employee.class.getMethod("equals",
              new Class[] {java.lang.Object.class})); // NOI18N
      methods[METHOD_equals0].setDisplayName("");
      methods[METHOD_hashCode1] =
          new MethodDescriptor(com.github.protobufel.el.Employee.class.getMethod("hashCode",
              new Class[] {})); // NOI18N
      methods[METHOD_hashCode1].setDisplayName("");
      methods[METHOD_overloaded12] =
          new MethodDescriptor(com.github.protobufel.el.Employee.class.getMethod("overloaded1",
              new Class[] {java.lang.String.class, int.class})); // NOI18N
      methods[METHOD_overloaded12].setDisplayName("");
      methods[METHOD_toString3] =
          new MethodDescriptor(com.github.protobufel.el.Employee.class.getMethod("toString",
              new Class[] {})); // NOI18N
      methods[METHOD_toString3].setDisplayName("");
    } catch (final Exception e) {
    }// GEN-HEADEREND:Methods
     // Here you can add code for customizing the methods array.

    return methods;
  }// GEN-LAST:Methods

  private static java.awt.Image iconColor16 = null;// GEN-BEGIN:IconsDef
  private static java.awt.Image iconColor32 = null;
  private static java.awt.Image iconMono16 = null;
  private static java.awt.Image iconMono32 = null;// GEN-END:IconsDef
  private static String iconNameC16 = null;// GEN-BEGIN:Icons
  private static String iconNameC32 = null;
  private static String iconNameM16 = null;
  private static String iconNameM32 = null;// GEN-END:Icons

  private static final int defaultPropertyIndex = -1;// GEN-BEGIN:Idx
  private static final int defaultEventIndex = -1;// GEN-END:Idx


  // GEN-FIRST:Superclass
  // Here you can add code for customizing the Superclass BeanInfo.

  // GEN-LAST:Superclass
  /**
   * Gets the bean's <code>BeanDescriptor</code>s.
   *
   * @return BeanDescriptor describing the editable properties of this bean. May return null if the
   *         information should be obtained by automatic analysis.
   */
  @Override
  public BeanDescriptor getBeanDescriptor() {
    return getBdescriptor();
  }

  /**
   * Gets the bean's <code>PropertyDescriptor</code>s.
   *
   * @return An array of PropertyDescriptors describing the editable properties supported by this
   *         bean. May return null if the information should be obtained by automatic analysis.
   *         <p>
   *         If a property is indexed, then its entry in the result array will belong to the
   *         IndexedPropertyDescriptor subclass of PropertyDescriptor. A client of
   *         getPropertyDescriptors can use "instanceof" to check if a given PropertyDescriptor is
   *         an IndexedPropertyDescriptor.
   */
  @Override
  public PropertyDescriptor[] getPropertyDescriptors() {
    return getPdescriptor();
  }

  /**
   * Gets the bean's <code>EventSetDescriptor</code>s.
   *
   * @return An array of EventSetDescriptors describing the kinds of events fired by this bean. May
   *         return null if the information should be obtained by automatic analysis.
   */
  @Override
  public EventSetDescriptor[] getEventSetDescriptors() {
    return getEdescriptor();
  }

  /**
   * Gets the bean's <code>MethodDescriptor</code>s.
   *
   * @return An array of MethodDescriptors describing the methods implemented by this bean. May
   *         return null if the information should be obtained by automatic analysis.
   */
  @Override
  public MethodDescriptor[] getMethodDescriptors() {
    return getMdescriptor();
  }

  /**
   * A bean may have a "default" property that is the property that will mostly commonly be
   * initially chosen for update by human's who are customizing the bean.
   *
   * @return Index of default property in the PropertyDescriptor array returned by
   *         getPropertyDescriptors.
   *         <P>
   *         Returns -1 if there is no default property.
   */
  @Override
  public int getDefaultPropertyIndex() {
    return defaultPropertyIndex;
  }

  /**
   * A bean may have a "default" event that is the event that will mostly commonly be used by
   * human's when using the bean.
   *
   * @return Index of default event in the EventSetDescriptor array returned by
   *         getEventSetDescriptors.
   *         <P>
   *         Returns -1 if there is no default event.
   */
  @Override
  public int getDefaultEventIndex() {
    return defaultEventIndex;
  }

  /**
   * This method returns an image object that can be used to represent the bean in toolboxes,
   * toolbars, etc. Icon images will typically be GIFs, but may in future include other formats.
   * <p>
   * Beans aren't required to provide icons and may return null from this method.
   * <p>
   * There are four possible flavors of icons (16x16 color, 32x32 color, 16x16 mono, 32x32 mono). If
   * a bean choses to only support a single icon we recommend supporting 16x16 color.
   * <p>
   * We recommend that icons have a "transparent" background so they can be rendered onto an
   * existing background.
   *
   * @param iconKind The kind of icon requested. This should be one of the constant values
   *        ICON_COLOR_16x16, ICON_COLOR_32x32, ICON_MONO_16x16, or ICON_MONO_32x32.
   * @return An image object representing the requested icon. May return null if no suitable icon is
   *         available.
   */
  @Override
  public java.awt.Image getIcon(final int iconKind) {
    switch (iconKind) {
      case ICON_COLOR_16x16:
        if (iconNameC16 == null) {
          return null;
        } else {
          if (iconColor16 == null) {
            iconColor16 = loadImage(iconNameC16);
          }
          return iconColor16;
        }
      case ICON_COLOR_32x32:
        if (iconNameC32 == null) {
          return null;
        } else {
          if (iconColor32 == null) {
            iconColor32 = loadImage(iconNameC32);
          }
          return iconColor32;
        }
      case ICON_MONO_16x16:
        if (iconNameM16 == null) {
          return null;
        } else {
          if (iconMono16 == null) {
            iconMono16 = loadImage(iconNameM16);
          }
          return iconMono16;
        }
      case ICON_MONO_32x32:
        if (iconNameM32 == null) {
          return null;
        } else {
          if (iconMono32 == null) {
            iconMono32 = loadImage(iconNameM32);
          }
          return iconMono32;
        }
      default:
        return null;
    }
  }

}
