# Backend Task

อัปเดตล่าสุด: 2026-05-25

## Current Task

Backend hardening รอบสุดท้ายก่อนเริ่ม frontend

## Scope

- cleanup repository/model/factory comments ที่เหลือ
- ลด repository duplication และ method ที่ไม่จำเป็น
- เพิ่ม controller tests สำหรับ public/admin songs, queue, และ exception handler behavior
- รัน backend test suite ให้ผ่านทั้งหมด
- เตรียม backend output/report สำหรับส่งต่อ frontend

## Non-Goals

- ไม่เปลี่ยน public endpoint path
- ไม่ redesign database schema ใหญ่
- ไม่เพิ่ม dependency ใหม่
- ไม่แตะ frontend

## Expected Output

- `backend-output.md`
- `backend-test-report.md`
- `manager-summary.md`
- `final-summary.md`
