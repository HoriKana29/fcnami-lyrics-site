# Frontend Task

อัปเดตล่าสุด: 2026-05-25

## Current Task

สร้าง frontend v1 หลัง backend hardening พร้อม โดยยึด design direction:

- Anime Music Room + Community Queue
- Midnight Subtitle Room ผสม usability แบบ lyric archive
- Thai-friendly browsing/search/queue flow

## Scope

- สร้าง frontend app structure ใน repo
- ทำ public routes หลัก:
  - `/`
  - `/songs`
  - `/songs/:slug`
  - `/request`
  - `/queue`
  - `/about`
- เชื่อม backend endpoints เท่าที่มี:
  - `GET /api/songs`
  - `GET /api/songs/{slug}`
  - `GET /api/queue`
- ถ้า backend ไม่พร้อมหรือไม่มี data ให้มี fallback/mock state ที่ UI ใช้งานได้
- ใช้ visual language ตาม notes:
  - midnight navy
  - soft pink accent
  - anime sky blue
  - cards/rows/search/status badges สำหรับ queue

## Non-Goals

- ยังไม่ทำ admin auth จริง
- ยังไม่ทำ request form บนเว็บ
- ยังไม่เพิ่ม YouTube API integration

## Expected Output

- `frontend-output.md`
- `ux-ui-review.md`
- อัปเดต `manager-summary.md`
- อัปเดต `final-summary.md`
