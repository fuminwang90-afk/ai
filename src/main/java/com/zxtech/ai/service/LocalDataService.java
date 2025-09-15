package com.zxtech.ai.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class LocalDataService {

    public List<String> getAllTexts() {
        // 模拟：从本地数据库取出的数据
        return Arrays.asList(
                "故宫是中国明清两代的皇家宫殿。",
                "长城是世界七大奇迹之一。",
                "杭州是中国著名的旅游城市。"
        );

        // 真实场景：你可以查询数据库，返回 List<String>
    }


    public List<String> getTextFromPdf(String filePath) {
        List<String> texts = new ArrayList<>();
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String fullText = pdfStripper.getText(document);

            for (String paragraph : fullText.split("\n")) {
                String cleaned = paragraph.trim();
                if (!cleaned.isEmpty()) {
                    texts.add(cleaned);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return texts;
    }
}
