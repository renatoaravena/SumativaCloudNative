package com.duoc.SumativaCloudNative.service;

import com.duoc.SumativaCloudNative.dto.GuiaRequest;
import com.duoc.SumativaCloudNative.model.GuiaDespacho;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    public GuiaService(
            AwsS3Service awsS3Service,
            EfsService efsService,
            @Value("${aws.s3.bucket}") String bucket,
            @Value("${efs.path}") String efsPath) {
        this.awsS3Service = awsS3Service;
        this.efsService = efsService;
        this.bucket = bucket;
        this.efsPath = efsPath;
    }

    public GuiaDespacho crearGuia(GuiaRequest request) throws IOException, DocumentException {
        String id = UUID.randomUUID().toString().substring(0, 8);
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Santiago"));
        String fecha = hoy.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        String nombreArchivo = "guia_" + id + ".pdf";

        // 1. Guardar PDF temporalmente en EFS
        String rutaEfs = efsPath + "/" + nombreArchivo;
        File archivoPdf = generarPdf(id, request, rutaEfs);
        log.info("PDF guardado en EFS: {}", rutaEfs);

        // 2. Subir desde EFS a S3 con estructura requerida
        String s3Key = fecha + "/" + request.getTransportista() + "/" + nombreArchivo;
        awsS3Service.uploadFile(bucket, s3Key, archivoPdf);
        log.info("PDF subido a S3: {}", s3Key);

        return GuiaDespacho.builder()
                .id(id)
                .transportista(request.getTransportista())
                .numeroPedido(request.getNumeroPedido())
                .destinatario(request.getDestinatario())
                .direccionDestino(request.getDireccionDestino())
                .descripcionCarga(request.getDescripcionCarga())
                .pesoKg(request.getPesoKg())
                .fechaCreacion(hoy)
                .s3Key(s3Key)
                .efsPath(rutaEfs)
                .build();
    }

    public byte[] descargarGuia(String s3Key, String transportista) {
        if (!s3Key.contains("/" + transportista + "/")) {
            throw new SecurityException("Acceso denegado: no tienes permisos para esta guía");
        }
        return awsS3Service.downloadAsBytes(bucket, s3Key);
    }

    public GuiaDespacho actualizarGuia(String s3KeyOriginal, GuiaRequest request)
            throws IOException, DocumentException {
        awsS3Service.deleteObject(bucket, s3KeyOriginal);
        return crearGuia(request);
    }

    public void eliminarGuia(String s3Key) {
        awsS3Service.deleteObject(bucket, s3Key);
        log.info("Guía eliminada de S3: {}", s3Key);
    }

    private File generarPdf(String id, GuiaRequest req, String rutaDestino)
            throws IOException, DocumentException {
        File file = new File(rutaDestino);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        Document doc = new Document();
        PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        Font titulo = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Font campo = new Font(Font.FontFamily.HELVETICA, 12);

        doc.add(new Paragraph("GUÍA DE DESPACHO", titulo));
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("ID Guía:        " + id, campo));
        doc.add(new Paragraph("N° Pedido:      " + req.getNumeroPedido(), campo));
        doc.add(new Paragraph("Transportista:  " + req.getTransportista(), campo));
        doc.add(new Paragraph("Destinatario:   " + req.getDestinatario(), campo));
        doc.add(new Paragraph("Dirección:      " + req.getDireccionDestino(), campo));
        doc.add(new Paragraph("Descripción:    " + req.getDescripcionCarga(), campo));
        doc.add(new Paragraph("Peso (kg):      " + req.getPesoKg(), campo));
        doc.add(new Paragraph("Fecha:          " + LocalDate.now(), campo));

        doc.close();
        return file;
    }
}