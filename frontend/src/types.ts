export type SongStatus =
  | "DRAFT"
  | "IDEA"
  | "TRANSLATING"
  | "EDITING"
  | "READY_TO_UPLOAD"
  | "UPLOADED"
  | "TRANSLATED"
  | "PUBLISHED"
  | "ARCHIVED";

export type LyricsResponse = {
  kanji?: string | null;
  romaji?: string | null;
  thai?: string | null;
  notes?: string | null;
  updatedAt?: string | null;
};

export type SongResponse = {
  id: number;
  slug: string;
  title: string;
  titleJapanese?: string | null;
  artist: string;
  sourceAnimeOrGame?: string | null;
  youtubeUrl?: string | null;
  youtubeVideoId?: string | null;
  thumbnailUrl?: string | null;
  status: SongStatus;
  tags: string[];
  moods: string[];
  createdAt?: string | null;
  updatedAt?: string | null;
  publishedAt?: string | null;
  lyrics?: LyricsResponse | null;
};

export type PageResponse<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
  first: boolean;
  last: boolean;
};

export type QueueItemResponse = {
  tier: string;
  queueNumber?: number | null;
  songTitle: string;
  requester?: string | null;
  songLinkLabel?: string | null;
  songUrl?: string | null;
  status?: string | null;
  translated?: boolean | null;
  requestDate?: string | null;
  waitingDays?: number | null;
  rawRow?: Record<string, string>;
};

export type QueueResponse = {
  syncedAt: string;
  total: number;
  items: QueueItemResponse[];
};
