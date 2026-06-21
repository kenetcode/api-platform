package com.rhu.api_platform.planilla.boleta;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.rhu.api_platform.planilla.dto.DetallePlanillaResponse;
import com.rhu.api_platform.planilla.dto.PlanillaResponse;
import org.springframework.stereotype.Service;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class GeneradorPdf {

    private static final Font TITULO_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
    private static final Font SUBTITULO_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font NEGRITA_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font PEQUENO_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8);
    private static final Color AZUL_HEADER = new Color(0, 51, 102);
    private static final Color GRIS_CLARO = new Color(240, 240, 240);

    public byte[] generarBoletaIndividual(BoletaResponse boleta) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
        try {
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Paragraph titulo = new Paragraph(boleta.getEmpresa(), TITULO_FONT);
            titulo.setAlignment(Element.ALIGN_CENTER);
            doc.add(titulo);

            Paragraph subtitulo = new Paragraph("BOLETA DE PAGO DE SALARIO", SUBTITULO_FONT);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            doc.add(subtitulo);

            String fechaStr = boleta.getFechaGeneracion() != null
                    ? boleta.getFechaGeneracion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "-";
            Paragraph periodo = new Paragraph(
                    "Período: " + boleta.getPeriodoMes() + "  |  " + boleta.getTipoPlanilla() +
                    "  |  Generada: " + fechaStr, PEQUENO_FONT);
            periodo.setAlignment(Element.ALIGN_CENTER);
            doc.add(periodo);
            doc.add(Chunk.NEWLINE);

            // Datos empleado
            PdfPTable tablaEmp = new PdfPTable(4);
            tablaEmp.setWidthPercentage(100);
            tablaEmp.setWidths(new float[]{2, 3, 2, 3});
            addCeldaEtiqueta(tablaEmp, "Empleado ID:");
            tablaEmp.addCell(celdaValor(String.valueOf(boleta.getEmpleadoId())));
            addCeldaEtiqueta(tablaEmp, "Nombre:");
            tablaEmp.addCell(celdaValor(boleta.getNombreCompleto()));
            addCeldaEtiqueta(tablaEmp, "DUI:");
            tablaEmp.addCell(celdaValor(boleta.getDui()));
            addCeldaEtiqueta(tablaEmp, "Cargo:");
            tablaEmp.addCell(celdaValor(boleta.getCargo()));
            addCeldaEtiqueta(tablaEmp, "Departamento:");
            tablaEmp.addCell(celdaValor(boleta.getDepartamento()));
            addCeldaEtiqueta(tablaEmp, "AFP:");
            tablaEmp.addCell(celdaValor(boleta.getAfp()));
            addCeldaEtiqueta(tablaEmp, "Días laborados:");
            tablaEmp.addCell(celdaValor(String.valueOf(boleta.getDiasLaborados())));
            addCeldaEtiqueta(tablaEmp, "Salario base:");
            tablaEmp.addCell(celdaValor("$" + fmt(boleta.getSalarioBase())));
            doc.add(tablaEmp);
            doc.add(Chunk.NEWLINE);

            // Percepciones y deducciones
            PdfPTable tablaCalc = new PdfPTable(2);
            tablaCalc.setWidthPercentage(100);

            PdfPTable perc = new PdfPTable(2);
            perc.addCell(celdaSeccion("PERCEPCIONES", 2));
            addFila(perc, "Salario proporcional", boleta.getSalarioProporcional());
            addFila(perc, "H.E. diurnas", boleta.getHorasExtraDiurnas());
            addFila(perc, "H.E. nocturnas", boleta.getHorasExtraNocturnas());
            addFila(perc, "Comisiones", boleta.getComisiones());
            addFila(perc, "Bonificaciones", boleta.getBonificaciones());
            addFilaTotal(perc, "TOTAL PERCEPCIONES", boleta.getTotalPercepciones());
            PdfPCell cellPerc = new PdfPCell(perc);
            cellPerc.setPadding(0);
            tablaCalc.addCell(cellPerc);

            PdfPTable ded = new PdfPTable(2);
            ded.addCell(celdaSeccion("DEDUCCIONES EMPLEADO", 2));
            addFila(ded, "ISSS (3%)", boleta.getIsss());
            addFila(ded, "AFP (7.25%)", boleta.getAfpMonto());
            addFila(ded, "ISR", boleta.getIsr());
            addFila(ded, "Descuentos voluntarios", boleta.getDescuentosVoluntarios());
            addFilaTotal(ded, "TOTAL DEDUCCIONES", boleta.getTotalDeducciones());
            PdfPCell cellDed = new PdfPCell(ded);
            cellDed.setPadding(0);
            tablaCalc.addCell(cellDed);
            doc.add(tablaCalc);
            doc.add(Chunk.NEWLINE);

            // Neto a pagar
            PdfPTable tablaNet = new PdfPTable(2);
            tablaNet.setWidthPercentage(50);
            tablaNet.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Font fNeto = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            fNeto.setColor(Color.WHITE);
            PdfPCell celdaNeto = new PdfPCell(new Phrase("NETO A PAGAR", fNeto));
            celdaNeto.setBackgroundColor(AZUL_HEADER);
            celdaNeto.setPadding(6);
            tablaNet.addCell(celdaNeto);
            PdfPCell celdaValorNeto = new PdfPCell(new Phrase("$" + fmt(boleta.getSalarioNeto()), NEGRITA_FONT));
            celdaValorNeto.setPadding(6);
            celdaValorNeto.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tablaNet.addCell(celdaValorNeto);
            doc.add(tablaNet);
            doc.add(Chunk.NEWLINE);

            // Aportes patronales
            PdfPTable tablaPatr = new PdfPTable(2);
            tablaPatr.setWidthPercentage(60);
            tablaPatr.addCell(celdaSeccion("APORTES PATRONALES", 2));
            addFila(tablaPatr, "ISSS patrono (7.5%)", boleta.getAportePatronalIsss());
            addFila(tablaPatr, "AFP patrono (8.75%)", boleta.getAportePatronalAfp());
            addFilaTotal(tablaPatr, "TOTAL APORTE PATRONAL", boleta.getTotalAportePatronal());
            doc.add(tablaPatr);

            Paragraph pie = new Paragraph("Este documento es un comprobante oficial de pago de salario.", PEQUENO_FONT);
            pie.setAlignment(Element.ALIGN_CENTER);
            doc.add(pie);
            doc.close();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar boleta PDF: " + e.getMessage(), e);
        }
        return baos.toByteArray();
    }

    public byte[] generarPlanillaCompleta(PlanillaResponse planilla, String nombreEmpresa) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 30, 30, 40, 40);
        try {
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Paragraph titulo = new Paragraph(nombreEmpresa, TITULO_FONT);
            titulo.setAlignment(Element.ALIGN_CENTER);
            doc.add(titulo);
            Paragraph subtitulo = new Paragraph(
                    "PLANILLA DE SALARIOS — Período: " + planilla.getPeriodoMes() +
                    " | " + planilla.getTipo() + " | Estado: " + planilla.getEstado(), SUBTITULO_FONT);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            doc.add(subtitulo);
            doc.add(Chunk.NEWLINE);

            if (planilla.getDetalles() != null && !planilla.getDetalles().isEmpty()) {
                PdfPTable tabla = new PdfPTable(12);
                tabla.setWidthPercentage(100);
                String[] headers = {"ID","Empleado","Días","Bruto","ISSS","AFP","ISR","Neto","P.ISSS","P.AFP","H.E.D","H.E.N"};
                for (String h : headers) {
                    Font fh = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
                    fh.setColor(Color.WHITE);
                    PdfPCell cell = new PdfPCell(new Phrase(h, fh));
                    cell.setBackgroundColor(AZUL_HEADER);
                    cell.setPadding(4);
                    tabla.addCell(cell);
                }
                boolean par = false;
                for (DetallePlanillaResponse d : planilla.getDetalles()) {
                    Color bg = par ? GRIS_CLARO : Color.WHITE;
                    addCeldaTabla(tabla, String.valueOf(d.getEmpleadoId()), bg);
                    addCeldaTabla(tabla, d.getNombreEmpleado(), bg);
                    addCeldaTabla(tabla, String.valueOf(d.getDiasLaborados()), bg);
                    addCeldaTabla(tabla, "$" + fmt(d.getSalarioBruto()), bg);
                    addCeldaTabla(tabla, "$" + fmt(d.getIsss()), bg);
                    addCeldaTabla(tabla, "$" + fmt(d.getAfp()), bg);
                    addCeldaTabla(tabla, "$" + fmt(d.getIsr()), bg);
                    addCeldaTabla(tabla, "$" + fmt(d.getSalarioNeto()), bg);
                    addCeldaTabla(tabla, "$" + fmt(d.getAportePatronalIsss()), bg);
                    addCeldaTabla(tabla, "$" + fmt(d.getAportePatronalAfp()), bg);
                    addCeldaTabla(tabla, fmt(d.getHorasExtraDiurnas()), bg);
                    addCeldaTabla(tabla, fmt(d.getHorasExtraNocturnas()), bg);
                    par = !par;
                }
                doc.add(tabla);
                doc.add(Chunk.NEWLINE);
            }

            PdfPTable tabTot = new PdfPTable(2);
            tabTot.setWidthPercentage(40);
            tabTot.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tabTot.addCell(celdaSeccion("TOTALES DEL PERÍODO", 2));
            addFila(tabTot, "Total bruto", planilla.getTotalBruto());
            addFila(tabTot, "Total ISSS", planilla.getTotalIsss());
            addFila(tabTot, "Total AFP", planilla.getTotalAfp());
            addFila(tabTot, "Total ISR", planilla.getTotalIsr());
            addFilaTotal(tabTot, "TOTAL NETO", planilla.getTotalNeto());
            doc.add(tabTot);
            doc.close();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF de planilla: " + e.getMessage(), e);
        }
        return baos.toByteArray();
    }

    private void addCeldaEtiqueta(PdfPTable t, String texto) {
        PdfPCell c = new PdfPCell(new Phrase(texto, NEGRITA_FONT));
        c.setPadding(3);
        c.setBorder(Rectangle.NO_BORDER);
        t.addCell(c);
    }
    private PdfPCell celdaValor(String texto) {
        PdfPCell c = new PdfPCell(new Phrase(texto != null ? texto : "-", NORMAL_FONT));
        c.setPadding(3);
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }
    private PdfPCell celdaSeccion(String texto, int colspan) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        f.setColor(Color.WHITE);
        PdfPCell c = new PdfPCell(new Phrase(texto, f));
        c.setColspan(colspan);
        c.setBackgroundColor(AZUL_HEADER);
        c.setPadding(4);
        return c;
    }
    private void addFila(PdfPTable t, String etiqueta, BigDecimal valor) {
        PdfPCell c1 = new PdfPCell(new Phrase(etiqueta, NORMAL_FONT));
        c1.setPadding(3);
        t.addCell(c1);
        PdfPCell c2 = new PdfPCell(new Phrase("$" + fmt(valor), NORMAL_FONT));
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c2.setPadding(3);
        t.addCell(c2);
    }
    private void addFilaTotal(PdfPTable t, String etiqueta, BigDecimal valor) {
        PdfPCell c1 = new PdfPCell(new Phrase(etiqueta, NEGRITA_FONT));
        c1.setBackgroundColor(GRIS_CLARO);
        c1.setPadding(3);
        t.addCell(c1);
        PdfPCell c2 = new PdfPCell(new Phrase("$" + fmt(valor), NEGRITA_FONT));
        c2.setBackgroundColor(GRIS_CLARO);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c2.setPadding(3);
        t.addCell(c2);
    }
    private void addCeldaTabla(PdfPTable t, String texto, Color bg) {
        PdfPCell c = new PdfPCell(new Phrase(texto != null ? texto : "-", PEQUENO_FONT));
        c.setBackgroundColor(bg);
        c.setPadding(3);
        t.addCell(c);
    }
    private String fmt(BigDecimal v) {
        return v == null ? "0.00" : String.format("%.2f", v);
    }
}
