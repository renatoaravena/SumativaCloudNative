package com.duoc.SumativaCloudNative.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuiaDespacho {

    private Long id;

    private String numeroGuia;

    private String cliente;

    private String transportista;

    private String origen;

    private String destino;

    private String fecha;

    private String estado;

    private String rutaPdf;

    private String rutaS3;
}