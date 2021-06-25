package org.openmrs.module.appointments.service;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Test {
    public static void main(String[] args) {
        String template = "Hi {0},\\\n" +
                "  <p>Your tele-consultation appointment with {1, date, medium} {1, time, short} has been scheduled!<br>\\\n" +
                "  The appointment time is on <b>{2}, {3} at {4}</b>.<br>\\\n" +
                "  The teleconsultation link is: <a href=\"{5}\">{5}</a><br>\\\n" +
                "  If you have any questions, please reach out to hospital administration for assistance.<br>\\\n" +
                "  See you soon!";
        MessageFormat form = new MessageFormat(template);
        Object[] testArgs = { "Angshu", new Date() };
        System.out.println(form.format(testArgs));

//        Stream<String> words = Arrays.asList("A", "B", "C", "D").stream();
//        String joinedString = words.map(e -> "A" + e).collect(Collectors.joining(", "));      //ABCD
//        System.out.println( joinedString );

            //new SimpleDateFormat("d MM yyyy")
    }
}
