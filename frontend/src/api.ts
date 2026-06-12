import type { PageResponse, QueueResponse, SongResponse } from "./types";

const fallbackSongs: SongResponse[] = [
  {
    id: 1,
    slug: "blue-bird",
    title: "Blue Bird",
    titleJapanese: "ブルーバード",
    artist: "Ikimono Gakari",
    sourceAnimeOrGame: "Naruto Shippuden",
    youtubeUrl: "https://www.youtube.com/watch?v=KpsJWFuVTdI",
    youtubeVideoId: "KpsJWFuVTdI",
    thumbnailUrl: null,
    status: "PUBLISHED",
    tags: ["anime", "opening"],
    moods: ["bright", "nostalgic"],
    lyrics: {
      kanji: "飛翔いたら 戻らないと言って",
      romaji: "Habataitara modoranai to itte",
      thai: "ถ้ากางปีกบินไปแล้ว ก็จะไม่หวนกลับมาอีก",
      notes: "ตัวอย่างข้อมูลสำหรับหน้า frontend"
    }
  },
  {
    id: 2,
    slug: "brave-shine",
    title: "Brave Shine",
    titleJapanese: "ブレイブシャイン",
    artist: "Aimer",
    sourceAnimeOrGame: "Fate/stay night",
    youtubeUrl: "https://www.youtube.com/watch?v=VQ2D8rZ0v8A",
    youtubeVideoId: "VQ2D8rZ0v8A",
    thumbnailUrl: null,
    status: "TRANSLATING",
    tags: ["anime", "ending"],
    moods: ["dramatic"],
    lyrics: {
      kanji: "左手に隠した 願いは願いのままで",
      romaji: "Hidarite ni kakushita negai wa negai no mama de",
      thai: "คำอธิษฐานที่ซ่อนไว้ในมือซ้าย ยังคงเป็นเพียงคำอธิษฐาน"
    }
  },
  {
    id: 3,
    slug: "compass-song",
    title: "Compass Song",
    titleJapanese: null,
    artist: "Kano",
    sourceAnimeOrGame: "Soukou Musume Senki",
    youtubeUrl: null,
    youtubeVideoId: null,
    thumbnailUrl: null,
    status: "IDEA",
    tags: ["game"],
    moods: ["soft"],
    lyrics: {
      kanji: "",
      romaji: "",
      thai: "กำลังจัดเตรียมคำแปล"
    }
  }
];

const fallbackQueue: QueueResponse = {
  syncedAt: new Date().toISOString(),
  total: 5,
  items: [
    {
      tier: "Main Road",
      queueNumber: 1,
      songTitle: "Tsunai da Te",
      requester: "คุณ Late night",
      songUrl: "https://youtube.com",
      status: "รอแปล",
      translated: false,
      requestDate: "2026-05-20",
      waitingDays: 5
    },
    {
      tier: "Main Road",
      queueNumber: 2,
      songTitle: "Authentic Symphony",
      requester: "คุณ anime ed",
      songUrl: "https://youtube.com",
      status: "กำลังแปล",
      translated: false,
      requestDate: "2026-05-21",
      waitingDays: 4
    },
    {
      tier: "2nd Request",
      queueNumber: 1,
      songTitle: "Brave Shine",
      requester: "คุณ warm sad",
      songUrl: "https://youtube.com",
      status: "มีในคลังเพลง",
      translated: true,
      requestDate: "2026-05-22",
      waitingDays: 3
    },
    {
      tier: "3rd Request",
      queueNumber: 1,
      songTitle: "Compass Song",
      requester: "คุณ acoustic",
      songUrl: "https://youtube.com",
      status: "รอคิว",
      translated: false,
      requestDate: "2026-05-23",
      waitingDays: 2
    },
    {
      tier: "4th Request",
      queueNumber: 1,
      songTitle: "ペテン師ごっこ",
      requester: "คุณ soft piano",
      songUrl: "https://youtube.com",
      status: "รอคิว",
      translated: false,
      requestDate: "2026-05-24",
      waitingDays: 1
    }
  ]
};

export async function fetchSongs(search = ""): Promise<PageResponse<SongResponse>> {
  const params = new URLSearchParams();
  if (search.trim()) params.set("search", search.trim());
  try {
    const response = await fetch(`/api/songs?${params.toString()}`);
    if (!response.ok) throw new Error("songs request failed");
    return await response.json();
  } catch {
    const content = filterSongs(fallbackSongs, search);
    return {
      content,
      totalElements: content.length,
      totalPages: 1,
      page: 0,
      size: content.length,
      first: true,
      last: true
    };
  }
}

export async function fetchSong(slug: string): Promise<SongResponse> {
  try {
    const response = await fetch(`/api/songs/${slug}`);
    if (!response.ok) throw new Error("song request failed");
    return await response.json();
  } catch {
    return fallbackSongs.find((song) => song.slug === slug) ?? fallbackSongs[0];
  }
}

export async function fetchQueue(search = "", status = "", tier = ""): Promise<QueueResponse> {
  const params = new URLSearchParams();
  if (search.trim()) params.set("search", search.trim());
  if (status.trim()) params.set("status", status.trim());
  if (tier.trim()) params.set("tier", tier.trim());
  try {
    const response = await fetch(`/api/queue?${params.toString()}`);
    if (!response.ok) throw new Error("queue request failed");
    return await response.json();
  } catch {
    const needle = search.trim().toLowerCase();
    const items = fallbackQueue.items.filter((item) => {
      const matchesSearch =
        !needle ||
        [item.songTitle, item.requester, item.status, item.tier].some((value) =>
          value?.toLowerCase().includes(needle)
        );
      const matchesStatus = !status || item.status === status;
      const matchesTier = !tier || item.tier === tier;
      return matchesSearch && matchesStatus && matchesTier;
    });
    return { ...fallbackQueue, total: items.length, items };
  }
}

export const demoSongs = fallbackSongs;
export const demoQueue = fallbackQueue;

function filterSongs(songs: SongResponse[], search: string) {
  const needle = search.trim().toLowerCase();
  if (!needle) return songs;
  return songs.filter((song) =>
    [song.title, song.titleJapanese, song.artist, song.sourceAnimeOrGame]
      .filter(Boolean)
      .some((value) => value!.toLowerCase().includes(needle))
  );
}
