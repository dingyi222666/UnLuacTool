package unluac.assemble;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import unluac.decompile.CodeExtract;
import unluac.decompile.Op;
import unluac.decompile.OperandFormat;
import unluac.util.StringUtils;

public class AssemblerFunction {

    class FunctionFixup {

        int code_index;
        String function;
        CodeExtract.Field field;

    }

    class JumpFixup {

        int code_index;
        String label;
        CodeExtract.Field field;
        boolean negate;

    }

    public AssemblerChunk chunk;
    public AssemblerFunction parent;
    public String name;
    public List<AssemblerFunction> children;

    public boolean hasSource;
    public String source;

    public boolean hasLineDefined;
    public int linedefined;

    public boolean hasLastLineDefined;
    public int lastlinedefined;

    public boolean hasMaxStackSize;
    public int maxStackSize;

    public boolean hasNumParams;
    public int numParams;

    public boolean hasVararg;
    public int vararg;

    public List<AssemblerLabel> labels;
    public List<AssemblerConstant> constants;
    public List<AssemblerUpvalue> upvalues;
    public List<Integer> code;
    public List<Integer> lines;
    public List<AssemblerAbsLineInfo> abslineinfo;
    public List<AssemblerLocal> locals;

    public List<FunctionFixup> f_fixup;
    public List<JumpFixup> j_fixup;

    public AssemblerFunction(AssemblerChunk chunk, AssemblerFunction parent, String name) {
        this.chunk = chunk;
        this.parent = parent;
        this.name = name;
        children = new ArrayList<AssemblerFunction>();

        hasSource = false;
        hasLineDefined = false;
        hasLastLineDefined = false;
        hasMaxStackSize = false;
        hasNumParams = false;
        hasVararg = false;

        labels = new ArrayList<AssemblerLabel>();
        constants = new ArrayList<AssemblerConstant>();
        upvalues = new ArrayList<AssemblerUpvalue>();
        code = new ArrayList<Integer>();
        lines = new ArrayList<Integer>();
        abslineinfo = new ArrayList<AssemblerAbsLineInfo>();
        locals = new ArrayList<AssemblerLocal>();

        f_fixup = new ArrayList<FunctionFixup>();
        j_fixup = new ArrayList<JumpFixup>();
    }

    public AssemblerFunction addChild(String name) {
        AssemblerFunction child = new AssemblerFunction(chunk, this, name);
        children.add(child);
        return child;
    }

    public AssemblerFunction getInnerParent(String[] parts, int index) throws AssemblerException {
        if (index + 1 == parts.length) return this;
        for (AssemblerFunction child : children) {
            if (child.name.equals(parts[index])) {
                return child.getInnerParent(parts, index + 1);
            }
        }
        throw new AssemblerException("Can't find outer function");
    }

    public AssemblerFunction getInnerChild(String[] parts, int index) throws AssemblerException {

        for (AssemblerFunction child : children) {
            if (child.name.equals(parts[index])) {
                return child;
            }
        }
        for (AssemblerFunction child : children) {
            AssemblerFunction inner = child.getInnerChild(parts, index + 1);
            if (inner != null) return inner;
        }
        throw new AssemblerException("Can't find inner function");
    }

    public void processFunctionDirective(Assembler a, Directive d) throws AssemblerException, IOException {
        switch (d) {
            case SOURCE:
                if (hasSource) throw new AssemblerException("Duplicate .source directive");
                hasSource = true;
                source = a.getString();
                break;
            case LINEDEFINED:
                if (hasLineDefined)
                    throw new AssemblerException("Duplicate .linedefined directive");
                hasLineDefined = true;
                linedefined = a.getInteger();
                break;
            case LASTLINEDEFINED:
                if (hasLastLineDefined)
                    throw new AssemblerException("Duplicate .lastlinedefined directive");
                hasLastLineDefined = true;
                lastlinedefined = a.getInteger();
                break;
            case MAXSTACKSIZE:
                if (hasMaxStackSize)
                    throw new AssemblerException("Duplicate .maxstacksize directive");
                hasMaxStackSize = true;
                maxStackSize = a.getInteger();
                break;
            case NUMPARAMS:
                if (hasNumParams) throw new AssemblerException("Duplicate .numparams directive");
                hasNumParams = true;
                numParams = a.getInteger();
                break;
            case IS_VARARG:
                if (hasVararg) throw new AssemblerException("Duplicate .is_vararg directive");
                hasVararg = true;
                vararg = a.getInteger();
                break;
            case LABEL: {
                String name = a.getAny();
                AssemblerLabel label = new AssemblerLabel();
                label.name = name;
                label.code_index = code.size();
                labels.add(label);
                break;
            }
            case CONSTANT: {
                String name = a.getName();
                String value = a.getAny();
                AssemblerConstant constant = new AssemblerConstant();
                constant.name = name;
                if (value.equals("nil")) {
                    constant.type = AssemblerConstant.Type.NIL;
                } else if (value.equals("true")) {
                    constant.type = AssemblerConstant.Type.BOOLEAN;
                    constant.booleanValue = true;
                } else if (value.equals("false")) {
                    constant.type = AssemblerConstant.Type.BOOLEAN;
                    constant.booleanValue = false;
                } else if (value.startsWith("\"")) {
                    constant.type = AssemblerConstant.Type.STRING;
                    constant.stringValue = StringUtils.fromPrintString(value);
                } else if (value.startsWith("L\"")) {
                    constant.type = AssemblerConstant.Type.LONGSTRING;
                    constant.stringValue = StringUtils.fromPrintString(value.substring(1));
                } else if (value.equals("null")) {
                    constant.type = AssemblerConstant.Type.STRING;
                    constant.stringValue = null;
                } else {
                    try {
                        // TODO: better check
                        if (chunk.number != null) {
                            constant.numberValue = Double.parseDouble(value);
                            constant.type = AssemblerConstant.Type.NUMBER;
                        } else {
                            if (value.contains(".") || value.contains("E") || value.contains("e")) {
                                constant.numberValue = Double.parseDouble(value);
                                constant.type = AssemblerConstant.Type.FLOAT;
                            } else {
                                constant.integerValue = new BigInteger(value);
                                constant.type = AssemblerConstant.Type.INTEGER;
                            }
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalStateException("Unrecognized constant value: " + value);
                    }
                }
                constants.add(constant);
                break;
            }
            case LINE: {
                lines.add(a.getInteger());
                break;
            }
            case ABSLINEINFO: {
                AssemblerAbsLineInfo info = new AssemblerAbsLineInfo();
                info.pc = a.getInteger();
                info.line = a.getInteger();
                abslineinfo.add(info);
                break;
            }
            case LOCAL: {
                AssemblerLocal local = new AssemblerLocal();
                local.name = a.getString();
                local.begin = a.getInteger();
                local.end = a.getInteger();
                locals.add(local);
                break;
            }
            case UPVALUE: {
                AssemblerUpvalue upvalue = new AssemblerUpvalue();
                upvalue.name = a.getString();
                upvalue.index = a.getInteger();
                upvalue.instack = a.getBoolean();
                upvalues.add(upvalue);
                break;
            }
            default:
                throw new IllegalStateException("Unhandled directive: " + d);
        }
    }

    public void processOp(Assembler a, CodeExtract extract, Op op, int opcode) throws AssemblerException, IOException {
        if (!hasMaxStackSize) throw new AssemblerException("Expected .maxstacksize before code");
        if (opcode >= 0 && !extract.op.check(opcode))
            throw new IllegalStateException("Invalid opcode: " + opcode);
        int codepoint = opcode >= 0 ? extract.op.encode(opcode) : 0;
        for (OperandFormat operand : op.operands) {
            CodeExtract.Field field;
            switch (operand.field) {
                case A:
                    field = extract.A;
                    break;
                case B:
                    field = extract.B;
                    break;
                case C:
                    field = extract.C;
                    break;
                case k:
                    field = extract.k;
                    break;
                case Ax:
                    field = extract.Ax;
                    break;
                case sJ:
                    field = extract.sJ;
                    break;
                case Bx:
                    field = extract.Bx;
                    break;
                case sBx:
                    field = extract.sBx;
                    break;
                case x:
                    field = extract.x;
                    break;
                default:
                    throw new IllegalStateException("Unhandled field: " + operand.field);
            }
            int x;
            switch (operand.format) {
                case RAW:
                case IMMEDIATE_INTEGER:
                case IMMEDIATE_FLOAT:
                    x = a.getInteger();
                    break;
                case IMMEDIATE_SIGNED_INTEGER:
                    x = a.getInteger();
                    x += field.max() / 2;
                    break;
                case REGISTER: {
                    x = a.getRegister();
                    //TODO: stack warning
                    break;
                }
                case REGISTER_K: {
                    Assembler.RKInfo rk = a.getRegisterK54();
                    x = rk.x;
                    if (rk.constant) {
                        x += chunk.version.rkoffset.get();
                    }
                    //TODO: stack warning
                    break;
                }
                case REGISTER_K54: {
                    Assembler.RKInfo rk = a.getRegisterK54();
                    codepoint |= extract.k.encode(rk.constant ? 1 : 0);
                    x = rk.x;
                    break;
                }
                case CONSTANT:
                case CONSTANT_INTEGER:
                case CONSTANT_STRING: {
                    x = a.getConstant();
                    break;
                }
                case UPVALUE: {
                    x = a.getUpvalue();
                    break;
                }
                case FUNCTION: {
                    FunctionFixup fix = new FunctionFixup();
                    fix.code_index = code.size();
                    fix.function = a.getAny();
                    fix.field = field;
                    f_fixup.add(fix);
                    x = 0;
                    break;
                }
                case JUMP: {
                    JumpFixup fix = new JumpFixup();
                    fix.code_index = code.size();
                    fix.label = a.getAny();
                    fix.field = field;
                    fix.negate = false;
                    j_fixup.add(fix);
                    x = 0;
                    break;
                }
                case JUMP_NEGATIVE: {
                    JumpFixup fix = new JumpFixup();
                    fix.code_index = code.size();
                    fix.label = a.getAny();
                    fix.field = field;
                    fix.negate = true;
                    j_fixup.add(fix);
                    x = 0;
                    break;
                }
                default:
                    throw new IllegalStateException("Unhandled operand format: " + operand.format);
            }
            if (!field.check(x)) {
                throw new AssemblerException("Operand " + operand.field + " out of range");
            }
            codepoint |= field.encode(x);
        }
        code.add(codepoint);
    }

    public void fixup(CodeExtract extract) throws AssemblerException {
        for (FunctionFixup fix : f_fixup) {
            int codepoint = code.get(fix.code_index);
            int x = -1;
            for (int f = 0; f < children.size(); f++) {
                AssemblerFunction child = children.get(f);
                if (fix.function.equals(child.name)) {
                    x = f;
                    break;
                }
            }
            if (x == -1) {
                throw new AssemblerException("Unknown function: " + fix.function);
            }
            codepoint = fix.field.clear(codepoint);
            codepoint |= fix.field.encode(x);
            code.set(fix.code_index, codepoint);
        }

        for (JumpFixup fix : j_fixup) {
            int codepoint = code.get(fix.code_index);
            int x = 0;
            boolean found = false;
            for (AssemblerLabel label : labels) {
                if (fix.label.equals(label.name)) {
                    x = label.code_index - fix.code_index - 1;
                    if (fix.negate) x = -x;
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new AssemblerException("Unknown label: " + fix.label);
            }
            codepoint = fix.field.clear(codepoint);
            codepoint |= fix.field.encode(x);
            code.set(fix.code_index, codepoint);
        }

        for (AssemblerFunction f : children) {
            f.fixup(extract);
        }
    }

}
