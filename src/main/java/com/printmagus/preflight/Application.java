package com.printmagus.preflight;

import java.io.File;
import java.util.List;

import com.printmagus.preflight.rule.*;
import com.printmagus.preflight.standard.X1a;
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

            File file = new File("src/main/resources/test3.pdf");
            PDDocument document = PDDocument.load(file);

            Preflight preflight = new Preflight();

            preflight.addStandard(new X1a());
            preflight.addRule(new PageCount(3, 5));
            preflight.addRule(new ImageMinDpi(300));
            preflight.addRule(new MaxInkDensityText(320));
            preflight.addRule(new MaxInkDensityImage(320));

            List<Violation> violations = preflight.validate(document);

            violations.forEach(System.out::println);

            stopWatch.stop();

            System.out.println(stopWatch.getTotalTimeMillis());
            System.exit(0);
        };
    }
}
