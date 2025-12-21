package org.itmo.lab3.controller;

import org.itmo.lab3.model.ImportHistory;
import org.itmo.lab3.service.DistributedTransactionService;
import org.itmo.lab3.service.ImportService;
import org.itmo.lab3.service.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/import")
public class ImportController {

    private final ImportService importService;
    private final DistributedTransactionService distributedTransactionService;

    @Autowired
    public ImportController(ImportService importService,
                          DistributedTransactionService distributedTransactionService) {
        this.importService = importService;
        this.distributedTransactionService = distributedTransactionService;
    }

    @GetMapping
    public String showImportPage(Model model) {
        List<ImportHistory> history = importService.getImportHistory();
        model.addAttribute("importHistory", history);
        return "import";
    }

    /**
     * Загрузка файла с использованием распределенной транзакции.
     * Файл сохраняется в MinIO, данные импортируются в БД атомарно.
     */
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                           @RequestParam("username") String username,
                           RedirectAttributes redirectAttributes) {
        
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Файл не выбран");
            return "redirect:/import";
        }

        if (username == null || username.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Имя пользователя не может быть пустым");
            return "redirect:/import";
        }

        // Выполняем распределенную транзакцию (MinIO + БД)
        DistributedTransactionService.ImportResult result = 
            distributedTransactionService.executeDistributedImport(file, username);
        
        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("success", 
                "Успешно импортировано " + result.getHistory().getImportedCount() + " организаций");
        } else {
            redirectAttributes.addFlashAttribute("error", 
                "Ошибка импорта: " + result.getErrorMessage());
        }

        return "redirect:/import";
    }

    /**
     * Скачивание файла импорта из MinIO.
     */
    @GetMapping("/download/{objectName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String objectName) {
        try {
            byte[] content = distributedTransactionService.downloadImportFile(objectName);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + objectName + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(content);
        } catch (MinioService.MinioDownloadException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
