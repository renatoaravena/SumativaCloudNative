package com.duoc.SumativaCloudNative.service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.duoc.SumativaCloudNative.dto.GuiaRequest;
import com.duoc.SumativaCloudNative.dto.GuiaResponse;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GuiaDespachoService {

    private final AwsS3Service awsS3Service;

    @Value("${efs.path}")
    private String efsPath;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public GuiaResponse crearGuia(GuiaRequest request) throws Exception {

        String numeroGuia =
                UUID.randomUUID()
                .toString()
                .substring(0,8);

        String nombrePdf =
                "guia-" + numeroGuia + ".pdf";

        File archivoPdf =
                new File(
                        efsPath +
                        "/guias/" +
                        nombrePdf);

        archivoPdf.getParentFile().mkdirs();

        generarPdf(
                archivoPdf,
                numeroGuia,
                request);

        awsS3Service.uploadFile(
                bucket,
                "guias/" + nombrePdf,
                archivoPdf);

        return new GuiaResponse(
                numeroGuia,
                nombrePdf,
                "Guía creada correctamente");
    }

    private void generarPdf(
            File archivo,
            String numeroGuia,
            GuiaRequest request)
            throws Exception {

        Document document = new Document();

        PdfWriter.getInstance(
                document,
                new FileOutputStream(archivo));

        document.open();

        document.add(new Paragraph("GUIA DE DESPACHO"));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Numero: " + numeroGuia));
        document.add(new Paragraph("Cliente: " + request.getCliente()));
        document.add(new Paragraph("Direccion: " + request.getDireccion()));
        document.add(new Paragraph("Producto: " + request.getProducto()));
        document.add(new Paragraph("Cantidad: " + request.getCantidad()));

        document.close();
    }
}