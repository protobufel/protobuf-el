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

/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */

package com.github.protobufel.el;

import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import com.github.protobufel.DynamicMessage;

/**
 * A generated BeanInfo for use by BeanELResolverEx.
 *
 * @author protobufel@gmail.com David Tesler
 */
// FIXME cleanup, refactor; remove if no longer needed; mostly, earlier leftover
final class BuilderBeanInfo extends SimpleBeanInfo {

  // Bean descriptor//GEN-FIRST:BeanDescriptor
  /* lazy BeanDescriptor */
  private static BeanDescriptor getBdescriptor() {
    final BeanDescriptor beanDescriptor = new BeanDescriptor(DynamicMessage.Builder.class, null); // NOI18N//GEN-HEADEREND:BeanDescriptor
    // Here you can add code for customizing the BeanDescriptor.

    return beanDescriptor;
  }// GEN-LAST:BeanDescriptor


  // Property identifiers//GEN-FIRST:Properties
  private static final int PROPERTY_allFields = 0;
  private static final int PROPERTY_attributeCount = 1;
  private static final int PROPERTY_attributeKeys = 2;
  private static final int PROPERTY_attributes = 3;
  private static final int PROPERTY_childCount = 4;
  private static final int PROPERTY_childKeys = 5;
  private static final int PROPERTY_children = 6;
  private static final int PROPERTY_defaultInstanceForType = 7;
  private static final int PROPERTY_descriptorForType = 8;
  private static final int PROPERTY_empty = 9;
  private static final int PROPERTY_root = 10;

  // Property array
  /* lazy PropertyDescriptor */
  private static PropertyDescriptor[] getPdescriptor() {
    final PropertyDescriptor[] properties = new PropertyDescriptor[11];

    try {
      properties[PROPERTY_allFields] =
          new PropertyDescriptor("allFields", DynamicMessage.Builder.class, "getAllFields", null); // NOI18N
      properties[PROPERTY_attributeCount] =
          new PropertyDescriptor("attributeCount", DynamicMessage.Builder.class,
              "getAttributeCount", null); // NOI18N
      properties[PROPERTY_attributeKeys] =
          new PropertyDescriptor("attributeKeys", DynamicMessage.Builder.class, "getAttributeKeys",
              null); // NOI18N
      properties[PROPERTY_attributes] =
          new PropertyDescriptor("attributes", DynamicMessage.Builder.class, "getAttributes", null); // NOI18N
      properties[PROPERTY_childCount] =
          new PropertyDescriptor("childCount", DynamicMessage.Builder.class, "getChildCount", null); // NOI18N
      properties[PROPERTY_childKeys] =
          new PropertyDescriptor("childKeys", DynamicMessage.Builder.class, "getChildKeys", null); // NOI18N
      properties[PROPERTY_children] =
          new PropertyDescriptor("children", DynamicMessage.Builder.class, "getChildren", null); // NOI18N
      properties[PROPERTY_defaultInstanceForType] =
          new PropertyDescriptor("defaultInstanceForType", DynamicMessage.Builder.class,
              "getDefaultInstanceForType", null); // NOI18N
      properties[PROPERTY_descriptorForType] =
          new PropertyDescriptor("descriptorForType", DynamicMessage.Builder.class,
              "getDescriptorForType", null); // NOI18N
      properties[PROPERTY_empty] =
          new PropertyDescriptor("empty", DynamicMessage.Builder.class, "isEmpty", null); // NOI18N
      properties[PROPERTY_root] =
          new PropertyDescriptor("root", DynamicMessage.Builder.class, "isRoot", null); // NOI18N
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
  private static final int METHOD_addAttribute0 = 0;
  private static final int METHOD_addAttribute1 = 1;
  private static final int METHOD_addChild2 = 2;
  private static final int METHOD_addChild3 = 3;
  private static final int METHOD_addChild4 = 4;
  private static final int METHOD_addChild5 = 5;
  private static final int METHOD_build6 = 6;
  private static final int METHOD_buildPartial7 = 7;
  private static final int METHOD_clear8 = 8;
  private static final int METHOD_clone9 = 9;
  private static final int METHOD_getAttribute10 = 10;
  private static final int METHOD_getAttribute11 = 11;
  private static final int METHOD_getChild12 = 12;
  private static final int METHOD_getChild13 = 13;
  private static final int METHOD_isAttribute14 = 14;
  private static final int METHOD_isFieldIndexed15 = 15;
  private static final int METHOD_mergeFrom16 = 16;
  private static final int METHOD_newSavePoint17 = 17;
  private static final int METHOD_removeAttribute18 = 18;
  private static final int METHOD_removeAttribute19 = 19;
  private static final int METHOD_removeChild20 = 20;
  private static final int METHOD_removeChild21 = 21;
  private static final int METHOD_setAttribute24 = 24;
  private static final int METHOD_setAttribute25 = 25;
  private static final int METHOD_setChild26 = 26;
  private static final int METHOD_setChild27 = 27;
  private static final int METHOD_setChild28 = 28;
  private static final int METHOD_setChild29 = 29;
  private static final int METHOD_size30 = 30;
  private static final int METHOD_toChild31 = 31;
  private static final int METHOD_toChild32 = 32;
  private static final int METHOD_toLastChild33 = 33;
  private static final int METHOD_toParent34 = 34;
  private static final int METHOD_toRoot35 = 35;

  // Method array
  /* lazy MethodDescriptor */
  private static MethodDescriptor[] getMdescriptor() {
    final MethodDescriptor[] methods = new MethodDescriptor[36];

    try {
      methods[METHOD_addAttribute0] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("addAttribute", new Class[] {
              java.lang.String.class, java.lang.Object.class})); // NOI18N
      methods[METHOD_addAttribute0].setDisplayName("");
      methods[METHOD_addAttribute1] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("addAttribute", new Class[] {
              java.lang.String.class, int.class, java.lang.Object.class})); // NOI18N
      methods[METHOD_addAttribute1].setDisplayName("");
      methods[METHOD_addChild2] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("addChild", new Class[] {
              java.lang.String.class, com.google.protobuf.Message.class})); // NOI18N
      methods[METHOD_addChild2].setDisplayName("");
      methods[METHOD_addChild3] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("addChild",
              new Class[] {java.lang.String.class})); // NOI18N
      methods[METHOD_addChild3].setDisplayName("");
      methods[METHOD_addChild4] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("addChild", new Class[] {
              java.lang.String.class, int.class, com.google.protobuf.Message.class})); // NOI18N
      methods[METHOD_addChild4].setDisplayName("");
      methods[METHOD_addChild5] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("addChild", new Class[] {
              java.lang.String.class, int.class})); // NOI18N
      methods[METHOD_addChild5].setDisplayName("");
      methods[METHOD_build6] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("build", new Class[] {})); // NOI18N
      methods[METHOD_build6].setDisplayName("");
      methods[METHOD_buildPartial7] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("buildPartial",
              new Class[] {})); // NOI18N
      methods[METHOD_buildPartial7].setDisplayName("");
      methods[METHOD_clear8] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("clear", new Class[] {})); // NOI18N
      methods[METHOD_clear8].setDisplayName("");
      methods[METHOD_clone9] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("clone", new Class[] {})); // NOI18N
      methods[METHOD_clone9].setDisplayName("");
      methods[METHOD_getAttribute10] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("getAttribute",
              new Class[] {java.lang.String.class})); // NOI18N
      methods[METHOD_getAttribute10].setDisplayName("");
      methods[METHOD_getAttribute11] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("getAttribute", new Class[] {
              java.lang.String.class, int.class})); // NOI18N
      methods[METHOD_getAttribute11].setDisplayName("");
      methods[METHOD_getChild12] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("getChild",
              new Class[] {java.lang.String.class})); // NOI18N
      methods[METHOD_getChild12].setDisplayName("");
      methods[METHOD_getChild13] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("getChild", new Class[] {
              java.lang.String.class, int.class})); // NOI18N
      methods[METHOD_getChild13].setDisplayName("");
      methods[METHOD_isAttribute14] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("isAttribute",
              new Class[] {java.lang.String.class})); // NOI18N
      methods[METHOD_isAttribute14].setDisplayName("");
      methods[METHOD_isFieldIndexed15] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("isFieldIndexed",
              new Class[] {java.lang.String.class})); // NOI18N
      methods[METHOD_isFieldIndexed15].setDisplayName("");
      methods[METHOD_mergeFrom16] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("mergeFrom",
              new Class[] {com.google.protobuf.Message.class})); // NOI18N
      methods[METHOD_mergeFrom16].setDisplayName("");
      methods[METHOD_newSavePoint17] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("newSavePoint",
              new Class[] {})); // NOI18N
      methods[METHOD_newSavePoint17].setDisplayName("");
      methods[METHOD_removeAttribute18] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("removeAttribute",
              new Class[] {java.lang.String.class})); // NOI18N
      methods[METHOD_removeAttribute18].setDisplayName("");
      methods[METHOD_removeAttribute19] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("removeAttribute",
              new Class[] {java.lang.String.class, int.class})); // NOI18N
      methods[METHOD_removeAttribute19].setDisplayName("");
      methods[METHOD_removeChild20] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("removeChild",
              new Class[] {java.lang.String.class})); // NOI18N
      methods[METHOD_removeChild20].setDisplayName("");
      methods[METHOD_removeChild21] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("removeChild", new Class[] {
              java.lang.String.class, int.class})); // NOI18N
      methods[METHOD_removeChild21].setDisplayName("");
      methods[METHOD_setAttribute24] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("setAttribute", new Class[] {
              java.lang.String.class, java.lang.Object.class})); // NOI18N
      methods[METHOD_setAttribute24].setDisplayName("");
      methods[METHOD_setAttribute25] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("setAttribute", new Class[] {
              java.lang.String.class, int.class, java.lang.Object.class})); // NOI18N
      methods[METHOD_setAttribute25].setDisplayName("");
      methods[METHOD_setChild26] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("setChild", new Class[] {
              java.lang.String.class, com.google.protobuf.Message.class})); // NOI18N
      methods[METHOD_setChild26].setDisplayName("");
      methods[METHOD_setChild27] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("setChild",
              new Class[] {java.lang.String.class})); // NOI18N
      methods[METHOD_setChild27].setDisplayName("");
      methods[METHOD_setChild28] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("setChild", new Class[] {
              java.lang.String.class, int.class, com.google.protobuf.Message.class})); // NOI18N
      methods[METHOD_setChild28].setDisplayName("");
      methods[METHOD_setChild29] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("setChild", new Class[] {
              java.lang.String.class, int.class})); // NOI18N
      methods[METHOD_setChild29].setDisplayName("");
      methods[METHOD_size30] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("size", new Class[] {})); // NOI18N
      methods[METHOD_size30].setDisplayName("");
      methods[METHOD_toChild31] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("toChild",
              new Class[] {java.lang.String.class})); // NOI18N
      methods[METHOD_toChild31].setDisplayName("");
      methods[METHOD_toChild32] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("toChild", new Class[] {
              java.lang.String.class, int.class})); // NOI18N
      methods[METHOD_toChild32].setDisplayName("");
      methods[METHOD_toLastChild33] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("toLastChild",
              new Class[] {java.lang.String.class})); // NOI18N
      methods[METHOD_toLastChild33].setDisplayName("");
      methods[METHOD_toParent34] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("toParent", new Class[] {})); // NOI18N
      methods[METHOD_toParent34].setDisplayName("");
      methods[METHOD_toRoot35] =
          new MethodDescriptor(DynamicMessage.Builder.class.getMethod("toRoot", new Class[] {})); // NOI18N
      methods[METHOD_toRoot35].setDisplayName("");
    } catch (final Exception e) {
    }// GEN-HEADEREND:Methods
     // Here you can add code for customizing the methods array.

    return methods;
  }// GEN-LAST:Methods

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
}
