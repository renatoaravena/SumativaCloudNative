package com.duoc.SumativaCloudNative.service;

import com.duoc.SumativaCloudNative.dto.GuiaRequest;
import com.duoc.SumativaCloudNative.model.GuiaDespacho;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.duoc.SumativaCloudNative.dto.GuiaMessage;
import com.duoc.SumativaCloudNative.producer.RabbitProducer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class GuiaService {

    private final AwsS3Service awsS3Service;
    private final EfsService efsService;
    private final String bucket;
    private final String efsPath;
    private final RabbitProducer rabbitProducer;

    public GuiaService(
        AwsS3Service awsS3Service,
        EfsService efsService,
        RabbitProducer rabbitProducer,
        @Value("${aws.s3.bucket}") String bucket,
        @Value("${efs.path}") String efsPath) {

        this.awsS3Service = awsS3Service;
        this.efsService = efsService;
        this.rabbitProducer = rabbitProducer;
        this.bucket = bucket;
        this.efsPath = efsPath;
    }

    

    public void publicarGuia(GuiaRequest request) {

        GuiaMessage message = GuiaMessage.builder()
                .operacion("CREAR")
                .transportista(request.getTransportista())
                .numeroPedido(request.getNumeroPedido())
                .destinatario(request.getDestinatario())
                .direccionDestino(request.getDireccionDestino())
                .descripcionCarga(request.getDescripcionCarga())
                .pesoKg(request.getPesoKg())
                .build();

        rabbitProducer.enviarGuia(message);
    }

    public byte[] descargarGuia(String s3Key, String transportista) {
        if (!s3Key.contains("/" + transportista + "/")) {
            throw new SecurityException("Acceso denegado: no tienes permisos para esta guía");
        }
        return awsS3Service.downloadAsBytes(bucket, s3Key);
    }

    public void actualizarGuia(String s3KeyOriginal, GuiaRequest request) {

        GuiaMessage message = GuiaMessage.builder()
                .operacion("ACTUALIZAR")
                .s3KeyOriginal(s3KeyOriginal)
                .transportista(request.getTransportista())
                .numeroPedido(request.getNumeroPedido())
                .destinatario(request.getDestinatario())
                .direccionDestino(request.getDireccionDestino())
                .descripcionCarga(request.getDescripcionCarga())
                .pesoKg(request.getPesoKg())
                .build();

        rabbitProducer.enviarGuia(message);
    }

    public void eliminarGuia(String s3Key) {
        awsS3Service.deleteObject(bucket, s3Key);
        log.info("Guía eliminada de S3: {}", s3Key);
    }

    
}