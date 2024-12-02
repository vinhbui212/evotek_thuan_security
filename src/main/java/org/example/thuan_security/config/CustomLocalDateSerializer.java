//package org.example.thuan_security.config;
//
//import com.fasterxml.jackson.core.JsonGenerator;
//import com.fasterxml.jackson.databind.JsonSerializer;
//import com.fasterxml.jackson.databind.SerializerProvider;
//import com.fasterxml.jackson.databind.annotation.JsonSerialize;
//
//import java.io.IOException;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//
//public class CustomLocalDateSerializer extends JsonSerializer<LocalDate> {
//
//    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//    @Override
//    public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
//        if (value != null) {
//            gen.writeString(value.format(formatter));
//        }
//    }
//}
//
