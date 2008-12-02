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

@ExposedType(name = "_ast.ExceptHandler", base = PyObject.class)
public class ExceptHandler extends excepthandlerType {
public static final PyType TYPE = PyType.fromClass(ExceptHandler.class);
    private exprType excepttype;
    public exprType getInternalExcepttype() {
        return excepttype;
    }
    @ExposedGet(name = "excepttype")
    public PyObject getExcepttype() {
        return excepttype;
    }
    @ExposedSet(name = "excepttype")
    public void setExcepttype(PyObject excepttype) {
        this.excepttype = AstAdapters.py2expr(excepttype);
    }

    private exprType name;
    public exprType getInternalName() {
        return name;
    }
    @ExposedGet(name = "name")
    public PyObject getName() {
        return name;
    }
    @ExposedSet(name = "name")
    public void setName(PyObject name) {
        this.name = AstAdapters.py2expr(name);
    }

    private java.util.List<stmtType> body;
    public java.util.List<stmtType> getInternalBody() {
        return body;
    }
    @ExposedGet(name = "body")
    public PyObject getBody() {
        return new AstList(body, AstAdapters.stmtAdapter);
    }
    @ExposedSet(name = "body")
    public void setBody(PyObject body) {
        this.body = AstAdapters.py2stmtList(body);
    }


    private final static String[] fields = new String[] {"excepttype", "name",
                                                          "body"};
@ExposedGet(name = "_fields")
    public String[] get_fields() { return fields; }

    public ExceptHandler() {
        this(TYPE);
    }
    public ExceptHandler(PyType subType) {
        super(subType);
    }
    @ExposedNew
    @ExposedMethod
    public void Module___init__(PyObject[] args, String[] keywords) {}
    public ExceptHandler(PyObject excepttype, PyObject name, PyObject body) {
        setExcepttype(excepttype);
        setName(name);
        setBody(body);
    }

    public ExceptHandler(Token token, exprType excepttype, exprType name,
    java.util.List<stmtType> body) {
        super(token);
        this.excepttype = excepttype;
        addChild(excepttype);
        this.name = name;
        addChild(name);
        this.body = body;
        if (body == null) {
            this.body = new ArrayList<stmtType>();
        }
        for(PythonTree t : this.body) {
            addChild(t);
        }
    }

    public ExceptHandler(Integer ttype, Token token, exprType excepttype,
    exprType name, java.util.List<stmtType> body) {
        super(ttype, token);
        this.excepttype = excepttype;
        addChild(excepttype);
        this.name = name;
        addChild(name);
        this.body = body;
        if (body == null) {
            this.body = new ArrayList<stmtType>();
        }
        for(PythonTree t : this.body) {
            addChild(t);
        }
    }

    public ExceptHandler(PythonTree tree, exprType excepttype, exprType name,
    java.util.List<stmtType> body) {
        super(tree);
        this.excepttype = excepttype;
        addChild(excepttype);
        this.name = name;
        addChild(name);
        this.body = body;
        if (body == null) {
            this.body = new ArrayList<stmtType>();
        }
        for(PythonTree t : this.body) {
            addChild(t);
        }
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "ExceptHandler";
    }

    public String toStringTree() {
        StringBuffer sb = new StringBuffer("ExceptHandler(");
        sb.append("excepttype=");
        sb.append(dumpThis(excepttype));
        sb.append(",");
        sb.append("name=");
        sb.append(dumpThis(name));
        sb.append(",");
        sb.append("body=");
        sb.append(dumpThis(body));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitExceptHandler(this);
    }

    public void traverse(VisitorIF visitor) throws Exception {
        if (excepttype != null)
            excepttype.accept(visitor);
        if (name != null)
            name.accept(visitor);
        if (body != null) {
            for (PythonTree t : body) {
                if (t != null)
                    t.accept(visitor);
            }
        }
    }

    private int lineno = -1;
@ExposedGet(name = "lineno")
    public int getLineno() {
        if (lineno != -1) {
            return lineno;
        }
        return getLine();
    }

@ExposedSet(name = "lineno")
    public void setLineno(int num) {
        lineno = num;
    }

    private int col_offset = -1;
@ExposedGet(name = "col_offset")
    public int getCol_offset() {
        if (col_offset != -1) {
            return col_offset;
        }
        return getCharPositionInLine();
    }

@ExposedSet(name = "col_offset")
    public void setCol_offset(int num) {
        col_offset = num;
    }

}
