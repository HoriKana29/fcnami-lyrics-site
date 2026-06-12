# UX/UI Review

อัปเดตล่าสุด: 2026-05-25

## 1. UX Issues

- ไม่มี blocker สำหรับ public flow หลัก
- หน้า request สื่อสารชัดว่า v1 ใช้ YouTube comments ไม่ใช่เว็บฟอร์ม
- หน้า queue มี search และ tier filter แต่ยังไม่มี status filter ใน UI เพราะ backend status จาก sheet ยังเป็นข้อความอิสระ

## 2. UI Issues

- Desktop screenshot ผ่าน: หน้า home และ queue ไม่ blank, text ไม่ล้น viewport, visual asset โหลดได้
- Palette ตรงทิศทาง Anime Music Room + Community Queue โดยใช้ midnight navy, pink, sky blue, mint และ gold
- Cards ใช้ radius 8px และไม่มี card ซ้อน card ใน page sections

## 3. Flow Concerns

- Browser direct route ใช้งานได้ใน Vite dev server
- Production deploy ที่ใช้ BrowserRouter ต้องมี fallback route ไป `index.html`
- ถ้า backend endpoint ยังไม่เปิดใน production UI จะ fallback demo data ซึ่งเหมาะกับรีวิว แต่ควรปิด/ปรับเมื่อมีข้อมูลจริง

## 4. Recommended Improvements

- เพิ่ม loading skeleton สำหรับ songs และ queue
- เพิ่ม admin dashboard เป็น task แยกหลัง auth
- เพิ่ม mobile visual QA ด้วย viewport จริงในรอบถัดไปถ้าเลือก browser automation stack ได้ครบ

## 5. Verification

- `npm run build` ผ่าน
- Browser smoke routes ผ่าน:
  - `/`
  - `/songs`
  - `/songs/blue-bird`
  - `/queue`
  - `/request`
  - `/about`
