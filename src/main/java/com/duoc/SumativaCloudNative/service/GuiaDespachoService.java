package com.duoc.SumativaCloudNative.service;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.duoc.SumativaCloudNative.dto.GuiaRequest;
import com.duoc.SumativaCloudNative.model.GuiaDespacho;

@Service
public class GuiaDespachoService {

    private final AtomicLong contador = new AtomicLong(1);

    public GuiaDespacho crearGuia(GuiaRequest request) {

        Long id = contador.getAndIncrement();

        return GuiaDespacho.builder()
                .id(id)
                .numeroGuia("GD-" + id)
                .cliente(request.getCliente())
                .transportista(request.getTransportista())
                .origen(request.getOrigen())
                .destino(request.getDestino())
                .fecha(LocalDate.now().toString())
                .estado("CREADA")
                .build();
    }
}
