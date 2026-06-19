import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, Loader2, FileText, ExternalLink, ThumbsUp, Eye } from 'lucide-react';
import { songService } from '@/services/api';
import { SongResponse } from '@/types';
import Navbar from '@/components/layout/Navbar';
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { parseSongTitle } from '@/lib/songParser';

const SongDetailPage = () => {
  const { slug } = useParams<{ slug: string }>();
  const navigate = useNavigate();
  const [song, setSong] = useState<SongResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [showLyrics, setShowLyrics] = useState(false);
  const [relatedSongs, setRelatedSongs] = useState<SongResponse[]>([]);

  useEffect(() => {
    const fetchSong = async () => {
      if (!slug) return;
      try {
        setLoading(true);
        const data = await songService.getSongBySlug(slug);
        setSong(data);

        // Determine playlist ID for related songs based on tags
        let playlistId = 'PL4wsZBSs9fgM30xS51w471hbiMvu6_HlW'; // Default all
        if (data.tags) {
          const lowerTags = Array.from(data.tags).map((t: string) => t.toLowerCase());
          if (lowerTags.includes('recommend')) {
            playlistId = 'PL4wsZBSs9fgMUEGPldykTC5o-dvh561X8';
          } else if (lowerTags.includes('long-play')) {
            playlistId = 'PL4wsZBSs9fgNl_NpEHpUbX2_zFyDHCyIR';
          } else if (lowerTags.includes('bandori')) {
            playlistId = 'PL4wsZBSs9fgPcrdcgxv_0QgcebGyA2Bms';
          } else if (lowerTags.includes('requested')) {
            playlistId = 'PL4wsZBSs9fgNUrzogGpFb7bhwzmwnqe-O';
          }
        }

        const related = await songService.getYouTubeSongs(playlistId);
        if (related && related.length > 0) {
          const filteredRelated = related
            .filter((s: SongResponse) => s.youtubeVideoId !== data.youtubeVideoId)
            .slice(0, 3);
          setRelatedSongs(filteredRelated);
        }
      } catch (error) {
        console.error('Failed to fetch song detail', error);
      } finally {
        setLoading(false);
      }
    };

    fetchSong();
  }, [slug]);

  if (loading) {
    return (
      <div className="bg-white min-h-screen flex flex-col">
        <Navbar />
        <div className="flex-1 flex items-center justify-center">
          <Loader2 className="animate-spin text-[#ff8c00]" size={48} />
        </div>
      </div>
    );
  }

  if (!song) return null;

  const parsed = parseSongTitle(song.title);

  return (
    <div className="bg-white min-h-screen flex flex-col">
      <Navbar />
      
      <div className="flex flex-col lg:flex-row min-h-screen pt-24 pb-16 px-6 md:px-16 lg:px-24 gap-12 max-w-7xl mx-auto w-full">
        {/* Left Side: Laptop frame & details */}
        <div className="flex-1 flex flex-col min-w-0">
          <header className="mb-6">
            <button 
              onClick={() => navigate(-1)} 
              className="inline-flex items-center gap-2 text-slate-400 hover:text-[#ff8c00] font-bold text-sm uppercase tracking-widest mb-4 transition-colors group"
            >
              <ArrowLeft size={16} className="group-hover:-translate-x-1 transition-transform" /> Back
            </button>
          </header>

          {/* Laptop Mock frame container */}
          <div className="relative w-full max-w-[640px] mb-8 self-center lg:self-start px-[4%]">
            {/* Laptop Screen Bezel */}
            <div className="relative border-[10px] md:border-[16px] border-slate-900 rounded-t-3xl bg-slate-900 shadow-2xl">
              {/* Camera Dot */}
              <div className="absolute top-1.5 md:top-2 left-1/2 -translate-x-1/2 w-1.5 h-1.5 md:w-2 md:h-2 rounded-full bg-slate-700"></div>
              {/* Screen Content */}
              <div className="aspect-video w-full overflow-hidden rounded bg-black">
                <iframe
                  width="100%"
                  height="100%"
                  src={`https://www.youtube.com/embed/${song.youtubeVideoId}?autoplay=0`}
                  title={parsed.title}
                  frameBorder="0"
                  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                  allowFullScreen
                  className="w-full h-full"
                ></iframe>
              </div>
            </div>
            {/* Laptop Base */}
            <div className="relative h-3 md:h-5 w-[106%] -left-[3%] rounded-b-2xl bg-gradient-to-b from-slate-200 via-slate-300 to-slate-400 border-t border-white/50 shadow-md">
              {/* Display Opener Notch */}
              <div className="absolute top-0 left-1/2 -translate-x-1/2 w-16 md:w-28 h-1 md:h-2 bg-slate-700/30 rounded-b-md"></div>
            </div>
          </div>

          {/* Details Row: Title & Likes/Views */}
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mt-2">
            <div className="flex flex-col">
              <h1 className="text-2xl md:text-3xl font-black text-slate-900 tracking-tight leading-tight">
                {song.title}
              </h1>
            </div>
            <div className="flex items-center gap-4 text-slate-800 flex-shrink-0">
              <button className="hover:text-[#ff8c00] transition-colors flex items-center gap-1.5 font-bold">
                <ThumbsUp size={24} className="stroke-[2.5]" />
              </button>
              <div className="flex items-center gap-1.5 font-bold">
                <Eye size={24} className="stroke-[2.5]" />
                <span className="text-lg">{song.viewCount ? song.viewCount.toLocaleString() : '0'}</span>
              </div>
            </div>
          </div>

          {/* Tags */}
          <div className="flex flex-wrap gap-2 mt-4">
            {song.tags?.map(tag => (
              <span key={tag} className="bg-slate-100 text-slate-500 px-3.5 py-1.5 rounded-full text-xs font-black uppercase tracking-tighter shadow-sm">
                {tag}
              </span>
            ))}
          </div>

          {/* Lyrics toggle section */}
          <div className="mt-6 flex flex-col gap-6">
            <button 
              onClick={() => setShowLyrics(!showLyrics)} 
              className="inline-flex items-center gap-2 bg-[#1a1a1a] hover:bg-[#ff8c00] text-white px-8 py-3.5 rounded-full font-black uppercase tracking-widest text-xs self-start transition-all hover:scale-105 active:scale-95 shadow-lg shadow-slate-200"
            >
              Lyrics <ExternalLink size={14} />
            </button>

            {/* Lyrics content drawer */}
            {showLyrics && (
              <div className="bg-slate-50 rounded-[2.5rem] p-6 md:p-10 border-2 border-slate-50 transition-all duration-300">
                <Tabs defaultValue="thai" className="w-full">
                  <TabsList className="bg-white p-1 rounded-2xl mb-8 border border-slate-100 flex w-fit">
                    <TabsTrigger value="thai" className="rounded-xl font-black uppercase tracking-widest text-xs px-6 py-3 data-[state=active]:bg-[#ff8c00] data-[state=active]:text-white transition-all">
                      Thai
                    </TabsTrigger>
                    <TabsTrigger value="romaji" className="rounded-xl font-black uppercase tracking-widest text-xs px-6 py-3 data-[state=active]:bg-[#ff8c00] data-[state=active]:text-white transition-all">
                      Romaji
                    </TabsTrigger>
                    <TabsTrigger value="kanji" className="rounded-xl font-black uppercase tracking-widest text-xs px-6 py-3 data-[state=active]:bg-[#ff8c00] data-[state=active]:text-white transition-all">
                      Kanji
                    </TabsTrigger>
                  </TabsList>
                  
                  <TabsContent value="thai" className="mt-0 focus-visible:outline-none">
                    <div className="whitespace-pre-line text-lg md:text-xl font-bold text-slate-800 leading-relaxed">
                      {song.lyrics?.thai || "Thai translation not available yet."}
                    </div>
                  </TabsContent>
                  
                  <TabsContent value="romaji" className="mt-0 focus-visible:outline-none">
                    <div className="whitespace-pre-line text-lg md:text-xl font-medium text-slate-600 leading-relaxed italic">
                      {song.lyrics?.romaji || "Romaji text not available yet."}
                    </div>
                  </TabsContent>
                  
                  <TabsContent value="kanji" className="mt-0 focus-visible:outline-none">
                    <div className="whitespace-pre-line text-2xl font-japanese text-slate-900 leading-loose">
                      {song.lyrics?.kanji || "Kanji text not available yet."}
                    </div>
                  </TabsContent>
                </Tabs>

                {song.lyrics?.notes && (
                  <div className="mt-12 pt-8 border-t border-slate-200">
                    <h4 className="text-slate-400 font-black uppercase tracking-widest text-xs mb-4 flex items-center gap-2">
                      <FileText size={14} /> Translator's Notes
                    </h4>
                    <p className="text-slate-600 font-medium leading-relaxed bg-white p-6 rounded-2xl border border-slate-100">
                      {song.lyrics.notes}
                    </p>
                  </div>
                )}
              </div>
            )}
          </div>

          {/* YouTube comments redirection */}
          <div className="mt-8 pt-6 border-t border-slate-100">
            <p className="text-slate-500 font-bold text-sm">
              Comments... &rarr;{' '}
              <a 
                href={song.youtubeUrl || `https://youtu.be/${song.youtubeVideoId}`} 
                target="_blank" 
                rel="noopener noreferrer" 
                className="text-[#ff8c00] hover:underline"
              >
                Link Youtube เม้นท์ที่นี่ โผล่ใน YT ด้วย
              </a>
            </p>
          </div>
        </div>

        {/* Right Side: Related Videos */}
        <div className="w-full lg:w-[340px] flex-shrink-0 flex flex-col">
          <h3 className="text-slate-950 font-black uppercase tracking-wider text-xs mb-6 border-b border-slate-100 pb-3">
            Related Translations
          </h3>
          <div className="flex flex-col gap-6">
            {relatedSongs.map(related => {
              const relParsed = parseSongTitle(related.title);
              return (
                <Link 
                  key={related.id} 
                  to={`/songs/${related.slug}`} 
                  className="flex items-center gap-4 group p-2 hover:bg-slate-50 rounded-2xl transition-all"
                >
                  <div className="w-28 md:w-32 aspect-video overflow-hidden rounded-xl bg-slate-100 flex-shrink-0 shadow-sm">
                    <img 
                      src={related.thumbnailUrl || '/assets/songs-hero.jpg'} 
                      alt={relParsed.title} 
                      className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                    />
                  </div>
                  <div className="flex-1 min-w-0">
                    <h4 className="text-slate-800 font-bold group-hover:text-[#ff8c00] transition-colors leading-tight text-sm line-clamp-3">
                      {related.title}
                    </h4>
                  </div>
                </Link>
              );
            })}
            {relatedSongs.length === 0 && (
              <p className="text-slate-400 font-bold text-sm italic">
                No related translations found.
              </p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default SongDetailPage;

