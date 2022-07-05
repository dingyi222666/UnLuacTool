package unluac.assemble;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import unluac.Version;
import unluac.decompile.Op;
import unluac.decompile.OpcodeMap;
import unluac.assemble.AssemblerChunk;

import unluac.util.StringUtils;

class AssemblerLabel {
  
  public String name;
  public int code_index;
  
}

class AssemblerConstant {
  
  enum Type {
    NIL,
    BOOLEAN,
    NUMBER,
    INTEGER,
    FLOAT,
    STRING,
    LONGSTRING,
  }
  
  public String name;
  public Type type;
  
  public boolean booleanValue;
  public double numberValue;
  public String stringValue;
  public BigInteger integerValue;
}

class AssemblerAbsLineInfo {
  
  public int pc;
  public int line;
  
}

class AssemblerLocal {
  
  public String name;
  public int begin;
  public int end;
  
}

class AssemblerUpvalue {
  
  public String name;
  public int index;
  public boolean instack;
  
}

public class Assembler {

  private Tokenizer t;
  private OutputStream out;
  private Version version;

  public Assembler(InputStream in, OutputStream out) {
    t = new Tokenizer(in);
    this.out = out;
  }

  public AssemblerChunk getChunk() throws AssemblerException, IOException {

    String tok = t.next();
    if (!tok.equals(".version"))
      throw new AssemblerException("First directive must be .version, instead was \"" + tok + "\"");
    tok = t.next();

    int major;
    int minor;
    String[] parts = tok.split("\\.");
    if (parts.length == 2) {
      try {
        major = Integer.valueOf(parts[0]);
        minor = Integer.valueOf(parts[1]);
      } catch (NumberFormatException e) {
        throw new AssemblerException("Unsupported version " + tok);
      }
    } else {
      throw new AssemblerException("Unsupported version " + tok);
    }
    if (major < 0 || major > 0xF || minor < 0 || minor > 0xF) {
      throw new AssemblerException("Unsupported version " + tok);
    }

    version = Version.getVersion(major, minor);

    if (version == null) {
      throw new AssemblerException("Unsupported version " + tok);
    }

    Map<String, Op> oplookup = null;
    Map<Op, Integer> opcodelookup = null;

    AssemblerChunk chunk = new AssemblerChunk(version);
    boolean opinit = false;

    while ((tok = t.next()) != null) {
      Directive d = Directive.lookup.get(tok);
      if (d != null) {
        switch (d.type) {
          case HEADER:
            chunk.processHeaderDirective(this, d);
            break;
          case NEWFUNCTION:
            if (!opinit) {
              opinit = true;
              OpcodeMap opmap;
              if (chunk.useropmap != null) {
                opmap = new OpcodeMap(chunk.useropmap);
              } else {
                opmap = version.getOpcodeMap();
              }
              oplookup = new HashMap<String, Op>();
              opcodelookup = new HashMap<Op, Integer>();
              for (int i = 0; i < opmap.size(); i++) {
                Op op = opmap.get(i);
                if (op != null) {
                  oplookup.put(op.name, op);
                  opcodelookup.put(op, i);
                }
              }

              oplookup.put(Op.EXTRABYTE.name, Op.EXTRABYTE);
              opcodelookup.put(Op.EXTRABYTE, -1);
            }

            chunk.processNewFunction(this);
            break;
          case FUNCTION:
            chunk.processFunctionDirective(this, d);
            break;
          default:
            throw new IllegalStateException();
        }

      } else {
        Op op = oplookup.get(tok);
        if (op != null) {
          // TODO:
          chunk.processOp(this, op, opcodelookup.get(op));
        } else {
          throw new AssemblerException("Unexpected token \"" + tok + "\"");
        }
      }

    }
    chunk.fixup();

    return chunk;
  }

  public void assemble() throws AssemblerException, IOException {

    AssemblerChunk chunk = getChunk();

    chunk.write(out);

  }
  
  String getAny() throws AssemblerException, IOException {
    String s = t.next();
    if(s == null) throw new AssemblerException("Unexcepted end of file");
    return s;
  }
  
  String getName() throws AssemblerException, IOException {
    String s = t.next();
    if(s == null) throw new AssemblerException("Unexcepted end of file");
    return s;
  }
  
  String getString() throws AssemblerException, IOException {
    String s = t.next();
    if(s == null) throw new AssemblerException("Unexcepted end of file");
    return StringUtils.fromPrintString(s);
  }
  
  int getInteger() throws AssemblerException, IOException {
    String s = t.next();
    if(s == null) throw new AssemblerException("Unexcepted end of file");
    int i;
    try {
      i = Integer.parseInt(s);
    } catch(NumberFormatException e) {
      throw new AssemblerException("Excepted number, got \"" + s + "\"");
    }
    return i;
  }
  
  boolean getBoolean() throws AssemblerException, IOException {
    String s = t.next();
    if(s == null) throw new AssemblerException("Unexcepted end of file");
    boolean b;
    if(s.equals("true")) {
      b = true;
    } else if(s.equals("false")) {
      b = false;
    } else {
      throw new AssemblerException("Expected boolean, got \"" + s + "\"");
    }
    return b;
  }
  
  int getRegister() throws AssemblerException, IOException {
    String s = t.next();
    if(s == null) throw new AssemblerException("Unexcepted end of file");
    int r;
    if(s.length() >= 2 && s.charAt(0) == 'r') {
      try {
        r = Integer.parseInt(s.substring(1));
      } catch(NumberFormatException e) {
        throw new AssemblerException("Excepted register, got \"" + s + "\"");
      }
    } else {
      throw new AssemblerException("Excepted register, got \"" + s + "\"");
    }
    return r;
  }
  
  static class RKInfo {
    int x;
    boolean constant;
  }
  
  RKInfo getRegisterK54() throws AssemblerException, IOException {
    String s = t.next();
    if(s == null) throw new AssemblerException("Unexcepted end of file");
    RKInfo rk = new RKInfo();
    if(s.length() >= 2 && s.charAt(0) == 'r') {
      rk.constant = false;
      try {
        rk.x = Integer.parseInt(s.substring(1));
      } catch(NumberFormatException e) {
        throw new AssemblerException("Excepted register, got \"" + s + "\"");
      }
    } else if(s.length() >= 2 && s.charAt(0) == 'k') {
      rk.constant = true;
      try {
        rk.x = Integer.parseInt(s.substring(1));
      } catch(NumberFormatException e) {
        throw new AssemblerException("Excepted constant, got \"" + s + "\"");
      }
    } else {
      throw new AssemblerException("Excepted register or constant, got \"" + s + "\"");
    }
    return rk;
  }
  
  int getConstant() throws AssemblerException, IOException {
    String s = t.next();
    if(s == null) throw new AssemblerException("Unexpected end of file");
    int k;
    if(s.length() >= 2 && s.charAt(0) == 'k') {
      try {
        k = Integer.parseInt(s.substring(1));
      } catch(NumberFormatException e) {
        throw new AssemblerException("Excepted constant, got \"" + s + "\"");
      }
    } else {
      throw new AssemblerException("Excepted constant, got \"" + s + "\"");
    }
    return k;
  }
  
  int getUpvalue() throws AssemblerException, IOException {
    String s = t.next();
    if(s == null) throw new AssemblerException("Unexcepted end of file");
    int u;
    if(s.length() >= 2 && s.charAt(0) == 'u') {
      try {
        u = Integer.parseInt(s.substring(1));
      } catch(NumberFormatException e) {
        throw new AssemblerException("Excepted register, got \"" + s + "\"");
      }
    } else {
      throw new AssemblerException("Excepted register, got \"" + s + "\"");
    }
    return u;
  }
  
}
