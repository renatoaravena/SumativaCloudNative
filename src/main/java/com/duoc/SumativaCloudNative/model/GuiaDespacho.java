package com.duoc.SumativaCloudNative.model;

import lombok.Data;

@Data
public class GuiaDespacho {

    private String numero;

    private String cliente;

    private String direccion;

    private String producto;

    private Integer cantidad;
}