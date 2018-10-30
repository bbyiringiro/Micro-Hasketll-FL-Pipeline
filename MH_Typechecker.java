
// File:   MH_Typechecker.java

// Java template file for typechecker component of Informatics 2A Assignment 1.
// Provides infrastructure for Micro-Haskell typechecking:
// the core typechecking operation is to be implemented by students.

import java.io.*;
import java.util.*;

class MH_Typechecker {

	static MH_Parser MH_Parser1 = MH_Type_Impl.MH_Parser1;

	// The core of the typechecker:
	// Computing the MH_TYPE of a given MH_EXP in a given TYPE_ENV.
	// Should raise TypeError if MH_EXP isn't well-typed

	// Examples illustrating how the MH_Exp operations are used:
	// If e is the MH_EXP tree for "if e_1 then e_2 else e_3", then
	// - e.first() returns the tree for e_1
	// - e.second() returns the tree for e_2
	// - e.third() returns the tree for e_3
	// - e.value() and e.infixOp() won't return anything sensible.
	// If e is the MH_EXP tree for "e_1 e_2" (function application), then
	// - e.first() returns the tree for e_1
	// - e.second() returns the tree for e_2
	// - e.third(), e.value() and e.infixOp() won't return anything sensible
	// If e is the MH_EXP tree for "e_1 == e_2", then
	// - e.first() returns the tree for e_1
	// - e.second() returns the tree for e_2
	// - e.infixOp() returns the string "==", identifying the infix involved
	// - e.third() and e.value() won't return anything sensible
	// and similarly for the other infix operations.
	// If e is the MH_EXP tree for a VAR, NUM or BOOLEAN, then
	// - e.value() returns its string representation, e.g. "x" or "42" or "True"
	// - e.first(), e.second(), e.third(), e.infixOp() won't return
	// anything sensible.

	static MH_TYPE IntegerType = MH_Type_Impl.IntegerType;
	static MH_TYPE BoolType = MH_Type_Impl.BoolType;

	static MH_TYPE computeType(MH_EXP exp, TYPE_ENV env) throws TypeError, UnknownVariable {
		// it checks expression and to recursion untill it gets to the base case and check if it of correct type depending on where it is in the program
		if (exp.isVAR()) {
			return env.typeOf(exp.value());
		} else if (exp.isBOOLEAN()) {
			return BoolType;
		} else if (exp.isNUM()) {
			return IntegerType;
		} else if (exp.isAPP()) {
			if(computeType(exp.first(), env).isFun())
				if (computeType(exp.first(), env).left().equals(computeType(exp.second(), env)))
					return computeType(exp.first(), env).right();
				else
					throw new TypeError("the function" + exp.first().toString(null) + "accepts the type of "+ computeType(exp.first(), env).left()+" but it got"+ 
					computeType(exp.second(), env).toString()+ "from the expression" +exp.second().toString(null));
			else
				throw new TypeError(exp.first().toString(null)+ " is expected to be a function");


		} else if (exp.isINFIX()) {
			MH_TYPE temp = computeType(exp.first(), env);
			if (exp.infixOp().equals("==") || exp.infixOp().equals("<=")) {
				if (temp.equals(computeType(exp.second(), env)) && temp.equals(IntegerType))
					return BoolType;
				else
					throw new TypeError(exp.first().toString(null) + " and " + exp.second().toString(null)
							+ " should be of the same type == operator to work");
			} else if (exp.infixOp().equals("+") || exp.infixOp().equals("-")) {
				if (temp.equals(computeType(exp.second(), env)) && temp.equals(IntegerType))
					return IntegerType;
				else
					throw new TypeError(exp.first().toString(null) + " and " + exp.second().toString(null)
							+ " should be of the same type == operator to work");
			} else
				throw new TypeError(exp.infixOp().toString() + " operator used is not recognized");

		} else if (exp.isIF()) {
			if (computeType(exp.first(), env).equals(BoolType)) {
				if (computeType(exp.second(), env).equals(computeType(exp.third(), env)))
					return computeType(exp.second(), env);
				else
					throw new TypeError("Both expressions" +exp.first().toString(null)+" and " + exp.second().toString(null)+" after if x then and  after else should be of same type");
			} else
				throw new TypeError("The if expression "+exp.first().toString(null)+" should of a Boolean type");
		} else {
			throw new TypeError(exp.toString(null) + " Expression not recognized! ");
		}

	}

	// Type environments:

	interface TYPE_ENV {
		MH_TYPE typeOf(String var) throws UnknownVariable;
	}

	static class MH_Type_Env implements TYPE_ENV {

		TreeMap<String, MH_TYPE> env;

		public MH_TYPE typeOf(String var) throws UnknownVariable {
			MH_TYPE t = (MH_TYPE) (env.get(var));
			if (t == null)
				throw new UnknownVariable(var);
			else
				return t;
		}

		// Constructor for cloning a type env
		MH_Type_Env(MH_Type_Env given) {
			this.env = new TreeMap<String, MH_TYPE>();
			this.env.putAll(given.env);
			// Old version (causes unchecked typecast warning):
			// this.env = (TreeMap<String,MH_TYPE>)given.env.clone() ;
		}

		// Constructor for building a type env from the type decls
		// appearing in a program
		MH_Type_Env(TREE prog) throws DuplicatedVariable {
			this.env = new TreeMap<String, MH_TYPE>();
			TREE prog1 = prog;
			while (prog1.getRhs() != MH_Parser.epsilon) {
				TREE typeDecl = prog1.getChildren()[0].getChildren()[0];
				String var = typeDecl.getChildren()[0].getValue();
				MH_TYPE theType = MH_Type_Impl.convertType(typeDecl.getChildren()[2]);
				if (env.containsKey(var))
					throw new DuplicatedVariable(var);
				else
					env.put(var, theType);
				prog1 = prog1.getChildren()[1];
			}
			System.out.println("Type conversions successful.");
		}

		// Augmenting a type env with a list of function arguments.
		// Takes the type of the function, returns the result type.
		MH_TYPE addArgBindings(TREE args, MH_TYPE theType) throws DuplicatedVariable, TypeError {
			TREE args1 = args;
			MH_TYPE theType1 = theType;
			while (args1.getRhs() != MH_Parser.epsilon) {
				if (theType1.isFun()) {
					String var = args1.getChildren()[0].getValue();
					if (env.containsKey(var)) {
						throw new DuplicatedVariable(var);
					} else {
						this.env.put(var, theType1.left());
						theType1 = theType1.right();
						args1 = args1.getChildren()[1];
					}
				} else
					throw new TypeError("Too many function arguments");
			}
			;
			return theType1;
		}
	}

	static MH_Type_Env compileTypeEnv(TREE prog) throws DuplicatedVariable {
		return new MH_Type_Env(prog);
	}

	// Building a closure (using lambda) from argument list and body
	static MH_EXP buildClosure(TREE args, MH_EXP exp) {
		if (args.getRhs() == MH_Parser.epsilon)
			return exp;
		else {
			MH_EXP exp1 = buildClosure(args.getChildren()[1], exp);
			String var = args.getChildren()[0].getValue();
			return new MH_Exp_Impl(var, exp1);
		}
	}

	// Name-closure pairs (result of processing a TermDecl).
	static class Named_MH_EXP {
		String name;
		MH_EXP exp;

		Named_MH_EXP(String name, MH_EXP exp) {
			this.name = name;
			this.exp = exp;
		}
	}

	static Named_MH_EXP typecheckDecl(TREE decl, MH_Type_Env env)
			throws TypeError, UnknownVariable, DuplicatedVariable, NameMismatchError {
		// typechecks the given decl against the env,
		// and returns a name-closure pair for the entity declared.
		String theVar = decl.getChildren()[0].getChildren()[0].getValue();
		String theVar1 = decl.getChildren()[1].getChildren()[0].getValue();
		if (!theVar.equals(theVar1))
			throw new NameMismatchError(theVar, theVar1);
		MH_TYPE theType = MH_Type_Impl.convertType(decl.getChildren()[0].getChildren()[2]);
		MH_EXP theExp = MH_Exp_Impl.convertExp(decl.getChildren()[1].getChildren()[3]);
		TREE theArgs = decl.getChildren()[1].getChildren()[1];
		MH_Type_Env theEnv = new MH_Type_Env(env);
		MH_TYPE resultType = theEnv.addArgBindings(theArgs, theType);
		MH_TYPE expType = computeType(theExp, theEnv);
		if (expType.equals(resultType)) {
			return new Named_MH_EXP(theVar, buildClosure(theArgs, theExp));
		} else
			throw new TypeError("RHS of declaration of " + theVar + " has wrong type");
	}

	static MH_Exp_Env typecheckProg(TREE prog, MH_Type_Env env)
			throws TypeError, UnknownVariable, DuplicatedVariable, NameMismatchError {
		TREE prog1 = prog;
		TreeMap<String, MH_EXP> treeMap = new TreeMap<String, MH_EXP>();
		while (prog1.getRhs() != MH_Parser.epsilon) {
			TREE theDecl = prog1.getChildren()[0];
			Named_MH_EXP binding = typecheckDecl(theDecl, env);
			treeMap.put(binding.name, binding.exp);
			prog1 = prog1.getChildren()[1];
		}
		System.out.println("Typecheck successful.");
		return new MH_Exp_Env(treeMap);
	}

	// For testing:

	public static void main(String[] args) throws Exception {
		Reader reader = new BufferedReader(new FileReader(args[0]));
		// try {
		LEX_TOKEN_STREAM MH_Lexer = new CheckedSymbolLexer(new MH_Lexer(reader));
		TREE prog = MH_Parser1.parseTokenStream(MH_Lexer);
		MH_Type_Env typeEnv = compileTypeEnv(prog);
		MH_Exp_Env runEnv = typecheckProg(prog, typeEnv);
		// } catch (Exception x) {
		// System.out.println ("MH Error: " + x.getMessage()) ;
		// }
	}
}
