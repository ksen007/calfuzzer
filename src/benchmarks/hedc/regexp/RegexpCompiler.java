/*
 * @(#)RegexpCompiler.java	1.3 96/02/02
 *
 * Copyright (c) 1995 Starwave Corporation.  All Rights Reserved.
 *
 * A perl-like regular expression compiler for java.
 *
 * @version 1.3, 02 Feb 1996
 * @author Jonathan Payne
 */

/* Regular expression compiler for java. */

package benchmarks.hedc.regexp;

/** Regular expression compiler. */

class CompilerState {
    String input;
    int offset;
    int groupCount = 0;
    int limit;
    boolean mapCase;
    boolean eof = false;

    CompilerState(String input, boolean mapCase) {
	this.input = input;
	this.mapCase = mapCase;
	limit = input.length();
    }

    final int nextChar() {
	if (offset < limit)
	    return input.charAt(offset++);
	eof = true;
	return -1;
    }

    final int currentChar() {
	if (eof)
	    return -1;
	return input.charAt(offset - 1);
    }

    final void ungetc() {
	eof = false;
	offset -= 1;
    }

    final int nextGroup() {
	return groupCount++;
    }

    final String substring(int from) {
	return input.substring(from, offset);
    }

    final boolean atEop() {
	return offset == limit;
    }

    public String toString() {
	return eof ? "EOF" : input.substring(offset);
    }
}

class RegexpCompiler {
    static Regexp compile(String expr, boolean mapCase) {
	CompilerState state = new CompilerState(expr, mapCase);

	Regexp first, reg;
	first = new Regexp(null);
	reg = compileAlternatives(first, state, -1);
	reg.next = Regexp.success;
	return first.next;
    }

    static Regexp compileAlternatives(Regexp prev, CompilerState state,
				      int term) {
	int group = state.nextGroup();
	prev = new Group(prev, '(', group);
	Alternatives alts = new Alternatives(prev);
	Regexp reg, last;

	int c;
	last = new Group(null, ')', group);
	
	do {
	    reg = compileAlternative(state, last);
	    c = state.currentChar();

	    if (c != term && c != '|')
		throw new MalformedRegexpException((char) c + " unexpected");
	    if (reg == null)
		break;
	    alts.addAlt(reg);
	} while (c != term);
	return last;
    }

    /** Compile one alternative, link it to the specified end. */
    static Regexp compileAlternative(CompilerState state, Regexp end) {
	Regexp root, last;
	int c;

	root = last = new Regexp(null);
	/* create dummy first element that we can delete at end */
	try {
	  loop:
	    while (true) {
		/* NOTE: in this switch statement, breaking from switch means
		   treat character as literal.  Use continue if you create
		   your own Regexp component. */
		switch (c = state.nextChar()) {
		  case '.':
		    last = new Dot(last);
		    continue;

		  case '+':
		  case '?':
		  case '*':
		    if (last.prev == null)
			break;	/* treat as literal */
		    if (!last.canStar())
			throw new MalformedRegexpException
			    ("cannot " + (char) c
			     + " " + last + "'s");
		    last = last.makeMulti(c);
		    continue;

		  case '[':
		    int start = state.offset - 1;
innerloop:		    while (true) {
			switch (c = state.nextChar()) {
			  case -1:
			    throw new MalformedRegexpException("Missing ]");

			  case ']':
			    break innerloop;

			  case '\\':
			    state.nextChar();
			    break;

			  default:
			    break;
			}
		    }
		    last = new CharClass(last, state.substring(start));
		    continue;

		  case '$':
		    if (state.atEop()) {
			last = new ContextMatch(last, c);
			continue;
		    }
		    break;

		  case '^':
		    if (state.offset == 1) {
			last = new ContextMatch(last, c);
			continue;
		    }
		    break;

		  case '(':
		    last = compileAlternatives(last, state, ')');
		    continue;

		  case '|':
		  case ')':
		  case -1:
		    break loop;

		  case '\\':
		    switch (c = state.nextChar()) {
		      case 's':	/* white space */
		      case 'S':	/* non-white space */
		      case 'w':	/* word chars */
		      case 'W':	/* non-word chars */
		      case 'd':	/* digits */
		      case 'D':	/* nondigits */
			last = CharClass.cloneCharClass(last, c);
			continue;

		      case 'b':
		      case 'B':
			last = new ContextMatch(last, c);
			continue;

		      case 'n':
			c = '\n';
			break;

		      case 'r':
			c = '\r';
			break;

		      case 'f':
			c = '\f';
			break;

		      case 't':
			c = '\t';
			break;

		      case '0':
		      case '1':
		      case '2':
		      case '3':
		      case '4':
		      case '5':
		      case '6':
		      case '7':
		      case '8':
		      case '9':
			/* REMIND: This doesn't catch at compile time
			   references to unfinished groups. */
			if (state.groupCount < (c - '0'))
			    throw new MalformedRegexpException("illegal forward reference: \\" + new Character((char) c));

			last = new GroupReference(last, c - '0');
			continue;
		    }
		    break;	/* do the default thang */

		  default:
		    break;
		}
		if (last instanceof Literal) 
		    ((Literal) last).appendChar(c);
		else
		    last = new Literal(last, c, state.mapCase);
	    }
	    last.next = end;
	    return root.next;
	} catch (MalformedRegexpException e) {
	    throw e;
	} catch (Exception e) {
	    System.out.println("#RegexpCompiler::compile - exception e="+e);
	    throw new MalformedRegexpException("near "
					       + state.substring(state.offset - 1));
	}
    }
}

