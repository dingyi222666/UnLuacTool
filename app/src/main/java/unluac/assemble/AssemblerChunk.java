package unluac.assemble;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import unluac.Version;
import unluac.decompile.CodeExtract;
import unluac.decompile.Op;
import unluac.parse.BHeader;
import unluac.parse.BInteger;
import unluac.parse.BIntegerType;
import unluac.parse.LAbsLineInfo;
import unluac.parse.LAbsLineInfoType;
import unluac.parse.LBoolean;
import unluac.parse.LBooleanType;
import unluac.parse.LConstantType;
import unluac.parse.LFunction;
import unluac.parse.LFunctionType;
import unluac.parse.LHeader;
import unluac.parse.LLocal;
import unluac.parse.LLocalType;
import unluac.parse.LNil;
import unluac.parse.LNumberType;
import unluac.parse.LObject;
import unluac.parse.LString;
import unluac.parse.LStringType;
import unluac.parse.LUpvalue;
import unluac.parse.LUpvalueType;

public class AssemblerChunk {

    public Version version;

    public int format;

    public LHeader.LEndianness endianness;

    public int int_size;
    public BIntegerType integer;

    public int size_t_size;
    public BIntegerType sizeT;

    public int instruction_size;
    public int op_size;
    public int a_size;
    public int b_size;
    public int c_size;

    public Map<Integer, Op> useropmap;

    public boolean number_integral;
    public int number_size;
    public LNumberType number;

    public LNumberType linteger;

    public LNumberType lfloat;

    public AssemblerFunction main;
    public AssemblerFunction current;

    private LFunction main_lfunction;

    private Map<AssemblerFunction, LFunction> lFunctionMap;

    public CodeExtract extract;

    public final Set<Directive> processed_directives;

    public AssemblerChunk(Version version) {
        this.version = version;
        processed_directives = new HashSet<Directive>();

        main = null;
        current = null;
        extract = null;
        lFunctionMap = new HashMap<AssemblerFunction, LFunction>();
    }

    public void processHeaderDirective(Assembler a, Directive d) throws AssemblerException, IOException {
        if (d != Directive.OP && processed_directives.contains(d)) {
            throw new AssemblerException("Duplicate " + d.name() + " directive");
        }
        processed_directives.add(d);
        switch (d) {
            case FORMAT:
                format = a.getInteger();
                break;
            case ENDIANNESS: {
                String endiannessName = a.getName();
                switch (endiannessName) {
                    case "LITTLE":
                        endianness = LHeader.LEndianness.LITTLE;
                        break;
                    case "BIG":
                        endianness = LHeader.LEndianness.BIG;
                        break;
                    default:
                        throw new AssemblerException("Unknown endianness \"" + endiannessName + "\"");
                }
                break;
            }
            case INT_SIZE:
                int_size = a.getInteger();
                integer = BIntegerType.create50Type(int_size);
                break;
            case SIZE_T_SIZE:
                size_t_size = a.getInteger();
                sizeT = BIntegerType.create50Type(size_t_size);
                break;
            case INSTRUCTION_SIZE:
                instruction_size = a.getInteger();
                break;
            case SIZE_OP:
                op_size = a.getInteger();
                break;
            case SIZE_A:
                a_size = a.getInteger();
                break;
            case SIZE_B:
                b_size = a.getInteger();
                break;
            case SIZE_C:
                c_size = a.getInteger();
                break;
            case NUMBER_FORMAT: {
                String numberTypeName = a.getName();
                switch (numberTypeName) {
                    case "integer":
                        number_integral = true;
                        break;
                    case "float":
                        number_integral = false;
                        break;
                    default:
                        throw new AssemblerException("Unknown number_format \"" + numberTypeName + "\"");
                }
                number_size = a.getInteger();
                number = new LNumberType(number_size, number_integral, LNumberType.NumberMode.MODE_NUMBER);
                break;
            }
            case INTEGER_FORMAT:
                linteger = new LNumberType(a.getInteger(), true, LNumberType.NumberMode.MODE_INTEGER);
                break;
            case FLOAT_FORMAT:
                lfloat = new LNumberType(a.getInteger(), false, LNumberType.NumberMode.MODE_FLOAT);
                break;
            case OP: {
                if (useropmap == null) {
                    useropmap = new HashMap<Integer, Op>();
                }
                int opcode = a.getInteger();
                String name = a.getName();
                Op op = version.getOpcodeMap().get(name);
                if (op == null) {
                    throw new AssemblerException("Unknown op name \"" + name + "\"");
                }
                useropmap.put(opcode, op);
                break;
            }
            default:
                throw new IllegalStateException("Unhandled directive: " + d);
        }
    }

    public CodeExtract getCodeExtract() throws AssemblerException {
        if (extract == null) {
            extract = new CodeExtract(version, op_size, a_size, b_size, c_size);
        }
        return extract;
    }

    public void processNewFunction(Assembler a) throws AssemblerException, IOException {
        String name = a.getName();
        String[] parts = name.split("/");
        if (main == null) {
            if (parts.length != 1)
                throw new AssemblerException("First (main) function declaration must not have a \"/\" in the name");
            main = new AssemblerFunction(this, null, name);
            current = main;
        } else {
            if (parts.length == 1 || !parts[0].equals(main.name))
                throw new AssemblerException("Function \"" + name + "\" isn't contained in the main function");
            AssemblerFunction parent = main.getInnerParent(parts, 1);
            current = parent.addChild(parts[parts.length - 1]);
        }
    }

    public void processFunctionDirective(Assembler a, Directive d) throws AssemblerException, IOException {
        if (current == null) {
            throw new AssemblerException("Misplaced function directive before declaration of any function");
        }
        current.processFunctionDirective(a, d);
    }

    public void processOp(Assembler a, Op op, int opcode) throws AssemblerException, IOException {
        if (current == null) {
            throw new AssemblerException("Misplaced code before declaration of any function");
        }
        current.processOp(a, getCodeExtract(), op, opcode);
    }

    public void fixup() throws AssemblerException {
        main.fixup(getCodeExtract());
    }

    public void write(OutputStream out) throws AssemblerException, IOException {
        LBooleanType bool = new LBooleanType();
        LStringType string = version.getLStringType();
        LConstantType constant = version.getLConstantType();
        LAbsLineInfoType abslineinfo = new LAbsLineInfoType();
        LLocalType local = new LLocalType();
        LUpvalueType upvalue = version.getLUpvalueType();
        LFunctionType function = version.getLFunctionType();
        CodeExtract extract = getCodeExtract();

        if (integer == null) {
            integer = BIntegerType.create54();
            sizeT = integer;
        }

        LHeader lheader = new LHeader(format, endianness, integer, sizeT, bool, number, linteger, lfloat, string, constant, abslineinfo, local, upvalue, function, extract);
        BHeader header = new BHeader(version, lheader);
        LFunction main = convert_function(header, this.main, null);
        header = new BHeader(version, lheader, main);

        header.write(out);
    }

    public LFunction convertToFunction(AssemblerFunction assemblerFunction) throws AssemblerException {
        //FIXME: 应该可以优化下性能？
        if (main_lfunction == null) {
            LBooleanType bool = new LBooleanType();
            LStringType string = version.getLStringType();
            LConstantType constant = version.getLConstantType();
            LAbsLineInfoType abslineinfo = new LAbsLineInfoType();
            LLocalType local = new LLocalType();
            LUpvalueType upvalue = version.getLUpvalueType();
            LFunctionType function = version.getLFunctionType();
            CodeExtract extract = getCodeExtract();

            if (integer == null) {
                integer = BIntegerType.create54();
                sizeT = integer;
            }

            LHeader lheader = new LHeader(format, endianness, integer, sizeT, bool, number, linteger, lfloat, string, constant, abslineinfo, local, upvalue, function, extract);
            BHeader header = new BHeader(version, lheader);

            main_lfunction = convert_function(header, main, null);
            header.main = main_lfunction;
            main_lfunction.setLevel(1);
            //再转换一次是为了让所有函数都有这个含有主函数的头配置，虽然性能也下降了就是
        }

         if (assemblerFunction == main) {
            return main_lfunction;
        } else {
            return lFunctionMap.get(assemblerFunction);
        }

    }

    private LFunction convert_function(BHeader header, AssemblerFunction function, LFunction parent) {
        int i;
        int[] code = new int[function.code.size()];
        i = 0;
        for (int codepoint : function.code) {
            code[i++] = codepoint;
        }
        int[] lines = new int[function.lines.size()];
        i = 0;
        for (int line : function.lines) {
            lines[i++] = line;
        }
        LAbsLineInfo[] abslineinfo = new LAbsLineInfo[function.abslineinfo.size()];
        i = 0;
        for (AssemblerAbsLineInfo info : function.abslineinfo) {
            abslineinfo[i++] = new LAbsLineInfo(info.pc, info.line);
        }
        LLocal[] locals = new LLocal[function.locals.size()];
        i = 0;
        for (AssemblerLocal local : function.locals) {
            locals[i++] = new LLocal(convert_string(header, local.name), new BInteger(local.begin), new BInteger(local.end));
        }
        LObject[] constants = new LObject[function.constants.size()];
        i = 0;
        for (AssemblerConstant constant : function.constants) {
            LObject object;
            switch (constant.type) {
                case NIL:
                    object = LNil.NIL;
                    break;
                case BOOLEAN:
                    object = constant.booleanValue ? LBoolean.LTRUE : LBoolean.LFALSE;
                    break;
                case NUMBER:
                    object = header.number.create(constant.numberValue);
                    break;
                case INTEGER:
                    object = header.linteger.create(constant.integerValue);
                    break;
                case FLOAT:
                    object = header.lfloat.create(constant.numberValue);
                    break;
                case STRING:
                    object = convert_string(header, constant.stringValue);
                    break;
                case LONGSTRING:
                    object = convert_long_string(header, constant.stringValue);
                    break;
                default:
                    throw new IllegalStateException();
            }
            constants[i++] = object;
        }
        LUpvalue[] upvalues = new LUpvalue[function.upvalues.size()];
        i = 0;
        for (AssemblerUpvalue upvalue : function.upvalues) {
            LUpvalue lup = new LUpvalue();
            lup.bname = convert_string(header, upvalue.name);
            lup.name = lup.bname.deref();
            lup.idx = upvalue.index;
            lup.instack = upvalue.instack;
            upvalues[i++] = lup;
        }
        LFunction[] functions = new LFunction[function.children.size()];
        i = 0;

        LFunction self = new LFunction(
                header,
                convert_string(header, function.source),
                function.linedefined,
                function.lastlinedefined,
                code,
                lines,
                abslineinfo,
                locals,
                constants,
                upvalues,
                functions,
                function.maxStackSize,
                function.upvalues.size(),
                function.numParams,
                function.vararg
        );
        for (AssemblerFunction f : function.children) {
            functions[i++] = convert_function(header, f, self);
        }
        self.parent = parent;

        //self.stripped = true;

        lFunctionMap.put(function, self);
        return self;
    }

    private LString convert_string(BHeader header, String string) {
        if (string == null) {
            return LString.NULL;
        } else {
            return new LString(string);
        }
    }

    private LString convert_long_string(BHeader header, String string) {
        return new LString(string, true);
    }

}
