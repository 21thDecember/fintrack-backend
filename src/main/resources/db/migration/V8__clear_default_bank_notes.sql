-- Xóa nội dung ghi chú mặc định do SePay webhook sinh ra: "Cổng: % | Tích lũy: %"
UPDATE transactions
SET note = NULL
WHERE note LIKE 'Cổng: %' AND (note LIKE '%Tích lũy:%' OR note LIKE '%Tích lũy: %');
