package com.duoc.SumativaCloudNative.dto;

import lombok.Data;

@Data
public class GuiaRequest {

    private String cliente;

    private String direccion;

    private String producto;

    private Integer cantidad;
}
