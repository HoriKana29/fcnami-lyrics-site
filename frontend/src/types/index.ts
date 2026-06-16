export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  pageNumber: number;
  pageSize: number;
  first: boolean;
  last: boolean;
}

export interface SongResponse {
  id: number;
  slug: string;
  title: string;
  titleJapanese: string;
  artist: string;
  sourceAnimeOrGame: string;
  youtubeUrl: string;
  youtubeVideoId: string;
  thumbnailUrl: string;
  status: 'DRAFT' | 'TRANSLATED' | 'PUBLISHED' | 'ARCHIVED';
  tags: string[];
  moods: string[];
  createdAt: string;
  updatedAt: string;
  publishedAt: string;
  viewCount?: number;
  lyrics: LyricsResponse;
}

export interface LyricsResponse {
  kanji: string;
  romaji: string;
  thai: string;
  notes: string;
  updatedAt: string;
}
