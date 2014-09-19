package zerot.atat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zerot on 9/18/2014.
 */
public class ATParser
{
    public static enum ATType
    {
        CLASS,
        METHOD,
        FIELD
    }

    public static enum Accessor
    {
        PUBLIC,
        PROTECTED,
        PRIVATE,
        DEFAULT
    }

    public static class ATTransform
    {
        public ATType type;
        public Accessor newAccessor;
        public boolean finalized;
        public boolean changeFinal;

        public String className;
        public String elementName;
        public String signature;

        @Override
        public String toString() {
            return type + ": " + newAccessor + " " + finalized + " " + changeFinal + ": " + className + " " + elementName + " " + signature;
        }
    }

    private List<ATTransform> transforms = new ArrayList<>();

    public List<ATTransform> getTransforms() {
        return transforms;
    }

    public ATParser(List<String> atLines)
    {
        // parse at
        for (String at : atLines)
        {
            int commentIndex = at.indexOf("#");
            if (commentIndex >= 0)
                at = at.substring(0,commentIndex);

            if (at.isEmpty())
                continue;


            String[] parts = at.split(" ");

            ATTransform transform = new ATTransform();

            if (parts.length == 2)
            {
                transform.type = ATType.CLASS;
            }
            else if (hasSignature(parts[2]))
            {
                transform.type = ATType.METHOD;
            }
            else
            {
                transform.type = ATType.FIELD;
            }

            parseAccessorAndFinal(parts[0], transform);

            transform.className = parts[1].replace('.','/');

            if (transform.type == ATType.FIELD)
            {
                transform.elementName = parts[2];
            }
            else if (transform.type == ATType.METHOD)
            {
                transform.elementName = parts[2].substring(0, parts[2].indexOf("("));
                transform.signature = parts[2].substring(parts[2].indexOf("("));
            }

            transforms.add(transform);

        }
    }

    private void parseAccessorAndFinal(String part, ATTransform transform) {
        if (part.regionMatches(true, 0, "public", 0, 6))
        {
            transform.newAccessor = Accessor.PUBLIC;
        }
        else if (part.regionMatches(true, 0, "private", 0, 7))
        {
            transform.newAccessor = Accessor.PRIVATE;
        }
        else if (part.regionMatches(true, 0, "protected", 0, 9))
        {
            transform.newAccessor = Accessor.PROTECTED;
        }
        else if (part.regionMatches(true, 0, "default", 0, 7))
        {
            transform.newAccessor = Accessor.DEFAULT;
        }

        if (part.regionMatches(true, part.length()-2, "-f", 0, 2))
        {
            transform.changeFinal = true;
            transform.finalized = false;
        }
        else if (part.regionMatches(true, part.length()-2, "+f", 0, 2))
        {
            transform.changeFinal = true;
            transform.finalized = true;
        }
        else
        {
            transform.changeFinal = false;
        }
    }

    private boolean hasSignature(String part) {
        return part.indexOf("(") >0;
    }
}
