// src/main/java/com/duoc/SumativaCloudNative/controller/GuiaController.java
package com.duoc.SumativaCloudNative.controller;

import com.duoc.SumativaCloudNative.dto.GuiaRequest;
import com.duoc.SumativaCloudNative.model.GuiaDespacho;
import com.duoc.SumativaCloudNative.service.AwsS3Service;
import com.duoc.SumativaCloudNative.service.GuiaService;
import com.itextpdf.text.DocumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/guias")
@RequiredArgsConstructor
public class GuiaController {

    private final GuiaService guiaService;
    private final AwsS3Service awsS3Service;

    @Value("${aws.s3.bucket}")
    private String bucket;

    /** POST /guias — Crear guía y subir automáticamente a S3 vía EFS */
    @PostMapping
    public ResponseEntity<GuiaDespacho> crearGuia(@RequestBody GuiaRequest request)
            throws IOException, DocumentException {
        GuiaDespacho guia = guiaService.crearGuia(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(guia);
    }

    /** GET /guias/download?s3Key=...&transportista=... — Descargar con validación */
    @GetMapping("/download")
    public ResponseEntity<byte[]> descargarGuia(
            @RequestParam String s3Key,
            @RequestParam String transportista) {
        byte[] bytes = guiaService.descargarGuia(s3Key, transportista);
        String filename = s3Key.substring(s3Key.lastIndexOf("/") + 1);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    /** PUT /guias?s3KeyOriginal=... — Modificar/actualizar guía existente */
    @PutMapping
    public ResponseEntity<GuiaDespacho> actualizarGuia(
            @RequestParam String s3KeyOriginal,
            @RequestBody GuiaRequest request)
            throws IOException, DocumentException {
        GuiaDespacho guia = guiaService.actualizarGuia(s3KeyOriginal, request);
        return ResponseEntity.ok(guia);
    }

    /** DELETE /guias?s3Key=... — Eliminar guía específica */
    @DeleteMapping
    public ResponseEntity<Void> eliminarGuia(@RequestParam String s3Key) {
        guiaService.eliminarGuia(s3Key);
        return ResponseEntity.noContent().build();
    }

    /** GET /guias/{transportista}?fecha=20240101 — Consultar por transportista y fecha */
    @GetMapping("/{transportista}")
    public ResponseEntity<List<String>> listarGuias(
            @PathVariable String transportista,
            @RequestParam(required = false) String fecha) {

        String prefix = (fecha != null ? fecha + "/" : "") + transportista + "/";

        List<String> keys = awsS3Service.listObjects(bucket)
                .stream()
                .map(dto -> dto.getKey())
                .filter(key -> key.startsWith(prefix))
                .collect(Collectors.toList());

        return ResponseEntity.ok(keys);
    }
}