package com.duoc.SumativaCloudNative.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.duoc.SumativaCloudNative.dto.GuiaRequest;
import com.duoc.SumativaCloudNative.dto.GuiaResponse;
import com.duoc.SumativaCloudNative.model.GuiaDespacho;
import com.duoc.SumativaCloudNative.service.GuiaDespachoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/guias")
@RequiredArgsConstructor
public class GuiaDespachoController {

    private final GuiaDespachoService guiaService;

    @PostMapping
    public ResponseEntity<GuiaResponse> crear(
            @RequestBody GuiaRequest request)
            throws Exception {

        return ResponseEntity.ok(
                guiaService.crearGuia(
                        request));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<GuiaDespacho>> buscar(
            @RequestParam String transportista,
            @RequestParam String fecha)
            throws Exception {

        return ResponseEntity.ok(
                guiaService.buscar(
                        transportista,
                        fecha));
    }
}