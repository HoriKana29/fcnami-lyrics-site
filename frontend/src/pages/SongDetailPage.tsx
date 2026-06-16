import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Loader2, FileText, ExternalLink } from 'lucide-react';
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

  useEffect(() => {
    const fetchSong = async () => {
      if (!slug) return;
      try {
        setLoading(true);
        const data = await songService.getSongBySlug(slug);
        setSong(data);
      } catch (error) {
        console.error('Failed to fetch song detail', error);
        // navigate('/songs'); // Redirect if not found
      } finally {
        setLoading(false);
      }
    };

    fetchSong();
  }, [slug, navigate]);

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
    <div className="bg-white min-h-screen">
      <Navbar />
      
      <div className="flex flex-col md:flex-row min-h-screen pt-16">
        {/* Left Side: Content */}
        <div className="flex-1 px-8 md:px-20 py-12 flex flex-col overflow-y-auto custom-scrollbar">
          <header className="mb-8">
            <button 
              onClick={() => navigate(-1)} 
              className="inline-flex items-center gap-2 text-slate-400 hover:text-[#ff8c00] font-bold text-sm uppercase tracking-widest mb-6 transition-colors group"
            >
              <ArrowLeft size={16} className="group-hover:-translate-x-1 transition-transform" /> Back
            </button>
            
            <div className="flex flex-wrap items-center gap-3 mb-4">
               {song.tags?.map(tag => (
                 <span key={tag} className="bg-slate-100 text-slate-500 px-3 py-1 rounded-lg text-[10px] font-black uppercase tracking-tighter">
                   {tag}
                 </span>
               ))}
            </div>

            <h1 className="text-4xl md:text-6xl font-black tracking-tighter leading-tight mb-4">
              <span className="text-slate-900">{parsed.title}</span>
            </h1>
            <div className="flex flex-wrap items-center gap-x-4 gap-y-2 text-xl font-bold">
               <span className="text-[#ff8c00] text-2xl md:text-3xl font-black uppercase tracking-tight">
                 {parsed.coveredBy ? `Cover by ${parsed.coveredBy}` : (parsed.artist && parsed.artist !== 'FCNami T_T' ? parsed.artist : song.artist)}
               </span>
               {(parsed.source || (song.sourceAnimeOrGame && song.sourceAnimeOrGame !== 'YouTube')) && (
                 <>
                   <span className="hidden md:block w-1.5 h-1.5 bg-slate-200 rounded-full" />
                   <span className="text-slate-400 uppercase tracking-wide">
                     {parsed.source || song.sourceAnimeOrGame}
                   </span>
                 </>
               )}
            </div>
          </header>

          {/* Video Player Section */}
          <div className="mb-12 aspect-video rounded-3xl overflow-hidden bg-slate-900 shadow-2xl shadow-orange-100/50 group relative">
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

          {/* Lyrics Tabs */}
          <div className="bg-slate-50 rounded-[2.5rem] p-8 md:p-12 border-2 border-slate-50">
            <Tabs defaultValue="thai" className="w-full">
              <TabsList className="bg-white/50 p-1 rounded-2xl mb-8 border border-slate-100">
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
                  {song.lyrics.thai || "Thai translation not available yet."}
                </div>
              </TabsContent>
              
              <TabsContent value="romaji" className="mt-0 focus-visible:outline-none">
                <div className="whitespace-pre-line text-lg md:text-xl font-medium text-slate-600 leading-relaxed italic">
                  {song.lyrics.romaji || "Romaji text not available yet."}
                </div>
              </TabsContent>
              
              <TabsContent value="kanji" className="mt-0 focus-visible:outline-none">
                <div className="whitespace-pre-line text-2xl font-japanese text-slate-900 leading-loose">
                  {song.lyrics.kanji || "Kanji text not available yet."}
                </div>
              </TabsContent>
            </Tabs>

            {song.lyrics.notes && (
              <div className="mt-12 pt-8 border-t border-slate-200">
                <h4 className="text-slate-400 font-black uppercase tracking-widest text-xs mb-4 flex items-center gap-2">
                   <FileText size={14} /> Translator's Notes
                </h4>
                <p className="text-slate-600 font-medium leading-relaxed bg-white/40 p-6 rounded-2xl border border-slate-100">
                  {song.lyrics.notes}
                </p>
              </div>
            )}
          </div>

          <div className="mt-12 flex justify-center">
            <a 
              href={song.youtubeUrl} 
              target="_blank" 
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 bg-slate-900 text-white px-8 py-4 rounded-2xl font-black uppercase tracking-widest text-sm hover:bg-[#ff8c00] transition-all hover:scale-105 active:scale-95 shadow-xl shadow-slate-200"
            >
              Watch on YouTube <ExternalLink size={18} />
            </a>
          </div>
        </div>

        {/* Right Side: Visual */}
        <div className="md:w-[40%] relative h-[30vh] md:h-screen">
          <div className="sticky top-0 h-full w-full overflow-hidden">
            <img 
              src="/assets/detail-hero.jpg" 
              alt="Song Detail Visual" 
              className="w-full h-full object-cover object-center"
            />
            <div className="absolute inset-0 bg-gradient-to-l from-transparent via-transparent to-white/10 md:to-white/30 pointer-events-none" />
          </div>
        </div>
      </div>
    </div>
  );
};

export default SongDetailPage;
