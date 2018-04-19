package printer;

import model.Ticket;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.format.TextStyle.FULL;
import static java.time.format.TextStyle.FULL_STANDALONE;

public class ExcelPrintService<T> implements PrintService<T> {
    private static final String TEMPLATE_FILE = "templates/template_report.xlsx";
    private final String reportPath;
    private LocalDate lastDayOfMonth;

    public ExcelPrintService(String reportPath) {
        this.reportPath = reportPath;
    }

    public void printReport(Set<Ticket> tickets) throws IOException {
        XSSFWorkbook workbook;
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(TEMPLATE_FILE)) {
            workbook = new XSSFWorkbook(is);
        }

        XSSFCellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(HSSFColor.WHITE.index);

        XSSFSheet sheet = workbook.getSheet("Jira ServiceDesk Report");

        int startRowNum = 15;
        System.out.println("Creating Jira ServiceDesk Report");

        setHeaderDates(sheet);
        fillTable(tickets, sheet, startRowNum, style);
        createReportFooter(workbook, sheet);
        setTotalAndResolvedOnTimeTicketsCount(tickets, sheet);
        saveReport(workbook);

        System.out.println("Done");
    }

    private void setHeaderDates(XSSFSheet sheet) {
        String headerDate1 = lastDayOfMonth.getMonth()
                .getDisplayName(FULL_STANDALONE, new Locale("ru")).toLowerCase() + " " +
                lastDayOfMonth.getYear() + "г.";
        String headerDate2 = "от " + lastDayOfMonth.getDayOfMonth() + " " +
                lastDayOfMonth.getMonth().getDisplayName(FULL, new Locale("ru")) + " " +
                lastDayOfMonth.getYear() + "г.";
        sheet.getRow(4).getCell(5).setCellValue(headerDate1);
        sheet.getRow(5).getCell(0).setCellValue(headerDate2);
    }

    private void fillTable(Set<Ticket> tickets, XSSFSheet sheet, int rowNum, XSSFCellStyle style) {
        for (Ticket ticket : tickets) {
            rowNum++;
            Row row = sheet.createRow(rowNum);

            int colNum = 0;
            createCell(style, ticket.getTicketKey(), row, colNum);
            createCell(style, ticket.getDescription(), row, ++colNum);
            createCell(style, ticket.getStatus(), row, ++colNum);
            createCell(style, ticket.getCreated()
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")), row, ++colNum);
            createCell(style, ticket.getType(), row, ++colNum);
            createCell(style, ticket.getPriority(), row, ++colNum);
            createCell(style, ticket.getPaid(), row, ++colNum);
            createCell(style, ticket.getElapsedTime()
                    .replace("h", "ч")
                    .replace("m", "м"), row, ++colNum);

            long minutes = ((ticket.getRemainingTime() / (1000 * 60)) % 60);
            long hours = ((ticket.getRemainingTime() / (1000 * 60 * 60)) % 24);
            createCell(style, hours + "ч " + minutes + "м", row, ++colNum);
            createCell(style, String.valueOf(ticket.getMinutesOfSupport()), row, ++colNum);
        }
    }

    private void createCell(XSSFCellStyle style, String par, Row row, int colNum) {
        Cell cell = row.createCell(colNum);
        cell.setCellStyle(style);
        cell.setCellValue(par);
    }

    private void createReportFooter(XSSFWorkbook workbook, XSSFSheet sheet) {
        XSSFSheet footerSheet = workbook.getSheet("Footer");
        footerSheet.getRow(13).getCell(3).setCellValue(lastDayOfMonth.getMonth()
                .getDisplayName(FULL_STANDALONE, new Locale("ru")).toLowerCase());
        footerSheet.getRow(13).getCell(4).setCellValue(lastDayOfMonth.getYear());
        List<Row> footer1 = new ArrayList<>();
        List<Row> footer2 = new ArrayList<>();
        List<Row> footer3 = new ArrayList<>();
        for (int row = 1; row < 9; row++) {
            footer1.add(footerSheet.getRow(row));
        }
        for (int row = 12; row < 16; row++) {
            footer2.add(footerSheet.getRow(row));
        }
        for (int row = 17; row < 19; row++) {
            footer3.add(footerSheet.getRow(row));
        }

        CellCopyPolicy policy = new CellCopyPolicy().createBuilder().build();
        sheet.copyRows(footer1, sheet.getLastRowNum() + 2, policy);
        sheet.copyRows(footer2, sheet.getLastRowNum() + 4, policy);
        sheet.copyRows(footer3, sheet.getLastRowNum() + 2, policy);
        workbook.removeSheetAt(1);
    }

    private void setTotalAndResolvedOnTimeTicketsCount(Set<Ticket> tickets, XSSFSheet sheet) {
        sheet.forEach(row -> {
            if (row.getCell(1) != null) {
                String header = row.getCell(1).getStringCellValue();
                Cell valueCell = row.getCell(3);
                switch (header) {
                    case "Количество поступивших заявок:":
                        valueCell.setCellValue(tickets.size());
                        break;
                    case "Количество выполненных своевременно:": {
                        int ticketCountResolvedOnTime = tickets.stream()
                                .filter(ticket -> !ticket.getElapsedTime().contains("-") && ticket.getStatus().equals("Closed"))
                                .collect(Collectors.toSet())
                                .size();
                        valueCell.setCellValue(ticketCountResolvedOnTime);
                        break;
                    }
                    case "Израсходованное количество минут поддержки": {
                        double minutesOfSupport = tickets.stream()
                                .map(Ticket::getMinutesOfSupport)
                                .mapToDouble(Double::doubleValue)
                                .sum();
                        valueCell.setCellValue(minutesOfSupport);
                        break;
                    }
                    case "Процент выполненных своевременно": {
                        double ticketsCompleted = tickets.stream()
                                .filter(ticket -> ticket.getStatus().equals("Closed"))
                                .count();
                        valueCell.setCellValue(ticketsCompleted / tickets.size() * 100 + "%");
                        break;
                    }
                    case "Общее количество дней отклонений от крайнего срока": {
                        double deviation = tickets.stream()
                                .filter(ticket -> ticket.getRemainingTime() < 0)
                                .mapToDouble(Ticket::getRemainingTime)
                                .sum();
                        int days = (int) ((deviation / (1000 * 60 * 60)) % 24) / 24;
                        valueCell.setCellValue(days);
                    }
                }
            }
        });
    }

    private void saveReport(XSSFWorkbook workbook) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(reportPath)) {
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            workbook.close();
        }
    }

    public void setDate(LocalDate lastDayOfMonth) {
        this.lastDayOfMonth = lastDayOfMonth;

    }

    @Override
    public void printReport(Collection<T> collection) {

    }
}
