package com.duoc.SumativaCloudNative.service;

import com.duoc.SumativaCloudNative.dto.GuiaRequest;
import com.duoc.SumativaCloudNative.model.GuiaDespacho;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuiaService {

    private final AwsS3Service awsS3Service;
    private final EfsService efsService;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${efs.path}")
    private String efsPath;

    /**
     * Crea una guía de despacho:
     * 1. Genera el PDF con iText
     * 2. Guarda temporalmente en EFS
     * 3. Sube automáticamente a S3 con estructura /YYYYMMDD/transportista/guia.pdf
     */
    public GuiaDespacho crearGuia(GuiaRequest request) throws IOException, DocumentException {
        String id = UUID.randomUUID().toString().substring(0, 8);
        LocalDate hoy = LocalDate.now();
        String fecha = hoy.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String nombreArchivo = "guia_" + id + ".pdf";

        // 1. Generar PDF en EFS temporalmente
        String efsDest = efsPath + "/" + nombreArchivo;
        File archivoPdf = generarPdf(id, request, efsDest);
        log.info("PDF guardado en EFS: {}", efsDest);

        // 2. Subir a S3 con estructura requerida: /YYYYMMDD/transportista/guia.pdf
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
                .efsPath(efsDest)
                .build();
    }

    /**
     * Descarga una guía de S3 validando que el transportista tenga permisos
     */
    public byte[] descargarGuia(String s3Key, String transportistaSolicitante) {
        // Validación de permisos: la key debe contener el nombre del transportista
        if (!s3Key.contains("/" + transportistaSolicitante + "/")) {
            throw new SecurityException("Acceso denegado: no tienes permisos para descargar esta guía");
        }
        return awsS3Service.downloadAsBytes(bucket, s3Key);
    }

    /**
     * Actualiza (reemplaza) una guía existente en S3
     */
    public GuiaDespacho actualizarGuia(String s3KeyOriginal, GuiaRequest request) 
            throws IOException, DocumentException {
        // Eliminamos la guía anterior
        awsS3Service.deleteObject(bucket, s3KeyOriginal);

        // Creamos la nueva con los datos actualizados
        return crearGuia(request);
    }

    /**
     * Elimina una guía de S3
     */
    public void eliminarGuia(String s3Key) {
        awsS3Service.deleteObject(bucket, s3Key);
        log.info("Guía eliminada: {}", s3Key);
    }

    /**
     * Genera el PDF de la guía de despacho con iTextPDF
     */
    private File generarPdf(String id, GuiaRequest req, String rutaDestino) 
            throws IOException, DocumentException {
        File file = new File(rutaDestino);
        file.getParentFile().mkdirs();

        Document doc = new Document();
        PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        Font titulo = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Font campo = new Font(Font.FontFamily.HELVETICA, 12);

        doc.add(new Paragraph("GUÍA DE DESPACHO", titulo));
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("ID Guía:          " + id, campo));
        doc.add(new Paragraph("N° Pedido:        " + req.getNumeroPedido(), campo));
        doc.add(new Paragraph("Transportista:    " + req.getTransportista(), campo));
        doc.add(new Paragraph("Destinatario:     " + req.getDestinatario(), campo));
        doc.add(new Paragraph("Dirección:        " + req.getDireccionDestino(), campo));
        doc.add(new Paragraph("Descripción:      " + req.getDescripcionCarga(), campo));
        doc.add(new Paragraph("Peso (kg):        " + req.getPesoKg(), campo));
        doc.add(new Paragraph("Fecha:            " + LocalDate.now(), campo));

        doc.close();
        return file;
    }
}
