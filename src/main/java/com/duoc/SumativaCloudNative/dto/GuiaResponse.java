package com.duoc.SumativaCloudNative.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuiaResponse {

    private Long id;

    private String numeroGuia;

    private String mensaje;

    private String archivo;

    private String bucket;

    private String estado;
}
