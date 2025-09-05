package com.virinchi.demo.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("imageHelper")
public class ImageHelper {
    private final ObjectMapper mapper = new ObjectMapper();

    public String firstImage(String imagesJson) {
        if (imagesJson == null || imagesJson.isBlank()) return null;
        try {
            List<String> list = mapper.readValue(imagesJson, new TypeReference<List<String>>(){});
            if (list != null && !list.isEmpty()) return list.get(0);
        } catch (Exception ignored) {}
        return null;
    }
}

