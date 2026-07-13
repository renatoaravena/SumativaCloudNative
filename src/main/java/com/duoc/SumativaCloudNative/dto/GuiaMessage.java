package com.duoc.SumativaCloudNative.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuiaMessage {

    private String operacion;
    private String s3KeyOriginal;

    private String transportista;
    private String numeroPedido;
    private String destinatario;
    private String direccionDestino;
    private String descripcionCarga;
    private double pesoKg;
    

}