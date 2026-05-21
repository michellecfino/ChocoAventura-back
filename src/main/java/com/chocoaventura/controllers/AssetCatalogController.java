package com.chocoaventura.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/assets-catalog")
@CrossOrigin(origins = "*")
public class AssetCatalogController {

    private final ObjectMapper objectMapper;
    private Map<String, Object> catalog = Collections.emptyMap();

    public AssetCatalogController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadCatalog() throws Exception {
        ClassPathResource resource = new ClassPathResource("assets_catalog.json");
        try (InputStream inputStream = resource.getInputStream()) {
            catalog = objectMapper.readValue(inputStream, new TypeReference<Map<String, Object>>() {});
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCatalog() {
        return ResponseEntity.ok(catalog);
    }

    @GetMapping("/destinos/{destinoKey}/imagenes")
    public ResponseEntity<List<String>> getDestinoImagenes(@PathVariable String destinoKey) {
        return ResponseEntity.ok(getNestedList("destinos", destinoKey));
    }

    @GetMapping("/actividades/{destinoKey}/imagenes")
    public ResponseEntity<List<String>> getActividadImagenes(@PathVariable String destinoKey) {
        return ResponseEntity.ok(getNestedList("actividades", destinoKey));
    }

    @GetMapping("/perfiles/{genero}")
    public ResponseEntity<List<String>> getPerfilImagenes(@PathVariable String genero) {
        return ResponseEntity.ok(getNestedList("perfiles", genero));
    }

    @GetMapping("/choco")
    public ResponseEntity<List<String>> getChocoImagenes() {
        Object choco = catalog.get("choco");
        if (choco instanceof List<?> list) {
            return ResponseEntity.ok(list.stream().map(String::valueOf).toList());
        }
        return ResponseEntity.ok(Collections.emptyList());
    }

    @SuppressWarnings("unchecked")
    private List<String> getNestedList(String section, String key) {
        Object sectionValue = catalog.get(section);
        if (!(sectionValue instanceof Map<?, ?> sectionMap)) {
            return Collections.emptyList();
        }
        Object listValue = sectionMap.get(key.toLowerCase());
        if (listValue instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return Collections.emptyList();
    }
}
