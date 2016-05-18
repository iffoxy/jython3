/*
 * Copyright (c) Corporation for National Research Initiatives
 * Copyright (c) Jython Developers
 */
package org.python.core;

import org.python.expose.ExposedDelete;
import org.python.expose.ExposedGet;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedSet;
import org.python.expose.ExposedType;

import java.util.LinkedList;

/**
 * A Python frame object.
 */
@ExposedType(name = "frame", isBaseType = false)
public class PyFrame extends PyObject implements Traverseproc {

    public static final PyType TYPE = PyType.fromClass(PyFrame.class);

    /** yield from generator */
    public PyObject f_yieldfrom;

    /**
     *  unused value, normally because yield from subgenerator
     *  has to return before the stack top is handled
     */
    public PyObject f_stacktop = Py.None;

    /** Previous frame or null. */
    @ExposedGet
    public PyFrame f_back;

    /** The underyling code object. */
    @ExposedGet
    public PyBaseCode f_code;

    /** builtin symbol table. */
    @ExposedGet
    public PyObject f_builtins;

    /** Global symbol table. */
    @ExposedGet
    public PyObject f_globals;

    /** Local symbol table. */
    public PyObject f_locals;

    /** Current line number. */
    public int f_lineno;

    public PyObject[] f_fastlocals;

    /** Nested scopes: cell + free env. */
    public PyCell[] f_env;

    private int env_j = 0;

    public int f_ncells;

    public int f_nfreevars;

    @ExposedGet
    public int f_lasti;

    public Object[] f_savedlocals;

    private Object generatorInput = Py.None;

    /** with context exits - used by generated bytecode */
    public PyObject[] f_exits;

    /** An interface to functions suitable for tracing, e.g. via sys.settrace(). */
    public TraceFunction tracefunc;

    private static final String NAME_ERROR_MSG = "name '%.200s' is not defined";

    private static final String GLOBAL_NAME_ERROR_MSG = "global name '%.200s' is not defined";

    private static final String UNBOUNDLOCAL_ERROR_MSG =
            "local variable '%.200s' referenced before assignment";

    public PyFrame(PyBaseCode code, PyObject locals, PyObject globals, PyObject builtins) {
        super(TYPE);
        f_code = code;
        f_locals = locals;
        f_globals = globals;
        f_builtins = builtins;
        // This needs work to be efficient with multiple interpreter states
        if (locals == null && code != null) {
            // ! f_fastlocals needed for arg passing too
            if (code.co_flags.isFlagSet(CodeFlag.CO_OPTIMIZED) || code.nargs > 0) {
                if (code.co_nlocals > 0) {
                    // internal: may change
                    f_fastlocals = new PyObject[code.co_nlocals - code.jy_npurecell];
                }
            } else {
                f_locals = new PyStringMap();
            }
        }
        if (code != null) { // reserve space for env
            int env_sz = 0;
            if (code.co_freevars != null) {
                env_sz += (f_nfreevars = code.co_freevars.length);
            }
            if (code.co_cellvars != null) {
                env_sz += (f_ncells = code.co_cellvars.length);
            }
            if (env_sz > 0) {
                f_env = new PyCell[env_sz];
            }
        }
    }

    public PyFrame(PyBaseCode code, PyObject globals) {
        this(code, null, globals, null);
    }

    /**
     * Populate the frame with closure variables, but at most once.
     *
     * @param freevars a <code>PyTuple</code> value
     */
    void setupEnv(PyTuple freevars) {
        int ntotal = f_ncells + f_nfreevars;
        // add space for the cellvars
        for (; env_j < f_ncells; env_j++) {
            f_env[env_j] = new PyCell();
        }
        // inherit the freevars
        for (int i = 0; env_j < ntotal; i++, env_j++) {
            f_env[env_j] = (PyCell)freevars.pyget(i);
        }
    }

    void setGeneratorInput(Object value) {
        generatorInput = value;
        if (f_yieldfrom != null && f_yieldfrom instanceof PyGenerator) {
            PyFrame yf_frame = ((PyGenerator) f_yieldfrom).gi_frame;
            if (yf_frame != null) {
                yf_frame.setGeneratorInput(value);
            }
        }
    }

    public Object getGeneratorInput() {
        Object input = generatorInput;
        generatorInput = Py.None;
        return input;
    }

    public PyObject getf_stacktop() {
        PyObject ret = f_stacktop;
        f_stacktop = Py.None;
        return ret;
    }

    public Object checkGeneratorInput() {
        return generatorInput;
    }

    /**
     * Return the locals dict. First merges the fast locals into
     * f_locals, then returns the updated f_locals.
     *
     * @return a PyObject mapping of locals
     */
    @ExposedGet(name = "f_locals")
    public PyObject getLocals() {
        if (f_locals == null) {
            f_locals = new PyStringMap();
        }
        if (f_code != null && (f_code.co_nlocals > 0 || f_nfreevars > 0)) {
            int i;
            if (f_fastlocals != null) {
                for (i = 0; i < f_fastlocals.length; i++) {
                    PyObject o = f_fastlocals[i];
                    if (o != null) f_locals.__setitem__(f_code.co_varnames[i], o);
                }
                if (!f_code.co_flags.isFlagSet(CodeFlag.CO_OPTIMIZED)) {
                    f_fastlocals = null;
                }
            }
            int j = 0;
            for (i = 0; i < f_ncells; i++, j++) {
                PyObject v = f_env[j].ob_ref;
                if (v != null) {
                    f_locals.__setitem__(f_code.co_cellvars[i], v);
                }
            }
            for (i = 0; i < f_nfreevars; i++, j++) {
                PyObject v = f_env[j].ob_ref;
                if (v != null) {
                    f_locals.__setitem__(f_code.co_freevars[i], v);
                }
            }
        }
        return f_locals;
    }

    @ExposedGet(name = "f_trace")
    public PyObject getTrace() {
        return tracefunc instanceof PythonTraceFunction ?
                ((PythonTraceFunction)tracefunc).tracefunc : Py.None;
    }

    @ExposedSet(name = "f_trace")
    public void setTrace(PyObject trace) {
        tracefunc = new PythonTraceFunction(trace);
    }

    @ExposedDelete(name = "f_trace")
    public void delTrace() {
        tracefunc = null;
    }

    /**
     * Return the current f_locals dict.
     *
     * @return a PyObject mapping of locals
     */
    public PyObject getf_locals() {
        // XXX: This could be deprecated, grab f_locals directly
        // instead. only the compiler calls this
        return f_locals;
    }

    /**
     * Track the current line number. Called by generated code.
     *
     * This is not to be confused with the CPython method
     * frame_setlineno() which causes the interpreter to jump to
     * the given line.
     */
    public void setline(int line) {
        f_lineno = line;
        if (tracefunc != null) {
            tracefunc = tracefunc.traceLine(this, line);
        }
    }

    @ExposedGet(name = "f_lineno")
    public int getline() {
        return tracefunc != null ? f_lineno : f_code.getline(this);
    }

    public PyObject getlocal(int index) {
        if (f_fastlocals != null) {
            PyObject ret = f_fastlocals[index];
            if (ret != null) {
                return ret;
            }
        }

        String name = f_code.co_varnames[index];
        if (f_locals != null) {
            PyObject ret = f_locals.__finditem__(name);
            if (ret != null) {
                return ret;
            }
        }
        throw Py.UnboundLocalError(String.format(UNBOUNDLOCAL_ERROR_MSG, name));
    }

    public PyObject getname(String index) {
        PyObject ret;
        if (f_locals == null || f_locals == f_globals) {
            ret = doGetglobal(index);
        } else {
            ret = f_locals.__finditem__(index);
            if (ret != null) {
                return ret;
            }
            ret = doGetglobal(index);
        }
        if (ret != null) {
            return ret;
        }
        throw Py.NameError(String.format(NAME_ERROR_MSG, index));
    }

    public PyObject getglobal(String index) {
        PyObject ret = doGetglobal(index);
        if (ret != null) {
            return ret;
        }
        throw Py.NameError(String.format(GLOBAL_NAME_ERROR_MSG, index));
    }

    private PyObject doGetglobal(String index) {
        PyObject ret = f_globals.__finditem__(index);
        if (ret != null) {
            return ret;
        }

        // Set up f_builtins if not already set
        if (f_builtins == null) {
            f_builtins = Py.getThreadState().systemState.builtins;
        }
        return f_builtins.__finditem__(index);
    }

    public void setlocal(int index, PyObject value) {
        if (f_fastlocals != null) {
            f_fastlocals[index] = value;
        } else {
            setlocal(f_code.co_varnames[index], value);
        }
    }

    public void setlocal(String index, PyObject value) {
        if (f_locals != null) {
            f_locals.__setitem__(index, value);
        } else {
            throw Py.SystemError(String.format("no locals found when storing '%s'", value));
        }
    }

    public void setglobal(String index, PyObject value) {
        f_globals.__setitem__(index, value);
    }

    public void dellocal(int index) {
        if (f_fastlocals != null) {
            if (f_fastlocals[index] == null) {
                throw Py.UnboundLocalError(String.format(UNBOUNDLOCAL_ERROR_MSG,
                                                         f_code.co_varnames[index]));
            }
            f_fastlocals[index] = null;
        } else {
            dellocal(f_code.co_varnames[index]);
        }
    }

    public void dellocal(String index) {
        if (f_locals != null) {
            try {
                f_locals.__delitem__(index);
            } catch (PyException pye) {
                if (pye.match(Py.KeyError)) {
                    throw Py.NameError(String.format(NAME_ERROR_MSG, index));
                }
                throw pye;
            }
        } else {
            throw Py.SystemError(String.format("no locals when deleting '%s'", index));
        }
    }

    public void delglobal(String index) {
        try {
            f_globals.__delitem__(index);
        } catch (PyException pye) {
            if (pye.match(Py.KeyError)) {
                throw Py.NameError(String.format(GLOBAL_NAME_ERROR_MSG, index));
            }
            throw pye;
        }
    }

    public PyObject clear() {
        return frame_clear();
    }

    @ExposedMethod(doc = BuiltinDocs.frame_clear_doc)
    final PyObject frame_clear() {
        f_locals = null;
        // XXX clean associated generator?
        return Py.None;
    }

    // nested scopes helpers

    public PyObject getclosure(int index) {
        return f_env[index];
    }

    public PyObject getderef(int index) {
        PyObject obj = f_env[index].ob_ref;
        if (obj != null) {
            return obj;
        }
        String name;
        if (index >= f_ncells) {
            name = f_code.co_freevars[index - f_ncells];
        } else {
            name = f_code.co_cellvars[index];
        }
        throw Py.UnboundLocalError(String.format(UNBOUNDLOCAL_ERROR_MSG, name));
    }

    public void setderef(int index, PyObject value) {
        f_env[index].ob_ref = value;
    }

    public void to_cell(int parm_index, int env_index) {
        f_env[env_index].ob_ref = f_fastlocals[parm_index];
    }


    /* Traverseproc implementation */
    @Override
    public int traverse(Visitproc visit, Object arg) {
        int retVal;
        if (f_back != null) {
            retVal = visit.visit(f_back, arg);
            if (retVal != 0) {
                return retVal;
            }
        }
        if (f_code != null) {
            retVal = visit.visit(f_code, arg);
            if (retVal != 0) {
                return retVal;
            }
        }
        if (f_builtins != null) {
            retVal = visit.visit(f_builtins, arg);
            if (retVal != 0) {
                return retVal;
            }
        }
        if (f_globals != null) {
            retVal = visit.visit(f_globals, arg);
            if (retVal != 0) {
                return retVal;
            }
        }
        if (f_locals != null) {
            retVal = visit.visit(f_locals, arg);
            if (retVal != 0) {
                return retVal;
            }
        }
        if (tracefunc != null && tracefunc instanceof Traverseproc) {
            retVal = ((Traverseproc) tracefunc).traverse(visit, arg);
            if (retVal != 0) {
                   return retVal;
               }
        }

//      CPython also features fields for an exception.
//        These are not present in PyFrame in Jython:
//        Py_VISIT(f->f_exc_type);
//        Py_VISIT(f->f_exc_value);
//        Py_VISIT(f->f_exc_traceback);

        /* locals */
        if (f_fastlocals != null) {
            for (PyObject ob: f_fastlocals) {
                if (ob != null) {
                    retVal = visit.visit(ob, arg);
                    if (retVal != 0) {
                        return retVal;
                    }
                }
            }
        }

        if (f_savedlocals != null) {
            for (Object ob: f_savedlocals) {
                if (ob != null) {
                    if (ob instanceof PyObject) {
                        retVal = visit.visit((PyObject) ob, arg);
                        if (retVal != 0) {
                            return retVal;
                        }
                    } else if (ob instanceof Traverseproc) {
                        retVal = ((Traverseproc) ob).traverse(visit, arg);
                        if (retVal != 0) {
                            return retVal;
                        }
                    }
                }
            }
        }

        /* Jython-only miscellaneous */
        if (f_env != null) {
            for (PyCell ob: f_env) {
                if (ob != null) {
                    retVal = visit.visit(ob, arg);
                    if (retVal != 0) {
                        return retVal;
                    }
                }
            }
        }

        if (generatorInput != null) {
            if (generatorInput instanceof PyObject) {
                retVal = visit.visit((PyObject) generatorInput, arg);
                if (retVal != 0) {
                    return retVal;
                }
            } else if (generatorInput instanceof Traverseproc) {
                retVal = ((Traverseproc) generatorInput).traverse(visit, arg);
                if (retVal != 0) {
                    return retVal;
                }
            }
        }

        if (f_exits != null) {
            for (PyObject ob: f_exits) {
                if (ob != null) {
                    retVal = visit.visit(ob, arg);
                    if (retVal != 0) {
                        return retVal;
                    }
                }
            }
        }

//      CPython also traverses the stack. This seems to be not necessary
//      in Jython since there is no stack-equivalent in PyFrame:
//
//      /* stack */
//      if (f->f_stacktop != NULL) {
//          for (p = f->f_valuestack; p < f->f_stacktop; p++)
//              Py_VISIT(*p);
//      }
        return 0;
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) throws UnsupportedOperationException {
        if (ob == null) {
            return false;
        } else if (ob == f_back || ob == f_code || ob == f_builtins
            || ob == f_globals || ob == f_locals || ob == generatorInput) {
            return true;
        }

        if (f_fastlocals != null) {
            for (PyObject obj: f_fastlocals) {
                if (obj == ob) {
                    return true;
                }
            }
        }
        if (f_env != null) {
            for (PyObject obj: f_env) {
                if (obj == ob) {
                    return true;
                }
            }
        }
        if (f_exits != null) {
            for (PyObject obj: f_exits) {
                if (obj == ob) {
                    return true;
                }
            }
        }
        if (f_savedlocals != null) {
            for (Object obj: f_savedlocals) {
                if (obj == ob) {
                    return true;
                } else if (obj != null && obj instanceof Traverseproc
                        &&((Traverseproc) obj).refersDirectlyTo(ob)) {
                    return true;
                }
            }
        }
        if (tracefunc != null && tracefunc instanceof Traverseproc
                &&((Traverseproc) tracefunc).refersDirectlyTo(ob)) {
            return true;
        }
        return generatorInput instanceof Traverseproc ?
            ((Traverseproc) generatorInput).refersDirectlyTo(ob) : false;
    }
}
