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

import static com.github.protobufel.grammar.ParserUtils.getEndPositionInLine;
import static com.github.protobufel.grammar.ParserUtils.getLineCount;
import static com.github.protobufel.grammar.ParserUtils.getTrimmedBlockCommentContent;
import static com.github.protobufel.grammar.ProtoParser.LINE_COMMENT;
import static com.github.protobufel.grammar.ProtoParser.RBRACE;
import static com.github.protobufel.grammar.ProtoParser.SEMI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.protobufel.grammar.ParserUtils.CommonTokenStreamEx;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.SourceCodeInfo;
import com.google.protobuf.DescriptorProtos.SourceCodeInfo.Location;
import com.google.protobuf.DescriptorProtos.UninterpretedOption;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;

// ********************* LocationBuilder START ****************************************************

// FIXME implement and enable
class LocationBuilder {
  private static final String NEW_LINE = "\n";
  private static final Set<Integer> COMMENT_SKIPPERS;
  private Location.Builder location;
  private final Path path;
  // private ScopeManager scopeManager;
  private final SourceCodeInfo.Builder sourceBuilder;
  private final IdentityHashMap<Token, String> leadingComments;
  private final CommonTokenStreamEx tokens;

  private Scope currentScope;

  // Because Location path requires the repeated element to know its index
  // within its parent,
  // we need either to use the current parent builder, or scopes with type
  // element counters, or
  // as a fake FileDescriptorProto only for repeated fields within it with empty
  // elements.

  static {
    COMMENT_SKIPPERS = new HashSet<Integer>();
    COMMENT_SKIPPERS.add(RBRACE);
    COMMENT_SKIPPERS.add(SEMI);
  }

  public LocationBuilder(final SourceCodeInfo.Builder sourceBuilder,
      final CommonTokenStreamEx tokens) {
    location = null;
    path = new Path();
    currentScope = new FileScope();
    this.sourceBuilder = sourceBuilder;
    leadingComments = new IdentityHashMap<Token, String>();
    this.tokens = tokens;
  }

  public LocationBuilder addMessageLocation() {
    final int elementIndex = currentScope.addMessage();
    return addLocation();
  }

  public LocationBuilder addEnumLocation() {
    final int elementIndex = currentScope.addEnum();
    return addLocation();
  }

  public LocationBuilder addFieldLocation() {
    final int elementIndex = currentScope.addField();
    return addLocation();
  }

  public LocationBuilder addOptionLocation(final ParserRuleContext parentCtx) {
    final int optionsFieldNumber = currentScope.getOptionsFieldNumber();
    addLocation().addPath(optionsFieldNumber).setAllSpan(parentCtx);
    final int elementIndex = currentScope.addOption(optionsFieldNumber);
    return addLocation();
  }

  public LocationBuilder addOptionLocation() {
    final int optionsFieldNumber = currentScope.getOptionsFieldNumber();
    addLocation().addPath(optionsFieldNumber);
    final int elementIndex = currentScope.addOption(optionsFieldNumber);
    return addLocation();
  }

  public LocationBuilder addOptionNameLocation() {
    final int elementIndex = currentScope.addOptionName();
    return addLocation();
  }

  public LocationBuilder addLocationForPrimitive(final int fieldNumber) {
    final int elementIndex = currentScope.addElement(fieldNumber);
    addLocation();

    if (elementIndex >= 0) {
      addAllPath(Arrays.asList(fieldNumber, elementIndex));
    } else {
      addAllPath(Arrays.asList(fieldNumber));
    }

    return this;
  }

  public LocationBuilder popScope() {
    currentScope = currentScope.popScope();
    return this;
  }

  public LocationBuilder addLocation(final int index) {
    location = path.setAllPath(sourceBuilder.addLocationBuilder(index));
    return this;
  }

  public LocationBuilder addLocation() {
    location = path.setAllPath(sourceBuilder.addLocationBuilder());
    return this;
  }

  public LocationBuilder addLocationClone() {
    location =
        location == null ? sourceBuilder.getLocationBuilder(sourceBuilder.getLocationCount() - 1)
            : location;
    location = sourceBuilder.addLocationBuilder().addAllPath(location.getPathList());
    return this;
  }

  // ******************************* Path stuff START

  public LocationBuilder addAllPath(final ParserRuleContext ctx, final ParseTree... parts) {
    location.addAllSpan(getSpan(ctx));

    // FIXME
    if (parts != null && parts.length > 0) {
      for (final ParseTree parseTree : parts) {
        location
            .addAllSpan(parseTree instanceof ParserRuleContext ? getSpan((ParserRuleContext) parseTree)
                : getSpan((TerminalNode) parseTree));
      }
    }

    return this;
  }

  public LocationBuilder addAllPath(final Iterable<? extends Integer> values) {
    location.addAllPath(values);
    return this;
  }

  public LocationBuilder addPath(final int value) {
    location.addPath(value);
    return this;
  }

  // ******************************* Span stuff START

  public LocationBuilder setAllSpan(final ParserRuleContext ctx) {
    return setAllSpan(getSpan(ctx));
  }

  public LocationBuilder setAllSpan(final TerminalNode ctx) {
    return setAllSpan(getSpan(ctx));
  }

  public LocationBuilder setAllSpan(final ParseTree ctx) {
    if (ctx instanceof ParserRuleContext) {
      setAllSpan((ParserRuleContext) ctx);
    } else {
      setAllSpan((TerminalNode) ctx);
    }

    return this;
  }

  private LocationBuilder setAllSpan(final Iterable<? extends Integer> values) {
    if (location.getSpanCount() > 0) {
      location.clearSpan();
    }

    location.addAllSpan(values);
    return this;
  }

  private Iterable<Integer> getSpan(final ParserRuleContext ctx) {
    final Token startToken = ctx.getStart();
    final Token stopToken = ctx.getStop();

    if (stopToken.getLine() == startToken.getLine()) {
      return Arrays.asList(startToken.getLine() - 1, startToken.getCharPositionInLine(),
          getEndPositionInLine(stopToken));

    } else {
      return Arrays.asList(startToken.getLine() - 1, startToken.getCharPositionInLine(),
          stopToken.getLine() - 1, getEndPositionInLine(stopToken));
    }
  }

  private Iterable<Integer> getSpan(final TerminalNode ctx) {
    final Token startToken = ctx.getSymbol();
    return Arrays.asList(startToken.getLine() - 1, startToken.getCharPositionInLine(),
        getEndPositionInLine(startToken));
  }

  // ***************************** Comments stuff START

  public LocationBuilder clearComments() {
    location.clearLeadingComments().clearTrailingComments();
    return this;
  }

  public LocationBuilder comments(final ParserRuleContext ctx) {
    String comments = getLeadingComments(ctx);

    if (!comments.isEmpty()) {
      location.setLeadingComments(comments);
    }

    comments = getTrailingComments(ctx);

    if (!comments.isEmpty()) {
      location.setTrailingComments(comments);
    }

    return this;
  }

  private String getLeadingComments(final ParserRuleContext ctx) {
    final Token startToken = ctx.getStart();
    final String result = leadingComments.remove(startToken);

    if (result != null) {
      return result;
    }

    final List<Token> comments =
        tokens.getHiddenTokensToLeft(startToken.getTokenIndex(), Token.HIDDEN_CHANNEL);

    if (comments == null) {
      return "";
    }

    // process all comments as the previousToken is not a comments magnet!
    final StringBuilder builder = new StringBuilder();
    int currentLine = startToken.getLine();

    for (final ListIterator<Token> iterator = comments.listIterator(comments.size()); iterator
        .hasPrevious();) {
      final Token token = iterator.previous();
      final String text = token.getText();

      // FIXME deal with in beetwen comments intervals and endings!

      if (token.getType() == LINE_COMMENT) {
        if (currentLine - token.getLine() > 1) {
          break;
        }

        builder.append(text, 2, text.length()).append(NEW_LINE);
      } else { // this must be COMMENT!
        if (currentLine - (token.getLine() + getLineCount(text)) > 1) {
          break;
        }

        builder.append(getTrimmedBlockCommentContent(text));
      }

      currentLine = token.getLine();
    }

    return builder.toString();
  }

  private String getTrailingComments(final ParserRuleContext ctx) {
    final Token stopToken = ctx.getStop();
    final List<Token> comments =
        tokens.getHiddenTokensToRight(stopToken.getTokenIndex(), Token.HIDDEN_CHANNEL);

    if (comments == null) {
      return "";
    }

    final Token nextToken =
        tokens.nextToken(comments.get(comments.size() - 1).getTokenIndex(), Token.DEFAULT_CHANNEL);
    int toIndex;

    if (nextToken == null || isCommentSkipper(nextToken)) {
      toIndex = comments.size();
    } else {
      toIndex = setLeadingComments(stopToken, nextToken, comments);
    }

    int currentLine = stopToken.getLine();
    final StringBuilder builder = new StringBuilder();

    // FIXME deal with in beetwen comments intervals and endings!

    for (final Token token : comments.subList(0, toIndex)) {
      if (token.getLine() - currentLine > 1) {
        break;
      }

      final String text = token.getText();

      if (token.getType() == LINE_COMMENT) {
        builder.append(text, 2, text.length()).append(NEW_LINE);
        currentLine = token.getLine();
      } else { // this must be COMMENT!
        builder.append(getTrimmedBlockCommentContent(text));
        currentLine = token.getLine() + getLineCount(text);
      }
    }

    return builder.toString();
  }

  private boolean isCommentSkipper(final Token nextToken) {
    return COMMENT_SKIPPERS.contains(nextToken);
  }

  private int setLeadingComments(final Token previousToken, final Token startToken,
      final List<Token> comments) {
    final int skipLine = previousToken.getLine();
    final StringBuilder builder = new StringBuilder();
    int currentLine = startToken.getLine();
    int index = comments.size();

    for (final ListIterator<Token> iterator = comments.listIterator(comments.size()); iterator
        .hasPrevious();) {
      final Token token = iterator.previous();

      if (token.getLine() == skipLine) {
        break;
      }

      final String text = token.getText();

      // FIXME deal with in beetwen comments intervals and endings!

      if (token.getType() == LINE_COMMENT) {
        if (currentLine - token.getLine() > 1) {
          break;
        }

        builder.append(text, 2, text.length()).append(NEW_LINE);
      } else { // this must be COMMENT!
        if (currentLine - (token.getLine() + getLineCount(text)) > 1) {
          break;
        }

        builder.append(getTrimmedBlockCommentContent(text));
      }

      index--;
      currentLine = token.getLine();
    }

    leadingComments.put(startToken, builder.toString());
    return index;
  }

  // *************************** Comments END **********************************

  // ************************* Path START ***********************

  private static final class Path {
    private final List<Integer> path;

    private Path() {
      path = new ArrayList<Integer>();
    }

    public boolean isEmpty() {
      return path.isEmpty();
    }

    public void pushPath(final Collection<Integer> elementPath) {
      path.addAll(elementPath);
    }

    public boolean popPath(final int subPathSize) {
      path.subList(path.size() - subPathSize, path.size()).clear();
      return !path.isEmpty();
    }

    public Location.Builder setAllPath(final Location.Builder builder) {
      if (builder.getPathCount() > 0) {
        builder.clearPath();
      }

      return builder.addAllPath(path);
    }

    public Location.Builder newLocationBuilder() {
      return Location.newBuilder().addAllPath(path);
    }
  }

  // ************************* Path END ***********************
  // ************************* Scope Management START ****************

  private abstract class Scope {
    /*
     * private static final Set<Descriptor> SCOPE_TYPES = new HashSet<Descriptor>(Arrays.asList(
     * FileDescriptorProto.getDescriptor(), DescriptorProto.getDescriptor(),
     * EnumDescriptorProto.getDescriptor(), ServiceDescriptorProto.getDescriptor(),
     * FileOptions.getDescriptor(), MessageOptions.getDescriptor(), FieldOptions.getDescriptor(),
     * EnumOptions.getDescriptor(), EnumValueOptions.getDescriptor(),
     * ServiceOptions.getDescriptor(), MethodOptions.getDescriptor(),
     * UninterpretedOption.getDescriptor()));
     */
    private final Descriptor protoDescriptor;
    private final Map<Integer, Integer> counters;
    private Scope parent;
    private final int pathSize;

    public Scope(final Descriptor protoDescriptor, final Scope parent,
        final Collection<Integer> scopePath) {
      this.protoDescriptor = protoDescriptor;
      counters = new HashMap<Integer, Integer>();
      this.parent = parent;
      path.pushPath(scopePath);
      pathSize = scopePath.size();
    }

    public Scope popScope() {
      if (parent == null) {
        throw new RuntimeException("cannot pop the root scope");
      }

      path.popPath(pathSize);
      final Scope result = parent;
      parent = null;
      return result;
    }

    public Scope popScope(final Descriptor protoDescriptor) {
      Scope scope = this;

      while (!scope.protoDescriptor.equals(protoDescriptor)) {
        scope = scope.popScope();
      }

      return scope;
    }

    public int addElement(final int fieldNumber) {
      final Integer counter = counters.get(fieldNumber);

      if (counter != null) {
        counters.put(fieldNumber, counter + 1);
        return counter;
      }

      if (protoDescriptor.findFieldByNumber(fieldNumber).isRepeated()) {
        counters.put(fieldNumber, 1);
        return 0;
      }

      return -1;
    }

    public Descriptor getElementType(final int fieldNumber) {
      final FieldDescriptor field = protoDescriptor.findFieldByNumber(fieldNumber);
      return field.getJavaType() == JavaType.MESSAGE ? field.getMessageType() : null;
    }

    public Descriptor getProtoDescriptor() {
      return protoDescriptor;
    }

    public int getElementCount(final int fieldNumber) {
      final Integer count = counters.get(fieldNumber);
      return count == null ? 0 : count;
    }

    public int addEnum() {
      throw new RuntimeException("not applicable in current scope!");
    }

    public int addMessage() {
      throw new RuntimeException("not applicable in current scope!");
    }

    public int addField() {
      throw new RuntimeException("not applicable in current scope!");
    }

    public int addOption(final int optionsFieldNumber) {
      final int index = addElement(optionsFieldNumber);
      currentScope = new OptionScope(this, Arrays.asList(optionsFieldNumber, index));
      return index;
    }

    public int getOptionsFieldNumber() {
      final int optionsFieldNumber = protoDescriptor.findFieldByName("options").getNumber();
      return optionsFieldNumber;
    }

    public int addOptionName() {
      throw new RuntimeException("not applicable in current scope!");
    }
  }

  private class FileScope extends Scope {

    public FileScope() {
      super(FileDescriptorProto.getDescriptor(), null, Collections.<Integer>emptyList());
    }

    @Override
    public int addEnum() {
      return addElement(FileDescriptorProto.ENUM_TYPE_FIELD_NUMBER);
    }

    @Override
    public int addMessage() {
      final int index = addElement(FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER);
      currentScope =
          new MessageScope(this,
              Arrays.asList(FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER, index));
      return index;
    }

    @Override
    public int addField() {
      return addElement(FileDescriptorProto.EXTENSION_FIELD_NUMBER);
    }
  }

  private class MessageScope extends Scope {

    public MessageScope(final Scope parent, final Collection<Integer> scopePath) {
      super(DescriptorProto.getDescriptor(), parent, scopePath);
    }

    @Override
    public int addEnum() {
      return addElement(DescriptorProto.ENUM_TYPE_FIELD_NUMBER);
    }

    @Override
    public int addMessage() {
      final int index = addElement(DescriptorProto.NESTED_TYPE_FIELD_NUMBER);
      currentScope =
          new MessageScope(this, Arrays.asList(DescriptorProto.NESTED_TYPE_FIELD_NUMBER, index));
      return index;
    }

    @Override
    public int addField() {
      return addElement(DescriptorProto.FIELD_FIELD_NUMBER);
    }
  }

  private class OptionScope extends Scope {

    public OptionScope(final Scope parent, final Collection<Integer> scopePath) {
      super(UninterpretedOption.getDescriptor(), parent, scopePath);
    }

    @Override
    public int addOptionName() {
      final int index = addElement(UninterpretedOption.NAME_FIELD_NUMBER);
      currentScope =
          new GeneralScope(UninterpretedOption.NamePart.getDescriptor(), this, Arrays.asList(
              UninterpretedOption.NAME_FIELD_NUMBER, index));
      return index;
    }
  }

  private class GeneralScope extends Scope {

    public GeneralScope(final Descriptor protoDescriptor, final Scope parent,
        final Collection<Integer> scopePath) {
      super(protoDescriptor, parent, scopePath);
    }
  }

  // ************************* Scope Management END ******************

}
