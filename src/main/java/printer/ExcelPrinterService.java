package printer;

import model.Ticket;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.stream.Collectors;

public class ExcelPrinterService {
    private static final String TEMPLATE_FILE = "templates/template_report.xlsx";
    private static final String TARGET_FILE_NAME = "jira_report.xlsx";

    public void printReport(Set<Ticket> tickets) throws IOException, URISyntaxException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(TEMPLATE_FILE);
        XSSFWorkbook workbook = new XSSFWorkbook(is);
        XSSFSheet sheet = workbook.getSheet("Jira ServiceDesk Report");

        int rowNum = 15;
        System.out.println("Creating Jira ServiceDesk Report");

        for (Ticket ticket : tickets) {
            rowNum++;
            if (rowNum != 16)
                sheet.shiftRows(rowNum, rowNum + 1, 1);

            Row row = sheet.createRow(rowNum);
            int colNum = 0;
            row.createCell(colNum++).setCellValue(ticket.getTicketKey());
            row.createCell(colNum++).setCellValue(ticket.getType());
            row.createCell(colNum++).setCellValue(ticket.getStatus());
            row.createCell(colNum++).setCellValue(ticket.getCreated());
            row.createCell(colNum++).setCellValue(ticket.getDescription());
            row.createCell(colNum++).setCellValue(ticket.getPriority());
            row.createCell(colNum++).setCellValue(ticket.getPaid());
            row.createCell(colNum++).setCellValue(ticket.getElapsedTime());
            row.createCell(colNum).setCellValue(ticket.getRemainingTime());
        }
        //Set total tickets count
        sheet.getRow(++rowNum).getCell(1).setCellValue(tickets.size());

        int ticketCountResolvedOnTime = tickets.stream()
                .filter(ticket -> !ticket.getElapsedTime().contains("-"))
                .collect(Collectors.toSet())
                .size();
        //Set total tickets count resolved on time
        sheet.getRow(++rowNum).getCell(1).setCellValue(ticketCountResolvedOnTime);

//        System.out.println(getClass().getProtectionDomain().getCodeSource().getLocation());
//        System.out.println(new File("").getAbsolutePath());
        try {
            FileOutputStream outputStream = new FileOutputStream(TARGET_FILE_NAME);
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Done");
    }

}
