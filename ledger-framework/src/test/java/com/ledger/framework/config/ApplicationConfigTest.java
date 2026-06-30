package com.ledger.framework.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

class ApplicationConfigTest
{
    private final TimeZone originalTimeZone = TimeZone.getDefault();

    @AfterEach
    void restoreTimeZone()
    {
        TimeZone.setDefault(originalTimeZone);
    }

    @Test
    void jacksonDateSerializationUsesShanghaiTimeZoneWhenJvmDefaultIsUtc() throws Exception
    {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        new ApplicationConfig().jacksonObjectMapperCustomization().customize(builder);
        ObjectMapper objectMapper = builder.build();

        String json = objectMapper.writeValueAsString(new DatePayload(new Date(0L)));

        assertTrue(json.contains("\"time\":\"1970-01-01 08:00:00\""));
    }

    static class DatePayload
    {
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private final Date time;

        DatePayload(Date time)
        {
            this.time = time;
        }

        public Date getTime()
        {
            return time;
        }
    }
}
