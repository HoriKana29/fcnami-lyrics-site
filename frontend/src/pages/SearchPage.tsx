import { useEffect, useState, useMemo } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { Search, Music, ChevronRight, Loader2, ArrowLeft, Filter, SortAsc, Tag, X } from 'lucide-react';
import { songService } from '@/services/api';
import { SongResponse } from '@/types';
import Navbar from '@/components/layout/Navbar';

type SortMethod = 'newest' | 'oldest' | 'views' | 'title';
type PlaylistFilter = 'all' | 'long-play' | 'requested' | 'bandori' | 'recommend';

const AVAILABLE_TAGS = [
  'Anime', 'J-Pop', 'Rhythm Game', 'Vocaloid', 'Uplifting', 'Emotional', 'Rock', 'Study'
];

const SearchPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [songs, setSongs] = useState<SongResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [syncing, setSyncing] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  
  // State for manual overrides
  const [activeSort, setActiveSort] = useState<SortMethod>('newest');
  const [activeFilter, setActiveFilter] = useState<PlaylistFilter>('all');
  const [selectedTags, setSelectedTags] = useState<string[]>([]);

  // Initial setup from URL
  useEffect(() => {
    const sortParam = searchParams.get('sort');
    const playlistParam = searchParams.get('playlist');

    if (sortParam === 'views') setActiveSort('views');
    
    if (playlistParam === 'long-play') setActiveFilter('long-play');
    else if (playlistParam === 'requested') setActiveFilter('requested');
    else if (playlistParam === 'bandori') setActiveFilter('bandori');
    else if (playlistParam === 'recommend') setActiveFilter('recommend');
    else setActiveFilter('all');
  }, [searchParams]);

  const fetchSongs = async () => {
    try {
      setLoading(true);
      const data = await songService.getSongs(searchTerm);
      setSongs(data.content);
    } catch (error) {
      console.error('Failed to search songs', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const performInitialSync = async () => {
      try {
        setSyncing(true);
        // Only sync if no search term or explicitly needed
        // For demo/prototype, we sync once on mount
        await songService.syncYouTube();
        await fetchSongs();
      } catch (error) {
        console.error('Failed to sync YouTube videos', error);
        await fetchSongs();
      } finally {
        setSyncing(false);
      }
    };

    performInitialSync();
  }, []);

  useEffect(() => {
    const debounce = setTimeout(fetchSongs, 300);
    return () => clearTimeout(debounce);
  }, [searchTerm]);

  // Derived filtered and sorted list
  const processedSongs = useMemo(() => {
    let result = [...songs];

    // 1. Filter by Playlist
    if (activeFilter !== 'all') {
      result = result.filter(s => s.tags?.includes(activeFilter));
    }

    // 2. Filter by Manual Tags
    if (selectedTags.length > 0) {
      result = result.filter(s => 
        selectedTags.every(tag => s.tags?.map(t => t.toLowerCase()).includes(tag.toLowerCase()))
      );
    }

    // 3. Sort Logic
    result.sort((a, b) => {
      if (activeSort === 'views') {
        return (b.id % 100) - (a.id % 100); 
      }
      if (activeSort === 'title') {
        return a.title.localeCompare(b.title);
      }
      
      const dateA = new Date(a.publishedAt || a.createdAt).getTime();
      const dateB = new Date(b.publishedAt || b.createdAt).getTime();
      
      if (activeSort === 'oldest') {
        return dateA - dateB;
      }
      return dateB - dateA;
    });

    return result;
  }, [songs, activeFilter, activeSort, selectedTags]);

  const getPageTitle = () => {
    if (activeFilter === 'long-play') return 'Long Tracks';
    if (activeFilter === 'requested') return 'Requested Songs';
    if (activeFilter === 'bandori') return 'Bandori Music';
    if (activeFilter === 'recommend') return 'Recommend';
    if (activeSort === 'views') return 'Popular Songs';
    return 'Search Results';
  };

  const updateUrl = (newFilter: PlaylistFilter, newSort: SortMethod) => {
    const params: any = {};
    if (newFilter !== 'all') params.playlist = newFilter;
    if (newSort === 'views') params.sort = 'views';
    setSearchParams(params);
  };

  const toggleTag = (tag: string) => {
    setSelectedTags(prev => 
      prev.includes(tag) ? prev.filter(t => t !== tag) : [...prev, tag]
    );
  };

  return (
    <div className="bg-white min-h-screen">
      <Navbar />
      
      <div className="flex flex-col md:flex-row min-h-screen pt-16">
        {/* Left Side: Search & Results */}
        <div className="flex-1 px-8 md:px-20 py-12 flex flex-col">
          <header className="mb-8">
            <Link to="/songs" className="inline-flex items-center gap-2 text-slate-400 hover:text-[#ff8c00] font-bold text-sm uppercase tracking-widest mb-6 transition-colors group">
              <ArrowLeft size={16} className="group-hover:-translate-x-1 transition-transform" /> Back to Categories
            </Link>
            <div className="flex items-end justify-between gap-4 mb-4">
              <h1 className="text-5xl md:text-7xl font-black tracking-tighter leading-none">
                <span className="text-[#ef6c00]">{getPageTitle()}</span>
              </h1>
              {syncing && (
                <div className="flex items-center gap-2 text-[#ff8c00] font-bold text-xs uppercase tracking-widest animate-pulse">
                  <Loader2 className="animate-spin" size={14} /> Syncing
                </div>
              )}
            </div>
            <p className="text-slate-400 font-medium italic">
              Showing {processedSongs.length} translations found for you
            </p>
          </header>

          {/* Controls Bar */}
          <div className="flex flex-col gap-6 mb-10">
            {/* Search Input */}
            <div className="relative group">
              <Search className="absolute left-5 top-1/2 -translate-y-1/2 text-slate-300 group-focus-within:text-[#ff8c00] transition-colors" size={22} />
              <input
                type="text"
                placeholder="Search songs, artists..."
                className="w-full bg-slate-50 border-2 border-slate-50 rounded-3xl py-5 pl-14 pr-6 text-slate-900 font-bold text-lg focus:outline-none focus:border-[#ff8c00]/30 focus:bg-white transition-all shadow-sm focus:shadow-md"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>

            {/* Manual Filters & Sorts */}
            <div className="flex flex-wrap items-center gap-4">
              <div className="flex items-center gap-3 bg-slate-50 p-2 rounded-2xl border border-slate-100 shadow-sm">
                <div className="bg-white p-2 rounded-xl text-slate-400 shadow-sm">
                  <Filter size={18} />
                </div>
                <select 
                  className="bg-transparent font-bold text-slate-700 outline-none pr-4 cursor-pointer"
                  value={activeFilter}
                  onChange={(e) => {
                    const val = e.target.value as PlaylistFilter;
                    setActiveFilter(val);
                    updateUrl(val, activeSort);
                  }}
                >
                  <option value="all">All Categories</option>
                  <option value="recommend">Recommend</option>
                  <option value="long-play">Long Track</option>
                  <option value="requested">Requested Song</option>
                  <option value="bandori">Bandori Music</option>
                </select>
              </div>

              <div className="flex items-center gap-3 bg-slate-50 p-2 rounded-2xl border border-slate-100 shadow-sm">
                <div className="bg-white p-2 rounded-xl text-slate-400 shadow-sm">
                  <SortAsc size={18} />
                </div>
                <select 
                  className="bg-transparent font-bold text-slate-700 outline-none pr-4 cursor-pointer"
                  value={activeSort}
                  onChange={(e) => {
                    const val = e.target.value as SortMethod;
                    setActiveSort(val);
                    updateUrl(activeFilter, val);
                  }}
                >
                  <option value="newest">Newest First</option>
                  <option value="oldest">Oldest First</option>
                  <option value="views">Most Popular</option>
                  <option value="title">Alphabetical</option>
                </select>
              </div>

              {(activeFilter !== 'all' || activeSort !== 'newest' || searchTerm || selectedTags.length > 0) && (
                <button 
                  onClick={() => {
                    setActiveFilter('all');
                    setActiveSort('newest');
                    setSearchTerm('');
                    setSelectedTags([]);
                    setSearchParams({});
                  }}
                  className="text-[#ff8c00] font-black text-sm uppercase tracking-widest hover:bg-orange-50 px-4 py-2 rounded-xl transition-colors"
                >
                  Reset All
                </button>
              )}
            </div>

            {/* Tag Selection */}
            <div className="flex flex-col gap-3">
              <div className="flex items-center gap-2 text-slate-400 font-bold text-xs uppercase tracking-widest px-2">
                <Tag size={14} /> Filter by Tags
              </div>
              <div className="flex flex-wrap gap-2">
                {AVAILABLE_TAGS.map(tag => (
                  <button
                    key={tag}
                    onClick={() => toggleTag(tag)}
                    className={`px-4 py-2 rounded-full text-sm font-bold transition-all border-2 ${
                      selectedTags.includes(tag)
                        ? 'bg-[#ff8c00] border-[#ff8c00] text-white shadow-lg shadow-orange-200'
                        : 'bg-white border-slate-100 text-slate-500 hover:border-[#ff8c00]/30 hover:text-[#ef6c00]'
                    }`}
                  >
                    {tag}
                  </button>
                ))}
              </div>
            </div>
          </div>

          {/* Results List */}
          <div className="flex-1 overflow-y-auto pr-2 custom-scrollbar">
            {loading && !syncing ? (
              <div className="flex flex-col items-center justify-center py-20 gap-4">
                <Loader2 className="animate-spin text-[#ff8c00]" size={48} />
                <p className="text-slate-400 font-bold animate-pulse">Loading songs...</p>
              </div>
            ) : processedSongs.length > 0 ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {processedSongs.map((song) => (
                  <Link
                    key={song.id}
                    to={`/songs/${song.slug}`}
                    className="flex flex-col bg-white border-2 border-slate-50 hover:border-[#ff8c00]/20 rounded-[2.5rem] transition-all group hover:shadow-2xl hover:shadow-orange-100/30 overflow-hidden"
                  >
                    <div className="relative aspect-video overflow-hidden">
                      {song.thumbnailUrl ? (
                        <img 
                          src={song.thumbnailUrl} 
                          alt={song.title}
                          className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700"
                        />
                      ) : (
                        <div className="w-full h-full bg-slate-100 flex items-center justify-center text-slate-300">
                          <Music size={48} />
                        </div>
                      )}
                      <div className="absolute inset-0 bg-gradient-to-t from-slate-900/60 to-transparent opacity-0 group-hover:opacity-100 transition-opacity flex items-end p-6">
                         <span className="text-white font-black text-sm uppercase tracking-widest flex items-center gap-2">
                           View Lyrics <ChevronRight size={16} />
                         </span>
                      </div>
                      {song.status === 'PUBLISHED' && (
                        <div className="absolute top-4 right-4 bg-green-500 text-white text-[10px] font-black px-3 py-1 rounded-full shadow-lg uppercase tracking-tighter">
                          Published
                        </div>
                      )}
                    </div>
                    
                    <div className="p-6 flex flex-col flex-1">
                      <div className="flex flex-wrap gap-1.5 mb-3">
                         {song.tags?.slice(0, 2).map(tag => (
                           <span key={tag} className="text-[9px] font-black uppercase tracking-tighter bg-slate-50 text-slate-400 px-2 py-0.5 rounded-md">
                             {tag}
                           </span>
                         ))}
                      </div>
                      
                      <h3 className="text-xl font-black text-slate-900 leading-tight group-hover:text-[#ef6c00] transition-colors line-clamp-2 mb-2">
                        {song.title}
                      </h3>
                      
                      <div className="mt-auto pt-4 flex items-center justify-between border-t border-slate-50">
                        <div className="flex flex-col">
                           <span className="text-slate-400 font-bold text-[10px] uppercase tracking-widest truncate max-w-[120px]">
                             {song.artist}
                           </span>
                           <span className="text-[#ff8c00] font-black text-[10px] uppercase tracking-widest truncate max-w-[120px]">
                             {song.sourceAnimeOrGame}
                           </span>
                        </div>
                        <div className="w-10 h-10 rounded-2xl bg-slate-50 text-slate-300 group-hover:bg-[#ff8c00] group-hover:text-white transition-all flex items-center justify-center shadow-inner group-hover:shadow-lg group-hover:shadow-orange-200">
                          <Music size={20} />
                        </div>
                      </div>
                    </div>
                  </Link>
                ))}
              </div>
            ) : syncing ? (
              <div className="flex flex-col items-center justify-center py-20 gap-4">
                <Loader2 className="animate-spin text-[#ff8c00]" size={48} />
                <p className="text-slate-400 font-bold animate-pulse text-center">
                  Fetching latest videos from FCNami T_T...<br/>
                  <span className="text-xs font-medium italic">Please wait while we sync the catalog</span>
                </p>
              </div>
            ) : (
              <div className="text-center py-20 px-10 bg-slate-50/50 rounded-[3rem] border-2 border-dashed border-slate-100 flex flex-col items-center">
                <div className="w-20 h-20 bg-white rounded-3xl flex items-center justify-center text-slate-200 mb-6 shadow-sm">
                  <Search size={40} />
                </div>
                <h3 className="text-xl font-black text-slate-900 mb-2">No results found</h3>
                <p className="text-slate-400 font-medium max-w-xs mx-auto">
                  Try adjusting your search terms or filters.
                </p>
              </div>
            )}
          </div>
        </div>

        {/* Right Side: Visual Section */}
        <div className="md:w-[42%] relative h-[30vh] md:h-screen">
          <div className="sticky top-0 h-full w-full overflow-hidden">
            <img 
              src="/assets/search-hero.jpg" 
              alt="Search Context Visual" 
              className="w-full h-full object-cover object-left"
            />
            <div className="absolute inset-0 bg-gradient-to-l from-transparent via-transparent to-white/10 md:to-white/30 pointer-events-none" />
            
            <div className="absolute bottom-12 right-12 bg-white/10 backdrop-blur-md border border-white/20 p-6 rounded-3xl hidden lg:block max-w-[240px]">
              <p className="text-white font-black text-lg leading-tight mb-2">
                "Keep listening, stay inspired."
              </p>
              <div className="w-12 h-1 bg-[#ff8c00] rounded-full" />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SearchPage;
