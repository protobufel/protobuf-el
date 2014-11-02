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

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;

public class GUITestParser1 {

  public static void main(final String[] args) throws Exception {
    String inputFile = "test0.proto";
    boolean isAbsolute = false;

    for (final String arg : args) {
      if (arg.equalsIgnoreCase("-a")) {
        isAbsolute = true;
      }

      if (!arg.startsWith("-")) {
        inputFile = arg;
      }
    }

    if (!isAbsolute) {
      inputFile = getResourceFileName(inputFile);
    }

    final ANTLRInputStream input = new ANTLRFileStream(inputFile);
    final ProtoLexer lexer = new ProtoLexer(input);
    final CommonTokenStream tokens = new CommonTokenStream(lexer);
    final ProtoParser parser = new ProtoParser(tokens);
    parser.setBuildParseTree(true);
    final RuleContext tree = parser.proto();
    tree.inspect(parser); // show in gui
    // tree.save(parser, "/tmp/R.ps"); // Generate postscript
    System.out.println(tree.toStringTree(parser));
  }

  private static String getResourceFileName(final String resource) {
    return GUITestParser1.class.getResource(resource).getFile();
  }
}
