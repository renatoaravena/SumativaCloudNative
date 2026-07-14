package com.duoc.SumativaCloudNative.repository;

import com.duoc.SumativaCloudNative.model.GuiaProcesada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface GuiaProcesadaRepository extends JpaRepository<GuiaProcesada, Long> {

    List<GuiaProcesada> findByTransportistaAndFechaProcesadoBetween(
            String transportista, LocalDateTime desde, LocalDateTime hasta);
}