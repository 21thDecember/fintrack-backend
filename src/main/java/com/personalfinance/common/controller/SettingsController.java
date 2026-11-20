package com.personalfinance.common.controller;

import com.personalfinance.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final List<Map<String, Object>> rules = Collections.synchronizedList(new ArrayList<>(List.of(
            new HashMap<>(Map.of("id", "1", "keyword", "GRAB", "categoryId", "cat-exp-2", "categoryName", "Di chuyển", "priority", 1)),
            new HashMap<>(Map.of("id", "2", "keyword", "SHOPEE", "categoryId", "cat-exp-3", "categoryName", "Mua sắm", "priority", 2)),
            new HashMap<>(Map.of("id", "3", "keyword", "EVN", "categoryId", "cat-exp-8", "categoryName", "Hóa đơn & Tiện ích", "priority", 3)),
            new HashMap<>(Map.of("id", "4", "keyword", "TIKI", "categoryId", "cat-exp-3", "categoryName", "Mua sắm", "priority", 4)),
            new HashMap<>(Map.of("id", "5", "keyword", "HIGHLANDS", "categoryId", "cat-exp-1", "categoryName", "Ăn uống", "priority", 5)),
            new HashMap<>(Map.of("id", "6", "keyword", "LUONG", "categoryId", "cat-inc-1", "categoryName", "Lương", "priority", 6))
    )));

    @GetMapping("/classification-rules")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRules() {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách quy tắc thành công", new ArrayList<>(rules)));
    }

    @PostMapping("/classification-rules")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addRule(@RequestBody Map<String, Object> rule) {
        Map<String, Object> newRule = new HashMap<>(rule);
        newRule.put("id", UUID.randomUUID().toString());
        rules.add(newRule);
        return ResponseEntity.ok(ApiResponse.success("Thêm quy tắc thành công", newRule));
    }

    @DeleteMapping("/classification-rules/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable String id) {
        rules.removeIf(r -> Objects.equals(r.get("id"), id));
        return ResponseEntity.ok(ApiResponse.success("Xóa quy tắc thành công", null));
    }
}
