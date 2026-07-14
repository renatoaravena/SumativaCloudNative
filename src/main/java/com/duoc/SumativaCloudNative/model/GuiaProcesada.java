package com.duoc.SumativaCloudNative.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "GUIAS_PROCESADAS")
@Data
public class GuiaProcesada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String operacion;
    private String s3KeyOriginal;
    private String transportista;
    private String numeroPedido;
    private String destinatario;
    private String direccionDestino;
    private String descripcionCarga;
    private double pesoKg;
    private LocalDateTime fechaProcesado;
}