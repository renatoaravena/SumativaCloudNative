package com.duoc.SumativaCloudNative.controller;

import com.duoc.SumativaCloudNative.dto.GuiaRequest;
import com.duoc.SumativaCloudNative.model.GuiaDespacho;
import com.duoc.SumativaCloudNative.service.AwsS3Service;
import com.duoc.SumativaCloudNative.service.GuiaService;
import com.itextpdf.text.DocumentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/guias")
public class GuiaController {

    private final GuiaService guiaService;
    private final AwsS3Service awsS3Service;
    private final String bucket;

    public GuiaController(
            GuiaService guiaService,
            AwsS3Service awsS3Service,
            @Value("${aws.s3.bucket}") String bucket) {
        this.guiaService = guiaService;
        this.awsS3Service = awsS3Service;
        this.bucket = bucket;
    }

    /** POST /guias — Crear guía, guardar en EFS y subir a S3 */
    @PostMapping
    public ResponseEntity<GuiaDespacho> crearGuia(@RequestBody GuiaRequest request)
            throws IOException, DocumentException {
        GuiaDespacho guia = guiaService.crearGuia(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(guia);
    }

    /** GET /guias/download?s3Key=... — Descargar guía por ruta completa */
    @GetMapping("/download")
    public ResponseEntity<byte[]> descargarGuia(@RequestParam String s3Key) {
        byte[] bytes = awsS3Service.downloadAsBytes(bucket, s3Key);
        String filename = s3Key.substring(s3Key.lastIndexOf("/") + 1);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    /** PUT /guias — Modificar guía existente */
    @PutMapping
    public ResponseEntity<GuiaDespacho> actualizarGuia(
            @RequestParam String s3KeyOriginal,
            @RequestBody GuiaRequest request)
            throws IOException, DocumentException {
        GuiaDespacho guia = guiaService.actualizarGuia(s3KeyOriginal, request);
        return ResponseEntity.ok(guia);
    }

    /** DELETE /guias — Eliminar guía por ruta completa */
    @DeleteMapping
    public ResponseEntity<Void> eliminarGuia(@RequestParam String s3Key) {
        guiaService.eliminarGuia(s3Key);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{transportista}")
    public ResponseEntity<List<String>> listarGuias(
            @PathVariable String transportista,
            @RequestParam(required = false) String fecha) {
        List<String> keys = awsS3Service.listObjects(bucket)
                .stream()
                .map(dto -> dto.getKey())
                .filter(key -> fecha != null 
                    ? key.startsWith(fecha + "/" + transportista + "/")
                    : key.contains("/" + transportista + "/"))
                .collect(Collectors.toList());
        return ResponseEntity.ok(keys);
    }
}