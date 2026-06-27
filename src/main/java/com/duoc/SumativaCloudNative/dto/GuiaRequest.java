package com.duoc.SumativaCloudNative.dto;

import lombok.Data;

@Data
public class GuiaRequest {
    private String transportista;
    private String numeroPedido;
    private String destinatario;
    private String direccionDestino;
    private String descripcionCarga;
    private double pesoKg;
}
