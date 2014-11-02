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

package com.github.protobufel.common.verifications;

import org.eclipse.jdt.annotation.Nullable;

public final class Verifications {
  private Verifications() {
  }

  public static <T> T assertNonNull(final @Nullable T value) {
    if (value == null) {
      throw new AssertionError();
    }
    
    return value;
  }

  public static <T> T assertNonNull(final @Nullable T value, final @Nullable String msg) {
    if (value == null) {
      throw new AssertionError(msg);
    }
    
    return value;
  }

  public static <E, T extends Iterable<E>> T verifyNonNullIterable(final @Nullable T values) {
    if (values == null) {
      throw new NullPointerException();
    }

    for (E value : values) {
      if (value == null) {
        throw new NullPointerException();
      }
    }

    return values;
  }
  
  public static <E, T extends Iterable<E>> T verifyNonNullIterable(final @Nullable T values,
      final @Nullable String msg) {
    if (values == null) {
      throw new NullPointerException();
    }

    for (E value : values) {
      if (value == null) {
        throw new NullPointerException(msg);
      }
    }

    return values;
  }

  public static <E, T extends Iterable<E>> T verifyNonNullElements(final T values) {
    for (E value : values) {
      if (value == null) {
        throw new NullPointerException();
      }
    }

    return values;
  }

  public static <E, T extends Iterable<E>> T verifyNonNullElements(final T values,
      final @Nullable String msg) {
    for (E value : values) {
      if (value == null) {
        throw new NullPointerException(msg);
      }
    }

    return values;
  }

  public static <T> T verifyNonNull(final @Nullable T value) {
    if (value == null) {
      throw new NullPointerException();
    }

    return value;
  }

  public static <T> T verifyNonNull(final @Nullable T value, final @Nullable String msg) {
    if (value == null) {
      throw new NullPointerException(msg);
    }

    return value;
  }

  public static <T> T verifyArgument(final boolean condition, final @Nullable T value) {
    if (!condition) {
      throw new IllegalArgumentException();
    } else if (value == null) {
      throw new NullPointerException();
    }

    return value;
  }

  public static <T> T verifyArgument(final boolean condition, final @Nullable T value,
      final @Nullable String msg) {
    if (!condition) {
      throw new IllegalArgumentException(msg);
    } else if (value == null) {
      throw new NullPointerException();
    }

    return value;
  }

  public static boolean verifyCondition(final boolean condition) {
    if (!condition) {
      throw new IllegalArgumentException();
    }

    return condition;
  }

  public static boolean verifyCondition(final boolean condition, final @Nullable String msg) {
    if (!condition) {
      throw new IllegalArgumentException(msg);
    }

    return condition;
  }
  
  public static <T> T nonNullValue(final @Nullable T value, final T defaultValue) {
    return (value == null) ? defaultValue : value;
  }
}
