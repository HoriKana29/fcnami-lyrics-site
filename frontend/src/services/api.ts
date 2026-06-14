import axios from 'axios';
import { PageResponse, SongResponse } from '@/types';

const api = axios.create({
  baseURL: '/api',
});

export const songService = {
  getSongs: async (search?: string, page = 0, size = 20) => {
    const response = await api.get<PageResponse<SongResponse>>('/songs', {
      params: { search, page, size },
    });
    return response.data;
  },
  getSongBySlug: async (slug: string) => {
    const response = await api.get<SongResponse>(`/songs/${slug}`);
    return response.data;
  },
  syncYouTube: async () => {
    // Note: This calls the admin endpoint. In a real app, this would need auth.
    const response = await api.post<{ syncedCount: number; message: string }>('/admin/songs/sync');
    return response.data;
  },
};

export default api;
