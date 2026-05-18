package com.fcnami.backend.Model.SongTags;

public enum TagType {
    // แนวเพลงว่าเป็นแนวไหน
    GENRE,
    // ให้อารมณ์เพลงแบบไหน
    MOOD,
    // ศิลปิน
    ARTIST,

    // เราแปลแต่เพลงญี่ปุ่นอยู่แล้ว น้อยมากกก ที่จะแปลเพลงภาษาอื่น ไม่ต้องมี
    LANGUAGE,

    // มาจาก Anime/Game/หนังเรื่องไหน
    SOURCE,

    // ความหมายเพลงแนวไหน
    THEME
}
