package com.printmagus.preflight;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.printmagus.preflight.rule.*;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StopWatch;

@SpringBootApplication
public class Application
{

    public static void main(String[] args)
    {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx)
    {
        return args -> {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            File file = new File("src/main/resources/hotel.pdf");
            PDDocument document = PDDocument.load(file);

            Preflight preflight = new Preflight();

            preflight.addRule(new PageCount(3, 5));
            preflight.addRule(new NoRgbText());
            preflight.addRule(new NoRgbImages());
            preflight.addRule(new ImageMinDpi(300));
            preflight.addRule(new MaxInkDensityText(320));
            preflight.addRule(new BoxNesting());
            preflight.addRule(new DocumentVersion());
            preflight.addRule(new DocumentIdExists());
            preflight.addRule(new OutputIntent());
            preflight.addRule(new InfoKeysExist(Arrays.asList(
                COSName.TITLE.getName(),
                COSName.CREATION_DATE.getName(),
                COSName.MOD_DATE.getName(),
                "GTS_PDFXVersion"
            )));

            List<Violation> violations = preflight.validate(document);

            violations.forEach(System.out::println);

            stopWatch.stop();

            System.out.println(stopWatch.getTotalTimeMillis());
            System.exit(0);
        };
    }

}
