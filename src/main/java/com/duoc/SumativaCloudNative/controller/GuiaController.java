package com.duoc.SumativaCloudNative.controller;

import com.duoc.SumativaCloudNative.dto.GuiaRequest;
import com.duoc.SumativaCloudNative.model.GuiaDespacho;
import com.duoc.SumativaCloudNative.model.GuiaProcesada;
import com.duoc.SumativaCloudNative.service.GuiaService;
import com.itextpdf.text.DocumentException;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/guias")
@RequiredArgsConstructor
public class GuiaController {

    private final GuiaService guiaService;

    /**
     * Publica la guía en RabbitMQ.
     * El consumidor será el encargado de crearla.
     */
    @PostMapping
    public ResponseEntity<String> crearGuia(@RequestBody GuiaRequest request) {

        guiaService.publicarGuia(request);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body("Solicitud enviada a la cola. La guía será procesada por el consumidor.");
    }

    @GetMapping(value = "/descargar", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> descargarGuia(
            @RequestParam String s3Key,
            Authentication authentication) {

        byte[] pdf = guiaService.descargarGuia(
                s3Key,
                authentication.getName()
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PutMapping
    public ResponseEntity<String> actualizarGuia(
            @RequestParam String s3Key,
            @RequestBody GuiaRequest request) {

        guiaService.actualizarGuia(s3Key, request);

        return ResponseEntity.accepted()
                .body("Solicitud de actualización enviada a la cola.");
    }

    @DeleteMapping
    public ResponseEntity<String> eliminarGuia(
            @RequestParam String s3Key) {

        guiaService.eliminarGuia(s3Key);

        return ResponseEntity.ok("Guía eliminada correctamente");
    }

    @GetMapping
        public ResponseEntity<List<GuiaProcesada>> consultarGuias(
                @RequestParam String transportista,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

            List<GuiaProcesada> resultado = guiaService.consultarPorTransportistaYFecha(transportista, fecha);
            return ResponseEntity.ok(resultado);
    }
}