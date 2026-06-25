package com.duoc.SumativaCloudNative.service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.duoc.SumativaCloudNative.dto.GuiaRequest;
import com.duoc.SumativaCloudNative.dto.GuiaResponse;
import com.duoc.SumativaCloudNative.model.GuiaDespacho;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GuiaDespachoService {

    private final AwsS3Service awsS3Service;

    private final ObjectMapper objectMapper;

    @Value("${efs.path}")
    private String efsPath;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public GuiaResponse crearGuia(GuiaRequest request) throws Exception {

        String numeroGuia =
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8);

        String nombrePdf =
                "guia-" + numeroGuia + ".pdf";

        File carpeta =
                new File(efsPath + "/guias");

        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        File archivoPdf =
                new File(carpeta, nombrePdf);

        generarPdf(
                archivoPdf,
                numeroGuia,
                request);

        GuiaDespacho guia =
                new GuiaDespacho();

        guia.setNumero(numeroGuia);
        guia.setCliente(request.getCliente());
        guia.setDireccion(request.getDireccion());
        guia.setProducto(request.getProducto());
        guia.setCantidad(request.getCantidad());
        guia.setTransportista(request.getTransportista());
        guia.setFecha(request.getFecha());
        guia.setPdf(nombrePdf);

        File archivoJson =
                new File(
                        carpeta,
                        "guia-" + numeroGuia + ".json");

        objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValue(
                        archivoJson,
                        guia);

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

        Document document =
                new Document();

        PdfWriter.getInstance(
                document,
                new FileOutputStream(
                        archivo));

        document.open();

        document.add(
                new Paragraph(
                        "GUIA DE DESPACHO"));

        document.add(
                new Paragraph(
                        "Numero: "
                                + numeroGuia));

        document.add(
                new Paragraph(
                        "Cliente: "
                                + request.getCliente()));

        document.add(
                new Paragraph(
                        "Direccion: "
                                + request.getDireccion()));

        document.add(
                new Paragraph(
                        "Producto: "
                                + request.getProducto()));

        document.add(
                new Paragraph(
                        "Cantidad: "
                                + request.getCantidad()));

        document.add(
                new Paragraph(
                        "Transportista: "
                                + request.getTransportista()));

        document.add(
                new Paragraph(
                        "Fecha: "
                                + request.getFecha()));

        document.close();
    }

    public List<GuiaDespacho> buscar(
            String transportista,
            String fecha)
            throws Exception {

        File carpeta =
                new File(
                        efsPath +
                                "/guias");

        File[] archivos =
                carpeta.listFiles(
                        (dir, name)
                                -> name.endsWith(".json"));

        List<GuiaDespacho> resultado =
                new ArrayList<>();

        if (archivos == null) {
            return resultado;
        }

        for (File archivo : archivos) {

            GuiaDespacho guia =
                    objectMapper.readValue(
                            archivo,
                            GuiaDespacho.class);

            if (guia.getTransportista()
                    .equalsIgnoreCase(
                            transportista)
                    &&
                    guia.getFecha()
                            .equals(fecha)) {

                resultado.add(
                        guia);
            }
        }

        return resultado;
    }
}