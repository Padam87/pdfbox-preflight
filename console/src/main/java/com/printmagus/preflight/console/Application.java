package com.printmagus.preflight.console;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.printmagus.preflight.Preflight;
import com.printmagus.preflight.Violation;
import com.printmagus.preflight.rule.*;
import com.printmagus.preflight.serializer.GsonBuilderFactory;
import com.printmagus.preflight.serializer.PDImageXObjectSerializer;
import com.printmagus.preflight.standard.X1a;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StopWatch;

@SpringBootApplication
public class Application implements ApplicationRunner
{
    public static void main(String[] args)
    {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception
    {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Path resultPath = Paths.get("results", String.valueOf(System.currentTimeMillis()));

        File file = new File("src/main/resources/liliput-magazin-beliv.pdf");
        PDDocument document = PDDocument.load(file);

        Preflight preflight = new Preflight();

        preflight.addStandard(new X1a());
        preflight.addRule(new PageCount(3, 5));
        preflight.addRule(new ImageMinDpi(300));
        preflight.addRule(new MaxInkDensityText(320));
        preflight.addRule(new MaxInkDensityImage(320));
        preflight.addRule(new BoxSize(COSName.TRIM_BOX, 680, 980));
        preflight.addRule(new BoxSize(COSName.BLEED_BOX, 686, 986));

        List<Violation> violations = preflight.validate(document);

        GsonBuilder builder = GsonBuilderFactory.create().setPrettyPrinting().serializeNulls();
        /*builder.registerTypeAdapter(
            PDImageXObject.class,
            new PDImageXObjectSerializer().new FileSerializer(resultPath.toString(), true)
        );*/

        Gson gson = builder.create();

        File result = new File(resultPath + "/data.json");
        result.getParentFile().mkdirs();

        PrintWriter writer = new PrintWriter(result);
        writer.println(gson.toJson(violations));
        writer.close();

        for (Violation violation: violations) {
            System.out.println(gson.toJson(violation));
            // System.out.println(violation);
        }

        System.out.println(violations.size());

        stopWatch.stop();

        System.out.println(stopWatch.getTotalTimeMillis());
        System.exit(0);
    }
}
