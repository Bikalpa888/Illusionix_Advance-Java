package com.virinchi.demo.controller;

import com.virinchi.demo.model.ExportRecord;
import com.virinchi.demo.model.Order;
import com.virinchi.demo.model.signupModel;
import com.virinchi.demo.repository.ExportRecordRepository;
import com.virinchi.demo.repository.OrderRepository;
import com.virinchi.demo.repository.signupRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.imageio.ImageIO;

@Controller
@RequestMapping("/admin/export")
public class AdminExportController {

    private final signupRepo signupRepo;
    private final OrderRepository orderRepository;
    private final ExportRecordRepository exportRecordRepository;

    public AdminExportController(signupRepo signupRepo,
                                 OrderRepository orderRepository,
                                 ExportRecordRepository exportRecordRepository) {
        this.signupRepo = signupRepo;
        this.orderRepository = orderRepository;
        this.exportRecordRepository = exportRecordRepository;
    }

    private boolean isAdmin(HttpSession session){
        Object a = session.getAttribute("isAdmin");
        return a instanceof Boolean && (Boolean) a;
    }

    private String adminEmail(HttpSession session){
        Object e = session.getAttribute("adminEmail");
        return (e instanceof String) ? (String) e : null;
    }

    // ===== USERS CSV =====
    @GetMapping(value = "/users.csv")
    public ResponseEntity<byte[]> exportUsersCsv(HttpSession session){
        if(!isAdmin(session)) return ResponseEntity.status(403).build();
        var users = signupRepo.findSummaries();
        StringBuilder sb = new StringBuilder();
        sb.append("id,username,email,created_at\n");
        for (var u : users) {
            String created = u.getCreatedAtStr()==null?"":u.getCreatedAtStr();
            sb.append(u.getId()).append(',')
              .append(escape(u.getUsername())).append(',')
              .append(escape(u.getEmail())).append(',')
              .append(created).append('\n');
        }
        byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
        String fileName = "users-"+System.currentTimeMillis()+".csv";
        persistExport("USERS","CSV",fileName,data,adminEmail(session));
        return attachment(data, fileName, MediaType.parseMediaType("text/csv"));
    }

    // ===== USERS PNG =====
    @GetMapping(value = "/users.png")
    public ResponseEntity<byte[]> exportUsersPng(HttpSession session) {
        if(!isAdmin(session)) return ResponseEntity.status(403).build();
        var users = signupRepo.findSummaries();
        String[] headers = {"ID","Username","Email","Created At"};
        String[][] rows = new String[users.size()][];
        for (int i=0;i<users.size();i++){
            var u = users.get(i);
            rows[i] = new String[]{ String.valueOf(u.getId()), nz(u.getUsername()), nz(u.getEmail()), u.getCreatedAtStr()==null?"":u.getCreatedAtStr() };
        }
        byte[] png = drawTablePng(headers, rows, "Users Export");
        String fileName = "users-"+System.currentTimeMillis()+".png";
        persistExport("USERS","PNG",fileName,png,adminEmail(session));
        return attachment(png, fileName, MediaType.IMAGE_PNG);
    }

    // ===== ORDERS CSV =====
    @GetMapping(value = "/orders.csv")
    public ResponseEntity<byte[]> exportOrdersCsv(HttpSession session){
        if(!isAdmin(session)) return ResponseEntity.status(403).build();
        var orders = orderRepository.findAllForExport();
        StringBuilder sb = new StringBuilder();
        sb.append("order_number,user_name,user_email,total,status,created_at\n");
        for (var o : orders) {
            String created = o.getCreatedAtStr()==null?"":o.getCreatedAtStr();
            String total = o.getTotalStr()==null?"":o.getTotalStr();
            sb.append(escape(o.getOrderNumber())).append(',')
              .append(escape(o.getUserName())).append(',')
              .append(escape(o.getUserEmail())).append(',')
              .append(total).append(',')
              .append(escape(o.getStatus())).append(',')
              .append(created).append('\n');
        }
        byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
        String fileName = "orders-"+System.currentTimeMillis()+".csv";
        persistExport("ORDERS","CSV",fileName,data,adminEmail(session));
        return attachment(data, fileName, MediaType.parseMediaType("text/csv"));
    }

    // ===== ORDERS PNG =====
    @GetMapping(value = "/orders.png")
    public ResponseEntity<byte[]> exportOrdersPng(HttpSession session){
        if(!isAdmin(session)) return ResponseEntity.status(403).build();
        var orders = orderRepository.findAllForExport();
        String[] headers = {"Order #","Customer","Email","Total","Status","Created At"};
        String[][] rows = new String[orders.size()][];
        for(int i=0;i<orders.size();i++){
            var o = orders.get(i);
            rows[i] = new String[]{ nz(o.getOrderNumber()), nz(o.getUserName()), nz(o.getUserEmail()), nz(o.getTotalStr()), nz(o.getStatus()), o.getCreatedAtStr()==null?"":o.getCreatedAtStr() };
        }
        byte[] png = drawTablePng(headers, rows, "Orders Export");
        String fileName = "orders-"+System.currentTimeMillis()+".png";
        persistExport("ORDERS","PNG",fileName,png,adminEmail(session));
        return attachment(png, fileName, MediaType.IMAGE_PNG);
    }

    private ResponseEntity<byte[]> attachment(byte[] data, String fileName, MediaType mediaType){
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(mediaType)
                .body(data);
    }

    private void persistExport(String type, String format, String fileName, byte[] content, String createdBy){
        try {
            ExportRecord er = new ExportRecord();
            er.setType(type);
            er.setFormat(format);
            er.setFileName(fileName);
            er.setContent(content);
            er.setCreatedBy(createdBy);
            exportRecordRepository.save(er);
        } catch (Exception ignored) {}
    }

    private static String escape(String v){
        if(v == null) return "";
        boolean needQuotes = v.contains(",") || v.contains("\n") || v.contains("\"");
        String s = v.replace("\"","\"\"");
        return needQuotes ? '"' + s + '"' : s;
    }

    private static String nz(String v){ return v == null ? "" : v; }

    // Very simple PNG table renderer (no external deps)
    private static byte[] drawTablePng(String[] headers, String[][] rows, String title){
        int padding = 16;
        int rowHeight = 24;
        int headerHeight = 28;
        int titleHeight = 30;
        int cols = headers.length;
        // Estimate column widths
        int[] colW = new int[cols];
        for (int c=0;c<cols;c++) colW[c] = Math.max(120, headers[c].length()*9);
        for (String[] r : rows) {
            for (int c=0;c<cols && c<r.length;c++) {
                int w = (r[c]==null?0:r[c].length())*8;
                if (w > colW[c]) colW[c] = Math.min(420, w);
            }
        }
        int tableWidth = padding*2;
        for (int w : colW) tableWidth += w + 16;
        int tableHeight = padding*2 + titleHeight + headerHeight + (Math.max(1, rows.length) * rowHeight) + padding;

        BufferedImage img = new BufferedImage(tableWidth, Math.max(120, tableHeight), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(250,250,250)); g.fillRect(0,0,img.getWidth(),img.getHeight());
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.setColor(new Color(33,37,41));
        g.drawString(title, padding, padding + 16);

        int y = padding + titleHeight;
        // Header
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.setColor(new Color(230, 244, 255));
        g.fillRect(padding, y, tableWidth - padding*2, headerHeight);
        g.setColor(new Color(13,110,253));
        int x = padding;
        for (int c=0;c<cols;c++){
            g.drawString(headers[c], x+8, y + 18);
            x += colW[c] + 16;
        }
        y += headerHeight;
        // Rows
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        for (int r=0;r<rows.length;r++){
            x = padding;
            if (r % 2 == 1) { g.setColor(new Color(245,245,245)); g.fillRect(padding, y-18, tableWidth - padding*2, rowHeight); }
            g.setColor(new Color(52,58,64));
            for (int c=0;c<cols;c++){
                String cell = (c<rows[r].length && rows[r][c]!=null) ? rows[r][c] : "";
                g.drawString(trimToFit(cell, colW[c]/7), x+8, y);
                x += colW[c] + 16;
            }
            y += rowHeight;
        }
        g.setColor(new Color(222,226,230));
        g.drawRect(8,8, tableWidth-16, Math.max(120, tableHeight)-16);
        g.dispose();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            ImageIO.write(img, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private static String trimToFit(String s, int maxChars){
        if (s == null) return "";
        if (s.length() <= maxChars) return s;
        return s.substring(0, Math.max(0, maxChars-1)) + "…";
    }
}
