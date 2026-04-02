package com.swmanager.system.qrcode.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.swmanager.system.qrcode.domain.QrLicense;
import com.swmanager.system.qrcode.repository.QrLicenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QrLicenseService {

    private final QrLicenseRepository repository;

    @Transactional(readOnly = true)
    public List<QrLicense> getAll() {
        return repository.findAllByOrderByIssuedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<QrLicense> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return getAll();
        return repository.searchByKeyword(keyword.trim());
    }

    @Transactional(readOnly = true)
    public Optional<QrLicense> getById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public QrLicense issue(QrLicense qr) {
        // QR 코드에 담을 내용 구성
        String qrContent = buildQrContent(qr);
        // QR 이미지 생성 (Base64)
        String base64Image = generateQrBase64(qrContent, 300, 300);
        qr.setQrImageData(base64Image);
        return repository.save(qr);
    }

    @Transactional
    public QrLicense update(QrLicense qr) {
        String qrContent = buildQrContent(qr);
        String base64Image = generateQrBase64(qrContent, 300, 300);
        qr.setQrImageData(base64Image);
        return repository.save(qr);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    /**
     * QR 코드에 담을 텍스트 내용 구성
     */
    private String buildQrContent(QrLicense qr) {
        StringBuilder sb = new StringBuilder();
        sb.append("[License Certificate]\n");
        appendIfPresent(sb, "End User", qr.getEndUserName());
        appendIfPresent(sb, "Address", qr.getAddress());
        appendIfPresent(sb, "Tel", qr.getTel());
        appendIfPresent(sb, "Fax", qr.getFax());
        appendIfPresent(sb, "Products", qr.getProducts());
        appendIfPresent(sb, "User/Units", qr.getUserUnits());
        appendIfPresent(sb, "Version", qr.getVersion());
        appendIfPresent(sb, "License Type", qr.getLicenseType());
        appendIfPresent(sb, "Serial No", qr.getSerialNumber());
        appendIfPresent(sb, "Application", qr.getApplicationName());
        return sb.toString().trim();
    }

    private void appendIfPresent(StringBuilder sb, String label, String value) {
        if (value != null && !value.isBlank()) {
            sb.append(label).append(": ").append(value.trim()).append("\n");
        }
    }

    /**
     * QR 코드 이미지를 Base64 문자열로 생성
     */
    public String generateQrBase64(String content, int width, int height) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (WriterException | IOException e) {
            throw new RuntimeException("QR 코드 생성 실패", e);
        }
    }
}
