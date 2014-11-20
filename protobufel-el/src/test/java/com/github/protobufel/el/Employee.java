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

import java.util.List;
import java.util.Map;

import com.google.protobuf.Descriptors.FieldDescriptor;

public final class Employee {
  private int salary;
  private String name;
  private List<String> keywords;
  private Map<String, Long> attributes;

  public Employee(final String name) {
    this.name = name;
  }

  public int getSalary() {
    return salary;
  }

  public void setSalary(final int salary) {
    this.salary = salary;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public List<String> getKeywords() {
    return keywords;
  }

  public void setKeywords(final List<String> keywords) {
    this.keywords = keywords;
  }

  public Map<String, Long> getAttributes() {
    return attributes;
  }

  public void setAttributes(final Map<String, Long> attributes) {
    this.attributes = attributes;
  }

  @Deprecated
  public String overloaded1(final FieldDescriptor p1, final int p2) {
    return "overloaded1_fieldDescriptor";
  }

  public String overloaded1(final String p1, final int p2) {
    return "overloaded1_string";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + (attributes == null ? 0 : attributes.hashCode());
    result = (prime * result) + (keywords == null ? 0 : keywords.hashCode());
    result = (prime * result) + (name == null ? 0 : name.hashCode());
    result = (prime * result) + salary;
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
    if (!(obj instanceof Employee)) {
      return false;
    }
    final Employee other = (Employee) obj;
    if (attributes == null) {
      if (other.attributes != null) {
        return false;
      }
    } else if (!attributes.equals(other.attributes)) {
      return false;
    }
    if (keywords == null) {
      if (other.keywords != null) {
        return false;
      }
    } else if (!keywords.equals(other.keywords)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (salary != other.salary) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("Employee [salary=").append(salary).append(", name=").append(name)
        .append(", keywords=").append(keywords).append(", attributes=").append(attributes)
        .append("]");
    return builder.toString();
  }
}
