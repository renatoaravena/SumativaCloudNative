package com.duoc.SumativaCloudNative.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GuiaResponse {

    private String numero;

    private String archivo;

    private String mensaje;
}
