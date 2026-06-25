package com.duoc.SumativaCloudNative.controller;

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
    public ResponseEntity<GuiaResponse> crearGuia(
            @RequestBody GuiaRequest request) {

        GuiaDespacho guia =
                guiaService.crearGuia(request);

        return ResponseEntity.ok(
                GuiaResponse.builder()
                        .id(guia.getId())
                        .numeroGuia(guia.getNumeroGuia())
                        .estado(guia.getEstado())
                        .mensaje("Guía creada correctamente")
                        .build()
        );
    }
}
