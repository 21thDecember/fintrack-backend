package com.personalfinance.backup.controller;

import com.personalfinance.backup.dto.BackupDataDto;
import com.personalfinance.backup.service.BackupService;
import com.personalfinance.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
public class BackupController {

    private final BackupService backupService;

    @GetMapping("/export")
    public ApiResponse<BackupDataDto> exportBackup() {
        BackupDataDto data = backupService.exportData();
        return ApiResponse.success("Xuất bản sao lưu dữ liệu thành công", data);
    }

    @PostMapping("/import")
    public ApiResponse<Void> importBackup(@RequestBody BackupDataDto data) {
        backupService.importData(data);
        return ApiResponse.success("Phục hồi dữ liệu từ bản sao lưu thành công", null);
    }
}
