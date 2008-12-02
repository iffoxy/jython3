// Autogenerated AST node
package org.python.antlr.ast;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.python.antlr.PythonTree;
import org.python.antlr.adapter.AstAdapters;
import org.python.core.AstList;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyType;
import org.python.expose.ExposedGet;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedNew;
import org.python.expose.ExposedSet;
import org.python.expose.ExposedType;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

@ExposedType(name = "_ast.keyword", base = PyObject.class)
public class keywordType extends PythonTree {
    public static final PyType TYPE = PyType.fromClass(keywordType.class);
    private String arg;
    public String getInternalArg() {
        return arg;
    }
    @ExposedGet(name = "arg")
    public PyObject getArg() {
        if (arg == null) return Py.None;
        return new PyString(arg);
    }
    @ExposedSet(name = "arg")
    public void setArg(PyObject arg) {
        this.arg = AstAdapters.py2identifier(arg);
    }

    private exprType value;
    public exprType getInternalValue() {
        return value;
    }
    @ExposedGet(name = "value")
    public PyObject getValue() {
        return value;
    }
    @ExposedSet(name = "value")
    public void setValue(PyObject value) {
        this.value = AstAdapters.py2expr(value);
    }


    private final static String[] fields = new String[] {"arg", "value"};
@ExposedGet(name = "_fields")
    public String[] get_fields() { return fields; }

    public keywordType() {
        this(TYPE);
    }
    public keywordType(PyType subType) {
        super(subType);
    }
    @ExposedNew
    @ExposedMethod
    public void Module___init__(PyObject[] args, String[] keywords) {}
    public keywordType(PyObject arg, PyObject value) {
        setArg(arg);
        setValue(value);
    }

    public keywordType(Token token, String arg, exprType value) {
        super(token);
        this.arg = arg;
        this.value = value;
        addChild(value);
    }

    public keywordType(Integer ttype, Token token, String arg, exprType value) {
        super(ttype, token);
        this.arg = arg;
        this.value = value;
        addChild(value);
    }

    public keywordType(PythonTree tree, String arg, exprType value) {
        super(tree);
        this.arg = arg;
        this.value = value;
        addChild(value);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "keyword";
    }

    public String toStringTree() {
        StringBuffer sb = new StringBuffer("keyword(");
        sb.append("arg=");
        sb.append(dumpThis(arg));
        sb.append(",");
        sb.append("value=");
        sb.append(dumpThis(value));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        traverse(visitor);
        return null;
    }

    public void traverse(VisitorIF visitor) throws Exception {
        if (value != null)
            value.accept(visitor);
    }

}
