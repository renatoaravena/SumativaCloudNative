package com.duoc.SumativaCloudNative.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuiaDespacho {
    private String id;
    private String transportista;
    private String numeroPedido;
    private String destinatario;
    private String direccionDestino;
    private String descripcionCarga;
    private double pesoKg;
    private LocalDate fechaCreacion;
    private String s3Key;      // ruta en S3
    private String efsPath;    // ruta temporal en EFS
}