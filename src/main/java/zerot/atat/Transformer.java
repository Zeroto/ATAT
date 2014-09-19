package zerot.atat;

import org.objectweb.asm.*;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created by Zerot on 9/18/2014.
 */
public class Transformer extends ClassVisitor
{
    private List<ATParser.ATTransform> transforms;
    private String className = "";

    public Transformer(ClassWriter classWriter, List<ATParser.ATTransform> transforms) {
        super(Opcodes.ASM5, classWriter);
        this.transforms = transforms;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

        className = name;

        for (ATParser.ATTransform transform : transforms)
        {
            if (transform.type == ATParser.ATType.CLASS && name.equals(transform.className))
            {
                access = getNewAccess(access, transform);
            }
        }

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {

        for (ATParser.ATTransform transform : transforms)
        {
            if (transform.type == ATParser.ATType.FIELD && className.equals(transform.className) && name.equals(transform.elementName))
            {
                access = getNewAccess(access, transform);
            }
        }

        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

        for (ATParser.ATTransform transform : transforms)
        {
            if (transform.type == ATParser.ATType.METHOD && className.equals(transform.className) && name.equals(transform.elementName) && desc.equals(transform.signature))
            {
                access = getNewAccess(access, transform);
            }
        }

        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    private int getNewAccess(int access, ATParser.ATTransform transform) {
        int ret = (access & ~7); // clear out existing access flags
        int curAccess = (access & 7);
        switch (transform.newAccessor)
        {
            case PRIVATE:
                if ((curAccess & ACC_PRIVATE) == ACC_PRIVATE)
                    ret |= ACC_PRIVATE;
                else
                    ret |= curAccess;
                break;
            case DEFAULT:
                if ((curAccess & ACC_PRIVATE) == ACC_PRIVATE || (curAccess == 0))
                    ret |= 0;
                else
                    ret |= curAccess;
                break;
            case PROTECTED:
                if ((curAccess & ACC_PRIVATE) == ACC_PRIVATE || (curAccess == 0) || (curAccess & ACC_PROTECTED) == ACC_PROTECTED)
                    ret |= ACC_PROTECTED;
                else
                    ret |= curAccess;
                break;
            case PUBLIC:
                ret |= ACC_PUBLIC; // we can always promote to public
                break;
        }

        if (transform.changeFinal)
        {
            if (transform.finalized)
                ret |= ACC_FINAL;
            else
                ret &= ~ACC_FINAL;
        }
        return ret;
    }
}
