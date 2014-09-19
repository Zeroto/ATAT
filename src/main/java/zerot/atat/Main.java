package zerot.atat;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by Zerot on 9/18/2014.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length<2)
        {
            printUsage();
            System.exit(-1);
            return;
        }

        List<ATParser.ATTransform> transformers = new ArrayList<>();
        for (int i=1; i<args.length; i++)
        {
            List<String> lines = Files.readAllLines(Paths.get(args[i]), StandardCharsets.UTF_8);
            ATParser parser = new ATParser(lines);
            for (ATParser.ATTransform transform : parser.getTransforms())
            {
                System.out.println(transform.toString());
                transformers.add(transform);
            }
        }

        processJar(args[0], transformers);
    }

    public static void printUsage()
    {
        System.out.println("USAGE: java -jar ATAT.jar <jar file> <at file> [at file...]");
    }

    private static void processJar(String path, List<ATParser.ATTransform> transforms) throws IOException {
        File origJar = new File(path);
        if (!origJar.exists())
        {
            System.out.println("Jar does not exist");
            System.exit(-2);
            return;
        }

        File backupJar = new File(path+".old");
        if (backupJar.exists())
        {
            backupJar.delete();
        }

        if (!origJar.renameTo(backupJar))
        {
            System.out.println("Was unable to move orig to old");
            System.exit(-3);
        }


        ZipInputStream input = new ZipInputStream(new BufferedInputStream(new FileInputStream(backupJar)));
        ZipOutputStream output = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(origJar)));

        ZipEntry entry;
        while((entry = input.getNextEntry()) != null)
        {
            if (entry.isDirectory())
            {
                output.putNextEntry(entry);
                continue;
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] dataBuffer = new byte[1024];

            int length;
            while((length = input.read(dataBuffer))>=0)
            {
                buffer.write(dataBuffer, 0, length);
            }

            byte[] fileData = buffer.toByteArray();

            String name = entry.getName();
            if (name.endsWith(".class") && !name.startsWith("."))
            {
                ClassReader reader = new ClassReader(fileData);
                ClassWriter writer = new ClassWriter(0);
                reader.accept(new Transformer(writer, transforms), 0);
                fileData = writer.toByteArray();
            }

            ZipEntry newEntry = new ZipEntry(name);
            output.putNextEntry(newEntry);
            output.write(fileData);
        }

        output.close();
        input.close();
    }
}
