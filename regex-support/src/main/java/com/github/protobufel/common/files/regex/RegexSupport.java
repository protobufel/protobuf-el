/*
 * Copyright (c) 1999, 2009, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.github.protobufel.common.files.regex;

import java.util.ArrayList;
import java.util.regex.Matcher;

import org.eclipse.jdt.annotation.NonNull;

/**
 * A utility class for use by ExtendedPattern and friends.
 * Uses some portions of {@link java.util.regex.Matcher} and {@link 
 * java.util.regex.ASCII} needed mainly due to lack of access to {@link 
 * Matcher#lastAppendPosition}.    
 *
 * @author protobufel@gmail.com David Tesler
 */
public final class RegexSupport {
  private RegexSupport() {}

  public static String[] split(IPatternSupport pattern, CharSequence input, int limit) {
    int index = 0;
    boolean matchLimited = limit > 0;
    final ArrayList<String> matchList = new ArrayList<String>();
    final IMatcherSupport m = pattern.matcher(input);

    // Add segments before each match found
    while (m.find()) {
      if (!matchLimited || matchList.size() < limit - 1) {
        String match = input.subSequence(index, m.start()).toString();
        matchList.add(match);
        index = m.end();
      } else if (matchList.size() == limit - 1) { // last one
        String match = input.subSequence(index, input.length()).toString();
        matchList.add(match);
        index = m.end();
      }
    }

    // If no match was found, return this
    if (index == 0) {
      return new String[] { input.toString() };
    }

    // Add remaining segment
    if (!matchLimited || matchList.size() < limit) {
      matchList.add(input.subSequence(index, input.length()).toString());
    }

    // Construct result
    int resultSize = matchList.size();

    if (limit == 0) {
      while (resultSize > 0 && matchList.get(resultSize - 1).equals("")) {
        resultSize--;
      }
    }

    String[] result = new String[resultSize];
    @SuppressWarnings("null")
    @NonNull String[] array = matchList.subList(0, resultSize).toArray(result);
    return array;
  }

  public static <T extends IMatcherSupport> T appendReplacement(T matcher, StringBuffer sb,
      String replacement) {
    final int lastAppendPosition = appendReplacement(matcher.getDelegate(), matcher.getInput(), sb,
        replacement, matcher.getLastAppendPosition());
    matcher.setLastAppendPosition(lastAppendPosition);
    return matcher;
  }

  public static int appendReplacement(Matcher matcher, CharSequence text, StringBuffer sb,
      String replacement, int lastAppendPosition) {
    matcher.start(); // will throw an exception if no match
    // Process substitution string to replace group references with groups
    int cursor = 0;
    StringBuilder result = new StringBuilder();

    while (cursor < replacement.length()) {
      char nextChar = replacement.charAt(cursor);

      if (nextChar == '\\') {
        cursor++;
        nextChar = replacement.charAt(cursor);
        result.append(nextChar);
        cursor++;
      } else if (nextChar == '$') {
        // Skip past $
        cursor++;
        // A StringIndexOutOfBoundsException is thrown if
        // this "$" is the last character in replacement
        // string in current implementation, a IAE might be
        // more appropriate.
        nextChar = replacement.charAt(cursor);
        int refNum = -1;

        if (nextChar == '{') {
          cursor++;
          StringBuilder gsb = new StringBuilder();

          while (cursor < replacement.length()) {
            nextChar = replacement.charAt(cursor);

            if (ASCII.isLower(nextChar) || ASCII.isUpper(nextChar) || ASCII.isDigit(nextChar)) {
              gsb.append(nextChar);
              cursor++;
            } else {
              break;
            }
          }

          if (gsb.length() == 0) {
            throw new IllegalArgumentException("named capturing group has 0 length name");
          }

          if (nextChar != '}') {
            throw new IllegalArgumentException("named capturing group is missing trailing '}'");
          }

          String gname = gsb.toString();

          if (ASCII.isDigit(gname.charAt(0))) {
            throw new IllegalArgumentException("capturing group name {" + gname
                + "} starts with digit character");
          }

          final String group = matcher.group(gname);

          // Append group
          if (group != null) {
            result.append(group);
          }

          cursor++;
        } else {
          // The first number is always a group
          refNum = (int) nextChar - '0';
          if ((refNum < 0) || (refNum > 9))
            throw new IllegalArgumentException("Illegal group reference");
          cursor++;
          // Capture the largest legal group string
          boolean done = false;
          while (!done) {
            if (cursor >= replacement.length()) {
              break;
            }
            int nextDigit = replacement.charAt(cursor) - '0';
            if ((nextDigit < 0) || (nextDigit > 9)) { // not a number
              break;
            }
            int newRefNum = (refNum * 10) + nextDigit;
            if (matcher.groupCount() < newRefNum) {
              done = true;
            } else {
              refNum = newRefNum;
              cursor++;
            }
          }

          // Append group
          if (matcher.start(refNum) != -1 && matcher.end(refNum) != -1) {
            result.append(text, matcher.start(refNum), matcher.end(refNum));
          }
        }
      } else {
        result.append(nextChar);
        cursor++;
      }
    }

    // Append the intervening text
    sb.append(text, lastAppendPosition, matcher.start());
    // Append the match substitution
    sb.append(result);

    return matcher.end();
  }

  public static StringBuffer appendTail(CharSequence text, int lastAppendPosition, 
      StringBuffer sb) {
    sb.append(text, lastAppendPosition, text.length());
    return sb;
  }

  public static String replaceAll(IMatcherSupport matcher, String replacement) {
    matcher.reset();
    boolean result = matcher.find();
    final CharSequence text = matcher.getInput();

    if (result) {
      StringBuffer sb = new StringBuffer();

      do {
        final int lastAppendPosition = appendReplacement(matcher.getDelegate(), text, sb,
            replacement, matcher.getLastAppendPosition());
        matcher.setLastAppendPosition(lastAppendPosition);
        result = matcher.find();
      } while (result);

      appendTail(text, matcher.getLastAppendPosition(), sb);
      @SuppressWarnings("null")
      @NonNull String str = sb.toString();
      return str;
    }
    
    @SuppressWarnings("null")
    @NonNull String str = text.toString();
    return str;
  }

  public static String replaceFirst(IMatcherSupport matcher, String replacement) {
//    if (replacement == null) {
//      throw new NullPointerException("replacement");
//    }

    final CharSequence text = matcher.getInput();
    matcher.reset();

    if (!matcher.find()) {
      @SuppressWarnings("null")
      @NonNull String str = text.toString();
      return str;
    }

    StringBuffer sb = new StringBuffer();
    final int lastAppendPosition = appendReplacement(matcher.getDelegate(), text, sb, replacement,
        matcher.getLastAppendPosition());
    matcher.setLastAppendPosition(lastAppendPosition);

    appendTail(text, matcher.getLastAppendPosition(), sb);
    @SuppressWarnings("null")
    @NonNull String str = sb.toString();
    return str;
  }

  /**
   * Utility class that implements the standard C ctype functionality.
   * Taken as-is from OpenJDK 7.
   * 
   * @author Hong Zhang
   */

  final static class ASCII {

    static final int UPPER = 0x00000100;

    static final int LOWER = 0x00000200;

    static final int DIGIT = 0x00000400;

    static final int SPACE = 0x00000800;

    static final int PUNCT = 0x00001000;

    static final int CNTRL = 0x00002000;

    static final int BLANK = 0x00004000;

    static final int HEX = 0x00008000;

    static final int UNDER = 0x00010000;

    static final int ASCII = 0x0000FF00;

    static final int ALPHA = (UPPER | LOWER);

    static final int ALNUM = (UPPER | LOWER | DIGIT);

    static final int GRAPH = (PUNCT | UPPER | LOWER | DIGIT);

    static final int WORD = (UPPER | LOWER | UNDER | DIGIT);

    static final int XDIGIT = (HEX);

    private static final int[] ctype = new int[] { CNTRL, /* 00 (NUL) */
    CNTRL, /* 01 (SOH) */
    CNTRL, /* 02 (STX) */
    CNTRL, /* 03 (ETX) */
    CNTRL, /* 04 (EOT) */
    CNTRL, /* 05 (ENQ) */
    CNTRL, /* 06 (ACK) */
    CNTRL, /* 07 (BEL) */
    CNTRL, /* 08 (BS) */
    SPACE + CNTRL + BLANK, /* 09 (HT) */
    SPACE + CNTRL, /* 0A (LF) */
    SPACE + CNTRL, /* 0B (VT) */
    SPACE + CNTRL, /* 0C (FF) */
    SPACE + CNTRL, /* 0D (CR) */
    CNTRL, /* 0E (SI) */
    CNTRL, /* 0F (SO) */
    CNTRL, /* 10 (DLE) */
    CNTRL, /* 11 (DC1) */
    CNTRL, /* 12 (DC2) */
    CNTRL, /* 13 (DC3) */
    CNTRL, /* 14 (DC4) */
    CNTRL, /* 15 (NAK) */
    CNTRL, /* 16 (SYN) */
    CNTRL, /* 17 (ETB) */
    CNTRL, /* 18 (CAN) */
    CNTRL, /* 19 (EM) */
    CNTRL, /* 1A (SUB) */
    CNTRL, /* 1B (ESC) */
    CNTRL, /* 1C (FS) */
    CNTRL, /* 1D (GS) */
    CNTRL, /* 1E (RS) */
    CNTRL, /* 1F (US) */
    SPACE + BLANK, /* 20 SPACE */
    PUNCT, /* 21 ! */
    PUNCT, /* 22 " */
    PUNCT, /* 23 # */
    PUNCT, /* 24 $ */
    PUNCT, /* 25 % */
    PUNCT, /* 26 & */
    PUNCT, /* 27 ' */
    PUNCT, /* 28 ( */
    PUNCT, /* 29 ) */
    PUNCT, /* 2A * */
    PUNCT, /* 2B + */
    PUNCT, /* 2C , */
    PUNCT, /* 2D - */
    PUNCT, /* 2E . */
    PUNCT, /* 2F / */
    DIGIT + HEX + 0, /* 30 0 */
    DIGIT + HEX + 1, /* 31 1 */
    DIGIT + HEX + 2, /* 32 2 */
    DIGIT + HEX + 3, /* 33 3 */
    DIGIT + HEX + 4, /* 34 4 */
    DIGIT + HEX + 5, /* 35 5 */
    DIGIT + HEX + 6, /* 36 6 */
    DIGIT + HEX + 7, /* 37 7 */
    DIGIT + HEX + 8, /* 38 8 */
    DIGIT + HEX + 9, /* 39 9 */
    PUNCT, /* 3A : */
    PUNCT, /* 3B ; */
    PUNCT, /* 3C < */
    PUNCT, /* 3D = */
    PUNCT, /* 3E > */
    PUNCT, /* 3F ? */
    PUNCT, /* 40 @ */
    UPPER + HEX + 10, /* 41 A */
    UPPER + HEX + 11, /* 42 B */
    UPPER + HEX + 12, /* 43 C */
    UPPER + HEX + 13, /* 44 D */
    UPPER + HEX + 14, /* 45 E */
    UPPER + HEX + 15, /* 46 F */
    UPPER + 16, /* 47 G */
    UPPER + 17, /* 48 H */
    UPPER + 18, /* 49 I */
    UPPER + 19, /* 4A J */
    UPPER + 20, /* 4B K */
    UPPER + 21, /* 4C L */
    UPPER + 22, /* 4D M */
    UPPER + 23, /* 4E N */
    UPPER + 24, /* 4F O */
    UPPER + 25, /* 50 P */
    UPPER + 26, /* 51 Q */
    UPPER + 27, /* 52 R */
    UPPER + 28, /* 53 S */
    UPPER + 29, /* 54 T */
    UPPER + 30, /* 55 U */
    UPPER + 31, /* 56 V */
    UPPER + 32, /* 57 W */
    UPPER + 33, /* 58 X */
    UPPER + 34, /* 59 Y */
    UPPER + 35, /* 5A Z */
    PUNCT, /* 5B [ */
    PUNCT, /* 5C \ */
    PUNCT, /* 5D ] */
    PUNCT, /* 5E ^ */
    PUNCT | UNDER, /* 5F _ */
    PUNCT, /* 60 ` */
    LOWER + HEX + 10, /* 61 a */
    LOWER + HEX + 11, /* 62 b */
    LOWER + HEX + 12, /* 63 c */
    LOWER + HEX + 13, /* 64 d */
    LOWER + HEX + 14, /* 65 e */
    LOWER + HEX + 15, /* 66 f */
    LOWER + 16, /* 67 g */
    LOWER + 17, /* 68 h */
    LOWER + 18, /* 69 i */
    LOWER + 19, /* 6A j */
    LOWER + 20, /* 6B k */
    LOWER + 21, /* 6C l */
    LOWER + 22, /* 6D m */
    LOWER + 23, /* 6E n */
    LOWER + 24, /* 6F o */
    LOWER + 25, /* 70 p */
    LOWER + 26, /* 71 q */
    LOWER + 27, /* 72 r */
    LOWER + 28, /* 73 s */
    LOWER + 29, /* 74 t */
    LOWER + 30, /* 75 u */
    LOWER + 31, /* 76 v */
    LOWER + 32, /* 77 w */
    LOWER + 33, /* 78 x */
    LOWER + 34, /* 79 y */
    LOWER + 35, /* 7A z */
    PUNCT, /* 7B { */
    PUNCT, /* 7C | */
    PUNCT, /* 7D } */
    PUNCT, /* 7E ~ */
    CNTRL, /* 7F (DEL) */
    };

    static int getType(int ch) {
      return ((ch & 0xFFFFFF80) == 0 ? ctype[ch] : 0);
    }

    static boolean isType(int ch, int type) {
      return (getType(ch) & type) != 0;
    }

    static boolean isAscii(int ch) {
      return ((ch & 0xFFFFFF80) == 0);
    }

    static boolean isAlpha(int ch) {
      return isType(ch, ALPHA);
    }

    static boolean isDigit(int ch) {
      return ((ch - '0') | ('9' - ch)) >= 0;
    }

    static boolean isAlnum(int ch) {
      return isType(ch, ALNUM);
    }

    static boolean isGraph(int ch) {
      return isType(ch, GRAPH);
    }

    static boolean isPrint(int ch) {
      return ((ch - 0x20) | (0x7E - ch)) >= 0;
    }

    static boolean isPunct(int ch) {
      return isType(ch, PUNCT);
    }

    static boolean isSpace(int ch) {
      return isType(ch, SPACE);
    }

    static boolean isHexDigit(int ch) {
      return isType(ch, HEX);
    }

    static boolean isOctDigit(int ch) {
      return ((ch - '0') | ('7' - ch)) >= 0;
    }

    static boolean isCntrl(int ch) {
      return isType(ch, CNTRL);
    }

    static boolean isLower(int ch) {
      return ((ch - 'a') | ('z' - ch)) >= 0;
    }

    static boolean isUpper(int ch) {
      return ((ch - 'A') | ('Z' - ch)) >= 0;
    }

    static boolean isWord(int ch) {
      return isType(ch, WORD);
    }

    static int toDigit(int ch) {
      return (ctype[ch & 0x7F] & 0x3F);
    }

    static int toLower(int ch) {
      return isUpper(ch) ? (ch + 0x20) : ch;
    }

    static int toUpper(int ch) {
      return isLower(ch) ? (ch - 0x20) : ch;
    }
  }
}
